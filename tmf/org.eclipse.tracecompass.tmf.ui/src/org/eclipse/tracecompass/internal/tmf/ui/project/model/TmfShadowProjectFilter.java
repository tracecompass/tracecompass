/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.project.model;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Viewer filter to filter out shadow project elements from Project Explorer.
 *
 * @author Bernd Hufmann
 */
public class TmfShadowProjectFilter extends ViewerFilter {

    /**
     * Constructor
     */
    public TmfShadowProjectFilter() {
        // do nothing
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IProject) {
            IProject project = (IProject) element;
            if (TmfProjectModelHelper.isShadowProject(project)) {
                return false;
            }
        }
        return true;
    }
}
