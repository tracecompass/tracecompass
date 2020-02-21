/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

    public static String ErrorDialog_Info;
    public static String ErrorDialog_InfoMessage;
    public static String ErrorDialog_Error;
    public static String ErrorDialog_ErrorMessage;

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
