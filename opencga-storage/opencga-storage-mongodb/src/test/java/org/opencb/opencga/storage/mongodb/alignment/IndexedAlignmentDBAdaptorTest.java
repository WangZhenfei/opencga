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

package org.opencb.opencga.storage.mongodb.alignment;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.alignment.Alignment;
import org.opencb.biodata.models.alignment.stats.MeanCoverage;
import org.opencb.biodata.models.alignment.stats.RegionCoverage;
import org.opencb.biodata.models.feature.Region;
import org.opencb.commons.test.GenericTest;
import org.opencb.datastore.core.ObjectMap;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.opencga.storage.core.alignment.json.AlignmentDifferenceJsonMixin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class IndexedAlignmentDBAdaptorTest  extends GenericTest{


    private IndexedAlignmentDBAdaptor dbAdaptor;
    //private AlignmentQueryBuilder dbAdaptor;
    private MongoDBAlignmentStorageManager manager;
    private AlignmentMetaDataDBAdaptor metadata;

    @Before
    public void before(){
        manager = new MongoDBAlignmentStorageManager(Paths.get("/media/jacobo/Nusado/opencga", "opencga.properties"));
        metadata = new AlignmentMetaDataDBAdaptor(manager.getProperties().getProperty("files-index", "/tmp/files-index.properties"));
        //manager.getMetadata();

        Path adaptorPath = null;
        adaptorPath = Paths.get("/media/jacobo/Nusado/opencga/sequence", "human_g1k_v37.fasta.gz.sqlite.db");
        manager.getProperties().setProperty(MongoDBAlignmentStorageManager.OPENCGA_STORAGE_SEQUENCE_DBADAPTOR, adaptorPath.toString());
        dbAdaptor = (IndexedAlignmentDBAdaptor) manager.getDBAdaptor(MongoDBAlignmentStorageManager.MONGO_DB_NAME, new ObjectMap());
    }

    @Test
    public void testGetAllAlignmentsByRegion() throws IOException {


        QueryOptions qo = new QueryOptions();
//        qo.put(IndexedAlignmentDBAdaptor.QO_BAM_PATH, metadata.getBamFromIndex("1").toString());
//        qo.put(IndexedAlignmentDBAdaptor.QO_BAI_PATH, metadata.getBaiFromIndex("1").toString());  //NOT NECESSARY
        //qo.put("view_as_pairs", true);

        qo.put("bam_path", "/media/jacobo/Nusado/opencga/alignment/HG04239.chrom20.ILLUMINA.bwa.ITU.low_coverage.20130415.bam");
        qo.put("bai_path", "/media/jacobo/Nusado/opencga/alignment/HG04239.chrom20.ILLUMINA.bwa.ITU.low_coverage.20130415.bam.bai");
        qo.put(IndexedAlignmentDBAdaptor.QO_PROCESS_DIFFERENCES, false);

        //Region region = new Region("20", 20000000, 20000100);
        Region region = new Region("20", 29829000, 29830000);

        QueryResult alignmentsByRegion = dbAdaptor.getAllAlignmentsByRegion(Arrays.asList(region), qo);
        printQueryResult(alignmentsByRegion);
        jsonQueryResult("HG04239",alignmentsByRegion);

        qo.put(IndexedAlignmentDBAdaptor.QO_PROCESS_DIFFERENCES, true);
        alignmentsByRegion = dbAdaptor.getAllAlignmentsByRegion(Arrays.asList(new Region("20", 29829000, 29829500)), qo);
        printQueryResult(alignmentsByRegion);
        jsonQueryResult("HG04239",alignmentsByRegion);

        alignmentsByRegion = dbAdaptor.getAllAlignmentsByRegion(Arrays.asList(new Region("20", 29829500, 29830000)), qo);
        printQueryResult(alignmentsByRegion);
        jsonQueryResult("HG04239",alignmentsByRegion);

    }

    @Test
    public void testGetHistogramCoverageByRegion() throws IOException {
//29337216, 29473005
        QueryOptions qo = new QueryOptions();
        qo.put(IndexedAlignmentDBAdaptor.QO_FILE_ID, "HG01551");
        qo.put(IndexedAlignmentDBAdaptor.QO_INTERVAL_SIZE, 10000);
        qo.put(IndexedAlignmentDBAdaptor.QO_BAM_PATH, "/media/jacobo/Nusado/opencga_bam_files/HG01551.bam");
        jsonQueryResult("HG01551.coverage",dbAdaptor.getCoverageByRegion(new Region("20", 29829001, 29830000), qo));
        jsonQueryResult("HG01551.coverage",dbAdaptor.getCoverageByRegion(new Region("20", 29830001, 29833000), qo));
        //qo.put(IndexedAlignmentDBAdaptor.QO_HISTOGRAM, false);
        jsonQueryResult("HG01551.mean-coverage.10k",dbAdaptor.getAllIntervalFrequencies(new Region("20", 29800000, 29900000), qo));
        qo.put(IndexedAlignmentDBAdaptor.QO_INCLUDE_COVERAGE, true);
        jsonQueryResult("HG01551",dbAdaptor.getAllAlignmentsByRegion(Arrays.asList(new Region("20", 29829001, 29830000)), qo));
        jsonQueryResult("HG01551",dbAdaptor.getAllAlignmentsByRegion(Arrays.asList(new Region("20", 29828951, 29830000)), qo));

    }


    @Test
    public void getAllIntervalFrequenciesAggregateTest() throws IOException {
        QueryOptions qo = new QueryOptions();
        qo.put(IndexedAlignmentDBAdaptor.QO_FILE_ID, "HG00096");

        jsonQueryResult("aggregate", dbAdaptor.getAllIntervalFrequencies(new Region("20", 50000, 100000), qo));


    }


    @Test
    public void testGetCoverageByRegion() throws IOException {

        QueryOptions qo = new QueryOptions();
        qo.put(IndexedAlignmentDBAdaptor.QO_FILE_ID, "HG04239");
        qo.put(IndexedAlignmentDBAdaptor.QO_BAM_PATH, "/media/jacobo/Nusado/opencga_bam_files/HG00096.bam");

        //Region region = new Region("20", 20000000, 20000100);

        printQueryResult(dbAdaptor.getCoverageByRegion(new Region("20", 29829000, 29830000), qo));
        printQueryResult(dbAdaptor.getCoverageByRegion(new Region("20", 29829000, 29850000), qo));
        qo.put(IndexedAlignmentDBAdaptor.QO_HISTOGRAM, false);
        printQueryResult(dbAdaptor.getCoverageByRegion(new Region("20", 29829000, 29830000), qo));
        qo.put(IndexedAlignmentDBAdaptor.QO_INTERVAL_SIZE, 1000000);
        printQueryResult(dbAdaptor.getCoverageByRegion(new Region("20", 1, 65000000), qo));

//        System.out.println(coverageByRegion);
//        System.out.println(coverageByRegion.getTime());
    }

    private void jsonQueryResult(String name, QueryResult qr) throws IOException {
        JsonFactory factory = new JsonFactory();
        ObjectMapper jsonObjectMapper = new ObjectMapper(factory);
        jsonObjectMapper.addMixInAnnotations(Alignment.AlignmentDifference.class, AlignmentDifferenceJsonMixin.class);
        JsonGenerator generator = factory.createGenerator(new FileOutputStream("/tmp/"+name+"."+qr.getId()+".json"));

        generator.writeObject(qr.getResult());

    }

    private void printQueryResult(QueryResult cr){
        String s = cr.getResultType();
        System.out.println("cr.getDbTime() = " + cr.getDbTime());
        if (s.equals(MeanCoverage.class.getCanonicalName())) {
            List<MeanCoverage> meanCoverageList = cr.getResult();
            for(MeanCoverage mc : meanCoverageList){
                System.out.println(mc.getRegion().toString()+" : " + mc.getCoverage());
            }
        } else if (s.equals(RegionCoverage.class.getCanonicalName())) {
            List<RegionCoverage> regionCoverageList = cr.getResult();
            for(RegionCoverage rc : regionCoverageList){
                System.out.print(new Region(rc.getChromosome(), (int) rc.getStart(), (int) rc.getEnd()).toString()+ " (");
                for (int i = 0; i < rc.getAll().length; i++) {
                    System.out.print(rc.getAll()[i] + ",");
                }
                System.out.println(");");

            }
        }
    }

}