/*
 * Copyright (c) 2008-2017 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.web.auth;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultBoolean;
import com.haulmont.cuba.core.config.defaults.DefaultString;
import com.haulmont.cuba.core.config.type.CommaSeparatedStringListTypeFactory;
import com.haulmont.cuba.core.config.type.Factory;
import com.haulmont.cuba.security.app.TrustedClientService;

import java.util.List;

@Source(type = SourceType.APP)
public interface WebAuthConfig extends Config {
    /**
     * @return true if password is mandatory for new users
     */
    @Property("cuba.web.requirePasswordForNewUsers")
    @DefaultBoolean(true)
    boolean getRequirePasswordForNewUsers();

    /**
     * @return list of users that are not allowed to use external authentication. They can use only standard authentication.
     *         Empty list means that everyone is allowed to login using external authentication.
     */
    @Property("cuba.web.standardAuthenticationUsers")
    @Factory(factory = CommaSeparatedStringListTypeFactory.class)
    List<String> getStandardAuthenticationUsers();

    /**
     * @return Password used by {@link TrustedClientService} method.
     * Trusted client may login without providing a user password. This is used for external authentication.
     *
     * <p>Must be equal to password set for the same property on the CORE.</p>
     */
    @Property("cuba.trustedClientPassword")
    @DefaultString("")
    String getTrustedClientPassword();

    /**
     * @return Whether to use an login/password authentication on client
     * instead of login/password authentication on middleware.
     */
    @Property("cuba.checkPasswordOnClient")
    @DefaultBoolean(false)
    boolean getCheckPasswordOnClient();
}