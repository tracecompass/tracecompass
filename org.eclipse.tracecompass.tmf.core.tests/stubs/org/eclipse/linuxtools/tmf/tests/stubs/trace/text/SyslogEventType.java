/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace.text;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

/**
 * The system log extension of the TMF event type.
 */
public class SyslogEventType extends TmfEventType {

    /** The event type id string. */
    public static final String TYPE_ID = "Syslog"; //$NON-NLS-1$
    /** The labels (field names) used for SA system log events. */
    @SuppressWarnings("nls")
    public static final String[] LABELS = {"Timestamp", "Host", "Logger", "Message"};
    /** A default instance of this class */
    public static final SyslogEventType INSTANCE = new SyslogEventType();

    /** Index numbers in the array of event field names and values. */
    public interface Index {
        /** Index for time stamp */
        int TIMESTAMP = 0;
        /** Index for the host name */
        int HOST      = 1;
        /** Index for the logger name */
        int LOGGER    = 2;
        /** Index for the event message */
        int MESSAGE   = 3;
    }

    /**
     * Default Constructor
     */
    public SyslogEventType() {
        super(ITmfEventType.DEFAULT_CONTEXT_ID, TYPE_ID, TmfEventField.makeRoot(LABELS));
    }

}
