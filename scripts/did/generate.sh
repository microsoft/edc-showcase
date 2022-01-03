#!/bin/bash

usage="$(basename "$0") [-h] [-e ENVIRONMENT] [-n NAME] [-r REGION] [-t TEMPLATE]
Generate private key and associated DID document for an entity (connector, registration service...). Options are:
    -h  show this help text
    -e  environment name (this is the one used to prefix all resources). This parameter is MANDATORY.
    -n  name of the entity. This parameter is MANDATORY.
    -r  region where resources are hosted. Default is: template/template.json.
    -t  template file used to generate the DID document. Default is: westeurope"

# COLLECT INPUT PARAMETERS
options=':he:n:r:t:'
while getopts $options option; do
  case "$option" in
    h) echo "$usage"; exit;;
    e) ENVIRONMENT=$OPTARG;;
    n) NAME=$OPTARG;;
    r) REGION=$OPTARG;;
    t) TEMPLATE=$OPTARG;;
    :) printf "missing argument for -%s\n" "$OPTARG" >&2; echo "$usage" >&2; exit 1;;
   \?) printf "illegal option: -%s\n" "$OPTARG" >&2; echo "$usage" >&2; exit 1;;
  esac
done

# CHECK MANDATORY PARAMETERS
if [ ! "$ENVIRONMENT" ] || [ ! "$NAME" ]; then
  echo "arguments -i and -v must be provided"
  echo "$usage" >&2; exit 1
fi

# SET DEFAULT PARAMETERS IF NOT SPECIFIED
if [ ! "$TEMPLATE" ]; then
  TEMPLATE="template/template.json"
fi
if [ ! "$REGION" ]; then
  REGION="westeurope"
fi

# FUNCTIONS
getValueByKey() {
  local JSON=$1
  local KEY=$2
  echo $(echo $JSON | sed 's|,|\n|g' \
            | grep "\"$KEY\"" \
            | cut -d ":" -f2- \
            | sed s/\"//g \
            | sed s/\}//g)
}

echo "Generate private key in file $NAME.pem"
openssl ecparam -name prime256v1 -genkey -noout -out $NAME.pem

echo "Generate the public key in JWK format"
readonly JWK=$(openssl ec -in $NAME.pem -pubout | docker run -i danedmunds/pem-to-jwk:latest --public)

readonly SANITIZED_ENVIRONMENT="${ENVIRONMENT//-}"

echo "Extract components from public key"
readonly X=$(getValueByKey $JWK "x")
readonly Y=$(getValueByKey $JWK "y")

#echo "JWK: $JWK"
#echo "X: $X"
#echo "Y: $Y"

echo "Generate DID document"
sed "s/{{ENVIRONMENT}}/$ENVIRONMENT/g" $TEMPLATE \
      | sed "s/{{NAME}}/$NAME/g" \
      | sed "s/{{ENVIRONMENT}}/$ENVIRONMENT/g" \
      | sed "s/{{REGION}}/$REGION/g" \
      | sed "s/{{X}}/$X/g" \
      | sed "s/{{Y}}/$Y/g" \
      | sed "s/{{SANITIZED_ENVIRONMENT}}/$SANITIZED_ENVIRONMENT/g" \
      | sed '/^\/\//d' > $NAME.json

echo "SUCCESS!"