/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.experiment.type;

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Event table stub for experiment type unit tests
 *
 * @author Geneviève Bastien
 */
public class TmfEventsTableExperimentStub extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    // Table column names
    private static final String[] COLUMN_NAMES = new String[] {
            Messages.TmfEventsTable_TimestampColumnHeader,
            Messages.TmfEventsTable_SourceColumnHeader,
            Messages.TmfEventsTable_TypeColumnHeader,
            Messages.TmfEventsTable_ReferenceColumnHeader,
            "Trace",
            Messages.TmfEventsTable_ContentColumnHeader
    };

    private static final ColumnData[] COLUMN_DATA = new ColumnData[] {
            new ColumnData(COLUMN_NAMES[0], 100, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[1], 100, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[2], 100, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[3], 100, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[4], 100, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[5], 100, SWT.LEFT)
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
    public TmfEventsTableExperimentStub(Composite parent, int cacheSize) {
        super(parent, cacheSize, COLUMN_DATA);
    }

    @Override
    protected ITmfEventField[] extractItemFields(ITmfEvent event) {
        ITmfEventField[] fields = new TmfEventField[0];
        if (event != null) {
            final String timestamp = event.getTimestamp().toString();
            final String source = event.getSource();
            final String type = event.getType().getName();
            final String reference = event.getReference();
            final String content = event.getContent().toString();
            fields = new TmfEventField[] {
                    new TmfEventField(ITmfEvent.EVENT_FIELD_TIMESTAMP, timestamp, null),
                    new TmfEventField(ITmfEvent.EVENT_FIELD_SOURCE, source, null),
                    new TmfEventField(ITmfEvent.EVENT_FIELD_TYPE, type, null),
                    new TmfEventField(ITmfEvent.EVENT_FIELD_REFERENCE, reference, null),
                    new TmfEventField("Trace", event.getTrace().getName(), null),
                    new TmfEventField(ITmfEvent.EVENT_FIELD_CONTENT, content, null)
            };
        }
        return fields;
    }

}
