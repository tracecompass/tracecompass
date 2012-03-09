/*******************************************************************************
 * Copyright (c) 2011 Ericsson
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

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.parser.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.stubs.trace.TmfTraceStub;

public class TmfUml2SDTestTrace implements ITmfEventParser<TmfEvent> {
    
    @Override
    @SuppressWarnings({ "unchecked", "nls" })    
    public TmfEvent parseNextEvent(ITmfTrace<TmfEvent> eventStream, ITmfContext context) throws IOException {
        if (! (eventStream instanceof TmfTraceStub)) {
            return null;
        }

        // Highly inefficient...
        RandomAccessFile stream = ((TmfTraceStub) eventStream).getStream();

        String name = eventStream.getName();
        name = name.substring(name.lastIndexOf('/') + 1);

        long location = 0;
        if (context != null)
            location = ((TmfLocation<Long>) (context.getLocation())).getLocation();
        stream.seek(location);

        try {
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
            fields[0] = new TmfEventField("sender", sender);
            fields[1] = new TmfEventField("receiver", receiver);
            fields[2] = new TmfEventField("signal", signal);
            
            ITmfEventField tmfContent = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, content, fields);
            TmfEvent tmfEvent = new TmfEvent(eventStream, new TmfTimestamp(ts, -9), source, tmfEventType, tmfContent, reference);

            return tmfEvent;
        } catch (EOFException e) {
        }
        return null;
    }

}
