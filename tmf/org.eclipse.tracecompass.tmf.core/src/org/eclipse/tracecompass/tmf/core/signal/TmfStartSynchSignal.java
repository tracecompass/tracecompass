/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

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
