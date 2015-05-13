/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * CPU usage view. It contains 2 viewers: one tree viewer showing all the
 * threads who were on the CPU in the time range, and one XY chart viewer
 * plotting the total time spent on CPU and the time of the threads selected in
 * the tree viewer.
 *
 * @author Geneviève Bastien
 */
public class CpuUsageView extends TmfView implements ITmfTimeAligned {

    /** ID string */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.cpuusage"; //$NON-NLS-1$

    private static final int[] DEFAULT_WEIGHTS = {1, 3};

    private CpuUsageComposite fTreeViewer = null;
    private CpuUsageXYViewer fXYViewer = null;

    private SashForm fSashForm;
    private Listener fSashDragListener;
    /** A composite that allows us to add margins */
    private Composite fXYViewerContainer;

    /**
     * Constructor
     */
    public CpuUsageView() {
        super(Messages.CpuUsageView_Title);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        fSashForm = new SashForm(parent, SWT.NONE);

        fTreeViewer = new CpuUsageComposite(fSashForm);

        fXYViewerContainer = new Composite(fSashForm, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        fXYViewerContainer.setLayout(layout);

        /* Build the XY chart part of the view */
        fXYViewer = new CpuUsageXYViewer(fXYViewerContainer);
        fXYViewer.setSendTimeAlignSignals(true);
        fXYViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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

        /* Initialize the viewers with the currently selected trace */
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            fTreeViewer.traceSelected(signal);
            fXYViewer.traceSelected(signal);
        }
        fTreeViewer.getControl().addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                super.controlResized(e);
            }
        });

        fXYViewer.getControl().addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                // Sashes in a SashForm are being created on layout so add the
                // drag listener here
                if (fSashDragListener == null) {
                    for (Control control : fSashForm.getChildren()) {
                        if (control instanceof Sash) {
                            fSashDragListener = new Listener() {

                                @Override
                                public void handleEvent(Event event) {
                                    TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(fSashForm, getTimeViewAlignmentInfo()));
                                }
                            };
                            control.removePaintListener(this);
                            control.addListener(SWT.Selection, fSashDragListener);
                            // There should be only one sash
                            break;
                        }
                    }
                }
            }
        });

        fSashForm.setWeights(DEFAULT_WEIGHTS);
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

    /**
     * @since 1.0
     */
    @Override
    public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
        if (fSashForm == null) {
            return null;
        }

        return new TmfTimeViewAlignmentInfo(fSashForm.getShell(), fSashForm.toDisplay(0, 0), getTimeAxisOffset());
    }

    private int getTimeAxisOffset() {
        int[] weights = fSashForm.getWeights();
        int width = (int) (((float) weights[0] / (weights[0] + weights[1])) * fSashForm.getBounds().width);
        int curTimeAxisOffset = width + fSashForm.getSashWidth() + fXYViewer.getPointAreaOffset();
        return curTimeAxisOffset;
    }

    /**
     * @since 1.0
     */
    @Override
    public int getAvailableWidth(int requestedOffset) {
        int pointAreaWidth = fXYViewer.getPointAreaWidth();
        int curTimeAxisOffset = getTimeAxisOffset();
        if (pointAreaWidth <= 0) {
            pointAreaWidth = fSashForm.getBounds().width - curTimeAxisOffset;
        }
        // TODO this is just an approximation that assumes that the end will be at the same position but that can change for a different data range/scaling
        int endOffset = curTimeAxisOffset + pointAreaWidth;
        GridLayout layout = (GridLayout) fXYViewerContainer.getLayout();
        int endOffsetWithoutMargin = endOffset + layout.marginRight;
        int availableWidth = endOffsetWithoutMargin - requestedOffset;
        availableWidth = Math.min(fSashForm.getBounds().width, Math.max(0, availableWidth));
        return availableWidth;
    }

    /**
     * @since 1.0
     */
    @Override
    public void performAlign(int offset, int width) {
        int total = fSashForm.getBounds().width;
        int plotAreaOffset = fXYViewer.getPointAreaOffset();
        int width1 = Math.max(0, offset - plotAreaOffset - fSashForm.getSashWidth());
        int width2 = Math.max(0, total - width1 - fSashForm.getSashWidth());
        fSashForm.setWeights(new int[] { width1, width2 });
        fSashForm.layout();

        Composite composite = fXYViewerContainer;
        GridLayout layout = (GridLayout) composite.getLayout();
        int timeAxisWidth = getAvailableWidth(offset);
        int marginSize = timeAxisWidth - width;
        layout.marginRight = Math.max(0, marginSize);
        composite.layout();
    }
}
