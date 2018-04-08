/*
 * The MIT License
 *
 * Copyright 2018 API Design.
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
        FairMines.at(m, 1, 1).setState(SquareType.N_3);

        FairMines compute = new FairMines(m, null);
        compute.run();

        assertCounts(compute, 0, 8);
    }

    @Test
    public void thereCannotBeBombsInTwoCorners() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        m.setMines(3);
        FairMines.at(m, 0, 0).setState(SquareType.N_2);
        FairMines.at(m, 2, 2).setState(SquareType.N_2);

        FairMines compute = new FairMines(m, null);
        compute.run();

        assertCounts(compute, 2, 5);
        assertSafe("Left bottom corner", compute, 0, 2);
        assertSafe("Right top corner", compute, 2, 0);
    }

    private void assertSafe(String msg, FairMines compute, int x, int y) {
        assertTrue(msg, compute.at(x, y).isSafe(compute.getCountConsistent()));
    }

    private void assertCounts(FairMines compute, int safe, int unsafe) {
        StringBuilder sb = new StringBuilder();
        int[] counter = { 0, 0 };
        compute.seachSquares((x, y, sq, sqModel) -> {
            if (sq.getState() != SquareType.UNKNOWN) {
                return false;
            }
            if (!sqModel.isSafe(compute.getCountConsistent())) {
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
        FairMines.at(m, 0, 2).setState(SquareType.N_2);
        FairMines.at(m, 1, 2).setState(SquareType.N_3);
        FairMines.at(m, 2, 2).setState(SquareType.N_2);

        FairMines compute = new FairMines(m, null);
        compute.run();

        int[] safeCounter = { 0 };
        compute.seachSquares((x, y, sq, sqModel) -> {
            if (sq.getState() == SquareType.UNKNOWN && sqModel.isSafe(compute.getCountConsistent())) {
                safeCounter[0]++;
                assertEquals("Row 0 for " + sq, 0, y);
            }
            return false;
        });

        assertEquals("No square in row 0 can contain the bomb", 3, safeCounter[0]);
    }

}
