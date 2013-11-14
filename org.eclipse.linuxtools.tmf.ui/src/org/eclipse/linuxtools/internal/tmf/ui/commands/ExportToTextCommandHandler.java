/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

/**
 * This handler exports traces to text files.
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExportToTextCommandHandler extends AbstractHandler {

    /** Id of the export-to-text command */
    public static final String COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.exportToText"; //$NON-NLS-1$
    /**
     * Id used to retrieve the TmfEventsTable that launched this command.
     * This TmfEventsTable is taken from the application context of this handler.
     */
    public static final String TMF_EVENT_TABLE_PARAMETER_ID = "org.eclipse.linuxtools.tmf.ui.exportToText.table"; //$NON-NLS-1$
    /**
     * Id used to retrieve the header (as a String) of the trace to export.
     * This header is from the application context of this handler.
     */
    public static final String TMF_EVENT_TABLE_HEADER_ID = "org.eclipse.linuxtools.tmf.ui.exportToText.header"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public ExportToTextCommandHandler() {
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String header = getHeader(event.getApplicationContext());
        TmfEventsTable table = getTable(event.getApplicationContext());
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        ITmfFilter filter = TmfTraceManager.getInstance().getCurrentFilter();
        if (trace != null) {
            FileDialog fd = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
            fd.setFilterExtensions(new String[] { "*.csv", "*.*", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            fd.setOverwrite(true);
            final String s = fd.open();
            if (s != null) {
                Job j = new ExportToTextJob(trace, filter, table, header, s);
                j.setUser(true);
                j.schedule();
            }
        }
        return null;
    }

    private static String getHeader(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            Object s = ((IEvaluationContext) evaluationContext).getVariable(TMF_EVENT_TABLE_HEADER_ID);
            if (s instanceof String) {
                return s.toString();
            }
        }
        return null;
    }

    private static TmfEventsTable getTable(Object evaluationContext) {
        if (evaluationContext instanceof IEvaluationContext) {
            Object o = ((IEvaluationContext) evaluationContext).getVariable(TMF_EVENT_TABLE_PARAMETER_ID);
            if (o instanceof TmfEventsTable) {
                return (TmfEventsTable) o;
            }
        }
        return null;
    }

}
