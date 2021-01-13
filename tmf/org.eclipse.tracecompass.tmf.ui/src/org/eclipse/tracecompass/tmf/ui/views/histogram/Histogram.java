/*******************************************************************************
 * Copyright (c) 2011, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Changed to updated histogram data model
 *   Francois Chouinard - Reformat histogram labels on format change
 *   Patrick Tasse - Support selection range
 *   Xavier Raynaud - Support multi-trace coloring
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.histogram;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tracecompass.internal.tmf.ui.views.histogram.HistogramTimeAdapter;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampDelta;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.Resolution;
import org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphScale;

/**
 * Re-usable histogram widget.
 *
 * It has the following features:
 * <ul>
 * <li>Y-axis labels displaying min/max count values
 * <li>X-axis labels displaying time range
 * <li>a histogram displaying the distribution of values over time (note that
 * the histogram might not necessarily fill the whole canvas)
 * </ul>
 * The widget also has 1 'marker' to identify:
 * <ul>
 * <li>a blue dashed line over the bar that contains the currently selected event
 * </ul>
 * Clicking on the histogram will select the current event at the mouse
 * location.
 * <p>
 * Once the histogram is selected, there is some limited keyboard support:
 * <ul>
 * <li>Home: go to the first histogram bar
 * <li>End: go to the last histogram bar
 * <li>Left: go to the previous histogram
 * <li>Right: go to the next histogram bar
 * </ul>
 * Finally, when the mouse hovers over the histogram, a tool tip showing the
 * following information about the corresponding histogram bar time range:
 * <ul>
 * <li>start of the time range
 * <li>end of the time range
 * <li>number of events in that time range
 * </ul>
 *
 * @version 1.1
 * @author Francois Chouinard
 */
public abstract class Histogram implements ControlListener, PaintListener, KeyListener, MouseListener, MouseMoveListener, MouseTrackListener, IHistogramModelListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final int TIME_SCALE_HEIGHT = 27;

    // Histogram colors

    // System colors, they do not need to be disposed
    private final Color fSelectionForegroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
    private final Color fSelectionBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

    // Application colors, they need to be disposed
    private final Color[] fHistoBarColors = new Color[] { new Color(Display.getDefault(), 90, 90, 255), // blue
            new Color(Display.getDefault(), 0, 240, 0), // green
            new Color(Display.getDefault(), 255, 0, 0), // red
            new Color(Display.getDefault(), 0, 255, 255), // cyan
            new Color(Display.getDefault(), 255, 80, 255), // magenta
            new Color(Display.getDefault(), 200, 200, 0), // yellow
            new Color(Display.getDefault(), 200, 150, 0), // brown
            new Color(Display.getDefault(), 150, 255, 150), // light green
            new Color(Display.getDefault(), 200, 80, 80), // dark red
            new Color(Display.getDefault(), 30, 150, 150), // dark cyan
            new Color(Display.getDefault(), 200, 200, 255), // light blue
            new Color(Display.getDefault(), 0, 120, 0), // dark green
            new Color(Display.getDefault(), 255, 150, 150), // lighter red
            new Color(Display.getDefault(), 140, 80, 140), // dark magenta
            new Color(Display.getDefault(), 150, 100, 50), // brown
            new Color(Display.getDefault(), 255, 80, 80), // light red
            new Color(Display.getDefault(), 200, 200, 200), // light grey
            new Color(Display.getDefault(), 255, 200, 80), // orange
            new Color(Display.getDefault(), 255, 255, 80), // pale yellow
            new Color(Display.getDefault(), 255, 200, 200), // pale red
            new Color(Display.getDefault(), 255, 200, 255), // pale magenta
            new Color(Display.getDefault(), 255, 255, 200), // pale pale yellow
            new Color(Display.getDefault(), 200, 255, 255), // pale pale blue
    };
    private final Color fTimeRangeColor = new Color(Display.getCurrent(), 255, 128, 0);
    private final Color fLostEventColor = new Color(Display.getCurrent(), 208, 62, 120);

    /**
     * Movement cursor
     * @since 6.2
     */
    protected final Cursor fMoveCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);
    /**
     * Zoom cursor
     * @since 6.2
     */
    protected final Cursor fZoomCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_SIZEWE);

    /**
     * Select cursor
     * @since 6.2
     */
    protected final Cursor fSelectCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_CROSS);

    // Drag states
    /**
     * No drag in progress
     */
    protected static final int DRAG_NONE = 0;
    /**
     * Drag the selection
     */
    protected static final int DRAG_SELECTION = 1;
    /**
     * Drag the time range
     */
    protected static final int DRAG_RANGE = 2;
    /**
     * Drag the zoom range
     */
    protected static final int DRAG_ZOOM = 3;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The parent TMF view.
     */
    protected TmfView fParentView;

    private Composite fComposite;
    private Font fFont;

    // Histogram text fields
    private Label fMaxNbEventsLabel;

    /**
     * Histogram drawing area
     */
    protected Canvas fCanvas;

    /**
     * The histogram data model.
     */
    protected final @NonNull HistogramDataModel fDataModel;

    /**
     * The histogram data model scaled to current resolution and screen width.
     */
    protected HistogramScaledData fScaledData;

    /**
     * The current event value
     */
    protected long fCurrentEventTime = 0L;

    /**
     * The current selection begin time
     */
    private long fSelectionBegin = 0L;

    /**
     * The current selection end time
     */
    private long fSelectionEnd = 0L;

    /**
     * The drag state
     *
     * @see #DRAG_NONE
     * @see #DRAG_SELECTION
     * @see #DRAG_RANGE
     * @see #DRAG_ZOOM
     */
    protected int fDragState = DRAG_NONE;

    /**
     * The button that started a mouse drag, or 0 if no drag in progress
     */
    protected int fDragButton = 0;

    /**
     * The bucket display offset
     */
    private int fOffset = 0;

    /**
     * show the traces or not
     */
    static boolean showTraces = true;


    private boolean fSendTimeAlignSignals = false;

    private IStatusLineManager fStatusLineManager;

    private TimeGraphScale fTimeLineScale;
    private TimeGraphColorScheme fColorScheme;
    private TmfAbstractToolTipHandler fToolTipHandler = new TmfAbstractToolTipHandler() {

        @Override
        protected void fill(Control control, MouseEvent event, Point pt) {
            if ((fDataModel.getNbEvents() != 0 || fDataModel.getStartTime() < fDataModel.getEndTime()) &&
                    fScaledData != null && event.x >= 0 && event.x - fOffset < fScaledData.fWidth) {
                fillTooltip(event.x - fOffset);
            }
            fCanvas.setToolTipText(null);
        }

        private void fillTooltip(final int index) {
            long startTime = fScaledData.getBucketStartTime(index);
            /*
             * negative values are possible if time values came into the model
             * in decreasing order
             */
            if (startTime < 0) {
                startTime = 0;
            }
            final long endTime = fScaledData.getBucketEndTime(index);
            final int nbEvents = (index >= 0) ? fScaledData.fData[index].getNbEvents() : 0;
            int selectionBeginBucket = Math.min(fScaledData.fSelectionBeginBucket, fScaledData.fSelectionEndBucket);
            int selectionEndBucket = Math.max(fScaledData.fSelectionBeginBucket, fScaledData.fSelectionEndBucket);
            if (selectionBeginBucket <= index && index <= selectionEndBucket && fSelectionBegin != fSelectionEnd) {
                long start = Math.abs(fSelectionEnd - fSelectionBegin);
                TmfTimestampDelta delta = new TmfTimestampDelta(start, ITmfTimestamp.NANOSECOND_SCALE);
                addItem(null, Messages.Histogram_selectionSpanToolTip, delta.toString());
            }
            addItem(null, ToolTipString.fromString(Messages.Histogram_bucketRangeToolTip),
                    ToolTipString.fromTimestamp(NLS.bind(Messages.Histogram_timeRange,
                            TmfTimestamp.fromNanos(startTime).toString(), TmfTimestamp.fromNanos(endTime).toString()), startTime));
            addItem(null, Messages.Histogram_eventCountToolTip, Long.toString(nbEvents));
            if (!HistogramScaledData.hideLostEvents) {
                final int nbLostEvents = (index >= 0) ? fScaledData.fLostEventsData[index] : 0;
                addItem(null, Messages.Histogram_lostEventCountToolTip, Long.toString(nbLostEvents));
            }
        }
    };

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param view
     *            A reference to the parent TMF view.
     * @param parent
     *            A parent composite
     */
    public Histogram(final TmfView view, final Composite parent) {
        this(view, parent, false);
    }

    /**
     * Full constructor.
     *
     * @param view
     *            A reference to the parent TMF view.
     * @param parent
     *            A parent composite
     * @param sendTimeAlignSignals
     *            Flag to send time alignment signals or not
     * @since 1.0
     */
    public Histogram(final TmfView view, final Composite parent, final boolean sendTimeAlignSignals) {
        fParentView = view;
        fSendTimeAlignSignals = sendTimeAlignSignals;
        fColorScheme = new TimeGraphColorScheme();
        fDataModel = new HistogramDataModel();
        fDataModel.addHistogramListener(this);
        fComposite = createWidget(parent);
        clear();

        fCanvas.addControlListener(this);
        fCanvas.addPaintListener(this);
        fCanvas.addKeyListener(this);
        fCanvas.addMouseListener(this);
        fCanvas.addMouseTrackListener(this);
        fCanvas.addMouseMoveListener(this);
        fToolTipHandler.activateHoverHelp(fCanvas);
        TmfSignalManager.register(this);
    }

    /**
     * Dispose resources and unregisters listeners.
     */
    public void dispose() {
        TmfSignalManager.deregister(this);
        fLostEventColor.dispose();
        for (Color c : fHistoBarColors) {
            c.dispose();
        }
        fTimeRangeColor.dispose();
        fFont.dispose();
        fDataModel.removeHistogramListener(this);
        fDataModel.dispose();
    }

    private Composite createWidget(final Composite parent) {

        fFont = adjustFont(parent);

        // --------------------------------------------------------------------
        // Define the histogram
        // --------------------------------------------------------------------

        final Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

        // Use all the horizontal space
        composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

        // Y-axis max event
        fMaxNbEventsLabel = new Label(composite, SWT.RIGHT);
        fMaxNbEventsLabel.setFont(fFont);
        fMaxNbEventsLabel.setText("0"); //$NON-NLS-1$
        fMaxNbEventsLabel.setLayoutData(GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).create());

        // Histogram itself
        Composite canvasComposite = new Composite(composite, SWT.BORDER);
        canvasComposite.setLayout(GridLayoutFactory.fillDefaults().spacing(0, 0).create());
        canvasComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(1, 1).create());
        fCanvas = new Canvas(canvasComposite, SWT.DOUBLE_BUFFERED);
        fCanvas.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        fCanvas.addDisposeListener(e -> {
            Object image = fCanvas.getData(IMAGE_KEY);
            if (image instanceof Image) {
                ((Image) image).dispose();
            }
        });

        fTimeLineScale = new TimeGraphScale(canvasComposite, fColorScheme, SWT.BOTTOM);
        fTimeLineScale.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        fTimeLineScale.setHeight(TIME_SCALE_HEIGHT);
        fTimeLineScale.setTimeProvider(new HistogramTimeAdapter(fDataModel));

        updateTimeFormat();

        return composite;
    }

    private static Font adjustFont(final Composite composite) {
        // Reduce font size for a more pleasing rendering
        final int fontSizeAdjustment = -2;
        final Font font = composite.getFont();
        final FontData fontData = font.getFontData()[0];
        return new Font(font.getDevice(), fontData.getName(), fontData.getHeight() + fontSizeAdjustment, fontData.getStyle());
    }

    /**
     * Assign the status line manager
     *
     * @param statusLineManager
     *            The status line manager, or null to disable status line
     *            messages
     * @since 4.0
     */
    public void setStatusLineManager(IStatusLineManager statusLineManager) {
        if (fStatusLineManager != null && statusLineManager == null) {
            fStatusLineManager.setMessage(""); //$NON-NLS-1$
        }
        fStatusLineManager = statusLineManager;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the start time (equal first bucket time)
     *
     * @return the start time.
     */
    public long getStartTime() {
        return fDataModel.getFirstBucketTime();
    }

    /**
     * Returns the end time.
     *
     * @return the end time.
     */
    public long getEndTime() {
        return fDataModel.getEndTime();
    }

    /**
     * Returns the time limit (end of last bucket)
     *
     * @return the time limit.
     */
    public long getTimeLimit() {
        return fDataModel.getTimeLimit();
    }

    /**
     * Returns a data model reference.
     *
     * @return data model.
     */
    public HistogramDataModel getDataModel() {
        return fDataModel;
    }

    /**
     * Set the max number events to be displayed
     *
     * @param maxNbEvents
     *            the maximum number of events
     */
    void setMaxNbEvents(long maxNbEvents) {
        fMaxNbEventsLabel.setText(Long.toString(maxNbEvents));
        fMaxNbEventsLabel.getParent().layout();
        fCanvas.redraw();
    }

    /**
     * Return <code>true</code> if the traces must be displayed in the
     * histogram, <code>false</code> otherwise.
     *
     * @return whether the traces should be displayed
     */
    public boolean showTraces() {
        return showTraces && fDataModel.getNbTraces() < getMaxNbTraces();
    }

    /**
     * Returns the maximum number of traces the histogram can display with
     * separate colors. If there is more traces, histogram will use only one
     * color to display them.
     *
     * @return the maximum number of traces the histogram can display.
     */
    public int getMaxNbTraces() {
        return fHistoBarColors.length;
    }

    /**
     * Returns the color used to display the trace at the given index.
     *
     * @param traceIndex
     *            a trace index
     * @return a {@link Color}
     */
    public Color getTraceColor(int traceIndex) {
        return fHistoBarColors[traceIndex % fHistoBarColors.length];
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Updates the time range.
     *
     * @param startTime
     *            A start time
     * @param endTime
     *            A end time.
     */
    public void updateTimeRange(long startTime, long endTime) {
        if (fDragState == DRAG_NONE) {
            ((HistogramView) fParentView).updateTimeRange(startTime, endTime);
        }
    }

    /**
     * Clear the histogram and reset the data
     */
    public void clear() {
        fDataModel.clear();
        if (fDragState == DRAG_SELECTION) {
            updateSelectionTime();
        }
        fDragState = DRAG_NONE;
        fDragButton = 0;
        synchronized (fDataModel) {
            fScaledData = null;
        }
    }

    /**
     * Sets the current selection time range and refresh the display
     *
     * @param beginTime
     *            The begin time of the current selection
     * @param endTime
     *            The end time of the current selection
     */
    public void setSelection(final long beginTime, final long endTime) {
        fSelectionBegin = (beginTime > 0) ? beginTime : 0;
        fSelectionEnd = (endTime > 0) ? endTime : 0;
        fDataModel.setSelectionNotifyListeners(beginTime, endTime);
    }

    /**
     * Computes the timestamp of the bucket at [offset]
     *
     * @param offset
     *            offset from the left on the histogram
     * @return the start timestamp of the corresponding bucket
     */
    public synchronized long getTimestamp(final int offset) {
        HistogramScaledData scaledData = fScaledData;
        if (scaledData != null) {
            return scaledData.fFirstBucketTime + Math.round(scaledData.fBucketDuration * offset);
        }
        return 0;
    }

    /**
     * Computes the offset of the timestamp in the histogram
     *
     * @param timestamp
     *            the timestamp
     * @return the offset of the corresponding bucket (-1 if invalid)
     */
    public synchronized int getOffset(final long timestamp) {
        if (timestamp < fDataModel.getFirstBucketTime() || timestamp > fDataModel.getEndTime()) {
            return -1;
        }
        return (int) ((timestamp - fDataModel.getFirstBucketTime()) / fScaledData.fBucketDuration);
    }

    /**
     * Set the bucket display offset
     *
     * @param offset
     *            the bucket display offset
     */
    protected void setOffset(final int offset) {
        fOffset = offset;
    }

    /**
     * Move the currently selected bar cursor.
     *
     * @param keyCode
     *            the SWT key code
     */
    protected void moveCursor(final int keyCode) {

        int index;
        switch (keyCode) {

        case SWT.HOME:
            fScaledData.fSelectionBeginBucket = 0;
            break;

        case SWT.END:
            fScaledData.fSelectionBeginBucket = fScaledData.fWidth - 1;
            break;

        case SWT.ARROW_RIGHT: {
            long prevStartTime = getTimestamp(fScaledData.fSelectionBeginBucket);
            index = Math.max(0, Math.min(fScaledData.fWidth - 1, fScaledData.fSelectionBeginBucket + 1));
            while (index < fScaledData.fWidth && (fScaledData.fData[index].isEmpty() || prevStartTime == getTimestamp(index))) {
                prevStartTime = getTimestamp(index);
                index++;
            }
            if (index >= fScaledData.fWidth) {
                index = fScaledData.fWidth - 1;
            }
            fScaledData.fSelectionBeginBucket = index;
            break;
        }

        case SWT.ARROW_LEFT: {
            long prevEndTime = getTimestamp(fScaledData.fSelectionBeginBucket + 1);
            index = Math.max(0, Math.min(fScaledData.fWidth - 1, fScaledData.fSelectionBeginBucket - 1));
            while (index >= 0 && (fScaledData.fData[index].isEmpty() || prevEndTime == getTimestamp(index + 1))) {
                prevEndTime = getTimestamp(index + 1);
                index--;
            }
            if (index <= 0) {
                index = 0;
            }
            fScaledData.fSelectionBeginBucket = index;
            break;
        }

        default:
            return;
        }

        fScaledData.fSelectionEndBucket = fScaledData.fSelectionBeginBucket;
        fSelectionBegin = getTimestamp(fScaledData.fSelectionBeginBucket);
        fSelectionEnd = fSelectionBegin;
        updateSelectionTime();
    }

    /**
     * Refresh the histogram display
     */
    @Override
    public void modelUpdated() {
        if (!fCanvas.isDisposed() && fCanvas.getDisplay() != null) {
            fCanvas.getDisplay().asyncExec(() ->  {
                    if (!fCanvas.isDisposed()) {
                        // Retrieve and normalize the data
                        final int canvasWidth = fCanvas.getBounds().width;
                        final int canvasHeight = fCanvas.getBounds().height;
                        if (canvasWidth <= 0 || canvasHeight <= 0) {
                            return;
                        }
                        fDataModel.setSelection(fSelectionBegin, fSelectionEnd);
                        fScaledData = fDataModel.scaleTo(canvasWidth, canvasHeight, 1);
                        synchronized (fDataModel) {
                            if (fScaledData != null) {
                                fCanvas.redraw();
                                HistogramTimeAdapter adapter = (HistogramTimeAdapter) fTimeLineScale.getTimeProvider();
                                adapter.setTimeSpace(canvasWidth);
                                // Display histogram and update X-,Y-axis labels
                                long maxNbEvents = HistogramScaledData.hideLostEvents ? fScaledData.fMaxValue : fScaledData.fMaxCombinedValue;
                                String old = fMaxNbEventsLabel.getText();
                                fMaxNbEventsLabel.setText(Long.toString(maxNbEvents));
                                // The Y-axis area might need to be re-sized
                                GridData gd = (GridData) fMaxNbEventsLabel.getLayoutData();
                                gd.widthHint = Math.max(gd.widthHint, fMaxNbEventsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
                                fMaxNbEventsLabel.getParent().layout();
                                if (old.length() < fMaxNbEventsLabel.getText().length()) {
                                    if ((fSendTimeAlignSignals) && (fParentView instanceof ITmfTimeAligned)) {
                                        TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(this, ((ITmfTimeAligned) fParentView).getTimeViewAlignmentInfo(), true));
                                    }
                                }
                            }
                            fTimeLineScale.redraw();
                        }
                }
            });
        }
    }

    /**
     * Add a mouse wheel listener to the histogram
     *
     * @param listener
     *            the mouse wheel listener
     */
    public void addMouseWheelListener(MouseWheelListener listener) {
        fCanvas.addMouseWheelListener(listener);
    }

    /**
     * Remove a mouse wheel listener from the histogram
     *
     * @param listener
     *            the mouse wheel listener
     */
    public void removeMouseWheelListener(MouseWheelListener listener) {
        fCanvas.removeMouseWheelListener(listener);
    }

    /**
     * Add a key listener to the histogram
     *
     * @param listener
     *            the key listener
     */
    public void addKeyListener(KeyListener listener) {
        fCanvas.addKeyListener(listener);
    }

    /**
     * Remove a key listener from the histogram
     *
     * @param listener
     *            the key listener
     */
    public void removeKeyListener(KeyListener listener) {
        fCanvas.removeKeyListener(listener);
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void updateSelectionTime() {
        ((HistogramView) fParentView).updateSelectionTime(fSelectionBegin, fSelectionEnd);
    }

    // ------------------------------------------------------------------------
    // PaintListener
    // ------------------------------------------------------------------------
    /**
     * Image key string for the canvas.
     */
    protected final String IMAGE_KEY = "double-buffer-image"; //$NON-NLS-1$

    @Override
    public void paintControl(final PaintEvent event) {

        // Get the geometry
        final int canvasWidth = fCanvas.getBounds().width;
        final int canvasHeight = fCanvas.getBounds().height;

        // Make sure we have something to draw upon
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            return;
        }

        // Retrieve image; re-create only if necessary
        Image image = (Image) fCanvas.getData(IMAGE_KEY);
        if (image == null || image.getBounds().width != canvasWidth || image.getBounds().height != canvasHeight) {
            if (image != null) {
                image.dispose();
            }
            image = new Image(event.display, canvasWidth, canvasHeight);
            fCanvas.setData(IMAGE_KEY, image);
        }

        // Draw the histogram on its canvas
        final GC imageGC = new GC(image);
        formatImage(imageGC, image);
        event.gc.drawImage(image, 0, 0);
        imageGC.dispose();
        fTimeLineScale.redraw();
    }

    private void formatImage(final GC imageGC, final Image image) {

        if (fScaledData == null) {
            return;
        }

        final HistogramScaledData scaledData = new HistogramScaledData(fScaledData);

        try {
            final int height = image.getBounds().height;

            // Clear the drawing area
            imageGC.setBackground(fTimeLineScale.getColorScheme().getColor(TimeGraphColorScheme.BACKGROUND));
            imageGC.fillRectangle(0, 0, image.getBounds().width + 1, image.getBounds().height + 1);

            // Draw the histogram bars
            final int limit = scaledData.fWidth;
            double factor = HistogramScaledData.hideLostEvents ? scaledData.fScalingFactor : scaledData.fScalingFactorCombined;
            final boolean showTracesColors = showTraces();
            for (int i = 0; i < limit; i++) {
                HistogramBucket hb = scaledData.fData[i];
                int totalNbEvents = hb.getNbEvents();
                int value = (int) Math.ceil(totalNbEvents * factor);
                int x = i + fOffset;

                /*
                 * in Linux, the last pixel in a line is not drawn, so draw lost
                 * events first, one pixel too far
                 */
                if (!HistogramScaledData.hideLostEvents) {
                    imageGC.setForeground(fLostEventColor);
                    final int lostEventValue = (int) Math.ceil(scaledData.fLostEventsData[i] * factor);
                    if (lostEventValue != 0) {
                        /*
                         * drawing a line is inclusive, so we should remove 1
                         * from y2 but we don't because Linux
                         */
                        imageGC.drawLine(x, height - value - lostEventValue, x, height - value);
                    }
                }

                // then draw normal events second, to overwrite that extra pixel
                if (!hb.isEmpty()) {
                    if (showTracesColors) {
                        for (int traceIndex = 0; traceIndex < hb.getNbTraces(); traceIndex++) {
                            int nbEventsForTrace = hb.getNbEvent(traceIndex);
                            if (nbEventsForTrace > 0) {
                                Color c = fHistoBarColors[traceIndex % fHistoBarColors.length];
                                imageGC.setForeground(c);
                                imageGC.drawLine(x, height - value, x, height);
                                totalNbEvents -= nbEventsForTrace;
                                value = (int) Math.ceil(totalNbEvents * scaledData.fScalingFactor);
                            }
                        }
                    } else {
                        Color c = fHistoBarColors[0];
                        imageGC.setForeground(c);
                        imageGC.drawLine(x, height - value, x, height);
                    }
                }
            }

            // Draw the selection bars
            int alpha = imageGC.getAlpha();
            imageGC.setAlpha(100);
            imageGC.setForeground(fSelectionForegroundColor);
            imageGC.setBackground(fSelectionBackgroundColor);
            final int beginBucket = scaledData.fSelectionBeginBucket + fOffset;
            if (beginBucket >= 0 && beginBucket < limit) {
                imageGC.drawLine(beginBucket, 0, beginBucket, height);
            }
            final int endBucket = scaledData.fSelectionEndBucket + fOffset;
            if (endBucket >= 0 && endBucket < limit && endBucket != beginBucket) {
                imageGC.drawLine(endBucket, 0, endBucket, height);
            }
            if (Math.abs(endBucket - beginBucket) > 1) {
                if (endBucket > beginBucket) {
                    imageGC.fillRectangle(beginBucket + 1, 0, endBucket - beginBucket - 1, height);
                } else {
                    imageGC.fillRectangle(endBucket + 1, 0, beginBucket - endBucket - 1, height);
                }
            }
            imageGC.setAlpha(alpha);
        } catch (final Exception e) {
            // Do nothing
        }
    }

    /**
     * Draw a time range window
     *
     * @param imageGC
     *            the GC
     * @param rangeStartTime
     *            the range start time
     * @param rangeDuration
     *            the range duration
     */
    protected void drawTimeRangeWindow(GC imageGC, long rangeStartTime, long rangeDuration) {

        if (fScaledData == null) {
            return;
        }

        // Map times to histogram coordinates
        double bucketSpan = fScaledData.fBucketDuration;
        long startTime = Math.min(rangeStartTime, rangeStartTime + rangeDuration);
        double rangeWidth = (Math.abs(rangeDuration) / bucketSpan);

        int left = (int) ((startTime - fDataModel.getFirstBucketTime()) / bucketSpan);
        int right = (int) (left + rangeWidth);
        int center = (left + right) / 2;
        int height = fCanvas.getSize().y;
        int arc = Math.min(15, (int) rangeWidth);

        // Draw the selection window
        imageGC.setForeground(fTimeRangeColor);
        imageGC.setLineWidth(1);
        imageGC.setLineStyle(SWT.LINE_SOLID);
        imageGC.drawRoundRectangle(left, 0, (int) rangeWidth, height - 1, arc, arc);

        // Fill the selection window
        imageGC.setBackground(fTimeRangeColor);
        imageGC.setAlpha(35);
        imageGC.fillRoundRectangle(left + 1, 1, (int) rangeWidth - 1, height - 2, arc, arc);
        imageGC.setAlpha(255);

        // Draw the cross hair
        imageGC.setForeground(fTimeRangeColor);
        imageGC.setLineWidth(1);
        imageGC.setLineStyle(SWT.LINE_SOLID);

        int chHalfWidth = ((rangeWidth < 60) ? (int) ((rangeWidth * 2) / 3) : 40) / 2;
        imageGC.drawLine(center - chHalfWidth, height / 2, center + chHalfWidth, height / 2);
        imageGC.drawLine(center, (height / 2) - chHalfWidth, center, (height / 2) + chHalfWidth);
    }

    /**
     * Get the offset of the point area, relative to the histogram canvas
     * We consider the point area to be from where the first point could
     * be drawn to where the last point could be drawn.
     *
     * @return the offset in pixels
     *
     * @since 1.0
     */
    public int getPointAreaOffset() {
        Point absCanvas = fCanvas.toDisplay(0, 0);
        Point viewPoint = fComposite.getParent().toDisplay(0, 0);
        return absCanvas.x - viewPoint.x;
    }

    /**
     * Get the width of the point area. We consider the point area to be from
     * where the first point could be drawn to where the last point could be
     * drawn. The point area differs from the plot area because there might be a
     * gap between where the plot area start and where the fist point is drawn.
     * This also matches the width that the use can select.
     *
     * @return the width in pixels
     *
     * @since 1.0
     */
    public int getPointAreaWidth() {
        if (!fCanvas.isDisposed()) {
            // Retrieve and normalize the data
            return fCanvas.getBounds().width;
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // KeyListener
    // ------------------------------------------------------------------------

    @Override
    public void keyPressed(final KeyEvent event) {
        moveCursor(event.keyCode);
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        // do nothing
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseDoubleClick(final MouseEvent event) {
        // do nothing
    }

    @Override
    public void mouseDown(final MouseEvent event) {
        if (fScaledData != null && event.button == 1 && fDragState == DRAG_NONE &&
                (fDataModel.getNbEvents() != 0 || fDataModel.getStartTime() < fDataModel.getEndTime())) {
            fDragState = DRAG_SELECTION;
            fCanvas.setCursor(fZoomCursor);
            fDragButton = event.button;
            if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT) {
                if (Math.abs(event.x - fScaledData.fSelectionBeginBucket) < Math.abs(event.x - fScaledData.fSelectionEndBucket)) {
                    fScaledData.fSelectionBeginBucket = fScaledData.fSelectionEndBucket;
                    fSelectionBegin = fSelectionEnd;
                }
                fSelectionEnd = getTimestamp(event.x);
                fScaledData.fSelectionEndBucket = event.x;
                fCanvas.setCursor(fSelectCursor);
            } else {
                fSelectionBegin = Math.min(getTimestamp(event.x), getEndTime());
                fScaledData.fSelectionBeginBucket = event.x;
                fSelectionEnd = fSelectionBegin;
                fScaledData.fSelectionEndBucket = fScaledData.fSelectionBeginBucket;
                fCanvas.setCursor(fSelectCursor);
            }
            updateStatusLine(fSelectionBegin, fSelectionEnd, getTimestamp(event.x));
            fCanvas.redraw();
            fTimeLineScale.redraw();
        }
    }

    @Override
    public void mouseUp(final MouseEvent event) {
        if (fDragState == DRAG_SELECTION && event.button == fDragButton) {
            fDragState = DRAG_NONE;
            fDragButton = 0;
            updateSelectionTime();
        }
        fCanvas.setCursor(null);
        updateStatusLine(fSelectionBegin, fSelectionEnd, getTimestamp(event.x));
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseMove(MouseEvent event) {
        if (fDragState == DRAG_SELECTION && (fDataModel.getNbEvents() != 0 || fDataModel.getStartTime() < fDataModel.getEndTime())) {
            fSelectionEnd = Math.max(getStartTime(), Math.min(getEndTime(), getTimestamp(event.x)));
            fScaledData.fSelectionEndBucket = Math.max(0, Math.min(fScaledData.fWidth - 1, event.x));
            fCanvas.redraw();
            fTimeLineScale.redraw();
        }
        updateStatusLine(fSelectionBegin, fSelectionEnd, getTimestamp(event.x));
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseEnter(final MouseEvent event) {
        updateStatusLine(fSelectionBegin, fSelectionEnd, getTimestamp(event.x));
    }

    @Override
    public void mouseExit(final MouseEvent event) {
        updateStatusLine(fSelectionBegin, fSelectionEnd, -1);
    }

    @Override
    public void mouseHover(final MouseEvent event) {
        // do nothing
    }

    private void updateStatusLine(long startTime, long endTime, long cursorTime) {
        ITimeDataProvider timeProvider = fTimeLineScale.getTimeProvider();
        if (timeProvider.getTime0() == timeProvider.getTime1()) {
            return;
        }
        TimeFormat timeFormat = timeProvider.getTimeFormat().convert();
        boolean isCalendar = timeFormat == TimeFormat.CALENDAR;

        StringBuilder message = new StringBuilder();
        String spaces = "     "; //$NON-NLS-1$
        if (cursorTime >= 0) {
            message.append("T: "); //$NON-NLS-1$
            if (isCalendar) {
                message.append(FormatTimeUtils.formatDate(cursorTime) + ' ');
            }
            message.append(FormatTimeUtils.formatTime(cursorTime, timeFormat, Resolution.NANOSEC));
            message.append(spaces);
        }

        if (startTime == endTime) {
            message.append("T1: "); //$NON-NLS-1$
            if (isCalendar) {
                message.append(FormatTimeUtils.formatDate(startTime) + ' ');
            }
            message.append(FormatTimeUtils.formatTime(startTime, timeFormat, Resolution.NANOSEC));
        } else {
            message.append("T1: "); //$NON-NLS-1$
            if (isCalendar) {
                message.append(FormatTimeUtils.formatDate(startTime) + ' ');
            }
            message.append(FormatTimeUtils.formatTime(startTime, timeFormat, Resolution.NANOSEC));
            message.append(spaces);
            message.append("T2: "); //$NON-NLS-1$
            if (isCalendar) {
                message.append(FormatTimeUtils.formatDate(endTime) + ' ');
            }
            message.append(FormatTimeUtils.formatTime(endTime, timeFormat, Resolution.NANOSEC));
            message.append(spaces);
            message.append("\u0394: " + FormatTimeUtils.formatDelta(endTime - startTime, timeFormat, Resolution.NANOSEC)); //$NON-NLS-1$
        }

        fStatusLineManager.setMessage(message.toString());
    }

    // ------------------------------------------------------------------------
    // ControlListener
    // ------------------------------------------------------------------------

    @Override
    public void controlMoved(final ControlEvent event) {
        fDataModel.complete();
    }

    @Override
    public void controlResized(final ControlEvent event) {
        fDataModel.complete();
    }

    // ------------------------------------------------------------------------
    // Signal Handlers
    // ------------------------------------------------------------------------

    /**
     * Format the timestamp and update the display
     *
     * @param signal
     *            the incoming signal
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        updateTimeFormat();
        fTimeLineScale.redraw();
        fComposite.layout();
    }

    private void updateTimeFormat() {
        HistogramTimeAdapter timeProvider = (HistogramTimeAdapter) fTimeLineScale.getTimeProvider();
        String datime = TmfTimePreferences.getPreferenceMap().get(ITmfTimePreferencesConstants.DATIME);
        if (ITmfTimePreferencesConstants.TIME_ELAPSED_FMT.equals(datime)) {
            timeProvider.setTimeFormat(TimeFormat.RELATIVE);
        } else {
            timeProvider.setTimeFormat(TimeFormat.CALENDAR);
        }
    }
}
