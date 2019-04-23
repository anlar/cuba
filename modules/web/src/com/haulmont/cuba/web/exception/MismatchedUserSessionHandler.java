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

package com.haulmont.cuba.web.exception;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.security.global.MismatchedUserSessionException;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.gui.icons.IconResolver;
import com.haulmont.cuba.web.widgets.CubaButton;
import com.haulmont.cuba.web.widgets.CubaLabel;
import com.haulmont.cuba.web.widgets.CubaWindow;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.haulmont.cuba.web.gui.components.WebComponentsHelper.setClickShortcut;

/**
 * Handles {@link MismatchedUserSessionException}.
 */
public class MismatchedUserSessionHandler extends AbstractExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MismatchedUserSessionHandler.class);

    protected final Locale locale;

    public MismatchedUserSessionHandler() {
        super(MismatchedUserSessionException.class.getName());

        Connection connection = App.getInstance().getConnection();
        //noinspection ConstantConditions
        locale = connection.getSession().getLocale();
    }

    @Override
    protected void doHandle(App app, String className, String message, @Nullable Throwable throwable) {
        try {
            AppUI ui = AppUI.getCurrent();

            closeAllDialogs(ui);
            showMismatchedSessionDialog(ui);
        } catch (Throwable th) {
            log.error("Unable to handle MismatchedUserSessionException", throwable);
            log.error("Exception in MismatchedUserSessionHandler", th);
        }
    }

    // we may show two or more dialogs if user pressed F5 and we have no valid user session
    // just remove previous dialog and show new
    protected void closeAllDialogs(AppUI ui) {
        List<Window> changedSessionDialogs = ui.getWindows()
                .stream()
                .filter(w -> w instanceof MismatchedUserSessionExceptionDialog)
                .collect(Collectors.toList());

        for (Window dialog : changedSessionDialogs) {
            ui.removeWindow(dialog);
        }
    }

    protected void showMismatchedSessionDialog(AppUI ui) {
        Messages messages = AppBeans.get(Messages.NAME);

        Window dialog = new MismatchedUserSessionExceptionDialog();
        dialog.setStyleName("c-sessionchanged-dialog");
        dialog.setCaption(messages.getMainMessage("dialogs.Information", locale));
        dialog.setClosable(false);
        dialog.setResizable(false);
        dialog.setModal(true);

        CubaLabel messageLab = new CubaLabel();
        messageLab.setWidthUndefined();
        messageLab.setValue(messages.getMainMessage("sessionChangedMsg", locale));

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(false);
        layout.setWidthUndefined();
        layout.setStyleName("c-sessionchanged-dialog-layout");
        layout.setSpacing(true);
        dialog.setContent(layout);

        CubaButton reloginBtn = new CubaButton();
        reloginBtn.addStyleName("c-primary-action");
        reloginBtn.addClickListener(event -> refreshUi());
        reloginBtn.setCaption(messages.getMainMessage(DialogAction.Type.OK.getMsgKey(), locale));

        String iconName = AppBeans.get(Icons.class).get(DialogAction.Type.OK.getIconKey());
        reloginBtn.setIcon(AppBeans.get(IconResolver.class).getIconResource(iconName));

        ClientConfig clientConfig = AppBeans.get(Configuration.class).getConfig(ClientConfig.class);
        setClickShortcut(reloginBtn, clientConfig.getCommitShortcut());

        reloginBtn.focus();

        layout.addComponent(messageLab);
        layout.addComponent(reloginBtn);

        layout.setComponentAlignment(reloginBtn, Alignment.BOTTOM_RIGHT);

        ui.addWindow(dialog);

        dialog.center();

        if (ui.isTestMode()) {
            dialog.setCubaId("optionDialog");
            reloginBtn.setCubaId("reloginBtn");
        }

        if (ui.isPerformanceTestMode()) {
            dialog.setId(ui.getTestIdManager().getTestId("optionDialog"));
            reloginBtn.setId(ui.getTestIdManager().getTestId("reloginBtn"));
        }
    }

    protected void refreshUi() {
        AppUI currentUi = AppUI.getCurrent();
        App app = App.getInstance();

        closeAllDialogs(currentUi);

        currentUi.setUserSession(app.getConnection().getSession());

        refreshPage();

        app.createTopLevelWindow(currentUi);
    }

    protected void refreshPage() {
        Page.getCurrent().open(ControllerUtils.getLocationWithoutParams(), "_self");
    }

    public static class MismatchedUserSessionExceptionDialog extends CubaWindow {
    }
}
