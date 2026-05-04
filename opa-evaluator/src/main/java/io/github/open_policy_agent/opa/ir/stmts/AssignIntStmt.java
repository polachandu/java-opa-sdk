package io.github.open_policy_agent.opa.ir.stmts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Objects;

/**
 * AssignIntStmt represents a dynamic append operation of a value onto an array.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the `StmtDeserializer` run again.
@JsonDeserialize
public class AssignIntStmt extends BaseStmt {
    public static final String StmtType = "AssignIntStmt";

    @JsonProperty("value")
    private Long value;

    @JsonProperty("target")
    private int target;

    public AssignIntStmt() {
    }

    public AssignIntStmt(Long value, int target) {
        this.value = value;
        this.target = target;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

  @Override
  public STMT_TYPE getType() {
    return STMT_TYPE.ASSIGN_INT;
  }

  @Override
  public int maxLocal() {
    return target;
  }

  @Override
  public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssignIntStmt that = (AssignIntStmt) o;

        if (target != that.target) return false;
        if (getFile() != that.getFile()) return false;
        if (getCol() != that.getCol()) return false;
        if (getRow() != that.getRow()) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + target;
        result = 31 * result + getFile();
        result = 31 * result + getCol();
        result = 31 * result + getRow();
        return result;
    }

    @Override
    public String toString() {
        return "AssignIntStmt{" + "value=" + value + ", target=" + target + ", file=" + getFile() + ", col=" + getCol() + ", row=" + getRow() + '}';
    }
}
