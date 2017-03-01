/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Add support for unknown trace type icon
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.model;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

/**
 * The TMF project label provider for the tree viewer in the project explorer view.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfNavigatorLabelProvider implements ICommonLabelProvider, IStyledLabelProvider {

    private static final Logger LOGGER = TraceCompassLog.getLogger(TmfNavigatorLabelProvider.class);

    private static final String BOUNDS_FILE_NAME = "bounds"; //$NON-NLS-1$
    private static Queue<TmfTraceElement> boundsToUpdate = new ConcurrentLinkedQueue<>();
    private static Job updateBounds = new Job(Messages.TmfNavigatorLabelProvider_UpdateBoundsJobName) {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            while (!boundsToUpdate.isEmpty()) {
                TmfTraceElement tElement = boundsToUpdate.poll();
                ITmfTimestamp start = tElement.getStartTime();
                ITmfTimestamp end = tElement.getEndTime();
                if (start != null && end != null) {
                    /*
                     * The start and end times are already known, no need to go
                     * any further
                     */
                    continue;
                }
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
                    tryWriteBoundsFile(monitor, tElement, folder, f);
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
        private void extractBoundsFromTrace(TmfTraceElement traceElement) {
            ITmfTimestamp start;
            ITmfTimestamp end;
            ITmfTrace trace = traceElement.getTrace();
            boolean wasInitBefore = (trace != null);
            if (!wasInitBefore) {
                trace = traceElement.instantiateTrace();
            }

            if (trace == null) {
                /*
                 * We could not instantiate the trace because its type is
                 * unknown, abandon.
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
                         * Intermediate refresh when we get the start time, will
                         * not re-trigger a job.
                         */
                        traceElement.refresh();

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
         * Save the bounds for a trace to supplementary file and refresh
         * elements
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
        private void tryWriteBoundsFile(IProgressMonitor monitor, TmfTraceElement traceElement, IFolder folder, File boundsFile) {
            try {
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
                folder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
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
        private void tryReadBoundsFile(TmfTraceElement traceElement, File boundsFile) {
            if (boundsFile.exists()) {
                /*
                 * We have already read the start and end times for this trace,
                 * we can get them from he supplementary file.
                 */
                try {
                    byte[] bytes = Files.readAllBytes(boundsFile.toPath());
                    ByteBuffer readBuffer = ByteBuffer.allocate(2 * Long.BYTES);
                    readBuffer.put(bytes);
                    readBuffer.flip();
                    /*
                     * If MIN_VALUE was written, then the bounds could not be
                     * read.
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
                    traceElement.refresh();
                } catch (IOException e) {
                    boundsFile.delete();
                    LOGGER.config("Failed to read time bounds supplementary file for trace: " + traceElement.getName()); //$NON-NLS-1$
                }
            }
        }
    };

    // ------------------------------------------------------------------------
    // ICommonLabelProvider
    // ------------------------------------------------------------------------

    @Override
    public Image getImage(Object element) {
        if (element instanceof ITmfProjectModelElement) {
            return ((ITmfProjectModelElement) element).getIcon();
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ITmfProjectModelElement) {
            return ((ITmfProjectModelElement) element).getLabelText();
        }
        return null;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void restoreState(IMemento aMemento) {
    }

    @Override
    public void saveState(IMemento aMemento) {
    }

    @Override
    public String getDescription(Object anElement) {
        return getText(anElement);
    }

    @Override
    public void init(ICommonContentExtensionSite aConfig) {
    }

    @Override
    public StyledString getStyledText(Object element) {
        String text = getText(element);
        StyledString styledString = null;
        if (text != null) {
            if (element instanceof ITmfStyledProjectModelElement) {
                Styler styler = ((ITmfStyledProjectModelElement) element).getStyler();
                if (styler != null) {
                    styledString = new StyledString(text, styler);
                }
            }
            styledString = new StyledString(text);
            boolean displayTimeRange = Activator.getDefault().getPreferenceStore().getBoolean(ITmfUIPreferences.TRACE_DISPLAY_RANGE_PROJECTEXPLORER);
            if (displayTimeRange && element instanceof TmfTraceElement) {
                styledString.append(formatTraceRange((TmfTraceElement) element));
            }
        }
        return styledString;
    }

    private static StyledString formatTraceRange(TmfTraceElement traceElement) {
        ITmfTimestamp start = traceElement.getStartTime();
        ITmfTimestamp end = traceElement.getEndTime();

        if (start == null) {
            boundsToUpdate.add(traceElement);
            if (updateBounds.getState() != Job.RUNNING) {
                updateBounds.schedule();
            }
            return new StyledString(" [...]", StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
        }

        if (start.equals(TmfTimestamp.BIG_BANG)) {
            /* Not a trace or empty */
            return new StyledString();
        }

        if (end == null || end.equals(TmfTimestamp.BIG_BANG)) {
            return new StyledString(" [" + TmfTimestampFormat.getDefaulTimeFormat().format(start.toNanos()) //$NON-NLS-1$
                    + " - ...]", //$NON-NLS-1$
                    StyledString.DECORATIONS_STYLER);
        }

        return new StyledString(" [" + TmfTimestampFormat.getDefaulTimeFormat().format(start.toNanos()) //$NON-NLS-1$
                + " - " + TmfTimestampFormat.getDefaulTimeFormat().format(end.toNanos()) + "]", //$NON-NLS-1$ //$NON-NLS-2$
                StyledString.DECORATIONS_STYLER);
    }

}
