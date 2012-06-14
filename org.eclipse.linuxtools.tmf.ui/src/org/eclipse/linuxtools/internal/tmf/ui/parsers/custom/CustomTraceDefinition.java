/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.text.SimpleDateFormat;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;


public abstract class CustomTraceDefinition {

    public static final int ACTION_SET = 0;
    public static final int ACTION_APPEND = 1;
    public static final int ACTION_APPEND_WITH_SEPARATOR = 2;

    public static final String TAG_TIMESTAMP = Messages.CustomTraceDefinition_timestampTag;
    public static final String TAG_MESSAGE = Messages.CustomTraceDefinition_messageTag;
    public static final String TAG_OTHER = Messages.CustomTraceDefinition_otherTag;

    public String definitionName;
    public List<OutputColumn> outputs;
    public String timeStampOutputFormat;

    public static class OutputColumn {
        public String name;

        public OutputColumn() {}

        public OutputColumn(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public String formatTimeStamp(TmfTimestamp timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeStampOutputFormat);
        return simpleDateFormat.format(timestamp.getValue());
    }

    public abstract void save();
    public abstract void save(String path);
}
