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

import java.io.Closeable;
import net.java.html.boot.script.Scripts;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Tests for behavior of @JavaScriptBody methods. Set your JavaScript 
 * environment up (for example define <code>alert</code> or use some
 * emulation library like <em>env.js</em>), register script presenter 
 * and then you can call methods that deal with JavaScript in your tests.
 */
public class JsInteractionTest {
    private Closeable jsEngine;
    @BeforeMethod public void initializeJSEngine() throws Exception {
        jsEngine = Fn.activate(Scripts.createPresenter());
    }
    
    @AfterMethod public void shutdownJSEngine() throws Exception {
        jsEngine.close();
    }
    
    @Test public void testCallbackFromJavaScript() throws Exception {
        class R implements Runnable {
            int called;

            @Override
            public void run() {
                called++;
            }
        }
        R callback = new R();
        
        Dialogs.confirmByUser("Hello", callback);
        
        assertEquals(callback.called, 1, "One immediate callback");
    }
  }
