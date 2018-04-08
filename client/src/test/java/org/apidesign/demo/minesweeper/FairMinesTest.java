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
    public void thereHasToBeBombAtOneOne() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        m.setMines(3);
        FairMines.at(m, 0, 0).setState(SquareType.N_2);
        FairMines.at(m, 2, 2).setState(SquareType.N_2);

        FairMines compute = new FairMines(m, null);
        compute.run();

        int[] found = { -1, -1 };
        int[] unsafeCounter = { 0 };
        compute.seachSquares((x, y, sq) -> {
            if (!sq.getUnsafe().isEmpty()) {
                unsafeCounter[0]++;
                found[0] = x;
                found[1] = y;
                System.err.println(x + ":" + y + " = " + sq.getUnsafe());
            }
            return false;
        });

        assertEquals("Square 1, 1 has to contain the bomb!", 1, unsafeCounter[0]);
        assertEquals("Square 1,1 x", 1, found[0]);
        assertEquals("Square 1,1 y", 1, found[1]);
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
        compute.seachSquares((x, y, sq) -> {
            if (sq.getState() == SquareType.UNKNOWN && sq.getUnsafe().isEmpty()) {
                safeCounter[0]++;
                assertEquals("Row 0 for " + sq, 0, y);
            }
            return false;
        });

        assertEquals("No square in row 0 can contain the bomb", 3, safeCounter[0]);
    }

}
