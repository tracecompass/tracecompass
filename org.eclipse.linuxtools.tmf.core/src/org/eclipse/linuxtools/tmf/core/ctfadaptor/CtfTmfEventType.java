/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEventTypeManager;

/**
 * The CTF extension of the TMF event type
 *
 * @version 1.0
 * @author Matthew khouzam
 */
public class CtfTmfEventType extends TmfEventType {

    private static final String CONTEXT_ID = "Ctf Event"; //$NON-NLS-1$

    /**
     * Constructor for CtfTmfEventType.
     *
     * @param eventName
     *            String
     * @param content
     *            ITmfEventField
     */
    public CtfTmfEventType(String eventName, ITmfEventField content) {
        super(CONTEXT_ID, eventName, content);
    }

    /**
     * Method toString.
     *
     * @return String
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * gets the event type for an event name
     *
     * @param eventName
     *            the event name
     * @return the event type
     */
    public static CtfTmfEventType get(String eventName){
        return (CtfTmfEventType) TmfEventTypeManager.getInstance().getType(CONTEXT_ID, eventName);
    }
}
