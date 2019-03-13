/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Kiss - Initial messages for basic symbol provider preference page
 *   Mikael Ferland - Add new messages to support multiple symbol files
 *
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

    public static String BasicSymbolProviderPrefPage_addFile_text;
    public static String BasicSymbolProviderPrefPage_removeFile_text;
    public static String BasicSymbolProviderPrefPage_priorityUp_text;
    public static String BasicSymbolProviderPrefPage_priorityUp_tooltip;
    public static String BasicSymbolProviderPrefPage_priorityDown_text;
    public static String BasicSymbolProviderPrefPage_priorityDown_tooltip;
    public static String BasicSymbolProviderPrefPage_description;
    public static String BasicSymbolProviderPrefPage_tabTitle;
    public static String BasicSymbolProviderPrefPage_invalidMappingFileDialogHeader;
    public static String BasicSymbolProviderPrefPage_invalidMappingFileMessage;

    public static String SymbolProviderConfigDialog_loadingConfigurations;
    public static String SymbolProviderConfigDialog_title;
    public static String SymbolProviderConfigDialog_message;
}
