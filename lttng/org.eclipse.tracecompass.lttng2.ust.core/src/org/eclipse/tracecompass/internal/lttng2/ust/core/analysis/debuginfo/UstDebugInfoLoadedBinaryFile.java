/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo;

/**
 * Simple extension to {@link UstDebugInfoBinaryFile} that adds the base address at
 * which the given binary or library is loaded.
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoLoadedBinaryFile extends UstDebugInfoBinaryFile {

    private final long baseAddress;

    /**
     * Constructor
     *
     * @param baseAddress
     *            The base address at which the binary or library is loaded
     * @param filePath
     *            The file path of the loaded binary/library
     * @param buildId
     *            The build ID of the binary object
     * @param isPic
     *            If the binary is position-independent or not
     */
    public UstDebugInfoLoadedBinaryFile(long baseAddress, String filePath, String buildId, boolean isPic) {
        super(filePath, buildId, isPic);
        this.baseAddress = baseAddress;
    }

    /**
     * Return the base address at which the object is loaded.
     *
     * @return The base address
     */
    public long getBaseAddress() {
        return baseAddress;
    }
}
