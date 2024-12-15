package org.apache.wayang.java.operators;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.wayang.basic.operators.ParquetSource;
import org.apache.wayang.core.api.exception.WayangException;
import org.apache.wayang.core.optimizer.OptimizationContext;
import org.apache.wayang.core.platform.ChannelDescriptor;
import org.apache.wayang.core.platform.ChannelInstance;
import org.apache.wayang.core.platform.lineage.ExecutionLineageNode;
import org.apache.wayang.java.channels.StreamChannel;
import org.apache.wayang.java.execution.JavaExecutor;
import org.apache.wayang.core.util.Tuple;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Java execution operator for the {@link ParquetSource}.
 */
public class JavaParquetSource extends ParquetSource implements JavaExecutionOperator {

    public JavaParquetSource(String inputUrl) {
        super(inputUrl);
    }

    /**
     * Copies an instance (exclusive of broadcasts). - this is just added as it is included in the other operators
     *
     * @param that that should be copied
     */
    public JavaParquetSource(ParquetSource that) {
        super(that);
    }

    @Override
    public Tuple<Collection<ExecutionLineageNode>, Collection<ChannelInstance>> evaluate(
            ChannelInstance[] inputs,
            ChannelInstance[] outputs,
            JavaExecutor javaExecutor,
            OptimizationContext.OperatorContext operatorContext) {

        assert inputs.length == 0;
        assert outputs.length == 1;

        String parquetFilePath = this.getInputUrl().trim();
        StreamChannel.Instance output = (StreamChannel.Instance) outputs[0];

        try {
            // Use AvroParquetReader to read records from the Parquet file.
            ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(new Path(parquetFilePath)).build();

            // Use a Stream.Builder to collect records into a stream.
            Stream.Builder<String> streamBuilder = Stream.builder();
            GenericRecord record;

            // Read records one by one and add to the stream builder.
            while ((record = reader.read()) != null) {
                streamBuilder.add(record.toString()); // We should convert to String, just as in the JavaTextFileSource returns a stream of Strings.
            }

            // Pass the stream to the output.
            output.accept(streamBuilder.build());

        } catch (Exception e) {
            throw new WayangException(String.format("Failed to read Parquet file: %s", parquetFilePath), e);
        }

        // Mark lineage for optimization purposes.
        ExecutionLineageNode lineageNode = new ExecutionLineageNode(operatorContext);
        output.getLineage().addPredecessor(lineageNode);

        return lineageNode.collectAndMark();
    }

    @Override
    public JavaParquetSource copy() {
        return new JavaParquetSource(this.getInputUrl());
    }

    @Override
    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        throw new UnsupportedOperationException("No have input channels.");
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        assert index == 0; // This operator only has one output channel.
        return Collections.singletonList(StreamChannel.DESCRIPTOR);
    }

}
