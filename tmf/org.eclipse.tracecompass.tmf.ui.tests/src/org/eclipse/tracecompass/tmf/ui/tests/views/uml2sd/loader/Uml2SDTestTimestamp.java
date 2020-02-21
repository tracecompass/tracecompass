/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
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
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.tests.views.uml2sd.loader;

import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * Timestamp implementation for UML2SD test cases.
 *
 * @author Bernd Hufmann
 *
 */
public class Uml2SDTestTimestamp extends TmfTimestamp {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final long fValue;
    private final int fScale;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param value time as long value (nanoseconds)
     */
    public Uml2SDTestTimestamp(long value) {
        fValue = value;
        fScale = IUml2SDTestConstants.TIME_SCALE;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public long getValue() {
        return fValue;
    }

    @Override
    public int getScale() {
        return fScale;
    }

}
