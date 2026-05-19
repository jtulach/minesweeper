/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2020 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import net.java.html.junit.BrowserRunner;
import org.apidesign.demo.minesweeper.js.Grid;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests behavior of {@link Mines} model together with {@link Grid}.
 */
@RunWith(BrowserRunner.class)
public class MinesGridTest {
    @Test
    public void movePieceToALocation() throws Exception {
        try {
            var model = new Mines();
            var grid = new Grid(3, 2) {
                volatile int onDropCount;

                @Override
                protected boolean onDrop(int prevX, int prevY, int x, int y) {
                    onDropCount++;
                    var actions = new boolean[2];
                    model.onDrop(prevX, prevY, x, y, actions);
                    return actions[0];
                }
            };
            {
                model.withGrid(grid);
                model.init(3, 3, 2, null);
                grid.flush();
                // Initially pieces are spread around the board and they
                // return back to target area. Reset the counter.
                grid.onDropCount = 0;
                assertEquals("Two mines are pending", 2, grid.getRemaining());
            }

            {
                grid.moveTo(1, 1);
                grid.flush();
                assertEquals("First onDrop event received", 1, grid.onDropCount);
                assertEquals("One less is pending", 1, grid.getRemaining());
            }
            var sq = model.getRows().get(1).getColumns().get(1);
            assertEquals("Marked as a potential mine", MinesModel.SquareType.MARKED, sq.getState());

            {
                // unmark the mine
                model.click(sq);
                assertEquals("Immediatelly marked as unknown", MinesModel.SquareType.UNKNOWN, sq.getState());
                grid.flush();
                assertEquals("All pending", 2, grid.getRemaining());
            }

            {
                grid.moveTo(2, 2);
                grid.flush();
                assertEquals("One less is pending", 1, grid.getRemaining());
            }
            assertEquals("Previous square state remains", MinesModel.SquareType.UNKNOWN, sq.getState());
            var sq2 = model.getRows().get(2).getColumns().get(2);
            assertEquals("New square marked as mine", MinesModel.SquareType.MARKED, sq2.getState());
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
}
