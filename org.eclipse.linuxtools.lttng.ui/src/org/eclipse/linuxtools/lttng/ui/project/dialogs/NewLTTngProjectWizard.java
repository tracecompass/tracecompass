/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson, MontaVista Software
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Yufen Kuo       (ykuo@mvista.com) - add support to allow user specify trace library path
 *   Francois Chouinard - Rebase on TMF NewProjectWizard
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.project.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.lttng.core.TraceHelper;
import org.eclipse.linuxtools.lttng.core.LTTngProjectNature;
import org.eclipse.linuxtools.tmf.core.TmfProjectNature;
import org.eclipse.linuxtools.tmf.ui.project.wizards.NewTmfProjectWizard;

/**
 * <b><u>NewLTTngProjectWizard</u></b>
 * <p>
 */
public class NewLTTngProjectWizard extends NewTmfProjectWizard {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TRACE_LIBRARY_PATH = "traceLibraryPath"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private TraceLibraryPathWizardPage traceLibraryPathPage;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    public NewLTTngProjectWizard() {
        this(Messages.NewProjectWizard_Title, Messages.NewProjectWizard_Description);
    }

    public NewLTTngProjectWizard(String title, String desc) {
        super(title, desc);
    }

    // ------------------------------------------------------------------------
    // NewProjectWizard
    // ------------------------------------------------------------------------

    @Override
    public void addPages() {
        super.addPages();
        traceLibraryPathPage = new TraceLibraryPathWizardPage(Messages.NewProjectWizard_Title);
        traceLibraryPathPage.setTitle(Messages.TraceLibraryPathWizardPage_Title);
        traceLibraryPathPage.setDescription(Messages.TraceLibraryPathWizardPage_Description);
        addPage(traceLibraryPathPage);
    }

    @Override
    public boolean performFinish() {
        // Create the tracing project
        super.performFinish();

        // Add the LTTng nature
        try {
            IProjectDescription description = fProject.getDescription();
            description.setNatureIds(new String[] { TmfProjectNature.ID, LTTngProjectNature.ID });
            fProject.setDescription(description, null);
        } catch (CoreException e) {
        }

        // Set the library path
        String traceLibraryPath = traceLibraryPathPage.getPath();
        if (traceLibraryPath != null) {
            return TraceHelper.setProjectPreference(fProject, TRACE_LIBRARY_PATH, traceLibraryPath);
        }

        return true;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    public IProject getProject() {
        return fProject;
    }

}