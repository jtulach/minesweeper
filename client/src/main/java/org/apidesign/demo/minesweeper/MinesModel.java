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
import java.util.Random;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.Property;
import net.java.html.sound.AudioClip;
import org.apidesign.demo.minesweeper.js.OpenURL;

/**
 * Model of the mine field.
 */
@Model(className = "Mines", targetId = "", properties = {
    @Property(name = "state", type = MinesModel.GameState.class),
    @Property(name = "rows", type = Row.class, array = true),})
public final class MinesModel {

    enum GameState {

        IN_PROGRESS, MARKING_MINE, WON, LOST;
    }

    @ComputedProperty
    static String gameStyle(GameState state) {
        return state == GameState.MARKING_MINE ? "MARKING" : "PLAYING";
    }

    @Model(className = "Row", properties = {
        @Property(name = "columns", type = Square.class, array = true)
    })
    static class RowModel {
    }

    @Model(className = "Square", properties = {
        @Property(name = "state", type = SquareType.class),
        @Property(name = "mine", type = boolean.class)
    })
    static class SquareModel {

        @ComputedProperty
        static String style(SquareType state) {
            return state == null ? null : state.toString();
        }
    }

    public enum SquareType {

        N_0, N_1, N_2, N_3, N_4, N_5, N_6, N_7, N_8,
        UNKNOWN, EXPLOSION, DISCOVERED, MARKED;

        final boolean isVisible() {
            return name().startsWith("N_");
        }

        final SquareType moreBombsAround() {
            switch (this) {
                case EXPLOSION:
                case UNKNOWN:
                case DISCOVERED:
                case N_8:
                    return this;
            }
            return values()[ordinal() + 1];
        }
    }

    @ComputedProperty
    static boolean fieldShowing(GameState state) {
        return state != null;
    }

    @ComputedProperty
    static boolean gameInProgress(GameState state) {
        return state == GameState.IN_PROGRESS;
    }

    @Function
    static void showHelp(Mines model) {
        model.setState(null);
    }

    @Function
    static void smallGame(Mines model) {
        model.init(5, 5, 5);
    }

    @Function
    static void normalGame(Mines model) {
        model.init(10, 10, 10);
    }

    @Function
    static void giveUp(Mines model) {
        showAllBombs(model, SquareType.EXPLOSION);
        model.setState(GameState.LOST);
    }

    @Function
    static void markMine(Mines model) {
        if (model.getState() == GameState.IN_PROGRESS) {
            model.setState(GameState.MARKING_MINE);
        }
    }

    @ModelOperation
    static void init(Mines model, int width, int height, int mines) {
        List<Row> rows = model.getRows();
        if (rows.size() != height || rows.get(0).getColumns().size() != width) {
            rows = new ArrayList<Row>(height);
            for (int y = 0; y < height; y++) {
                Square[] columns = new Square[width];
                for (int x = 0; x < width; x++) {
                    columns[x] = new Square(SquareType.UNKNOWN, false);
                }
                rows.add(new Row(columns));
            }
        } else {
            for (Row row : rows) {
                for (Square sq : row.getColumns()) {
                    sq.setState(SquareType.UNKNOWN);
                    sq.setMine(false);
                }
            }
        }

        Random r = new Random();
        while (mines > 0) {
            int x = r.nextInt(width);
            int y = r.nextInt(height);
            final Square s = rows.get(y).getColumns().get(x);
            if (s.isMine()) {
                continue;
            }
            s.setMine(true);
            mines--;
        }

        model.setState(GameState.IN_PROGRESS);
        if (rows != model.getRows()) {
            model.getRows().clear();
            model.getRows().addAll(rows);
        }
    }

    @ModelOperation
    static void computeMines(Mines model) {
        List<Integer> xBombs = new ArrayList<Integer>();
        List<Integer> yBombs = new ArrayList<Integer>();
        final List<Row> rows = model.getRows();
        boolean emptyHidden = false;
        SquareType[][] arr = new SquareType[rows.size()][];
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            arr[y] = new SquareType[columns.size()];
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                if (sq.isMine()) {
                    xBombs.add(x);
                    yBombs.add(y);
                }
                if (sq.getState().isVisible()) {
                    arr[y][x] = SquareType.N_0;
                } else {
                    if (!sq.isMine()) {
                        emptyHidden = true;
                    }
                }
            }
        }
        for (int i = 0; i < xBombs.size(); i++) {
            int x = xBombs.get(i);
            int y = yBombs.get(i);

            incrementAround(arr, x, y);
        }
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                final SquareType newState = arr[y][x];
                if (newState != null && newState != sq.getState()) {
                    sq.setState(newState);
                }
            }
        }

        if (!emptyHidden) {
            model.setState(GameState.WON);
            showAllBombs(model, SquareType.DISCOVERED);
            AudioClip applause = AudioClip.create("applause.mp3");
            applause.play();
        }
    }

    private static void incrementAround(SquareType[][] arr, int x, int y) {
        incrementAt(arr, x - 1, y - 1);
        incrementAt(arr, x - 1, y);
        incrementAt(arr, x - 1, y + 1);

        incrementAt(arr, x + 1, y - 1);
        incrementAt(arr, x + 1, y);
        incrementAt(arr, x + 1, y + 1);

        incrementAt(arr, x, y - 1);
        incrementAt(arr, x, y + 1);
    }

    private static void incrementAt(SquareType[][] arr, int x, int y) {
        if (y >= 0 && y < arr.length) {
            SquareType[] r = arr[y];
            if (x >= 0 && x < r.length) {
                SquareType sq = r[x];
                if (sq != null) {
                    r[x] = sq.moreBombsAround();
                }
            }
        }
    }

    static void showAllBombs(Mines model, SquareType state) {
        for (Row row : model.getRows()) {
            for (Square square : row.getColumns()) {
                if (square.isMine()) {
                    square.setState(state);
                }
            }
        }
    }

    @Function
    static void click(Mines model, Square data) {
        if (model.getState() == GameState.MARKING_MINE) {
            if (data.getState() == SquareType.UNKNOWN) {
                data.setState(SquareType.MARKED);
                if (allMarked(model)) {
                    model.setState(GameState.WON);
                    return;
                }
            }
            model.setState(GameState.IN_PROGRESS);
            return;
        }
        if (model.getState() != GameState.IN_PROGRESS) {
            return;
        }
        if (data.getState() == SquareType.MARKED) {
            data.setState(SquareType.UNKNOWN);
            if (allMarked(model)) {
                model.setState(GameState.WON);
            }
            return;
        }
        if (data.getState() != SquareType.UNKNOWN) {
            return;
        }
        if (data.isMine()) {
            Square fair = atLeastOnePlaceWhereBombCantBe(model);
            if (fair == null) {
                if (placeBombElseWhere(model, data)) {
                    cleanedUp(model, data);
                    return;
                }
            }
            explosion(model);
        } else {
            Square takeFrom = tryStealBomb(model, data);
            if (takeFrom != null) {
                final Square fair = atLeastOnePlaceWhereBombCantBe(model);
                if (fair != null) {
                    takeFrom.setMine(false);
                    data.setMine(true);
                    explosion(model);
                    return;
                }
            }
            cleanedUp(model, data);
        }
    }

    @Function
    static void urlProjectPage(Mines model) {
        String url = "https://dukescript.com";
        openURL(url);
    }

    @Function
    static void urlProjectDoc(Mines model) {
        String url = "https://dukescript.com/documentation.html"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlBck2Brwsr(Mines model) {
        String url = "http://bck2brwsr.apidesign.org"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlBrowserSweeper(Mines model) {
        String url = "http://xelfi.cz/minesweeper/bck2brwsr/"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlPresenters(Mines model) {
        String url = "https://github.com/dukescript/dukescript-presenters"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlGooglePlay(Mines model) {
        String url = "https://play.google.com/store/apps/details?id=org.apidesign.demo.minesweeper"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlRoboVM(Mines model) {
        String url = "http://www.robovm.org"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlAppStore(Mines model) {
        String url = "https://itunes.apple.com/us/app/fair-minesweeper/id903688146"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlNetBeansPlugin(Mines model) {
        String url = "http://plugins.netbeans.org/plugin/53864/"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlNetBeans(Mines model) {
        String url = "http://www.netbeans.org"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlDevelop(Mines model) {
        String url = "https://dukescript.com/getting_started.html"; // NOI18N
        openURL(url);
    }

    private static void openURL(String url) {
        OpenURL.openURL(url);
    }

    private static void cleanedUp(Mines model, Square data) {
        AudioClip touch = AudioClip.create("move.mp3");
        touch.play();
        expandKnown(model, data);
        model.computeMines();
    }

    private static void explosion(Mines model) {
        showAllBombs(model, SquareType.EXPLOSION);
        model.setState(GameState.LOST);
        AudioClip oops = AudioClip.create("oops.mp3");
        oops.play();
    }

    private static Square tryStealBomb(Mines model, Square data) {
        data.setMine(true);
        final List<Row> rows = model.getRows();
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                if (sq == data) {
                    continue;
                }
                if (sq.isMine()) {
                    sq.setMine(false);
                    final boolean ok = isConsistent(model);
                    sq.setMine(true);
                    if (ok) {
                        data.setMine(false);
                        return sq;
                    }
                }
            }
        }
        data.setMine(false);
        return null;
    }

    private static Square atLeastOnePlaceWhereBombCantBe(Mines model) {
        final List<Row> rows = model.getRows();
        Square cantBe = null;
        int discovered = 0;
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                if (sq.getState() == SquareType.UNKNOWN) {
                    if (!sq.isMine()) {
                        if (tryStealBomb(model, sq) == null) {
                            cantBe = sq;
                        }
                    }
                } else {
                    discovered++;
                }
            }
        }

        if (discovered > 5) {
            return cantBe;
        }

        return null;
    }

    private static boolean placeBombElseWhere(Mines model, Square moveBomb) {
        List<Square> ok = new ArrayList<Square>();
        moveBomb.setMine(false);
        final List<Row> rows = model.getRows();
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                if (sq == moveBomb || sq.isMine() || sq.getState().isVisible()) {
                    continue;
                }
                sq.setMine(true);
                if (isConsistent(model)) {
                    ok.add(sq);
                }
                sq.setMine(false);
            }
        }
        if (ok.isEmpty()) {
            moveBomb.setMine(true);
            return false;
        } else {
            int r = new Random().nextInt(ok.size());
            ok.get(r).setMine(true);
            return true;
        }
    }

    private static void expandKnown(Mines model, Square data) {
        final List<Row> rows = model.getRows();
        for (int y = 0; y < rows.size(); y++) {
            final List<Square> columns = rows.get(y).getColumns();
            for (int x = 0; x < columns.size(); x++) {
                Square sq = columns.get(x);
                if (sq == data) {
                    expandKnown(model, x, y);
                    return;
                }
            }
        }
    }

    private static void expandKnown(Mines model, int x, int y) {
        if (y < 0 || y >= model.getRows().size()) {
            return;
        }
        final List<Square> columns = model.getRows().get(y).getColumns();
        if (x < 0 || x >= columns.size()) {
            return;
        }
        final Square sq = columns.get(x);
        if (sq.getState() == SquareType.UNKNOWN) {
            int around = around(model, x, y);
            final SquareType t = SquareType.valueOf("N_" + around);
            sq.setState(t);
            if (t == SquareType.N_0) {
                expandKnown(model, x - 1, y - 1);
                expandKnown(model, x - 1, y);
                expandKnown(model, x - 1, y + 1);
                expandKnown(model, x, y - 1);
                expandKnown(model, x, y + 1);
                expandKnown(model, x + 1, y - 1);
                expandKnown(model, x + 1, y);
                expandKnown(model, x + 1, y + 1);
            }
        }
    }

    private static int around(Mines model, int x, int y) {
        return minesAt(model, x - 1, y - 1)
            + minesAt(model, x - 1, y)
            + minesAt(model, x - 1, y + 1)
            + minesAt(model, x, y - 1)
            + minesAt(model, x, y + 1)
            + minesAt(model, x + 1, y - 1)
            + minesAt(model, x + 1, y)
            + minesAt(model, x + 1, y + 1);
    }

    private static int minesAt(Mines model, int x, int y) {
        if (y < 0 || y >= model.getRows().size()) {
            return 0;
        }
        final List<Square> columns = model.getRows().get(y).getColumns();
        if (x < 0 || x >= columns.size()) {
            return 0;
        }
        Square sq = columns.get(x);
        return sq.isMine() ? 1 : 0;
    }

    private static boolean isConsistent(Mines m) {
        for (int row = 0; row < m.getRows().size(); row++) {
            Row r = m.getRows().get(row);
            for (int col = 0; col < r.getColumns().size(); col++) {
                Square sq = r.getColumns().get(col);
                if (sq.getState().isVisible()) {
                    int around = around(m, col, row);
                    if (around != sq.getState().ordinal()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean allMarked(Mines m) {
        for (Row r : m.getRows()) {
            for (Square sq : r.getColumns()) {
                if (sq.isMine() == (sq.getState() != SquareType.MARKED)) {
                    return false;
                }
            }
        }
        for (Row r : m.getRows()) {
            for (Square sq : r.getColumns()) {
                if (sq.isMine()) {
                    sq.setState(SquareType.DISCOVERED);
                } else {
                    sq.setState(SquareType.N_0);
                }
            }
        }
        computeMines(m);
        return true;
    }

    private static Mines ui;

    public static void main(String... args) throws Exception {
        ui = new Mines();
        ui.applyBindings();
    }
}
