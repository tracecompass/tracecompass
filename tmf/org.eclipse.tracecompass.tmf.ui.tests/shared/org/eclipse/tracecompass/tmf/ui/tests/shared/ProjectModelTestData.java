/*******************************************************************************
 * Copyright (c) 2013, 2018 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.shared;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Creates objects used for this package's testing purposes
 *
 * @author Geneviève Bastien
 */
public class ProjectModelTestData {

    /** Default test project name */
    public static final String PROJECT_NAME = "Test_Project";

    private static final TmfTestTrace testTrace = TmfTestTrace.A_TEST_10K;

    /**
     * Gets a project element with traces all initialized
     *
     * @return A project stub element
     * @throws CoreException
     *             If something happened with the project creation
     */
    public static TmfProjectElement getFilledProject() throws CoreException {

        IProject project = TmfProjectRegistry.createProject(PROJECT_NAME, null, null);
        final TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
        TmfTraceFolder tracesFolder = projectElement.getTracesFolder();
        if (tracesFolder != null) {
            IFolder traceFolder = tracesFolder.getResource();

            /* Create a trace, if it exist, it will be replaced */
            final IPath pathString = new Path(testTrace.getFullPath());
            IResource linkedTrace = TmfImportHelper.createLink(traceFolder, pathString, pathString.lastSegment());
            if (!(linkedTrace != null && linkedTrace.exists())) {
                return null;
            }
            linkedTrace.setPersistentProperty(TmfCommonConstants.TRACETYPE,
                    "org.eclipse.linuxtools.tmf.core.tests.tracetype");

            // Refresh the project model
            tracesFolder.refresh();

            TmfTraceElement traceElement = tracesFolder.getTraces().get(0);
            traceElement.refreshTraceType();
        }
        projectElement.refresh();

        return projectElement;
    }

    /**
     * Adds a new experiment to the project
     *
     * @param projectElement
     *            The project to add to
     * @param experimentName
     *            Name of the experiment
     * @return The newly created experiment
     */
    public static TmfExperimentElement addExperiment(TmfProjectElement projectElement, String experimentName) {
        TmfExperimentFolder experimentsFolder = projectElement.getExperimentsFolder();
        if (experimentsFolder != null) {
            IFolder experimentFolder = experimentsFolder.getResource();
            final IFolder folder = experimentFolder.getFolder(experimentName);

            WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
                @Override
                public void execute(IProgressMonitor monitor) throws CoreException {
                    monitor.beginTask("", 1000);
                    folder.create(false, true, monitor);
                    monitor.done();
                }
            };
            try {
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
            } catch (InterruptedException | InvocationTargetException | RuntimeException exception) {
                exception.printStackTrace();
            }
            experimentsFolder.refresh();
            for (ITmfProjectModelElement el : experimentsFolder.getChildren()) {
                if (el.getName().equals(experimentName) && (el instanceof TmfExperimentElement)) {
                    return (TmfExperimentElement) el;
                }
            }
        }
        return null;
    }

    /**
     * Get the name of the test trace element
     *
     * @return The trace name
     */
    public static String getTraceName() {
        File file = new File(testTrace.getPath());
        String path = file.getAbsolutePath();
        final IPath pathString = Path.fromOSString(path);
        return pathString.lastSegment();
    }

    /**
     * Deletes a project
     *
     * @param project
     *            Project to delete
     */
    public static void deleteProject(TmfProjectElement project) {
        /* Delete experiments */
        TmfExperimentFolder experimentsFolder = project.getExperimentsFolder();
        if (experimentsFolder != null) {
            ITmfProjectModelElement[] experiments = experimentsFolder.getChildren().toArray(new ITmfProjectModelElement[0]);
            for (ITmfProjectModelElement element : experiments) {
                if (element instanceof TmfExperimentElement) {
                    TmfExperimentElement experiment = (TmfExperimentElement) element;
                    IResource resource = experiment.getResource();

                    /* Close the experiment if open */
                    experiment.closeEditors();

                    IPath path = resource.getLocation();
                    if (path != null) {
                        /* Delete supplementary files */
                        experiment.deleteSupplementaryFolder();
                    }

                    /* Finally, delete the experiment */
                    try {
                        resource.delete(true, null);
                    } catch (CoreException e) {
                        Activator.getDefault().logError("Error deleting experiment element", e);
                    }
                }
            }
        }

        /* Delete traces */
        TmfTraceFolder tracesFolder = project.getTracesFolder();
        if (tracesFolder != null) {
            ITmfProjectModelElement[] traces = tracesFolder.getChildren().toArray(new ITmfProjectModelElement[0]);
            for (ITmfProjectModelElement element : traces) {
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    IResource resource = trace.getResource();

                    /* Close the trace if open */
                    trace.closeEditors();

                    IPath path = resource.getLocation();
                    if (path != null) {
                        /* Delete supplementary files */
                        trace.deleteSupplementaryFolder();
                    }

                    /* Finally, delete the trace */
                    try {
                        resource.delete(true, new NullProgressMonitor());
                    } catch (CoreException e) {
                        Activator.getDefault().logError("Error deleting trace element", e);
                    }
                }
            }
        }

        /* Delete the project itself */
        try {
            project.getResource().delete(true, null);
        } catch (CoreException e) {
            Activator.getDefault().logError("Error deleting project", e);
        }
    }

    /**
     * Makes the main display thread sleep, so it gives a chance to other
     * threads needing the main display to execute
     *
     * @param waitTimeMillis
     *            time to wait in millisecond
     */
    public static void delayThread(final long waitTimeMillis) {
        final Display display = Display.getCurrent();
        if (display != null) {
            final long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
            while (System.currentTimeMillis() < endTimeMillis) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
                display.update();
            }
        } else {
            try {
                Thread.sleep(waitTimeMillis);
            } catch (final InterruptedException e) {
                // Ignored
            }
        }
    }

    /**
     * Makes the main display thread sleep to give a chance to other threads to
     * execute. It sleeps until the a trace element's corresponding trace is
     * available (opened) or returns after a timeout. It allows to set short
     * delays, while still not failing tests when it randomly takes a bit more
     * time for the trace to open.
     *
     * @param traceElement
     *            The trace element we are waiting for.
     * @throws WaitTimeoutException
     *             If after the maximum number of delays the trace is still
     *             null, we throw a timeout exception, the trace has not opened.
     */
    public static void delayUntilTraceOpened(final TmfCommonProjectElement traceElement) throws WaitTimeoutException {
        WaitUtils.waitUntil(new IWaitCondition() {
            @Override
            public boolean test() throws Exception {
                return traceElement.getTrace() != null;
            }

            @Override
            public String getFailureMessage() {
                return "Timeout while waiting for " + traceElement;
            }
        });
    }
}
