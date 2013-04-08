/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.gdbtrace.ui.views.events;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.internal.gdbtrace.core.event.GdbTraceEvent;
import org.eclipse.linuxtools.internal.gdbtrace.core.event.GdbTraceEventContent;
import org.eclipse.linuxtools.internal.gdbtrace.core.trace.GdbTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

/**
 * GDB Events Table
 * @author Patrick Tasse
 *
 */
public class GdbEventsTable extends TmfEventsTable {

    private static final String TRACE_FRAME_COLUMN = GdbTraceEventContent.TRACE_FRAME;
    private static final String TRACEPOINT_COLUMN = GdbTraceEventContent.TRACEPOINT;
    private static final String FILE_COLUMN = "File"; //$NON-NLS-1$
    private static final String CONTENT_COLUMN = "Content"; //$NON-NLS-1$
    private static final ColumnData[] COLUMN_DATA = new ColumnData[] {
        new ColumnData(TRACE_FRAME_COLUMN, 100, SWT.RIGHT),
        new ColumnData(TRACEPOINT_COLUMN, 100, SWT.RIGHT),
        new ColumnData(FILE_COLUMN, 100, SWT.LEFT),
        new ColumnData(CONTENT_COLUMN, 100, SWT.LEFT)
    };

    private GdbTrace fSelectedTrace = null;
    private long fSelectedFrame = 0;

    /**
     * Constructor
     * @param parent the parent
     * @param cacheSize the cache size
     */
    public GdbEventsTable(Composite parent, int cacheSize) {
        super(parent, cacheSize, COLUMN_DATA);
        // Set search field ids for event filter
        fTable.getColumns()[2].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_REFERENCE);
        fTable.getColumns()[3].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_CONTENT);

        // Synchronize currently selected frame in GDB with table selection
        addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent e) {
                TableItem[] selection = fTable.getSelection();
                if (selection.length > 0) {
                    TableItem selectedTableItem = selection[0];
                    if (selectedTableItem != null) {
                        Object data = selectedTableItem.getData();
                        if (data instanceof GdbTraceEvent) {
                            GdbTraceEvent event = (GdbTraceEvent) data;
                            GdbTrace gdbTrace = (GdbTrace) event.getTrace();
                            GdbTraceEventContent content = (GdbTraceEventContent) event.getContent();
                            gdbTrace.selectFrame(content.getFrameNumber());
                            fSelectedTrace = gdbTrace;
                            fSelectedFrame = content.getFrameNumber();
                            return;
                        }
                    }
                }
                fSelectedTrace = null;
            }
        });
    }

    @Override
    public void setTrace(ITmfTrace trace, boolean disposeOnClose) {
        super.setTrace(trace, disposeOnClose);
        if (trace instanceof GdbTrace) {
            fSelectedTrace = (GdbTrace) trace;
            fSelectedFrame = 0;
        } else if (trace instanceof TmfExperiment) {
            TmfExperiment experiment = (TmfExperiment) trace;
            if (experiment.getTraces().length > 0) {
                fSelectedTrace = (GdbTrace) experiment.getTraces()[0];
                fSelectedFrame = 0;
            }
        }
    }

    @Override
    protected void populateCompleted() {
        if (fSelectedTrace != null) {
            fSelectedTrace.selectFrame(fSelectedFrame);
        }
    }

    @Override
    protected ITmfEventField[] extractItemFields(ITmfEvent event) {
        ITmfEventField[] fields = new TmfEventField[0];
        if (event != null) {
            GdbTraceEventContent content = (GdbTraceEventContent) event.getContent();
            fields = new TmfEventField[] {
                    new TmfEventField(TRACE_FRAME_COLUMN, content.getFrameNumber(), null),
                    new TmfEventField(TRACEPOINT_COLUMN, content.getTracepointNumber(), null),
                    new TmfEventField(FILE_COLUMN, event.getReference(), null),
                    new TmfEventField(CONTENT_COLUMN, content.toString(), null)
            };
        }
        return fields;
    }

    @Override
    @TmfSignalHandler
    public void currentTimeUpdated(final TmfTimeSynchSignal signal) {
        // do not synchronize on time
    }
}
