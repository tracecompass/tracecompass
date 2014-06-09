/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpuusage;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * CPU usage view. It contains 2 viewers: one tree viewer showing all the
 * threads who were on the CPU in the time range, and one XY chart viewer
 * plotting the total time spent on CPU and the time of the threads selected in
 * the tree viewer.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageView extends TmfView {

    /** ID string */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.cpuusage"; //$NON-NLS-1$

    private CpuUsageComposite fTreeViewer = null;
    private CpuUsageXYViewer fXYViewer = null;

    /**
     * Constructor
     */
    public CpuUsageView() {
        super(Messages.CpuUsageView_Title);
    }

    @Override
    public void createPartControl(Composite parent) {

        final SashForm sash = new SashForm(parent, SWT.NONE);

        fTreeViewer = new CpuUsageComposite(sash);

        /* Build the XY chart part of the view */
        fXYViewer = new CpuUsageXYViewer(sash);

        /* Add selection listener to tree viewer */
        fTreeViewer.addSelectionChangeListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object structSelection = ((IStructuredSelection) selection).getFirstElement();
                    if (structSelection instanceof CpuUsageEntry) {
                        CpuUsageEntry entry = (CpuUsageEntry) structSelection;
                        fTreeViewer.setSelectedThread(entry.getTid());
                        fXYViewer.setSelectedThread(Long.valueOf(entry.getTid()));
                    }
                }
            }
        });

        sash.setLayout(new FillLayout());

        /* Initialize the viewers with the currently selected trace */
        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            fTreeViewer.traceSelected(signal);
            fXYViewer.traceSelected(signal);
        }

    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fTreeViewer != null) {
            fTreeViewer.dispose();
        }
        if (fXYViewer != null) {
            fXYViewer.dispose();
        }
    }

}
