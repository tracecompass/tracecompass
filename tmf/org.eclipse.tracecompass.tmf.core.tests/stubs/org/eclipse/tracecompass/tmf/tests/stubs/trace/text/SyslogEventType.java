/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Patrick Tasse - Move field declarations to trace
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace.text;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.TmfEventType;

/**
 * The system log extension of the TMF event type.
 */
public class SyslogEventType extends TmfEventType {

    /** The event type id string. */
    public static final @NonNull String TYPE_ID = "Syslog"; //$NON-NLS-1$

    /** A default instance of this class */
    public static final SyslogEventType INSTANCE = new SyslogEventType();

    /**
     * Default Constructor
     */
    public SyslogEventType() {
        super(TYPE_ID, null);
    }

}
