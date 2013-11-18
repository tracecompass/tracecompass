/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;

/**
 * This TMF Requests exports traces to text files.
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExportToTextRequest extends TmfDataRequest {

    private final Writer fWriter;
    private final ITmfFilter fFilter;
    private final TmfEventsTable fTable;
    private IOException fIOException;

    /**
     * Constructor
     * @param w
     *          a Writer, typically a FileWriter.
     * @param filter
     *          a TmfFilter, if we want to filter some events. May be <code>null</code>.
     * @param table
     *            the {@link TmfEventsTable} requesting the export (may be <code>null</code>)
     */
    public ExportToTextRequest(Writer w, ITmfFilter filter, TmfEventsTable table) {
        super(ITmfEvent.class, 0, TmfDataRequest.ALL_DATA, ExecutionType.FOREGROUND);
        this.fWriter = w;
        this.fFilter = filter;
        this.fTable = table;
    }

    /**
     * Gets the IOException thrown by this export request, if any.
     * @return the fIoException
     */
    public IOException getIOException() {
        return fIOException;
    }

    @Override
    public void handleData(final ITmfEvent event) {
        super.handleData(event);
        if (isCancelled()) {
            return;
        }
        try {
            if (fFilter == null || fFilter.matches(event)) {
                if (fTable != null) {
                    ITmfEventField[] fields = fTable.getItemFields(event);
                    boolean needTab = false;
                    for (ITmfEventField field: fields) {
                        if (needTab) {
                            fWriter.write('\t');
                        }
                        Object value = field.getValue();
                        printValue(value);
                        needTab = true;
                    }
                } else { // fallback to default formatting
                    fWriter.write(event.getTimestamp().toString());
                    fWriter.write('\t');
                    fWriter.write(event.getSource());
                    fWriter.write('\t');
                    fWriter.write(event.getType().getName());
                    fWriter.write('\t');
                    fWriter.write(event.getReference());
                    fWriter.write('\t');
                    ITmfEventField content = event.getContent();
                    printValue(content.getValue());
                }
                fWriter.write('\n');
            }
        } catch (IOException ex) {
            fIOException = ex;
            fail();
        }
    }

    private void printValue(Object value) throws IOException {
        if (value != null) {
            fWriter.write(value.toString());
        }
    }

}
