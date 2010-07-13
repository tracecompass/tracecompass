/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.events;

import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class EventsTable extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    // Table column names
    static private final String TIMESTAMP_COLUMN = "Timestamp";
    static private final String SOURCE_COLUMN    = "Source";
    static private final String TYPE_COLUMN      = "Type";
    static private final String REFERENCE_COLUMN = "Reference";
    static private final String CONTENT_COLUMN   = "Content";
    static private final String[] COLUMN_NAMES =  new String[] {
        TIMESTAMP_COLUMN,
        SOURCE_COLUMN,
        TYPE_COLUMN,
        REFERENCE_COLUMN,
        CONTENT_COLUMN
    };

    // Column data
    static private class ColumnData {
        public final String header;
        public final int    width;
        public final int    alignment;

        public ColumnData(String h, int w, int a) {
            header = h;
            width = w;
            alignment = a;
        }
    };

    static private final ColumnData[] COLUMN_DATA = new ColumnData[] {
        new ColumnData(COLUMN_NAMES[0], 125, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[1], 100, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[2], 200, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[3], 200, SWT.LEFT),
        new ColumnData(COLUMN_NAMES[4], 100, SWT.LEFT)
    };

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public EventsTable(Composite parent, int cacheSize) {
        super(parent, cacheSize);
    }

    /**
     * @param table
     * 
     * FIXME: Add support for column selection
     */
    @Override
    protected void createColumnHeaders(Table table) {
        for (int i = 0; i < COLUMN_DATA.length; i++) {
            final TableColumn column = new TableColumn(table, COLUMN_DATA[i].alignment, i);
            column.setText(COLUMN_DATA[i].header);
            column.setWidth(COLUMN_DATA[i].width);
            // TODO: Investigate why the column resizing doesn't work by default
            // Anything to do with SWT_VIRTUAL?
            column.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    // TODO Auto-generated method stub
                }
                public void widgetSelected(SelectionEvent e) {
                    column.pack();
                }
            });
        }
    }

    /**
     * @param event
     * @return
     */
    @Override
    protected String[] extractItemFields(TmfEvent event) {
        String[] fields = new String[0];
        
        if (event != null) {
            fields = new String[] {
                    event.getTimestamp().toString(),
                    event.getSource().toString(),
                    event.getType().toString(),
                    event.getReference().toString(),
                    ((LttngEventContent)event.getContent()).toString() 
                };
        }
        return fields;
    }

}
