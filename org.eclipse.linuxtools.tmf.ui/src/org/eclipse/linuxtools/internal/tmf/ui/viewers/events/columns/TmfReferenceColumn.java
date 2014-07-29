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
 ******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.viewers.events.columns;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;

/**
 * Column for the event reference
 *
 * TODO Remove me, replace with trace-type-specific columns
 */
public final class TmfReferenceColumn extends TmfEventTableColumn {

    @SuppressWarnings("null")
    private static final @NonNull String HEADER = Messages.TmfEventsTable_ReferenceColumnHeader;

    /**
     * Constructor
     */
    public TmfReferenceColumn() {
        super(HEADER);
    }

    @Override
    public String getItemString(ITmfEvent event) {
        String ref = event.getReference();
        return (ref == null ? EMPTY_STRING : ref);
    }

    @Override
    public String getFilterFieldId() {
        return ITmfEvent.EVENT_FIELD_REFERENCE;
    }
}