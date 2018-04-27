/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.project.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.tmf.core.TmfProjectNature;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectModelPreferences;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for the TmfProjectRegistry class.
 */
public class TmfProjectRegistryTest {

    private static final String SOME_PROJECT_NAME = "SomeProject";
    private static final String NEW_PROJECT_NAME = "SomeProject2";
    private static final String TRACING_PROJECT_NAME = "SomeTracingProject";
    private static final String SOME_OTHER_PROJECT_NAME = "SomeOtherProject";
    private static final String TRACES_LABEL_TEXT = "Traces [0]";
    private static final String EXPERIMENTS_LABEL_TEXT = "Experiments [0]";

    private static final String TRACING_NATURE_TRACES_PATH = new Path("Traces").toOSString();
    private static final String TRACING_NATURE_EXPERIMENTS_PATH = new Path("Experiments").toOSString();
    private static final String TRACING_NATURE_SUPPL_PATH = new Path(".tracing").toOSString();
    private static final String HIDDEN_TRACECOMPASS_DIRECTORY = ".tracecompass";

    private static final String SHADOW_PROJECT_NAME = ".tracecompass-SomeProject";
    private static final String NEW_SHADOW_PROJECT_NAME = ".tracecompass-SomeProject2";

    private static IWorkspaceRoot fWorkspaceRoot;
    private static IProject fSomeProject;
    private static IProject fShadowSomeProject;
    private static IProject fSomeOtherProject;

    /**
     * Perform test class initialization.
     *
     * @throws CoreException if exception happens
     */
    @BeforeClass
    public static void init() throws CoreException {
        IProgressMonitor progressMonitor = new NullProgressMonitor();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Create C project
        fWorkspaceRoot = workspace.getRoot();
        fSomeProject = fWorkspaceRoot.getProject(SOME_PROJECT_NAME);
        fSomeProject.create(progressMonitor);
        fSomeProject.open(progressMonitor);
        IProjectDescription description = fSomeProject.getDescription();
        description.setNatureIds(new String[] { "org.eclipse.cdt.core.cnature" });
        fSomeProject.setDescription(description, null);
        fSomeProject.open(progressMonitor);
        fShadowSomeProject = fWorkspaceRoot.getProject(SHADOW_PROJECT_NAME);

        // Create generic project (no project nature)
        fSomeOtherProject  = fWorkspaceRoot.getProject(SOME_OTHER_PROJECT_NAME);
        fSomeOtherProject.create(progressMonitor);
        fSomeOtherProject.open(progressMonitor);
    }

    /**
     * Clean-ups
     *
     * @throws CoreException if exception happens
     */
    @AfterClass
    public static void tearDown() throws CoreException {
        if (fSomeProject != null) {
            fSomeProject.delete(true, true, new NullProgressMonitor());
        }

        if (fSomeOtherProject != null) {
            fSomeOtherProject.delete(true, true, new NullProgressMonitor());
        }
    }

    /**
     * Test {@link TmfProjectRegistry#addTracingNature(IProject, IProgressMonitor)}.
     * Also, test {@link TmfNavigatorContentProvider} in added project nature case.
     *
     * @throws Exception if exception happens
     */
    @Test
    public void testAddingTracingNature() throws Exception {
        IProgressMonitor progressMonitor = new NullProgressMonitor();
        IProjectDescription desc = fSomeProject.getDescription();
        assertFalse(desc.hasNature(TmfProjectNature.ID));

        TmfProjectRegistry.addTracingNature(fSomeProject, progressMonitor);
        desc = fSomeProject.getDescription();
        assertTrue(desc.hasNature(TmfProjectNature.ID));
        WaitUtils.waitUntil(project -> project.exists(), fShadowSomeProject, "Shadow project did not get created");

        IFolder hiddenTcFile = fSomeProject.getFolder(HIDDEN_TRACECOMPASS_DIRECTORY);
        assertTrue(hiddenTcFile.exists());

        // Verify shadow project
        TmfProjectElement projectElement = TmfProjectRegistry.getProject(fSomeProject, true);
        TmfProjectElement projectElement2 = TmfProjectRegistry.getProject(fShadowSomeProject, true);
        assertEquals(projectElement2, projectElement);
        assertEquals(TmfProjectModelPreferences.getProjectModelLabel(), projectElement.getLabelText());
        assertEquals(TmfProjectModelPreferences.getProjectModelIcon(), projectElement.getIcon());

        // Supplementary folder
        IFolder supplFolder = projectElement.getSupplementaryFolder();
        IPath path = supplFolder.getProjectRelativePath();
        assertEquals(TRACING_NATURE_SUPPL_PATH, path.toOSString());

        // Verify content provider for project and project model element
        TmfNavigatorContentProvider contentProvider = new TmfNavigatorContentProvider();
        assertEquals(fShadowSomeProject.getParent(), contentProvider.getParent(fSomeProject));
        assertEquals(fSomeProject, contentProvider.getParent(projectElement));
        Object[] children = contentProvider.getChildren(fSomeProject);
        assertEquals(1, children.length);
        assertEquals(projectElement, children[0]);

        // Verify content provider for shadow project if project is closed
        fSomeProject.close(progressMonitor);
        WaitUtils.waitUntil(project -> (project.exists() && !project.isOpen()), fShadowSomeProject, "Shadow project did not get closed");

        fSomeProject.open(progressMonitor);
        WaitUtils.waitUntil(project -> project.isOpen(), fShadowSomeProject, "Shadow project did not get opened");
        // Bug 534157: Wait until open operation is done and project description is set
        WaitUtils.waitUntil(project -> fSomeProject.getLocation().isPrefixOf(project.getLocation()), fShadowSomeProject, "Shadow project location did not get set");
        // Traces folder
        TmfTraceFolder traceFolder = projectElement.getTracesFolder();
        assertNotNull(traceFolder);
        String name = traceFolder.getLabelText();
        assertEquals(TRACES_LABEL_TEXT, name);
        path = traceFolder.getResource().getProjectRelativePath();
        assertEquals(TRACING_NATURE_TRACES_PATH, path.toOSString());

        IFile file = traceFolder.getResource().getFile("trace");
        File tmpFile = file.getLocation().toFile();
        tmpFile.createNewFile();
        traceFolder.getResource().refreshLocal(IResource.DEPTH_ZERO, progressMonitor);
        traceFolder.refresh();
        assertEquals(projectElement, contentProvider.getParent(traceFolder));

        // Experiments folder
        TmfExperimentFolder expFolder = projectElement.getExperimentsFolder();
        assertNotNull(expFolder);
        name = expFolder.getLabelText();
        assertEquals(EXPERIMENTS_LABEL_TEXT, name);
        path = expFolder.getResource().getProjectRelativePath();
        assertEquals(TRACING_NATURE_EXPERIMENTS_PATH, path.toOSString());

        IFolder folder = expFolder.getResource().getFolder("exp");
        folder.create(true, true, progressMonitor);
        expFolder.getResource().refreshLocal(IResource.DEPTH_ZERO, progressMonitor);
        expFolder.refresh();
        assertEquals(projectElement, contentProvider.getParent(expFolder));

        // Verify children of project element
        children = contentProvider.getChildren(projectElement);
        assertEquals(2, children.length);
        List<Object> childrenList = new ArrayList<>();
        childrenList.addAll(Arrays.asList(children[0], children[1]));

        assertTrue(childrenList.contains(expFolder));
        assertTrue(childrenList.contains(traceFolder));

        // Delete Traces and Experiment directory
        traceFolder.getResource().delete(true, progressMonitor);
        expFolder.getResource().delete(true, progressMonitor);

        fSomeProject.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
        projectElement.refresh();
        traceFolder = projectElement.getTracesFolder();
        assertNotNull(traceFolder);
        assertFalse(traceFolder.getResource().exists());
        expFolder = projectElement.getExperimentsFolder();
        assertNotNull(expFolder);
        assertFalse(expFolder.getResource().exists());

        TmfProjectRegistry.addTracingNature(fSomeProject, progressMonitor);
        projectElement.refresh();

        // Verify that traces and experiments are back
        traceFolder = projectElement.getTracesFolder();
        assertNotNull(traceFolder);
        assertTrue(traceFolder.getResource().exists());
        expFolder = projectElement.getExperimentsFolder();
        assertNotNull(expFolder);
        assertTrue(expFolder.getResource().exists());

        // Verify that after closing of the shadow project the parent doesn't
        // show the tracing project folder
        fShadowSomeProject.close(progressMonitor);
        children = contentProvider.getChildren(fSomeProject);
        assertEquals(0, children.length);

        // Verify that after opening of the shadow project the parent shows
        // the tracing project folder
        fShadowSomeProject.open(progressMonitor);
        children = contentProvider.getChildren(fSomeProject);
        assertEquals(1, children.length);
        assertEquals(projectElement, children[0]);

        // Verify that after deletion the content provider returns 0 elements
        fShadowSomeProject.delete(true, progressMonitor);
        children = contentProvider.getChildren(fSomeProject);
        assertEquals(0, children.length);

        // Verify that shadow project is back
        TmfProjectRegistry.addTracingNature(fSomeProject, progressMonitor);
        projectElement.refresh();
        assertEquals(fShadowSomeProject.getParent(), contentProvider.getParent(fSomeProject));
        assertEquals(fSomeProject, contentProvider.getParent(projectElement));
        children = contentProvider.getChildren(fSomeProject);
        assertEquals(1, children.length);
        assertEquals(projectElement, children[0]);

        // Rename project
        fSomeProject.move(new Path(NEW_PROJECT_NAME), true, progressMonitor);
        IProject shadowProject = fWorkspaceRoot.getProject(NEW_SHADOW_PROJECT_NAME);
        WaitUtils.waitUntil(project -> project.exists(), shadowProject, "Shadow project did get moved");

        // Verify that after deletion of the parent project the shadow project is removed from the workspace
        IProject newProject = fWorkspaceRoot.getProject(NEW_PROJECT_NAME);
        newProject.delete(false, true, progressMonitor);
        shadowProject = fWorkspaceRoot.getProject(NEW_SHADOW_PROJECT_NAME);
        WaitUtils.waitUntil(project -> !project.exists(), shadowProject, "Shadow project did not get deleted");
    }

    /**
     * Test {@link TmfProjectRegistry#addTracingNature(IProject, IProgressMonitor)}
     *
     * @throws Exception if exception happens
     */
    @Test
    public void testAddingTracingNatureNotAllowed() throws Exception {
        IProgressMonitor progressMonitor = new NullProgressMonitor();
        IProjectDescription desc = fSomeOtherProject.getDescription();
        assertFalse(desc.hasNature(TmfProjectNature.ID));

        TmfProjectRegistry.addTracingNature(fSomeOtherProject, progressMonitor);
        desc = fSomeOtherProject.getDescription();
        assertFalse(desc.hasNature(TmfProjectNature.ID));

        IFile hiddenTcFile = fSomeOtherProject.getFile(HIDDEN_TRACECOMPASS_DIRECTORY);
        assertFalse(hiddenTcFile.exists());
    }

    /**
     * Test
     * {@link TmfProjectRegistry#createProject(String, java.net.URI, IProgressMonitor)}
     * Also, test {@link TmfNavigatorContentProvider} in normal project nature case.
     *
     * @throws Exception if exception happens
     */
    @Test
    public void testCreateTracingProject() throws Exception {
        IProgressMonitor progressMonitor = new NullProgressMonitor();
        IProject project = TmfProjectRegistry.createProject(TRACING_PROJECT_NAME, null, progressMonitor);
        IProjectDescription desc = project.getDescription();
        assertTrue(desc.hasNature(TmfProjectNature.ID));

        TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);

        // Supplementary folder
        IFolder supplFolder = projectElement.getSupplementaryFolder();
        IPath path = supplFolder.getProjectRelativePath();
        assertEquals(TRACING_NATURE_SUPPL_PATH, path.toOSString());

        // Verify content provider for project and project model element
        TmfNavigatorContentProvider contentProvider = new TmfNavigatorContentProvider();
        assertEquals(project.getParent(), contentProvider.getParent(project));
        Object[] children = contentProvider.getChildren(project);
        assertEquals(2, children.length);
        List<Object> childrenList = new ArrayList<>();
        childrenList.addAll(Arrays.asList(children[0], children[1]));

        // Traces folder
        TmfTraceFolder traceFolder = projectElement.getTracesFolder();
        assertNotNull(traceFolder);
        String name = traceFolder.getLabelText();
        assertEquals(TRACES_LABEL_TEXT, name);
        path = traceFolder.getResource().getProjectRelativePath();
        assertEquals(TRACING_NATURE_TRACES_PATH, path.toOSString());

        IFile file = traceFolder.getResource().getFile("trace");
        File tmpFile = file.getLocation().toFile();
        tmpFile.createNewFile();
        traceFolder.getResource().refreshLocal(IResource.DEPTH_ZERO, progressMonitor);
        traceFolder.refresh();
        assertEquals(project, contentProvider.getParent(traceFolder));

        // Experiments folder
        TmfExperimentFolder expFolder = projectElement.getExperimentsFolder();
        assertNotNull(expFolder);
        name = expFolder.getLabelText();
        assertEquals(EXPERIMENTS_LABEL_TEXT, name);
        path = expFolder.getResource().getProjectRelativePath();
        assertEquals(TRACING_NATURE_EXPERIMENTS_PATH, path.toOSString());

        IFolder folder = expFolder.getResource().getFolder("exp");
        folder.create(true, true, progressMonitor);
        expFolder.getResource().refreshLocal(IResource.DEPTH_ZERO, progressMonitor);
        expFolder.refresh();
        assertEquals(project, contentProvider.getParent(expFolder));

        assertTrue(childrenList.contains(expFolder));
        assertTrue(childrenList.contains(traceFolder));

        // Delete Traces and Experiment directory
        traceFolder.getResource().delete(true, progressMonitor);
        expFolder.getResource().delete(true, progressMonitor);

        projectElement.refresh();
        traceFolder = projectElement.getTracesFolder();
        assertNotNull(traceFolder);
        assertFalse(traceFolder.getResource().exists());
        expFolder = projectElement.getExperimentsFolder();
        assertNotNull(expFolder);
        assertFalse(expFolder.getResource().exists());

    }
}