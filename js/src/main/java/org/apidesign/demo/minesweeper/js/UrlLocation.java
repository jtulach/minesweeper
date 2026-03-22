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

public final class UrlLocation {
    private UrlLocation() {
    }

    /**
     * Replaces the current hash in the URL
     * @param newHash the new hash
     */
    public static void setHash(String newHash) {
        var l = location();
        var hash = l.indexOf('#');
        if (hash == -1) {
            hash = l.length();
        }
        var baseUrl = l.substring(0, hash);
        if (newHash.length() > 0) {
            var href = baseUrl + "#" + newHash;
            location(href);
        } else {
            location(baseUrl);
        }
    }

    @JavaScriptBody(args = {}, body = """
    return (typeof window !== 'undefined') ? window.location.href : "";
    """)
    private static String location() {
        return "";
    }

    @JavaScriptBody(args = {"href"}, body = """
    if (typeof window !== 'undefined') {
        window.location.href = href;
    }
    """)
    private static void location(String href) {
    }
}
