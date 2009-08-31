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

package org.eclipse.linuxtools.lttng.ui.views.events;

import org.eclipse.linuxtools.lttng.ui.views.Labels;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.ui.views.TmfEventsView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * <b><u>EventsView</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class EventsView extends TmfEventsView {

    public static final String ID = Labels.EventsView_ID;

    // ========================================================================
    // Table data
    // ========================================================================

    // Table column names
    private final String TIMESTAMP_COLUMN = "Timestamp";
    private final String SOURCE_COLUMN    = "Source";
    private final String TYPE_COLUMN      = "Type";
    private final String REFERENCE_COLUMN = "Reference";
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

    public EventsView() {
    }

    /**
     * @param table
     */
    @Override
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
                    event.getContent().getContent() };
        }
        return fields;
    }

}
