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
 * Base class for TMF signals
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfSignal {

    private final Object fSource;
    private int fReference;

    /**
     * Basic constructor, which uses a default of "0" for the reference index
     *
     * @param source
     *            Object sending this signal
     */
    public TmfSignal(Object source) {
        this(source, 0);
    }

    /**
     * Standard constructor
     *
     * @param source
     *            Object sending this signal
     * @param reference
     *            Reference index to assign to this signal
     */
    public TmfSignal(Object source, int reference) {
        fSource = source;
        fReference = reference;
    }

    /**
     * @return The source object of this signal
     */
    public Object getSource() {
        return fSource;
    }

    /**
     * Change this signal's reference index
     *
     * @param reference
     *            The new reference to use
     */
    public void setReference(int reference) {
        fReference = reference;
    }

    /**
     * @return This signal's reference index
     */
    public int getReference() {
        return fReference;
    }

}
