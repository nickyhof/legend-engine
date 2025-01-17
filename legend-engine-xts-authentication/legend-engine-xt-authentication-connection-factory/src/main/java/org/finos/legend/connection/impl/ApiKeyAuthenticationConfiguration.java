// Copyright 2021 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.connection.impl;

import org.finos.legend.connection.protocol.AuthenticationConfiguration;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault.CredentialVaultSecret;

public class ApiKeyAuthenticationConfiguration extends AuthenticationConfiguration
{
    public Location location;
    public String keyName;
    public CredentialVaultSecret value;

    public ApiKeyAuthenticationConfiguration()
    {
    }

    public ApiKeyAuthenticationConfiguration(String location, String keyName, CredentialVaultSecret value)
    {
        this.location = Location.valueOf(location.toUpperCase());
        this.keyName = keyName;
        this.value = value;
    }

    public enum Location
    {
        HEADER,
        COOKIE
    }
}
