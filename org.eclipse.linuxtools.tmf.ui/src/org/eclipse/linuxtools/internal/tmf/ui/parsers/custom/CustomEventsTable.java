/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Events table for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class CustomEventsTable extends TmfEventsTable {

    private final CustomTraceDefinition fDefinition;

    /**
     * Constructor.
     *
     * @param definition
     *            Trace definition object
     * @param parent
     *            Parent composite of the view
     * @param cacheSize
     *            How many events to keep in cache
     */
    public CustomEventsTable(CustomTraceDefinition definition, Composite parent, int cacheSize) {
        super(parent, cacheSize, new ColumnData[0]);
        fDefinition = definition;
        createColumnHeaders();
    }

    /**
     * Create the table's headers.
     */
    protected void createColumnHeaders() {
        if (fDefinition == null) {
            return;
        }
        List<ColumnData> columnData = new LinkedList<>();
        for (OutputColumn outputColumn : fDefinition.outputs) {
            ColumnData column = new ColumnData(outputColumn.name, 0, SWT.LEFT);
            columnData.add(column);
        }
        setColumnHeaders(columnData.toArray(new ColumnData[0]));
    }

    @Override
    public TmfEventField[] extractItemFields(ITmfEvent event) {
        if (event instanceof CustomEvent) {
            TmfEventField[] fields = ((CustomEvent) event).extractItemFields();
//            String[] labels = new String[fields.length];
//            for (int i = 0; i < fields.length; i++) {
//                labels[i] = (String) fields[i].getValue();
//            }
            return fields;
        }
        return new TmfEventField[0];
    }
}
