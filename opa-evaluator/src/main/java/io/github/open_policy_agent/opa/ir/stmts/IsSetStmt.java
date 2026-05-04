package io.github.open_policy_agent.opa.ir.stmts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Objects;
import io.github.open_policy_agent.opa.ir.Operand;

/**
 * IsSetStmt represents a dynamic type check on a local variable.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the `StmtDeserializer` run again.
@JsonDeserialize
public class IsSetStmt extends BaseStmt {
    public static final String StmtType = "IsSetStmt";

    @JsonProperty("source")
    private Operand source;

    public IsSetStmt() {
    }

    public IsSetStmt(Operand source) {
        this.source = source;
    }

    public Operand getSource() {
        return source;
    }

    public void setSource(Operand source) {
        this.source = source;
    }

  @Override
  public STMT_TYPE getType() {
    return STMT_TYPE.IS_SET;
    }

  @Override
  public int maxLocal() {
    return getLocalFromOperand(source);
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IsSetStmt that = (IsSetStmt) o;

        if (getFile() != that.getFile()) return false;
        if (getCol() != that.getCol()) return false;
        if (getRow() != that.getRow()) return false;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        int result = source != null ? source.hashCode() : 0;
        result = 31 * result + getFile();
        result = 31 * result + getCol();
        result = 31 * result + getRow();
        return result;
    }

    @Override
    public String toString() {
        return "IsArrayStmt{"
                + "source="
                + source
                + ", file="
                + getFile()
                + ", col="
                + getCol()
                + ", row="
                + getRow()
                + '}';
    }
}
