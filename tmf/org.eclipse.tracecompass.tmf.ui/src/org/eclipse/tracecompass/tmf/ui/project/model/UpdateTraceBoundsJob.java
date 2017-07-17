/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Loïc Prieur-Drevon - Initial API and implementation
 *   Simon Delisle - Move this job in its own class
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Queue;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Update traces bounds, by using supplementary files if they exist or by
 * reading the trace to determine start and end time of the traces.
 *
 * @author Loïc Prieur-Drevon
 * @since 3.1
 *
 */
public class UpdateTraceBoundsJob extends Job {

    private static final Logger LOGGER = TraceCompassLog.getLogger(UpdateTraceBoundsJob.class);
    private static final String BOUNDS_FILE_NAME = "bounds"; //$NON-NLS-1$

    private final Queue<TmfTraceElement> fTraceBoundsToUpdate;

    /**
     * Constructor.
     *
     * @param name
     *            The name of the job
     * @param traceBoundsToUpdate
     *            Queue of TmfTraceElement to update
     */
    public UpdateTraceBoundsJob(String name, Queue<TmfTraceElement> traceBoundsToUpdate) {
        super(name);
        fTraceBoundsToUpdate = traceBoundsToUpdate;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        SubMonitor subMonitor = SubMonitor.convert(monitor, fTraceBoundsToUpdate.size());
        while (!fTraceBoundsToUpdate.isEmpty()) {
            subMonitor.setTaskName(getName());
            if (subMonitor.isCanceled()) {
                return Status.CANCEL_STATUS;
            }

            TmfTraceElement tElement = fTraceBoundsToUpdate.poll();
            ITmfTimestamp start = tElement.getStartTime();
            ITmfTimestamp end = tElement.getEndTime();
            if (start != null && end != null) {
                /*
                 * The start and end times are already known, no need to go any
                 * further
                 */
                subMonitor.worked(1);
                continue;
            }
            /*
             * Try to get the element bounds from the supplementary files.
             */
            IFolder folder = tElement.getTraceSupplementaryFolder(tElement.getSupplementaryFolderPath());
            tElement.refreshSupplementaryFolder();
            File f = folder.getFile(BOUNDS_FILE_NAME).getLocation().toFile();
            tryReadBoundsFile(tElement, f);

            start = tElement.getStartTime();
            end = tElement.getEndTime();
            if (start == null || end == null) {
                /*
                 * We are missing a bound, we must go and read them from the
                 * trace.
                 */
                extractBoundsFromTrace(tElement);
                tryWriteBoundsFile(subMonitor.newChild(1), tElement, folder, f);
            } else {
                subMonitor.worked(1);
            }
        }
        return Status.OK_STATUS;
    }

    /**
     * Extract the bounds from a trace and refresh its trace elements
     *
     * @param traceElement
     *            the trace element to refresh
     */
    private static void extractBoundsFromTrace(TmfTraceElement traceElement) {
        ITmfTimestamp start;
        ITmfTimestamp end;
        ITmfTrace trace = traceElement.getTrace();
        boolean wasInitBefore = (trace != null);
        if (!wasInitBefore) {
            trace = traceElement.instantiateTrace();
        }

        if (trace == null) {
            /*
             * We could not instantiate the trace because its type is unknown,
             * abandon.
             */
            traceElement.setStartTime(TmfTimestamp.BIG_BANG);
            traceElement.setEndTime(TmfTimestamp.BIG_BANG);
        } else {
            try {
                if (!wasInitBefore) {
                    trace.initTrace(traceElement.getResource(), traceElement.getResource().getLocation().toOSString(),
                            traceElement.instantiateEvent().getClass(), traceElement.getElementPath(), traceElement.getTraceType());
                }
                start = trace.readStart();
                if (start != null) {
                    traceElement.setStartTime(start);
                    /*
                     * Intermediate refresh when we get the start time, will not
                     * re-trigger a job.
                     */
                    traceElement.refreshViewer();

                    end = trace.readEnd();
                    traceElement.setEndTime((end != null) ? end : TmfTimestamp.BIG_BANG);
                } else {
                    traceElement.setStartTime(TmfTimestamp.BIG_BANG);
                    traceElement.setEndTime(TmfTimestamp.BIG_BANG);
                }
            } catch (TmfTraceException e1) {
                /*
                 * Set the bounds to BIG_BANG to avoid trying to reread the
                 * trace.
                 */
                traceElement.setStartTime(TmfTimestamp.BIG_BANG);
                traceElement.setEndTime(TmfTimestamp.BIG_BANG);
                LOGGER.config("Failed to read time bounds from trace: " + traceElement.getName()); //$NON-NLS-1$
            } finally {
                /*
                 * Leave the trace at the same initialization status as
                 * previously.
                 */
                if (!wasInitBefore) {
                    trace.dispose();
                }
            }
        }
    }

    /**
     * Save the bounds for a trace to supplementary file and refresh elements
     *
     * @param monitor
     *            a progress monitor
     * @param traceElement
     *            the traceElement which we are refreshing
     * @param folder
     *            the IFolder for this trace elements supplementary files
     * @param bounds
     *            File file in which the trace's bounds will be persisted
     */
    private static void tryWriteBoundsFile(IProgressMonitor monitor, TmfTraceElement traceElement, IFolder folder, File boundsFile) {
        try {
            SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
            /*
             * Now that we know the bounds we can persist them to disk
             */
            boundsFile.createNewFile();
            ByteBuffer writeBuffer = ByteBuffer.allocate(2 * Long.BYTES);
            long writeStart = traceElement.getStartTime() != null ? traceElement.getStartTime().toNanos() : Long.MIN_VALUE;
            long writeEnd = traceElement.getEndTime() != null ? traceElement.getEndTime().toNanos() : Long.MIN_VALUE;
            writeBuffer.putLong(writeStart);
            writeBuffer.putLong(writeEnd);
            Files.write(boundsFile.toPath(), writeBuffer.array(), StandardOpenOption.WRITE);

            SubMonitor newChild = subMonitor.newChild(1);
            folder.refreshLocal(IResource.DEPTH_INFINITE, newChild);
            /*
             * It seems that the subTask name is never cleared. The current solution is to
             * set the name to null.
             */
            newChild.subTask(""); //$NON-NLS-1$
        } catch (IOException e) {
            LOGGER.config("Failed to write time bounds supplementary file for trace: " + traceElement.getName()); //$NON-NLS-1$
        } catch (CoreException e) {
            LOGGER.config("Failed to refresh supplementary files for trace: " + traceElement.getName()); //$NON-NLS-1$
        }
    }

    /**
     * Try and read the bounds from a trace element's supplementary data and
     * refresh the elements bounds.
     *
     * @param traceElement
     *            the trace element whose trace we must read and update
     * @param boundsFile
     *            Supplementary file with persisted trace bounds
     */
    private static void tryReadBoundsFile(TmfTraceElement traceElement, File boundsFile) {
        if (boundsFile.exists()) {
            /*
             * We have already read the start and end times for this trace, we
             * can get them from the supplementary file.
             */
            try {
                byte[] bytes = Files.readAllBytes(boundsFile.toPath());
                ByteBuffer readBuffer = ByteBuffer.allocate(2 * Long.BYTES);
                readBuffer.put(bytes);
                readBuffer.flip();
                /*
                 * If MIN_VALUE was written, then the bounds could not be read.
                 */
                long tmp = readBuffer.getLong();
                if (tmp == Long.MIN_VALUE) {
                    traceElement.setStartTime(TmfTimestamp.BIG_BANG);
                } else {
                    traceElement.setStartTime(TmfTimestamp.fromNanos(tmp));
                }
                tmp = readBuffer.getLong();
                if (tmp == Long.MIN_VALUE) {
                    traceElement.setEndTime(TmfTimestamp.BIG_BANG);
                } else {
                    traceElement.setEndTime(TmfTimestamp.fromNanos(tmp));
                }
                traceElement.refreshViewer();
            } catch (IOException e) {
                boundsFile.delete();
                LOGGER.config("Failed to read time bounds supplementary file for trace: " + traceElement.getName()); //$NON-NLS-1$
            }
        }
    }

}
