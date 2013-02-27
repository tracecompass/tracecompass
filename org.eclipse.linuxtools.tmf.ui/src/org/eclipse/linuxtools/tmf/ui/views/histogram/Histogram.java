/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Changed to updated histogram data model
 *   Francois Chouinard - Reformat histogram labels on format change
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

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
 * The widget also has 2 'markers' to identify:
 * <ul>
 * <li>a red dashed line over the bar that contains the currently selected event
 * <li>a dark red dashed line that delimits the right end of the histogram (if
 * it doesn't fill the canvas)
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
public abstract class Histogram implements ControlListener, PaintListener, KeyListener, MouseListener, MouseTrackListener, IHistogramModelListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Histogram colors
    private final Color fBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    private final Color fCurrentEventColor = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
    private final Color fLastEventColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
    private final Color fHistoBarColor = new Color(Display.getDefault(), 74, 112, 139);

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
    private Text fMaxNbEventsText;
    private Text fMinNbEventsText;
    private Text fTimeRangeStartText;
    private Text fTimeRangeEndText;

    /**
     *  Histogram drawing area
     */
    protected Canvas fCanvas;

    /**
     *  The histogram data model.
     */
    protected final HistogramDataModel fDataModel;

    /**
     * The histogram data model scaled to current resolution and screen width.
     */
    protected HistogramScaledData fScaledData;

    /**
     * The current event value
     */
    protected long fCurrentEventTime = 0L;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Full constructor.
     *
     * @param view A reference to the parent TMF view.
     * @param parent A parent composite
     */
    public Histogram(final TmfView view, final Composite parent) {
        fParentView = view;

        fComposite = createWidget(parent);
        fDataModel = new HistogramDataModel();
        fDataModel.addHistogramListener(this);
        clear();

        fCanvas.addControlListener(this);
        fCanvas.addPaintListener(this);
        fCanvas.addKeyListener(this);
        fCanvas.addMouseListener(this);
        fCanvas.addMouseTrackListener(this);

        TmfSignalManager.register(this);
    }

    /**
     * Dispose resources and unregisters listeners.
     */
    public void dispose() {
        TmfSignalManager.deregister(this);

        fHistoBarColor.dispose();
        fDataModel.removeHistogramListener(this);
    }

    private Composite createWidget(final Composite parent) {

        final Color labelColor = parent.getBackground();
        fFont = adjustFont(parent);

        final int initalWidth = 10;

        // --------------------------------------------------------------------
        // Define the histogram
        // --------------------------------------------------------------------

        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        final Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(gridLayout);

        // Use all the horizontal space
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        composite.setLayoutData(gridData);

        // Y-axis max event
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.TOP;
        fMaxNbEventsText = new Text(composite, SWT.READ_ONLY | SWT.RIGHT);
        fMaxNbEventsText.setFont(fFont);
        fMaxNbEventsText.setBackground(labelColor);
        fMaxNbEventsText.setEditable(false);
        fMaxNbEventsText.setText("0"); //$NON-NLS-1$
        fMaxNbEventsText.setLayoutData(gridData);

        // Histogram itself
        Composite canvasComposite = new Composite(composite, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.verticalSpan = 2;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        canvasComposite.setLayoutData(gridData);
        canvasComposite.setLayout(new FillLayout());
        fCanvas = new Canvas(canvasComposite, SWT.DOUBLE_BUFFERED);

        // Y-axis min event (always 0...)
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.BOTTOM;
        fMinNbEventsText = new Text(composite, SWT.READ_ONLY | SWT.RIGHT);
        fMinNbEventsText.setFont(fFont);
        fMinNbEventsText.setBackground(labelColor);
        fMinNbEventsText.setEditable(false);
        fMinNbEventsText.setText("0"); //$NON-NLS-1$
        fMinNbEventsText.setLayoutData(gridData);

        // Dummy cell
        gridData = new GridData(initalWidth, SWT.DEFAULT);
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.BOTTOM;
        final Text dummyText = new Text(composite, SWT.READ_ONLY);
        dummyText.setFont(fFont);
        dummyText.setBackground(labelColor);
        dummyText.setEditable(false);
        dummyText.setText(""); //$NON-NLS-1$
        dummyText.setLayoutData(gridData);

        // Window range start time
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.BOTTOM;
        fTimeRangeStartText = new Text(composite, SWT.READ_ONLY);
        fTimeRangeStartText.setFont(fFont);
        fTimeRangeStartText.setBackground(labelColor);
        fTimeRangeStartText.setLayoutData(gridData);

        // Window range end time
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.BOTTOM;
        fTimeRangeEndText = new Text(composite, SWT.READ_ONLY);
        fTimeRangeEndText.setFont(fFont);
        fTimeRangeEndText.setBackground(labelColor);
        fTimeRangeEndText.setLayoutData(gridData);

        return composite;
    }

    private static Font adjustFont(final Composite composite) {
        // Reduce font size for a more pleasing rendering
        final int fontSizeAdjustment = -2;
        final Font font = composite.getFont();
        final FontData fontData = font.getFontData()[0];
        return new Font(font.getDevice(), fontData.getName(), fontData.getHeight() + fontSizeAdjustment, fontData.getStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the start time (equal first bucket time)
     * @return the start time.
     */
    public long getStartTime() {
        return fDataModel.getFirstBucketTime();
    }

    /**
     * Returns the end time.
     * @return the end time.
     */
    public long getEndTime() {
        return fDataModel.getEndTime();
    }

    /**
     * Returns the time limit (end of last bucket)
     * @return the time limit.
     */
    public long getTimeLimit() {
        return fDataModel.getTimeLimit();
    }

    /**
     * Returns a data model reference.
     * @return data model.
     */
    public HistogramDataModel getDataModel() {
        return fDataModel;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Updates the time range.
     * @param startTime A start time
     * @param endTime A end time.
     */
    public abstract void updateTimeRange(long startTime, long endTime);

    /**
     * Clear the histogram and reset the data
     */
    public void clear() {
        fDataModel.clear();
        synchronized (fDataModel) {
            fScaledData = null;
        }
    }

    /**
     * Increase the histogram bucket corresponding to [timestamp]
     *
     * @param eventCount
     *            The new event count
     * @param timestamp
     *            The latest timestamp
     */
    public void countEvent(final long eventCount, final long timestamp) {
        fDataModel.countEvent(eventCount, timestamp);
    }

    /**
     * Sets the current event time and refresh the display
     *
     * @param timestamp
     *            The time of the current event
     */
    public void setCurrentEvent(final long timestamp) {
        fCurrentEventTime = (timestamp > 0) ? timestamp : 0;
        fDataModel.setCurrentEventNotifyListeners(timestamp);
    }

    /**
     * Computes the timestamp of the bucket at [offset]
     *
     * @param offset offset from the left on the histogram
     * @return the start timestamp of the corresponding bucket
     */
    public synchronized long getTimestamp(final int offset) {
        assert offset > 0 && offset < fScaledData.fWidth;
        try {
            return fDataModel.getFirstBucketTime() + fScaledData.fBucketDuration * offset;
        } catch (final Exception e) {
            return 0; // TODO: Fix that racing condition (NPE)
        }
    }

    /**
     * Computes the offset of the timestamp in the histogram
     *
     * @param timestamp the timestamp
     * @return the offset of the corresponding bucket (-1 if invalid)
     */
    public synchronized int getOffset(final long timestamp) {
        if (timestamp < fDataModel.getFirstBucketTime() || timestamp > fDataModel.getEndTime()) {
            return -1;
        }
        return (int) ((timestamp - fDataModel.getFirstBucketTime()) / fScaledData.fBucketDuration);
    }

    /**
     * Move the currently selected bar cursor to a non-empty bucket.
     *
     * @param keyCode the SWT key code
     */
    protected void moveCursor(final int keyCode) {

        if (fScaledData.fCurrentBucket == HistogramScaledData.OUT_OF_RANGE_BUCKET) {
            return;
        }

        int index;
        switch (keyCode) {

            case SWT.HOME:
                index = 0;
                while (index < fScaledData.fLastBucket && fScaledData.fData[index] == 0) {
                    index++;
                }
                if (index < fScaledData.fLastBucket) {
                    fScaledData.fCurrentBucket = index;
                }
                break;

            case SWT.ARROW_RIGHT:
                index = fScaledData.fCurrentBucket + 1;
                while (index < fScaledData.fWidth && fScaledData.fData[index] == 0) {
                    index++;
                }
                if (index < fScaledData.fLastBucket) {
                    fScaledData.fCurrentBucket = index;
                }
                break;

            case SWT.END:
                index = fScaledData.fLastBucket;
                while (index >= 0 && fScaledData.fData[index] == 0) {
                    index--;
                }
                if (index >= 0) {
                    fScaledData.fCurrentBucket = index;
                }
                break;

            case SWT.ARROW_LEFT:
                index = fScaledData.fCurrentBucket - 1;
                while (index >= 0 && fScaledData.fData[index] == 0) {
                    index--;
                }
                if (index >= 0) {
                    fScaledData.fCurrentBucket = index;
                }
                break;

            default:
                return;
        }

        updateCurrentEventTime();
    }

    /**
     * Refresh the histogram display
     */
    @Override
    public void modelUpdated() {
        if (!fCanvas.isDisposed() && fCanvas.getDisplay() != null) {
            fCanvas.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!fCanvas.isDisposed()) {
                        // Retrieve and normalize the data
                        final int canvasWidth = fCanvas.getBounds().width;
                        final int canvasHeight = fCanvas.getBounds().height;
                        if (canvasWidth <= 0 || canvasHeight <= 0) {
                            return;
                        }
                        fDataModel.setCurrentEvent(fCurrentEventTime);
                        fScaledData = fDataModel.scaleTo(canvasWidth, canvasHeight, 1);
                        synchronized(fDataModel) {
                            if (fScaledData != null) {
                                fCanvas.redraw();
                                if (fDataModel.getNbEvents() != 0) {
                                    // Display histogram and update X-,Y-axis labels
                                    fTimeRangeStartText.setText(TmfTimestampFormat.getDefaulTimeFormat().format(fDataModel.getFirstBucketTime()));
                                    fTimeRangeEndText.setText(TmfTimestampFormat.getDefaulTimeFormat().format(fDataModel.getEndTime()));
                                } else {
                                    fTimeRangeStartText.setText(""); //$NON-NLS-1$
                                    fTimeRangeEndText.setText(""); //$NON-NLS-1$
                                }
                                fMaxNbEventsText.setText(Long.toString(fScaledData.fMaxValue));
                                // The Y-axis area might need to be re-sized
                                fMaxNbEventsText.getParent().layout();
                            }
                        }
                    }
                }
            });
        }
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void updateCurrentEventTime() {
        final long bucketStartTime = getTimestamp(fScaledData.fCurrentBucket);
        ((HistogramView) fParentView).updateCurrentEventTime(bucketStartTime);
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
            image = new Image(event.display, canvasWidth, canvasHeight);
            fCanvas.setData(IMAGE_KEY, image);
        }

        // Draw the histogram on its canvas
        final GC imageGC = new GC(image);
        formatImage(imageGC, image);
        event.gc.drawImage(image, 0, 0);
        imageGC.dispose();
    }

    private void formatImage(final GC imageGC, final Image image) {

        if (fScaledData == null) {
            return;
        }

        final HistogramScaledData scaledData = new HistogramScaledData(fScaledData);

        try {
            // Get drawing boundaries
            final int width = image.getBounds().width;
            final int height = image.getBounds().height;

            // Clear the drawing area
            imageGC.setBackground(fBackgroundColor);
            imageGC.fillRectangle(0, 0, image.getBounds().width + 1, image.getBounds().height + 1);

            // Draw the histogram bars
            imageGC.setBackground(fHistoBarColor);
            final int limit = width < scaledData.fWidth ? width : scaledData.fWidth;
            for (int i = 0; i < limit; i++) {
                final int value = (int) Math.ceil(scaledData.fData[i] * scaledData.fScalingFactor);
                imageGC.fillRectangle(i, height - value, 1, value);
            }

            // Draw the current event bar
            final int currentBucket = scaledData.fCurrentBucket;
            if (currentBucket >= 0 && currentBucket < limit) {
                drawDelimiter(imageGC, fCurrentEventColor, height, currentBucket);
            }

            // Add a dashed line as a delimiter (at the right of the last bar)
            int lastEventIndex = limit - 1;
            while (lastEventIndex >= 0 && scaledData.fData[lastEventIndex] == 0) {
                lastEventIndex--;
            }
            lastEventIndex += (lastEventIndex < limit - 1) ? 1 : 0;
            drawDelimiter(imageGC, fLastEventColor, height, lastEventIndex);
        } catch (final Exception e) {
            // Do nothing
        }
    }

    private static void drawDelimiter(final GC imageGC, final Color color,
            final int height, final int index) {
        imageGC.setBackground(color);
        final int dash = height / 4;
        imageGC.fillRectangle(index, 0 * dash, 1, dash - 1);
        imageGC.fillRectangle(index, 1 * dash, 1, dash - 1);
        imageGC.fillRectangle(index, 2 * dash, 1, dash - 1);
        imageGC.fillRectangle(index, 3 * dash, 1, height - 3 * dash);
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
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseDoubleClick(final MouseEvent event) {
    }

    @Override
    public void mouseDown(final MouseEvent event) {
        if (fDataModel.getNbEvents() > 0 && fScaledData.fLastBucket >= event.x) {
            fScaledData.fCurrentBucket = event.x;
            updateCurrentEventTime();
        }
    }

    @Override
    public void mouseUp(final MouseEvent event) {
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseEnter(final MouseEvent event) {
    }

    @Override
    public void mouseExit(final MouseEvent event) {
    }

    @Override
    public void mouseHover(final MouseEvent event) {
        if (fDataModel.getNbEvents() > 0 && fScaledData != null && fScaledData.fLastBucket >= event.x) {
            final String tooltip = formatToolTipLabel(event.x);
            fCanvas.setToolTipText(tooltip);
        } else {
            fCanvas.setToolTipText(null);
        }
    }

    private String formatToolTipLabel(final int index) {
        long startTime = fScaledData.getBucketStartTime(index);
        // negative values are possible if time values came into the model in decreasing order
        if (startTime < 0) {
            startTime = 0;
        }
        final long endTime = fScaledData.getBucketEndTime(index);
        final int nbEvents = (index >= 0) ? fScaledData.fData[index] : 0;

        final StringBuffer buffer = new StringBuffer();
        buffer.append("Range = ["); //$NON-NLS-1$
        buffer.append(new TmfTimestamp(startTime, ITmfTimestamp.NANOSECOND_SCALE).toString());
        buffer.append(","); //$NON-NLS-1$
        buffer.append(new TmfTimestamp(endTime, ITmfTimestamp.NANOSECOND_SCALE).toString());
        buffer.append(")\n"); //$NON-NLS-1$
        buffer.append("Event count = "); //$NON-NLS-1$
        buffer.append(nbEvents);
        return buffer.toString();
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
     * @param signal the incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        if (fDataModel.getNbEvents() == 0) {
            return;
        }

        String newTS = TmfTimestampFormat.getDefaulTimeFormat().format(fDataModel.getFirstBucketTime());
        fTimeRangeStartText.setText(newTS);

        newTS = TmfTimestampFormat.getDefaulTimeFormat().format(fDataModel.getEndTime());
        fTimeRangeEndText.setText(newTS);

        fComposite.layout();
    }

}
