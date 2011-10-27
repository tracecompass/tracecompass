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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * <b><u>TmfProjectElement</u></b>
 * <p>
 */
public class TmfProjectElement extends TmfProjectModelElement {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    TmfTraceFolder fTraceFolder = null;
    TmfExperimentFolder fExperimentFolder = null;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public TmfProjectElement(String name, IProject project, ITmfProjectModelElement parent) {
        super(name, project, parent);
    }

    // ------------------------------------------------------------------------
    // TmfProjectModelElement
    // ------------------------------------------------------------------------

    @Override
    public IProject getResource() {
        return (IProject) fResource;
    }

    @Override
    public void addChild(ITmfProjectModelElement child) {
        super.addChild(child);
        if (child instanceof TmfTraceFolder) {
            fTraceFolder = (TmfTraceFolder) child;
            return;
        }
        if (child instanceof TmfExperimentFolder) {
            fExperimentFolder = (TmfExperimentFolder) child;
            return;
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public TmfTraceFolder getTracesFolder() {
        return fTraceFolder;
    }

    public TmfExperimentFolder getExperimentsFolder() {
        return fExperimentFolder;
    }

    // ------------------------------------------------------------------------
    // TmfProjectElement
    // ------------------------------------------------------------------------

    @Override
    public void refresh() {
        try {
            new WorkspaceModifyOperation() {
                @Override
                protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
                  IProject project = getResource();
                  project.touch(null);
                }
            }.run(null);
        } catch (InvocationTargetException e) {
        } catch (InterruptedException e) {
        } catch (RuntimeException e) {
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            refresh();
        }
    }

    @Override
    public TmfProjectElement getProject() {
        return this;
    }

}
