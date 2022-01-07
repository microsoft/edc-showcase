/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package com.microsoft.edc.showcase.demo;

import org.eclipse.dataspaceconnector.core.system.runtime.BaseRuntime;

public class RegistrationServiceRuntime extends BaseRuntime {

    public static void main(String[] args) {
        new RegistrationServiceRuntime().boot();
    }

}
