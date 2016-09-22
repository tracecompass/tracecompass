/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.segment.interfaces;

import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Interface to be implemented by segments that have a name to provide. This
 * name can be used in analyses and outputs to identify segments.
 *
 * This interface is a qualifier interface for segments. A concrete segment type
 * can implement many such qualifier interfaces.
 *
 * @since 1.2
 */
public interface INamedSegment extends ISegment {

    /**
     * Get the name of this segment
     *
     * @return Name
     */
    String getName();

}
