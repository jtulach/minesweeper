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

import net.java.html.js.JavaScriptBody;

public final class Audio {
    private final Object js;

    private Audio(Object js) {
        this.js = js;
    }

    public static Audio create(String url) {
        var js = audioNew(url);
        if (js instanceof String) {
            System.err.println(js);
            return new Audio(null);
        } else {
            return new Audio(js);
        }
    }

    public void play() {
        if (js != null) {
            audioPlay(js);
        }
    }
    @JavaScriptBody(args = { "url" }, body = """
        try {
            return new Audio(url);
        } catch (err) {
            return "No Audio: " + err;
        }
        """
    )
    private static native Object audioNew(String url);

    @JavaScriptBody(args = {"audio"}, body = """
        audio.play();
        """
    )
    private static native String audioPlay(Object audio);

}
