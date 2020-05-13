/*******************************************************************************
 * Copyright (c) 2017, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.SelectTracesOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.operations.TmfWorkspaceModifyOperation;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfOpenTraceHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceTypeUIUtils;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTracesFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for the Open As Experiment command
 */
public class OpenAsExperimentHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.type"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        final String experimentType = event.getParameter(TYPE_PARAMETER);
        final TraceTypeHelper traceTypeHelper = TmfTraceType.getTraceType(experimentType);
        if (traceTypeHelper == null) {
            return null;
        }

        IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);

        String experimentName = Messages.OpenAsExperimentHandler_DefaultExperimentName;
        Set<@NonNull TmfTraceElement> traces = new HashSet<>();
        TmfProjectElement project = null;
        for (Object element : selection.toList()) {
            if (element instanceof TmfTracesFolder) {
                TmfTracesFolder tracesFolder = (TmfTracesFolder) element;
                traces.addAll(tracesFolder.getTraces());
                project = tracesFolder.getProject();
            } else if (element instanceof TmfTraceFolder) {
                TmfTraceFolder traceFolder = (TmfTraceFolder) element;
                traces.addAll(traceFolder.getTraces());
                project = traceFolder.getProject();
                if (selection.size() == 1) {
                    experimentName = traceFolder.getName();
                }
            } else if (element instanceof TmfTraceElement) {
                TmfTraceElement traceElement = (TmfTraceElement) element;
                traces.add(traceElement);
                project = traceElement.getProject();
                if (selection.size() == 1) {
                    experimentName = traceElement.getName();
                }
            }
        }
        if (project == null) {
            return null;
        }
        final TmfExperimentFolder experimentsFolder = project.getExperimentsFolder();
        if (experimentsFolder == null) {
            return null;
        }

        final IFolder experimentFolder = getExperimentFolder(experimentName, experimentsFolder, traces);
        boolean exists = experimentFolder.exists();

        if (!exists) {
            try {
                experimentFolder.create(false, true, null);
            } catch (CoreException e) {
                TraceUtils.displayErrorMsg(e.toString(), e.toString());
                return null;
            }
        }

        TmfExperimentElement experimentElement = experimentsFolder.getExperiment(experimentFolder);
        if (experimentElement == null) {
            return null;
        }

        if (!exists) {
            TmfWorkspaceModifyOperation operation = new TmfWorkspaceModifyOperation() {
                @Override
                public void execute(IProgressMonitor monitor) throws CoreException {
                    SelectTracesOperation selectTracesOperation = new SelectTracesOperation(experimentElement, traces.toArray(new @NonNull TmfTraceElement[traces.size()]), Collections.emptyMap());
                    selectTracesOperation.run(monitor);
                }
            };

            try {
                PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (InvocationTargetException e) {
                TraceUtils.displayErrorMsg(e.toString(), e.getTargetException().toString());
                return null;
            }
        }

        IStatus status = traceTypeHelper.validate(experimentFolder.getLocation().toOSString());
        if (!status.isOK()) {
            TraceUtils.displayErrorMsg(Messages.OpenAsExperimentHandler_ValidationErrorTitle, NLS.bind(Messages.OpenAsExperimentHandler_ValidationErrorMessage, status.getMessage()));
            return null;
        }

        if (!exists || !experimentElement.getTraceType().equals(traceTypeHelper.getTraceTypeId())) {
            if (exists) {
                experimentElement.closeEditors();
            }
            TmfWorkspaceModifyOperation operation = new TmfWorkspaceModifyOperation() {
                @Override
                public void execute(IProgressMonitor monitor) throws CoreException {
                    if (exists) {
                        experimentElement.deleteSupplementaryResources();
                    }
                    TmfTraceTypeUIUtils.setTraceType(experimentFolder, traceTypeHelper, false);
                }
            };

            try {
                PlatformUI.getWorkbench().getProgressService().run(true, true, operation);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (InvocationTargetException e) {
                TraceUtils.displayErrorMsg(e.toString(), e.getTargetException().toString());
                return null;
            }
        }

        status = TmfOpenTraceHelper.openFromElement(experimentElement);
        if (!status.isOK()) {
            TraceUtils.displayErrorMsg(Messages.OpenAsExperimentHandler_OpeningErrorTitle, NLS.bind(Messages.OpenAsExperimentHandler_OpeningErrorMessage, status.getMessage()));
        }

        return null;
    }

    private static IFolder getExperimentFolder(String experimentName, final TmfExperimentFolder experimentsFolder, Set<@NonNull TmfTraceElement> traces) {
        IFolder folder = experimentsFolder.getResource().getFolder(experimentName);
        int i = 2;
        while (folder.exists()) {
            TmfExperimentElement experimentElement = experimentsFolder.getExperiment(folder);
            if (experimentElement != null) {
                Set<TmfTraceElement> existingTraces = new HashSet<>();
                for (TmfTraceElement trace : experimentElement.getTraces()) {
                    existingTraces.add(trace.getElementUnderTraceFolder());
                }
                if (existingTraces.equals(traces)) {
                    return folder;
                }
            }
            folder = experimentsFolder.getResource().getFolder(experimentName + '(' + Integer.toString(i++) + ')');
        }
        return folder;
    }
}
