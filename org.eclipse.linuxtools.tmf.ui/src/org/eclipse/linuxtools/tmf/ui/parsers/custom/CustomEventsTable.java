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
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class CustomEventsTable extends TmfEventsTable {

    private CustomTraceDefinition fDefinition;
    
    public CustomEventsTable(CustomTraceDefinition definition, Composite parent, int cacheSize) {
        super(parent, cacheSize);
        fDefinition = definition;
        createColumnHeaders();
    }

    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"; //$NON-NLS-1$
    private static final SimpleDateFormat TIMESTAMP_SIMPLE_DATE_FORMAT = new SimpleDateFormat(TIMESTAMP_FORMAT); 
    static {
        TIMESTAMP_SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
    }

    protected void createColumnHeaders() {
		if (fDefinition == null)
			return;
    	List<ColumnData> columnData = new LinkedList<ColumnData>();
		for (OutputColumn outputColumn : fDefinition.outputs) {
			ColumnData column = new ColumnData(outputColumn.name, 0, SWT.LEFT);
			columnData.add(column);
		}
    	setColumnHeaders((ColumnData[]) columnData.toArray(new ColumnData[0]));
    }

//    @Override
//    public void createColumnHeaders(final Table table) {
//        if (fDefinition == null) return; // ignore when called by the super constructor
//        for (OutputColumn outputColumn : fDefinition.outputs) {
//            TableColumn column = new TableColumn(table, SWT.LEFT);
//            column.setText(outputColumn.name);
//            column.pack();
//        }
//    }

    @Override
    public String[] extractItemFields(TmfEvent event) {
        if (event instanceof CustomEvent) {
            return ((CustomEvent) event).extractItemFields();
        }
        return new String[0];
    }
}
