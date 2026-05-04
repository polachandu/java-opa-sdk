package io.github.openpolicyagent.opa.jackson;

import io.github.openpolicyagent.opa.ir.Operand;
import io.github.openpolicyagent.opa.ir.policy.types.Type;
import io.github.openpolicyagent.opa.ir.stmts.Stmt;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Jackson {@link SimpleModule} that registers the OPA IR deserializers.
 *
 * <p>Register this module on any {@link com.fasterxml.jackson.databind.ObjectMapper} that will
 * parse OPA IR policy documents:
 *
 * <pre>{@code
 * ObjectMapper mapper = new ObjectMapper().registerModule(new IrModule());
 * }</pre>
 */
public class IrModule extends SimpleModule {

  public IrModule() {
    super("opa-ir");
    addDeserializer(Operand.class, new OperandDeserializer());
    addDeserializer(Stmt.class, new StmtDeserializer());
    addDeserializer(Type.class, new TypeDeserializer());
  }
}