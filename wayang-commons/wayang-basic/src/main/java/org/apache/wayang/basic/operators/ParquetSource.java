package org.apache.wayang.basic.operators;
import org.apache.wayang.core.plan.wayangplan.UnarySource;
import org.apache.wayang.core.types.DataSetType;

// This class is a Wayang Source Operator for Parquet files
// It is based on the TextFileSource class, though a very simple version of it
// It extends the UnarySource class, which is a base class for sources with a single output
public class ParquetSource extends UnarySource<String>{
    // Wayang Source Operator for Parquet files

    private final String inputUrl;
    // We might want to add more parameters here, such as a Logger

    // Constructor - takes a URL as input and sets the DataSetType to String
    // Meaning, 
    public ParquetSource(String inputUrl) {
        // The default DataSetType is String, meaning that the source will output lines of tex?
        super(DataSetType.createDefault(String.class));
        this.inputUrl = inputUrl;
    }

    // Copy constructor - copies an instance of the ParquetSource
    // Not really sure when this is useful, but it is included in the other operators.
    public ParquetSource(ParquetSource that) {
        super(that);
        this.inputUrl = that.getInputUrl();
    }

    public String getInputUrl() {
        return this.inputUrl;
    }


}
