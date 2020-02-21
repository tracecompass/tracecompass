/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * A default simple, identity timestamp transform. It is a singleton class and
 * returns the timestamp itself
 *
 * @author Geneviève Bastien
 */
public final class TmfTimestampTransform implements ITmfTimestampTransformInvertible {

    /**
     * Generated serial UID
     */
    private static final long serialVersionUID = -1480581417493073304L;

    /**
     * The unique instance of this transform, since it is always the same
     */
    public static final @NonNull TmfTimestampTransform IDENTITY = new TmfTimestampTransform();

    /**
     * Default constructor
     */
    private TmfTimestampTransform() {

    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return timestamp;
    }

    @Override
    public long transform(long timestamp) {
        return timestamp;
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        /* Since this transform will not modify anything, return the other */
        return composeWith;
    }

    @Override
    public String toString() {
        return "TmfTimestampTransform [ IDENTITY ]"; //$NON-NLS-1$
    }

    @Override
    public ITmfTimestampTransform inverse() {
        return IDENTITY;
    }

}
