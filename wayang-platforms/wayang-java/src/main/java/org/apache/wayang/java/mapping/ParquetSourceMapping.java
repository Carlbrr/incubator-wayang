package org.apache.wayang.java.mapping;

import java.util.Collection;
import java.util.Collections;

import org.apache.wayang.basic.operators.ParquetSource;
import org.apache.wayang.core.mapping.Mapping;
import org.apache.wayang.core.mapping.PlanTransformation;
import org.apache.wayang.java.platform.JavaPlatform;
import org.apache.wayang.core.mapping.OperatorPattern;
import org.apache.wayang.core.mapping.ReplacementSubplanFactory;
import org.apache.wayang.core.mapping.SubplanPattern;
import org.apache.wayang.java.operators.JavaParquetSource;
// This file is a mapping file for the ParquetSource operator. It is used to map the ParquetSource operator to the Java platform.
// It implements the Mapping interface and overrides the getTransformations method to return a collection of PlanTransformations.
// Additionally, it defines a SubplanPattern that matches the ParquetSource operator and a ReplacementSubplanFactory that creates a new JavaParquetSource operator.

@SuppressWarnings("unchecked")
public class ParquetSourceMapping implements Mapping {
    
    @Override
    public Collection<PlanTransformation> getTransformations() {
        return Collections.singleton(new PlanTransformation(
                this.createSubplanPattern(),
                this.createReplacementSubplanFactory(),
                JavaPlatform.getInstance()
        ));
    }

    private SubplanPattern createSubplanPattern() {
        @SuppressWarnings("rawtypes")
        final OperatorPattern operatorPattern = new OperatorPattern(
                "source", new org.apache.wayang.basic.operators.ParquetSource((String) null), false
        );
        return SubplanPattern.createSingleton(operatorPattern);
    }
    
    private ReplacementSubplanFactory createReplacementSubplanFactory() {
        return new ReplacementSubplanFactory.OfSingleOperators<ParquetSource>(
                (matchedOperator, epoch) -> new JavaParquetSource(matchedOperator).at(epoch)
        );
    }



}
