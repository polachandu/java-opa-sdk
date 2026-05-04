package io.github.openpolicyagent.opa.ir.stmts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.openpolicyagent.opa.ir.Frame;
import io.github.openpolicyagent.opa.ir.IREvaluationContext;

/**
 * NopStmt adds a nop instruction. Useful during development and debugging only.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
// subclasses have "same" @JsonDeserialize annotation as their parent class, therefore we add an
// empty one to
// avoid having the `StmtDeserializer` run again.
@JsonDeserialize
public class NopStmt extends BaseStmt {
    public static final String StmtType = "NopStmt";

    public NopStmt() {
    }

    public NopStmt(int file, int col, int row) {
        super(file, col, row);
    }

    public void evaluate(Frame parent, IREvaluationContext ctx) {
    }

  @Override
  public STMT_TYPE getType() {
    return STMT_TYPE.NOP;
    }

  @Override
  public int maxLocal() {
    return -1;
  }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NopStmt nopStmt = (NopStmt) o;

        if (getFile() != nopStmt.getFile()) return false;
        if (getCol() != nopStmt.getCol()) return false;
        return getRow() == nopStmt.getRow();
    }

    @Override
    public int hashCode() {
        int result = getFile();
        result = 31 * result + getCol();
        result = 31 * result + getRow();
        return result;
    }

    @Override
    public String toString() {
        return "NopStmt{" + "file=" + getFile() + ", col=" + getCol() + ", row=" + getRow() + '}';
    }
}
