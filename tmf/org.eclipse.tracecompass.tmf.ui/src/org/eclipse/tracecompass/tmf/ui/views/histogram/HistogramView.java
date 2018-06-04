/*******************************************************************************
 * Copyright (c) 2009, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   William Bourque - Initial API and implementation
 *   Yuriy Vashchuk - GUI reorganisation, simplification and some related code improvements.
 *   Yuriy Vashchuk - Histograms optimisation.
 *   Yuriy Vashchuk - Histogram Canvas Heritage correction
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Patrick Tasse - Update for mouse wheel zoom
 *   Xavier Raynaud - Support multi-trace coloring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.histogram;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalThrottler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IActionBars;

/**
 * The purpose of this view is to provide graphical time distribution statistics about the trace events.
 * <p>
 * The view is composed of two histograms and two controls:
 * <ul>
 * <li>an event distribution histogram for the whole trace;
 * <li>an event distribution histogram for current time window (window span);
 * <li>the timestamp of the currently selected event;
 * <li>the window span (size of the time window of the smaller histogram).
 * </ul>
 * The histograms x-axis show their respective time range.
 *
 * @version 2.0
 * @author Francois Chouinard
 */
public class HistogramView extends TmfView implements ITmfTimeAligned {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     *  The view ID as defined in plugin.xml
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.tmf.ui.views.histogram"; //$NON-NLS-1$

    private static final Image LINK_IMG = Activator.getDefault().getImageFromPath(ITmfImageConstants.IMG_UI_LINK);

    private static final int[] DEFAULT_WEIGHTS = {1, 3};

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The current trace
    private ITmfTrace fTrace;

    // Current timestamp/time window - everything in the TIME_SCALE
    private long fTraceStartTime;
    private long fTraceEndTime;
    private long fWindowStartTime;
    private long fWindowEndTime;
    private long fWindowSpan;
    private long fSelectionBeginTime;
    private long fSelectionEndTime;

    // SashForm
    private SashForm fSashForm;
    private ScrolledComposite fScrollComposite;
    private Composite fTimeControlsComposite;
    private Composite fTimeRangeComposite;
    private Listener fSashDragListener;

    // Time controls
    private HistogramTextControl fSelectionStartControl;
    private HistogramTextControl fSelectionEndControl;
    private HistogramTextControl fTimeSpanControl;

    // Link
    private Label fLinkButton;
    private boolean fLinkState;

    // Histogram/request for the full trace range
    private FullTraceHistogram fFullTraceHistogram;
    private HistogramRequest fFullTraceRequest;

    // Histogram/request for the selected time range
    private TimeRangeHistogram fTimeRangeHistogram;
    private HistogramRequest fTimeRangeRequest;

    // Legend area
    private Composite fLegendArea;
    private Image[] fLegendImages;

    // Throttlers for the time sync and time-range sync signals
    private final TmfSignalThrottler fTimeSyncThrottle;
    private final TmfSignalThrottler fTimeRangeSyncThrottle;

    // Action for toggle showing the lost events
    private Action hideLostEventsAction;
    // Action for toggle showing the traces
    private Action showTraceAction;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public HistogramView() {
        super(ID);
        fTimeSyncThrottle = new TmfSignalThrottler(this, 200);
        fTimeRangeSyncThrottle = new TmfSignalThrottler(this, 200);
    }

    @Override
    public void dispose() {
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        fFullTraceHistogram.dispose();
        fTimeRangeHistogram.dispose();
        fSelectionStartControl.dispose();
        fSelectionEndControl.dispose();
        fTimeSpanControl.dispose();
        disposeLegendImages();

        super.dispose();
    }

    private void disposeLegendImages() {
        if (fLegendImages != null) {
            for (Image i: fLegendImages) {
                i.dispose();
            }
        }
        fLegendImages = null;
    }

    // ------------------------------------------------------------------------
    // TmfView
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        // Control labels
        final String selectionStartLabel = Messages.HistogramView_selectionStartLabel;
        final String selectionEndLabel = Messages.HistogramView_selectionEndLabel;
        final String windowSpanLabel = Messages.HistogramView_windowSpanLabel;

        // --------------------------------------------------------------------
        // Set the HistogramView layout
        // --------------------------------------------------------------------
        Composite viewComposite = new Composite(getParentComposite(), SWT.FILL);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        viewComposite.setLayout(gridLayout);

        // --------------------------------------------------------------------
        // Add a sash for time controls and time range histogram
        // --------------------------------------------------------------------

        /*
         * The ScrolledComposite preferred size can be larger than its visible
         * width. This affects the preferred width of the SashForm. Set the
         * preferred width to 1 to prevent it from affecting the preferred width
         * of the view composite.
         */
        fSashForm = new SashForm(viewComposite, SWT.NONE) {
            @Override
            public Point computeSize(int wHint, int hHint) {
                Point computedSize = super.computeSize(wHint, hHint);
                if (wHint == SWT.DEFAULT) {
                    return new Point(1, computedSize.y);
                }
                return computedSize;
            }
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                Point computedSize = super.computeSize(wHint, hHint, changed);
                if (wHint == SWT.DEFAULT) {
                    return new Point(1, computedSize.y);
                }
                return computedSize;
            }
        };
        GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, true);
        fSashForm.setLayoutData(gridData);

        // --------------------------------------------------------------------
        // Time controls
        // --------------------------------------------------------------------
        fScrollComposite = new PackedScrolledComposite(fSashForm, SWT.H_SCROLL | SWT.V_SCROLL);
        fTimeControlsComposite = new Composite(fScrollComposite, SWT.NONE);
        fScrollComposite.setContent(fTimeControlsComposite);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        fScrollComposite.setLayout(gridLayout);
        fScrollComposite.setExpandHorizontal(true);
        fScrollComposite.setExpandVertical(true);

        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        fTimeControlsComposite.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, true);
        fTimeControlsComposite.setLayoutData(gridData);

        Composite innerComp = new Composite(fTimeControlsComposite, SWT.NONE);

        gridLayout = new GridLayout(2, false);
        innerComp.setLayout(gridLayout);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 5;
        gridLayout.verticalSpacing = 1;
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, true);
        innerComp.setLayoutData(gridData);

        Composite selectionGroup = new Composite(innerComp, SWT.BORDER);
        gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        selectionGroup.setLayout(gridLayout);
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        selectionGroup.setLayoutData(gridData);

        // Selection start control
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        fSelectionStartControl = new HistogramSelectionStartControl(this, selectionGroup, selectionStartLabel, 0L);
        fSelectionStartControl.setLayoutData(gridData);
        fSelectionStartControl.setValue(Long.MIN_VALUE);

        // Selection end control
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        fSelectionEndControl = new HistogramSelectionEndControl(this, selectionGroup, selectionEndLabel, 0L);
        fSelectionEndControl.setLayoutData(gridData);
        fSelectionEndControl.setValue(Long.MIN_VALUE);

        // Link button
        gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
        fLinkButton = new Label(innerComp, SWT.NONE);
        fLinkButton.setImage(LINK_IMG);
        fLinkButton.setLayoutData(gridData);
        addLinkButtonListeners();

        // Window span time control
        gridData = new GridData(GridData.FILL, GridData.CENTER, false, false);
        fTimeSpanControl = new HistogramTimeRangeControl(this, innerComp, windowSpanLabel, 0L);
        fTimeSpanControl.setLayoutData(gridData);
        fTimeSpanControl.setValue(Long.MIN_VALUE);

        IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        // --------------------------------------------------------------------
        // Time range histogram
        // --------------------------------------------------------------------
        fTimeRangeComposite = new Composite(fSashForm, SWT.NONE);
        gridLayout = new GridLayout(1, true);
        gridLayout.marginTop = 0;
        gridLayout.marginWidth = 0;
        fTimeRangeComposite.setLayout(gridLayout);

        // Use remaining horizontal space
        gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
        fTimeRangeComposite.setLayoutData(gridData);

        // Histogram
        fTimeRangeHistogram = new TimeRangeHistogram(this, fTimeRangeComposite, true);
        fTimeRangeHistogram.setStatusLineManager(statusLineManager);

        // --------------------------------------------------------------------
        // Full range histogram
        // --------------------------------------------------------------------
        final Composite fullRangeComposite = new Composite(viewComposite, SWT.FILL);
        gridLayout = new GridLayout(1, true);
        fullRangeComposite.setLayout(gridLayout);

        // Use remaining horizontal space
        gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
        fullRangeComposite.setLayoutData(gridData);

        // Histogram
        fFullTraceHistogram = new FullTraceHistogram(this, fullRangeComposite);
        fFullTraceHistogram.setStatusLineManager(statusLineManager);

        fLegendArea = new Composite(viewComposite, SWT.FILL);
        fLegendArea.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
        fLegendArea.setLayout(new RowLayout());

        // Add mouse wheel listener to time span control
        MouseWheelListener listener = fFullTraceHistogram.getZoom();
        fTimeSpanControl.addMouseWheelListener(listener);

        // View Action Handling
        contributeToActionBars();

        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }

        fSashForm.setVisible(true);
        fSashForm.setWeights(DEFAULT_WEIGHTS);

        fTimeControlsComposite.addPaintListener(new PaintListener() {
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
    }

    @Override
    public void setFocus() {
        fFullTraceHistogram.fCanvas.setFocus();
    }

    void refresh() {
    	getParentComposite().layout(true);
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
        return fScrollComposite.getSize().x + fSashForm.getSashWidth() + fTimeRangeHistogram.getPointAreaOffset();
    }

    /**
     * @since 1.0
     */
    @Override
    public int getAvailableWidth(int requestedOffset) {
        int pointAreaWidth = fTimeRangeHistogram.getPointAreaWidth();
        int curTimeAxisOffset = getTimeAxisOffset();
        if (pointAreaWidth <= 0) {
            pointAreaWidth = fSashForm.getBounds().width - curTimeAxisOffset;
        }
        int endOffset = curTimeAxisOffset + pointAreaWidth;
        GridLayout layout = (GridLayout) fTimeRangeComposite.getLayout();
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
        int plotAreaOffset = fTimeRangeHistogram.getPointAreaOffset();
        int width1 = Math.max(0, offset - plotAreaOffset - fSashForm.getSashWidth());
        int width2 = Math.max(0, total - width1 - fSashForm.getSashWidth());
        if (width1 >= 0 && width2 > 0 || width1 > 0 && width2 >= 0) {
            fSashForm.setWeights(new int[] { width1, width2 });
            fSashForm.layout();
        }

        // calculate right margin
        GridLayout layout = (GridLayout) fTimeRangeComposite.getLayout();
        int timeBasedControlsWidth = fTimeRangeComposite.getSize().x;
        int marginSize = timeBasedControlsWidth - width - plotAreaOffset;
        layout.marginRight = Math.max(0, marginSize);
        fTimeRangeComposite.layout();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the current trace handled by the view
     *
     * @return the current trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Returns the time range of the current selected window (base on default time scale).
     *
     * @return the time range of current selected window.
     */
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(
                TmfTimestamp.fromNanos(fWindowStartTime),
                TmfTimestamp.fromNanos(fWindowEndTime));
    }

    /**
     * get the show lost events action
     *
     * @return The action object
     */
    public Action getShowLostEventsAction() {
        if (hideLostEventsAction == null) {
            /* show lost events */
            hideLostEventsAction = new Action(Messages.HistogramView_hideLostEvents, IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                    HistogramScaledData.hideLostEvents = hideLostEventsAction.isChecked();
                    long maxNbEvents = HistogramScaledData.hideLostEvents ? fFullTraceHistogram.fScaledData.fMaxValue : fFullTraceHistogram.fScaledData.fMaxCombinedValue;
                    fFullTraceHistogram.setMaxNbEvents(maxNbEvents);
                    maxNbEvents = HistogramScaledData.hideLostEvents ? fTimeRangeHistogram.fScaledData.fMaxValue : fTimeRangeHistogram.fScaledData.fMaxCombinedValue;
                    fTimeRangeHistogram.setMaxNbEvents(maxNbEvents);
                }
            };
            hideLostEventsAction.setText(Messages.HistogramView_hideLostEvents);
            hideLostEventsAction.setToolTipText(Messages.HistogramView_hideLostEvents);
            hideLostEventsAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SHOW_LOST_EVENTS));
        }
        return hideLostEventsAction;
    }

    /**
     * get the show trace action
     *
     * @return The action object
     */
    public Action getShowTraceAction() {
        if (showTraceAction == null) {
            /* show lost events */
            showTraceAction = new Action(Messages.HistogramView_showTraces, IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                    Histogram.showTraces = showTraceAction.isChecked();
                    fFullTraceHistogram.fCanvas.redraw();
                    fTimeRangeHistogram.fCanvas.redraw();
                    updateLegendArea();
                }
            };
            showTraceAction.setChecked(true);
            showTraceAction.setText(Messages.HistogramView_showTraces);
            showTraceAction.setToolTipText(Messages.HistogramView_showTraces);
            showTraceAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SHOW_HIST_TRACES));
        }
        return showTraceAction;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Broadcast TmfSignal about new current selection time range.
     * @param beginTime the begin time of current selection.
     * @param endTime the end time of current selection.
     */
    void updateSelectionTime(long beginTime, long endTime) {
        updateDisplayedSelectionTime(beginTime, endTime);
        ITmfTimestamp beginTs = TmfTimestamp.fromNanos(beginTime);
        ITmfTimestamp endTs = TmfTimestamp.fromNanos(endTime);
        TmfSelectionRangeUpdatedSignal signal = new TmfSelectionRangeUpdatedSignal(this, beginTs, endTs, fTrace);
        fTimeSyncThrottle.queue(signal);
    }

    /**
     * Get selection begin time
     * @return the begin time of current selection
     */
    long getSelectionBegin() {
        return fSelectionBeginTime;
    }

    /**
     * Get selection end time
     * @return the end time of current selection
     */
    long getSelectionEnd() {
        return fSelectionEndTime;
    }

    /**
     * Get the link state
     * @return true if begin and end selection time should be linked
     */
    boolean getLinkState() {
        return fLinkState;
    }

    /**
     * Broadcast TmfSignal about new selection time range.
     * @param startTime the new start time
     * @param endTime the new end time
     */
    void updateTimeRange(long startTime, long endTime) {
        if (fTrace != null) {
            // Build the new time range; keep the current time
            TmfTimeRange timeRange = new TmfTimeRange(
                    TmfTimestamp.fromNanos(startTime),
                    TmfTimestamp.fromNanos(endTime));
            fTimeSpanControl.setValue(endTime - startTime);

            updateDisplayedTimeRange(startTime, endTime);

            // Send the FW signal
            TmfWindowRangeUpdatedSignal signal = new TmfWindowRangeUpdatedSignal(this, timeRange, fTrace);
            fTimeRangeSyncThrottle.queue(signal);
        }
    }

    /**
     * Broadcast TmfSignal about new selected time range.
     * @param newDuration new duration (relative to current start time)
     */
    public synchronized void updateTimeRange(long newDuration) {
        if (fTrace != null) {
            long delta = newDuration - fWindowSpan;
            long newStartTime = fWindowStartTime - (delta / 2);
            setNewRange(newStartTime, newDuration);
        }
    }

    private void setNewRange(long startTime, long duration) {
        long realStart = startTime;

        if (realStart < fTraceStartTime) {
            realStart = fTraceStartTime;
        }

        long endTime = realStart + duration;
        if (endTime > fTraceEndTime) {
            endTime = fTraceEndTime;
            if ((endTime - duration) > fTraceStartTime) {
                realStart = endTime - duration;
            } else {
                realStart = fTraceStartTime;
            }
        }
        updateTimeRange(realStart, endTime);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handles trace opened signal. Loads histogram if new trace time range is not
     * equal <code>TmfTimeRange.NULL_RANGE</code>
     * @param signal the trace opened signal
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        assert (signal != null);
        fTrace = signal.getTrace();
        loadTrace();
    }

    /**
     * Handles trace selected signal. Loads histogram if new trace time range is not
     * equal <code>TmfTimeRange.NULL_RANGE</code>
     * @param signal the trace selected signal
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        assert (signal != null);
        if (fTrace != signal.getTrace()) {
            fTrace = signal.getTrace();
            loadTrace();
        }
    }

    private void loadTrace() {
        initializeHistograms();
        getParentComposite().redraw();
    }

    /**
     * Handles trace closed signal. Clears the view and data model and cancels requests.
     * @param signal the trace closed signal
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        // Kill any running request
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }

        // Initialize the internal data
        fTrace = null;
        fTraceStartTime = 0L;
        fTraceEndTime = 0L;
        fWindowStartTime = 0L;
        fWindowEndTime = 0L;
        fWindowSpan = 0L;
        fSelectionBeginTime = 0L;
        fSelectionEndTime = 0L;

        // Clear the UI widgets
        fFullTraceHistogram.clear();
        fFullTraceHistogram.fDataModel.setTrace(null);
        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.fDataModel.setTrace(null);
        fSelectionStartControl.setValue(Long.MIN_VALUE);
        fSelectionEndControl.setValue(Long.MIN_VALUE);

        fTimeSpanControl.setValue(Long.MIN_VALUE);

        for (Control c: fLegendArea.getChildren()) {
            c.dispose();
        }
        disposeLegendImages();
        fLegendArea.layout();
        fLegendArea.getParent().layout();
    }

    /**
     * Handles trace range updated signal. Extends histogram according to the new time range. If a
     * HistogramRequest is already ongoing, it will be cancelled and a new request with the new range
     * will be issued.
     *
     * @param signal the trace range updated signal
     */
    @TmfSignalHandler
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        TmfTimeRange fullRange = signal.getRange();

        fTraceStartTime = fullRange.getStartTime().toNanos();
        fTraceEndTime = fullRange.getEndTime().toNanos();

        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);

        sendFullRangeRequest(fullRange);
    }

    /**
     * Handles the trace updated signal. Used to update time limits (start and end time)
     * @param signal the trace updated signal
     */
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }
        TmfTimeRange fullRange = signal.getTrace().getTimeRange();
        fTraceStartTime = fullRange.getStartTime().toNanos();
        fTraceEndTime = fullRange.getEndTime().toNanos();

        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);

        if ((fFullTraceRequest != null) && fFullTraceRequest.getRange().getEndTime().compareTo(signal.getRange().getEndTime()) < 0) {
            sendFullRangeRequest(fullRange);
        }
}

    /**
     * Handles the selection range updated signal. Sets the current time
     * selection in the time range histogram as well as the full histogram.
     *
     * @param signal
     *            the signal to process
     * @since 1.0
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        if (Display.getCurrent() == null) {
            // Make sure the signal is handled in the UI thread
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (getParentComposite().isDisposed()) {
                        return;
                    }
                    selectionRangeUpdated(signal);
                }
            });
            return;
        }

        // Update the selected time range
        long beginTime = signal.getBeginTime().toNanos();
        long endTime = signal.getEndTime().toNanos();

        updateDisplayedSelectionTime(beginTime, endTime);
    }

    /**
     * Updates the current window time range in the time range histogram and
     * full range histogram.
     *
     * @param signal
     *            the signal to process
     * @since 1.0
     */
    @TmfSignalHandler
    public void windowRangeUpdated(final TmfWindowRangeUpdatedSignal signal) {
        if (Display.getCurrent() == null) {
            // Make sure the signal is handled in the UI thread
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (getParentComposite().isDisposed()) {
                        return;
                    }
                    windowRangeUpdated(signal);
                }
            });
            return;
        }

        if (fTrace != null) {
            // Validate the time range
            TmfTimeRange range = signal.getCurrentRange().getIntersection(fTrace.getTimeRange());
            if (range == null) {
                return;
            }

            updateDisplayedTimeRange(
                    range.getStartTime().toNanos(),
                    range.getEndTime().toNanos());

            // Send the event request to populate the small histogram
            sendTimeRangeRequest(fWindowStartTime, fWindowEndTime);

            fTimeSpanControl.setValue(fWindowSpan);
        }
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void initializeHistograms() {
        TmfTimeRange fullRange = updateTraceTimeRange();

        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        long selectionBeginTime = ctx.getSelectionRange().getStartTime().toNanos();
        long selectionEndTime = ctx.getSelectionRange().getEndTime().toNanos();
        long startTime = ctx.getWindowRange().getStartTime().toNanos();
        long duration = ctx.getWindowRange().getEndTime().toNanos() - startTime;

        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setTimeRange(startTime, duration);
        fTimeRangeHistogram.setSelection(selectionBeginTime, selectionEndTime);
        fTimeRangeHistogram.fDataModel.setTrace(fTrace);

        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        fFullTraceHistogram.clear();
        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fFullTraceHistogram.setTimeRange(startTime, duration);
        fFullTraceHistogram.setSelection(selectionBeginTime, selectionEndTime);
        fFullTraceHistogram.fDataModel.setTrace(fTrace);

        fWindowStartTime = startTime;
        fWindowSpan = duration;
        fWindowEndTime = startTime + duration;

        fSelectionBeginTime = selectionBeginTime;
        fSelectionEndTime = selectionEndTime;
        fSelectionStartControl.setValue(fSelectionBeginTime);
        fSelectionEndControl.setValue(fSelectionEndTime);

        // make sure that the scrollbar is setup properly
        fScrollComposite.setMinSize(fTimeControlsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        fTimeSpanControl.setValue(duration);

        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(fTrace);
        if (!traces.isEmpty()) {
            this.showTraceAction.setEnabled(traces.size() < fFullTraceHistogram.getMaxNbTraces());
        }
        updateLegendArea();

        if (!fullRange.equals(TmfTimeRange.NULL_RANGE)) {
            sendTimeRangeRequest(startTime, startTime + duration);
            sendFullRangeRequest(fullRange);
        }
    }

    private void updateLegendArea() {
        for (Control c: fLegendArea.getChildren()) {
            c.dispose();
        }
        disposeLegendImages();
        if (fFullTraceHistogram.showTraces()) {
            Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(fTrace);
            fLegendImages = new Image[traces.size()];
            int traceIndex = 0;
            for (ITmfTrace trace : traces) {
                fLegendImages[traceIndex] = new Image(fLegendArea.getDisplay(), 16, 16);
                GC gc = new GC(fLegendImages[traceIndex]);
                gc.setBackground(fFullTraceHistogram.getTraceColor(traceIndex));
                gc.fillRectangle(0, 0, 15, 15);
                gc.setForeground(fLegendArea.getDisplay().getSystemColor(SWT.COLOR_BLACK));
                gc.drawRectangle(0, 0, 15, 15);
                gc.dispose();

                CLabel label = new CLabel(fLegendArea, SWT.NONE);
                label.setText(trace.getName());
                label.setImage(fLegendImages[traceIndex]);
                traceIndex++;
            }
        }
        fLegendArea.layout();
        fLegendArea.getParent().layout();
    }

    private void updateDisplayedSelectionTime(long beginTime, long endTime) {
        fSelectionBeginTime = beginTime;
        fSelectionEndTime = endTime;

        fFullTraceHistogram.setSelection(fSelectionBeginTime, fSelectionEndTime);
        fTimeRangeHistogram.setSelection(fSelectionBeginTime, fSelectionEndTime);
        fSelectionStartControl.setValue(fSelectionBeginTime);
        fSelectionEndControl.setValue(fSelectionEndTime);
    }

    private void updateDisplayedTimeRange(long start, long end) {
        fWindowStartTime = start;
        fWindowEndTime = end;
        fWindowSpan = fWindowEndTime - fWindowStartTime;
        fFullTraceHistogram.setTimeRange(fWindowStartTime, fWindowSpan);
    }

    private TmfTimeRange updateTraceTimeRange() {
        fTraceStartTime = 0L;
        fTraceEndTime = 0L;

        TmfTimeRange timeRange = fTrace.getTimeRange();
        if (!timeRange.equals(TmfTimeRange.NULL_RANGE)) {
            fTraceStartTime = timeRange.getStartTime().toNanos();
            fTraceEndTime = timeRange.getEndTime().toNanos();
        }
        return timeRange;
    }

    private void sendTimeRangeRequest(long startTime, long endTime) {
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        ITmfTimestamp startTS = TmfTimestamp.fromNanos(startTime);
        ITmfTimestamp endTS = TmfTimestamp.fromNanos(endTime);
        TmfTimeRange timeRange = new TmfTimeRange(startTS, endTS);

        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setTimeRange(startTime, endTime - startTime);

        int cacheSize = fTrace.getCacheSize();
        fTimeRangeRequest = new HistogramRequest(fTimeRangeHistogram.getDataModel(),
                timeRange, 0, ITmfEventRequest.ALL_DATA, cacheSize, ExecutionType.FOREGROUND, false);
        fTrace.sendRequest(fTimeRangeRequest);
    }

    private void sendFullRangeRequest(TmfTimeRange fullRange) {
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        int cacheSize = fTrace.getCacheSize();
        fFullTraceRequest = new HistogramRequest(fFullTraceHistogram.getDataModel(),
                fullRange,
                (int) fFullTraceHistogram.fDataModel.getNbEvents(),
                ITmfEventRequest.ALL_DATA,
                cacheSize,
                ExecutionType.BACKGROUND, true);
        fTrace.sendRequest(fFullTraceRequest);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().add(getShowLostEventsAction());
        bars.getToolBarManager().add(getShowTraceAction());
        bars.getToolBarManager().add(new Separator());
    }

    private void addLinkButtonListeners() {
        fLinkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                fSelectionEndControl.setEnabled(fLinkState);
                fLinkState = !fLinkState;
                fLinkButton.redraw();
            }
        });

        fLinkButton.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                if (fLinkState) {
                    Rectangle r = fLinkButton.getBounds();
                    r.x = -1;
                    r.y = -1;
                    e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
                    e.gc.drawRectangle(r);
                    r.x = 0;
                    r.y = 0;
                    e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
                    e.gc.drawRectangle(r);
                }
            }
        });
    }

    private static class PackedScrolledComposite extends ScrolledComposite {
        Point fScrollBarSize;  // Size of OS-specific scrollbar

        public PackedScrolledComposite(Composite parent, int style) {
            super(parent, style);
            Composite composite = new Composite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
            composite.setSize(1, 1);
            fScrollBarSize = composite.computeSize(0, 0);
            composite.dispose();
        }

        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            Point point = super.computeSize(wHint, hHint, changed);
            // Remove scrollbar size if applicable
            point.x += ((getStyle() & SWT.V_SCROLL) != 0) ? -fScrollBarSize.x : 0;
            point.y += ((getStyle() & SWT.H_SCROLL) != 0) ? -fScrollBarSize.y : 0;
            return point;
        }
    }
}
