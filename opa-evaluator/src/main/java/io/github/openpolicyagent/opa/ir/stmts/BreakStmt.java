package io.github.openpolicyagent.opa.ir.stmts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * BreakStmt represents a jump out of the current block. The index specifies how many blocks to jump
 * starting from zero (the current block). Execution will continue from the end of the block that is
 * jumped to.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the `StmtDeserializer` run again.
@JsonDeserialize
public class BreakStmt extends BaseStmt {
    public static final String StmtType = "BreakStmt";

    @JsonProperty("index")
    private int index;

    public BreakStmt() {
    }

    public BreakStmt(int index) {
        this.index = index;
    }

    public BreakStmt(int file, int col, int row) {
        super(file, col, row);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

  @Override
  public STMT_TYPE getType() {
    return STMT_TYPE.BREAK;
    }

  @Override
  public int maxLocal() {
    return -1;
  }

    @Override
    public String toString() {
        return "BreakStmt{"
                + "index="
                + index
                + ", file="
                + getFile()
                + ", col="
                + getCol()
                + ", row="
                + getRow()
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BreakStmt breakStmt = (BreakStmt) o;

        if (index != breakStmt.index) return false;
        if (getFile() != breakStmt.getFile()) return false;
        if (getCol() != breakStmt.getCol()) return false;
        return getRow() == breakStmt.getRow();
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + getFile();
        result = 31 * result + getCol();
        result = 31 * result + getRow();
        return result;
    }
}
