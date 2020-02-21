/*******************************************************************************
 * Copyright (c) 2016-2017 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.symbols;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Instance of this interface can be contributed using the
 * <code>org.eclipse.tracecompass.tmf.core.symbolProvider</code> extension and is
 * used to create instances of {@link ISymbolProvider}
 *
 * @author Robert Kiss
 *
 * @since 3.0
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
    @Nullable ISymbolProvider createProvider(ITmfTrace trace);

}