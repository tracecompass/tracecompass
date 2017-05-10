/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.segmentstore.core;

import org.eclipse.tracecompass.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * This is a segment that can be saved in a history tree backend
 *
 * Note: It should be ISegment, but that would be API breaking, so this
 * interface was added so a segment can be both segment and history tree object.
 *
 * FIXME: Move that to ISegment when breaking the API
 *
 * @author Geneviève Bastien
 * @since 1.2
 */
public interface ISegment2 extends ISegment, IHTInterval {

}
