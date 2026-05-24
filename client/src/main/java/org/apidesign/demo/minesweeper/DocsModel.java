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

import net.java.html.json.Function;
import net.java.html.json.Model;
import org.apidesign.demo.minesweeper.js.OpenURL;

/**
 * Model of the documentation actions.
 */
@Model(className = "Docs", targetId = "", instance = true, properties = {})
public final class DocsModel {
    @Function
    static void urlProjectPage(Docs model) {
        String url = "https://dukescript.com";
        openURL(url);
    }

    @Function
    static void urlProjectDoc(Docs model) {
        String url = "https://dukescript.com/documentation.html"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlPrivacy(Docs model) {
        String url = OpenURL.relativeUrl("privacy.html"); // NOI18N
        openURL(url);
    }

    @Function
    static void urlBck2Brwsr(Docs model) {
        String url = "http://bck2brwsr.apidesign.org"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlBrowserSweeper(Docs model) {
        String url = "http://xelfi.cz/minesweeper/bck2brwsr/"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlPresenters(Docs model) {
        String url = "https://github.com/dukescript/dukescript-presenters"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlGooglePlay(Docs model) {
        String url = "https://play.google.com/store/apps/details?id=org.apidesign.demo.minesweeper"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlRoboVM(Docs model) {
        String url = "http://www.robovm.org"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlAppStore(Docs model) {
        String url = "https://itunes.apple.com/us/app/fair-minesweeper/id903688146"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlNetBeansPlugin(Docs model) {
        String url = "http://plugins.netbeans.org/plugin/53864/"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlNetBeans(Docs model) {
        String url = "http://www.netbeans.org"; // NOI18N
        openURL(url);
    }

    @Function
    static void urlDevelop(Docs model) {
        String url = "https://dukescript.com/getting_started.html"; // NOI18N
        openURL(url);
    }

    private static void openURL(String url) {
        OpenURL.openURL(url);
    }
}
