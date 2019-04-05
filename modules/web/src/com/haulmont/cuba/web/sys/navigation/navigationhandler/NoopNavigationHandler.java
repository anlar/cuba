/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.web.sys.navigation.navigationhandler;

import com.haulmont.cuba.gui.navigation.NavigationState;
import com.haulmont.cuba.web.AppUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Order(NavigationHandler.LOWEST_PLATFORM_PRECEDENCE - 10)
public class NoopNavigationHandler extends AbstractNavigationHandler implements NavigationHandler {

    private static final Logger log = LoggerFactory.getLogger(NoopNavigationHandler.class);

    @Override
    public boolean doHandle(NavigationState requestedState, AppUI ui) {
        log.info("Failed to handle a route: '{}'", requestedState.asRoute());
        revertNavigationState(ui);

        return false;
    }
}
