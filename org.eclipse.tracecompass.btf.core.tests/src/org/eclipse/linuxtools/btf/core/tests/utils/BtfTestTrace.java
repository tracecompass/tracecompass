/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.btf.core.tests.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.btf.core.tests.BtfTestPlugin;
import org.eclipse.linuxtools.btf.core.trace.BtfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.osgi.framework.Bundle;

/**
 * Wrapper like CtfTestTrace but for BTF, the best trace format
 * @author Matthew Khouzam
 */
public enum BtfTestTrace {

    /** btf test trace*/
    BTF_TEST("20140219-123819.btf");

    private final String fPath;
    private final String fDirectory = "testfiles";
    private BtfTrace fTrace = null;

    private BtfTestTrace(String file) {
        fPath = file;
    }

    /**
     * Get the path of the trace
     *
     * @return The path of this trace
     */
    public String getPath() {
        return fPath;
    }

    /**
     * Get the full path of the trace
     *
     * @return The full path of the trace
     */
    public String getFullPath() {
        return fDirectory + File.separator + fPath;
    }

    /**
     * Return a ITmfTrace object of this test trace. It will be already
     * initTrace()'ed. This method will always return a new trace and dispose of
     * the old one.
     *
     * After being used by unit tests, traces must be properly disposed of by
     * calling the {@link BtfTestTrace#dispose()} method.
     *
     * @return A {@link ITmfTrace} reference to this trace
     */
    public BtfTrace getTrace() {
        if (fTrace != null) {
            fTrace.dispose();
        }
        Bundle bundle = BtfTestPlugin.getBundle();
        Path path = new Path(fDirectory + File.separator + fPath);
        final URL location = FileLocator.find(bundle,path, null);
            File test;
            try {
                test = new File(FileLocator.toFileURL(location).toURI());
                fTrace = new BtfTrace();
                fTrace.initTrace(null, test.getAbsolutePath(), null);
            } catch (URISyntaxException | IOException | TmfTraceException e) {
                throw new RuntimeException(e);
            }
        return fTrace;
    }

    /**
     * Dispose of the trace
     */
    public void dispose() {
        if (fTrace != null) {
            fTrace.dispose();
            fTrace = null;
        }
    }
}
