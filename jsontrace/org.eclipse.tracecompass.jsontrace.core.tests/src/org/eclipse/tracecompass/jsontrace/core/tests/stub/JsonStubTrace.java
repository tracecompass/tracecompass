/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.jsontrace.core.tests.stub;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tracecompass.internal.provisional.jsontrace.core.trace.JsonTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Json stub trace to test {@link JsonTrace}
 *
 * @author Simon Delisle
 */
public class JsonStubTrace extends JsonTrace {

    private static final String TIMESTAMP_KEY = "timestamp"; //$NON-NLS-1$
    private Gson GSON = new Gson();

    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        fProperties.put("Type", "JSON-Stub"); //$NON-NLS-1$ //$NON-NLS-2$
        String dir = TmfTraceManager.getSupplementaryFileDir(this);
        fFile = new File(dir + new File(path).getName());
        if (!fFile.exists()) {
            Job sortJob = new JsonStubTraceSortingJob(this, path);
            sortJob.schedule();
            while (sortJob.getResult() == null) {
                try {
                    sortJob.join();
                } catch (InterruptedException e) {
                    throw new TmfTraceException(e.getMessage(), e);
                }
            }
            IStatus result = sortJob.getResult();
            if (!result.isOK()) {
                throw new TmfTraceException("Job failed " + result.getMessage()); //$NON-NLS-1$
            }
        }
        try {
            fFileInput = new BufferedRandomAccessFile(fFile, "r"); //$NON-NLS-1$
            goToCorrectStart(fFileInput);
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public IStatus validate(IProject project, String path) {
        // Slow, but meh, it's a unit test and this is more readable.
        if (path.matches("traces/.*sortedTrace.json")) { //$NON-NLS-1$
            return new TraceValidationStatus(MAX_CONFIDENCE, "json.trace.stub"); //$NON-NLS-1$
        }
        return Status.CANCEL_STATUS;
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        ITmfLocation location = context.getLocation();
        if (location instanceof TmfLongLocation) {
            TmfLongLocation tmfLongLocation = (TmfLongLocation) location;
            Long locationInfo = tmfLongLocation.getLocationInfo();
            if (location.equals(NULL_LOCATION)) {
                locationInfo = 0L;
            }
            try {
                if (!locationInfo.equals(fFileInput.getFilePointer())) {
                    fFileInput.seek(locationInfo);
                }
                String nextJson = readNextEventString(() -> fFileInput.read());
                while (nextJson != null) {
                    if (nextJson != null) {
                        JsonObject object = GSON.fromJson(nextJson, JsonObject.class);
                        // Ignore events with no timestamp, they are there just to make sure the traces
                        // parses in those cases
                        JsonElement tsElement = object.get(TIMESTAMP_KEY);
                        if (tsElement != null) {
                            long timestamp = tsElement.getAsLong();
                            return new TmfEvent(this, context.getRank(), TmfTimestamp.fromNanos(timestamp),
                                new TmfEventType("JsonStubEvent", null), null); //$NON-NLS-1$
                        }
                        nextJson = readNextEventString(() -> fFileInput.read());
                    }
                }
            } catch (IOException e) {
                // Nothing to do
            }
        }
        return null;
    }

    private static void goToCorrectStart(RandomAccessFile rafile) throws IOException {
        // skip start (ex.: {"events":)
        StringBuilder sb = new StringBuilder();
        int val = rafile.read();
        /*
         * Skip list contains all the odd control characters
         */
        Set<Integer> skipList = new HashSet<>();
        skipList.add((int) ':');
        skipList.add((int) '\t');
        skipList.add((int) '\n');
        skipList.add((int) '\r');
        skipList.add((int) ' ');
        skipList.add((int) '\b');
        skipList.add((int) '\f');
        while (val != -1 && val != ':' && sb.length() < 9) {
            if (!skipList.contains(val)) {
                sb.append((char) val);
            }
            val = rafile.read();
        }

        if (sb.toString().startsWith('{' + "\"events\"") && rafile.length() > 9) { //$NON-NLS-1$
            rafile.seek(9);
        } else {
            rafile.seek(0);
        }
    }
}
