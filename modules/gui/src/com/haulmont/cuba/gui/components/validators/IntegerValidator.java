/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.gui.components.validators;

import com.haulmont.chile.core.datatypes.Datatype;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import org.dom4j.Element;

import java.text.ParseException;

public class IntegerValidator extends NumberValidator {

    public IntegerValidator(Element element, String messagesPack) {
        super(element, messagesPack);
    }

    public IntegerValidator(String message) {
        super(message);
    }

    public IntegerValidator() {
    }

    public IntegerValidator(Integer min, Integer max) {
        super(min, max);
    }

    @Override
    protected Number parse(Object value) throws UnsupportedOperationException {
        if (value instanceof Integer) {
            return (Number) value;
        } else if (value instanceof String) {
            try {
                Datatype<Integer> datatype = Datatypes.getNN(Integer.class);
                UserSessionSource sessionSource = AppBeans.get(UserSessionSource.NAME);
                return datatype.parse((String) value, sessionSource.getLocale());
            } catch (ParseException e) {
                throw new UnsupportedOperationException(e);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected boolean checkOnPositive(Number value) {
        return (Integer) value >= 0;
    }

    @Override
    protected int compareNumbers(Number first, Number second) {
        return Integer.compare((Integer) first, (Integer) second);
    }
}