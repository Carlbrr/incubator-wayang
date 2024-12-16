# Adding a Parquet Source Operator in Apache Wayang

**Author: Carl Bruun (carbr@itu.dk)**

This repository includes all changes made in Wayang in accordance with project-3 of Advanced Data Systems at ITU (2024, MSc., ComSci).


Folders changed/added: 
- **/parquet-reader** 
Contains standalone Java program to read Parquet files with Avro-Parquet and return records to std output
- **/wayang-commons**
Added the source operator, ParquetSource.java
- **/wayang-platforms/wayang-java**
Added:
  - The java specific text source operator, JavaParquetSource.java
  - A new mapping class between the source operator and execution operator, ParquetSourceMapping.java
  - Updated the Mappings.java to register the new operator mapping.
- **wayang-api**
Updated the JavaPlanBuilder.java as to register the new operator as an endpoint (enables use in pipelines)
- **/wayang-benchmark**
Added four pipelines that each iterates 6 times, and returns the avg execution time (not including the first run which is much higher - heavily afftected by warm-up or caching cost I assume):
  - ParquetSourceWithJava.java: a simple read and return pipe for Parquet
  - TextFileSourceWithJava.java: a simple read and return pipe for textfiles (CSV tested in this case)
  - ParquetSourceRegionCount.java: a bit more complex read and count region-nation counts on SSB "customer" tables in Parquet format. Returns a count for each combination
  - TextSourceRegionCount.java: a bit more complex read and count region-nation counts on SSB "customer" tables in .CSV format (or another txt file). Returns a count for each combination

## How To Run:
- **Dockerfiler**
The Dockerfile installs all required dependencies manually and sets environment variables:
  - Java 11
  - Maven
  - Spark
  - Hadoop
It then mounts the locally cloned repository into the container

- **docker-compose**
Includes Wayang base-image and all dependencies
Do note that, when building -Drat.skip=true must be included to skip rat license checks.


It is adviced to run the docker-compose.yaml:

```console

$ docker compose up -d

$ docker exec -it apache-wayang-appJA bash

// Note that you need to disable RAT licensing checks as well as aggregate add third party (also licensing issues) to build
// Other skips are just to make rebuild faster
$ mvn clean install -DskipTests -Drat.skip=true -Dmaven.javadoc.skip=true -Djacoco.skip=true -Dlicense.skipAggregateAddThirdParty=true
$ mvn clean package -pl :wayang-assembly -Pdistribution

// in folder wayang-assembly/target
$ tar -xvf apache-wayang-assembly-0.7.1-incubating-dist.tar.gz

$ cd wayang-0.7.1

// converts windows-like file endings to unix
$ apt-get update
$ apt-get install dos2unix
$ dos2unix ./bin/wayang-submit

$ echo "export WAYANG_HOME=$(pwd)" >> ~/.bashrc
$ echo "export PATH=${PATH}:${WAYANG_HOME}/bin" >> ~/.bashrc
$ source ~/.bashrc

// standard word count example
$ ./bin/wayang-submit org.apache.wayang.apps.wordcount.Main java file://$(pwd)/README.md

// running ParquetSourceWithJava pipeline:
$ mvn exec:java -pl wayang-benchmark -Dexec.mainClass="org.apache.wayang.apps.parquetBench.ParquetSourceWithJava" -Dexec.args="wayang-benchmark/src/main/java/org/apache/wayang/apps/parquetBench/cus_sf1.parquet"
```

If testing SF100 table files of the SSB dataset, increasing memory might be necessary:
```console
export _JAVA_OPTIONS="-Xmx6g" 
```




