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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jface.preference.IPreferencePage;

/**
 * Allow the user to configure a {@link ISymbolProvider}
 *
 * @author Robert Kiss
 * @since 2.0
 * @deprecated Use the class with same name in the
 *             org.eclipse.tracecompass.analysis.profiling.ui plugin
 */
@Deprecated
@NonNullByDefault
public interface ISymbolProviderPreferencePage extends IPreferencePage {

    /**
     * Return the {@link ISymbolProvider} associated with this preference page
     *
     * @return the associated {@link ISymbolProvider}
     */
    ISymbolProvider getSymbolProvider();

    /**
     * Save the configuration that is currently in UI into the corresponding
     * {@link ISymbolProvider}.
     */
    void saveConfiguration();

}
