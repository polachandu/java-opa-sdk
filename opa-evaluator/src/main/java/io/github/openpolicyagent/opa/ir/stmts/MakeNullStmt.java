package io.github.openpolicyagent.opa.ir.stmts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * MakeNullStmt constructs a local variable that refers to a null value.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the `StmtDeserializer` run again.
@JsonDeserialize
public class MakeNullStmt extends BaseStmt {
    public static final String StmtType = "MakeNullStmt";

    @JsonProperty("target")
    private int target;

    public MakeNullStmt(int target) {
        this.target = target;
    }

    public MakeNullStmt() {
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

  @Override
  public STMT_TYPE getType() {
    return STMT_TYPE.MAKE_NULL;
    }

  @Override
  public int maxLocal() {
    return target;
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MakeNullStmt that = (MakeNullStmt) o;

        if (target != that.target) return false;
        if (getFile() != that.getFile()) return false;
        if (getCol() != that.getCol()) return false;
        return getRow() == that.getRow();
    }

    @Override
    public int hashCode() {
        int result = target;
        result = 31 * result + getFile();
        result = 31 * result + getCol();
        result = 31 * result + getRow();
        return result;
    }

    @Override
    public String toString() {
        return "MakeNullStmt{"
                + "target="
                + target
                + ", file="
                + getFile()
                + ", col="
                + getCol()
                + ", row="
                + getRow()
                + '}';
    }
}
