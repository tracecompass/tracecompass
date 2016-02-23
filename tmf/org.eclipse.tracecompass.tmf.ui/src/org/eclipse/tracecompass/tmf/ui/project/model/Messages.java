/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouamé - Initial API and implementation
 *   Patrick Tasse - Add support for source location
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for TMF model handling.
 *
 * @author Jean-Christian Kouamé
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.project.model.messages"; //$NON-NLS-1$

    /**
     * The category of the analysis helper properties
     *
     * @since 2.0
     */
    public static String TmfAnalysisElement_HelperProperties;

    /**
     * The category of the analysis properties
     *
     * @since 2.0
     */
    public static String TmfAnalysisElement_AnalysisProperties;

    /** Instantiate analysis message box title */
    public static String TmfAnalysisElement_InstantiateAnalysis;

    /** The message when analysis view is not available */
    public static String TmfAnalysisViewOutput_ViewUnavailable;
    /** Analysis view title */
    public static String TmfAnalysisViewOutput_Title;

    /** Error message when closing editor */
    public static String TmfCommonProjectElement_ErrorClosingEditor;

    /** Error message when refreshing persistent property */
    public static String TmfCommonProjectElement_ErrorRefreshingProperty;

    /** Error message when instantiating trace */
    public static String TmfExperimentElement_ErrorInstantiatingTrace;
    /** Experiment text */
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

    /** The descriptor for the linked property */
    public static String TmfTraceElement_IsLinked;

    /** The descriptor for the source location property */
    public static String TmfTraceElement_SourceLocation;

    /** The descriptor for the time offset property */
    public static String TmfTraceElement_TimeOffset;

    /** The descriptor for the last modified property */
    public static String TmfTraceElement_LastModified;

    /** The descriptor for the size property */
    public static String TmfTraceElement_Size;

    /** The format string for the size property of a file */
    public static String TmfTraceElement_FileSizeString;

    /** The format string for the size property of a folder */
    public static String TmfTraceElement_FolderSizeString;

    /** The format string for the size property of a folder with too many members */
    public static String TmfTraceElement_FolderSizeOverflowString;

    /** Trace text */
    public static String TmfTraceElement_TypeName;

    /** Name of the "Views" element
     * @since 2.0*/
    public static String TmfViewsElement_Name;

    /**
     * The title for the select trace type dialog */
    public static String TmfTraceType_SelectTraceType;

    /** Error opening a trace or experiment */
    public static String TmfOpenTraceHelper_ErrorOpeningElement;
    /** Could not link trace */
    public static String TmfOpenTraceHelper_LinkFailed;
    /** Open trace or experiment */
    public static String TmfOpenTraceHelper_OpenElement;
    /** No trace or experiment type */
    public static String TmfOpenTraceHelper_NoTraceOrExperimentType;
    /** No trace type */
    public static String TmfOpenTraceHelper_NoTraceType;
    /** Error opening trace or experiment */
    public static String TmfOpenTraceHelper_ErrorElement;
    /** Init error */
    public static String TmfOpenTraceHelper_InitError;
    /** Trace not found */
    public static String TmfOpenTraceHelper_TraceNotFound;


    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
