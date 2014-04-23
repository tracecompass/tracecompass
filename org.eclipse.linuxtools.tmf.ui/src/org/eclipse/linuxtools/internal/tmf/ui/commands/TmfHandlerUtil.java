/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *     Patrick Tasse - Add support for folder elements
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;

/**
 * Utility methods for handlers
 *
 * @author Marc-Andre Laperle
 */
public class TmfHandlerUtil {

    /**
     * Get the enclosing project from the selection
     *
     * @param selection
     *            the selection
     *
     * @return the enclosing project or null if selection is no enclosed by a
     *         project
     */
    public static IProject getProjectFromSelection(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object firstElement = structuredSelection.getFirstElement();
            if (firstElement instanceof ITmfProjectModelElement) {
                ITmfProjectModelElement tmfProjectElement = (ITmfProjectModelElement) firstElement;
                return tmfProjectElement.getProject().getResource();
            }
        }

        return null;
    }

    /**
     * Get the trace folder from the selection
     *
     * @param selection
     *            the selection
     *
     * @return the enclosing project or null if selection is no enclosed by a
     *         project
     */
    public static TmfTraceFolder getTraceFolderFromSelection(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object firstElement = structuredSelection.getFirstElement();
            if (firstElement instanceof ITmfProjectModelElement) {
                ITmfProjectModelElement element = (ITmfProjectModelElement) firstElement;
                while (element != null) {
                    if (element instanceof TmfTraceFolder) {
                        return (TmfTraceFolder) element;
                    }
                    element = element.getParent();
                }
            }
        }
        return null;
    }
}
