/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Non-externalized strings for use with the CTF plugin (event names, field
 * names, etc.)
 *
 * @author Alexandre Montplaisir
 * @since 2.2
 */
@SuppressWarnings("nls")
@NonNullByDefault
public interface CTFStrings {

    /** Event name for lost events */
    String LOST_EVENT_NAME = "Lost event";

    /**
     * Name of the field in lost events indicating how many actual events were
     * lost
     */
    String LOST_EVENTS_FIELD = "Lost events";

    /**
     * Name of the field in lost events indicating the time range
     */
    String LOST_EVENTS_DURATION = "duration";
}
