/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.project.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.tmf.ui.editors.ITmfEventsEditorConstants;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Implementation of ILinkHelper interface for linking with editor extension for traces and
 * experiments.
 *
 * @author Bernd Hufmann
 */
public class TmfEditorLinkHelper implements ILinkHelper {

    @Override
    public IStructuredSelection findSelection(IEditorInput anInput) {
        IFile file = ResourceUtil.getFile(anInput);
        if (file != null) {

            try {
                // Get the trace type ID
                String traceTypeId = file.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                if (traceTypeId == null) {
                    return StructuredSelection.EMPTY;
                }

                final TmfProjectElement project = TmfProjectRegistry.getProject(file.getProject(), true);
                if (project == null) {
                    return StructuredSelection.EMPTY;
                }
                TmfTraceFolder tracesFolder = project.getTracesFolder();

                // Check for experiments, traces which are folders or traces which are files
                if (ITmfEventsEditorConstants.EXPERIMENT_INPUT_TYPE_CONSTANTS.contains(traceTypeId)) {
                    TmfExperimentFolder experimentFolder = project.getExperimentsFolder();
                    // Case 1: Experiment
                    if (experimentFolder != null) {
                        for (final TmfExperimentElement experimentElement : experimentFolder.getExperiments()) {
                            if (experimentElement.getResource().equals(file.getParent())) {
                                return new StructuredSelection(experimentElement);
                            }
                        }
                    }
                } else if (ITmfEventsEditorConstants.TRACE_INPUT_TYPE_CONSTANTS.contains(traceTypeId)) {
                    // Case 2: Trace that is a folder

                    if (tracesFolder != null) {
                        for (final TmfTraceElement traceElement : tracesFolder.getTraces()) {
                            if (traceElement.getResource().equals(file.getParent())) {
                                return new StructuredSelection(traceElement);
                            }
                        }
                    }
                } else {
                    // Case 3: Trace that is a file
                    if (tracesFolder != null) {
                        for (final TmfTraceElement traceElement : tracesFolder.getTraces()) {
                            if (traceElement.getResource().equals(file)) {
                                return new StructuredSelection(traceElement);
                            }
                        }
                    }
                }
            } catch (CoreException e) {
                return StructuredSelection.EMPTY;
            }
        }
        return StructuredSelection.EMPTY;
    }

    @Override
    public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
        if (aSelection == null || aSelection.isEmpty()) {
            return;
        }

        IFile file = null;

        if ((aSelection.getFirstElement() instanceof TmfTraceElement)) {
            TmfTraceElement traceElement = ((TmfTraceElement)aSelection.getFirstElement());

            // If trace is under an experiment, use the original trace from the traces folder
            traceElement = traceElement.getElementUnderTraceFolder();
            file = traceElement.getBookmarksFile();
        } else if ((aSelection.getFirstElement() instanceof TmfExperimentElement)) {
            TmfExperimentElement experimentElement = (TmfExperimentElement) aSelection.getFirstElement();
            file = experimentElement.getBookmarksFile();
        }

        if (file != null) {
            IEditorInput tmpInput = new FileEditorInput(file);
            IEditorPart localEditor = aPage.findEditor(tmpInput);
            if (localEditor != null) {
                // Editor found.
                aPage.bringToTop(localEditor);
            } else {
                // Search in references for corresponding editor
                IEditorReference[] refs = aPage.getEditorReferences();
                for (IEditorReference editorReference : refs) {
                    try {
                        if (editorReference.getEditorInput().equals(tmpInput)) {
                            localEditor = editorReference.getEditor(true);
                            if (localEditor != null) {
                                aPage.bringToTop(localEditor);
                            }
                        }
                    } catch (PartInitException e) {
                        // Ignore
                    }
                }
            }
        }
    }
}

