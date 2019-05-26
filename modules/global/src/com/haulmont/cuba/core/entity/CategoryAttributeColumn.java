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

package com.haulmont.cuba.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.Entity;
import javax.persistence.*;

@Entity(name = "sys$CategoryAttributeColumn")
@Table(name = "SYS_CATEGORY_COLUMN")
//@NamePattern("%s|localeName") todo
@NamePattern("%s|name")
@SystemLevel
public class CategoryAttributeColumn extends StandardEntity {

    private static final long serialVersionUID = -1959592228534115702L;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @Column(name = "NAME", length = 255, nullable = false)
    private String name;

    @Column(name = "CODE", length = 50, nullable = false)
    private String code;

    @Column(name = "FORMAT", length = 500, nullable = false)
    private String format;

    @Column(name = "WIDTH", length = 20)
    private String width;

    @Column(name = "ORDER_NO")
    private Integer orderNo;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }
}
