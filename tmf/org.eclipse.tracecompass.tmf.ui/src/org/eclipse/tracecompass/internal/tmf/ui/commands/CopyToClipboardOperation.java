/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.commands;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.ui.PlatformUI;

/**
 * This operation copies the text of selected trace events to the clipboard.
 */
public class CopyToClipboardOperation implements IRunnableWithProgress {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
    private final ITmfTrace fTrace;
    private final ITmfFilter fFilter;
    private final List<TmfEventTableColumn> fColumns;
    private final long fStartRank;
    private final long fEndRank;

    /**
     * Constructor.
     *
     * @param trace
     *            the trace to copy events from
     * @param filter
     *            the filter to apply to trace events, or null
     * @param columns
     *            the list of event table columns
     * @param start
     *            the start rank of the selection
     * @param end
     *            the end rank of the selection
     */
    public CopyToClipboardOperation(ITmfTrace trace, ITmfFilter filter, List<TmfEventTableColumn> columns, long start, long end) {
        fTrace = trace;
        fFilter = filter;
        fColumns = columns;
        fStartRank = start;
        fEndRank = end;
    }

    @Override
    public void run(IProgressMonitor monitor) {
        final StringBuilder sb = new StringBuilder();
        monitor.beginTask(Messages.CopyToClipboardOperation_TaskName, (int) (fEndRank - fStartRank + 1));

        boolean needTab = false;
        for (TmfEventTableColumn column : fColumns) {
            if (needTab) {
                sb.append('\t');
            }
            sb.append(column.getHeaderName());
            needTab = true;
        }
        sb.append(LINE_SEPARATOR);

        copy(sb, monitor);

        Display.getDefault().syncExec(() -> {
            if (sb.length() == 0) {
                return;
            }
            Clipboard clipboard = new Clipboard(Display.getDefault());
            try {
                clipboard.setContents(new Object[] { sb.toString() },
                        new Transfer[] { TextTransfer.getInstance() });
            } catch (OutOfMemoryError e) {
                sb.setLength(0);
                sb.trimToSize();
                showErrorDialog();
            } finally {
                clipboard.dispose();
            }
        });

        monitor.done();
    }

    private IStatus copy(final StringBuilder sb, final IProgressMonitor monitor) {
        ITmfEventRequest request = new TmfEventRequest(ITmfEvent.class, TmfTimeRange.ETERNITY, fStartRank, (int) (fEndRank - fStartRank + 1), ExecutionType.FOREGROUND) {
            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (monitor.isCanceled()) {
                    cancel();
                    return;
                }
                monitor.worked(1);
                if (fFilter == null || fFilter.matches(event)) {
                    try {
                        boolean needTab = false;
                        for (TmfEventTableColumn column : fColumns) {
                            if (needTab) {
                                sb.append('\t');
                            }
                            sb.append(column.getItemString(event));
                            needTab = true;
                        }
                        sb.append(LINE_SEPARATOR);
                    } catch (OutOfMemoryError e) {
                        sb.setLength(0);
                        sb.trimToSize();
                        showErrorDialog();
                        cancel();
                    }
                }
            }
        };
        fTrace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            Activator.getDefault().logError("Wait for completion interrupted for copy to clipboard ", e); //$NON-NLS-1$
        }
        return Status.OK_STATUS;
    }

    private static void showErrorDialog() {
        Display.getDefault().syncExec(() -> {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            MessageBox confirmOperation = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
            confirmOperation.setText(Messages.CopyToClipboardOperation_OutOfMemoryErrorTitle);
            confirmOperation.setMessage(Messages.CopyToClipboardOperation_OutOfMemoryErrorMessage);
            confirmOperation.open();
        });
    }
}
