/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.symbols;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Instance of this interface can be contributed using the
 * <code>org.eclipse.tracecompass.tmf.ui.symbolProvider</code> extension and is
 * used to create instances of {@link ISymbolProvider}
 *
 * @author Robert Kiss
 *
 * @since 2.0
 */
public interface ISymbolProviderFactory {

    /**
     * Create a provider for the given trace. If this factory does not know how
     * to handle the given trace it will return null;
     *
     * @param trace
     *            A non-null trace
     * @return A newly created provider that can resolve symbols from the given
     *         trace or null if no such provider can be created by this factory
     */
    @Nullable ISymbolProvider createProvider(@NonNull ITmfTrace trace);

}
