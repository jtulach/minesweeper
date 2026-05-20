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
package org.apidesign.demo.minesweeper.js;

import java.util.Arrays;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

@JavaScriptResource(value = "grid.js")
public abstract class Grid {
    private final Object jsGrid;

    /** Constructor for subclasses */
    protected Grid(int size, int mines) {
        this(initializeGridAndMock(size, mines));
    }

    /** Subclasses has to implement this method. Then they obtain
     * callbacks about bombs being dropped at particular piece.
     *
     * @param prevX column or {@code -1} when off-grid
     * @param prevY row or {@code -1} when off-grid
     * @param x column or {@code -1} when off-grid
     * @param y row or {@code -1} when off-grid
     * @return {@code true} if the drop is acceptable, {@code false} to cancel the drop
     */
    protected abstract boolean onDrop(int prevX, int prevY, int x, int y);

    /**
     * Move all pieces to target location.
     */
    public final void init() {
        initGrid(jsGrid);
    }

    /**
     *  Adjust the CSS variables & co. Right now.
     * @param markX x-coordinates of marked fields
     * @param markY y-coordinates of marked fields
     */
    public final void update(int[] markX, int[] markY) {
        updateGrid(jsGrid, markX, markY);
    }

    /** Initiates a move of an unplaced piece (if available) to provided location.
     *
     * @param x column
     * @param y row
     */
    public final void moveTo(int x, int y) {
        moveTo(jsGrid, x, y);
    }

    /** Clears a square from a piece if it has any.
     *
     * @param x column
     * @param y row
     */
    public final void clear(int x, int y) {
        backToTarget(jsGrid, x, y);
    }

    /** Finishes all animations.
     */
    public final void flush() {
        flushAnimations(jsGrid);
    }


    /**
     * Return the number of remaining pieces.
     *
     * @return count from {@code 0} to number of {@code mines}
     */
    public final int getRemaining() {
        return getRemaining(jsGrid);
    }


    //
    // Internal implementaton of a bridge to JavaScript
    //

    private Grid(Object jsGrid) {
        this.jsGrid = jsGrid;
        registerDrop(jsGrid, this);
    }

    private static Object initializeGridAndMock(int size, int mines) {
        if (!isDefined("window")) {
            MockGrid.children("Init mock");
        }
        return initializeGrid(size, mines);
    }

    @JavaScriptBody(args = {"symbol"}, body = """
        let global = (0 || eval)("this");
        let v = global[symbol];
        return typeof v !== 'undefined';
    """)
    private static native boolean isDefined(String symbol);

    @JavaScriptBody(args = {"size", "mines"}, body = """
        return initializeGrid(size, mines);
    """)
    private static native Object initializeGrid(int size, int mines);

    @JavaScriptBody(args = {"grid" }, body = "grid.initGrid();")
    private static native Object initGrid(Object grid);

    @JavaScriptBody(args = {"grid" }, body = "grid.flushAnimations();")
    private static native void flushAnimations(Object grid);

    @JavaScriptBody(args = {"grid" }, body = "return grid.getRemaining();")
    private static native int getRemaining(Object grid);

    @JavaScriptBody(args = {"grid", "markX", "markY" }, body = "grid.updateGrid(markX, markY);")
    private static native Object updateGrid(Object grid, int[] markX, int[] markY);

    @JavaScriptBody(args = {"grid", "x", "y"}, body = "grid.moveTo(x, y);")
    private static native Object moveTo(Object grid, int x, int y);

    @JavaScriptBody(args = {"grid", "x", "y"}, body = "grid.backToTarget(x, y);")
    private static native Object backToTarget(Object grid, int x, int y);

    @JavaScriptBody(args = {"jsGrid", "self"}, keepAlive = true, javacall = true, body = """
    jsGrid.registerDrop((prevX, prevY, x, y) => {
      return self.@org.apidesign.demo.minesweeper.js.Grid::onDrop(IIII)(prevX, prevY, x, y);
    });
    """)
    private static native void registerDrop(Object jsGrid, Grid self);

    @JavaScriptBody(args = {}, body = """
    return Date.now();
    """)
    public static native long timeNow();

    @JavaScriptBody(args = {"msg", "arr"}, body = """
    var all = [ msg ].push(...arr);
    console.log(all);
    """)
    public static void log(String msg, Object... arr) {
        System.err.println(msg + ": " + Arrays.deepToString(arr));
    }
}
