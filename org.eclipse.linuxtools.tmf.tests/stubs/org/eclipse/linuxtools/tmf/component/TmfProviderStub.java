/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;

/**
 * <b><u>TmfProviderStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfProviderStub extends TmfProvider<TmfEvent> {

    private TmfTraceStub fTrace;

    public TmfProviderStub(String path) throws IOException {
    	super(TmfEvent.class);
        URL location = FileLocator.find(TmfCoreTestPlugin.getPlugin().getBundle(), new Path(path), null);
		try {
			File test = new File(FileLocator.toFileURL(location).toURI());
			fTrace = new TmfTraceStub(test.getPath(), true);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    }

	@Override
	public ITmfContext setContext(TmfDataRequest<TmfEvent> request) {
		if (request instanceof TmfEventRequest<?>) {
			TmfContext context = fTrace.seekEvent(((TmfEventRequest<?>) request).getRange().getStartTime());
			return context;
		}
		return null;
	}

	@Override
	public TmfEvent getNext(ITmfContext context) {
		return fTrace.getNext(context);
	}

	@Override
	public boolean isCompleted(TmfDataRequest<TmfEvent> request, TmfEvent event) {
		TmfTimestamp endTime = ((TmfEventRequest<?>) request).getRange().getEndTime();
		return event.getTimestamp().compareTo(endTime, false) <= 0;
	}

}