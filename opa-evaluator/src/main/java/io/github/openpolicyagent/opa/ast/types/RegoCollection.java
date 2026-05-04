package io.github.openpolicyagent.opa.ast.types;

import java.util.stream.Stream;

public interface RegoCollection extends RegoValue{

    Stream<RegoValue> valueStream();
}
