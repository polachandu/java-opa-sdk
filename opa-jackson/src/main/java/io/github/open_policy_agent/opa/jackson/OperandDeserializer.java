package io.github.open_policy_agent.opa.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_policy_agent.opa.ir.Operand;
import io.github.open_policy_agent.opa.ir.vals.BoolVal;
import io.github.open_policy_agent.opa.ir.vals.LocalVal;
import io.github.open_policy_agent.opa.ir.vals.StringIndexVal;
import io.github.open_policy_agent.opa.ir.vals.Val;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class OperandDeserializer extends JsonDeserializer<Operand> {
  private static final Map<String, Class<? extends Val>> VAL_REGISTRY =
      new HashMap<String, Class<? extends Val>>() {
        {
          put("bool", BoolVal.class);
          put("string_index", StringIndexVal.class);
          put("local", LocalVal.class);
        }
      };

  @Override
  public Operand deserialize(JsonParser jp, DeserializationContext ctx)
      throws IOException, JsonProcessingException {
    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    JsonNode node = mapper.readTree(jp);
    JsonNode opNode = node.get("type");
    if (opNode == null) {
      return null;
    }

    String opValType = opNode.asText();
    Class<? extends Val> opClass = VAL_REGISTRY.get(opValType);
    if (opClass == null) {
      throw new IOException("unknown val type: " + opValType);
    }

    return new Operand(mapper.treeToValue(node, opClass));
  }
}