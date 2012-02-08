/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;

public class CustomEvent extends TmfEvent {

    protected static final String TIMESTAMP_INPUT_FORMAT_KEY = "CE_TS_I_F"; //$NON-NLS-1$
    protected static final String NO_MESSAGE = ""; //$NON-NLS-1$
    public static final byte TIMESTAMP_SCALE = -3;
    
    protected CustomTraceDefinition fDefinition;
    protected Map<String, String> fData;
    private String[] fColumnData;

    public CustomEvent(CustomTraceDefinition definition) {
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    public CustomEvent(CustomTraceDefinition definition, TmfEvent other) {
        super(other);
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    public CustomEvent(CustomTraceDefinition definition, ITmfTrace<?> parentTrace, TmfTimestamp timestamp, String source, TmfEventType type, TmfEventReference reference) {
        super(parentTrace, timestamp, source, type, reference);
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    @Override
    public TmfTimestamp getTimestamp() {
        if (fData != null) processData();
        return super.getTimestamp();
    }

    public String[] extractItemFields() {
        if (fData != null) processData();
        return fColumnData;
    }

    private void processData() {
        String timeStampString = fData.get(CustomTraceDefinition.TAG_TIMESTAMP);
        String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
        Date date = null;
        if (timeStampInputFormat != null && timeStampString != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(timeStampInputFormat);
            try {
                date = dateFormat.parse(timeStampString);
                fTimestamp = new TmfTimestamp(date.getTime(), TIMESTAMP_SCALE);
            } catch (ParseException e) {
                fTimestamp = TmfTimestamp.Zero;
            }
        } else {
            fTimestamp = TmfTimestamp.Zero;
        }
        
        int i = 0;
        fColumnData = new String[fDefinition.outputs.size()];
        for (OutputColumn outputColumn : fDefinition.outputs) {
            String value = fData.get(outputColumn.name);
            if (outputColumn.name.equals(CustomTraceDefinition.TAG_TIMESTAMP) && date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(fDefinition.timeStampOutputFormat);
                fColumnData[i++] = dateFormat.format(date);
            } else {
                fColumnData[i++] = (value != null ? value : ""); //$NON-NLS-1$
            }
        }
        fData = null;
    }
}
