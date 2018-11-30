/*******************************************************************************
 * Copyright (c) 2011, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added project creation utility
 *   Patrick Tasse - Refactor resource change listener
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.internal.tmf.ui.project.model.TmfProjectModelHelper;
import org.eclipse.tracecompass.tmf.core.TmfProjectNature;
import org.eclipse.tracecompass.tmf.core.io.ResourceUtil;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Factory class storing TMF tracing projects and creating TMF project model elements.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfProjectRegistry implements IResourceChangeListener {

    // Create the singleton instance
    private static final TmfProjectRegistry INSTANCE = new TmfProjectRegistry();

    // The map of project resource to project model elements
    private static final Map<IProject, TmfProjectElement> registry = new HashMap<>();

    private static final Queue<TmfTraceElement> promptQueue = new ArrayDeque<>();

    private TmfProjectRegistry() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
        TmfSignalManager.register(this);
    }

    /**
     * Initializes the project registry
     *
     * @since 3.3
     */
    public static void init() {
        /* static variables are initialized */
    }

    /**
     * Disposes the project registry
     *
     * @since 2.3
     */
    public static void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(INSTANCE);
        TmfSignalManager.deregister(INSTANCE);
        registry.values().forEach(TmfProjectElement::dispose);
    }

    /**
     * Get the project model element for a project resource
     * @param project the project resource
     * @return the project model element or null if it does not exist
     */
    public static synchronized TmfProjectElement getProject(IProject project) {
        try {
            if (project.hasNature(TmfProjectNature.ID)) {
                return getProject(project, true);
            }
        } catch (CoreException e) {
            /* ignore */
        }
        return null;
    }

    /**
     * Get the project model element for a project resource
     * @param aProject the project resource
     * @param force a flag controlling whether a new project should be created if it doesn't exist
     * @return the project model element
     */
    public static synchronized TmfProjectElement getProject(IProject aProject, boolean force) {
        IProject project = aProject;
        if (TmfProjectElement.showProjectRoot(project)) {
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject shadowProject = workspace.getRoot().getProject(TmfProjectModelHelper.getShadowProjectName(project.getName()));
            if (shadowProject.exists()) {
                project = shadowProject;
            }
        }
        TmfProjectElement element = registry.get(project);
        if (element == null && force) {
            element = new TmfProjectElement(project.getName(), project, null);
            registry.put(project, element);
            // force the model to be populated
            element.refreshChildren();
        }
        return element;
    }

    /**
     * Utility method to create a tracing project.
     *
     * @param projectName
     *          - A project name
     * @param projectLocation
     *          - A project location URI. Use null for default location (which is workspace).
     * @param monitor
     *          - A progress monitor
     * @return the IProject object or null
     */
    public static IProject createProject(String projectName, final URI projectLocation, IProgressMonitor monitor) {

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        final IProject project = root.getProject(projectName);
        WorkspaceModifyOperation action = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor progressMonitor) throws CoreException, InvocationTargetException, InterruptedException {
                if (!project.exists()) {
                    IProjectDescription description = workspace.newProjectDescription(project.getName());
                    if (projectLocation != null) {
                        description.setLocationURI(projectLocation);
                    }
                    project.create(description, progressMonitor);
                }

                if (!project.isOpen()) {
                    project.open(progressMonitor);
                }

                IProjectDescription description = project.getDescription();
                description.setNatureIds(new String[] { TmfProjectNature.ID });
                project.setDescription(description, null);

                TmfProjectElement.createFolderStructure(project);
            }
        };
        try {
            action.run(monitor);
        } catch (InvocationTargetException e) {
            Activator.getDefault().logError("Error creating TMF project " + project.getName(), e); //$NON-NLS-1$
        } catch (InterruptedException e) {
        }
        return project;
    }

    /**
     * Add a the tracing nature to the given project resource
     *
     * @param project
     *            the project resource
     * @param monitor
     *          - A progress monitor
     * @since 3.2
     */
    public static void addTracingNature(IProject project, IProgressMonitor monitor) {
        WorkspaceModifyOperation action = new WorkspaceModifyOperation() {
            @Override
            protected void execute(IProgressMonitor progressMonitor) throws CoreException, InvocationTargetException, InterruptedException {
                if (!project.isOpen()) {
                    project.open(progressMonitor);
                }
                IProjectDescription description = project.getDescription();
                boolean hasNature = description.hasNature(TmfProjectNature.ID);
                String[] natures = description.getNatureIds();
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                if (workspace == null) {
                    return;
                }
                if (!hasNature && natures.length > 0) {
                    // Only add nature if project doesn't have the tracing nature
                    String[] newNatures = new String[natures.length + 1];
                    System.arraycopy(natures, 0, newNatures, 0, natures.length);

                    // add the tracing nature ID
                    newNatures[natures.length] = TmfProjectNature.ID;

                    // validate the natures
                    IStatus status = workspace.validateNatureSet(newNatures);

                    // only apply new nature, if the status is ok
                    if (status.isOK()) {
                        description.setNatureIds(newNatures);
                        project.setDescription(description, null);
                    } else {
                        Activator.getDefault().getLog().log(status);
                    }
                }

                // Create shadow project
                description = project.getDescription();
                hasNature = description.hasNature(TmfProjectNature.ID);
                natures = description.getNatureIds();
                if (hasNature && natures.length > 1) {
                    String shadowProjectName = TmfProjectModelHelper.getShadowProjectName(project.getName());
                    IProject shadowProject = workspace.getRoot().getProject(shadowProjectName);

                    if (shadowProject.exists() && !shadowProject.isAccessible()) {
                        /*
                         * If a shadow project is not accessible, i.e. closed or deleted from file
                         * system while not being removed from the workspace, then remove the project
                         * from workspace in order to be able to successfully re-create the shadow
                         * project afterwards.
                         */
                        shadowProject.delete(false, true, progressMonitor);
                    }

                    if (!shadowProject.exists()) {
                        // Get or create shadow project
                        IFolder shadowProjectFolder = TmfProjectElement.createFolderStructure(project, null);
                        shadowProject = createProject(shadowProjectName, shadowProjectFolder.getLocationURI(), progressMonitor);
                        shadowProjectFolder.refreshLocal(IResource.DEPTH_INFINITE, progressMonitor);
                    }

                    if (!shadowProject.isOpen()) {
                        shadowProject.open(progressMonitor);
                    }
                    // Create project directory in shadow project
                    TmfProjectElement.createFolderStructure(project, shadowProject);

                    // If the traces or experiment or supplementary folder is null
                    // then refresh children to make sure that the created folder(s) are
                    // available
                    TmfProjectElement tmfProject = TmfProjectRegistry.getProject(project, false);
                    if ((tmfProject != null) &&
                            ((tmfProject.getTracesFolder() == null) || (tmfProject.getExperimentsFolder() == null) || (tmfProject.getSupplementaryFolder() == null))) {
                        tmfProject.refreshChildren();
                    }
                }
            }
        };
        try {
            action.run(monitor);
        } catch (InvocationTargetException e) {
            Activator.getDefault().logError("Error adding tracing nature to project " + project.getName(), e); //$NON-NLS-1$
        } catch (InterruptedException e) {
        }
    }

    // ------------------------------------------------------------------------
    // IResourceChangeListener
    // ------------------------------------------------------------------------

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            if (event.getResource() instanceof IProject) {
                IProject aProject = (IProject) event.getResource();
                try {
                    boolean isShadowProject = TmfProjectModelHelper.isShadowProject(Objects.requireNonNull(aProject));
                    if (aProject.isAccessible() && aProject.hasNature(TmfProjectNature.ID)) {
                        // A tracing project is being deleted.
                        IProject project = aProject;
                        if (!isShadowProject) {
                            // If a shadow project exists, use the shadow project.
                            IProject shadowProject = TmfProjectModelHelper.getShadowProject(project);
                            if (shadowProject.exists()) {
                                project = shadowProject;
                            }
                        }

                        TmfProjectElement tmfProjectElement = registry.get(project);
                        if (tmfProjectElement != null) {
                            // Close all traces editors
                            TmfTraceFolder tracesFolder = tmfProjectElement.getTracesFolder();
                            if (tracesFolder != null) {
                                final List<TmfTraceElement> traces = tracesFolder.getTraces();
                                if (!traces.isEmpty()) {
                                    // Close editors in UI Thread
                                    Display.getDefault().syncExec(() -> traces.forEach(TmfTraceElement::closeEditors));
                                }
                            }
                        }

                        boolean removeProjectElement = (event.getType() == IResourceChangeEvent.PRE_CLOSE);

                        // If parent project was deleted and a shadow project exists,
                        if (!isShadowProject) {
                            final IProject shadowProject = TmfProjectModelHelper.getShadowProject(aProject);
                            if (shadowProject.exists()) {
                                if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
                                    Display.getDefault().asyncExec(() -> {
                                        try {
                                            shadowProject.delete(false, true, null);
                                        } catch (CoreException e) {
                                            Activator.getDefault().logError("Error deleting shadow project when is parent project being deleted " + shadowProject.getName(), e); //$NON-NLS-1$
                                        }
                                    });
                                } else {
                                    Display.getDefault().asyncExec(() -> {
                                        try {
                                            shadowProject.close(null);
                                        } catch (CoreException e) {
                                            Activator.getDefault().logError("Error removing shadow project when is parent project being closed " + shadowProject.getName(), e); //$NON-NLS-1$
                                        }
                                    });
                                }
                                removeProjectElement = false;
                            }
                        }

                        if (removeProjectElement) {
                            // IResourceChangeEvent.PRE_CLOSE of a non-shadow project
                            TmfProjectElement projectElement = registry.remove(project);
                            if (projectElement != null) {
                                projectElement.dispose();
                            }
                        }
                    }
                } catch (CoreException e) {
                    Activator.getDefault().logError("Error handling resource change event for " + aProject.getName(), e); //$NON-NLS-1$
                }
            }
        } else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
                if (delta.getResource() instanceof IProject) {
                    IProject project = (IProject) delta.getResource();

                    try {
                        if (delta.getKind() == IResourceDelta.REMOVED) {
                            // Handle project deleted (it is also closed)
                            TmfProjectElement projectElement = registry.remove(project);

                            if (projectElement != null) {
                                projectElement.dispose();
                            }

                            // Refresh viewer for parent project
                            IProject parentProject = TmfProjectModelHelper.getProjectFromShadowProject(project);
                            if (parentProject != null) {
                                new TmfProjectElement(parentProject.getName(), parentProject, null).refreshViewer();
                            }
                        } else if (!project.isOpen() || !project.hasNature(TmfProjectNature.ID)) {
                            // Project is closed or does not have tracing nature, skip it
                            continue;
                        } else if ((delta.getKind() == IResourceDelta.ADDED) && (delta.getFlags() & IResourceDelta.MOVED_FROM) != 0 ) {
                            // Handle project moved - it covers only if it's not shadow project
                            handleProjectMoved(project);
                        } else if (delta.getKind() == IResourceDelta.CHANGED) {
                            // If shadow project exists, handle resource change in the shadow project
                            if (TmfProjectModelHelper.shadowProjectAccessible(project)) {
                                handleParentProjectRefresh(project);
                                continue;
                            } else if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
                                handleParentProjectOpen(project);
                                continue;
                            }

                            // Handle resource change in the relevant project
                            IProject parentProject = TmfProjectModelHelper.getProjectFromShadowProject(project);
                            Set<IResource> resourcesToRefresh = new HashSet<>();
                            Map<IResource, Integer> resourceFlags = new HashMap<>();
                            delta.accept(visited -> {
                                resourceFlags.put(visited.getResource(), visited.getFlags());
                                if ((visited.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.LOCAL_CHANGED)) != 0) {
                                    // visited resource content has changed, refresh it
                                    resourcesToRefresh.add(visited.getResource());
                                } else if ((visited.getKind() != IResourceDelta.CHANGED)) {
                                    IResource parent = visited.getResource().getParent();
                                    if (((visited.getFlags() & (IResourceDelta.MOVED_FROM | IResourceDelta.MOVED_TO)) == 0)) {
                                        // visited resource is added or removed, refresh it and its parent
                                        resourcesToRefresh.add(visited.getResource());
                                        resourcesToRefresh.add(parent);
                                    } else {
                                        Integer parentFlags = resourceFlags.get(parent);
                                        if (parentFlags != null && ((parentFlags & (IResourceDelta.MOVED_FROM | IResourceDelta.MOVED_TO)) == 0)) {
                                            // visited resource is moved, refresh its parent if not moved itself
                                            resourcesToRefresh.add(parent);
                                        }
                                    }
                                }
                                return true;
                            });
                            Set<ITmfProjectModelElement> elementsToRefresh = new HashSet<>();
                            for (IResource resource : resourcesToRefresh) {
                                ITmfProjectModelElement element = findElement(resource, false);
                                if (element != null) {
                                    elementsToRefresh.add(element);
                                }
                            }
                            Set<TmfTraceElement> deletedTraces = new HashSet<>();
                            Set<TmfTraceElement> changedTraces = new HashSet<>();
                            Iterator<ITmfProjectModelElement> iterator = elementsToRefresh.iterator();
                            while (iterator.hasNext()) {
                                ITmfProjectModelElement element = iterator.next();
                                if (element instanceof TmfTraceElement) {
                                    TmfTraceElement trace = (TmfTraceElement) element;
                                    IResource resource = trace.getResource();
                                    if (isHandleDeleted(resource, parentProject)) {
                                        deletedTraces.add(trace);
                                    } else {
                                        changedTraces.add(trace);
                                    }
                                }
                                for (ITmfProjectModelElement parent : elementsToRefresh) {
                                    // remove element if any of its parents is included
                                    if (parent.getPath().isPrefixOf(element.getPath()) && !parent.equals(element)) {
                                        iterator.remove();
                                        break;
                                    }
                                }
                            }
                            if (!deletedTraces.isEmpty() || !changedTraces.isEmpty()) {
                                Job.createSystem("TmfProjectRegistry.resourceChanged Job", monitor -> { //$NON-NLS-1$
                                    try {
                                        if (!deletedTraces.isEmpty()) {
                                            Display.getDefault().syncExec(() -> {
                                                for (TmfTraceElement deletedTrace : deletedTraces) {
                                                    deletedTrace.closeEditors();
                                                }
                                            });
                                        }
                                        ResourcesPlugin.getWorkspace().run((ICoreRunnable) (mon -> {
                                            for (TmfTraceElement deletedTrace : deletedTraces) {
                                                handleTraceDeleted(deletedTrace);
                                            }
                                            for (TmfTraceElement changedTrace : changedTraces) {
                                                handleTraceContentChanged(changedTrace);
                                            }
                                        }), project, IWorkspace.AVOID_UPDATE, null);
                                    } catch (CoreException e) {
                                        Activator.getDefault().logError("Error handling resource change event for " + project.getName(), e); //$NON-NLS-1$
                                    }
                                    if (parentProject != null) {
                                        new TmfProjectElement(parentProject.getName(), parentProject, null).refreshViewer();
                                    }
                                }).schedule();
                            }
                            for (ITmfProjectModelElement element : elementsToRefresh) {
                                element.refresh();
                            }
                            TmfProjectElement projectElement = registry.get(project);
                            if (projectElement != null) {
                                // refresh only the viewer for the affected project
                                projectElement.refreshViewer();
                            }
                        }
                    } catch (CoreException e) {
                        Activator.getDefault().logError("Error handling resource change event for " + project.getName(), e); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    private static boolean isHandleDeleted(IResource resource, IProject parentProject) {
        IPath path = ResourceUtil.getLocation(resource);
        if (path == null) {
            return false;
        }
        return ((parentProject != null && parentProject.getLocation().isPrefixOf(path)) // link from shadow project to parent project
              || resource.getWorkspace().getRoot().getLocation().isPrefixOf(path)) && // link within the workspace
                !path.toFile().exists();
    }

    // -------------------------------------------------------
    // Signal handlers
    // -------------------------------------------------------

    /**
     * Handler for the Trace Opened signal
     *
     * @param signal
     *            The incoming signal
     * @since 3.1
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        ITmfProjectModelElement element = findElement(signal.getTrace().getResource(), true);
        if (element != null) {
            element.refresh();
            if (element instanceof TmfExperimentElement) {
                TmfExperimentElement experiment = (TmfExperimentElement) element;
                for (TmfTraceElement trace : experiment.getTraces()) {
                    trace.getElementUnderTraceFolder().refresh();
                }
            }
        }
    }

    // -------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------

    /**
     * Finds an existing project model element that matches the given resource.
     * Elements are not created if they do not already exist. If an exact match is
     * not found and <code>exact</code> is false, returns the nearest existing
     * parent element.
     *
     * @param resource
     *            the resource
     * @param exact
     *            if an exact match is not found, returns <code>null</code> if true,
     *            or the nearest parent if false
     * @return the element, or <code>null</code>
     * @since 3.1
     */
    public static ITmfProjectModelElement findElement(IResource resource, boolean exact) {
        if ((resource == null) || (resource.getProject() == null)) {
            return null;
        }
        ITmfProjectModelElement element = getProject(resource.getProject());
        if (element == null) {
            return null;
        }
        for (String segment : resource.getProjectRelativePath().segments()) {
            List<ITmfProjectModelElement> children = element.getChildren();
            boolean match = false;
            for (ITmfProjectModelElement child : children) {
                IResource childResource = child.getResource();
                if (resource.equals(childResource)) {
                    return child;
                }
                if (childResource != null && segment.equals(childResource.getName())) {
                    element = child;
                    match = true;
                    break;
                }
            }
            if (!match) {
                /* do not return project as nearest parent */
                return exact ? null : element instanceof TmfProjectElement ? null : element;
            }
        }
        return element;
    }

    private static void handleTraceContentChanged(TmfTraceElement traceElement) {
        boolean opened = false;
        for (ITmfTrace openedTrace : TmfTraceManager.getInstance().getOpenedTraces()) {
            for (ITmfTrace trace : TmfTraceManager.getTraceSet(openedTrace)) {
                if (traceElement.getResource().equals(trace.getResource())) {
                    opened = true;
                    break;
                }
            }
        }
        if (!opened) {
            traceElement.deleteSupplementaryResources();
            return;
        }
        synchronized (promptQueue) {
            if (!promptQueue.isEmpty()) {
                /* already prompting the user */
                if (!promptQueue.contains(traceElement)) {
                    promptQueue.add(traceElement);
                }
                return;
            }
            promptQueue.add(traceElement);
        }
        Display.getDefault().asyncExec(() -> {
            TmfTraceElement prompting = traceElement;
            while (prompting != null) {
                boolean always = Activator.getDefault().getPreferenceStore().getBoolean(ITmfUIPreferences.ALWAYS_CLOSE_ON_RESOURCE_CHANGE);
                if (always) {
                    prompting.closeEditors();
                    deleteSupplementaryResources(prompting);
                } else {
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

                    MessageDialog messageDialog = new MessageDialog(shell,
                            Messages.TmfProjectRegistry_TraceChangedDialogTitle,
                            null,
                            NLS.bind(Messages.TmfProjectRegistry_TraceChangedDialogMessage, prompting.getElementPath()),
                            MessageDialog.QUESTION,
                            2, // Always is the default.
                            IDialogConstants.NO_LABEL,
                            IDialogConstants.YES_LABEL,
                            Messages.TmfProjectRegistry_Always);
                    int returnCode = messageDialog.open();
                    if (returnCode >= 1) {
                        // yes or always.
                        prompting.closeEditors();
                        deleteSupplementaryResources(prompting);
                    }
                    if (returnCode == 2) {
                        // remember to always delete supplementary files and close editors
                        Activator.getDefault().getPreferenceStore().setValue(ITmfUIPreferences.ALWAYS_CLOSE_ON_RESOURCE_CHANGE, true);
                    }
                }
                /*
                 * Poll at end of the loop: The element must remain in the queue while prompting
                 * the user, so that another element being handled will be added to the queue
                 * instead of opening another dialog, and also to prevent the user from being
                 * prompted twice for the same element.
                 */
                synchronized (promptQueue) {
                    promptQueue.remove();
                    prompting = promptQueue.peek();
                }
            }
        });
    }

    private static void deleteSupplementaryResources(TmfTraceElement trace) {
        /*
         * Delete supplementary resources as an atomic workspace operation. To ensure no
         * dialog is shown to the user, run it in a job outside of the UI thread.
         */
        Job.createSystem("TmfProjectRegistry.deleteSupplementaryResources Job", monitor -> { //$NON-NLS-1$
            try {
                ResourcesPlugin.getWorkspace().run((ICoreRunnable) (mon -> {
                    trace.deleteSupplementaryResources();
                }), trace.getProject().getResource(), IWorkspace.AVOID_UPDATE, monitor);
            } catch (CoreException e) {
                Activator.getDefault().logError("Error deleting supplementary resources for " + trace.getName(), e); //$NON-NLS-1$
            }
        }).schedule();
    }

    private static void handleTraceDeleted(TmfTraceElement trace) {
        try {
            trace.preDelete(new NullProgressMonitor(), false);
            ResourceUtil.deleteIfBrokenSymbolicLink(trace.getResource());
        } catch (CoreException e) {
            Activator.getDefault().logError("Error deleting trace " + trace.getName(), e); //$NON-NLS-1$
        }
    }

    private static void handleParentProjectOpen(IProject project) {
        Job job = Job.createSystem("TmfProjectRegistry.handleParentProjectOpen Job", monitor -> { //$NON-NLS-1$
            IProject shadowProject = TmfProjectModelHelper.getShadowProject(project);
            if (shadowProject != null && shadowProject.exists() && !shadowProject.isOpen()) {
                try {
                    shadowProject.open(new NullProgressMonitor());
                } catch (CoreException e) {
                    // Do nothing ... addTracingNature() will handle this
                }
            }
            addTracingNature(project, monitor);
        });
        job.schedule();
    }

    private static void handleParentProjectRefresh(IProject project) {
        Job job = Job.createSystem("TmfProjectRegistry.handleParentProjectRefresh Job", monitor -> { //$NON-NLS-1$
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IProject shadowProject = workspace.getRoot().getProject(TmfProjectModelHelper.getShadowProjectName(project.getName()));
            if (shadowProject.exists()) {
                try {
                    shadowProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
                } catch (CoreException e) {
                    Activator.getDefault().logError("Error refeshing shadow project " + shadowProject.getName(), e); //$NON-NLS-1$
                }
            }
        });
        job.schedule();
    }

    private static void handleProjectMoved(IProject newProject) {
        Job job = Job.createSystem("TmfProjectRegistry.handleProjectMoved Job", monitor -> { //$NON-NLS-1$
            addTracingNature(newProject, monitor);
            IProject shadowProject = TmfProjectModelHelper.getShadowProject(newProject);
            String newShadowProjectName = TmfProjectModelHelper.getShadowProjectName(newProject.getName());
            try {
                if (shadowProject.exists()) {
                    IProjectDescription desc = shadowProject.getDescription();
                    desc.setName(newShadowProjectName);
                    shadowProject.move(desc, true, monitor);
                }
            } catch (CoreException e) {
                Activator.getDefault().logError("Error renaming shadow project " + shadowProject.getName() + " to " + newShadowProjectName, e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
        job.schedule();
    }
}
