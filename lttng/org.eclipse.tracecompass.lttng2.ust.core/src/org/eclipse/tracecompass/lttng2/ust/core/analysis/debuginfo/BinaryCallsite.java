/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo;

/**
 * Object carrying the information about the "binary callsite" corresponding to
 * an instruction pointer. This consists in:
 *
 * <ul>
 * <li>Binary file</li>
 * <li>Symbol name</li>
 * <li>Offset (within the binary)</li>
 * </ul>
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class BinaryCallsite {

    private final String fBinaryFilePath;
    private final String fBuildId;
    private final long fOffset;
    private final boolean fIsPic;

    /**
     * Constructor
     *
     * @param binaryFilePath
     *            The path to the binary file on disk, as specified in the trace
     *            (may or may not be present on the system opening the trace).
     * @param buildId
     *            The Build-Id of the binary file. This is an unique identifier
     *            for a given object, so it can be used to make sure the file at
     *            the given path is the exact one we expect.
     * @param offset
     *            The offset of the call site. Its exact meaning will depend on
     *            the value of 'isPic'. This should be ready to be passed as-is
     *            to tools like addr2line.
     * @param isPic
     *            Indicates if the specified binary is Position-Independant Code
     *            or not. This will indicate if the 'offset' parameter is an
     *            absolute address (if isPic is false), or if it is an offset in
     *            the binary (is isPic is true).
     */
    public BinaryCallsite(String binaryFilePath, String buildId, long offset, boolean isPic) {
        if (offset < 0) {
            throw new IllegalArgumentException("Address offset cannot be negative"); //$NON-NLS-1$
        }

        fBinaryFilePath = binaryFilePath;
        fBuildId = buildId;
        fOffset = offset;
        fIsPic = isPic;
    }

    /**
     * Get the binary file's path
     *
     * @return The binary file path
     */
    public String getBinaryFilePath() {
        return fBinaryFilePath;
    }

    /**
     * The build-id of the binary
     *
     * @return The build-id
     */
    public String getBuildId() {
        return fBuildId;
    }

    /**
     * Get the address offset within the binary file corresponding to the
     * instruction pointer.
     *
     * @return The address offset
     */
    public long getOffset() {
        return fOffset;
    }

    @Override
    public String toString() {
        /*
         * Output is of the following format:
         *
         * For PIC/PIE binaries: /usr/lib/mylib.so+0x123
         * For non-PIC binaries: /usr/myprogram@0x401234
         */
        StringBuilder sb = new StringBuilder()
                .append(fBinaryFilePath);

        if (fIsPic) {
            sb.append('+');
        } else {
            sb.append('@');
        }

        sb.append("0x").append(Long.toHexString(fOffset)); //$NON-NLS-1$

        return sb.toString();
    }
}
