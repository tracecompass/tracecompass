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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;

/**
 * Constant transform, just offset your timestamp with another.
 *
 * @author Matthew Khouzam
 */
public class TmfConstantTransform implements ITmfTimestampTransformInvertible {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 417299521984404532L;
    private final long fOffset;

    /**
     * Default constructor
     */
    public TmfConstantTransform() {
        // we really should be using an identity transform here.
        fOffset = 0;
    }

    /**
     * Constructor with offset
     *
     * @param offset
     *            The offset of the linear transform in nanoseconds
     */
    public TmfConstantTransform(long offset) {
        fOffset = offset;
    }

    /**
     * Constructor with offset timestamp
     *
     * @param offset
     *            The offset of the linear transform
     */
    public TmfConstantTransform(@NonNull ITmfTimestamp offset) {
        this(new TmfNanoTimestamp(offset).getValue());
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return timestamp.normalize(fOffset, ITmfTimestamp.NANOSECOND_SCALE);
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
        return fOffset + timestamp;
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfConstantTransform) {
            TmfConstantTransform tct = (TmfConstantTransform) composeWith;
            final long offset = fOffset + tct.fOffset;
            if (offset == 0) {
                return TmfTimestampTransform.IDENTITY;
            }
            return new TmfConstantTransform(offset);
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
        builder.append("TmfConstantTransform [ offset = "); //$NON-NLS-1$
        builder.append(fOffset);
        builder.append(" ]"); //$NON-NLS-1$
        return builder.toString();
    }

    @Override
    public ITmfTimestampTransform inverse() {
        return TimestampTransformFactory.createWithOffset(-1 * fOffset);
    }

}
