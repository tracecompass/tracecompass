/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.parsers.custom;

import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 * Base class for custom trace definitions.
 *
 * @author Patrick Tassé
 * @since 3.0
 */
public abstract class CustomTraceDefinition {

    /** "set" action */
    public static final int ACTION_SET = 0;

    /** "append" action */
    public static final int ACTION_APPEND = 1;

    /** "append with separator" action */
    public static final int ACTION_APPEND_WITH_SEPARATOR = 2;

    /** Timestamp tag */
    public static final String TAG_TIMESTAMP = Messages.CustomTraceDefinition_timestampTag;

    /** Message tag */
    public static final String TAG_MESSAGE = Messages.CustomTraceDefinition_messageTag;

    /** "Other" tag */
    public static final String TAG_OTHER = Messages.CustomTraceDefinition_otherTag;

    /** Name of this trace definition */
    public String definitionName;

    /** List of output columns */
    public List<OutputColumn> outputs;

    /** Timestamp format */
    public String timeStampOutputFormat;

    /**
     * Definition of an output column
     */
    public static class OutputColumn {

        /** Name of this column */
        public String name;

        /**
         * Default constructor (empty)
         */
        public OutputColumn() {}

        /**
         * Constructor
         *
         * @param name Name of this output column
         */
        public OutputColumn(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Format a timestamp in this trace's current time stamp format.
     *
     * @param timestamp
     *            The timestamp to format
     * @return The same timestamp as a formatted string
     */
    public String formatTimeStamp(TmfTimestamp timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeStampOutputFormat);
        return simpleDateFormat.format(timestamp.getValue());
    }

    /**
     * Save this custom trace in the default path.
     */
    public abstract void save();

    /**
     * Save this custom trace in the supplied path.
     *
     * @param path
     *            The path to save to
     */
    public abstract void save(String path);
}
