/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.core.sys.http.api;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.MetadataHelper;
import com.haulmont.cuba.core.global.MetadataProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.persistence.Id;
import javax.servlet.http.HttpServletResponse;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;

/**
 * Marshals a root instance and its persistent closure as JSON object.
 * The closure is resolved against the persistence context that contains the root instance.
 * The JSON format introduces a $id and $ref to address reference that pure JSON does not.
 *
 * @author Pinaki Poddar
 */
public class JSONConvertor implements Convertor {
    public static final String MIME_STR = "application/json;charset=UTF-8";
    public static final MimeType MIME_TYPE_JSON;

    static {
        try {
            MIME_TYPE_JSON = new MimeType(MIME_STR);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }
    }

    public MimeType getMimeType() {
        return MIME_TYPE_JSON;
    }

    public MyJSONObject process(Entity entity, MetaClass metaclass, String requestURI)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return encodeInstance(entity, new HashSet<Entity>(), metaclass);
    }

    public MyJSONObject.Array process(List<Entity> entities, MetaClass metaClass, String requestURI)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        MyJSONObject.Array result = new MyJSONObject.Array();
        for (Entity entity : entities) {
            MyJSON item = encodeInstance(entity, new HashSet<Entity>(), metaClass);
            result.add(item);
        }
        return result;
    }

    public MyJSONObject.Array process(Map<Entity, Entity> entityMap, String requestURI)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        MyJSONObject.Array result = new MyJSONObject.Array();
        for (Map.Entry<Entity, Entity> entry : entityMap.entrySet()) {
            Entity key = entry.getKey();
            Entity value = entry.getValue();
            MyJSONObject keyJson = encodeInstance(key, new HashSet<Entity>(), getMetaClass(key));
            MyJSONObject valueJson = encodeInstance(value, new HashSet<Entity>(), getMetaClass(value));

            MyJSONObject.Array mapping = new MyJSONObject.Array();
            mapping.add(keyJson);
            mapping.add(valueJson);
            result.add(mapping);
        }
        return result;
    }

    private MetaClass getMetaClass(Entity entity) {
        return MetadataProvider.getSession().getClass(entity.getClass());
    }

    public void write(HttpServletResponse response, Object o) {
        response.setContentType(MIME_STR);
        try {
            PrintWriter writer = response.getWriter();
            writer.write(o.toString());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CommitRequest parseCommitRequest(String content) {
        try {
            JSONObject jsonContent = new JSONObject(content);
            CommitRequest result = new CommitRequest();

            if (jsonContent.has("commitInstances")) {
                JSONArray entitiesNodeList = jsonContent.getJSONArray("commitInstances");
                result.setCommitInstances(parseIntoList(result, entitiesNodeList));
            }

            if (jsonContent.has("removeInstances")) {
                JSONArray entitiesNodeList = jsonContent.getJSONArray("removeInstances");
                result.setRemoveInstances(parseIntoList(result, entitiesNodeList));
            }

            if (jsonContent.has("softDeletion")) {
                result.setSoftDeletion(jsonContent.getBoolean("softDeletion"));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<BaseUuidEntity> parseIntoList(CommitRequest commitRequest, JSONArray nodeList)
            throws JSONException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException, ParseException {
        List<BaseUuidEntity> result = new ArrayList<BaseUuidEntity>(nodeList.length());

        for (int j = 0; j < nodeList.length(); j++) {
            JSONObject jsonObject = nodeList.getJSONObject(j);
            InstanceRef ref = commitRequest.parseInstanceRefAndRegister(jsonObject.getString("id"));
            MetaClass metaClass = ref.getMetaClass();
            BaseUuidEntity instance = ref.getInstance();
            asJavaTree(commitRequest, instance, metaClass, jsonObject);
            result.add(instance);
        }
        return result;
    }

    private void asJavaTree(CommitRequest commitRequest, Object bean, MetaClass metaClass, JSONObject json)
            throws JSONException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, IntrospectionException, ParseException {
        Iterator iter = json.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            MetaProperty property = metaClass.getProperty(key);

            if (json.get(key) == null) {
                setField(bean, key, new Object[]{null});
                continue;
            }

            if ("id".equals(key)) {
                // id was parsed already
                continue;
            }

            switch (property.getType()) {
                case DATATYPE:
                    setField(bean, key, property.getRange().<Object>asDatatype().parse(json.getString(key)));
                    break;
                case ENUM:
                    setField(bean, key, property.getRange().asEnumeration().parse(json.getString(key)));
                    break;
                case AGGREGATION:
                case ASSOCIATION:
                    if (!property.getRange().getCardinality().isMany()) {
                        JSONObject jsonChild = json.getJSONObject(key);
                        Object child;
                        MetaClass childMetaClass;

                        if (jsonChild.has("id")) {
                            InstanceRef ref = commitRequest.parseInstanceRefAndRegister(jsonChild.getString("id"));
                            childMetaClass = ref.getMetaClass();
                            child = ref.getInstance();
                        } else {
                            childMetaClass = property.getRange().asClass();
                            child = childMetaClass.createInstance();
                        }
                        asJavaTree(commitRequest, child, childMetaClass, jsonChild);
                        setField(bean, key, child);
                    } else {
                        JSONArray jsonArray = json.getJSONArray(key);
                        Collection coll = property.getRange().isOrdered() ? new ArrayList() : new HashSet();
                        setField(bean, key, coll);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            Object arrayValue = jsonArray.get(i);
                            if (arrayValue == null)
                                coll.add(null);
                            else {
                                //assuming no simple type here
                                JSONObject jsonChild = (JSONObject) arrayValue;
                                InstanceRef ref = commitRequest.parseInstanceRefAndRegister(jsonChild.getString("id"));
                                Object child = ref.getInstance();
                                coll.add(child);
                                asJavaTree(commitRequest, child, ref.getMetaClass(), jsonChild);
                            }
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown property type");
            }
        }
    }

    private void setField(Object bean, String field, Object value)
            throws IllegalAccessException, InvocationTargetException, IntrospectionException {
        new PropertyDescriptor(field, bean.getClass()).getWriteMethod().invoke(bean, value);
    }

    /**
     * Encodes the closure of a persistent instance into JSON.
     *
     * @param entity  the managed instance to be encoded. Can be null.
     * @param visited the persistent instances that had been encoded already. Must not be null or immutable.
     * @return the new element. The element has been appended as a child to the given parent in this method.
     */
    private MyJSONObject encodeInstance(final Entity entity, final Set<Entity> visited,
                                        MetaClass metaClass) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (visited == null) {
            throw new IllegalArgumentException("null closure for encoder");
        }
        if (entity == null) {
            return null;
        }

        boolean ref = !visited.add(entity);

        MyJSONObject root = new MyJSONObject(idof(entity), ref);

        if (ref) {
            return root;
        }

        List<MetaProperty> properties = ConvertorHelper.getOrderedProperties(metaClass);
        for (MetaProperty property : properties) {
            if (MetadataHelper.isTransient(entity, property.getName()))
                continue;

            Object value = ((Instance) entity).getValue(property.getName());
            if (property.getAnnotatedElement().isAnnotationPresent(Id.class)) {
                //skipping: we encoded it before
                continue;
            }
            switch (property.getType()) {
                case DATATYPE:
                    root.set(property.getName(), property.getRange().<Object>asDatatype().format(value));
                    break;
                case ENUM:
                    root.set(property.getName(), property.getRange().asEnumeration().format(value));
                    break;
                case AGGREGATION:
                case ASSOCIATION:
                    if (!property.getRange().getCardinality().isMany()) {
                        if (value == null) {
                            root.set(property.getName(), null);
                        } else {
                            root.set(property.getName(), encodeInstance((Entity) value, visited,
                                    property.getRange().asClass()));
                        }
                    } else {
                        if (value == null) {
                            root.set(property.getName(), null);
                            break;
                        }

                        MyJSONObject.Array array = new MyJSONObject.Array();
                        root.set(property.getName(), array);

                        Collection<?> members = (Collection<?>) value;
                        for (Object o : members) {
                            if (o == null) {
                                array.add(null);
                            } else {
                                array.add(encodeInstance((Entity) o, visited,
                                        property.getRange().asClass()));
                            }
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown property type");
            }
        }

        return root;
    }

    private String idof(Entity entity) {
        return EntityLoadInfo.create(entity).toString();
    }


    String typeOf(Entity entity) {
        return entity.getClass().getSimpleName();
    }

}
