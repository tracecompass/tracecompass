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

/**
 */
public class CtfTmfEventType extends TmfEventType {

    /**
     * Constructor for CtfTmfEventType.
     * @param contextId String
     * @param eventName String
     * @param content ITmfEventField
     */
    public CtfTmfEventType(String contextId, String eventName,
            ITmfEventField content) {
        super(contextId, eventName, content);
    }

    /**
     * Method toString.
     * @return String
     */
    @Override
    public String toString()
    {
        return this.getName();
    }
}
