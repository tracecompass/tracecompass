/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.symbols;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;

/**
 * An ISymbolProvider is used to map symbol addresses that might be found inside
 * an {@link TmfTrace} into human readable strings. This interface should be
 * used to augment
 * {@link org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider} to support
 * preference pages.
 *
 * @author Robert Kiss
 * @since 2.0
 * @see ISymbolProviderFactory
 */
public interface ISymbolProvider extends org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider {

    /**
     * Create the {@link ISymbolProviderPreferencePage} that can be used to
     * configure this {@link ISymbolProvider}
     *
     * @return the {@link ISymbolProviderPreferencePage} or null if this symbol
     *         provider does not offer a configuration UI
     */
    @Nullable ISymbolProviderPreferencePage createPreferencePage();

}
