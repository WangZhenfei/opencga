package org.opencb.opencga.storage.hbase.variant;

import org.opencb.biodata.formats.variant.io.VariantWriter;
import org.opencb.datastore.core.ObjectMap;
import org.opencb.opencga.storage.core.StorageManagerException;
import org.opencb.opencga.storage.core.variant.VariantStorageManager;
import org.opencb.opencga.storage.core.variant.adaptors.VariantDBAdaptor;

import java.io.IOException;
import java.net.URI;

/**
 * Created by hpccoll1 on 13/05/15.
 */
public class HBaseVariantStorageManager extends VariantStorageManager {

    @Override
    public void addConfigUri(URI configUri) {
        super.addConfigUri(configUri);
    }

    @Override
    public URI transform(URI inputUri, URI pedigreeUri, URI outputUri, ObjectMap params)
            throws StorageManagerException {
        return super.transform(inputUri, pedigreeUri, outputUri, params);
    }

    @Override
    public URI load(URI input, ObjectMap params) throws IOException, StorageManagerException {
        return null;
    }

    @Override
    public VariantWriter getDBWriter(String dbName, ObjectMap params) throws StorageManagerException {
        return null;
    }

    @Override
    public VariantDBAdaptor getDBAdaptor(String dbName, ObjectMap params) throws StorageManagerException {
        return null;
    }
}
