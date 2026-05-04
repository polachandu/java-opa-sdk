package io.github.openpolicyagent.opa.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.openpolicyagent.opa.ir.stmts.ArrayAppendStmt;
import io.github.openpolicyagent.opa.ir.stmts.AssignIntStmt;
import io.github.openpolicyagent.opa.ir.stmts.AssignVarOnceStmt;
import io.github.openpolicyagent.opa.ir.stmts.AssignVarStmt;
import io.github.openpolicyagent.opa.ir.stmts.BlockStmt;
import io.github.openpolicyagent.opa.ir.stmts.BreakStmt;
import io.github.openpolicyagent.opa.ir.stmts.CallDynamicStmt;
import io.github.openpolicyagent.opa.ir.stmts.CallStmt;
import io.github.openpolicyagent.opa.ir.stmts.DotStmt;
import io.github.openpolicyagent.opa.ir.stmts.EqualStmt;
import io.github.openpolicyagent.opa.ir.stmts.IsArrayStmt;
import io.github.openpolicyagent.opa.ir.stmts.IsDefinedStmt;
import io.github.openpolicyagent.opa.ir.stmts.IsObjectStmt;
import io.github.openpolicyagent.opa.ir.stmts.IsSetStmt;
import io.github.openpolicyagent.opa.ir.stmts.IsUndefinedStmt;
import io.github.openpolicyagent.opa.ir.stmts.LenStmt;
import io.github.openpolicyagent.opa.ir.stmts.MakeArrayStmt;
import io.github.openpolicyagent.opa.ir.stmts.MakeNullStmt;
import io.github.openpolicyagent.opa.ir.stmts.MakeNumberIntStmt;
import io.github.openpolicyagent.opa.ir.stmts.MakeNumberRefStmt;
import io.github.openpolicyagent.opa.ir.stmts.MakeObjectStmt;
import io.github.openpolicyagent.opa.ir.stmts.MakeSetStmt;
import io.github.openpolicyagent.opa.ir.stmts.NopStmt;
import io.github.openpolicyagent.opa.ir.stmts.NotEqualStmt;
import io.github.openpolicyagent.opa.ir.stmts.NotStmt;
import io.github.openpolicyagent.opa.ir.stmts.ObjectInsertOnceStmt;
import io.github.openpolicyagent.opa.ir.stmts.ObjectInsertStmt;
import io.github.openpolicyagent.opa.ir.stmts.ObjectMergeStmt;
import io.github.openpolicyagent.opa.ir.stmts.ResetLocalStmt;
import io.github.openpolicyagent.opa.ir.stmts.ResultSetAddStmt;
import io.github.openpolicyagent.opa.ir.stmts.ReturnLocalStmt;
import io.github.openpolicyagent.opa.ir.stmts.ScanStmt;
import io.github.openpolicyagent.opa.ir.stmts.SetAddStmt;
import io.github.openpolicyagent.opa.ir.stmts.Stmt;
import io.github.openpolicyagent.opa.ir.stmts.WithStmt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class StmtDeserializer extends JsonDeserializer<Stmt> {
  // Maps JSON type strings to statement classes.
  // String values match the OPA IR spec and the STMT_TYPE enum's typeName values.
  private static final Map<String, Class<? extends Stmt>> STMT_REGISTRY =
      new HashMap<>() {
        {
          put(ArrayAppendStmt.StmtType,       ArrayAppendStmt.class);
          put(AssignVarOnceStmt.StmtType,     AssignVarOnceStmt.class);
          put(AssignIntStmt.StmtType,         AssignIntStmt.class);
          put(AssignVarStmt.StmtType,         AssignVarStmt.class);
          put(BlockStmt.StmtType,             BlockStmt.class);
          put(BreakStmt.StmtType,             BreakStmt.class);
          put(CallDynamicStmt.StmtType,       CallDynamicStmt.class);
          put(CallStmt.StmtType,              CallStmt.class);
          put(DotStmt.StmtType,               DotStmt.class);
          put(EqualStmt.StmtType,             EqualStmt.class);
          put(IsArrayStmt.StmtType,           IsArrayStmt.class);
          put(IsDefinedStmt.StmtType,         IsDefinedStmt.class);
          put(IsObjectStmt.StmtType,          IsObjectStmt.class);
          put(IsUndefinedStmt.StmtType,       IsUndefinedStmt.class);
          put(IsSetStmt.StmtType,             IsSetStmt.class);
          put(LenStmt.StmtType,               LenStmt.class);
          put(MakeArrayStmt.StmtType,         MakeArrayStmt.class);
          put(MakeNullStmt.StmtType,          MakeNullStmt.class);
          put(MakeNumberIntStmt.StmtType,     MakeNumberIntStmt.class);
          put(MakeNumberRefStmt.StmtType,     MakeNumberRefStmt.class);
          put(MakeObjectStmt.StmtType,        MakeObjectStmt.class);
          put(MakeSetStmt.StmtType,           MakeSetStmt.class);
          put(NotEqualStmt.StmtType,          NotEqualStmt.class);
          put(NotStmt.StmtType,               NotStmt.class);
          put(ObjectInsertOnceStmt.StmtType,  ObjectInsertOnceStmt.class);
          put(ObjectInsertStmt.StmtType,      ObjectInsertStmt.class);
          put(ObjectMergeStmt.StmtType,       ObjectMergeStmt.class);
          put(ResetLocalStmt.StmtType,        ResetLocalStmt.class);
          put(ResultSetAddStmt.StmtType,      ResultSetAddStmt.class);
          put(ReturnLocalStmt.StmtType,       ReturnLocalStmt.class);
          put(ScanStmt.StmtType,              ScanStmt.class);
          put(SetAddStmt.StmtType,            SetAddStmt.class);
          put(WithStmt.StmtType,              WithStmt.class);
          put(NopStmt.StmtType,               NopStmt.class);
        }
      };

  @Override
  public Stmt deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    JsonNode root = mapper.readTree(jp);
    JsonNode typeNode = root.get("type");
    if (typeNode == null) {
      return null;
    }

    String stmtType = typeNode.asText();
    Class<? extends Stmt> stmtClass = STMT_REGISTRY.get(stmtType);
    if (stmtClass == null) {
      throw new IOException("unknown stmt type: " + stmtType);
    }

    JsonNode stmtNode = root.get("stmt");
    if (stmtNode == null) {
      throw new IOException("missing stmt field for stmt: " + stmtType);
    }

    Stmt stmt = mapper.treeToValue(stmtNode, stmtClass);
    JsonNode file = stmtNode.get("file");
    JsonNode row = stmtNode.get("row");
    JsonNode col = stmtNode.get("col");

    if (file != null && row != null && col != null) {
      stmt.setLocation(file.asInt(), row.asInt(), col.asInt());
    }

    return stmt;
  }
}