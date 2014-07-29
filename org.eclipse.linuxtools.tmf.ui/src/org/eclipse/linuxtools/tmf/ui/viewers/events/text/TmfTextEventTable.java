/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events.text;

import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEvent;
import org.eclipse.linuxtools.tmf.core.trace.text.TextTraceEventContent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.widgets.Composite;

/**
 * Event table for text traces, which has one column for every event field.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 * @deprecated Users of this class should instead use
 *             {@link TmfEventsTable#TmfEventsTable(Composite, int, java.util.Collection)}
 *             , by passing
 *             {@link org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn}
 *             or
 *             {@link org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableFieldColumn}
 *             .
 */
@Deprecated
public class TmfTextEventTable extends TmfEventsTable {

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     * @param columnData
     *            The column data to use for this table
     */
    public TmfTextEventTable(Composite parent, int cacheSize, ColumnData[] columnData) {
        super(parent, cacheSize, columnData);
    }

    /**
     * @param event
     *            The event to get the column strings for. It should be an
     *            instance of {@link TextTraceEvent}.
     */
    @Override
    public String[] getItemStrings(ITmfEvent event) {
        if (event instanceof TextTraceEvent) {
            List<TextTraceEventContent> fields = ((TextTraceEvent) event).getContent().getFields();
            String[] strings = new String[fields.size()];
            for (int i = 0; i < strings.length; i++) {
                Object value = fields.get(i).getValue();
                strings[i] = (value == null ? EMPTY_STRING : value.toString());
            }
            return strings;
        }
        return EMPTY_STRING_ARRAY;
    }
}
