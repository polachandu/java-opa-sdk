package io.github.openpolicyagent.opa.ir.stmts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.openpolicyagent.opa.ir.Location;
import io.github.openpolicyagent.opa.ir.Operand;
import io.github.openpolicyagent.opa.ir.vals.LocalVal;

public abstract class BaseStmt implements Stmt {
    @JsonIgnore
    private Location location;

    @JsonProperty("file")
    private int file; // index of source filename

    @JsonProperty("col")
    private int col; // column in the source file

    @JsonProperty("row")
    private int row; // row in the source file

    protected BaseStmt(int file, int col, int row) {
        this.file = file;
        this.col = col;
        this.row = row;
        this.location = new Location(file, col, row);
    }

    protected BaseStmt() {
    }

    @Override
    public Location setLocation(int file, int row, int col) {
        this.file = file;
        this.col = col;
        this.row = row;
        this.location = new Location(file, col, row);
        return this.location;
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    public Location setLocation(Location location) {
        this.location = location;
        return this.location;
    }

    public int getFile() {
        return file;
    }

    public void setFile(int file) {
        this.file = file;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

  /** Helper method to extract local value from an Operand if it contains a LocalVal */
  protected int getLocalFromOperand(Operand operand) {
    if (operand != null && operand.getVal() instanceof LocalVal) {
      return ((LocalVal) operand.getVal()).getValue();
    }
    return -1;
  }

//  public abstract String getType();
//
//  public abstract void evaluate(Frame caller, IREvaluationContext ctx);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseStmt baseStmt = (BaseStmt) o;

        if (file != baseStmt.file) return false;
        if (col != baseStmt.col) return false;
        return row == baseStmt.row;
    }

    @Override
    public int hashCode() {
        int result = file;
        result = 31 * result + col;
        result = 31 * result + row;
        return result;
    }
}
