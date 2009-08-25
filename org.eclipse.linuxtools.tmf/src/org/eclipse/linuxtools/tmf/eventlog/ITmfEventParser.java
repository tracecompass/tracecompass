/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.eventlog;

import java.io.IOException;

import org.eclipse.linuxtools.tmf.event.TmfEvent;

/**
 * <b><u>ITmfEventParser</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public interface ITmfEventParser {

    /**
     * @return
     * @throws IOException 
     */
    public TmfEvent getNextEvent(TmfEventStream stream) throws IOException;
}
