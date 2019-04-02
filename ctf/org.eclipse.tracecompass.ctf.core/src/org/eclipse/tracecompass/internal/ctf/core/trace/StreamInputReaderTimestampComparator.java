/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.tracecompass.internal.ctf.core.utils.Utils;

/**
 * <b><u>StreamInputReaderTimestampComparator</u></b>
 * <p>
 * Compares two StreamInputReader by their timestamp (smaller comes before).
 */
public class StreamInputReaderTimestampComparator implements
        Comparator<CTFStreamInputReader>, Serializable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 1066434959451875045L;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @throws NullPointerException
     *             If any {@link CTFStreamInputReader} parameter is null, of if
     *             any of them does not contain a current event.
     */
    @Override
    public int compare(CTFStreamInputReader a, CTFStreamInputReader b) {
        IEventDefinition eventA = checkNotNull(a.getCurrentEvent());
        IEventDefinition eventB = checkNotNull(b.getCurrentEvent());

        long ta = eventA.getTimestamp();
        long tb = eventB.getTimestamp();
        return Utils.unsignedCompare(ta, tb);
    }

}
