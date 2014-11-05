/**********************************************************************
 * Copyright (c) 2014 Wind River Systems, Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Markus Schorn - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.ui.views.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;

class InputReader {
    private final InputStreamReader fReader;
    private final Thread fThread;
    private final StringBuilder fResult;
    private volatile boolean fDone;

    public InputReader(InputStream inputStream) {
        fResult = new StringBuilder();
        fReader = new InputStreamReader(inputStream);
        fThread = new Thread() {
            @Override
            public void run() {
                final char[] buffer = new char[1024];
                int read;
                try {
                    while (!fDone && (read = fReader.read(buffer)) > 0) {
                        fResult.append(buffer, 0, read);
                    }
                } catch (IOException e) {
                }
            }
        };
        fThread.start();
    }

    public void waitFor(IProgressMonitor monitor) throws InterruptedException {
        while (fThread.isAlive() && (monitor == null || !monitor.isCanceled())) {
            fThread.join(300);
        }
    }

    public void stop() {
        fDone = true;
        fThread.interrupt();
    }

    @Override
    public String toString() {
        return fResult.toString();
    }

}
