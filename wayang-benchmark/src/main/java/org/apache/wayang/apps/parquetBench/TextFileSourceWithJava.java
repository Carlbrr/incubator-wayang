package org.apache.wayang.apps.parquetBench;

import java.io.StringBufferInputStream;
import java.util.Collection;

import org.apache.wayang.api.JavaPlanBuilder; // cant resolve this import because it is scala code i think
import org.apache.wayang.basic.data.Tuple2;
import org.apache.wayang.core.api.Configuration;
import org.apache.wayang.core.api.WayangContext;
import org.apache.wayang.java.Java;
import org.apache.wayang.spark.Spark;

public class TextFileSourceWithJava {
    public static void main(String[] args){
        if (args.length == 0) {
                System.err.print("Usage: <input CSV file URL> (must be absolute and prefixed with file://)");
                System.exit(1);
        }
        
        // Okay, so the file argument MUST be absolute and prefixed with file://. e.g. file://$(pwd)/path/to/file.csv
        String csvFilePath = args[0];

        /* Get a WayangContext */
        WayangContext wayangContext = new WayangContext(new Configuration())
                .withPlugin(Java.basicPlugin());
        
        JavaPlanBuilder planBuilder = new JavaPlanBuilder(wayangContext)
                .withJobName("readCSVFile")
                .withUdfJarOf(TextFileSourceWithJava.class);

        Collection<String> records = null;
        
        var times = new long[6];
        
        // 5 Iterations
        for (int i = 0; i < 6; i++) {
                long startTime = System.currentTimeMillis();

                // actual pipeline
                records = planBuilder
                    .readTextFile(csvFilePath)
                    .withName("read CSV file")
                    .collect();   

                long endTime = System.currentTimeMillis();
                times[i] = endTime - startTime;
        }

        //average times
        long avgTime = 0;
        System.out.println("CSV Read Records - times:");
        for (int i = 0; i < 6; i++) {
                System.out.println(i+1 + " " + times[i]);
                if(i!=0){
                        avgTime += times[i]; 
                }
        }
        avgTime /= 5;
        System.out.println("Average time (ms): " + avgTime);
        System.out.println("Ex output (first 10 records):");
        records.stream().limit(10).forEach(System.out::println);
    }
}