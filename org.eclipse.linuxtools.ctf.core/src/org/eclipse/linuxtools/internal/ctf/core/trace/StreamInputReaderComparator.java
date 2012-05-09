/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.trace;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;


/**
 * <b><u>StreamInputReaderComparator</u></b>
 * <p>
 * TODO Implement me. Please.
 */
public class StreamInputReaderComparator implements
        Comparator<StreamInputReader>, Serializable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long serialVersionUID = 7477206132343139753L;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public int compare(StreamInputReader a, StreamInputReader b) {
        // TODO: use unsigned comparison to avoid sign errors if needed
        long ta = a.getCurrentEvent().getTimestamp();
        long tb = b.getCurrentEvent().getTimestamp();

        if (ta < tb) {
            return -1;
        } else if (ta > tb) {
            return 1;
        } else {
            return 0;
        }
    }

}
