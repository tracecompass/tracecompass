/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.synchronization;

import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;

/**
 * Interface for timestamp transform who also provide an inverse transform.
 *
 * @author Geneviève Bastien
 */
public interface ITmfTimestampTransformInvertible extends ITmfTimestampTransform {

    /**
     * Returns the inverse of this transform. The transform composed with its
     * inverse yields the identity (or as close to it as mathematical
     * approximations in the formulae allow).
     *
     * @return The inverse transform
     */
    ITmfTimestampTransform inverse();

}
