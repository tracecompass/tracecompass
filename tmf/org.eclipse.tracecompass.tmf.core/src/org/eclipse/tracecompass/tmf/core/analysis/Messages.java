/*******************************************************************************
 * Copyright (c) 2013, 2015 École Polytechnique de Montréal
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for org.eclipse.tracecompass.tmf.core.analysis
 *
 * @author Geneviève Bastien
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.core.analysis.messages"; //$NON-NLS-1$

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

    /** The trace to set was null */
    public static String TmfAbstractAnalysisModule_NullTrace;

    /** The label for the ID property
     * @since 2.0*/
    public static String TmfAbstractAnalysisModule_LabelId;

    /** Additional information on a requirement */
    public static String TmfAnalysis_RequirementInformation;

    /** Mandatory values of a requirement */
    public static String TmfAnalysis_RequirementMandatoryValues;

    /** A requirement is not fulfilled */
    public static String TmfAnalysis_RequirementNotFulfilled;

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
