/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;

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
}
