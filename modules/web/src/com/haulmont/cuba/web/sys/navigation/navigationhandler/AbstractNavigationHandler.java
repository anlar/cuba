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

import com.haulmont.cuba.core.global.AccessDeniedException;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.navigation.NavigationState;
import com.haulmont.cuba.gui.screen.EditorScreen;
import com.haulmont.cuba.gui.screen.FrameOwner;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.security.entity.PermissionType;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.sys.navigation.UrlChangeHandler;
import com.haulmont.cuba.web.sys.navigation.accessfilter.NavigationFilter;
import org.apache.commons.lang3.StringUtils;

import static com.haulmont.cuba.web.sys.navigation.UrlTools.pushState;

public abstract class AbstractNavigationHandler implements NavigationHandler {

    protected boolean isEmptyState(NavigationState requestedState) {
        return requestedState == null || requestedState == NavigationState.EMPTY;
    }

    protected void revertNavigationState(AppUI ui) {
        UrlChangeHandler urlChangeHandler = ui.getUrlChangeHandler();
        Screen screen = urlChangeHandler.findActiveScreenByState(ui.getHistory().getNow());
        if (screen == null) {
            screen = urlChangeHandler.getActiveScreen();
        }

        pushState(urlChangeHandler.getResolvedState(screen).asRoute());
    }

    protected boolean isRootRoute(WindowInfo windowInfo) {
        return windowInfo != null && windowInfo.getRouteDefinition().isRoot();
    }

    protected boolean isNotPermittedToNavigate(NavigationState requestedState, WindowInfo windowInfo,
                                               Security security, AppUI ui) {
        boolean screenPermitted = security.isScreenPermitted(windowInfo.getId());
        if (!screenPermitted) {
            revertNavigationState(ui);
            throw new AccessDeniedException(PermissionType.SCREEN, windowInfo.getId());
        }

        UrlChangeHandler owner = ui.getUrlChangeHandler();

        NavigationFilter.AccessCheckResult result = owner.navigationAllowed(requestedState);
        if (result.isRejected()) {
            if (StringUtils.isNotEmpty(result.getMessage())) {
                owner.showNotification(result.getMessage());
            }
            revertNavigationState(ui);

            return true;
        }
        return false;
    }

    protected boolean isEditor(WindowInfo windowInfo) {
        return EditorScreen.class.isAssignableFrom(windowInfo.getControllerClass());
    }

    protected boolean isLegacyScreen(Class<? extends FrameOwner> controllerClass) {
        return LegacyFrame.class.isAssignableFrom(controllerClass);
    }
}
