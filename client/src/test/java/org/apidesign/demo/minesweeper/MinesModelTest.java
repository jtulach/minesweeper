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

import org.apidesign.demo.minesweeper.MinesModel.SquareType;
import org.junit.Assert;
import org.junit.Test;

public class MinesModelTest {
    @Test public void tenTenTen() {
        Mines m = new Mines();
        m.init(10, 10, 10);
        
        assertEquals(m.getRows().size(), 10, "Ten rows");
        int cnt = 0;
        for (Row row : m.getRows()) {
            assertEquals(row.getColumns().size(), 10, "Ten columns in each row");
            for (Square square : row.getColumns()) {
                if (square.isMine()) {
                    cnt++;
                }
            }
        }
        assertEquals(cnt, 10, "Ten mines");
    }
    
    @Test public void clickRemovesMarkedSign() {
        Mines m = new Mines();
        m.init(10, 10, 10);
        
        final Square sq = m.getRows().get(5).getColumns().get(5);
        MinesModel.markMine(m);
        m.click(sq);
        
        assertEquals(sq.getState(), SquareType.MARKED, "Changed to marked");
        
        m.click(sq);
        
        assertEquals(sq.getState(), SquareType.UNKNOWN, "Changed back to unknown");
    }

    @Test public void gameWonWhenAllMarked() {
        Mines m = new Mines();
        m.init(10, 10, 10);
        
        for (Row row : m.getRows()) {
            for (Square sq : row.getColumns()) {
                if (sq.isMine()) {
                    MinesModel.markMine(m);
                    m.click(sq);
                }
            }
        }
        
        assertEquals(m.getState(), MinesModel.GameState.WON, "All mines found. You have won!");
    }
    
    @Test public void gameNotWonWhenTooMuchIsMarked() {
        Mines m = new Mines();
        m.init(10, 10, 10);
        
        Square additional = null;
        for (Row row : m.getRows()) {
            for (Square sq : row.getColumns()) {
                if (sq.isMine()) {
                    MinesModel.markMine(m);
                    m.click(sq);
                } else if (additional == null) {
                    MinesModel.markMine(m);
                    m.click(additional = sq);
                }
            } 
        }
        
        assertEquals(m.getState(), MinesModel.GameState.IN_PROGRESS, "One additional mine is marked!");
        
        // remove the mark
        m.click(additional);
        
        assertEquals(m.getState(), MinesModel.GameState.WON, "All mines found. You have won!");
        
        
    }
    
    @Test public void bombsSet() {
        Mines m = new Mines();
        m.init(10, 10, 0);
        
        set(m, 1, 1, SquareType.UNKNOWN, true);
        set(m, 0, 0, SquareType.N_0, false);
        set(m, 0, 1, SquareType.N_2, false);
        set(m, 0, 2, SquareType.N_3, false);
        set(m, 1, 0, SquareType.N_5, false);
        set(m, 2, 0, SquareType.N_8, false);
        set(m, 3, 0, SquareType.N_2, false);
        
        m.computeMines();
        
        assertSquare(m, 0, 0, SquareType.N_1);
        assertSquare(m, 1, 0, SquareType.N_1);
        assertSquare(m, 2, 0, SquareType.N_1);
        assertSquare(m, 3, 0, SquareType.N_0);
        assertSquare(m, 0, 1, SquareType.N_1);
        assertSquare(m, 0, 2, SquareType.N_1);
        assertSquare(m, 2, 2, SquareType.UNKNOWN);
    }
    
    @Test public void gameIsWonIfAllMinesDiscovered() {
        Mines m = new Mines();
        m.init(2, 1, 0);
        set(m, 0, 0, SquareType.UNKNOWN, true);
        m.computeMines();
        Assert.assertEquals(m.getState(), MinesModel.GameState.IN_PROGRESS);
        set(m, 1, 0, SquareType.N_0, false);
        m.computeMines();
        assertEquals(m.getState(), MinesModel.GameState.WON, "All non-bomb squares discovered");
    }

    @Test public void unhideNeibourghsOfEmptyPieces() {
        Mines m = new Mines();
        m.init(3, 3, 0);
        set(m, 0, 0, SquareType.UNKNOWN, true);
        m.click(m.getRows().get(2).getColumns().get(2));

        assertSquare(m, 0, 0, SquareType.DISCOVERED);
        assertSquare(m, 0, 1, SquareType.N_1);
        assertSquare(m, 1, 1, SquareType.N_1);
        assertSquare(m, 1, 0, SquareType.N_1);
        assertSquare(m, 2, 0, SquareType.N_0);
        assertSquare(m, 2, 1, SquareType.N_0);
        assertSquare(m, 2, 2, SquareType.N_0);
        assertSquare(m, 1, 2, SquareType.N_0);
    }
    
    private static void set(Mines m, int x, int y, SquareType squareType, boolean mine) {
        Square sq = m.getRows().get(y).getColumns().get(x);
        sq.setState(squareType);
        sq.setMine(mine);
    }
    
    private static void assertSquare(Mines m, int x, int y, SquareType t) {
        Square sq = m.getRows().get(y).getColumns().get(x);
        assertEquals(sq.getState(), t, "Expecting at " + x + ":" + y);
    }

    private static void assertEquals(Object act, Object exp, String msg) {
        Assert.assertEquals(msg, exp, act);
    }
}
