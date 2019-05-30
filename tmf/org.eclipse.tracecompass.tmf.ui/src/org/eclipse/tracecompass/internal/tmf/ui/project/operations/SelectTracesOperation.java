/*******************************************************************************
 * Copyright (c) 2016, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Simon Delisle - Add time range selection support
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.project.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.UpdateTraceBoundsJob;

import com.google.common.collect.Iterables;

/**
 * Operation to add traces to an experiment.
 *
 * @author Bernd Hufmann
 */
public class SelectTracesOperation implements IRunnableWithProgress {

    private final @Nullable TmfExperimentElement fExperimentElement;
    private final @Nullable TmfTraceFolder fParentTraceFolder;
    private final @Nullable List<TmfTraceElement> fTraceElements;
    private final @Nullable List<IResource> fResources;
    private final @Nullable Map<String, TmfTraceElement> fPreviousTraces;
    private @NonNull IStatus fStatus = Status.OK_STATUS;
    private final ITmfTimestamp fStartTimeRange;
    private final ITmfTimestamp fEndTimeRange;

    /**
     * Constructor
     *
     * @param experimentFolderElement
     *              workspace experiment folder containing the experiment
     * @param experiment
     *              experiment folder where to add traces
     * @param parentTraceFolder
     *              the parent trace folder containing the trace resources
     * @param resources
     *              the trace resources to add to the experiment
     */
    public SelectTracesOperation(@NonNull TmfExperimentFolder experimentFolderElement, @NonNull IFolder experiment, @NonNull TmfTraceFolder parentTraceFolder, @NonNull List<IResource> resources) {
        this(experimentFolderElement.getExperiment(experiment), parentTraceFolder, null, resources, null, null, null);
    }

    /**
     * Constructor. It will add traces to given experiment and remove traces
     * that don't exist anymore.
     *
     * @param experimentElement
     *              the experiment element to add the traces
     * @param traces
     *              the trace elements
     * @param previousTraces
     *              map of traces currently available in the experiment
     */
    public SelectTracesOperation(@NonNull TmfExperimentElement experimentElement, @NonNull TmfTraceElement[] traces, @NonNull Map<String, TmfTraceElement> previousTraces) {
        this(experimentElement, null, traces, null, previousTraces, null, null);
    }

    /**
     * Constructor.
     *
     * @param experimentElement
     *            the experiment element to add the traces
     * @param traces
     *            the trace elements
     * @param previousTraces
     *            map of traces currently available in the experiment
     * @param startTimeRange
     *            The start timestamp
     * @param endTimeRange
     *            The end timestamp
     */
    public SelectTracesOperation(@NonNull TmfExperimentElement experimentElement, @NonNull TmfTraceElement[] traces, @NonNull Map<String, TmfTraceElement> previousTraces, ITmfTimestamp startTimeRange, ITmfTimestamp endTimeRange) {
        this(experimentElement, null, traces, null, previousTraces, startTimeRange, endTimeRange);
    }

    // Full constructor for internal use only
    private SelectTracesOperation(TmfExperimentElement experimentElement, TmfTraceFolder parentTraceFolder, TmfTraceElement[] traces, List<IResource> resources, Map<String, TmfTraceElement> previousTraces, ITmfTimestamp startTimeRange,
            ITmfTimestamp endTimeRange) {
        fExperimentElement = experimentElement;
        fParentTraceFolder = parentTraceFolder;
        if (traces == null) {
            fTraceElements = null;
        } else {
            fTraceElements = new ArrayList<>(Arrays.asList(traces));
        }
        fResources = resources;
        fPreviousTraces = previousTraces;
        if (startTimeRange != null && endTimeRange != null && startTimeRange.compareTo(endTimeRange) > 0) {
            fStartTimeRange = endTimeRange;
            fEndTimeRange = startTimeRange;
        } else {
            fStartTimeRange = startTimeRange;
            fEndTimeRange = endTimeRange;
        }
    }

    @Override
    public void run(IProgressMonitor progressMonitor) {
        TmfExperimentElement experimentElement = fExperimentElement;
        if (experimentElement == null) {
            return;
        }

        // Check if operation was cancelled.
        boolean changed = false;

        Map<String, TmfTraceElement> previousTraces = new HashMap<>();
        if (fPreviousTraces != null) {
            previousTraces = fPreviousTraces;
        }

        List<TmfTraceElement> elements = fTraceElements;
        if (elements == null) {
            if ((fParentTraceFolder != null) && (fResources != null)) {
                elements = fParentTraceFolder.getTraceElements(fResources);
            } else {
                return;
            }
        }

        Set<String> keys = previousTraces.keySet();
        int workLoad = elements.size() + keys.size();
        SubMonitor subMonitor = SubMonitor.convert(progressMonitor, workLoad);

        if (fStartTimeRange != null && fEndTimeRange != null) {
            workLoad += elements.size();
            subMonitor.setWorkRemaining(workLoad);
            elements = filterTraceElementByTimeRange(elements, subMonitor.newChild(elements.size()));
        }
        // We might have less elements now
        subMonitor.setWorkRemaining(elements.size() + keys.size());

        // Add the selected traces to the experiment
        try {
            for (TmfTraceElement trace : elements) {
                ModalContext.checkCanceled(progressMonitor);
                String name = trace.getElementPath();
                if (keys.contains(name)) {
                    subMonitor.setTaskName(Messages.SelectTracesWizardPage_TraceRemovalTask + " " + trace.getElementPath()); //$NON-NLS-1$
                    keys.remove(name);
                } else {
                    subMonitor.setTaskName(Messages.SelectTracesWizardPage_TraceSelectionTask + " " + trace.getElementPath()); //$NON-NLS-1$
                    experimentElement.addTrace(trace, false);
                    changed = true;
                }
                subMonitor.worked(1);
            }

            // Remove traces that were unchecked (thus left in fPreviousTraces)
            for (Map.Entry<String, TmfTraceElement> entry : previousTraces.entrySet()) {
                ModalContext.checkCanceled(progressMonitor);
                TmfTraceElement trace = entry.getValue();
                subMonitor.setTaskName(Messages.SelectTracesWizardPage_TraceRemovalTask + " " + trace.getElementPath()); //$NON-NLS-1$

                try {
                    experimentElement.removeTrace(trace);
                } catch (CoreException e) {
                    Activator.getDefault().logError(Messages.SelectTracesWizardPage_SelectionError + " " + experimentElement.getName(), e); //$NON-NLS-1$
                }
                changed = true;
                subMonitor.worked(1);
            }
            if (changed) {
                Display.getDefault().asyncExec(() -> {
                    experimentElement.closeEditors();
                    experimentElement.deleteSupplementaryResources();
                });
            }
            setStatus(Status.OK_STATUS);
        } catch (InterruptedException e) {
            setStatus(Status.CANCEL_STATUS);
        } catch (Exception e) {
            Activator.getDefault().logError(Messages.SelectTracesWizardPage_SelectionError, e);
            setStatus(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.SelectTracesWizardPage_SelectionError, e));
        }
    }

    /**
     * Filter the given list according to the time range.
     *
     * @param elements
     *            List of TmfTraceElement to filter
     * @param progressMonitor
     *            Progress monitor for the job
     * @return A filtered list that contain traces that are in or overlap the
     *         time range
     */
    private List<TmfTraceElement> filterTraceElementByTimeRange(List<TmfTraceElement> elements, IProgressMonitor progressMonitor) {
        SubMonitor subMonitor = SubMonitor.convert(progressMonitor, elements.size());
        List<TmfTraceElement> filteredElements = new ArrayList<>();
        List<TmfTraceElement> knownBounds = new ArrayList<>();
        Queue<TmfTraceElement> unknownBounds = new ConcurrentLinkedQueue<>();
        Iterable<TmfTraceElement> traceElements = Iterables.concat(knownBounds, unknownBounds);

        for (TmfTraceElement tmfTraceElement : elements) {
            if (tmfTraceElement.getStartTime() == null || tmfTraceElement.getEndTime() == null) {
                unknownBounds.add(tmfTraceElement);
            } else {
                knownBounds.add(tmfTraceElement);
            }
        }

        if (!unknownBounds.isEmpty()) {
            UpdateTraceBoundsJob job = new UpdateTraceBoundsJob(Messages.SelectTracesWizardPage_UpdateTraceBoundsJobName, new ConcurrentLinkedQueue<>(unknownBounds));
            job.run(subMonitor.newChild(1));
        }

        for (TmfTraceElement element : traceElements) {
            if (isInTimeRange(element.getStartTime(), element.getEndTime())) {
                filteredElements.add(element);
            }
        }
        return filteredElements;
    }

    /**
     * Check if the time range overlap or is completely included in this
     * SelectTracesOperation's time range.
     *
     * @param traceStartTime
     *            Start timestamp of the trace
     * @param traceEndTime
     *            End timestamp of the trace
     * @return True if the the trace is in or overlap the time range
     */
    private boolean isInTimeRange(ITmfTimestamp traceStartTime, ITmfTimestamp traceEndTime) {
        return traceStartTime.compareTo(fEndTimeRange) <= 0 && traceEndTime.compareTo(fStartTimeRange) >= 0;
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
