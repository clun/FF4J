package org.ff4j.store.mongodb;

import java.util.HashMap;

/*
 * #%L
 * ff4j-store-mongodb
 * %%
 * Copyright (C) 2013 - 2014 Ff4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ff4j.core.Feature;
import org.ff4j.core.FlippingStrategy;
import org.ff4j.property.Property;
import org.ff4j.property.util.PropertyJsonBean;
import org.ff4j.utils.JsonUtils;
import org.ff4j.utils.MappingUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * MApping from Mongo document to Feature.
 * 
 * @author <a href="mailto:cedrick.lunven@gmail.com">Cedrick LUNVEN</a>
 */
public final class FeatureDBObjectMapper implements FeatureStoreMongoConstants {

    /**
     * Convert {@link DBObject} to {@link Feature}.
     * 
     * @param dbObject
     *            document in mongodb.
     * @return
     */
    public Feature mapFeature(DBObject dbObject) {
        String featUid = (String) dbObject.get(UUID);
        boolean status = (Boolean) dbObject.get(ENABLE);

        Feature f = new Feature(featUid, status);
        f.setDescription((String) dbObject.get(DESCRIPTION));
        f.setGroup((String) dbObject.get(GROUPNAME));
        f.setPermissions(mapAuthorization(dbObject));
        f.setFlippingStrategy(mapStrategy(featUid, dbObject));
        f.setCustomProperties(mapCustomProperties(dbObject));
        return f;
    }

    /**
     * Convert {@link Feature} to {@link DBObject}.
     * 
     * @param feature
     *            target feature.
     * @return document in mongo db
     */
    public DBObject toDBObject(Feature feature) {
        String strategyColumn = null;
        String expressionColumn = null;
        if (feature.getFlippingStrategy() != null) {
            strategyColumn = feature.getFlippingStrategy().getClass().getCanonicalName();
            expressionColumn = MappingUtil.fromMap(feature.getFlippingStrategy().getInitParams());
        }
        String customProperties = null;
        if (feature.getCustomProperties() != null) {
            customProperties = JsonUtils.customPropertiesAsJson(feature.getCustomProperties());
        }
        return new FeatureDBObjectBuilder().addFeatUid(feature.getUid()).//
                addEnable(feature.isEnable()).//
                addDescription(feature.getDescription()).//
                addGroupName(feature.getGroup()).//
                addStrategy(strategyColumn).//
                addExpression(expressionColumn).//
                addCustomProperties(customProperties).
                addRoles(feature.getPermissions()).build();
    }

    /**
     * Map from {@link DBObject} to authorizations..
     * 
     * @param dbObject
     *            target
     * @return
     */
    @SuppressWarnings("rawtypes")
    private Set<String> mapAuthorization(DBObject dbObject) {
        Set<String> authorisation = new HashSet<String>();
        if (dbObject.containsField(ROLES)) {
            for (Object role : (Iterable) dbObject.get(ROLES)) {
                authorisation.add(role.toString());
            }
        }
        return authorisation;
    }

    /**
     * Map from {@link DBObject} to strategy.
     * 
     * @param dbObject
     *            target
     * @return
     */
    private FlippingStrategy mapStrategy(String featUid, DBObject dbObject) {
        String strategy = (String) dbObject.get(STRATEGY);
        Map < String, String > initParams = MappingUtil.toMap((String) dbObject.get(EXPRESSION));
        if (strategy != null && !"".equals(strategy)) {
            return MappingUtil.instanceFlippingStrategy(featUid, strategy, initParams);
        }
        return null;
    }
    
    /**
     * Custom Properties.
     *
     * @param dbObject
     *      db object
     * @return
     *      list of property
     */
    @SuppressWarnings("unchecked")
    private Map < String, Property<?> > mapCustomProperties(DBObject dbObject) {
        Map < String, Property<?> > mapOfCustomProperties = new HashMap<String, Property<?>>();
        if (dbObject.containsField(CUSTOMPROPERTIES)) {
            String properties = (String) dbObject.get(CUSTOMPROPERTIES);
            Map < String, BasicDBObject > values = (Map<String, BasicDBObject>) JSON.parse(properties);
            for (String key : values.keySet()) {
                mapOfCustomProperties.put(key, mapProperty(values.get(key)));
            }
        }
        return mapOfCustomProperties;
    }
    
    /**
     * Map a property.
     *
     * @param dbObject
     *      db object
     * @return
     *      list of property
     */
    private Property< ? > mapProperty(DBObject dbObject) {
        PropertyJsonBean pf = new PropertyJsonBean();
        pf.setName((String) dbObject.get("name"));
        pf.setDescription((String) dbObject.get("description"));
        pf.setType((String) dbObject.get("type"));
        pf.setValue((String) dbObject.get("value"));
        if (dbObject.containsField("fixedValues")) {
            BasicDBList dbList = (BasicDBList) dbObject.get("fixedValues");
            if (dbList != null) {
                for(Object item : dbList) {
                    pf.addFixedValue((String) item);
                }
            }
        }
        return pf.asProperty();
    }

}
