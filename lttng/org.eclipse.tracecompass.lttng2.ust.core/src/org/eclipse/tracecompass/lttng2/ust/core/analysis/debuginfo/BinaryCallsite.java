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
    private final String fSymbolName;
    private final long fOffset;

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
     * @param symbolName
     *            The name of the symbol in the path. Should not be null, but
     *            can be an empty string if not available.
     * @param offset
     *            The offset *within the binary* of the call site. This should
     *            be ready to be passed as-is to tools like addr2line.
     */
    public BinaryCallsite(String binaryFilePath, String buildId, String symbolName, long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Address offset cannot be negative"); //$NON-NLS-1$
        }

        fBinaryFilePath = binaryFilePath;
        fBuildId = buildId;
        fSymbolName = symbolName;
        fOffset = offset;
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
     * Get the name of the symbol this instruction pointer is from, if it is
     * available.
     *
     * @return The symbol name, or an empty string if not available
     */
    public String getSymbolName() {
        return fSymbolName;
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
        StringBuilder sb = new StringBuilder();
        sb.append(fBinaryFilePath);
        if (!fSymbolName.equals("")) { //$NON-NLS-1$
            sb.append(", "); //$NON-NLS-1$
            sb.append(Messages.UstDebugInfoAnalysis_Symbol);
            sb.append('=');
            sb.append(fSymbolName);
        }
        sb.append(", "); //$NON-NLS-1$
        sb.append(Messages.UstDebugInfoAnalysis_Offset);
        sb.append('=');
        sb.append(fOffset);

        return sb.toString();
    }
}
