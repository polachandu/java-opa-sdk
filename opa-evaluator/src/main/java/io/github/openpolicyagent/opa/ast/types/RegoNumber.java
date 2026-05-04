package io.github.openpolicyagent.opa.ast.types;


import java.math.BigInteger;

public interface RegoNumber extends RegoValue {

  Double getDecimalValue();

    BigInteger getBigIntValue();

    default boolean isDecimal() {
        return false;
    }

}
