/*******************************************************************************
 * Copyright (c) 2013, 2014 Kalray, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *   Bernd Hufmann - Adapted to new events table column API
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.osgi.util.NLS;
import org.eclipse.linuxtools.tmf.ui.viewers.events.columns.TmfEventTableColumn;

/**
 * This job exports traces to text files.
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExportToTextJob extends Job {

    private static final int TOTAL_WORK = 100;
    private static final int SLEEPING_INTERVAL = 100;

    /** the ExportToCSV job family */
    public static final Object ExportToCSVJobFamily = new Object();

    private final ITmfTrace fTrace;
    private final ITmfFilter fFilter;
    private final List<TmfEventTableColumn> fColumns;
    private final String destination;

    /**
     * Job constructor.
     *
     * @param trace
     *            the trace to export
     * @param filter
     *            the filter to apply when exporting the trace. may be null.
     * @param columns
     *            the header to put at top of the exported file (may be <code>null</code>)
     * @param destination
     *            the path of the file where the data is exported.
     */
    public ExportToTextJob(ITmfTrace trace, ITmfFilter filter, List<TmfEventTableColumn> columns, String destination) {
        super(MessageFormat.format(Messages.ExportToTextJob_Export_to, destination));
        this.fTrace = trace;
        this.fFilter = filter;
        this.fColumns = columns;
        this.destination = destination;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(NLS.bind(Messages.ExportToTextJob_Export_trace_to, destination), TOTAL_WORK);
        IStatus ret = saveImpl(monitor);
        monitor.done();
        return ret;
    }

    private IStatus saveImpl(IProgressMonitor monitor) {
        try (final BufferedWriter bw = new BufferedWriter(new FileWriter(destination));) {
            if (fColumns != null) {
                boolean needTab = false;
                for (TmfEventTableColumn column : fColumns) {
                    if (needTab) {
                        bw.write('\t');
                    }
                    bw.write(column.getHeaderName());
                    needTab = true;
                }
                bw.append('\n');
            }
            return saveImpl(bw, monitor);
        } catch (IOException ex) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    MessageFormat.format(Messages.ExportToTextJob_Unable_to_export_trace, destination),
                    ex);
            return status;
        }
    }

    private IStatus saveImpl(Writer bw, IProgressMonitor monitor) {
        ExportToTextRequest request = new ExportToTextRequest(bw, fFilter, fColumns);
        fTrace.sendRequest(request);
        int currentIndex = 0;
        while (!request.isCompleted()) {
            if (monitor.isCanceled()) {
                request.cancel();
                return Status.CANCEL_STATUS;
            }
            int index = (int) (request.getNbRead() * TOTAL_WORK / fTrace.getNbEvents());
            if (index > currentIndex) {
                int progress = index - currentIndex;
                monitor.worked(progress);
                currentIndex = index;
            }
            try {
                Thread.sleep(SLEEPING_INTERVAL);
            } catch (InterruptedException e) {
            }
        }
        if (request.isFailed()) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    MessageFormat.format(Messages.ExportToTextJob_Unable_to_export_trace, destination),
                    request.getIOException());
            return status;
        }
        return Status.OK_STATUS;
    }

    @Override
    public boolean belongsTo(Object family) {
        return ExportToCSVJobFamily.equals(family);
    }

}
