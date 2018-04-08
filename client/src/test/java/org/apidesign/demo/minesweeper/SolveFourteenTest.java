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

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import net.java.html.BrwsrCtx;
import net.java.html.json.Models;
import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(BrowserRunner.class)
public class SolveFourteenTest {
    @Test
    public void solveForteenUnknowns() throws Exception {
        InputStream is = getClass().getResourceAsStream("14.json");
        assertNotNull("14.json found", is);
        Mines model = Models.parse(BrwsrCtx.findDefault(Fairness.class), Mines.class, is);
        assertNotNull("14 module loaded", model);
        assertEquals("Non empty model loaded", MinesModel.SquareType.N_1, Fairness.at(model, 3, 0).getState());

        Fairness fair = new Fairness(model, null);

        dumpMines(fair, System.err);

        fair.run();

        assertTrue("finished", fair.compute(0));
        List<Square> safe = dumpMines(fair, System.err);
        assertEquals("four safe places", 4, safe.size());
    }

    private List<Square> dumpMines(Fairness fair, final PrintStream ps) {
        List<Square> safe = new ArrayList<>();
        fair.seachSquares((x, y, sq, m) -> {
            if (x == 0) {
                ps.println();
            }
            char ch = '?';
            if (sq.getState().isVisible()) {
                if (sq.getState() == MinesModel.SquareType.N_0) {
                    ch = ' ';
                } else {
                    ch = (char) ('0' + sq.getState().ordinal());
                }
            } else if (sq.getState().isUnknown()) {
                if (fair.isSafe(x, y)) {
                    ch = 'X';
                    safe.add(sq);
                }
            }
            ps.print(ch);
            ps.print(' ');
            return false;
        });
        ps.println();
        return safe;
    }

}
