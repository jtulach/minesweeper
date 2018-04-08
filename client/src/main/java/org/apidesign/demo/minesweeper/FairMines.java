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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.apidesign.demo.minesweeper.MinesModel.SquareModel;

final class FairMines implements Runnable {
    private final Mines mines;
    private final int minesClicks;
    private final int count;
    private final List<Bomb> bombs;
    private final Executor reschedule;
    private boolean finished;
    private int countConsistent;

    FairMines(Mines mines, Executor reschedule) {
        this.mines = mines;
        this.minesClicks = mines.getClicks();
        this.reschedule = reschedule;
        this.count = mines.getMines();
        this.countConsistent = 0;
        prepareNoMinesEverythingIsSafe();

        List<Bomb> tmp = new ArrayList<>(count);
        Bomb first = new Bomb(mines, 0, 0);
        Bomb b = first;
        for (int i = 0; i < count; i++) {
            tmp.add(b);
            b = nextLocation(mines, b);
        }
        bombs = tmp;
    }

    private boolean oneRoundCheck() {
        if (finished) {
            return true;
        }

        if (checkConsistent(bombs)) {
            countConsistent++;
            markUnsafe(bombs);
        }
        if (nextBombsLocations(count - 1)) {
            return false;
        } else {
            finished = true;
            return true;
        }
    }

    private boolean nextBombsLocations(int i) {
        if (i < 0) {
            return false;
        }
        Bomb next = nextLocation(mines, bombs.get(i));
        if (next != null) {
            bombs.set(i, next);
            return true;
        } else {
            if (nextBombsLocations(i - 1)) {
                bombs.set(i, nextLocation(mines, bombs.get(i - 1)));
                return true;
            }
            return false;
        }
    }

    private boolean compute(int time) {
        long up = time < 0 ? Long.MAX_VALUE : System.currentTimeMillis() + time;
        do {
            if (oneRoundCheck()) {
                int[] oneSafe = seachSquares((__, ___, sq, m) -> {
                    return sq.getState().isUnknown() && m.isSafe(countConsistent);
                });

                if (oneSafe == null) {
                    seachSquares((__, ___, sq, m) -> {
                        m.unsafe(null, null);
                        return false;
                    });
                }
                return true;
            }
        } while (up > System.currentTimeMillis());
        return false;
    }


    @Override
    public void run() {
        if (mines.getClicks() != minesClicks) {
            return;
        }
        if (!compute(reschedule == null ? -1 : 50)) {
            reschedule.execute(this);
        }
    }


    static int countMinesAround(Mines model, int x, int y, boolean bombs) {
        return minesAt(model, x - 1, y - 1, bombs)
            + minesAt(model, x - 1, y, bombs)
            + minesAt(model, x - 1, y + 1, bombs)
            + minesAt(model, x, y - 1, bombs)
            + minesAt(model, x, y + 1, bombs)
            + minesAt(model, x + 1, y - 1, bombs)
            + minesAt(model, x + 1, y, bombs)
            + minesAt(model, x + 1, y + 1, bombs);
    }

    private static int minesAt(Mines model, int x, int y, boolean bombs) {
        if (y < 0 || y >= model.getRows().size()) {
            return 0;
        }
        final List<Square> columns = model.getRows().get(y).getColumns();
        if (x < 0 || x >= columns.size()) {
            return 0;
        }
        Square sq = columns.get(x);
        if (bombs) {
            SquareModel[] arr = { null };
            sq.read(false, arr);
            return arr[0].getBomb() != null ? 1 : 0;
        } else {
            return sq.isMine() ? 1 : 0;
        }
    }

    static boolean isConsistent(Mines m, boolean bombs) {
        for (int row = 0; row < m.getRows().size(); row++) {
            Row r = m.getRows().get(row);
            for (int col = 0; col < r.getColumns().size(); col++) {
                Square sq = r.getColumns().get(col);
                if (sq.getState().isVisible()) {
                    int around = countMinesAround(m, col, row, bombs);
                    if (around != sq.getState().ordinal()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    int[] seachSquares(VisitSquare v) {
        return searchSquares(0, 0, v);
    }

    boolean isSafe(int x, int y) {
        return at(x, y).isSafe(countConsistent);
    }

    private SquareModel at(int x, int y) {
        Square sq = mines.getRows().get(y).getColumns().get(x);
        SquareModel[] arr = { null };
        sq.read(false, arr);
        assert arr[0] != null;
        return arr[0];
    }
    
    private int[] searchSquares(int beginX, int beginY, VisitSquare v) {
        int y = 0;
        for (Row row : mines.getRows()) {
            int x = 0;
            if (y >= beginY) {
                boolean check = y == beginY;
                for (Square sq : row.getColumns()) {
                    if (!check || x >= beginX) {
                        SquareModel[] arr = { null };
                        sq.read(false, arr);
                        assert arr[0] != null;
                        if (v.visit(x, y, sq, arr[0])) {
                            return new int[] { x, y };
                        }
                    }
                    x++;
                }
            }
            y++;
        }
        return null;
    }

    private void prepareNoMinesEverythingIsSafe() {
        seachSquares((__, ___, sq, m) -> {
            m.clean();
            return false;
        });
    }
    
    static Square at(Mines tmp, int x, int y) {
        return tmp.getRows().get(y).getColumns().get(x);
    }

    private boolean checkConsistent(List<Bomb> bombs) {
        seachSquares((x, y, sq, m) -> {
            m.setBomb(null);
            return false;
        });

        for (Bomb bomb : bombs) {
            if (bomb == null) {
                return false;
            }
            final Square sq = at(mines, bomb.x, bomb.y);
            if (!sq.getState().isUnknown() || sq.isMine()) {
                return false;
            }
            SquareModel[] arr = { null };
            sq.read(false, arr);
            arr[0].setBomb(bomb);
        }
        return isConsistent(mines, true);
    }

    private void markUnsafe(List<Bomb> bombs) {
        List<Bomb> locations = new ArrayList<>(bombs);
        seachSquares((x, y, sq, m) -> {
            m.incEmpty();
            return false;
        });
        for (Bomb b : bombs) {
            final Square square = at(mines, b.x, b.y);
            square.unsafe(locations);
            square.decEmptyIncMine();
        }
    }

    private static Bomb nextLocation(Mines mines, Bomb b) {
        if (b == null) {
            return null;
        }
        int nextX = b.x + 1;
        if (nextX < mines.getWidth()) {
            return new Bomb(mines, nextX, b.y);
        } else {
            int nextY = b.y + 1;
            if (nextY < mines.getHeight()) {
                return new Bomb(mines, 0, nextY);
            } else {
                return null;
            }
        }
    }

    static interface VisitSquare {
        public boolean visit(int x, int y, Square sq, SquareModel m);
    }

    static class Bomb {
        final int x;
        final int y;

        Bomb(Mines mines, int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Bomb{" + "x=" + x + ", y=" + y + '}';
        }
    }
}
