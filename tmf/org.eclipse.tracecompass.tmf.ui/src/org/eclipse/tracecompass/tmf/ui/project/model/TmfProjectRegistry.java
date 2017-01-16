/*******************************************************************************
 * Copyright (c) 2011, 2017 Ericsson
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.TmfProjectNature;
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
    private static Map<IProject, TmfProjectElement> registry = new HashMap<>();

    private static Queue<TmfTraceElement> promptQueue = new ArrayDeque<>();

    private TmfProjectRegistry() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
        TmfSignalManager.register(this);
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
        return getProject(project, false);
    }

    /**
     * Get the project model element for a project resource
     * @param project the project resource
     * @param force a flag controlling whether a new project should be created if it doesn't exist
     * @return the project model element
     */
    public static synchronized TmfProjectElement getProject(IProject project, boolean force) {
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

                IFolder folder = project.getFolder(TmfTracesFolder.TRACES_RESOURCE_NAME);
                if (!folder.exists()) {
                    folder.create(true, true, null);
                }

                folder = project.getFolder(TmfExperimentFolder.EXPER_RESOURCE_NAME);
                if (!folder.exists()) {
                    folder.create(true, true, null);
                }

                // create folder for supplementary tracing files
                folder = project.getFolder(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER_NAME);

                if (!folder.exists()) {
                    folder.create(true, true, null);
                }
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().run(false, false, action);
        } catch (InvocationTargetException e) {
            Activator.getDefault().logError("Error creating TMF project " + project.getName(), e); //$NON-NLS-1$
        } catch (InterruptedException e) {
        }
        return project;
    }

    // ------------------------------------------------------------------------
    // IResourceChangeListener
    // ------------------------------------------------------------------------

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_DELETE || event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            if (event.getResource() instanceof IProject) {
                IProject project = (IProject) event.getResource();
                try {
                    if (project.isAccessible() && project.hasNature(TmfProjectNature.ID)) {
                        TmfProjectElement tmfProjectElement = registry.get(project);
                        if (tmfProjectElement == null) {
                            return;
                        }
                        TmfTraceFolder tracesFolder = tmfProjectElement.getTracesFolder();
                        if (tracesFolder != null) {
                            final List<TmfTraceElement> traces = tracesFolder.getTraces();
                            if (!traces.isEmpty()) {
                                // Close editors in UI Thread
                                Display.getDefault().syncExec(() -> traces.forEach(TmfTraceElement::closeEditors));
                            }
                        }
                    }
                } catch (CoreException e) {
                    Activator.getDefault().logError("Error handling resource change event for " + project.getName(), e); //$NON-NLS-1$
                }
            }
        } else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            for (IResourceDelta delta : event.getDelta().getAffectedChildren()) {
                if (delta.getResource() instanceof IProject) {
                    IProject project = (IProject) delta.getResource();
                    try {
                        if (delta.getKind() == IResourceDelta.CHANGED &&
                                project.isOpen() && project.hasNature(TmfProjectNature.ID)) {
                            Set<IResource> resourcesToRefresh = new HashSet<>();
                            event.getDelta().accept(visited -> {
                                if ((visited.getFlags() & IResourceDelta.CONTENT) != 0) {
                                    // visited resource content has changed, refresh it
                                    resourcesToRefresh.add(visited.getResource());
                                } else if (visited.getKind() != IResourceDelta.CHANGED) {
                                    // visited resource is added or removed, refresh its parent
                                    resourcesToRefresh.add(visited.getResource().getParent());
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
                            Set<TmfTraceElement> changedTraces = new HashSet<>();
                            Iterator<ITmfProjectModelElement> iterator = elementsToRefresh.iterator();
                            while (iterator.hasNext()) {
                                ITmfProjectModelElement element = iterator.next();
                                if (element instanceof TmfTraceElement && element.getParent() instanceof TmfTraceFolder) {
                                    changedTraces.add((TmfTraceElement) element);
                                }
                                for (ITmfProjectModelElement parent : elementsToRefresh) {
                                    // remove element if any of its parents is included
                                    if (parent.getPath().isPrefixOf(element.getPath()) && !parent.equals(element)) {
                                        iterator.remove();
                                        break;
                                    }
                                }
                            }
                            for (TmfTraceElement trace : changedTraces) {
                                handleTraceContentChanged(trace);
                            }
                            for (ITmfProjectModelElement element : elementsToRefresh) {
                                element.refresh();
                            }
                            TmfProjectElement projectElement = registry.get(project);
                            if (projectElement != null) {
                                // refresh only the viewer for the affected project
                                projectElement.refreshViewer();
                            }
                        } else if (delta.getKind() == IResourceDelta.REMOVED) {
                            TmfProjectElement projectElement = registry.remove(project);
                            if (projectElement != null) {
                                projectElement.dispose();
                            }
                        }
                    } catch (CoreException e) {
                        Activator.getDefault().logError("Error handling resource change event for " + project.getName(), e); //$NON-NLS-1$
                    }
                }
            }
        }
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
        if (resource == null) {
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
        Display.getDefault().asyncExec(() -> {
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
            if (!promptQueue.isEmpty()) {
                /* already prompting the user */
                if (!promptQueue.contains(traceElement)) {
                    promptQueue.add(traceElement);
                }
                return;
            }
            promptQueue.add(traceElement);
            TmfTraceElement prompting = traceElement;
            while (prompting != null) {
                boolean always = Activator.getDefault().getPreferenceStore().getBoolean(ITmfUIPreferences.ALWAYS_CLOSE_ON_RESOURCE_CHANGE);
                if (always) {
                    prompting.closeEditors();
                    prompting.deleteSupplementaryResources();
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
                        prompting.deleteSupplementaryResources();
                    }
                    if (returnCode == 2) {
                        // remember to always delete supplementary files and close editors
                        Activator.getDefault().getPreferenceStore().setValue(ITmfUIPreferences.ALWAYS_CLOSE_ON_RESOURCE_CHANGE, true);
                    }
                }
                /**
                 * Poll at end of the loop: The element must remain in the queue while prompting
                 * the user, so that another element being handled will be added to the queue
                 * instead of opening another dialog, and also to prevent the user from being
                 * prompted twice for the same element.
                 */
                promptQueue.remove();
                prompting = promptQueue.peek();
            }
        });
    }
}
