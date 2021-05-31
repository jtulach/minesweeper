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
import org.apidesign.demo.minesweeper.MinesModel.SquareType;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BrowserRunner.class)
public class MinesModelTest {
    @Test public void tenTenTen() {
        Mines m = new Mines();
        m.init(10, 10, 10);
        
        assertEquals("Ten rows", 10, m.getRows().size());
        int cnt = 0;
        for (Row row : m.getRows()) {
            assertEquals("Ten columns in each row", 10, row.getColumns().size());
            for (Square square : row.getColumns()) {
                if (square.isMine()) {
                    cnt++;
                }
            }
        }
        assertEquals("Ten mines", 10, cnt);
    }
    
    @Test public void clickRemovesMarkedSign() {
        Mines m = new Mines();
        m.init(10, 10, 10);
        
        final Square sq = m.getRows().get(5).getColumns().get(5);
        MinesModel.markMine(m);
        m.click(sq);
        
        assertEquals("Changed to marked", SquareType.MARKED, sq.getState());
        
        m.click(sq);
        
        assertEquals("Changed back to unknown", SquareType.UNKNOWN, sq.getState());
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
        
        assertEquals("All mines found. You have won!", MinesModel.GameState.WON, m.getState());
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
        
        assertEquals("One additional mine is marked!", MinesModel.GameState.IN_PROGRESS, m.getState());
        
        // remove the mark
        m.click(additional);
        
        assertEquals("All mines found. You have won!", MinesModel.GameState.WON, m.getState());
        
        
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
        assertEquals("In progress", MinesModel.GameState.IN_PROGRESS, m.getState());
        set(m, 1, 0, SquareType.N_0, false);
        m.computeMines();
        assertEquals("All non-bomb squares discovered", MinesModel.GameState.WON, m.getState());
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
        assertEquals("Expecting at " + x + ":" + y, t, sq.getState());
    }
}
