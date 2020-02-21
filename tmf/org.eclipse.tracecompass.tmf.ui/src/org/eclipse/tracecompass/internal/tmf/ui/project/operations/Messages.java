/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
  *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.operations;

import org.eclipse.osgi.util.NLS;

/**
 * The messages for workspace operations.
 * @author Bernd Hufmann
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.project.operations.messages"; //$NON-NLS-1$

    /**
     * The task name for removing of a trace for an experiment.
     */
    public static String SelectTracesWizardPage_TraceRemovalTask;

    /**
     * The task name for selecting of a trace for an experiment.
     */
    public static String SelectTracesWizardPage_TraceSelectionTask;

    /**
     * The error message when selecting of traces for an experiment fails.
     */
    public static String SelectTracesWizardPage_SelectionError;

    /**
     * The job name for updating the trace bounds.
     */
    public static String SelectTracesWizardPage_UpdateTraceBoundsJobName;

    /** The error message when an experiment could not be created */
    public static String NewExperimentOperation_CreationError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
