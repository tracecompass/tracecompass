/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.symbols;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.callstack.FunctionNameMapper;
import org.eclipse.tracecompass.internal.tmf.core.callstack.MappingFile;

/**
 * Interface that mapping file classes must implement. This interface also
 * provides factory method to create the mappings from binary or text files.
 *
 * @author Geneviève Bastien
 * @since 3.1
 */
public interface IMappingFile {

    /**
     * Create a mapping file from a path
     *
     * @param fullPath
     *            The full path of the file to load
     * @param isBinaryFile
     *            <code>true</code> if the file is a binary file
     * @return The MappingFile object, or <code>null</code> if the file is invalid
     */
    public static @Nullable IMappingFile create(String fullPath, boolean isBinaryFile) {
        File file = new File(fullPath);

        Map<Long, TmfResolvedSymbol> results = null;
        if (isBinaryFile) {
            results = FunctionNameMapper.mapFromBinaryFile(file);
        } else {
            results = FunctionNameMapper.mapFromNmTextFile(file);
        }

        // results is null if mapping file is invalid
        if (results == null) {
            return null;
        }
        return new MappingFile(fullPath, isBinaryFile, results);
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
     * @return The entry with its address/symbol if it's within this mapping's
     *         space, or <code>null</code> if this address is not mapped in this
     *         file
     */
    @Nullable TmfResolvedSymbol getSymbolEntry(long address);

}
