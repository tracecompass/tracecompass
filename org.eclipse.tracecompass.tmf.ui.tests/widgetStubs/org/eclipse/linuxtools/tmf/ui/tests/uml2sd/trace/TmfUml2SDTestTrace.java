/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.tests.uml2sd.trace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * Parser implementation for Uml2SD Test Traces.
 *
 */
public class TmfUml2SDTestTrace implements ITmfEventParser {

    ITmfTrace fEventStream;

    /**
     * Default Constructor
     */
    public TmfUml2SDTestTrace() {
    }

    /**
     * Constructor
     * @param eventStream ITmfTrace implementation
     */
    public TmfUml2SDTestTrace(ITmfTrace eventStream) {
        fEventStream = eventStream;
    }

    /**
     * @param eventStream ITmfTrace implementation to set
     */
    public void setTrace(ITmfTrace eventStream) {
        fEventStream = eventStream;
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        if (! (fEventStream instanceof TmfTraceStub)) {
            return null;
        }

        // Highly inefficient...
        RandomAccessFile stream = ((TmfTraceStub) fEventStream).getStream();

//        String name = eventStream.getName();
//        name = name.substring(name.lastIndexOf('/') + 1);

        long location = 0;
        if (context != null) {
            location = (Long) context.getLocation().getLocationInfo();
        }

        try {
            stream.seek(location);

            long ts        = stream.readLong();
            String source  = stream.readUTF();
            String type    = stream.readUTF();
            String reference = stream.readUTF();
            String sender = stream.readUTF();
            String receiver = stream.readUTF();
            String signal = stream.readUTF();

            String[] labels = {"sender", "receiver", "signal"};

            TmfEventType tmfEventType = new TmfEventType("UnitTest", type, TmfEventField.makeRoot(labels));

            String content = "[";
            content += sender;
            content += "," + receiver;
            content += "," + signal;
            content += "]";

            // Pre-parse the content
            TmfEventField[] fields = new TmfEventField[3];
            fields[0] = new TmfEventField("sender", sender, null);
            fields[1] = new TmfEventField("receiver", receiver, null);
            fields[2] = new TmfEventField("signal", signal, null);

            ITmfEventField tmfContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, content, fields);
            ITmfEvent tmfEvent = new TmfEvent(fEventStream, new TmfTimestamp(ts, -9), source, tmfEventType, tmfContent, reference);

            return tmfEvent;
        } catch (final EOFException e) {
        } catch (final IOException e) {
        }
        return null;
    }

}
