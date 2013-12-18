/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.shared;

import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.ui.project.model.ITmfProjectModelElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.swt.widgets.Display;

/**
 * Creates objects used for this package's testing purposes
 *
 * @author Geneviève Bastien
 */
public class ProjectModelTestData {

    /** Default test project name */
    public static final String PROJECT_NAME = "Test_Project";

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.KERNEL;

    /**
     * Gets a project element with traces all initialized
     *
     * @return A project stub element
     * @throws CoreException
     *             If something happened with the project creation
     */
    public static TmfProjectElement getFilledProject() throws CoreException {

        assumeTrue(CtfTmfTestTrace.KERNEL.exists());

        IProject project = TmfProjectRegistry.createProject(PROJECT_NAME, null, null);
        IFolder traceFolder = project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME);

        /* Create a trace, if it exist, it will be replaced */
        File file = new File(testTrace.getPath());
        String path = file.getAbsolutePath();
        final IPath pathString = Path.fromOSString(path);
        IResource linkedTrace = TmfImportHelper.createLink(traceFolder, pathString, pathString.lastSegment());
        if (!(linkedTrace != null && linkedTrace.exists())) {
            return null;
        }
        linkedTrace.setPersistentProperty(TmfCommonConstants.TRACETYPE,
                "org.eclipse.linuxtools.tmf.tests.ctf.tracetype");

        final TmfProjectElement projectElement = TmfProjectRegistry.getProject(project, true);
        TmfTraceElement traceElement = projectElement.getTracesFolder().getTraces().get(0);
        traceElement.refreshTraceType();

        return projectElement;
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
        for (ITmfProjectModelElement element : project.getExperimentsFolder().getChildren()) {
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

        /* Delete traces */
        for (ITmfProjectModelElement element : project.getTracesFolder().getChildren()) {
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

}
