/*******************************************************************************
 * Copyright (c) 2013, 2014 Kalray, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *   Bernd Hufmann - Adapted to new events table column API
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.viewers.events.columns.TmfEventTableColumn;
import org.eclipse.ui.PlatformUI;

/**
 * This handler exports traces to text files.
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExportToTextCommandHandler extends AbstractHandler {

    /** Id of the export-to-text command */
    public static final String COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.exportToText"; //$NON-NLS-1$
    /**
     * Id used to retrieve the header (as a String) of the trace to export.
     * This header is from the application context of this handler.
     */
    public static final String TMF_EVENT_TABLE_COLUMNS_ID = "org.eclipse.linuxtools.tmf.ui.exportToText.columns"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public ExportToTextCommandHandler() {
        // Do nothing
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        List<TmfEventTableColumn> columns = getColumns(event.getApplicationContext());
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        ITmfFilter filter = TmfTraceManager.getInstance().getCurrentTraceContext().getFilter();
        if (trace != null) {
            FileDialog fd = TmfFileDialogFactory.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
            fd.setFilterExtensions(new String[] { "*.csv", "*.*", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            fd.setOverwrite(true);
            final String s = fd.open();
            if (s != null) {
                Job j = new ExportToTextJob(trace, filter, columns, s);
                j.setUser(true);
                j.schedule();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<TmfEventTableColumn> getColumns(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            Object s = ((IEvaluationContext) evaluationContext).getVariable(TMF_EVENT_TABLE_COLUMNS_ID);
            if (s instanceof List<?>) {
                return (List<TmfEventTableColumn>) s;
            }
        }
        return null;
    }

}
