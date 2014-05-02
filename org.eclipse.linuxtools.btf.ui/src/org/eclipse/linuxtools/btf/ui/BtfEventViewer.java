/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Updated to new Event Table API
 *******************************************************************************/

package org.eclipse.linuxtools.btf.ui;

import org.eclipse.linuxtools.btf.core.event.BtfEvent;
import org.eclipse.linuxtools.btf.core.trace.BtfColumnNames;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * BTF event viewer
 *
 * @author Matthew Khouzam
 */
public class BtfEventViewer extends TmfEventsTable {

    private static final TmfEventField NULL_NOTE_FIELD =
            new TmfEventField(BtfColumnNames.NOTES.toString(), null, null);

    private static final String[] COLUMN_NAMES = new String[] {
            BtfColumnNames.TIMESTAMP.toString(),
            BtfColumnNames.SOURCE.toString(),
            BtfColumnNames.SOURCE_INSTANCE.toString(),
            BtfColumnNames.EVENT_TYPE.toString(),
            BtfColumnNames.TARGET.toString(),
            BtfColumnNames.TARGET_INSTANCE.toString(),
            BtfColumnNames.EVENT.toString(),
            BtfColumnNames.NOTES.toString()
    };

    private static final ColumnData[] COLUMN_DATA = new ColumnData[] {
            new ColumnData(COLUMN_NAMES[0], 150, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[1], 120, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[2], 100, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[3], 120, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[4], 90, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[5], 100, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[6], 110, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[7], 100, SWT.LEFT),
    };

    /**
     * Basic constructor, will use default column data.
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     */
    public BtfEventViewer(Composite parent, int cacheSize) {
        super(parent, cacheSize, COLUMN_DATA);
        fTable.getColumns()[0].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_TIMESTAMP);
        fTable.getColumns()[1].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_SOURCE);
        fTable.getColumns()[3].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_TYPE);
        fTable.getColumns()[4].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_REFERENCE);
    }

    @Override
    public String[] getItemStrings(ITmfEvent event) {
        if (!(event instanceof BtfEvent)) {
            return EMPTY_STRING_ARRAY;
        }
        final BtfEvent btfEvent = (BtfEvent) event;
        final ITmfEventField content = btfEvent.getContent();
        final ITmfEventField notesField = content.getField(BtfColumnNames.NOTES.toString());

        return new String[] {
                event.getTimestamp().toString(),
                event.getSource(),
                content.getField(BtfColumnNames.SOURCE_INSTANCE.toString()).toString(),
                btfEvent.getType().getName(),
                event.getReference(),
                content.getField(BtfColumnNames.TARGET_INSTANCE.toString()).toString(),
                content.getField(BtfColumnNames.EVENT.toString()).toString(),
                ((notesField != null) ? notesField : NULL_NOTE_FIELD).toString()
        };
    }
}
