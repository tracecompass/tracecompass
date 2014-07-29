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

import java.util.Collection;

import org.eclipse.linuxtools.btf.core.trace.BtfColumnNames;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableFieldColumn;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableList;

/**
 * BTF event viewer
 *
 * @author Matthew Khouzam
 */
public class BtfEventViewer extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Column definition
    // ------------------------------------------------------------------------

    private static final Collection<TmfEventTableColumn> BTF_COLUMNS = ImmutableList.of(
            TmfEventTableColumn.BaseColumns.TIMESTAMP,
            new BtfSourceColumn(),
            new BtfSourceInstanceColumn(),
            TmfEventTableColumn.BaseColumns.EVENT_TYPE,
            new BtfTargetColumn(),
            new BtfTargetInstanceColumn(),
            new BtfEventColumn(),
            new BtfNotesColumn()
            );

    /**
     * The "source" column, whose value comes from {@link ITmfEvent#getSource()}
     */
    private static class BtfSourceColumn extends TmfEventTableColumn {

        public BtfSourceColumn() {
            super(BtfColumnNames.SOURCE.toString(), null);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            String ret = event.getSource();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterFieldId() {
            return ITmfEvent.EVENT_FIELD_SOURCE;
        }
    }

    /**
     * The "source instance" column, whose value comes from the field of the
     * same name.
     */
    private static class BtfSourceInstanceColumn extends TmfEventTableFieldColumn {
        public BtfSourceInstanceColumn() {
            super(BtfColumnNames.SOURCE_INSTANCE.toString());
        }
    }

    /**
     * The "target" column, taking its value from
     * {@link ITmfEvent#getReference()}.
     */
    private static class BtfTargetColumn extends TmfEventTableColumn {

        public BtfTargetColumn() {
            super(BtfColumnNames.TARGET.toString());
        }

        @Override
        public String getItemString(ITmfEvent event) {
            String ret = event.getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }

        @Override
        public String getFilterFieldId() {
            return ITmfEvent.EVENT_FIELD_REFERENCE;
        }
    }

    /**
     * The "target instance" column, whose value comes from the field of the
     * same name.
     */
    private static class BtfTargetInstanceColumn extends TmfEventTableFieldColumn {
        public BtfTargetInstanceColumn() {
            super(BtfColumnNames.TARGET_INSTANCE.toString());
        }
    }

    /**
     * The "event" column, whose value comes from the field of the same name.
     */
    private static class BtfEventColumn extends TmfEventTableFieldColumn {
        public BtfEventColumn() {
            super(BtfColumnNames.EVENT.toString());
        }
    }

    /**
     * The "notes" column, whose value comes from the field of the same name, if
     * present.
     */
    private static class BtfNotesColumn extends TmfEventTableFieldColumn {
        public BtfNotesColumn() {
            super(BtfColumnNames.NOTES.toString());
        }
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Basic constructor, will use default column data.
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     */
    public BtfEventViewer(Composite parent, int cacheSize) {
        super(parent, cacheSize, BTF_COLUMNS);
    }
}