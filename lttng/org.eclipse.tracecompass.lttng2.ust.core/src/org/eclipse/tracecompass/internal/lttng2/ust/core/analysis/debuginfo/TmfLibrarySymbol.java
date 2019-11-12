/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo;

import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;

/**
 * This class represents a resolved symbol that comes from a library. The symbol
 * name will thus contain the library name in parenthesis.
 *
 * @author Geneviève Bastien
 */
public class TmfLibrarySymbol extends TmfResolvedSymbol {

    private final String fSourceFile;

    /**
     * Constructor
     *
     * @param address
     *            The address of this symbol
     * @param sourceFile
     *            The source file of this symbol
     */
    public TmfLibrarySymbol(long address, String sourceFile) {
        super(address, "0x" + Long.toHexString(address)); //$NON-NLS-1$
        fSourceFile = sourceFile;
    }

    @Override
    public String getSymbolName() {
        return super.getSymbolName() + ' ' +'(' + fSourceFile + ')';
    }
}
