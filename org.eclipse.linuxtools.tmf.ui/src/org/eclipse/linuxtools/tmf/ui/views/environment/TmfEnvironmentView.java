/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Bernd Hufmann - Updated to use Tree with columns to be able to group traces
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.environment;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Displays the CTF trace properties.
 *
 * @version 1.1
 * @author Matthew Khouzam
 */
public class TmfEnvironmentView extends TmfView {

    /** The Environment View's ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.environment"; //$NON-NLS-1$

    private ITmfTrace fTrace;
    private Tree fTree;

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

    @Override
    public void createPartControl(Composite parent) {
        fTree = new Tree(parent, SWT.NONE);
        TreeColumn nameCol = new TreeColumn(fTree, SWT.NONE, 0);
        TreeColumn valueCol = new TreeColumn(fTree, SWT.NONE, 1);
        nameCol.setText("Environment Variable"); //$NON-NLS-1$
        valueCol.setText("Value"); //$NON-NLS-1$

        fTree.setItemCount(0);

        fTree.setHeaderVisible(true);
        nameCol.pack();
        valueCol.pack();

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    private void updateTable() {
        fTree.setItemCount(0);
        if (fTrace == null) {
            return;
        }

        for (ITmfTrace trace : fTraceManager.getActiveTraceSet()) {
            if (trace instanceof CtfTmfTrace) {
                TreeItem item = new TreeItem(fTree, SWT.NONE);
                item.setText(0, trace.getName());
                CtfTmfTrace ctfTrace = (CtfTmfTrace) trace;
                for (String varName : ctfTrace.getEnvNames()) {
                    TreeItem subItem = new TreeItem(item, SWT.NONE);
                    subItem.setText(0, varName);
                    subItem.setText(1, ctfTrace.getEnvValue(varName));
                }
            }
        }

        // Expand the tree items
        for (int i = 0; i < fTree.getItemCount(); i++) {
            fTree.getItem(i).setExpanded(true);
        }

        for (TreeColumn column : fTree.getColumns()) {
            column.pack();
        }
    }

    @Override
    public void setFocus() {
        fTree.setFocus();
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
            fTree.setItemCount(0);
        }
    }

}

