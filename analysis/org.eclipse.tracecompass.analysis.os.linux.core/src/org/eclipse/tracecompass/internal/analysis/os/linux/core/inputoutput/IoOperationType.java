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

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

/**
 * Enum type for IO operations
 *
 * @author Geneviève Bastien
 */
public enum IoOperationType {

    /**
     * Write to the disk
     */
    WRITE,
    /**
     * Read on the disk
     */
    READ,
    /**
     * Flush data to disk
     */
    FLUSH,
    /**
     * Other type of request
     */
    OTHER;

    /**
     * The flags below are taken from lttng-module's block device
     * instrumentation file, precisely, as of the time the feature was added, in
     * the file include/instrumentation/events/block.h
     *
     * For now, reads, writes and flushes are supported. Here are other values
     * that could be eventually supported as well
     *
     * private static final int RWBS_FLAG_DISCARD = 1 << 1; private static final
     * int RWBS_FLAG_BARRIER = 1 << 4; private static final int RWBS_FLAG_SYNC =
     * 1 << 5; private static final int RWBS_FLAG_META = 1 << 6; private static
     * final int RWBS_FLAG_SECURE = 1 << 7; private static final int
     * RWBS_FLAG_FUA = 1 << 9;
     */
    private static final int RWBS_FLAG_WRITE = 1 << 0;
    private static final int RWBS_FLAG_READ = 1 << 2;
    private static final int RWBS_FLAG_RAHEAD = 1 << 3;
    private static final int RWBS_FLAG_FLUSH = 1 << 8;
    private static final int RWBS_FLAG_PREFLUSH = 1 << 10;

    /**
     * Get the BIO type from the rwbs value of a block operation.
     *
     * @param rwbs
     *            The rwbs value of the block operation
     * @return The BIO type
     */
    public static IoOperationType getType(int rwbs) {
        if ((rwbs & RWBS_FLAG_WRITE) != 0) {
            return WRITE;
        }
        if (((rwbs & RWBS_FLAG_READ) != 0) || ((rwbs & RWBS_FLAG_RAHEAD) != 0)) {
            return READ;
        }
        if (((rwbs & RWBS_FLAG_FLUSH) != 0) || ((rwbs & RWBS_FLAG_PREFLUSH) != 0)) {
            return FLUSH;
        }
        return OTHER;
    }

    /**
     * Get the operation type from a byte value
     *
     * @param value
     *            The byte value
     * @return The operation type
     */
    public static IoOperationType fromNumber(Integer value) {
        if (value == WRITE.ordinal()) {
            return WRITE;
        }
        if (value == READ.ordinal()) {
            return READ;
        }
        if (value == FLUSH.ordinal()) {
            return FLUSH;
        }
        return OTHER;
    }
}
