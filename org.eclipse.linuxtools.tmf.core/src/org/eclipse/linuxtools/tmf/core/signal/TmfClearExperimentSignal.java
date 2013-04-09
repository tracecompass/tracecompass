/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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
 * Clear experiment signal
 *
 * @version 1.0
 * @author Francois Chouinard
 * @since 2.0
 */
public class TmfClearExperimentSignal extends TmfSignal {

    /**
     * @param source the signal source
     */
    public TmfClearExperimentSignal(Object source) {
        super(source);
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfClearExperimentSignal]";
    }
}
