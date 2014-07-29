/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.synchronization;

import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TmfTimestampTransformLinear;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;

/**
 * Constant transform, just offset your timestamp with another.
 *
 * @author Matthew Khouzam
 */
public class TmfConstantTransform implements ITmfTimestampTransform {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 417299521984404532L;
    private ITmfTimestamp fOffset;

    /**
     * Default constructor
     */
    public TmfConstantTransform() {
        fOffset = new TmfNanoTimestamp(0);
    }

    /**
     * Constructor with offset
     *
     * @param offset
     *            The offset of the linear transform in nanoseconds
     */
    public TmfConstantTransform(long offset) {
        fOffset = new TmfNanoTimestamp(offset);
    }

    /**
     * Constructor with offset timestamp
     *
     * @param offset
     *            The offset of the linear transform
     */
    public TmfConstantTransform(ITmfTimestamp offset) {
        fOffset = offset;
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return fOffset.normalize(timestamp.getValue(), timestamp.getScale());
    }

    /**
     * {@inheritDoc}
     *
     * @param timestamp
     *            the timestamp in nanoseconds
     * @return the timestamp in nanoseconds
     */
    @Override
    public long transform(long timestamp) {
        return fOffset.normalize(timestamp, ITmfTimestamp.NANOSECOND_SCALE).getValue();
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfConstantTransform) {
            TmfConstantTransform tct = (TmfConstantTransform) composeWith;
            return new TmfConstantTransform(fOffset.getValue() + tct.fOffset.getValue());
        } else if (composeWith instanceof TmfTimestampTransformLinear) {
            throw new UnsupportedOperationException("Cannot compose a constant and linear transform yet"); //$NON-NLS-1$
        } else {
            /*
             * We do not know what to do with this kind of transform, just
             * return this
             */
            return this;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TmfConstantTransform [fOffset="); //$NON-NLS-1$
        builder.append(fOffset);
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

}
