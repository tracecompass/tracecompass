/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.internal.tmf.core.TmfProjectNature;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

/**
 * <b><u>NewTmfProjectWizard</u></b>
 * <p>
 */
public class NewTmfProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

    private final String fTtitle;
    private final String fDescription;

    protected NewTmfProjectMainWizardPage fMainPage;
    protected String fProjectName;
    protected URI fProjectLocation;
    protected IConfigurationElement fConfigElement;

    protected IProject fProject;

    /**
     * 
     */
    public NewTmfProjectWizard() {
        this(Messages.NewProjectWizard_DialogHeader, Messages.NewProjectWizard_DialogMessage);
    }

    /**
     * @param title
     * @param desc
     */
    public NewTmfProjectWizard(String title, String desc) {
        super();
        setDialogSettings(TmfUiPlugin.getDefault().getDialogSettings());
        setNeedsProgressMonitor(true);
        setForcePreviousAndNextButtons(true);
        setWindowTitle(title);
        fTtitle = title;
        fDescription = desc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        fMainPage = new NewTmfProjectMainWizardPage(Messages.NewProjectWizard_DialogHeader);
        fMainPage.setTitle(fTtitle);
        fMainPage.setDescription(fDescription);
        addPage(fMainPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performCancel()
     */
    @Override
    public boolean performCancel() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        fProjectName = fMainPage.getProjectName();
        fProjectLocation = fMainPage.useDefaults() ? null : fMainPage.getLocationURI();
        fProject = createProject(fProjectName, fProjectLocation, new NullProgressMonitor());
        BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
        return true;
    }

    /**
     * @param projectName
     * @param projectLocation
     * @param monitor
     * @return
     */
    private IProject createProject(String projectName, URI projectLocation, IProgressMonitor monitor) {

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        IProject project = root.getProject(projectName);

        try {
            if (!project.exists()) {
                IProjectDescription description = workspace.newProjectDescription(project.getName());
                if (projectLocation != null)
                    description.setLocationURI(projectLocation);
                project.create(description, monitor);
            }

            if (!project.isOpen())
                project.open(monitor);

            IProjectDescription description = project.getDescription();
            description.setNatureIds(new String[] { TmfProjectNature.ID });
            project.setDescription(description, null);

            IFolder folder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);
            if (!folder.exists())
                folder.create(true, true, null);

            folder = project.getFolder(TmfExperimentFolder.EXPER_FOLDER_NAME);
            if (!folder.exists())
                folder.create(true, true, null);

            return project;
        } catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // INewWizard
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench iworkbench, IStructuredSelection istructuredselection) {
    }

    // ------------------------------------------------------------------------
    // IExecutableExtension
    // ------------------------------------------------------------------------

    @Override
    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
        fConfigElement = config;
    }

}
