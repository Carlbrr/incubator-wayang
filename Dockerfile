#Special thanks to Mads Cornelius for the help..
# Tried with both docker compose as per the official guide: https://wayang.apache.org/docs/guide/wayang-docker
# Did not make anything easy - unlicensed errors.
# Tried with the offical docker image as described in: https://github.com/juripetersen/d3a-hackathon-wayang - did not work either.

# Alternative: pull java 11 base image with the dependencies required here
# then install maven, move to correct folder, and add to path
# Then install:
    #Spark
    #Hadoop
# And set these to the specified env variables
# Copy our local (forked) source code to image
# When image is built, we can then do all the extra configuration which i also tried to make work in here..
    # with mvn we install all packages in Wayang
    # then packaging the project to build the executable
    # then prepare environment with tar on the generated assembly target
    # then we update path again
    # then we can use Wayang :)

FROM mcr.microsoft.com/devcontainers/java:1-11-bookworm

RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
RUN tar -xvf apache-maven-3.9.9-bin.tar.gz && rm apache-maven-3.9.9-bin.tar.gz
RUN mv apache-maven-3.9.9 /opt/
ENV PATH=/opt/apache-maven-3.9.9/bin:$PATH

# Spark Installation
WORKDIR /opt/spark
RUN curl -O https://archive.apache.org/dist/spark/spark-3.2.0/spark-3.2.0-bin-hadoop3.2.tgz && \
    tar -xvf spark-3.2.0-bin-hadoop3.2.tgz && \
    chmod -R 777 /opt/spark

# Hadoop Installation
WORKDIR /opt/hadoop
RUN curl -O https://dlcdn.apache.org/hadoop/common/hadoop-3.4.0/hadoop-3.4.0-aarch64.tar.gz && \
    tar -xvf hadoop-3.4.0-aarch64.tar.gz && \
    chmod -R 777 /opt/hadoop

# Coursier Installation - might be unneccesary - it is
WORKDIR /opt/coursier
RUN curl -fL https://github.com/VirtusLab/coursier-m1/releases/latest/download/cs-aarch64-pc-linux.gz --output cs.gz && \
    cat cs.gz | gzip -d > cs && chmod +x cs && mv cs /usr/local/bin/

# Set Environment Variables
ENV SPARK_HOME=/opt/spark/spark-3.2.0-bin-hadoop3.2
ENV HADOOP_HOME=/opt/hadoop/hadoop-3.4.0
ENV PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

# Wayang environment variables
ENV WAYANG_HOME=/workspaces/wayang/wayang-0.7.1
ENV PATH=$PATH:$WAYANG_HOME/bin

# Copy the Wayang project from the host machine into the container
WORKDIR /opt/wayang
COPY . /opt/wayang

# Expose necessary port
EXPOSE 8080 7077 4040

# Default command
CMD ["bash"]