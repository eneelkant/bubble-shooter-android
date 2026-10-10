package com.eneelkant.bubbleshooter.game;

import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BubbleGridTest {

    @Test
    public void oddRowsHaveOneFewerColumn() {
        BubbleGrid grid = new BubbleGrid(6, 8);
        assertEquals(8, grid.maxColumnsForRow(0));
        assertEquals(7, grid.maxColumnsForRow(1));
    }

    @Test
    public void matchThreeClearsCluster() {
        BubbleGrid grid = new BubbleGrid(8, 8);
        grid.set(0, 0, BubbleColor.RED);
        grid.set(0, 1, BubbleColor.RED);
        grid.set(1, 0, BubbleColor.RED);
        int removed = grid.resolveAfterSnap(1, 0);
        assertTrue(removed >= 3);
        assertNull(grid.get(0, 0));
        assertNull(grid.get(0, 1));
        assertNull(grid.get(1, 0));
    }

    @Test
    public void twoBubblesDoNotClear() {
        BubbleGrid grid = new BubbleGrid(8, 8);
        grid.set(0, 0, BubbleColor.BLUE);
        grid.set(0, 1, BubbleColor.BLUE);
        int removed = grid.resolveAfterSnap(0, 1);
        assertEquals(0, removed);
        assertEquals(BubbleColor.BLUE, grid.get(0, 0));
        assertEquals(BubbleColor.BLUE, grid.get(0, 1));
    }

    @Test
    public void floatingBubblesDropAfterMatch() {
        BubbleGrid grid = new BubbleGrid(8, 8);
        // Anchor chain of reds on top-left, with a green hanging only via the reds.
        grid.set(0, 0, BubbleColor.RED);
        grid.set(1, 0, BubbleColor.RED);
        grid.set(2, 0, BubbleColor.RED);
        grid.set(3, 0, BubbleColor.GREEN);
        int removed = grid.resolveAfterSnap(2, 0);
        assertTrue(removed >= 4);
        assertTrue(grid.isCleared() || grid.get(3, 0) == null);
        assertFalse(grid.reachesCeiling());
    }

    @Test
    public void fillTopRowsRespectsColorCount() {
        BubbleGrid grid = new BubbleGrid(10, 8);
        grid.fillTopRows(3, 3, new Random(42));
        assertTrue(grid.countFilled() > 0);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < grid.maxColumnsForRow(r); c++) {
                BubbleColor color = grid.get(r, c);
                assertTrue(color.ordinal() < 3);
            }
        }
    }

    @Test
    public void findClusterReturnsConnectedSameColor() {
        BubbleGrid grid = new BubbleGrid(8, 8);
        grid.set(0, 0, BubbleColor.YELLOW);
        grid.set(0, 1, BubbleColor.YELLOW);
        grid.set(0, 2, BubbleColor.PURPLE);
        List<GridPos> cluster = grid.findCluster(0, 0);
        assertEquals(2, cluster.size());
    }
}
