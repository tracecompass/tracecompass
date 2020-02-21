/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenXYDataProvider.DisplayType;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;

/**
 * {@link TimeGraphEntryModel} for XML Time Graphs
 *
 * @author Loic Prieur-Drevon
 * @since 3.0
 */
public class DataDrivenOutputEntryModel extends TimeGraphEntryModel {

    /**
     * Builder for the {@link DataDrivenOutputEntryModel}, encapsulates logic and fields
     * necessary to build the {@link DataDrivenOutputEntryModel}, but that we do not
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
        private final DisplayType fDisplayType;

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
         * @param displayType
         *            The type of display for this entry
         */
        public EntryBuilder(long id, long parentEntryId, int displayQuark, String name, String xmlId, String xmlParentId, long entryStart, long entryEnd, boolean displayText, DisplayType displayType) {
            fId = id;
            fName = name;
            fXmlId = xmlId;
            fXmlParentId = xmlParentId;
            fStart = entryStart;
            fEnd = entryEnd;
            fParentId = parentEntryId;
            fDisplayLabel = displayText;
            fDisplayQuark = displayQuark;
            fDisplayType = displayType;
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
         * Getter for this {@link DataDrivenOutputEntryModel}'s XML ID
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
         * Generate an {@link DataDrivenOutputEntryModel} from the builder.
         *
         * @return a new {@link DataDrivenOutputEntryModel} instance.
         */
        public DataDrivenOutputEntryModel build() {
            return new DataDrivenOutputEntryModel(fId, fParentId, fDisplayQuark, fName, fStart, fEnd, fXmlId, fXmlParentId, fDisplayLabel, fDisplayType);
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
    private final DisplayType fDisplayType;

    /**
     * @param id
     *            unique entry model id
     * @param parentId
     *            parent's unique entry model id
     * @param displayQuark
     *            The quark to display
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
     * @param displayType
     *            The type of display for this entry
     */
    public DataDrivenOutputEntryModel(long id, long parentId, int displayQuark, String name, long startTime, long endTime,
            String xmlId, String xmlParentId, boolean showText, DisplayType displayType) {
        super(id, parentId, name, startTime, endTime);
        fXmlId = xmlId;
        fXmlParentId = xmlParentId;
        fShowText = showText;
        fDisplayQuark = displayQuark;
        fDisplayType = displayType;
    }


    /**
     * Getter for this {@link DataDrivenOutputEntryModel}'s XML ID
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

    /**
     * Get the type of display computation to do for this entry
     *
     * @return The display type
     */
    public DisplayType getDisplayType() {
        return fDisplayType;
    }

}
