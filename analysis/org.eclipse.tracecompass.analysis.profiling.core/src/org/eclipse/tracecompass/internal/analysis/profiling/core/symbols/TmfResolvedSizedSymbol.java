/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.symbols;

import org.eclipse.tracecompass.analysis.profiling.core.symbols.TmfResolvedSymbol;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * A resolved symbol that has a size
 *
 * @author Geneviève Bastien
 */
public class TmfResolvedSizedSymbol extends TmfResolvedSymbol implements ISegment {

    /**
     * The generated serial UID
     */
    private static final long serialVersionUID = -726052365583654243L;
    private final long fSize;

    /**
     * Constructor
     *
     * @param address
     *            The address of this symbol
     * @param name
     *            The name this symbol resolves to
     * @param size
     *            The size of the symbol space
     */
    public TmfResolvedSizedSymbol(long address, String name, long size) {
        super(address, name);
        fSize = size;
    }

    @Override
    public long getStart() {
        return getBaseAddress();
    }

    @Override
    public long getEnd() {
        return getBaseAddress() + fSize;
    }

    @Override
    public long getLength() {
        return fSize;
    }

}
