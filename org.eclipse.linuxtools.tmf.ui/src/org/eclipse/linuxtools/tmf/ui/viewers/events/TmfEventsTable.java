/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Factored out from events view
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import org.eclipse.linuxtools.tmf.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.component.TmfComponent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * <b><u>TmfEventsTable</u></b>
 */
public class TmfEventsTable extends TmfComponent {

//    private Shell fShell;
    
    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    protected Table fTable;
    protected ITmfTrace fTrace;
    protected boolean fPackDone = false;

    // Table column names
    private final String TIMESTAMP_COLUMN = "Timestamp";
    private final String SOURCE_COLUMN    = "Source";
    private final String TYPE_COLUMN      = "Type";
    private final String REFERENCE_COLUMN = "File";
    private final String CONTENT_COLUMN   = "Content";
    private final String[] columnProperties =  new String[] {
        TIMESTAMP_COLUMN,
        SOURCE_COLUMN,
        TYPE_COLUMN,
        REFERENCE_COLUMN,
        CONTENT_COLUMN
    };

    // Column data
    private class ColumnData {
        public final String header;
        public final int    width;
        public final int    alignment;

        public ColumnData(String h, int w, int a) {
            header = h;
            width = w;
            alignment = a;
        }
    };

    private ColumnData[] columnData = new ColumnData[] {
        new ColumnData(columnProperties[0], 100, SWT.LEFT),
        new ColumnData(columnProperties[1], 100, SWT.LEFT),
        new ColumnData(columnProperties[2], 100, SWT.LEFT),
        new ColumnData(columnProperties[3], 100, SWT.LEFT),
        new ColumnData(columnProperties[4], 100, SWT.LEFT)
    };

    // ------------------------------------------------------------------------
    // Event cache
    // ------------------------------------------------------------------------

    private final int fCacheSize;
    private TmfEvent[] cache = new TmfEvent[1];
    private int cacheStartIndex = 0;
    private int cacheEndIndex = 0;
//    private IResourceChangeListener fResourceChangeListener;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public TmfEventsTable(Composite parent, int cacheSize) {
        super("TmfEventsTable");
        
        fCacheSize = cacheSize;
        
//        fShell = parent.getShell();
        
        // Create a virtual table
        // TODO: change SINGLE to MULTI line selection and adjust the selection listener
        final int style = SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL;
        fTable = new Table(parent, style);

        // Set the table layout
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);

        // Set the columns
        createColumnHeaders(fTable);

        // Handle the table item requests 
        fTable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TmfTimestamp ts = (TmfTimestamp) fTable.getSelection()[0].getData();
                broadcast(new TmfTimeSynchSignal(fTable, ts));
            }
        });

        // Handle the table item requests 
        fTable.addListener(SWT.SetData, new Listener() {

            @SuppressWarnings("unchecked")
			public void handleEvent(Event event) {

                final TableItem item = (TableItem) event.item;
                final int index = fTable.indexOf(item);

                // Note: this works because handleEvent() is called once for each row, in sequence  
                if ((index >= cacheStartIndex ) && (index < cacheEndIndex)) {
                    int i = index - cacheStartIndex;
                    item.setText(extractItemFields(cache[i]));
                    item.setData(new TmfTimestamp(cache[i].getTimestamp()));
                    return;
                }

                TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(TmfEvent.class, index, fCacheSize) {
                    @Override
                    public void handleData() {
                        TmfEvent[] tmpEvent = getData();
                        if ((tmpEvent != null) && (tmpEvent.length > 0)) {
                            cache = tmpEvent;
                            cacheStartIndex = index;
                            cacheEndIndex = index + tmpEvent.length;
                        }
                    }
                };
                ((ITmfDataProvider<TmfEvent>) fTrace).sendRequest(request);
                try {
                    request.waitForCompletion();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                if (cache[0] != null && cacheStartIndex == index) {
                    item.setText(extractItemFields(cache[0]));
                    item.setData(new TmfTimestamp(cache[0].getTimestamp()));
                    packColumns(fTable);
                }
                
            }
        });

        fTable.setItemCount(0);
    }

    public void dispose() {
        fTable.dispose();
        if (fTrace != null) {
            fTrace.dispose();
        }
        super.dispose();
    }

    public Table getTable() {
        return fTable;
    }
    
    /**
     * @param table
     * 
     * FIXME: Add support for column selection
     */
    protected void createColumnHeaders(Table table) {
        for (int i = 0; i < columnData.length; i++) {
            TableColumn column = new TableColumn(table, columnData[i].alignment, i);
            column.setText(columnData[i].header);
            column.setWidth(columnData[i].width);
        }
    }

    protected void packColumns(Table table) {
        if (fPackDone) return;
        for (TableColumn column : fTable.getColumns()) {
            int headerWidth = column.getWidth();
            column.pack();
            if (column.getWidth() < headerWidth) {
                column.setWidth(headerWidth);
            }
        }
        fPackDone = true;
    }
    
    /**
     * @param event
     * @return
     * 
     * FIXME: Add support for column selection
     */
    protected String[] extractItemFields(TmfEvent event) {
        String[] fields = new String[0];
        if (event != null) {
            fields = new String[] {
                new Long(event.getTimestamp().getValue()).toString(),       
                event.getSource().getSourceId().toString(),
                event.getType().getTypeId().toString(),
                event.getReference().getReference().toString(),
                event.getContent().toString()
            };
        }
        return fields;
    }

    public void setFocus() {
        fTable.setFocus();
    }

    public void setTrace(ITmfTrace trace) {
        fTrace = trace;
        
        // Perform the updates on the UI thread
        fTable.getDisplay().syncExec(new Runnable() {
            public void run() {
                //fTable.setSelection(0); PATA
                fTable.removeAll();
                cacheStartIndex = cacheEndIndex = 0; // Clear the cache
                
                if (!fTable.isDisposed() && fTrace != null) {
                    //int nbEvents = (int) fTrace.getNbEvents();
                    //fTable.setItemCount((nbEvents > 100) ? nbEvents : 100);
                    fTable.setItemCount((int) fTrace.getNbEvents());
                }
            }
        });
//        ProgressMonitorDialog dialog = new ProgressMonitorDialog(fShell);
//        try {
//            dialog.run(false, false, new IRunnableWithProgress() {
//                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//                    monitor.beginTask("Cleaning up, please wait", 0);
//
//
//                    monitor.done();
//                }
//              });
//        } catch (InvocationTargetException e) {
//        } catch (InterruptedException e) {
//        }
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------
    
    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        if (signal.getExperiment() != fTrace) return;
        // Perform the refresh on the UI thread
        fTable.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (!fTable.isDisposed() && fTrace != null) {
                    fTable.setItemCount((int) fTrace.getNbEvents());
                }
            }
        });
    }
    
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if (signal.getTrace() != fTrace) return;
        // Perform the refresh on the UI thread
        fTable.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (!fTable.isDisposed() && fTrace != null) {
                    //int nbEvents = (int) fTrace.getNbEvents();
                    //fTable.setItemCount((nbEvents > 100) ? nbEvents : 100);
                    fTable.setItemCount((int) fTrace.getNbEvents());
                }
            }
        });
    }

    private boolean fRefreshPending = false;
    @TmfSignalHandler
    public synchronized void rangeSynched(TmfRangeSynchSignal signal) {
        if (!fRefreshPending) {
            // Perform the refresh on the UI thread
            fRefreshPending = true;
            fTable.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    fRefreshPending = false;
                    if (!fTable.isDisposed() && fTrace != null) {
                        fTable.setItemCount((int) fTrace.getNbEvents());
                    }
                }
            });
        }
    }
    
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
        if (signal.getSource() != fTable && fTrace != null) {
            final int index = (int) fTrace.getRank(signal.getCurrentTime());
            // Perform the updates on the UI thread
            fTable.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    fTable.setSelection(index);
                    // The timestamp might not correspond to an actual event
                    // and the selection will point to the next experiment event.
                    // But we would like to display both the event before and
                    // after the selected timestamp.
                    // This works fine by default except when the selected event
                    // is the top displayed event. The following ensures that we
                    // always see both events.
                    if ((index > 0) && (index == fTable.getTopIndex())) {
                        fTable.setTopIndex(index - 1);
                    }
                }
            });
        }
    }
}
