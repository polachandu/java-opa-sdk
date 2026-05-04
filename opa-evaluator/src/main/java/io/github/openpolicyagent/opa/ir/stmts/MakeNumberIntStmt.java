package io.github.openpolicyagent.opa.ir.stmts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * MakeNumberIntStmt constructs a local variable that refers to an integer value.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the `StmtDeserializer` run again.
@JsonDeserialize
public class MakeNumberIntStmt extends BaseStmt {
    public static final String StmtType = "MakeNumberIntStmt";

    @JsonProperty("value")
    private long value;

    @JsonProperty("target")
    private int target;

    public MakeNumberIntStmt(long value, int target) {
        this.value = value;
        this.target = target;
    }

    public MakeNumberIntStmt() {
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
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
    return STMT_TYPE.MAKE_NUMBER_INT;
    }

  @Override
  public int maxLocal() {
    return target;
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MakeNumberIntStmt that = (MakeNumberIntStmt) o;

        if (value != that.value) return false;
        if (target != that.target) return false;
        if (getFile() != that.getFile()) return false;
        if (getCol() != that.getCol()) return false;
        return getRow() == that.getRow();
    }

    @Override
    public int hashCode() {
        int result = (int) (value ^ (value >>> 32));
        result = 31 * result + target;
        result = 31 * result + getFile();
        result = 31 * result + getCol();
        result = 31 * result + getRow();
        return result;
    }

    @Override
    public String toString() {
        return "MakeNumberIntStmt{"
                + "value="
                + value
                + ", target="
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
