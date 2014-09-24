/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

/**
 * Start of signal synchronization
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfStartSynchSignal extends TmfSignal {


    /**
     * Constructor
     *
     * @param synchId
     *            The synch ID of this signal
     */
    public TmfStartSynchSignal(int synchId) {
        super(null, synchId);
    }

}
