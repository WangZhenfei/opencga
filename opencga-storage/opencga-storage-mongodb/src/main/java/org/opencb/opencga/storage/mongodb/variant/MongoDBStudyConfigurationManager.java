/*
 * Copyright 2015 OpenCB
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

package org.opencb.opencga.storage.mongodb.variant;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import org.opencb.datastore.core.ObjectMap;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDBCollection;
import org.opencb.datastore.mongodb.MongoDataStore;
import org.opencb.datastore.mongodb.MongoDataStoreManager;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.StudyConfiguration;
import org.opencb.opencga.storage.core.variant.StudyConfigurationManager;
import org.opencb.opencga.storage.mongodb.utils.MongoCredentials;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacobo Coll <jacobo167@gmail.com>
 */
public class MongoDBStudyConfigurationManager extends StudyConfigurationManager {

    private final MongoDataStoreManager mongoManager;
    private final MongoDataStore db;
    private final String collectionName;

    private final Map<Integer, StudyConfiguration> studyConfigurationMap = new HashMap();
    private final DBObjectToStudyConfigurationConverter studyConfigurationConverter = new DBObjectToStudyConfigurationConverter();

    public MongoDBStudyConfigurationManager(ObjectMap objectMap) throws IllegalOpenCGACredentialsException, UnknownHostException {
        this(new MongoCredentials(
                        objectMap.getString("mongoHost"),
                        objectMap.getInt("mongoPort"),
                        objectMap.getString("mongoDbName"),
                        objectMap.getString("mongoUser"),
                        objectMap.getString("mongoPassword", null)
                ),
                objectMap.getString("mongoStudyConfigurationCollectionName", "files"));
    }

    public MongoDBStudyConfigurationManager(MongoCredentials credentials, String collectionName) throws UnknownHostException {
        super(null);
        // Mongo configuration
        mongoManager = new MongoDataStoreManager(credentials.getDataStoreServerAddresses());
        db = mongoManager.get(credentials.getMongoDbName(), credentials.getMongoDBConfiguration());
        this.collectionName = collectionName;
    }

    @Override
    public QueryResult<StudyConfiguration> getStudyConfiguration(int studyId, QueryOptions options) {
        long start = System.currentTimeMillis();
        StudyConfiguration studyConfiguration;
        if (!studyConfigurationMap.containsKey(studyId)) {
            MongoDBCollection coll = db.getCollection(collectionName);

            BasicDBObject query = new BasicDBObject("studyId", studyId);
            if (options != null) {
                if (options.containsKey("fileId")) {
                    query.put(DBObjectToStudyConfigurationConverter.FIELD_FILE_IDS, options.getInt("fileId"));
                }
            }

            QueryResult<StudyConfiguration> queryResult = coll.find(query, null, studyConfigurationConverter, options);
            if (queryResult.getResult().isEmpty()) {
                studyConfiguration = null;
            } else {
                studyConfiguration = queryResult.first();
            }
            studyConfigurationMap.put(studyId, studyConfiguration);
        } else {
            studyConfiguration = studyConfigurationMap.get(studyId);
        }

        return new QueryResult<>("getStudyConfiguration", ((int) (System.currentTimeMillis() - start)), 1, 1, "", "", Collections.singletonList(studyConfiguration));
    }

    @Override
    public QueryResult updateStudyConfiguration(StudyConfiguration studyConfiguration, QueryOptions options) {
        MongoDBCollection coll = db.getCollection(collectionName);
        DBObject studyMongo = new DBObjectToStudyConfigurationConverter().convertToStorageType(studyConfiguration);

        DBObject query = new BasicDBObject("studyId", studyConfiguration.getStudyId());
        QueryResult<WriteResult> queryResult = coll.update(query, studyMongo, new QueryOptions("upsert", true));
        studyConfigurationMap.put(studyConfiguration.getStudyId(), studyConfiguration);

        return queryResult;
    }

}
