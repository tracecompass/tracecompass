/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@NonNullByDefault({})
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String LamiAnalysis_MainTaskName;

    public static String ParameterDialog_BaseCommand;
    public static String ParameterDialog_ExternalParameters;
    public static String ParameterDialog_ExternalParametersDescription;
    public static String ParameterDialog_StringValidatorMessage;
    public static String ParameterDialog_ReportNameSuffix;
    public static String ParameterDialog_Error;
    public static String ParameterDialog_ErrorMessage;
    public static String AddAnalysisDialog_Name;
    public static String AddAnalysisDialog_Command;
    public static String AddAnalysisDialog_NameEmptyErrorMessage;
    public static String AddAnalysisDialog_CommandEmptyErrorMessage;
    public static String AddAnalysisDialog_Title;
    public static String AddAnalysisDialog_ErrorBoxTitle;
    public static String AddAnalysisDialog_ErrorBoxMessage;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
