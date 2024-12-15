package org.apache.wayang.apps.parquetBench;

import org.apache.wayang.api.JavaPlanBuilder;
import org.apache.wayang.basic.data.Tuple2;
import org.apache.wayang.core.api.Configuration;
import org.apache.wayang.core.api.WayangContext;
import org.apache.wayang.java.Java;

import java.util.Collection;

public class ParquetSourceRegionCount {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: <input Parquet file URL> - ONLY WORKS FOR SSB customer table");
            System.exit(1);
        }

        String parquetFilePath = args[0];

        // Create a WayangContext with the Java plugin
        WayangContext wayangContext = new WayangContext(new Configuration())
                .withPlugin(Java.basicPlugin());

        // Initialize the PlanBuilder...
        JavaPlanBuilder planBuilder = new JavaPlanBuilder(wayangContext)
                .withJobName("RegionNationCountFromParquet")
                .withUdfJarOf(ParquetSourceRegionCount.class);

        var times = new long[6];
        Collection<Tuple2<String, Integer>> regionNationCounts = null;

        for (int i = 0; i < 6; i++) {
            long startTime = System.currentTimeMillis();

            // Build the pipeline
            regionNationCounts = planBuilder
                .readParquetFile(parquetFilePath).withName("Read Parquet file")
                .map(record -> {
                    // Extract the `c_region` and `c_nation` fields from Parquet record - JSON-like from Parquet
                    String region = record.split("\"c_region\": \"")[1].split("\"")[0];
                    String nation = record.split("\"c_nation\": \"")[1].split("\"")[0];

                    // Combine them into a single key (e.g., "AFRICA-MOROCCO" or smth..)
                    String regionNation = region + "-" + nation;

                    return new Tuple2<>(regionNation, 1);
                }).withName("Extract and combine fields")
                .reduceByKey(
                    Tuple2::getField0, // Group by the combined key
                    (t1, t2) -> new Tuple2<>(t1.getField0(), t1.getField1() + t2.getField1()) // Sum counts
                ).withName("Count region-nation combinations")
                .collect();

            long endTime = System.currentTimeMillis();
            times[i] = endTime - startTime;
        }

    
        long avgTime = 0;
        System.out.println("Parquet Region-Nation Count - Execution Times (ms):");
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
