/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;

/**
 * Tagging aspect to tell views that this is a category to group by
 *
 * @author Matthew Khouzam
 * @since 5.0
 *
 */
public interface IGroupingSegmentAspect extends ISegmentAspect {

}
