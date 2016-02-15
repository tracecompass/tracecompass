/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.cpuusage;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

/**
 * CPU usage view. It contains 2 viewers: one tree viewer showing all the
 * threads who were on the CPU in the time range, and one XY chart viewer
 * plotting the total time spent on CPU and the time of the threads selected in
 * the tree viewer.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageView extends TmfChartView {

    /** ID string */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.cpuusage"; //$NON-NLS-1$

    private @Nullable CpuUsageComposite fTreeViewer = null;
    private @Nullable CpuUsageXYViewer fXYViewer = null;

    /**
     * Constructor
     */
    public CpuUsageView() {
        super(Messages.CpuUsageView_Title);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        /* Initialize the viewers with the currently selected trace */
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            if (fTreeViewer != null) {
                fTreeViewer.traceSelected(signal);
            }
            if (fXYViewer != null) {
                fXYViewer.traceSelected(signal);
            }
        }
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        CpuUsageXYViewer viewer = new CpuUsageXYViewer(parent);
        viewer.setSendTimeAlignSignals(true);
        fXYViewer = viewer;
        return viewer;
    }

    @Override
    public TmfViewer createLeftChildViewer(Composite parent) {
        final CpuUsageComposite viewer = new CpuUsageComposite(parent);

        /* Add selection listener to tree viewer */
        viewer.addSelectionChangeListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object structSelection = ((IStructuredSelection) selection).getFirstElement();
                    if (structSelection instanceof CpuUsageEntry) {
                        CpuUsageEntry entry = (CpuUsageEntry) structSelection;
                        if (fTreeViewer != null) {
                            fTreeViewer.setSelectedThread(entry.getTid());
                        }
                        if (fXYViewer != null) {
                            fXYViewer.setSelectedThread(Long.valueOf(entry.getTid()));
                        }
                    }
                }
            }
        });

        viewer.getControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                super.controlResized(e);
            }
        });

        fTreeViewer = viewer;
        return fTreeViewer;
    }

    @Override
    public void setFocus() {
        if (fXYViewer != null) {
            fXYViewer.getControl().setFocus();
        }
    }

}
