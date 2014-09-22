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
 *******************************************************************************/

package org.eclipse.linuxtools.btf.core.event;

import java.util.Collection;
import java.util.List;

import org.eclipse.linuxtools.btf.core.Messages;
import org.eclipse.linuxtools.btf.core.trace.BtfColumnNames;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

import com.google.common.collect.ImmutableList;

/**
 * Btf event type, can get a description from the spec
 *
 * @author Matthew Khouzam
 */
public class BtfEventType extends TmfEventType {

    private static final String[] FIELD_WITH_NOTES_COLUMNS = new String[] {
            BtfColumnNames.EVENT.toString(),
            BtfColumnNames.SOURCE_INSTANCE.toString(),
            BtfColumnNames.TARGET_INSTANCE.toString() };

    private static final String[] FIELDS_WITHOUT_NOTES_COLUMNS = new String[] {
            BtfColumnNames.EVENT.toString(),
            BtfColumnNames.SOURCE_INSTANCE.toString(),
            BtfColumnNames.TARGET_INSTANCE.toString(),
            BtfColumnNames.NOTES.toString() };
    private static final ITmfEventField FIELDS_WITHOUT_NOTES = TmfEventField.makeRoot(FIELD_WITH_NOTES_COLUMNS);
    private static final ITmfEventField FIELDS_WITH_NOTES = TmfEventField.makeRoot(FIELDS_WITHOUT_NOTES_COLUMNS);
    private final String fName;
    private final String fDescription;
    private final boolean fHasNotes;
    private final List<String> fCols;
    private final ITmfEventField fFields;

    /**
     * The type constructor
     * @param name the event name
     * @param description the event description
     */
    public BtfEventType(String name, String description) {
        super();
        fName = name;
        fDescription = description;
        fHasNotes = (fName.equals(Messages.BtfTypeId_SIGName) || fName.equals(Messages.BtfTypeId_SEMName));
        fCols = ImmutableList.copyOf(fHasNotes ? FIELDS_WITHOUT_NOTES_COLUMNS : FIELD_WITH_NOTES_COLUMNS);
        fFields = (fHasNotes ? FIELDS_WITH_NOTES : FIELDS_WITHOUT_NOTES);
    }

    /**
     * does the event have an eighth column
     *
     * @return if the name is "sem" or "sig" true
     */
    public boolean hasNotes() {
        return fHasNotes;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return fName;
    }

    @Override
    public Collection<String> getFieldNames() {
        return fCols;
    }

    @Override
    public ITmfEventField getRootField() {
        return fFields;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return fDescription;
    }

    /**
     * Gets the event field values
     *
     * @param event
     *            the "event" payload
     * @param sourceInstance
     *            source instance
     * @param targetInstance
     *            target instance
     * @return a field.
     */
    public ITmfEventField generateContent(String event, long sourceInstance, long targetInstance) {
        String[] data;
        TmfEventField retField;
        TmfEventField sourceInstanceField = new TmfEventField(fCols.get(1), sourceInstance, null);
        TmfEventField targetInstanceField = new TmfEventField(fCols.get(2), sourceInstance, null);
        if (fHasNotes) {
            data = event.split(",", 2); //$NON-NLS-1$
            TmfEventField eventField = new TmfEventField(fCols.get(0), data[0], BTFPayload.getFieldDescription(data[0]));
            TmfEventField notesField = new TmfEventField(fCols.get(3), data[1], null);
            retField = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, new TmfEventField[] { eventField, sourceInstanceField, targetInstanceField, notesField });
        } else {
            data = new String[] { event };
            TmfEventField eventField = new TmfEventField(fCols.get(0), data[0], BTFPayload.getFieldDescription(data[0]));
            retField = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, new TmfEventField[] { eventField, sourceInstanceField, targetInstanceField });
        }
        return retField;
    }
}
