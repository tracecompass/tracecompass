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

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Viewer sorter for TMF project model elements
 *
 * @since 3.0
 */
public class TmfViewerSorter extends ViewerSorter {

    @Override
    public int category(Object element) {
        if (element instanceof TmfExperimentFolder) {
            return 0;
        }
        if (element instanceof TmfTraceFolder) {
            return 0;
        }
        if (element instanceof TmfExperimentElement) {
            return 1;
        }
        if (element instanceof TmfTraceElement) {
            return 1;
        }
        return 2;
    }


}
