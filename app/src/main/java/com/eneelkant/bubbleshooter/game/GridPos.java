package com.eneelkant.bubbleshooter.game;

import java.util.Objects;

/** Row/column position in the offset bubble grid. */
public final class GridPos {
    public final int row;
    public final int col;

    public GridPos(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GridPos)) {
            return false;
        }
        GridPos gridPos = (GridPos) o;
        return row == gridPos.row && col == gridPos.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "GridPos{r=" + row + ",c=" + col + "}";
    }
}
