/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.tmf.event.*;

/**
 * <b><u>LttngEventSource</u></b>
 * <p>
 * Lttng specific implementation of the TmfEventSource
 * <p>
 * The Lttng implementation is the same as the basic Tmf Implementation but allow construction with a String as paramter
 */
public class LttngEventSource extends TmfEventSource {
    /**
     * Constructor with parameters
     * 
     * @param newSource    Name as string of the source.
     */
    public LttngEventSource(String newSource) {
        super(newSource);
    }
}
