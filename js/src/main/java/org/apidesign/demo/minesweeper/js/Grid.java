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

import java.util.List;
import java.util.function.BiFunction;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;

@JavaScriptResource(value = "grid.js")
public abstract class Grid {
    private final Object jsGrid;

    /** Constructor for subclasses */
    protected Grid(int size, int mines) {
        this(initializeGrid(size, mines));
    }

    /** Subclasses has to implement this method. Then they obtain
     * callbacks about bombs being dropped at particular piece.
     *
     * @param x column
     * @param y row
     * @return {@code true} if the drop is acceptable, {@code false} to cancel the drop
     */
    protected abstract boolean onDrop(int x, int y);

    /** Unmarks drop of a piece.
     *
     */
    public final void unDrop() {

    }

    /** Adjust the CSS variables & co. Right now.
     */
    public final void update() {
        updateGrid(jsGrid);
    }

    //
    // Internal implementaton of a bridge to JavaScript
    //

    private Grid(Object jsGrid) {
        this.jsGrid = jsGrid;
        registerDrop(jsGrid, this);
    }

    @JavaScriptBody(args = {"size", "mines"}, body = """
        return initializeGrid(size, mines);
    """)
    private static native Object initializeGrid(int size, int mines);

    @JavaScriptBody(args = {"grid"}, body = "grid.updateGrid();")
    private static native Object updateGrid(Object grid);

    @JavaScriptBody(args = {"jsGrid", "self"}, keepAlive = true, javacall = true, body = """
    jsGrid.registerDrop((x, y) => {
      return self.@org.apidesign.demo.minesweeper.js.Grid::onDrop(II)(x, y);
    });
    """)
    private static native void registerDrop(Object jsGrid, Grid self);
}
