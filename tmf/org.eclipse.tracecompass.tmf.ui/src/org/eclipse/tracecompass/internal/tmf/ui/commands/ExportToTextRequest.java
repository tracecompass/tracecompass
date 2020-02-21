/*******************************************************************************
 * Copyright (c) 2013, 2014 Kalray, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *   Bernd Hufmann - Adapted to new events table column API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.commands;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;

/**
 * This TMF Requests exports traces to text files.
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExportToTextRequest extends TmfEventRequest {

    private final Writer fWriter;
    private final ITmfFilter fFilter;
    private final List<TmfEventTableColumn> fColumns;

    /**
     * Constructor
     * @param w
     *          a Writer, typically a FileWriter.
     * @param filter
     *          a TmfFilter, if we want to filter some events. May be <code>null</code>.
     * @param columns
     *            the {@link TmfEventTableColumn} requesting the export (may be <code>null</code>)
     */
    public ExportToTextRequest(Writer w, ITmfFilter filter, List<TmfEventTableColumn> columns) {
        super(ITmfEvent.class, TmfTimeRange.ETERNITY, 0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        this.fWriter = w;
        this.fFilter = filter;
        this.fColumns = columns;
    }

    @Override
    public void handleData(final ITmfEvent event) {
        super.handleData(event);
        if (isCancelled()) {
            return;
        }
        try {
            if (fFilter == null || fFilter.matches(event)) {
                if (fColumns != null) {
                    boolean needTab = false;
                    for (TmfEventTableColumn column : fColumns) {
                        if (needTab) {
                            fWriter.write('\t');
                        }
                        fWriter.write(column.getItemString(event));
                        needTab = true;
                    }
                } else { // fallback to default formatting
                    fWriter.write(event.getTimestamp().toString());
                    fWriter.write('\t');
                    fWriter.write(event.getName());
                    fWriter.write('\t');
                    ITmfEventField content = event.getContent();
                    Object value = content.getValue();
                    if (value != null) {
                        fWriter.write(value.toString());
                    }
                }
                fWriter.write('\n');
            }
        } catch (IOException ex) {
            fail(ex);
        }
    }

}
