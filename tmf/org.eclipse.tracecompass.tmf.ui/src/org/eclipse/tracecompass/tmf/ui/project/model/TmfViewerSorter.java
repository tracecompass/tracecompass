/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Viewer sorter for TMF project model elements
 */
// Use deprecated base class for Neon backward compatibility
public class TmfViewerSorter extends ViewerSorter {

    private static int UNSET_PRIO = Integer.MAX_VALUE;
    private static int EXPERIMENT_FOLDER_PRIO = 0;
    private static int TRACE_FOLDER_PRIO = 1;
    private static int EXPERIMENT_ELEMENT_PRIO = 2;
    private static int TRACE_ELEMENT_PRIO = 3;
    private static int VIEWS_PRIO = 4;
    private static int ON_DEMAND_ANALYSIS_PRIO = 5;
    private static int REPORTS_PRIO = 6;

    @Override
    public int category(Object element) {
        int prio = UNSET_PRIO;
        if (element instanceof TmfExperimentFolder) {
            prio = EXPERIMENT_FOLDER_PRIO;
        } else if (element instanceof TmfTraceFolder) {
            prio = TRACE_FOLDER_PRIO;
        } else if (element instanceof TmfTraceElement) {
            prio = TRACE_ELEMENT_PRIO;
        } else if (element instanceof TmfExperimentElement) {
            prio = EXPERIMENT_ELEMENT_PRIO;
        } else if (element instanceof TmfViewsElement) {
            prio = VIEWS_PRIO;
        } else if (element instanceof TmfOnDemandAnalysesElement) {
            prio = ON_DEMAND_ANALYSIS_PRIO;
        } else if (element instanceof TmfReportsElement) {
            prio = REPORTS_PRIO;
        }
        return prio;
    }
}
