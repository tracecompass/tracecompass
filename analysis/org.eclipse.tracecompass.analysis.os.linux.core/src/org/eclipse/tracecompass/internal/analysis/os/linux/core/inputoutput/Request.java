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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.IoOperationType;

/**
 * Represents a request to a disk of the system
 *
 * @author Houssem Daoud
 */
public class Request {

    private Long fSector;
    private int fNrSector;
    private final DiskWriteModel fDisk;
    private final List<BlockIO> fBios = new ArrayList<>();
    private @Nullable Integer fIssuedFrom = null;
    private IoOperationType fType;

    /**
     * Constructor
     *
     * @param disk
     *            The disk for this request
     * @param sector
     *            The base sector of this request
     * @param rwbs
     *            The read/write bits
     */
    public Request(DiskWriteModel disk, Long sector, int rwbs) {
        fSector = sector;
        fNrSector = 0;
        fDisk = disk;
        fType = IoOperationType.getType(rwbs);
    }

    /**
     * Constructor from a Block IO structure
     *
     * @param bio
     *            The BIO to start this request from
     */
    public Request(BlockIO bio) {
        fSector = bio.getSector();
        fNrSector = bio.getNrSector();
        fType = bio.getType();
        fBios.add(0, bio);
        fDisk = bio.getDisk();
    }

    /**
     * Get the base sector of this request
     *
     * @return The base sector
     */
    public Long getSector() {
        return fSector;
    }

    /**
     * Get the number of sectors of this request
     *
     * @return The number of sectors
     */
    public int getNrSector() {
        return fNrSector;
    }

    /**
     * Updates the number of sectors for this request
     *
     * @param nrSector
     *            The new number of sectors
     */
    public void setNrSector(int nrSector) {
        fNrSector = nrSector;
    }

    /**
     * Get the disk this request is for
     *
     * @return The disk of this BIO
     */
    public DiskWriteModel getDisk() {
        return fDisk;
    }

    /**
     * Get the type of request
     *
     * @return The type of request
     */
    public IoOperationType getType() {
        return fType;
    }

    /**
     * Set the read/write mode of this request
     *
     * @param rwbs
     *            The read/write bits of the request
     */
    public void setType(int rwbs) {
        fType = IoOperationType.getType(rwbs);
    }

    /**
     * Get the list of BIOs included in this request
     *
     * @return The list of BIOs
     */
    public List<BlockIO> getBios() {
        return Collections.unmodifiableList(fBios);
    }

    /**
     * Get the request this request is based on
     *
     * @return The quark of the request this is issued from
     */
    public @Nullable Integer getIssuedFrom() {
        return fIssuedFrom;
    }

    /**
     * Insert the BIO into this request
     *
     * @param bio
     *            The Block IO to insert in this request
     */
    public void insertBio(BlockIO bio) {
        fBios.add(bio);
        fNrSector += bio.getNrSector();
        if (bio.getSector() < getSector()) {
            fSector = bio.getSector();
        }
    }

    /**
     * Merges a request into this one
     *
     * @param request
     *            The second request to merge
     */
    public void mergeRequest(Request request) {
        fBios.addAll(request.getBios());
        fNrSector = getNrSector() + request.getNrSector();
    }

}
