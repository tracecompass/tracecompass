/*******************************************************************************
 * Copyright (c) 2009, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Add signal manager disposal
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.annotations.CustomDefinedOutputAnnotationProviderFactory;
import org.eclipse.tracecompass.internal.tmf.core.annotations.LostEventsOutputAnnotationProviderFactory;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.symbols.SymbolProviderManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceAdapterManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle. No more than one such
 * plug-in can exist at any time.
 * <p>
 * It also provides the plug-in's general logging facility and manages the
 * internal tracer.
 */
public class Activator extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The plug-in ID
     */
    public static final @NonNull String PLUGIN_ID = "org.eclipse.tracecompass.tmf.core"; //$NON-NLS-1$

    /**
     * The shared instance
     */
    private static Activator fPlugin;
    private static final LostEventsOutputAnnotationProviderFactory LOST_EVENTS_ANNOTATION_PROVIDER_FACTORY = new LostEventsOutputAnnotationProviderFactory();
    private static final CustomDefinedOutputAnnotationProviderFactory CUSTOM_DEFINED_OUTPUT_ANNOTATION_PROVIDER_FACTORY = new CustomDefinedOutputAnnotationProviderFactory();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public Activator() {
        setDefault(this);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the TMF Core plug-in instance.
     *
     * @return the TMF Core plug-in instance.
     */
    public static Activator getDefault() {
        return fPlugin;
    }

    // Sets plug-in instance
    private static void setDefault(Activator plugin) {
        fPlugin = plugin;
    }

    // ------------------------------------------------------------------------
    // Plugin
    // ------------------------------------------------------------------------

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        setDefault(this);
        TmfCoreTracer.init();
        /* Initialize the trace manager */
        TmfTraceManager.getInstance();
        /* Initialize the analysis manager */
        TmfAnalysisManager.initialize();
        /* Initialize the symbol provider manager */
        SymbolProviderManager.getInstance();
        /* Initialize the data provider manager */
        DataProviderManager.getInstance();
        TmfTraceAdapterManager.registerFactory(LOST_EVENTS_ANNOTATION_PROVIDER_FACTORY, ITmfTrace.class);
        TmfTraceAdapterManager.registerFactory(CUSTOM_DEFINED_OUTPUT_ANNOTATION_PROVIDER_FACTORY, ITmfTrace.class);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        TmfTraceAdapterManager.unregisterFactory(LOST_EVENTS_ANNOTATION_PROVIDER_FACTORY);
        TmfTraceAdapterManager.unregisterFactory(CUSTOM_DEFINED_OUTPUT_ANNOTATION_PROVIDER_FACTORY);
        LOST_EVENTS_ANNOTATION_PROVIDER_FACTORY.dispose();
        CUSTOM_DEFINED_OUTPUT_ANNOTATION_PROVIDER_FACTORY.dispose();
        TmfCoreTracer.stop();
        TmfTraceManager.getInstance().dispose();
        TmfAnalysisManager.dispose();
        SymbolProviderManager.dispose();
        DataProviderManager.dispose();
        TmfSignalManager.dispose();
        setDefault(null);
        super.stop(context);
    }


    // ------------------------------------------------------------------------
    // Log an IStatus
    // ------------------------------------------------------------------------

    /**
     * Log an IStatus object directly
     *
     * @param status
     *            The status to log
     */
    public static void log(IStatus status) {
        fPlugin.getLog().log(status);
    }

    // ------------------------------------------------------------------------
    // Log INFO
    // ------------------------------------------------------------------------

    /**
     * Logs a message with severity INFO in the runtime log of the plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logInfo(String message) {
        fPlugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity INFO in the runtime log of the
     * plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logInfo(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, exception));
    }

    // ------------------------------------------------------------------------
    // Log WARNING
    // ------------------------------------------------------------------------

    /**
     * Logs a message and exception with severity WARNING in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logWarning(String message) {
        fPlugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logWarning(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, exception));
    }

    // ------------------------------------------------------------------------
    // Log ERROR
    // ------------------------------------------------------------------------

    /**
     * Logs a message and exception with severity ERROR in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     */
    public static void logError(String message) {
        fPlugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of
     * the plug-in.
     *
     * @param message
     *            A message to log
     * @param exception
     *            The corresponding exception
     */
    public static void logError(String message, Throwable exception) {
        fPlugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
    }
}
