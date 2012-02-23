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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class CustomEventsTable extends TmfEventsTable {

    private CustomTraceDefinition fDefinition;
    
    public CustomEventsTable(CustomTraceDefinition definition, Composite parent, int cacheSize) {
        super(parent, cacheSize, new ColumnData[0]);
        fDefinition = definition;
        createColumnHeaders();
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

    @Override
    public TmfEventField[] extractItemFields(TmfEvent event) {
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
