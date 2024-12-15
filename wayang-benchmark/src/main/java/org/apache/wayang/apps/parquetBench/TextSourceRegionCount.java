package org.apache.wayang.apps.parquetBench;

import org.apache.wayang.api.JavaPlanBuilder;
import org.apache.wayang.basic.data.Tuple2;
import org.apache.wayang.core.api.Configuration;
import org.apache.wayang.core.api.WayangContext;
import org.apache.wayang.java.Java;

import java.util.Collection;

//mvn exec:java -pl wayang-benchmark -Dexec.mainClass="org.apache.wayang.apps.parquetBench.TextSourceRegionCount" -Dexec.args="file://$(pwd)/wayang-benchmark/src/main/java/org/apache/wayang/apps/parquetBench/cus_sf1.csv"
public class TextSourceRegionCount {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: <input CSV file URL> (must be absolute and prefixed with file:// - ONLY WORKS FOR SSB customer table)");
            System.exit(1);
        }

        // file path must be absolute and prefixed with file://
        String csvFilePath = args[0];

        // Create the context with the Java plugin
        WayangContext wayangContext = new WayangContext(new Configuration())
                .withPlugin(Java.basicPlugin());

        // Initialize the PlanBuilder
        JavaPlanBuilder planBuilder = new JavaPlanBuilder(wayangContext)
                .withJobName("RegionNationCountFromCSV")
                .withUdfJarOf(TextSourceRegionCount.class);

        var times = new long[6];
        Collection<Tuple2<String, Integer>> regionNationCounts = null;

        for (int i = 0; i < 6; i++) {
            long startTime = System.currentTimeMillis();

            // Build the pipeline
            regionNationCounts = planBuilder
                .readTextFile(csvFilePath).withName("Read CSV file")
                .map(line -> {
                    // Extract the `c_region` (column index 5) and `c_nation` (column index 4) fields
                    String[] fields = line.split(",");
                    String region = fields[5];
                    String nation = fields[4]; //the address attribute sometimes has commas in it, so we need to account for that
                    if (fields.length > 8 ) {
                        //System.out.println("LENGHT OF FIELDS: " + fields.length);
                        return new Tuple2<>("(Comma found in attribute)", 1);
                    }

                    // Combine `region` and `nation` into a single key (e.g., "AFRICA-MOROCCO")
                    String regionNation = region + "-" + nation;

                    return new Tuple2<>(regionNation, 1);
                }).withName("Extract and combine fields")
                .reduceByKey(
                    Tuple2::getField0, // Group by the combined key
                    (t1, t2) -> new Tuple2<>(t1.getField0(), t1.getField1() + t2.getField1()) // Sum counts of two tuples
                ).withName("Count region-nation combinations")
                .collect();

            long endTime = System.currentTimeMillis();
            times[i] = endTime - startTime;
        }

        long avgTime = 0;
        System.out.println("Text Region-Nation Count - Execution Times (ms):");
        for (int i = 0; i < 6; i++) {
            System.out.println((i + 1) + ": " + times[i] + " ms");
            if(i!=0){
                avgTime += times[i]; 
            }
        }
        avgTime /= 5;
        System.out.println("Average Execution Time: " + avgTime + " ms");

        // Print the region-nation counts
        System.out.println("Region-Nation Counts:");
        regionNationCounts.forEach(count ->
            System.out.println(count.getField0() + " -> " + count.getField1())
        );
    }
}
