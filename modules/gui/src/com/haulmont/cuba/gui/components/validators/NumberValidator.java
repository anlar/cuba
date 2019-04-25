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

package com.haulmont.cuba.gui.components.validators;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.ValidationException;
import org.dom4j.Element;

import java.util.Objects;

public abstract class NumberValidator implements Field.Validator {

    protected String message;
    protected String messagesPack;
    protected String onlyPositive;

    protected Number minValue;
    protected Number maxValue;

    protected Messages messages = AppBeans.get(Messages.NAME);

    public NumberValidator(Element element, String messagesPack) {
        this.message = element.attributeValue("message");
        this.onlyPositive = element.attributeValue("onlyPositive");
        this.messagesPack = messagesPack;
    }

    public NumberValidator(String message) {
        this.message = message;
    }

    public NumberValidator() {
        this.message = messages.getMainMessage("validation.invalidNumber");
    }

    public NumberValidator(Number minValue, Number maxValue) {
        this();
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public void validate(Object value) throws ValidationException {
        boolean result = true;
        Number number = null;

        try {
            number = parse(value);
        } catch (UnsupportedOperationException e) {
            result = false;
        }

        if (result) {
            result = !checkOnPositiveEnabled() || checkOnPositive(number);
        }

        if (!result) {
            String msg = message != null ? messages.getTools().loadString(messagesPack, message) : "Invalid value '%s'";
            throw new ValidationException(String.format(msg, value));
        }

        if (number != null) {
            if (minValue != null && compareNumbers(number, minValue) < 0) {
                throw new ValidationException(messages.formatMainMessage("validation.invalidNumberMin", minValue));
            }

            if (maxValue != null && compareNumbers(number, maxValue) > 0) {
                throw new ValidationException(messages.formatMainMessage("validation.invalidNumberMax", maxValue));
            }
        }
    }

    protected boolean checkOnPositiveEnabled() {
        return Objects.equals("true", onlyPositive);
    }

    protected abstract Number parse(Object value) throws UnsupportedOperationException;

    protected abstract boolean checkOnPositive(Number value);

    protected abstract int compareNumbers(Number first, Number second);
}
