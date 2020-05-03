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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import org.apidesign.demo.minesweeper.MinesModel.SquareModel;
import org.apidesign.demo.minesweeper.MinesModel.SquareType;

final class Fairness implements Runnable {
    private final Mines mines;
    private final List<Bomb> unknowns;
    private final int[] bombs;
    private final Executor reschedule;
    private Stage stage;
    private int countConsistent;

    Fairness(Mines mines, Executor reschedule) {
        this.mines = mines;
        this.reschedule = reschedule;
        this.bombs = new int[mines.getMines()];
        for (int i = 0; i < this.bombs.length; i++) {
            this.bombs[i] = i;
        }
        this.countConsistent = 0;
        this.unknowns = prepareNoMinesEverythingIsSafe();
        this.stage = Stage.SAT;
    }

    private boolean oneRoundCheck() {
        if (stage == Stage.FINISHED) {
            return true;
        }
        if (unknowns.size() < bombs.length) {
            stage = Stage.FINISHED;
            return true;
        }

        Bomb[] select = new Bomb[bombs.length];
        for (int i = 0; i < bombs.length; i++) {
            select[i] = unknowns.get(bombs[i]);
        }
        List<Bomb> selectList = Arrays.asList(select);

        if (checkConsistent(selectList)) {
            countConsistent++;
            markUnsafe(selectList);
        }
        if (nextBombsLocations(bombs, unknowns.size())) {
            return false;
        } else {
            stage = Stage.FINISHED;
            seachSquares((x, y, sq, m) -> {
                if (x == 0) {
                    System.err.println("");
                }
                if (sq.getState() != SquareType.UNKNOWN) {
                    System.err.print(" _/_");
                } else {
                    System.err.print(" " + m.getCountMine() + "/" + m.getCountEmpty());
                }
                if (m.isSafe(countConsistent)) {
                    if (sq.getState() == MinesModel.SquareType.UNKNOWN) {
                        sq.setSafe(true);
                    }
                    sq.setBomb(false);
                } else {
                    if (m.getCountEmpty() == 0 && m.getCountMine() > 0) {
                        sq.setBomb(true);
                    }
                    sq.setSafe(false);
                }
                return false;
            });
            System.err.println("\n****************");
            return true;
        }
    }

    boolean compute(int time) {
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
        mines.computeFairness(this, reschedule);
    }


    static int countMinesAround(Mines model, int x, int y, VisitSquare<Integer> fn) {
        return countMinesAround(model, x, y, 0, fn);
    }
    static int countMinesAround(Mines model, int x, int y, int outValue, VisitSquare<Integer> fn) {
        return minesAt(model, x - 1, y - 1, outValue, fn)
            + minesAt(model, x - 1, y, outValue, fn)
            + minesAt(model, x - 1, y + 1, outValue, fn)
            + minesAt(model, x, y - 1, outValue, fn)
            + minesAt(model, x, y + 1, outValue, fn)
            + minesAt(model, x + 1, y - 1, outValue, fn)
            + minesAt(model, x + 1, y, outValue, fn)
            + minesAt(model, x + 1, y + 1, outValue, fn);
    }

    private static int minesAt(Mines model, int x, int y, int outValue, VisitSquare<Integer> fn) {
        if (y < 0 || y >= model.getRows().size()) {
            return outValue;
        }
        final List<Square> columns = model.getRows().get(y).getColumns();
        if (x < 0 || x >= columns.size()) {
            return outValue;
        }
        Square sq = columns.get(x);
        SquareModel[] arr = { null };
        sq.read(false, arr);
        assert arr[0] != null;
        return fn.visit(x, y, sq, arr[0]);
    }

    static boolean isConsistent(Mines m, boolean bombs) {
        for (int row = 0; row < m.getRows().size(); row++) {
            Row r = m.getRows().get(row);
            for (int col = 0; col < r.getColumns().size(); col++) {
                Square sq = r.getColumns().get(col);
                if (sq.getState().isVisible()) {
                    int around = countMinesAround(m, col, row, (x, y, __, sqm) -> {
                        return sqm.getBomb() != null ? 1 : 0;
                    });
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
        return stage.isFinished() && at(x, y).isSafe(countConsistent);
    }

    private SquareModel at(int x, int y) {
        Square sq = mines.getRows().get(y).getColumns().get(x);
        SquareModel[] arr = { null };
        sq.read(false, arr);
        assert arr[0] != null;
        return arr[0];
    }

    private int[] searchSquares(int beginX, int beginY, VisitSquare<Boolean> v) {
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

    private List<Bomb> prepareNoMinesEverythingIsSafe() {
        boolean changed[] = { false };

        List<Bomb> arr = new ArrayList<>();
        seachSquares((x, y, sq, m) -> {
            m.clean();
            if (sq.getState() == SquareType.UNKNOWN) {
                int visibleAround = countMinesAround(mines, x, y, 1, (__, ___, around, ____) -> {
                    MinesModel.SquareType s = around.getState();
                    return s.isVisible() ? 1 : 0;
                });
                arr.add(new Bomb(x, y, visibleAround));
                return false;
            }
            int unknownAround = countMinesAround(mines, x, y, 0, (ax, ay, around, ____) -> {
                MinesModel.SquareType s = around.getState();
                if (s.isVisible()) {
                    return 0;
                } else {
                    return 1;
                }
            });
            if (unknownAround == sq.getState().ordinal()) {
                countMinesAround(mines, x, y, 0, (ax, ay, around, ____) -> {
                    MinesModel.SquareType s = around.getState();
                    if (!s.isVisible()) {
                        changed[0] = true;
                        around.setBomb(true);
                        return 0;
                    }
                    return 0;
                });
            }
            return false;
        });
        Collections.sort(arr);

        int found = arr.size();
        while (--found >= 0) {
            Bomb mostExposed = arr.get(found);
            if (mostExposed.expose != 8) {
                break;
            }
            final Square sureBomb = at(mines, mostExposed.x, mostExposed.y);
            if (!sureBomb.isBomb()) {
                changed[0] = true;
                sureBomb.setBomb(true);
            }
        }

        return arr;
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
            if (!sq.getState().isUnknown()) {
                return false;
            }
            SquareModel[] arr = { null };
            sq.read(false, arr);
            if (arr[0].getBomb() != null) {
                return false;
            }
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

    static boolean nextBombsLocations(int[] indices, int maxLimit) {
        int roundLimit = maxLimit;
        for (int i = indices.length - 1; i >= 0; i--) {
            if (++indices[i] < roundLimit) {
                while (++i < indices.length) {
                    indices[i] = indices[i - 1] + 1;
                }
                return indices[indices.length - 1] < maxLimit;
            }
            roundLimit--;
        }
        return false;
    }

    static interface VisitSquare<R> {
        public R visit(int x, int y, Square sq, SquareModel m);
    }

    static class Bomb implements Comparable<Bomb> {
        final int x;
        final int y;
        final int expose;

        Bomb(int x, int y, int expose) {
            this.x = x;
            this.y = y;
            this.expose = expose;
        }

        @Override
        public String toString() {
            return "Bomb{" + "x=" + x + ", y=" + y + ", #=" + expose + '}';
        }

        @Override
        public int compareTo(Bomb o) {
            int d;
            d = expose - o.expose;
            if (d != 0) {
                return d;
            }
            d = x - o.x;
            if (d != 0) {
                return d;
            }
            return y - o.y;
        }
    }

    private enum Stage {
        SAT, FINISHED;

        boolean isFinished() {
            return this == FINISHED;
        }
    }
}
