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

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

public class CustomEvent extends TmfEvent {

    protected static final String TIMESTAMP_INPUT_FORMAT_KEY = "CE_TS_I_F"; //$NON-NLS-1$
    protected static final String NO_MESSAGE = ""; //$NON-NLS-1$
    public static final byte TIMESTAMP_SCALE = -3;
    
    protected CustomTraceDefinition fDefinition;
    protected Map<String, String> fData;
    private TmfEventField[] fColumnData;

    public CustomEvent(CustomTraceDefinition definition) {
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    public CustomEvent(CustomTraceDefinition definition, TmfEvent other) {
        super(other);
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    public CustomEvent(CustomTraceDefinition definition, ITmfTrace<?> parentTrace, ITmfTimestamp timestamp, String source, TmfEventType type, String reference) {
        super(parentTrace, timestamp, source, type, reference);
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    @Override
    public ITmfTimestamp getTimestamp() {
        if (fData != null) processData();
        return super.getTimestamp();
    }

    public TmfEventField[] extractItemFields() {
        if (fData != null) processData();
        return Arrays.copyOf(fColumnData, fColumnData.length);
    }

    private void processData() {
        String timeStampString = fData.get(CustomTraceDefinition.TAG_TIMESTAMP);
        String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
        Date date = null;
        if (timeStampInputFormat != null && timeStampString != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(timeStampInputFormat);
            try {
                date = dateFormat.parse(timeStampString);
                setTimestamp(new TmfTimestamp(date.getTime(), TIMESTAMP_SCALE));
            } catch (ParseException e) {
                setTimestamp(TmfTimestamp.ZERO);
            }
        } else {
            setTimestamp(TmfTimestamp.ZERO);
        }
        
        int i = 0;
        fColumnData = new TmfEventField[fDefinition.outputs.size()];
        for (OutputColumn outputColumn : fDefinition.outputs) {
            String value = fData.get(outputColumn.name);
            if (outputColumn.name.equals(CustomTraceDefinition.TAG_TIMESTAMP) && date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(fDefinition.timeStampOutputFormat);
                fColumnData[i++] = new TmfEventField(outputColumn.name, dateFormat.format(date));
            } else {
                fColumnData[i++] = new TmfEventField(outputColumn.name, (value != null ? value : "")); //$NON-NLS-1$
            }
        }
        fData = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fDefinition == null) ? 0 : fDefinition.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CustomEvent)) {
            return false;
        }
        CustomEvent other = (CustomEvent) obj;
        if (fDefinition == null) {
            if (other.fDefinition != null) {
                return false;
            }
        } else if (!fDefinition.equals(other.fDefinition)) {
            return false;
        }
        return true;
    }

}
