/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.project.dialogs;

import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.project.TmfProjectNature;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfProjectNode;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * <b><u>NewProjectWizard</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class NewProjectWizard extends BasicNewResourceWizard {

    private String fTtitle;
    private String fDescription;

    protected NewProjectMainWizardPage fMainPage;
    protected String fProjectName;
    protected URI fProjectLocation;
    protected IConfigurationElement fConfigElement;

    protected IProject fProject;

    /**
     * 
     */
    public NewProjectWizard() {
        this(Messages.NewProjectWizard_DialogHeader, Messages.NewProjectWizard_DialogMessage);
    }

    /**
     * @param title
     * @param desc
     */
    public NewProjectWizard(String title, String desc) {
        super();
        setDialogSettings(TmfUiPlugin.getDefault().getDialogSettings());
        setNeedsProgressMonitor(true);
        setForcePreviousAndNextButtons(true);
        setWindowTitle(title);
        fTtitle = title;
        fDescription = desc;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        fMainPage= new NewProjectMainWizardPage(Messages.NewProjectWizard_DialogHeader);
        fMainPage.setTitle(fTtitle);
        fMainPage.setDescription(fDescription);
        addPage(fMainPage);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performCancel()
     */
    @Override
    public boolean performCancel() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        fProjectName = fMainPage.getProjectName();
        fProjectLocation = fMainPage.useDefaults() ? null : fMainPage.getLocationURI();
        fProject = createProject(fProjectName, fProjectLocation, new NullProgressMonitor());
        return true;
    }

    public IProject getProject() {
    	return fProject;
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
            description.setNatureIds(new String[] { TmfProjectNature.ID } );
            project.setDescription(description, null);

            IFolder folder = project.getFolder(TmfProjectNode.TRACE_FOLDER_NAME);
            if (!folder.exists())
                folder.create(true, true, null);

            folder = project.getFolder(TmfProjectNode.EXPER_FOLDER_NAME);
            if (!folder.exists())
                folder.create(true, true, null);

            return project;
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
        return null;
    }

}