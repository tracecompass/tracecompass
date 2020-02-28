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
    READ;

    /**
     * Get the BIO type from the rwbs value of a block operation.
     *
     * @param rwbs
     *            The rwbs value of the block operation
     * @return The BIO type
     */
    public static IoOperationType getType(int rwbs) {
        /* Even is a "read", odd is a "write" */
        return (rwbs % 2 == 0) ? READ : WRITE;
    }
}
