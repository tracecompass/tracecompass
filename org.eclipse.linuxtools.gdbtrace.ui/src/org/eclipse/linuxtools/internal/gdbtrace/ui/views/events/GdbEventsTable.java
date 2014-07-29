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

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.internal.gdbtrace.core.event.GdbTraceEvent;
import org.eclipse.linuxtools.internal.gdbtrace.core.event.GdbTraceEventContent;
import org.eclipse.linuxtools.internal.gdbtrace.core.trace.GdbTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableFieldColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.ImmutableList;

/**
 * GDB Events Table
 * @author Patrick Tasse
 */
public class GdbEventsTable extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Column definition
    // ------------------------------------------------------------------------

    private static final Collection<TmfEventTableColumn> GDB_COLUMNS = ImmutableList.of(
            new GdbTraceFrameColumn(),
            new GdbTracepointColumn(),
            new GdbFileColumn()
            );

    private static class GdbTraceFrameColumn extends TmfEventTableFieldColumn {
        public GdbTraceFrameColumn() {
            super(GdbTraceEventContent.TRACE_FRAME);
        }
    }

    private static class GdbTracepointColumn extends TmfEventTableFieldColumn {
        public GdbTracepointColumn() {
            super(GdbTraceEventContent.TRACEPOINT);
        }
    }

    private static class GdbFileColumn extends TmfEventTableColumn {

        public GdbFileColumn() {
            super("File"); //$NON-NLS-1$
        }

        @Override
        public String getItemString(ITmfEvent event) {
            String ret = event.getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterFieldId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private GdbTrace fSelectedTrace = null;
    private long fSelectedFrame = 0;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param parent
     *            the parent
     * @param cacheSize
     *            the cache size
     */
    public GdbEventsTable(Composite parent, int cacheSize) {
        super(parent, cacheSize, GDB_COLUMNS);
        // Set the alignment of the first two columns
        fTable.getColumns()[0].setAlignment(SWT.RIGHT);
        fTable.getColumns()[1].setAlignment(SWT.RIGHT);

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
                            GdbTraceEventContent content = event.getContent();
                            selectFrame(gdbTrace, content.getFrameNumber());
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
    @TmfSignalHandler
    public void currentTimeUpdated(final TmfTimeSynchSignal signal) {
        // do not synchronize on time
    }

    private void selectFrame(final GdbTrace gdbTrace, final long frameNumber) {
        Job b = new Job("GDB Trace select frame") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // This sends commands to GDB and can potentially wait on the UI
                // thread (gdb traces console buffer full) so it needs to be
                // exectued on a non-UI thread
                gdbTrace.selectFrame(frameNumber);
                fSelectedTrace = gdbTrace;
                fSelectedFrame = frameNumber;
                return Status.OK_STATUS;
            }
        };
        b.setSystem(true);
        b.schedule();
    }

    @Override
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        super.traceUpdated(signal);
        if (fSelectedTrace.getNbFrames() == fSelectedTrace.getNbEvents()) {
            selectFrame(fSelectedTrace, fSelectedFrame);
        }
    }
}
