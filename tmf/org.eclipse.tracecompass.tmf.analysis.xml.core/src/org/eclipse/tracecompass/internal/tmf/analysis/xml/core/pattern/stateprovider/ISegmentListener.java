/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * Listener for segment creation.
 * <p>
 * Segment store analyses could build their store based on segments created by
 * another analysis. In this case, the segment store analysis should implement
 * this listener in order to receive the segments as they are created.
 *
 * @author Jean-Christian Kouame
 *
 */
public interface ISegmentListener {

    /**
     * Called each time a segment will be created
     *
     * @param segment
     *            The new segment
     */
    void onNewSegment(@NonNull ISegment segment);
}
