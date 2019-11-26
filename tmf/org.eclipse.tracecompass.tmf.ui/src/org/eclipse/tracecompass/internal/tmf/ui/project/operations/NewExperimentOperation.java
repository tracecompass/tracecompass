/*******************************************************************************
 * Copyright (c) 2016, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.operations;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;

/**
 * Operation to create a new experiment.
 *
 * @author Bernd Hufmann
 *
 */
public class NewExperimentOperation implements IRunnableWithProgress {

    private final @NonNull String fExperimentName;
    private final @NonNull TmfExperimentFolder fExperimentFolderRoot;
    private final String fExperimentType;
    private final List<TmfTraceElement> fTraceElements;
    private @Nullable IFolder fExperimentFolder = null;
    private @NonNull IStatus fStatus = Status.OK_STATUS;

    /**
     * Constructor
     *
     * @param experimentFolder
     *            the experiment folder root {@link TmfExperimentFolder}
     * @param experimentName
     *            the name of the experiment
     */
    public NewExperimentOperation (@NonNull TmfExperimentFolder experimentFolder, @NonNull String experimentName) {
        fExperimentFolderRoot = experimentFolder;
        fExperimentName = experimentName;
        fExperimentType = TmfTraceType.DEFAULT_EXPERIMENT_TYPE;
        fTraceElements = Collections.emptyList();
    }

    /**
     * Constructor
     *
     * @param experimentFolder
     *            the experiment folder root {@link TmfExperimentFolder}
     * @param experimentName
     *            the name of the experiment
     * @param experimentType
     *            the experiment type, or null for default
     * @param traceElements
     *            the initial trace elements
     */
    public NewExperimentOperation (@NonNull TmfExperimentFolder experimentFolder, @NonNull String experimentName, String experimentType, List<TmfTraceElement> traceElements) {
        fExperimentFolderRoot = experimentFolder;
        fExperimentName = experimentName;
        fExperimentType = experimentType != null ? experimentType : TmfTraceType.DEFAULT_EXPERIMENT_TYPE;
        fTraceElements = traceElements != null ? traceElements : Collections.emptyList();
    }

    @Override
    public void run(IProgressMonitor monitor) {
        final IFolder experimentFolder = createExperiment(fExperimentName);
        try {
            monitor.beginTask("", 1000); //$NON-NLS-1$
            ModalContext.checkCanceled(monitor);
            experimentFolder.create(false, true, monitor);

            /*
             * Experiments can be set to the default experiment type. No
             * need to force user to select an experiment type
             */
            IConfigurationElement ce = TmfTraceType.getTraceAttributes(fExperimentType);
            if (ce != null) {
                experimentFolder.setPersistentProperty(TmfCommonConstants.TRACETYPE, ce.getAttribute(TmfTraceType.ID_ATTR));
            }
            fExperimentFolder = experimentFolder;
            TmfExperimentElement experimentElement = fExperimentFolderRoot.addExperiment(experimentFolder);
            for (TmfTraceElement trace : fTraceElements) {
                experimentElement.addTrace(trace, false);
            }
            experimentElement.refreshTraceType();
            experimentElement.refresh();
            setStatus(Status.OK_STATUS);
        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
            Thread.currentThread().interrupt();
        } catch (InvalidRegistryObjectException | CoreException e) {
            String msg = NLS.bind(Messages.NewExperimentOperation_CreationError, fExperimentName);
            Activator.getDefault().logError(msg, e);
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg, e));
        }
    }

    /**
     * @return the experimentFolder
     */
    public @Nullable IFolder getExperimentFolder() {
        return fExperimentFolder;
    }

    private IFolder createExperiment(String experimentName) {
        IFolder expResource = fExperimentFolderRoot.getResource();
        IWorkspaceRoot workspaceRoot = expResource.getWorkspace().getRoot();
        IPath folderPath = expResource.getFullPath().append(experimentName);
        IFolder folder = workspaceRoot.getFolder(folderPath);
        return folder;
    }

    /**
     * Set the status for this operation
     *
     * @param status
     *            the status
     */
    protected void setStatus(@NonNull IStatus status) {
        fStatus = status;
    }

    /**
     * Returns the status of the operation execution.
     *
     * @return status
     */
    public @NonNull IStatus getStatus() {
        return fStatus;
    }

}
