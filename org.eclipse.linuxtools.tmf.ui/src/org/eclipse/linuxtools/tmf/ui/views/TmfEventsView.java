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
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.trace.TmfTraceUpdatedSignal;
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
				// Get the timestamp string
				String time = fTable.getSelection()[0].getText();

				// Compose a timestamp
				int pos = time.indexOf('.');
				String integer = time.substring(0, pos);
				String fraction = time.substring(pos + 1);

				byte exponent = (byte) -fraction.length();
				String value = integer + fraction;
				TmfTimestamp ts = new TmfTimestamp(new Long(value), exponent);

				// Generate the synchronization event
				TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(fTable, ts));
			}
        });

        // Handle the table item requests 
        fTable.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				int index = fTable.indexOf(item);
				final TmfEvent[] evt = new TmfEvent[1];
				TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(index, 0, 1) {
					@Override
					public void handleData() {
						TmfEvent[] result = getData();
						evt[0] = (result.length > 0) ? result[0] : null;
					}
				};
				fExperiment.processRequest(request, true);
				item.setText(extractItemFields(evt[0]));
			}
        });

        fTable.setItemCount(0);
    	fTitlePrefix = getTitle();
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
				event.getReference().getValue().toString(),
				event.getContent().getContent()
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

    // ========================================================================
    // Signal handlers
    // ========================================================================
    
	@TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
		// Update the trace reference
		if (fExperiment != null)
			fExperiment.dispose();
    	fExperiment = signal.getExperiment();
    	setPartName(fTitlePrefix + " - " + fExperiment.getExperimentId());

        // Perform the updates on the UI thread
        fTable.getDisplay().asyncExec(new Runnable() {
        	public void run() {
            	fTable.clearAll();
            	fTable.setItemCount(fExperiment.getNbEvents());        
        	}
        });
    }

	@TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        // Perform the refresh on the UI thread
    	fTable.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!fTable.isDisposed()) {
			    	fTable.setItemCount(fExperiment.getNbEvents());        
				}
			}
        });
    }

    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
    	if (signal.getSource() != fTable) {
    		final int index = fExperiment.getIndex(signal.getCurrentTime());
            // Perform the updates on the UI thread
            fTable.getDisplay().asyncExec(new Runnable() {
            	public void run() {
            		fTable.setSelection(index);
            	}
            });
    	}
    }

}