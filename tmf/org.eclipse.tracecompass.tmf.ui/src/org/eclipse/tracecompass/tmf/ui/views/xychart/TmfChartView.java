/**********************************************************************
 * Copyright (c) 2013, 2020 Ericsson, Draeger, Auriga
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.views.xychart;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swtchart.Chart;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.xychart.TmfXyUiUtils;
import org.eclipse.tracecompass.internal.tmf.ui.views.xychart.LockRangeDialog;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.ILegendImageProvider2;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfTimeViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractSelectTreeViewer2;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.XYChartLegendImageProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.ITimeReset;
import org.eclipse.tracecompass.tmf.ui.views.ITmfAllowMultiple;
import org.eclipse.tracecompass.tmf.ui.views.ITmfPinnable;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.views.ManyEntriesSelectedDialogPreCheckedListener;
import org.eclipse.tracecompass.tmf.ui.views.ResetUtil;
import org.eclipse.tracecompass.tmf.ui.views.SaveImageUtil;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.TriStateFilteredCheckboxTree;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * Base class to be used with a chart viewer {@link TmfXYChartViewer}.
 * It is responsible to instantiate the viewer class and load the trace
 * into the viewer when the view is created.
 *
 * @author Bernd Hufmann
 * @author Mikael Ferland
 * @since 6.0
 */
public abstract class TmfChartView extends TmfView implements ITmfTimeAligned, ITimeReset, ITmfPinnable, ITmfAllowMultiple {

    private static final int[] DEFAULT_WEIGHTS = {1, 3};
    private static final String TMF_VIEW_UI_CONTEXT = "org.eclipse.tracecompass.tmf.ui.view.context"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** The TMF XY Chart reference */
    private TmfXYChartViewer fChartViewer;
    /** A composite that allows us to add margins */
    private Composite fXYViewerContainer;
    private TmfViewer fTmfViewer;
    private SashForm fSashForm;
    private Listener fSashDragListener;
    /** The original view title */
    private String fOriginalTabLabel;

    private final Action fResetScaleAction = ResetUtil.createResetAction(this);
    private Action fZoomInAction;
    private Action fZoomOutAction;

    private List<IContextActivation> fActiveContexts = new ArrayList<>();
    private IContextService fContextService;

    /** The clamp to the Y axis action */
    private @Nullable IAction fClampAction = null;

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
     * @since 5.0
     */
    public TmfXYChartViewer getChartViewer() {
        return fChartViewer;
    }

    /**
     * Returns the left TMF viewer implementation.
     *
     * @return the left TMF viewer {@link TmfViewer}
     * @since 5.0
     */
    public TmfViewer getLeftChildViewer() {
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
     * @since 3.3
     */
    @Override
    public ITmfTrace getTrace() {
        TmfXYChartViewer chartViewer = getChartViewer();
        if (chartViewer != null) {
            return chartViewer.getTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

        fClampAction = createClampAction();
        menuManager.add(new Separator());
        menuManager.add(fClampAction);

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
                            fSashDragListener = event -> TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(fSashForm, getTimeViewAlignmentInfo()));
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
        fZoomInAction = getZoomInAction();
        fZoomOutAction = getZoomOutAction();

        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fResetScaleAction);
        toolBarManager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fZoomInAction);
        toolBarManager.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fZoomOutAction);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            loadTrace();
        }

        IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        fChartViewer.setStatusLineManager(statusLineManager);

        fOriginalTabLabel = getPartName();
        coupleSelectViewer();

        IWorkbenchPartSite site = getSite();
        fContextService = site.getWorkbenchWindow().getService(IContextService.class);

        TmfXYChartViewer chartViewer = getChartViewer();
        if (chartViewer != null) {
            chartViewer.getControl().addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    deactivateContextService();
                }
                @Override
                public void focusGained(FocusEvent e) {
                    activateContextService();
                }
            });
        }
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
        /* Force activation because focus gained event is not always received */
        activateContextService();
    }

    /**
     * Load the trace into view.
     */
    protected void loadTrace() {
        // Initialize the tree viewer with the currently selected trace
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            TmfTraceSelectedSignal signal = new TmfTraceSelectedSignal(this, trace);
            TmfViewer leftViewer = getLeftChildViewer();
            if (leftViewer instanceof TmfTimeViewer) {
                ((TmfTimeViewer) leftViewer).traceSelected(signal);
            }
            TmfXYChartViewer chartViewer = getChartViewer();
            if (chartViewer != null) {
                chartViewer.traceSelected(signal);
            }
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
        TmfWindowRangeUpdatedSignal signal = new TmfWindowRangeUpdatedSignal(this, TmfTimeRange.ETERNITY, getTrace());
        if (notify) {
            broadcast(signal);
        } else {
            getChartViewer().windowRangeUpdated(signal);
        }
    }

    @Override
    public void setPinned(@Nullable ITmfTrace trace) {

        TmfViewer leftViewer = getLeftChildViewer();
        if (leftViewer instanceof ITmfPinnable) {
            ((ITmfPinnable) leftViewer).setPinned(trace);
        }

        ITmfPinnable chartViewer = getChartViewer();
        if (chartViewer != null) {
            chartViewer.setPinned(trace);
        }

        if (trace != null) {
            /* Ignore relevant inbound signals */
            TmfSignalManager.addIgnoredInboundSignal(this, TmfTraceOpenedSignal.class);
            TmfSignalManager.addIgnoredInboundSignal(this, TmfTraceSelectedSignal.class);
            setPartName(String.format("%s <%s>", fOriginalTabLabel, TmfTraceManager.getInstance().getTraceUniqueName(trace))); //$NON-NLS-1$
        } else {
            /* Handle relevant inbound signals */
            TmfSignalManager.removeIgnoredInboundSignal(this, TmfTraceOpenedSignal.class);
            TmfSignalManager.removeIgnoredInboundSignal(this, TmfTraceSelectedSignal.class);
            setPartName(fOriginalTabLabel);
        }
        if (fPinAction != null) {
            fPinAction.setPinnedTrace(trace);
        }
    }

    private IAction createClampAction() {
        Action action = new Action(Messages.TmfChartView_LockYAxis, IAction.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                LockRangeDialog rangeDialog = new LockRangeDialog(getSite().getShell(), getChartViewer());
                rangeDialog.open();
            }
        };
        action.setChecked(false);
        return action;
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
            // Do nothing
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

    /**
     * Handles the trace closed signal
     *
     * @param signal
     *            the signal
     * @since 3.3
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        ITmfTrace trace = getTrace();
        if ((trace == null || signal.getTrace() == trace) && isPinned()) {
            setPinned(null);
        }
    }

    /**
     * Method to couple {@link AbstractSelectTreeViewer2} and
     * {@link TmfFilteredXYChartViewer} so that they use the same legend and that
     * the chart listens to selected items in the tree
     */
    private void coupleSelectViewer() {
        TmfViewer tree = getLeftChildViewer();
        TmfXYChartViewer chart = getChartViewer();
        if (tree instanceof AbstractSelectTreeViewer2 && chart instanceof TmfFilteredXYChartViewer) {
            ILegendImageProvider2 legendImageProvider = new XYChartLegendImageProvider((TmfCommonXAxisChartViewer) chart);
            AbstractSelectTreeViewer2 selectTree = (AbstractSelectTreeViewer2) tree;
            selectTree.addTreeListener((TmfFilteredXYChartViewer) chart);
            selectTree.setLegendImageProvider(legendImageProvider);
            TriStateFilteredCheckboxTree checkboxTree = selectTree.getTriStateFilteredCheckboxTree();
            checkboxTree.addPreCheckStateListener(new ManyEntriesSelectedDialogPreCheckedListener(checkboxTree));
        }
    }

    private void activateContextService() {
        if (fActiveContexts.isEmpty()) {
            fActiveContexts.add(fContextService.activateContext(TMF_VIEW_UI_CONTEXT));
        }
    }

    private void deactivateContextService() {
        fContextService.deactivateContexts(fActiveContexts);
        fActiveContexts.clear();
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        TmfXYChartViewer chart = getChartViewer();
        if (chart != null) {
            return chart.getAdapter(adapter);
        }
        return super.getAdapter(adapter);
    }

    private Action getZoomInAction() {
        Action zoomInAction = fZoomInAction;
        if (zoomInAction == null) {
            zoomInAction = new Action() {
                @Override
                public void run() {
                    TmfXYChartViewer viewer = getChartViewer();
                    if (viewer == null) {
                        return;
                    }
                    Chart chart = viewer.getSwtChart();
                    if (chart == null) {
                        return;
                    }
                    TmfXyUiUtils.zoom(viewer, chart, true);
                }
            };
            zoomInAction.setText(Messages.TmfTimeGraphViewer_ZoomInActionNameText);
            zoomInAction.setToolTipText(Messages.TmfTimeGraphViewer_ZoomInActionToolTipText);
            zoomInAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_IN_MENU));
        }
        return zoomInAction;
    }

    private Action getZoomOutAction() {
        Action zoomOutAction = fZoomOutAction;
        if (zoomOutAction == null) {
            zoomOutAction = new Action() {
                @Override
                public void run() {
                    TmfXYChartViewer viewer = getChartViewer();
                    if (viewer == null) {
                        return;
                    }
                    Chart chart = viewer.getSwtChart();
                    if (chart == null) {
                        return;
                    }
                    TmfXyUiUtils.zoom(viewer, chart, false);
                }
            };
            zoomOutAction.setText(Messages.TmfTimeGraphViewer_ZoomOutActionNameText);
            zoomOutAction.setToolTipText(Messages.TmfTimeGraphViewer_ZoomOutActionToolTipText);
            zoomOutAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_ZOOM_OUT_MENU));
        }
        return zoomOutAction;
    }
}
