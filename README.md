# The `ion-demo` application
Demo Application to show how the EDC and ION can be used together to implement distributed identities

_This document describes the working concept rather than the finished application._

## Setup
- create 3 Keypairs, one for each connector, one for the Verifier
- pre-define two Hub URLs (ideally they should look exactly how ACI URLs or AKS URLs are generated)
- for each connector:
  + generate a DID Document containing the Public Key and its Hub URL on ION
  + generate a JWT on every request (signed with connectors Private Key) containing the DID URL as claim (=payload) and an expiration date (t+5min)
- for the Verifier (="accenture"): put a DID with it's public key on ION

## Deployment
for each connector:
  + one AKS cluster
  + 2 pods: connector + hub
  + ??? a database for each hub to contain object data

## Data seeding

- the hubs get their "additional data object" data seeded.
- additional data objects are again JWEs signed with the Verifier's private key. Each data property is a claim, complex
  properties should just be JSON strings

## Verification process

The following sequence has to be performed during reception of every request:

A ... Consumer, B ... Provider

1. A presents JWT to B (in message header, i.e. in the `_securityToken_` field of a IDS messages)
1. B resolves the DID URL from the VC received from A
1. B resolves A's DID Document from ION and from it retrieves A's public key and A's Hub URL
1. B verifies A's VC using A's public key (from the DID Document)
1. B obtains object data from A's Hub:
    - B sends query to A's hub together with its public key
    - A decrypts hub data object and re-encrypts with B's public key
    - B receives and decrypts A's hub data object
1. B obtains the Verifier's DID document from ION (Verifier's DID URL must be contained in the hub data object)
1. B uses Verifier's public key to verify A's object data

## Terminology
- Verifiable Credential and JWT are the same thing. Or: a VC is represented by a JWT
- the Verifier is a trusted third party, e.g. some company like Accenture used to verify additional object data
- "(additional) object data" refers to an arbitrary set of properties or a JSON structure that are stored in a connector's Hub
- Hub and Identity Hub are the same thing
- DID and DID Document are the same thing
- VerifiableAssertion = hub data object

## General notes and restrictions
- The Verifier (or Attestator) in this demo is just another Key Pair
- DIDs are generated and anchored once during initial setup, it does **not** happen during deployment
- The will be one set of object data per hub and one hub per connector (so no filtering at this time)
- The hub runs in its separate runtime and exposes a simple GET API
- connectors **must** have a unique and stable ID
