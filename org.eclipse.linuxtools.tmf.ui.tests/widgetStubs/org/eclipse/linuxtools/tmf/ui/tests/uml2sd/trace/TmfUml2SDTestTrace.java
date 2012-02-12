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

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.parser.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.stubs.trace.TmfTraceStub;

public class TmfUml2SDTestTrace implements ITmfEventParser {
    
    @Override
    @SuppressWarnings({ "unchecked", "nls" })    
    public TmfEvent parseNextEvent(ITmfTrace<?> eventStream, TmfContext context) throws IOException {
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

            TmfEventType tmfEventType = new TmfEventType("UnitTest", type, labels);
            TmfEvent tmfEvent = new TmfEvent(new TmfTimestamp(ts, (byte)-9), source, tmfEventType, reference);

            String content = "[";
            content += sender;
            content += "," + receiver;
            content += "," + signal;
            content += "]";

            TmfEventContent tmfContent = new TmfEventContent(tmfEvent, content) {
                @Override
                public void parseContent() {
                    String raw = (String) fRawContent;
                    int i = raw.indexOf(",");
                    String sender = raw.substring(1, i);
                    int k = raw.indexOf(",", i+1);
                    String receiver = raw.substring(i+1, k);
                    i = raw.indexOf(",", k+1);
                    String signal = raw.substring(k+1, raw.length() - 1);
                    fFields = new Object[3];
                    fFields[0] = new TmfEventField(this, "sender", sender);
                    fFields[1] = new TmfEventField(this, "receiver", receiver);;
                    fFields[2] = new TmfEventField(this, "signal", signal);;
                }
            };
            tmfEvent.setContent(tmfContent);

            return tmfEvent;
        } catch (EOFException e) {
        }
        return null;
    }

}
