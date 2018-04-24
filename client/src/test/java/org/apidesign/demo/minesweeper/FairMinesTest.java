/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.apidesign.demo.minesweeper;

import org.apidesign.demo.minesweeper.MinesModel.SquareType;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class FairMinesTest {
    public FairMinesTest() {
    }

    @Test
    public void uncertainPosition() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        m.setMines(3);
        Fairness.at(m, 1, 1).setState(SquareType.N_3);

        Fairness compute = new Fairness(m, null);
        compute.run();

        assertCounts(compute, 0, 8);
    }

    @Test
    public void thereCannotBeBombsInTwoCorners() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        m.setMines(3);
        Fairness.at(m, 0, 0).setState(SquareType.N_2);
        Fairness.at(m, 2, 2).setState(SquareType.N_2);

        Fairness compute = new Fairness(m, null);
        compute.run();

        assertCounts(compute, 2, 5);
        assertSafe("Left bottom corner", compute, 0, 2);
        assertSafe("Right top corner", compute, 2, 0);
    }

    @Test
    public void exposeInTheCorner() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        m.setMines(3);
        Fairness.at(m, 1, 0).setState(SquareType.N_1);
        Fairness.at(m, 1, 1).setState(SquareType.N_3);
        Fairness.at(m, 0, 1).setState(SquareType.N_1);

        Fairness compute = new Fairness(m, null);
        compute.run();

        int[] found = compute.seachSquares((x, y, sq, sqM) -> {
            if (x != 2 && y != 0) {
                return false;
            }
            assertEquals("Even in corner, we count expose as 8", sqM.getBomb().expose, 8);
            return true;
        });

        assertNotNull("Found the corner", found);
    }

    @Test
    public void oneInCornerImpliesBomb() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        m.setMines(1);
        for (Row r : m.getRows()) {
            for (Square s : r.getColumns()) {
                s.setState(SquareType.N_0);
            }
        }
        Fairness.at(m, 0, 0).setState(SquareType.UNKNOWN);
        Fairness.at(m, 1, 0).setState(SquareType.N_1);
        Fairness.at(m, 1, 1).setState(SquareType.N_1);
        Fairness.at(m, 0, 1).setState(SquareType.N_1);

        Fairness compute = new Fairness(m, null);

        assertTrue("There has to be bomb", Fairness.at(m, 0, 0).isBomb());
    }

    @Test
    public void fourCornerBombs() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        m.setMines(4);
        Fairness.at(m, 1, 1).setState(SquareType.N_4);
        Fairness.at(m, 1, 0).setState(SquareType.N_2);
        Fairness.at(m, 0, 1).setState(SquareType.N_2);
        Fairness.at(m, 1, 2).setState(SquareType.N_2);
        Fairness.at(m, 2, 1).setState(SquareType.N_2);

        Fairness compute = new Fairness(m, null);

        assertTrue("There has to be bomb in corner1", Fairness.at(m, 0, 0).isBomb());
        assertTrue("There has to be bomb in corner2", Fairness.at(m, 2, 0).isBomb());
        assertTrue("There has to be bomb in corner3", Fairness.at(m, 0, 2).isBomb());
        assertTrue("There has to be bomb in corner4", Fairness.at(m, 2, 2).isBomb());
    }

    @Test
    public void nextArrFromNextLine() {
        int[] arr = { 0, 5, 6 };
        assertNextLoc(arr, 7);
        assertArr(arr, 1, 2, 3);
        assertNextLoc(arr, 7);
        assertNextLoc(arr, 7);
        assertNextLoc(arr, 7);
        assertArr(arr, 1, 2, 6);
        assertNextLoc(arr, 7);
        assertArr(arr, 1, 3, 4);
        assertNextLoc(arr, 7);
        assertNextLoc(arr, 7);
        assertArr(arr, 1, 3, 6);
        assertNextLoc(arr, 7);
        assertNextLoc(arr, 7);
        assertArr(arr, 1, 4, 6);
        assertNextLoc(arr, 7);
        assertArr(arr, 1, 5, 6);
        assertNextLoc(arr, 7);
        assertArr(arr, 2, 3, 4);
        assertNextLoc(arr, 7);
        assertNextLoc(arr, 7);
        assertArr(arr, 2, 3, 6);
        assertNextLoc(arr, 7);
        assertArr(arr, 2, 4, 5);
        assertNextLoc(arr, 7);
        assertArr(arr, 2, 4, 6);
        assertNextLoc(arr, 7);
        assertArr(arr, 2, 5, 6);
        assertNextLoc(arr, 7);
        assertArr(arr, 3, 4, 5);
        assertNextLoc(arr, 7);
        assertArr(arr, 3, 4, 6);
        assertNextLoc(arr, 7);
        assertArr(arr, 3, 5, 6);
        assertNextLoc(arr, 7);
        assertArr(arr, 4, 5, 6);
        assertFalse("No more", Fairness.nextBombsLocations(arr, 7));
    }

    private static void assertNextLoc(int[] arr, int limit) {
        assertTrue("Next location available", Fairness.nextBombsLocations(arr, limit));
    }

    private static void assertArr(int[] arr, int... exp) {
        Assert.assertArrayEquals(exp, arr);
    }

    private void assertSafe(String msg, Fairness compute, int x, int y) {
        assertTrue(msg, compute.isSafe(x, y));
    }

    private void assertCounts(Fairness compute, int safe, int unsafe) {
        StringBuilder sb = new StringBuilder();
        int[] counter = { 0, 0 };
        compute.seachSquares((x, y, sq, sqModel) -> {
            if (sq.getState() != SquareType.UNKNOWN) {
                return false;
            }
            if (!compute.isSafe(x, y)) {
                counter[1]++;
                sb.append("\nunsafe ").append(x).append(":").append(y).append(" = ").append(sqModel.getUnsafe());
            } else {
                counter[0]++;
                sb.append("\nsafe   ").append(x).append(":").append(y);
            }
            return false;
        });
        assertEquals("Safe count is OK" + sb, safe, counter[0]);
        assertEquals("Unsafe count is OK" + sb, unsafe, counter[1]);
    }

    @Test
    public void noBombAtZeroRow() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        m.setMines(3);
        Fairness.at(m, 0, 2).setState(SquareType.N_2);
        Fairness.at(m, 1, 2).setState(SquareType.N_3);
        Fairness.at(m, 2, 2).setState(SquareType.N_2);

        Fairness compute = new Fairness(m, null);
        compute.run();

        int[] safeCounter = { 0 };
        compute.seachSquares((x, y, sq, sqModel) -> {
            if (sq.getState() == SquareType.UNKNOWN && compute.isSafe(x, y)) {
                safeCounter[0]++;
                assertEquals("Row 0 for " + sq, 0, y);
            }
            return false;
        });

        assertEquals("No square in row 0 can contain the bomb", 3, safeCounter[0]);
    }

}
