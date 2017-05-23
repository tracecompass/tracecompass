/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.symbols;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private Messages() {
    }

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.symbols.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String BasicSymbolProviderPrefPage_radioBinaryFile_text;
    public static String BasicSymbolProviderPrefPage_radioBinaryFile_tooltip;
    public static String BasicSymbolProviderPrefPage_radioMappingFile_text;
    public static String BasicSymbolProviderPrefPage_radioMappingFile_tooltip;
    public static String BasicSymbolProviderPrefPage_btnBrowse;
    public static String BasicSymbolProviderPrefPage_description;
    public static String BasicSymbolProviderPrefPage_tabTitle;
    public static String BasicSymbolProviderPrefPage_ImportMappingDialogTitle;
    public static String BasicSymbolProviderPrefPage_ImportBinaryFileDialogTitle;
    public static String BasicSymbolProviderPrefPage_errorFileDoesNotExists;
    public static String BasicSymbolProviderPrefPage_errorSpecifyFile;

}
