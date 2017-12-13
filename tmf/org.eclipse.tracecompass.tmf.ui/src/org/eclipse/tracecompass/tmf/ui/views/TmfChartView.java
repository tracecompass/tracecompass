/**********************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;

/**
 * Base class to be used with a chart viewer {@link TmfXYChartViewer}.
 * It is responsible to instantiate the viewer class and load the trace
 * into the viewer when the view is created.
 *
 * @author Bernd Hufmann
 * @author Mikael Ferland
 */
public abstract class TmfChartView extends TmfView implements ITmfTimeAligned, ITimeReset {

    private static final int[] DEFAULT_WEIGHTS = {1, 3};

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** The TMF XY Chart reference */
    private TmfXYChartViewer fChartViewer;
    /** The Trace reference */
    private ITmfTrace fTrace;
    /** A composite that allows us to add margins */
    private Composite fXYViewerContainer;
    private TmfViewer fTmfViewer;
    private SashForm fSashForm;
    private Listener fSashDragListener;

    private final Action fResetScaleAction = ResetUtil.createResetAction(this);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard Constructor
     *
     * @param viewName
     *            The view name
     */
    public TmfChartView(String viewName) {
        super(viewName);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the TMF XY chart viewer implementation.
     *
     * @return the TMF XY chart viewer {@link TmfXYChartViewer}
     */
    protected TmfXYChartViewer getChartViewer() {
        return fChartViewer;
    }

    /**
     * Returns the left TMF viewer implementation.
     *
     * @return the left TMF viewer {@link TmfViewer}
     * @since 3.1
     */
    protected TmfViewer getLeftChildViewer() {
        return fTmfViewer;
    }

    /**
     * Create a {@link TmfViewer} instance to be added to the left composite
     * of the sash. Default implementation provides an empty composite and
     * don't overwrite this method if not needed.
     *
     * @param parent
     *          the parent control
     * @return a {@link TmfViewer} instance
     * @since 2.0
     */
    protected @NonNull TmfViewer createLeftChildViewer(Composite parent) {
        return new EmptyViewer(parent);
    }

    /**
     * Create the TMF XY chart viewer implementation
     *
     * @param parent
     *            the parent control
     *
     * @return The TMF XY chart viewer {@link TmfXYChartViewer}
     * @since 1.0
     */
    abstract protected TmfXYChartViewer createChartViewer(Composite parent);

    /**
     * Returns the ITmfTrace implementation
     *
     * @return the ITmfTrace implementation {@link ITmfTrace}
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Sets the ITmfTrace implementation
     *
     * @param trace
     *            The ITmfTrace implementation {@link ITmfTrace}
     */
    protected void setTrace(ITmfTrace trace) {
        fTrace = trace;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        fSashForm = new SashForm(parent, SWT.NONE);
        fTmfViewer = createLeftChildViewer(fSashForm);
        fXYViewerContainer = new Composite(fSashForm, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        fXYViewerContainer.setLayout(layout);

        fChartViewer = createChartViewer(fXYViewerContainer);
        fChartViewer.setSendTimeAlignSignals(true);
        fChartViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fChartViewer.getSwtChart().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                super.mouseDoubleClick(e);
                resetStartFinishTime();
            }
        });

        fChartViewer.getControl().addPaintListener(new PaintListener() {
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
        getViewSite().getActionBars().getToolBarManager().add(fResetScaleAction);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            setTrace(trace);
            loadTrace();
        }

        IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        fChartViewer.setStatusLineManager(statusLineManager);
    }

    @Override
    protected IAction createSaveAction() {
        // FIXME export tree viewer or legend.
        return SaveImageUtil.createSaveAction(getName(), this::getChartViewer);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fChartViewer != null) {
            fChartViewer.dispose();
        }

        if (fTmfViewer != null) {
            fTmfViewer.dispose();
        }
    }

    @Override
    public void setFocus() {
        fChartViewer.getControl().setFocus();
    }

    /**
     * Load the trace into view.
     */
    protected void loadTrace() {
        if (fChartViewer != null) {
            fChartViewer.loadTrace(fTrace);
        }
    }

    /**
     * @since 1.0
     */
    @Override
    public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
        if (fChartViewer == null) {
            return null;
        }

        return new TmfTimeViewAlignmentInfo(fChartViewer.getControl().getShell(), fSashForm.toDisplay(0, 0), getTimeAxisOffset());
    }

    private int getTimeAxisOffset() {
        return fSashForm.getChildren()[0].getSize().x + fSashForm.getSashWidth() + fChartViewer.getPointAreaOffset();
    }

    /**
     * @since 1.0
     */
    @Override
    public int getAvailableWidth(int requestedOffset) {
        if (fChartViewer == null) {
            return 0;
        }

        int pointAreaWidth = fChartViewer.getPointAreaWidth();
        int curTimeAxisOffset = getTimeAxisOffset();
        if (pointAreaWidth <= 0) {
            pointAreaWidth = fSashForm.getBounds().width - curTimeAxisOffset;
        }
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
        int plotAreaOffset = fChartViewer.getPointAreaOffset();
        int width1 = Math.max(0, offset - plotAreaOffset - fSashForm.getSashWidth());
        int width2 = Math.max(0, total - width1 - fSashForm.getSashWidth());
        if (width1 >= 0 && width2 > 0 || width1 > 0 && width2 >= 0) {
            fSashForm.setWeights(new int[] { width1, width2 });
            fSashForm.layout();
        }

        Composite composite = fXYViewerContainer;
        GridLayout layout = (GridLayout) composite.getLayout();
        int timeAxisWidth = getAvailableWidth(offset);
        int marginSize = timeAxisWidth - width;
        layout.marginRight = Math.max(0, marginSize);
        composite.layout();
    }

    @Override
    public void resetStartFinishTime(boolean notify) {
        TmfWindowRangeUpdatedSignal signal = new TmfWindowRangeUpdatedSignal(this, TmfTimeRange.ETERNITY);
        if (notify) {
            broadcast(signal);
        } else {
            getChartViewer().windowRangeUpdated(signal);
        }
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------
    /**
     * Empty @{link TmfViewer} class.
     */
    private class EmptyViewer extends TmfViewer {
        private Composite fComposite;
        public EmptyViewer(Composite parent) {
            super(parent);
            fComposite = new Composite(parent, SWT.NONE);
        }
        @Override
        public void refresh() {
        }
        @Override
        public Control getControl() {
            return fComposite;
        }
    }

    /**
     * Returns whether or not this chart viewer is dirty. The viewer is considered
     * dirty if it has yet to completely update its model. This method is meant to
     * be used by tests in order to know when it is safe to proceed.
     *
     * @return true if the time graph view has yet to completely update its model,
     *         false otherwise
     * @since 3.2
     */
    public boolean isDirty() {
        return fChartViewer.isDirty();
    }

}
