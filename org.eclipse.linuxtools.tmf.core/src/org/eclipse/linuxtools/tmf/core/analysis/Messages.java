/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.analysis;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for org.eclipse.linuxtools.tmf.core.analysis
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.core.analysis.messages"; //$NON-NLS-1$

    /** Trace was set more than once for this module */
    public static String TmfAbstractAnalysisModule_TraceSetMoreThanOnce;

    /** Analysis Module cannot execute on trace */
    public static String TmfAbstractAnalysisModule_AnalysisCannotExecute;

    /** Analysis Module does not apply to trace */
    public static String TmfAnalysisModuleHelper_AnalysisDoesNotApply;

    /** Analysis Module for trace */
    public static String TmfAbstractAnalysisModule_AnalysisForTrace;

    /** Analysis Module presentation */
    public static String TmfAbstractAnalysisModule_AnalysisModule;

    /** Parameter is invalid */
    public static String TmfAbstractAnalysisModule_InvalidParameter;

    /** Running analysis */
    public static String TmfAbstractAnalysisModule_RunningAnalysis;

    /** Error instantiating parameter provider */
    public static String TmfAnalysisManager_ErrorParameterProvider;

    /** Impossible to instantiate module from helper */
    public static String TmfAnalysisModuleHelper_ImpossibleToCreateModule;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
