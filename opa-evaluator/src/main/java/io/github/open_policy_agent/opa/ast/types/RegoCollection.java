package io.github.open_policy_agent.opa.ast.types;

import java.util.stream.Stream;

public interface RegoCollection extends RegoValue{

    Stream<RegoValue> valueStream();
}
