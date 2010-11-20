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
 *   Francois Chouinard - Replaced Table by TmfVirtualTable
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import org.eclipse.linuxtools.tmf.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.component.TmfComponent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.ColumnData;
import org.eclipse.linuxtools.tmf.ui.widgets.TmfVirtualTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * <b><u>TmfEventsTable</u></b>
 */
public class TmfEventsTable extends TmfComponent {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    protected TmfVirtualTable fTable;
    protected ITmfTrace fTrace;
    protected boolean fPackDone = false;

    // Table column names
    static private final String[] COLUMN_NAMES =  new String[] {
        Messages.TmfEventsTable_TimestampColumnHeader,
        Messages.TmfEventsTable_SourceColumnHeader,
        Messages.TmfEventsTable_TypeColumnHeader,
        Messages.TmfEventsTable_ReferenceColumnHeader,
        Messages.TmfEventsTable_ContentColumnHeader
    };

    static private ColumnData[] COLUMN_DATA = new ColumnData[] {
        new ColumnData(COLUMN_NAMES[0], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[1], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[2], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[3], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[4], 100, SWT.LEFT)
    };

    // ------------------------------------------------------------------------
    // Event cache
    // ------------------------------------------------------------------------

    private final int  fCacheSize;
    private TmfEvent[] fCache;
    private int fCacheStartIndex = 0;
    private int fCacheEndIndex   = 0;

    private boolean fDisposeOnClose;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public TmfEventsTable(Composite parent, int cacheSize) {
    	this(parent, cacheSize, COLUMN_DATA);
    }

    public TmfEventsTable(Composite parent, int cacheSize, ColumnData[] columnData) {
        super("TmfEventsTable"); //$NON-NLS-1$
        
        fCacheSize = cacheSize;
        fCache = new TmfEvent[fCacheSize];
        
        // Create a virtual table
        final int style = SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER;
        fTable = new TmfVirtualTable(parent, style);

        // Set the table layout
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        fTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);

        // Set the columns
        setColumnHeaders(columnData);

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

            @Override
			@SuppressWarnings("unchecked")
			public void handleEvent(Event event) {

                final TableItem item = (TableItem) event.item;
                final int index = fTable.indexOf(item);

                // Note: this works because handleEvent() is called once for each row, in sequence  
                if ((index >= fCacheStartIndex) && (index < fCacheEndIndex)) {
                    int i = index - fCacheStartIndex;
                    item.setText(extractItemFields(fCache[i]));
                    item.setData(new TmfTimestamp(fCache[i].getTimestamp()));
                    return;
                }

                fCacheStartIndex = index;
                fCacheEndIndex = index;

                TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(TmfEvent.class, index, fCacheSize) {
                	private int count = 0;

                	@Override
                    public void handleData(TmfEvent event) {
                		super.handleData(event);
                        if (event != null) {
                            fCache[count++] = event.clone();
                            fCacheEndIndex++;
                        }
                    }

                };

                ((ITmfDataProvider<TmfEvent>) fTrace).sendRequest(request);
                try {
                    request.waitForCompletion();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                if (fCache[0] != null && fCacheStartIndex == index) {
                    item.setText(extractItemFields(fCache[0]));
                    item.setData(new TmfTimestamp(fCache[0].getTimestamp()));
                    packColumns();
                }
                
            }
        });

        fTable.setItemCount(0);
    }

    @Override
	public void dispose() {
        fTable.dispose();
        if (fTrace != null && fDisposeOnClose) {
            fTrace.dispose();
        }
        super.dispose();
    }

    public TmfVirtualTable getTable() {
        return fTable;
    }
    
    /**
     * @param table
     * 
     * FIXME: Add support for column selection
     */
    protected void setColumnHeaders(ColumnData[] columnData) {
    	fTable.setColumnHeaders(columnData);
    }

    protected void packColumns() {
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

    /**
     * @param trace
     * @param disposeOnClose true if the trace should be disposed when the table is disposed
     */
    public void setTrace(ITmfTrace trace, boolean disposeOnClose) {
        if (fTrace != null && fDisposeOnClose) {
            fTrace.dispose();
        }
        fTrace = trace;
        fDisposeOnClose = disposeOnClose;
        
        // Perform the updates on the UI thread
        fTable.getDisplay().syncExec(new Runnable() {
            @Override
			public void run() {
                //fTable.setSelection(0);
                fTable.removeAll();
                fCacheStartIndex = fCacheEndIndex = 0; // Clear the cache
                
                if (!fTable.isDisposed() && fTrace != null) {
                    //int nbEvents = (int) fTrace.getNbEvents();
                    //fTable.setItemCount((nbEvents > 100) ? nbEvents : 100);
                    fTable.setItemCount((int) fTrace.getNbEvents());
                }
            }
        });
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------
    
    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        if ((signal.getExperiment() != fTrace) || fTable.isDisposed()) return;
        // Perform the refresh on the UI thread
        fTable.getDisplay().asyncExec(new Runnable() {
            @Override
			public void run() {
                if (!fTable.isDisposed() && fTrace != null) {
                    fTable.setItemCount((int) fTrace.getNbEvents());
                    fTable.refresh();
                }
            }
        });
    }
    
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if ((signal.getTrace() != fTrace ) || fTable.isDisposed()) return;
        // Perform the refresh on the UI thread
        fTable.getDisplay().asyncExec(new Runnable() {
            @Override
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
        if (!fRefreshPending && !fTable.isDisposed()) {
            // Perform the refresh on the UI thread
            fRefreshPending = true;
            fTable.getDisplay().asyncExec(new Runnable() {
                @Override
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
    public void currentTimeUpdated(final TmfTimeSynchSignal signal) {
    	if ((signal.getSource() != fTable) && (fTrace != null) && (!fTable.isDisposed())) {

    		// Create a request for one event that will be queued after other ongoing requests. When this request is completed 
    		// do the work to select the actual event with the timestamp specified in the signal. This procedure prevents 
    		// the method fTrace.getRank() from interfering and delaying ongoing requests.
    		final TmfDataRequest<TmfEvent> subRequest = new TmfDataRequest<TmfEvent>(TmfEvent.class, 0, 1, ExecutionType.FOREGROUND) {

    			@Override
    			public void handleData(TmfEvent event) {
    				super.handleData(event);
    			}

    			@Override
    			public void handleCompleted() {
    				// Get the rank for the event selection in the table
    				final int index = (int) fTrace.getRank(signal.getCurrentTime());

    				fTable.getDisplay().asyncExec(new Runnable() {
    					@Override
                        public void run() {
    						// Return if table is disposed
    						if (fTable.isDisposed()) return;

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
    				super.handleCompleted();
    			}
    		};

    		@SuppressWarnings("unchecked")
            TmfExperiment<TmfEvent> experiment = (TmfExperiment<TmfEvent>)TmfExperiment.getCurrentExperiment();
    		if (experiment != null) {
    			experiment.sendRequest(subRequest);
    		}
    	}
	}

}
