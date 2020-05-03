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

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.apidesign.demo.minesweeper.js.OpenURL;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = OpenURL.class)
public final class NbOpenURL extends OpenURL {
    @Override
    protected String baseUrl() {
        return null;
    }

    @Override
    protected boolean handleURL(String url) {
        if (url.endsWith("/getting_started.html")) {
            if (invokeProjectWizard()) {
                return true;
            }
        }
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURLExternal(new URL(url));
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    private static final Logger LOG = Logger.getLogger(NbOpenURL.class.getName());
    private boolean invokeProjectWizard() {
        Action a = null;
        try {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = Thread.currentThread().getContextClassLoader();
            }
            if (l == null) {
                l = NbOpenURL.class.getClassLoader();
            }
            Class<?> newPrj = Class.forName("org.netbeans.spi.project.ui.support.CommonProjectActions", true, l); // NOI18N
            a = (Action) newPrj.getMethod("newProjectAction").invoke(null); // NOI18N
        } catch (Exception ex) {
            LOG.log(Level.FINE, "Cannot find New project action!", ex);
        }
        if (a != null) {
            FileObject fo = FileUtil.getConfigFile("Templates/Project/ClientSide"); // NOI18N
            if (fo != null) {
                a.putValue("PRESELECT_CATEGORY", "ClientSide"); // NOI18N
                for (FileObject template : fo.getChildren()) {
                    final String n = template.getName();
                    if (n.contains("htmljava") || n.contains("dukescript")) { // NOI18N
                        a.putValue("PRESELECT_TEMPLATE", template.getName()); // NOI18N
                        a.actionPerformed(new ActionEvent(this, 0, null));
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
