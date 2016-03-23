/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.symbols;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProviderFactory;

/**
 * Basic symbol provider factory that can handle any trace. It will create a
 * {@link BasicSymbolProvider}.
 *
 * @author Robert Kiss
 *
 */
@NonNullByDefault
public class BasicSymbolProviderFactory implements ISymbolProviderFactory {

    @Override
    public @NonNull ISymbolProvider createProvider(ITmfTrace trace) {
        /* This provider can apply to any trace */
        return new BasicSymbolProvider(trace);
    }

}
