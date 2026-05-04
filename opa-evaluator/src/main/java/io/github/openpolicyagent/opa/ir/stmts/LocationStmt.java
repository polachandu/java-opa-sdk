package io.github.openpolicyagent.opa.ir.stmts;

import io.github.openpolicyagent.opa.ir.Location;

public interface LocationStmt {
    Location setLocation(int file, int row, int col);

    Location getLocation();
}
