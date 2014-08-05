/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.ui.editor;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.pcap.core.event.PcapEvent;
import org.eclipse.linuxtools.tmf.pcap.core.protocol.TmfProtocol;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Default event table for pcap traces.
 *
 * @author Vincent Perot
 */
public class PcapEventsTable extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Table data
    // ------------------------------------------------------------------------

    // Table column names
    private static final String[] COLUMN_NAMES = new String[] {
            Messages.PcapEventsTable_Timestamp,
            Messages.PcapEventsTable_Source,
            Messages.PcapEventsTable_Destination,
            Messages.PcapEventsTable_Reference,
            Messages.PcapEventsTable_Protocol,
            Messages.PcapEventsTable_Content
    };

    private static final ColumnData[] COLUMN_DATA = new ColumnData[] {
            new ColumnData(COLUMN_NAMES[0], 150, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[1], 120, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[2], 200, SWT.LEFT),
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
    public PcapEventsTable(Composite parent, int cacheSize) {
        super(parent, cacheSize, COLUMN_DATA);
        fTable.getColumns()[0].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_TIMESTAMP);
        fTable.getColumns()[1].setData(Key.FIELD_ID, PcapEvent.EVENT_FIELD_PACKET_SOURCE);
        fTable.getColumns()[2].setData(Key.FIELD_ID, PcapEvent.EVENT_FIELD_PACKET_DESTINATION);
        fTable.getColumns()[3].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_REFERENCE);
        fTable.getColumns()[4].setData(Key.FIELD_ID, PcapEvent.EVENT_FIELD_PACKET_PROTOCOL);
        fTable.getColumns()[5].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_CONTENT);
    }

    @Override
    public String[] getItemStrings(@Nullable ITmfEvent event) {

        if (event == null || !(event instanceof PcapEvent)) {
            return EMPTY_STRING_ARRAY;
        }

        PcapEvent pcapEvent = (PcapEvent) event;
        TmfProtocol protocol = pcapEvent.getMostEncapsulatedProtocol();

        return new String[] {
                pcapEvent.getTimestamp().toString(),
                pcapEvent.getSourceEndpoint(protocol),
                pcapEvent.getDestinationEndpoint(protocol),
                pcapEvent.getReference(),
                protocol.getShortName().toUpperCase(),
                pcapEvent.getContent().toString()
        };
    }
}
