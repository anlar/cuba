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

package com.haulmont.cuba.client.sys;

import com.haulmont.cuba.core.app.PersistenceManagerService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side caching proxy for the <code>PersistenceManagerService</code>.
 * <p>
 * Caches the PersistenceManager information for the whole life time of the client application.
 * The web-client's <code>Caching</code> MBean contains a method to clear this cache.
 * </p>
 */
@Component(PersistenceManagerClient.NAME)
@Primary
public class PersistenceManagerClient implements PersistenceManagerService {

    public static final String NAME = "cuba_PersistenceManagerClient";

    protected static class CacheEntry {
        Boolean useLazyCollection;
        Boolean useLookupScreen;
        Integer fetchUI;
        Integer maxFetchUI;
    }

    protected static class DbmsCacheEntry {
        boolean supportsLobSortingAndFiltering;
        boolean emulateEqualsByLike;
    }

    protected Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    protected Map<String, DbmsCacheEntry> dbmsCache = new ConcurrentHashMap<>();

    protected volatile String dbmsType;
    protected volatile String dbmsVersion;
    protected volatile String uniqueConstraintViolationPattern;
    protected volatile Boolean defaultNullSorting;

    @Inject
    protected PersistenceManagerService service;

    protected CacheEntry getCacheEntry(String entityName) {
        CacheEntry cacheEntry = cache.get(entityName);
        if (cacheEntry == null) {
            cacheEntry = new CacheEntry();
            cache.put(entityName, cacheEntry);
        }
        return cacheEntry;
    }

    @Override
    public boolean useLazyCollection(String entityName) {
        CacheEntry cacheEntry = getCacheEntry(entityName);
        if (cacheEntry.useLazyCollection == null) {
            cacheEntry.useLazyCollection = service.useLazyCollection(entityName);
        }
        return cacheEntry.useLazyCollection;
    }

    @Override
    public boolean useLookupScreen(String entityName) {
        CacheEntry cacheEntry = getCacheEntry(entityName);
        if (cacheEntry.useLookupScreen == null) {
            cacheEntry.useLookupScreen = service.useLookupScreen(entityName);
        }
        return cacheEntry.useLookupScreen;
    }

    @Override
    public int getFetchUI(String entityName) {
        CacheEntry cacheEntry = getCacheEntry(entityName);
        if (cacheEntry.fetchUI == null) {
            cacheEntry.fetchUI = service.getFetchUI(entityName);
        }
        return cacheEntry.fetchUI;
    }

    @Override
    public int getMaxFetchUI(String entityName) {
        CacheEntry cacheEntry = getCacheEntry(entityName);
        if (cacheEntry.maxFetchUI == null) {
            cacheEntry.maxFetchUI = service.getMaxFetchUI(entityName);
        }
        return cacheEntry.maxFetchUI;
    }

    @Override
    public String getDbmsType() {
        if (dbmsType == null)
            dbmsType = service.getDbmsType();
        return dbmsType;
    }

    @Override
    public String getDbmsVersion() {
        if (dbmsVersion == null)
            dbmsVersion = service.getDbmsVersion();
        return dbmsVersion;
    }

    @Override
    public String getUniqueConstraintViolationPattern() {
        if (uniqueConstraintViolationPattern == null)
            uniqueConstraintViolationPattern = service.getUniqueConstraintViolationPattern();
        return uniqueConstraintViolationPattern;
    }

    @Override
    public boolean isNullsLastSorting() {
        if (defaultNullSorting == null)
            defaultNullSorting = service.isNullsLastSorting();
        return defaultNullSorting;
    }

    @Override
    public boolean supportsLobSortingAndFiltering(String storeName) {
        return storeName == null || getDbmsCacheEntry(storeName).supportsLobSortingAndFiltering;
    }

    @Override
    public boolean emulateEqualsByLike(String storeName) {
        return storeName == null || getDbmsCacheEntry(storeName).emulateEqualsByLike;
    }

    protected DbmsCacheEntry getDbmsCacheEntry(String storeName) {
        return dbmsCache.computeIfAbsent(storeName, s -> {
            DbmsCacheEntry cacheEntry = new DbmsCacheEntry();
            cacheEntry.supportsLobSortingAndFiltering = service.supportsLobSortingAndFiltering(s);
            cacheEntry.emulateEqualsByLike = service.emulateEqualsByLike(s);
            return cacheEntry;
        });
    }

    public void clearCache() {
        cache.clear();
    }
}