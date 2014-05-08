/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.viewers.events;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Events table specific for LTTng 2.0 kernel traces
 */
public class LTTng2EventsTable extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    // Table column names
    static private final String TIMESTAMP_COLUMN = Messages.EventsTable_timestampColumn;
    static private final String CHANNEL_COLUMN = Messages.EventsTable_channelColumn;
    static private final String TYPE_COLUMN = Messages.EventsTable_typeColumn;
    static private final String CONTENT_COLUMN = Messages.EventsTable_contentColumn;
    static private final String[] COLUMN_NAMES = new String[] {
            TIMESTAMP_COLUMN,
            CHANNEL_COLUMN,
            TYPE_COLUMN,
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

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param cacheSize
     *            The size of the rows cache
     */
    public LTTng2EventsTable(Composite parent, int cacheSize) {
        super(parent, cacheSize, COLUMN_DATA);
        fTable.getColumns()[0].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_TIMESTAMP);
        fTable.getColumns()[1].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_REFERENCE);
        fTable.getColumns()[2].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_TYPE);
        fTable.getColumns()[3].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_CONTENT);
    }

    @Override
    public String[] getItemStrings(ITmfEvent event) {
        if (event == null) {
            return EMPTY_STRING_ARRAY;
        }
        return new String[] {
                event.getTimestamp().toString(),
                event.getReference(),
                event.getType().getName(),
                event.getContent().toString()
        };
    }
}
