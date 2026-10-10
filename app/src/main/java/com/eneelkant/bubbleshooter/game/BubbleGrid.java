package com.eneelkant.bubbleshooter.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Offset-row bubble grid with match-3 clearing and floating-group removal.
 * Even rows have {@code columns} cells; odd rows have {@code columns - 1}.
 */
public final class BubbleGrid {
    public static final int MIN_MATCH = 3;

    private final int rows;
    private final int columns;
    private final BubbleColor[][] cells;

    public BubbleGrid(int rows, int columns) {
        if (rows < 3 || columns < 4) {
            throw new IllegalArgumentException("Grid too small");
        }
        this.rows = rows;
        this.columns = columns;
        this.cells = new BubbleColor[rows][columns];
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int maxColumnsForRow(int row) {
        return (row % 2 == 0) ? columns : columns - 1;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < maxColumnsForRow(row);
    }

    public BubbleColor get(int row, int col) {
        if (!inBounds(row, col)) {
            return null;
        }
        return cells[row][col];
    }

    public void set(int row, int col, BubbleColor color) {
        if (!inBounds(row, col)) {
            throw new IllegalArgumentException("Out of bounds: " + row + "," + col);
        }
        cells[row][col] = color;
    }

    public void clear(int row, int col) {
        if (inBounds(row, col)) {
            cells[row][col] = null;
        }
    }

    public void fillTopRows(int filledRows, int colorCount, Random random) {
        int limit = Math.min(filledRows, rows);
        for (int r = 0; r < limit; r++) {
            int cols = maxColumnsForRow(r);
            for (int c = 0; c < cols; c++) {
                cells[r][c] = BubbleColor.randomFrom(random, colorCount);
            }
        }
    }

    public int countFilled() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < maxColumnsForRow(r); c++) {
                if (cells[r][c] != null) {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean isCleared() {
        return countFilled() == 0;
    }

    public boolean reachesCeiling() {
        // Loss line: any bubble occupying the last playable row.
        int lossRow = rows - 1;
        for (int c = 0; c < maxColumnsForRow(lossRow); c++) {
            if (cells[lossRow][c] != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Neighbors for an offset grid (pointy-top hex approximation using rows).
     */
    public List<GridPos> neighbors(int row, int col) {
        List<GridPos> result = new ArrayList<>(6);
        boolean odd = row % 2 == 1;
        int[][] deltas = odd
                ? new int[][]{{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, 0}, {1, 1}}
                : new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
        for (int[] d : deltas) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (inBounds(nr, nc)) {
                result.add(new GridPos(nr, nc));
            }
        }
        return result;
    }

    public List<GridPos> findCluster(int row, int col) {
        List<GridPos> cluster = new ArrayList<>();
        BubbleColor color = get(row, col);
        if (color == null) {
            return cluster;
        }
        Queue<GridPos> queue = new ArrayDeque<>();
        Set<GridPos> seen = new HashSet<>();
        GridPos start = new GridPos(row, col);
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty()) {
            GridPos pos = queue.remove();
            cluster.add(pos);
            for (GridPos n : neighbors(pos.row, pos.col)) {
                if (!seen.contains(n) && color == get(n.row, n.col)) {
                    seen.add(n);
                    queue.add(n);
                }
            }
        }
        return cluster;
    }

    /**
     * After snapping a bubble, clear match clusters (>=3) and then floating groups.
     * @return number of bubbles removed
     */
    public int resolveAfterSnap(int row, int col) {
        List<GridPos> cluster = findCluster(row, col);
        int removed = 0;
        if (cluster.size() >= MIN_MATCH) {
            for (GridPos pos : cluster) {
                clear(pos.row, pos.col);
                removed++;
            }
            removed += dropFloatingBubbles();
        }
        return removed;
    }

    public int dropFloatingBubbles() {
        Set<GridPos> anchored = new HashSet<>();
        Queue<GridPos> queue = new ArrayDeque<>();
        for (int c = 0; c < maxColumnsForRow(0); c++) {
            if (cells[0][c] != null) {
                GridPos p = new GridPos(0, c);
                anchored.add(p);
                queue.add(p);
            }
        }
        while (!queue.isEmpty()) {
            GridPos pos = queue.remove();
            for (GridPos n : neighbors(pos.row, pos.col)) {
                if (!anchored.contains(n) && get(n.row, n.col) != null) {
                    anchored.add(n);
                    queue.add(n);
                }
            }
        }
        int removed = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < maxColumnsForRow(r); c++) {
                if (cells[r][c] != null) {
                    GridPos p = new GridPos(r, c);
                    if (!anchored.contains(p)) {
                        cells[r][c] = null;
                        removed++;
                    }
                }
            }
        }
        return removed;
    }

    /**
     * Finds the nearest empty cell to a world hit near an occupied neighbor.
     */
    public GridPos findSnapCell(int nearRow, int nearCol, float preferCol) {
        // Prefer empty neighbor of the contacted bubble.
        List<GridPos> candidates = new ArrayList<>();
        if (inBounds(nearRow, nearCol) && get(nearRow, nearCol) != null) {
            for (GridPos n : neighbors(nearRow, nearCol)) {
                if (get(n.row, n.col) == null) {
                    candidates.add(n);
                }
            }
        }
        if (candidates.isEmpty()) {
            // Fallback: any empty cell in nearby rows.
            for (int r = Math.max(0, nearRow - 1); r <= Math.min(rows - 1, nearRow + 1); r++) {
                for (int c = 0; c < maxColumnsForRow(r); c++) {
                    if (get(r, c) == null) {
                        candidates.add(new GridPos(r, c));
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        GridPos best = candidates.get(0);
        float bestScore = Float.MAX_VALUE;
        for (GridPos c : candidates) {
            float score = Math.abs(c.col - preferCol) + Math.abs(c.row - nearRow) * 0.35f;
            if (score < bestScore) {
                bestScore = score;
                best = c;
            }
        }
        return best;
    }
}
