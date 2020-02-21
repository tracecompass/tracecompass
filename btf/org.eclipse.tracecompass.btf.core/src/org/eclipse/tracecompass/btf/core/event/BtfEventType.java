/*******************************************************************************
 * Copyright (c) 2014, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Fix target instance field
 *******************************************************************************/

package org.eclipse.tracecompass.btf.core.event;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.btf.core.trace.BtfColumnNames;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

import com.google.common.collect.ImmutableList;

/**
 * Btf event type, can get a description from the spec
 *
 * @author Matthew Khouzam
 */
public class BtfEventType extends TmfEventType {

    private static final String @NonNull [] FIELDS_WITH_NOTES_COLUMNS = new String[] {
            BtfColumnNames.EVENT.toString(),
            BtfColumnNames.SOURCE_INSTANCE.toString(),
            BtfColumnNames.TARGET_INSTANCE.toString(),
            BtfColumnNames.NOTES.toString() };
    private static final @NonNull ITmfEventField FIELDS_WITH_NOTES = TmfEventField.makeRoot(FIELDS_WITH_NOTES_COLUMNS);
    private final @NonNull String fName;
    private final String fDescription;
    private final List<String> fCols;
    private final ITmfEventField fFields;

    /**
     * The type constructor
     * @param name the event name
     * @param description the event description
     */
    public BtfEventType(@NonNull String name, String description) {
        super();
        fName = name;
        fDescription = description;
        fCols = ImmutableList.copyOf(FIELDS_WITH_NOTES_COLUMNS);
        fFields = FIELDS_WITH_NOTES;
    }

    /**
     * does the event have an eighth column
     *
     * @return if the name is "sem" or "sig" true
     * @deprecated - notes column is always there, while the content is optional
     */
    @Deprecated
    public boolean hasNotes() {
        return true;
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
        return generateContent(event, sourceInstance, targetInstance, null);
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
     * @param notes
     *            optional notes (use null if no notes)
     * @return a field.
     * @since 2.2
     */
    public ITmfEventField generateContent(String event, long sourceInstance, long targetInstance, @Nullable String notes) {
        String notesString = notes == null ? "" : notes; //$NON-NLS-1$
        TmfEventField retField;
        TmfEventField sourceInstanceField = new TmfEventField(BtfColumnNames.SOURCE_INSTANCE.toString(), sourceInstance, null);
        TmfEventField targetInstanceField = new TmfEventField(BtfColumnNames.TARGET_INSTANCE.toString(), targetInstance, null);
        TmfEventField eventField = new TmfEventField(BtfColumnNames.EVENT.toString(), event, BTFPayload.getFieldDescription(event));
        TmfEventField notesField = new TmfEventField(BtfColumnNames.NOTES.toString(), notesString, null);
        retField = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, new TmfEventField[] { eventField, sourceInstanceField, targetInstanceField, notesField });
        return retField;
    }
}
