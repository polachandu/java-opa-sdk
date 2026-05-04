package io.github.openpolicyagent.opa.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.openpolicyagent.opa.ir.policy.types.AnyType;
import io.github.openpolicyagent.opa.ir.policy.types.ArrayType;
import io.github.openpolicyagent.opa.ir.policy.types.BooleanType;
import io.github.openpolicyagent.opa.ir.policy.types.FunctionType;
import io.github.openpolicyagent.opa.ir.policy.types.NullType;
import io.github.openpolicyagent.opa.ir.policy.types.NumberType;
import io.github.openpolicyagent.opa.ir.policy.types.ObjectType;
import io.github.openpolicyagent.opa.ir.policy.types.SetType;
import io.github.openpolicyagent.opa.ir.policy.types.StringType;
import io.github.openpolicyagent.opa.ir.policy.types.Type;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class TypeDeserializer extends JsonDeserializer<Type> {
  // Type marker strings match the OPA IR spec typeMarker constants.
  private static final Map<String, Class<? extends Type>> TYPE_REGISTRY =
      new HashMap<>() {
        {
          put(NullType.TypeMarker,     NullType.class);
          put(BooleanType.TypeMarker,  BooleanType.class);
          put(NumberType.TypeMarker,   NumberType.class);
          put(StringType.TypeMarker,   StringType.class);
          put(ArrayType.TypeMarker,    ArrayType.class);
          put(ObjectType.TypeMarker,   ObjectType.class);
          put(SetType.TypeMarker,      SetType.class);
          put(AnyType.TypeMarker,      AnyType.class);
          put(FunctionType.TypeMarker, FunctionType.class);
        }
      };

  @Override
  public Type deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    JsonNode root = mapper.readTree(jp);
    JsonNode typeNode = root.get("type");
    if (typeNode == null) {
      return null;
    }

    String type = typeNode.asText();
    Class<? extends Type> typeClass = TYPE_REGISTRY.get(type);
    if (typeClass == null) {
      throw new IOException("unknown type: " + type);
    }

    return mapper.treeToValue(root, typeClass);
  }
}