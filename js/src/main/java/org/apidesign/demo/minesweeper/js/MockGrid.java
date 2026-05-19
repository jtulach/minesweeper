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

/**
 * Helper class for using Grid in tests.
 */
abstract class MockGrid extends Grid {

    static {
        defineDom();
    }

    MockGrid(int size, int mines) {
        super(size, mines);
    }

    @JavaScriptBody(args = {}, body = """
        class MockAudio {
            play() {
            }
        }
        class MockStyle {
            setProperty(name, value) {
            }
        }

        class MockClassList extends Set {
            contains(n) {
                return this.has(n);
            }

            remove(n) {
                return this.delete(n);
            }
        }

        class MockElem {
            constructor(id) {
                this.id = id;
                this.children = [];
                this.dataset = {};
                this.style = new MockStyle();
                this.classList = new MockClassList();
                this.listeners = new Map();
                this.clientWidth = 400;
                this.clientHeight = 600;
            }

            addEventListener(type, fn, config) {
                let arr = this.listeners.get(type);
                if (!arr) {
                    arr = [];
                    this.listeners.set(type, arr);
                }
                arr.push({
                    'fn' : fn,
                    'config' : config
                });
            }

            appendChild(ch) {
                this.children.push(ch);
            }
        }

        class MockDoc {
            constructor() {
                this.documentElement = new MockElem();
                this.ids = new Map();
                this.selectors = new Map();
            }

            getElementById(id) {
                let e = this.ids.get(id);
                if (!e) {
                    e = new MockElem(id);
                    this.ids.set(id, e);
                }
                return e;
            }

            querySelector(sel) {
                let e = this.selectors.get(sel);
                if (!e) {
                    e = new MockElem(sel);
                    this.selectors.set(sel, e);
                }
                return e;
            }
            createElement(type) {
                return new MockElem(null);
            }
        }

        class MockWindow extends MockElem {
            constructor() {
                super();
                this.innerWidth = 480;
                this.innerHeight = 640;
            }
        }
        let global = (0 || eval)('this');
        global.document = new MockDoc();
        global.window = new MockWindow();
        global.Audio = MockAudio;
    """)
    private static native void defineDom();

    @JavaScriptBody(args = {"id"}, body = """
        let global = (0 || eval)('this');
        return global.document.getElementById(id);
    """)
    static native Object getElementById(String id);

    @JavaScriptBody(args = {"e"}, body = """
        return e.children;
    """)
    static native Object[] children(Object e);

    @JavaScriptBody(args = { "e", "type", "propertyName" }, body = """
        var arr = e.listeners.get(type);
        if (arr) {
            for (var fnAndConfig of arr) {
                let fn = fnAndConfig.fn;
                if (fnAndConfig.config && fnAndConfig.config.once) {
                    e.listeners.delete(type);
                }
                fn({ 'propertyName' : propertyName });
            }
        }
    """)
    static native Object[] emitEvent(Object e, String type, String propertyName);

    @JavaScriptBody(args = {"e"}, body = """
        return Array.from(e.classList);
    """)
    static native Object[] classList(Object e);

}
