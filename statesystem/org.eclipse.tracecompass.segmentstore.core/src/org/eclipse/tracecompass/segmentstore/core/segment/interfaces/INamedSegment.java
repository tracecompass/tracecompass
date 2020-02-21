/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 * @since 2.0
 */
public interface INamedSegment extends ISegment {

    /**
     * Get the name of this segment
     *
     * @return Name
     */
    String getName();

}
