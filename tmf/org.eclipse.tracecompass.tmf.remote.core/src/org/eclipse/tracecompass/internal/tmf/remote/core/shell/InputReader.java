/**********************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Markus Schorn - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.core.shell;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.remote.core.shell.ICommandOutputListener;

@NonNullByDefault
class InputReader {
    private static final int JOIN_TIMEOUT = 300;
    private static final int BYTES_PER_KB = 1024;

    private final InputStreamReader fReader;
    private final Thread fThread;
    private final StringBuilder fResult;
    private volatile boolean fDone;

    public InputReader(InputStream inputStream, @Nullable final ICommandOutputListener listener, final boolean isStdOut) {
        fResult = new StringBuilder();
        fReader = new InputStreamReader(inputStream);
        fThread = new Thread() {
            @Override
            public void run() {
                final char[] buffer = new char[BYTES_PER_KB];
                int read;
                try {
                    while (!fDone && (read = fReader.read(buffer)) > 0) {
                        fResult.append(buffer, 0, read);
                        if (listener != null) {
                            if (isStdOut) {
                                listener.outputUpdated(String.valueOf(buffer, 0, read));
                            } else {
                                listener.errorOutputUpdated(String.valueOf(buffer, 0, read));
                            }
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        fThread.start();
    }

    public void waitFor(IProgressMonitor monitor) throws InterruptedException {
        while (fThread.isAlive() && (!monitor.isCanceled())) {
            fThread.join(JOIN_TIMEOUT);
        }
    }

    public void stop() {
        fDone = true;
        fThread.interrupt();
    }

    @Override
    public String toString() {
        return nullToEmptyString(fResult.toString());
    }

}
