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
 * Class that represents a block IO structure of the os kernel
 *
 * @author Houssem Daoud
 */
public class BlockIO {

    private final Long fSector;
    private final int fNrSector;
    private final DiskWriteModel fDisk;
    private final IoOperationType fType;

    /**
     * Constructor
     *
     * @param sector
     *            Start sector of this block IO
     * @param nr_sector
     *            The number of sectors from this block IO
     * @param disk
     *            The disk this BIO is on
     * @param rwbs
     *            The rwbs value of a block operation
     */
    public BlockIO(Long sector, int nr_sector, DiskWriteModel disk, IoOperationType rwbs) {
        fSector = sector;
        fNrSector = nr_sector;
        fDisk = disk;
        fType = rwbs;
    }

    /**
     * Get the base sector of this BIO
     *
     * @return The base sector
     */
    public Long getSector() {
        return fSector;
    }

    /**
     * Get the number of sectors of this BIO
     *
     * @return The number of sectors
     */
    public int getNrSector() {
        return fNrSector;
    }

    /**
     * Get the disk this block IO is for
     *
     * @return The disk of this BIO
     */
    public DiskWriteModel getDisk() {
        return fDisk;
    }

    /**
     * Get the type of BIO
     *
     * @return The type of BIO
     */
    public IoOperationType getType() {
        return fType;
    }
}
