/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;

/**
 * {@link TimeGraphEntryModel} for XML Time Graphs
 *
 * @author Loic Prieur-Drevon
 * @since 3.0
 */
public class DataDrivenTimeGraphEntryModel extends TimeGraphEntryModel {

    /**
     * Builder for the {@link DataDrivenTimeGraphEntryModel}, encapsulates logic and fields
     * necessary to build the {@link DataDrivenTimeGraphEntryModel}, but that we do not
     * want to share with the client
     *
     * @author Loic Prieur-Drevon
     */
    protected static class EntryBuilder implements ITimeGraphEntryModel {

        private final long fId;
        private long fParentId;
        private String fName = StringUtils.EMPTY;
        private final long fStart;
        private final long fEnd;
        private String fXmlId = StringUtils.EMPTY;
        private String fXmlParentId = StringUtils.EMPTY;
        private final boolean fDisplayLabel;
        private final int fDisplayQuark;

        /**
         * Constructor
         *
         * @param id
         *            The unique ID of this entry
         * @param parentEntryId
         *            The parent entry ID
         * @param displayQuark
         *            The quark containing the data to display in the row
         * @param name
         *            The name of this entry
         * @param xmlId
         *            The ID of this entry in the XML analysis
         * @param xmlParentId
         *            The ID of the parent entry in the XML analysis. Does not
         *            necessarily match the ID of the parent entry
         * @param entryStart
         *            The timestamp of the entry start
         * @param entryEnd
         *            The timestamp of the entry end
         * @param displayText
         *            Whether to display some text as label
         */
        public EntryBuilder(long id, long parentEntryId, int displayQuark, String name, String xmlId, String xmlParentId, long entryStart, long entryEnd, boolean displayText) {
            fId = id;
            fName = name;
            fXmlId = xmlId;
            fXmlParentId = xmlParentId;
            fStart = entryStart;
            fEnd = entryEnd;
            fParentId = parentEntryId;
            fDisplayLabel = displayText;
            fDisplayQuark = displayQuark;
        }

        @Override
        public long getId() {
            return fId;
        }

        @Override
        public long getParentId() {
            return fParentId;
        }

        @Override
        public @NonNull String getName() {
            return fName;
        }

        @Override
        public long getStartTime() {
            return fStart;
        }

        @Override
        public long getEndTime() {
            return fEnd;
        }

        /**
         * Getter for this {@link DataDrivenTimeGraphEntryModel}'s XML ID
         *
         * @return this entry's XML ID.
         */
        public String getXmlId() {
            return fXmlId;
        }

        /**
         * Getter for this Entry's XML parent
         *
         * @return this entry's XML parent ID.
         */
        public String getXmlParentId() {
            return fXmlParentId;
        }

        /**
         * Setter for this Entry's XML parent
         *
         * @param newParentId
         *            new parent XML ID
         */
        public void setParentId(long newParentId) {
            fParentId = newParentId;
        }

        /**
         * Generate an {@link DataDrivenTimeGraphEntryModel} from the builder.
         *
         * @return a new {@link DataDrivenTimeGraphEntryModel} instance.
         */
        public DataDrivenTimeGraphEntryModel build() {
            return new DataDrivenTimeGraphEntryModel(fId, fParentId, fDisplayQuark, fName, fStart, fEnd, fXmlId, fXmlParentId, fDisplayLabel);
        }

        @Override
        public String toString() {
            return fName + " - " + fXmlId + " - " + fXmlParentId + " - " + fId + " - " + fParentId; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

    }

    private final String fXmlId;
    private final String fXmlParentId;
    private final boolean fShowText;
    private final int fDisplayQuark;

    /**
     * @param id
     *            unique entry model id
     * @param parentId
     *            parent's unique entry model id
     * @param displayQuark The quark to display
     * @param name
     *            default entry name
     * @param startTime
     *            entry start time
     * @param endTime
     *            entry end time
     * @param xmlId
     *            XML ID
     * @param xmlParentId
     *            XML parent ID
     * @param showText
     *            if the text should be shown for this entry or not.
     */
    public DataDrivenTimeGraphEntryModel(long id, long parentId, int displayQuark, String name, long startTime, long endTime,
            String xmlId, String xmlParentId, boolean showText) {
        super(id, parentId, name, startTime, endTime);
        fXmlId = xmlId;
        fXmlParentId = xmlParentId;
        fShowText = showText;
        fDisplayQuark = displayQuark;
    }


    /**
     * Getter for this {@link DataDrivenTimeGraphEntryModel}'s XML ID
     *
     * @return this entry's XML ID.
     */
    public String getXmlId() {
        return fXmlId;
    }

    /**
     * Getter for this Entry's XML parent
     *
     * @return this entry's XML parent ID.
     */
    public String getXmlParentId() {
        return fXmlParentId;
    }

    /**
     * If the labels should be displayed on the states
     *
     * @return to display or not to display the text on states for this entry
     */
    public boolean showText() {
        return fShowText;
    }

    /**
     * Get the display quark for this entry
     *
     * @return The display quark
     */
    public int getDisplayQuark() {
        return fDisplayQuark;
    }

}
