/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

/**
 * <b><u>TmfProjectRegistry</u></b>
 * <p>
 */
public class TmfProjectRegistry {

    private static Map<IProject, TmfProjectElement> registry = new HashMap<IProject, TmfProjectElement>();

    public static synchronized TmfProjectElement getProject(IProject project) {
        TmfProjectElement element = registry.get(project);
        if (element == null) {
            registry.put(project, new TmfProjectElement(project.getName(), project, null));
            element = registry.get(project);
        }
        return element;
    }

}
