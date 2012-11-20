/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.environment;

import java.util.ArrayList;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;

/**
 * Displays the CTF trace properties.
 *
 * @version 1.0
 * @author Matthew Khouzam
 */
public class TmfEnvironmentView extends TmfView {

    /** The Environment View's ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.environment"; //$NON-NLS-1$

    private ITmfTrace fTrace;
    private Table fTable;

    /**
     * Default constructor
     */
    public TmfEnvironmentView() {
        super("EnvironmentVariables"); //$NON-NLS-1$
//        fTitlePrefix = getTitle();
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------
    final private class Pair{
        final private String key;
        final private String value;
        public Pair(String k) { key = k ; value = "";} //$NON-NLS-1$
        public Pair(String k, String v){ key = k; value = v; }
        public String getKey() { return key; }
        public String getValue() { return value; }
    }

    @Override
    public void createPartControl(Composite parent) {
        fTable = new Table(parent, SWT.NONE);
        TableColumn nameCol = new TableColumn(fTable, SWT.NONE, 0);
        TableColumn valueCol = new TableColumn(fTable, SWT.NONE, 1);
        nameCol.setText("Environment Variable"); //$NON-NLS-1$
        valueCol.setText("Value"); //$NON-NLS-1$

        fTable.setItemCount(0);

        fTable.setHeaderVisible(true);
        nameCol.pack();
        valueCol.pack();

        IEditorPart editor = getSite().getPage().getActiveEditor();
        if (editor instanceof ITmfTraceEditor) {
            ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
            if (trace != null) {
                traceSelected(new TmfTraceSelectedSignal(this, trace));
            }
        }
    }

    private void updateTable() {
        fTable.setItemCount(0);
        if (fTrace == null) {
            return;
        }
        ITmfTrace[] traces;
        if (fTrace instanceof TmfExperiment) {
            TmfExperiment experiment = (TmfExperiment) fTrace;
            traces = experiment.getTraces();
        } else {
            traces = new ITmfTrace[] { fTrace };
        }
        ArrayList<Pair> tableData = new ArrayList<Pair>();
        for (ITmfTrace trace : traces) {
            Pair traceEntry = new Pair(trace.getName());
            tableData.add(traceEntry);
            if (trace instanceof CtfTmfTrace) {
                CtfTmfTrace ctfTrace = (CtfTmfTrace) trace;
                for (String varName : ctfTrace.getEnvNames()) {
                    tableData.add(new Pair(varName, ctfTrace.getEnvValue(varName)));
                }
            }
        }

        for (Pair pair : tableData) {
            TableItem item = new TableItem(fTable, SWT.NONE);
            item.setText(0, pair.getKey());
            item.setText(1, pair.getValue());
        }

        for (TableColumn column : fTable.getColumns()) {
            column.pack();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        fTable.setFocus();
    }

    /**
     * Handler for the trace selected signal.
     *
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        // Update the trace reference
        ITmfTrace trace = signal.getTrace();
        if (!trace.equals(fTrace)) {
            fTrace = trace;
            updateTable();
        }
    }

    /**
     * Handler for the trace closed signal.
     *
     * @param signal the incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        if (signal.getTrace() == fTrace) {
            fTrace = null;
            fTable.setItemCount(0);
        }
    }

}
