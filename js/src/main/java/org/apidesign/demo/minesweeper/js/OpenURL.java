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

public abstract class OpenURL {
    private final OpenURL next;
    private static OpenURL last;

    protected OpenURL() {
        this(last);
        last = this;
    }

    private OpenURL(OpenURL n) {
        next = n;
    }

    protected abstract boolean handleURL(String url);
    protected abstract String baseUrl();

    public static String relativeUrl(String rel) {
        String base = null;
        for (OpenURL handler = last; handler != null && base == null; handler = handler.next) {
            base = handler.baseUrl();
        }
        if (base == null) {
            base = baseUrl0();
        }

        int slash = base.lastIndexOf('/');
        return base.substring(0, slash + 1) + rel;
    }

    public static void openURL(String url) {
        for (OpenURL handler = last; handler != null; handler = handler.next) {
            if (handler.handleURL(url)) {
                return;
            }
        }

        changeURL(url);
    }

    @JavaScriptBody(wait4js = false, args = { "url" }, body =
        "window.open(url, '_blank');"
    )
    private static native void changeURL(String url);

    @JavaScriptBody(args = {}, body = ""
            + "return window.location.href;\n"
    )
    private static native String baseUrl0();

}
