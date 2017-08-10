/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.symbols;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.callstack.FunctionNameMapper;
import org.eclipse.tracecompass.internal.tmf.core.callstack.MappingFile;
import org.eclipse.tracecompass.internal.tmf.core.callstack.SizedMappingFile;

/**
 * Interface that mapping file classes must implement. This interface also
 * provides factory method to create the mappings from binary or text files.
 *
 * @author Geneviève Bastien
 * @since 3.1
 */
public interface IMappingFile {

    /**
     * Create a mapping file from a path.
     *
     * If the file name before the extension finishes by -[0-9]+, then the number is
     * taken as the ID of the process this mapping applies to.
     *
     * @param fullPath
     *            The full path of the file to load.
     * @param isBinaryFile
     *            <code>true</code> if the file is a binary file
     * @return The MappingFile object, or <code>null</code> if the file is invalid
     */
    static @Nullable IMappingFile create(String fullPath, boolean isBinaryFile) {
        Path path = Paths.get(fullPath);

        // Look for a process ID at the end of the filename.
        String filename = path.getFileName().toString();
        final Pattern pattern = Pattern.compile("(.+)-([0-9]+)\\.(.+)"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(filename);
        int pid = -1;
        if (matcher.find()) {
            try {
                pid = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
                // The number did not match to a proper integer, maybe it's a
                // long, ignore it then
            }
        }

        return create(fullPath, isBinaryFile, pid);
    }

    /**
     * Create a mapping file from a path and associate it with the PID this mapping
     * applies to.
     *
     * @param fullPath
     *            The full path of the file to load.
     * @param isBinaryFile
     *            <code>true</code> if the file is a binary file
     * @param pid
     *            The ID of the process that this mapping describes
     * @return The MappingFile object, or <code>null</code> if the file is invalid
     */
    static @Nullable IMappingFile create(String fullPath, boolean isBinaryFile, int pid) {
        Path path = Paths.get(fullPath);

        Map<Long, TmfResolvedSymbol> results = null;
        if (isBinaryFile) {
            results = FunctionNameMapper.mapFromBinaryFile(path.toFile());
            return new MappingFile(fullPath, isBinaryFile, results, pid);
        }
        switch(FunctionNameMapper.guessMappingType(path.toFile())) {
        case MAP_WITH_SIZE:
            results = FunctionNameMapper.mapFromSizedTextFile(path.toFile());
            return results == null ? null : new SizedMappingFile(fullPath, isBinaryFile, results, pid);
        case NM:
            results = FunctionNameMapper.mapFromNmTextFile(path.toFile());
            return results == null ? null : new MappingFile(fullPath, isBinaryFile, results, pid);
        case UNKNOWN: // Fall-through
        default:
            return null;
        }
    }

    /**
     * @return path leading to mapping file
     */
    String getFullPath();

    /**
     * @return type of the mapping file
     */
    boolean isBinaryFile();

    /**
     * Get the entry that may correspond to the symbol
     *
     * @param address
     *            The address of the symbol to look for
     * @return The entry with its address/symbol if it's within this mapping's space
     */
    @Nullable TmfResolvedSymbol getSymbolEntry(long address);

    /**
     * Get the ID of the process this mapping is for.
     *
     * @return the process ID that this mapping applies to. A negative value means
     *         it applies to no specific process.
     */
    int getPid();
}
