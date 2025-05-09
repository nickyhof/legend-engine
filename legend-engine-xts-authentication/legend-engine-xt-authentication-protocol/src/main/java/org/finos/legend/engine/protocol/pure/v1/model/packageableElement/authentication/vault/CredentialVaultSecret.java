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

package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.vault;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.finos.legend.engine.protocol.pure.m3.SourceInformation;
import org.finos.legend.engine.protocol.pure.m3.PackageableElement;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.PackageableElementVisitor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
public abstract class CredentialVaultSecret extends PackageableElement
{
    public SourceInformation sourceInformation;

    public CredentialVaultSecret()
    {
        // jackson
    }

    // TODO: @akphi - turn this into abstract, this default implementation is meant for backward compatibility
    public String shortId()
    {
        return "CredentialVaultSecret";
    }

    public <T> T accept(PackageableElementVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

    public abstract <T> T accept(CredentialVaultSecretVisitor<T> visitor);
}