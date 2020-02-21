/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.matching;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Traces can be matched using TCP network packets. To uniquely match a TCP
 * packet from a trace with one from another trace, the three following fields
 * are used: sequence number, acknowledgment number and the 16-bits following
 * the acknowledgment number (data offset, reserved and flags).
 *
 * All match definitions using TCP fields should return a key of this type so
 * all TCP matching methods are compatible.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TcpEventKey implements IEventMatchingKey {

    private static final HashFunction HF = checkNotNull(Hashing.goodFastHash(32));
    private final long fSeq;
    private final long fAckseq;
    private final long fFlags;

    /**
     * Constructor
     *
     * @param sequence
     *            The sequence number of the TCP packet
     * @param ack
     *            The acknowledgement number of the TCP packet
     * @param flags
     *            The 16 bits following the acknowledgment: data offset,
     *            reserved and flags)
     */
    public TcpEventKey(long sequence, long ack, long flags) {
        fSeq = sequence;
        fAckseq = ack;
        fFlags = flags;
    }

    @Override
    public int hashCode() {
        return HF.newHasher()
                .putLong(fSeq)
                .putLong(fAckseq)
                .putLong(fFlags).hash().asInt();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof TcpEventKey) {
            TcpEventKey key = (TcpEventKey) o;
            return (key.fSeq == fSeq &&
                    key.fAckseq == fAckseq &&
                    key.fFlags == fFlags);
        }
        return false;
    }
}
