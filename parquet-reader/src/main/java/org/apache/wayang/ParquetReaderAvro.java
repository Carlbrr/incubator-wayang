package org.apache.wayang;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;


public class ParquetReaderAvro {
    public static void main(String[] args) {
        String parquetFilePath = "src/main/java/org/apache/wayang/date.parquet";

        try {
            // Create a reader for the Parquet file - based on https://stackoverflow.com/questions/28615511/how-to-read-a-parquet-file-in-a-standalone-java-code
            //ParquetReaderAvro<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(new Path(parquetFilePath)).build();
            ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(new Path(parquetFilePath)).build();

            GenericRecord record;
            while ((record = reader.read()) != null) {
                System.out.println(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
