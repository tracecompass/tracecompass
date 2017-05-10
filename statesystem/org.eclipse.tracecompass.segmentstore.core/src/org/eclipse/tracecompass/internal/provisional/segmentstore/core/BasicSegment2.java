/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.segmentstore.core;

import org.eclipse.tracecompass.datastore.core.interval.HTInterval;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;

/**
 * Extension of the BasicSegment, to implement ISegment2 so that it can be
 * stored in a history tree backend.
 *
 * @since 1.2
 */
public class BasicSegment2 extends HTInterval implements ISegment2 {

    /**
     * The factory to read an object from a buffer
     */
    public static final IHTIntervalReader<BasicSegment2> BASIC_SEGMENT_READ_FACTORY = buffer -> {
            return new BasicSegment2(buffer.getLong(), buffer.getLong());
    };

    /**
     * Constructor
     *
     * @param start
     *            start of the segment
     * @param end
     *            end of the segment
     */
    public BasicSegment2(long start, long end) {
        super(start, end);
    }

    /**
     *
     */
    private static final long serialVersionUID = -3083730702354275357L;

}
