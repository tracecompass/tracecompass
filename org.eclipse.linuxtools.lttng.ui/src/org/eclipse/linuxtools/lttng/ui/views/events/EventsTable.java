/*******************************************************************************
 * Copyright (c) 2009, 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Aligned columns with domain terminology
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.events;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class EventsTable extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    // Table column names
    static private final String TIMESTAMP_COLUMN = Messages.EventsTable_timestampColumn;
    static private final String TRACE_COLUMN = Messages.EventsTable_traceColumn;
    static private final String MARKER_COLUMN = Messages.EventsTable_markerColumn;
    static private final String CONTENT_COLUMN = Messages.EventsTable_contentColumn;
    static private final String[] COLUMN_NAMES = new String[] {
            TIMESTAMP_COLUMN,
            TRACE_COLUMN,
            MARKER_COLUMN,
            CONTENT_COLUMN
    };

    static private final ColumnData[] COLUMN_DATA = new ColumnData[] {
            new ColumnData(COLUMN_NAMES[0], 150, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[1], 120, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[2], 200, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[3], 100, SWT.LEFT)
    };

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public EventsTable(Composite parent, int cacheSize) {
        super(parent, cacheSize, COLUMN_DATA);
        fTable.getColumns()[0].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_TIMESTAMP);
        fTable.getColumns()[1].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_REFERENCE);
        fTable.getColumns()[2].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_TYPE);
        fTable.getColumns()[3].setData(Key.FIELD_ID, TmfEventContent.FIELD_ID_CONTENT);
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
                    event.getReference().toString(),
                    event.getType().toString(),
                    event.getContent().toString()
            };
        }
        return fields;
    }

}
