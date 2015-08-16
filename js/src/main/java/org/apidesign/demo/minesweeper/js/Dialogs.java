/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2013-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import net.java.html.js.JavaScriptBody;

public final class Dialogs {
    private Dialogs() {
    }
    
    /** Shows confirmation dialog to the user.
     * 
     * @param msg the message
     * @param callback called back when the use accepts (can be null)
     * @return true or false
     */
    /** Shows direct interaction with JavaScript */
    @JavaScriptBody(
        args = { "msg", "callback" }, 
        javacall = true, 
        body = "if (confirm(msg)) {\n"
             + "  callback.@java.lang.Runnable::run()();\n"
             + "}\n"
    )
    public static native void confirmByUser(String msg, Runnable callback);
    
    @JavaScriptBody(
        args = {}, body = 
        "var w = window,\n" +
        "    d = document,\n" +
        "    e = d.documentElement,\n" +
        "    g = d.getElementsByTagName('body')[0],\n" +
        "    x = w.innerWidth || e.clientWidth || g.clientWidth,\n" +
        "    y = w.innerHeight|| e.clientHeight|| g.clientHeight;\n" +
        "\n" +
        "return 'Screen size is ' + x + ' times ' + y;\n"
    )
    public static native String screenSize();
}
