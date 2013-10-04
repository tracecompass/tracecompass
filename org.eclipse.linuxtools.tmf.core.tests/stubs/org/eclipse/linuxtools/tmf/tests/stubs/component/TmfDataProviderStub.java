/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.component.TmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * <b><u>TmfDataProviderStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("javadoc")
public class TmfDataProviderStub extends TmfDataProvider {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-10K";

    private TmfTraceStub fTrace;

    public TmfDataProviderStub(final String path) throws IOException {
        super("TmfDataProviderStub", ITmfEvent.class);
        final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
        try {
            final File test = new File(FileLocator.toFileURL(location).toURI());
            fTrace = new TmfTraceStub(test.getPath(), 0, true, null);
        } catch (final TmfTraceException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public TmfDataProviderStub() throws IOException {
        this(DIRECTORY + File.separator + TEST_STREAM);
    }

    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------

    @Override
    public ITmfContext armRequest(final ITmfDataRequest request) {
        if (request instanceof ITmfEventRequest) {
            final ITmfContext context = fTrace.seekEvent(((ITmfEventRequest) request).getRange().getStartTime());
            return context;
        }
        return null;
    }

    @Override
    public ITmfEvent getNext(final ITmfContext context) {
        return fTrace.getNext(context);
    }

    @Override
    public boolean isCompleted(final ITmfDataRequest request, final ITmfEvent data, final int nbRead) {
        return false;
    }

}
