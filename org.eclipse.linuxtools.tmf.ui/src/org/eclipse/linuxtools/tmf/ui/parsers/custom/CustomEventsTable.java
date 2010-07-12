/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class CustomEventsTable extends TmfEventsTable {

    private CustomTraceDefinition fDefinition;
    
    public CustomEventsTable(CustomTraceDefinition definition, Composite parent, int cacheSize) {
        super(parent, cacheSize);
        fDefinition = definition;
        createColumnHeaders(fTable);
    }

    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final SimpleDateFormat TIMESTAMP_SIMPLE_DATE_FORMAT = new SimpleDateFormat(TIMESTAMP_FORMAT); 
    static {
        TIMESTAMP_SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Override
    public void createColumnHeaders(final Table table) {
        if (fDefinition == null) return; // ignore when called by the super constructor
        for (OutputColumn outputColumn : fDefinition.outputs) {
            TableColumn column = new TableColumn(table, SWT.LEFT);
            column.setText(outputColumn.name);
            column.pack();
        }
    }

    @Override
    public String[] extractItemFields(TmfEvent event) {
        if (event instanceof CustomEvent) {
            return ((CustomEvent) event).extractItemFields();
        }
        return new String[0];
    }
}
