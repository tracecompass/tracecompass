/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.trace.TmfExperimentUpdatedSignal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * <b><u>TmfEventsView</u></b>
 * <p>
 *
 * TODO: Implement me. Please.
 * TODO: Handle column selection, sort, ... generically (nothing less...)
 * TODO: Implement hide/display columns
 */
public class TmfEventsView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.viewer.events";

    private TmfExperiment fExperiment;
    private String fTitlePrefix;

    // ========================================================================
    // Table data
    // ========================================================================

    private Table fTable;

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

    // ========================================================================
    // Constructor
    // ========================================================================

    public TmfEventsView() {
    	super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
	public void createPartControl(Composite parent) {
    	
    	// Create a virtual table
    	// TODO: change SINGLE to MULTI line selection and adjust the selection listener
        final int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL;
        fTable = new Table(parent, style);

        // Set the table layout
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.horizontalSpan= columnData.length;
        fTable.setLayoutData(layoutData);

        // Some cosmetic enhancements
        fTable.setHeaderVisible(true);
        fTable.setLinesVisible(true);

        // Set the columns
        setColumnHeaders(fTable);

        // Handle the table item requests 
        fTable.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			public void widgetSelected(SelectionEvent e) {
				TmfTimestamp ts = extractTimestamp(fTable.getSelection()[0].getText());
				broadcastSignal(new TmfTimeSynchSignal(fTable, ts));
			}
        });

        // Handle the table item requests 
        fTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				final int index = fTable.indexOf(item);
				// Note: this works because handleEvent() is called once for each row, in sequence  
				if ((index >= cacheStartIndex ) && (index < cacheEndIndex)) {
					item.setText(extractItemFields(cache[index - cacheStartIndex]));
					return;
				}
				
				// *** TODO ***
				// This is broken!
				// 
				// This one fails to return any result : 
				// 		TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(fExperiment.getTimeRange(), index, 1) {
				// All these will return THE SAME RESULT!
				//		TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(index, 1) {
				// 		TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(index+10, 1) {
				// 		TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(null, 1) {
				// 		
				// THIS IS ONLY A TEMPORARY FIX! 
				
				TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(index, 1) {
					@Override
					public void handleData() {
						TmfEvent[] tmpEvent = getData();
						if ( (tmpEvent != null) && (tmpEvent.length > 0) ) {
							cache = tmpEvent;
						}
//						cacheStartIndex = index;
//						cacheEndIndex = index + cache.length; 
					}
				};
				fExperiment.processRequest(request, true);
				
				if (cache[0] != null) {
					item.setText(extractItemFields(cache[0]));
				}
				
			}
        });

        fTable.setItemCount(0);
    	fTitlePrefix = getTitle();

    	// If an experiment is already selected, update the table
    	fExperiment = TmfExperiment.getCurrentExperiment();
    	if (fExperiment != null) {
    		experimentSelected(new TmfExperimentSelectedSignal(fTable, fExperiment));
    	}
    }

    // Events cache - temporary stuff
//    private final int CACHE_SIZE = 1;
    private TmfEvent[] cache;
    private int cacheStartIndex = 0;
    private int cacheEndIndex = 0;

    
    private TmfTimestamp extractTimestamp(String entry) {
    	TmfTimestamp ts = null;

    	int pos = entry.indexOf('.');
    	if (pos > 0) {
    		String integer = entry.substring(0, pos);
    		String fraction = entry.substring(pos + 1);

    		byte exponent = (byte) -fraction.length();
    		String value = integer + fraction;
    		ts = new TmfTimestamp(new Long(value), exponent);
    	}

    	return ts;
    }

	/**
	 * @param table
	 * 
	 * FIXME: Add support for column selection
	 */
	protected void setColumnHeaders(Table table) {
        for (int i = 0; i < columnData.length; i++) {
            TableColumn column = new TableColumn(table, columnData[i].alignment, i);
            column.setText(columnData[i].header);
            column.setWidth(columnData[i].width);
        }
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
	public void setFocus() {
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
    	return "[TmfEventsView]";
    }

    // ========================================================================
    // Signal handlers
    // ========================================================================
    
	@TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
		// Update the trace reference
    	fExperiment = signal.getExperiment();
    	setPartName(fTitlePrefix + " - " + fExperiment.getExperimentId());

        // Perform the updates on the UI thread
        fTable.getDisplay().asyncExec(new Runnable() {
        	public void run() {
//        		// TODO: Potentially long operation. Add some feedback for the user
        		fTable.setSelection(0);
            	fTable.clearAll();
            	fTable.setItemCount(fExperiment.getNbEvents());        
        	}
        });
    }

	@TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        // Perform the refresh on the UI thread
    	fTable.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!fTable.isDisposed() && fExperiment != null) {
			    	fTable.setItemCount(fExperiment.getNbEvents());        
				}
			}
        });
    }

    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
    	if (signal.getSource() != fTable && fExperiment != null) {
    		final int index = (int) fExperiment.getIndex(signal.getCurrentTime());
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
