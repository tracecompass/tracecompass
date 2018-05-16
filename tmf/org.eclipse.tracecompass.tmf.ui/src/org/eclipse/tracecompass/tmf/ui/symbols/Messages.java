/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.symbols;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 *
 * @noreference Messages class
 * @deprecated Use the class with same name in the
 *             org.eclipse.tracecompass.analysis.profiling.ui plugin
 */
@Deprecated
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private Messages() {
    }

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.symbols.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String SymbolProviderConfigDialog_loadingConfigurations;
    public static String SymbolProviderConfigDialog_title;
    public static String SymbolProviderConfigDialog_message;

}
