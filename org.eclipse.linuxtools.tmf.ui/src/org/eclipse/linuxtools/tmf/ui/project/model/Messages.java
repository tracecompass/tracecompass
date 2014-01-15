/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouamé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for TMF model handling.
 *
 * @author Jean-Christian Kouamé
 * @since 2.1
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.project.model.messages"; //$NON-NLS-1$

    /** Instantiate analysis message box title
     * @since 3.0*/
    public static String TmfAnalysisElement_InstantiateAnalysis;

    /** The message when analysis view is not available
     * @since 3.0*/
    public static String TmfAnalysisViewOutput_ViewUnavailable;
    /** Analysis view title
     * @since 3.0 */
    public static String TmfAnalysisViewOutput_Title;

    /** Error message when closing editor
     * @since 3.0 */
    public static String TmfCommonProjectElement_ErrorClosingEditor;

    /** Error message when refreshing persistent property
     * @since 3.0 */
    public static String TmfCommonProjectElement_ErrorRefreshingProperty;

    /** Error message when instantiating trace
     * @since 3.0 */
    public static String TmfExperimentElement_ErrorInstantiatingTrace;
    /** Experiment text
     * @since 3.0*/
    public static String TmfExperimentElement_TypeName;

    /** The category of the resource properties */
    public static String TmfTraceElement_ResourceProperties;

    /** The category of the trace properties */
    public static String TmfTraceElement_TraceProperties;

    /** The descriptor for the name property */
    public static String TmfTraceElement_Name;

    /** The descriptor for the path property */
    public static String TmfTraceElement_Path;

    /** The descriptor for the location properties */
    public static String TmfTraceElement_Location;

    /** The descriptor for the event type property */
    public static String TmfTraceElement_EventType;

    /** The description for the linked property */
    public static String TmfTraceElement_IsLinked;
    /** Trace text
     * @since 3.0*/
    public static String TmfTraceElement_TypeName;
    /**
     * The title for the select trace type dialog
     * @since 2.2
     * */
    public static String TmfTraceType_SelectTraceType;

    /** Error opening a trace or experiment
     * @since 3.0*/
    public static String TmfOpenTraceHelper_ErrorOpeningElement;
    /** Could not link trace */
    public static String TmfOpenTraceHelper_LinkFailed;
    /** No trace type match */
    public static String TmfOpenTraceHelper_NoTraceTypeMatch;
    /** Open trace or experiment
     * @since 3.0*/
    public static String TmfOpenTraceHelper_OpenElement;
    /** Reduce was too efficient, no candidates found! */
    public static String TmfOpenTraceHelper_ReduceError;
    /** No trace or experiment type
     * @since 3.0*/
    public static String TmfOpenTraceHelper_NoTraceOrExperimentType;
    /** No trace type */
    public static String TmfOpenTraceHelper_NoTraceType;
    /** Error opening trace or experiment
     * @since 3.0*/
    public static String TmfOpenTraceHelper_ErrorElement;
    /** Init error */
    public static String TmfOpenTraceHelper_InitError;
    /** Trace not found
     * @since 3.0*/
    public static String TmfOpenTraceHelper_TraceNotFound;


    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
