package io.github.open_policy_agent.opa.ir.stmts;

import io.github.open_policy_agent.opa.ir.Location;

public interface LocationStmt {
    Location setLocation(int file, int row, int col);

    Location getLocation();
}
