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

package com.microsoft.edc.showcase.connector;

import org.eclipse.dataspaceconnector.system.runtime.BaseRuntime;

public class Runtime extends BaseRuntime {

    public static void main(String[] args) {
        var runtime = new Runtime();
        runtime.boot();

    }

}
