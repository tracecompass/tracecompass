/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Philippe Sawicki (INF4990.A2010@gmail.com)   - Initial API and implementation
 *   Mathieu Denis    (mathieu.denis55@gmail.com) - Refactored code
 *   Bernd Hufmann - Adapted to new model-view-controller design, display improvements
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.views.latency.listeners;

import org.eclipse.linuxtools.internal.lttng.ui.views.latency.AbstractViewer;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.model.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * <b><u>AbstractPaintListener</u></b>
 * <p> 
 * Abstract paint listener. Draws the graphs on the view canvas.
 * 
 * @author Philippe Sawicki
 */
public abstract class AbstractPaintListener implements PaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    /**
     *  Default colors and fonts
     */
    protected final Color DEFAULT_DATA_COLOR = new Color(Display.getDefault(), 74, 112, 139);
    protected final static Color DEFAULT_LABEL_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    protected final static Color DEFAULT_TEXT_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
    protected final static Color DEFAULT_DATA_BACKGROUND_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    protected final static Color DEFAULT_CURRENT_EVENT_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

    protected final Font DEFAULT_TITLE_FONT = new Font(Display.getDefault(), "Arial", 10, SWT.BOLD); //$NON-NLS-1$
    protected final Font DEFAULT_VALUES_FONT = new Font(Display.getDefault(), "Arial", 7, SWT.NORMAL); //$NON-NLS-1$
    protected final Font DEFAULT_LABEL_FONT = new Font(Display.getDefault(), "Arial", 8, SWT.NORMAL); //$NON-NLS-1$
    
    /**
     * A reference to the listener's view.
     */
    protected AbstractViewer fViewer;

    /**
     * Graph title
     */
    protected String fGraphTitle;

    /**
     * X-axis label.
     */
    protected String fXAxisLabel;
    
    /**
     * Y-axis label.
     */
    protected String fYAxisLabel;

    /**
     * Horizontal offset for the x-axis label.
     */
    protected int fXAxisLabelOffset;

    /**
     * Vertical offset for the horizontal axis offset.
     */
    protected int fHorizontalAxisYOffset = 20;

    /**
     * Graph padding.
     */
    protected int fPadding = Config.GRAPH_PADDING;

    /**
     * Graph client area.
     */
    protected Rectangle fClientArea = new Rectangle(0, 0, 1, 1);

    /**
     * Foreground color.
     */
    protected Color fForegroundColor;
    
    /**
     * Background color.
     */
    protected Color fBackgroundColor;
    
    /**
     * Data plotting color.
     */
    protected Color fDataColor;
    
    /**
     * Axis label color.
     */
    protected Color fLabelColor;
    
    /**
     * Text color.
     */
    protected Color fTextColor;
    
    /**
     * Data background color.
     */
    protected Color fDataBackgroundColor;

    /**
     * Color for current event time line.
     */
    protected Color fCurrentEventColor;

    /**
     * Original canvas font.
     */
    protected Font fOriginalFont;

    /**
     * Font for the title of the graph.
     */
    protected Font fTitleFont;

    /**
     * Font for the values on the horizontal and vertical axis.
     */
    protected Font fValuesFont;

    /**
     * Font for the horizontal and vertical labels.
     */
    protected Font fLabelFont;

    /**
     * Horizontal offset for the axis arrow.
     */
    protected final int ARROW_DELTA_X = 10;

    /**
     * Vertical offset for the axis arrow.
     */
    protected final int ARROW_DELTA_Y = 4;

    /**
     * Max horizontal distance between ticks.
     */
    protected final int MAX_WIDTH_BETWEEN_TICKS = 40;

    /**
     * Max vertical distance between ticks.
     */
    protected final int MAX_HEIGHT_BETWEEN_TICKS = 30;

    /**
     * Max characters that can be displayed on the vertical axis.
     */
    protected final int MAX_CHAR_VERTICAL_DISPLAY = 5;

    /**
     * Draw label each "drawLabelEachNTicks_" ticks.
     */
    protected int fDrawLabelEachNTicks = 1;

    /**
     * Image drawn on the canvas.
     */
    protected Image fImage;

    /**
     * Paint canvas, where the values are plotted.
     */
    protected GC fAxisImage;

    /**
     * Is the paint listener initialized ?
     */
    protected boolean fInitialized = false;

    /**
     * Draw area.
     */
    protected Rectangle fDrawArea;

    /**
     * Right padding (in pixels).
     */
    protected int fPaddingRight = Config.GRAPH_PADDING;

    /**
     * Top padding (in pixels).
     */
    protected int fPaddingTop = Config.GRAPH_PADDING;

    /**
     * Vertical axis offset (in pixels).
     */
    protected int fVerticalAxisOffset = 2 * Config.GRAPH_PADDING;

    /**
     * Vertical axis factor for values (10^delta). When values larger than MAX_CHAR_VERTICAL_DISPLAY.
     */
    protected int fDelta = 0;

    /**
     * The barWidth of a bar
     */
    protected int fBarWith = Config.DEFAULT_HISTOGRAM_BAR_WIDTH;

    /**
     * Minimum value on horizontal axis
     */
    protected long fXMin = -1;

    /**
     * Maximum value on horizontal axis
     */
    protected long fXMax = -1;

    /**
     * Minimum value on vertical axis
     */
    protected long fYMin = -1;

    /**
     * Maximum value on vertical axis
     */
    protected long fYMax = -1;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param view
     *            A reference to the listener's view.
     */
    public AbstractPaintListener(AbstractViewer view) {
        fViewer = view;
        fDataColor = DEFAULT_DATA_COLOR;
        fLabelColor = DEFAULT_LABEL_COLOR;
        fTextColor = DEFAULT_TEXT_COLOR;
        fDataBackgroundColor = DEFAULT_DATA_BACKGROUND_COLOR;
        fCurrentEventColor = DEFAULT_CURRENT_EVENT_COLOR;
        
        fTitleFont = DEFAULT_TITLE_FONT;
        fValuesFont = DEFAULT_VALUES_FONT;
        fLabelFont = DEFAULT_LABEL_FONT;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    
    /**
     * Returns the draw area height.
     * @return The draw area height.
     */
    public double getHeight() {
        return (fClientArea.height - 2.0 * fPadding - fHorizontalAxisYOffset - fPaddingTop);
    }

    /**
     * Returns the histogram's draw area width.
     * @return The histogram's draw area width.
     */
    public double getWidth() {
        return (fClientArea.width - 2.0 * fPadding - fVerticalAxisOffset - fPaddingRight);  // width of the plot area;
    }

    /**
     * Returns the histogram's draw area padding.
     * @return The histogram's draw area padding.
     */
    public int getPadding() {
        return fPadding;
    }

    /**
     * Returns the histogram's draw area top padding.
     * @return The histogram's draw area top padding.
     */
    public int getPaddingTop() {
        return fPaddingTop;
    }

    /**
     * Returns the histogram's vertical axis offset.
     * @return The histogram's vertical axis offset.
     */
    public int getVerticalAxisOffset() {
        return fVerticalAxisOffset;
    }

    /**
     * Returns the histogram's horizontal axis offset.
     * @return The histogram's horizontal axis offset.
     */
    public int getHorizontalAxisYOffset() {
        return fHorizontalAxisYOffset;
    }

    /**
     *  Returns the horizontal minimum value
     *  @return The horizontal minimum value.
     */
    public long getXMin() {
        return fXMin;
    }

    /**
     *  Returns the horizontal maximum value
     *  @return The horizontal maximum value.
     */
    public long getXMax() {
        return fXMax;
    }

    /**
     *  Returns the horizontal minimum value
     *  @return The horizontal minimum value.
     */
    public long getYMin() {
        return fYMin;
    }

    /**
     *  Returns the vertical maximum value
     *  @return The vertical maximum value.
     */
    public long getYMax() {
        return fYMax;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /**
     * Disposes local resources (e.g. colors or fonts)
     */
    public void dispose() {
        DEFAULT_DATA_COLOR.dispose();
        DEFAULT_TITLE_FONT.dispose();
        DEFAULT_VALUES_FONT.dispose();
        DEFAULT_LABEL_FONT.dispose();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
     */
    @Override
    public void paintControl(PaintEvent e) {
        fClientArea = fViewer.getClientArea();

        fForegroundColor = e.gc.getForeground();
        fBackgroundColor = e.gc.getBackground();
        fOriginalFont = e.gc.getFont();

        scale();
        
        if (!fInitialized) {
            fImage = new Image(Display.getDefault(), fViewer.getBounds());

            fAxisImage = new GC(fImage);

            fAxisImage.setForeground(fForegroundColor);
            fAxisImage.setBackground(fBackgroundColor);
            fAxisImage.fillRectangle(fImage.getBounds());

            fInitialized = true;
        }
        
        paintGraphTitle();
        paintBackground();     
        paintHorizontalAxis();
        paintVerticalAxis();
        paintContent();

        e.gc.drawImage(fImage, 0, 0);
    }

    /**
     * Paints the title of the graph.
     */
    public void paintGraphTitle() {
        if (fGraphTitle != null) {
            fAxisImage.setFont(fTitleFont);
            fAxisImage.setForeground(fLabelColor);
            fAxisImage.setBackground(fBackgroundColor);

            int zoomFactor = fViewer.getZoomFactor() / fViewer.getZoomIncrement() + 1;
            int labelWidth = fAxisImage.stringExtent(fGraphTitle).x;
            // Draws the zoom factor in the title only if there is one
            if (fViewer.getZoomFactor() > 1)
                fAxisImage.drawText(fGraphTitle + " (" + zoomFactor + "x)", (fViewer.getBounds().width - fPadding - labelWidth) / 2, 0); //$NON-NLS-1$ //$NON-NLS-2$
            else
                fAxisImage.drawText(fGraphTitle, (fViewer.getBounds().width - fPadding - labelWidth) / 2, 0);
        }
    }

    /**
     * Paints the background of the draw area.
     */
    public void paintBackground() {
        fAxisImage.setBackground(fDataBackgroundColor);

        fAxisImage.fillRectangle(fPadding + fVerticalAxisOffset, fPadding + fPaddingTop, (int)getWidth() + 1, (int)getHeight());
    }

    /**
     * Paints the horizontal axis.
     */
    public void paintHorizontalAxis() {
        fAxisImage.setForeground(fForegroundColor);

        int y = fClientArea.height - fPadding - fHorizontalAxisYOffset;

        fAxisImage.drawLine(fClientArea.x + fPadding + fVerticalAxisOffset, y, fClientArea.width - fPadding, y);

        paintHorizontalArrow(fClientArea.width - fPadding, y);
        // Draw the axis graphic details only if there are some data points (i.e. do not draw the axis graphic details
        // if the window timerange is so small that no latency can be computed, or if there are no matching events in
        // the timerange (for example, when an experiment has many traces with a large time gap between the logged
        // events sets).
        if (fXMin != Long.MAX_VALUE && fXMax != Long.MIN_VALUE && fXMin != fXMax) {
            paintHorizontalTicks(y);
            paintHorizontalAxisValues(y + 30);
        }
        paintHorizontalAxisLabel(y + fHorizontalAxisYOffset - 5);
    }

    /**
     * Paints the vertical axis.
     */
    public void paintVerticalAxis() {
        fAxisImage.setForeground(fForegroundColor);

        int x = fClientArea.x + fPadding + fVerticalAxisOffset;

        fAxisImage.drawLine(x, fPadding, x, fClientArea.height - fPadding - fHorizontalAxisYOffset);

        paintVerticalArrow(x, fClientArea.y + fPadding);
        // Draw the axis graphic details only if there are some data points (i.e. do not draw the axis graphic details
        // if the window timerange is so small that no latency can be computed, or if there are no matching events in
        // the timerange (for example, when an experiment has many traces with a large time gap between the logged
        // events sets).
        if (fXMin != Long.MAX_VALUE && fXMax != Long.MIN_VALUE && fXMin != fXMax) {
            paintVerticalTicks(x);
            paintVerticalAxisValues(x);
        }
        paintVerticalAxisLabel(x);
    }

    /**
     * Paints the arrow on the horizontal axis.
     * @param x
     *            The x-coordinate of the point where the arrow points.
     * @param y
     *            The y-coordinate of the point where the arrow points.
     */
    public void paintHorizontalArrow(int x, int y) {
        // Arrow top line
        fAxisImage.drawLine(x - ARROW_DELTA_X, y - ARROW_DELTA_Y, x, y);
        // Arrow bottom line
        fAxisImage.drawLine(x - ARROW_DELTA_X, y + ARROW_DELTA_Y, x, y);
    }

    /**
     * Paints the arrow on the vertical axis.
     * @param x
     *            The x-coordinate of the point where the arrow points.
     * @param y
     *            The y-coordinate of the point where the arrow points.
     */
    public void paintVerticalArrow(int x, int y) {
        // Arrow left line
        fAxisImage.drawLine(x - ARROW_DELTA_Y, y + ARROW_DELTA_X, x, y);
        // Arrow right line
        fAxisImage.drawLine(x + ARROW_DELTA_Y, y + ARROW_DELTA_X, x, y);
    }

    /**
     * Paints the horizontal ticks.
     * @param y
     *            The y coordinate where to draw the axis.
     */
    public void paintHorizontalTicks(int y) {
        if (fXMin >= 0L && fXMax >= 0L) {
            int nbTicks = (int)(getWidth()) / MAX_WIDTH_BETWEEN_TICKS + 1;

            for (int i = 0; i < nbTicks; i++) {
                if (i % fDrawLabelEachNTicks == 0) {
                    int x = i * MAX_WIDTH_BETWEEN_TICKS + fPadding + fVerticalAxisOffset;
                    fAxisImage.drawLine(x, y, x, y + 3);
                }
            }
        }
    }

    /**
     * Paints the horizontal axis values.
     * @param y
     *            The y coordinate where to draw the axis.
     */
    public void paintHorizontalAxisValues(int y) {
        if (fXMin >= 0L && fXMax >= 0L) {
            fAxisImage.setForeground(fTextColor);
            fAxisImage.setBackground(fBackgroundColor);

            double width = getWidth();
            int nbTicks = ((int)getWidth()) / MAX_WIDTH_BETWEEN_TICKS + 1;

            for (int i = 0; i < nbTicks; i++) {
                if (i % fDrawLabelEachNTicks == 0) {
                    int x = i * MAX_WIDTH_BETWEEN_TICKS + fPadding + fVerticalAxisOffset;
                    
                    long currentValue = (i * MAX_WIDTH_BETWEEN_TICKS)* (long)((fXMax - fXMin) / width) + fXMin;
                    String currentLabel = formatStringForHorizontalAxis(currentValue);

                    fAxisImage.setFont(fValuesFont);
                    fAxisImage.drawText(currentLabel, x, y - 24);
                }
            }
        }
    }

    /**
     * Paints the horizontal axis label.
     * @param y
     *            The y-coordinate where to draw the label.
     */
    public void paintHorizontalAxisLabel(int y) {
        if (fXAxisLabel != null) {
            fAxisImage.setFont(fLabelFont);
            fAxisImage.setForeground(fLabelColor);

            int labelWidth = fAxisImage.stringExtent(fXAxisLabel).x;

            fAxisImage.drawText(fXAxisLabel, fClientArea.width - fPadding - labelWidth, y);
        }
    }

    /**
     * Paints the vertical axis ticks.
     * @param x
     *            The x-coordinate where to draw the ticks.
     */
    public void paintVerticalTicks(int x) {
        if (fYMin != 0L && fYMax != 0L) {
            int nbTicks = (int)(getHeight() / MAX_HEIGHT_BETWEEN_TICKS + 1);

            for (int i = 0; i < nbTicks; i++) {
                int y = fClientArea.height - fPadding - fHorizontalAxisYOffset - i * MAX_HEIGHT_BETWEEN_TICKS;
                fAxisImage.drawLine(x - 3, y, x, y);
            }
        }
    }

    /**
     * Paints the vertical axis values.
     * @param x
     *            The x-coordinate where to draw the values.
     */
    public void paintVerticalAxisValues(int x) {
        if (fYMin >= 0L && fYMax >= 0L) {
            fAxisImage.setForeground(fTextColor);
            fAxisImage.setBackground(fBackgroundColor);

            double height = getHeight();
            int nbTicks = (int)(height / MAX_HEIGHT_BETWEEN_TICKS + 1);

            // System.out.println("nbTicks = " + nbTicks);

            for (int i = 0; i < nbTicks; i++) {
                int y = fClientArea.height - fPadding - fHorizontalAxisYOffset - i * MAX_HEIGHT_BETWEEN_TICKS;

                long currentValue = (i * MAX_HEIGHT_BETWEEN_TICKS)* (long)((fYMax - fYMin) / height) + fYMin;
                String currentLabel = formatStringForVerticalAxis(currentValue);

                fAxisImage.setFont(fValuesFont);

                Point textDimensions = fAxisImage.stringExtent(currentLabel);
                fAxisImage.drawText(currentLabel, x - textDimensions.x - 5, y - textDimensions.y / 2);
            }
        }
    }

    /**
     * Increases the bar width. 
     */
    public void increaseBarWitdh() {
        fBarWith = fBarWith << 1;
        if (fBarWith > Config.MAX_HISTOGRAM_BAR_WIDTH) {
            fBarWith = Config.MAX_HISTOGRAM_BAR_WIDTH;
        }
    }

    /**
     * Decreases the bar width. 
     */
    public void decreaseBarWitdh() {
        fBarWith = fBarWith >> 1;
        if (fBarWith < Config.MIN_HISTOGRAM_BAR_WIDTH) {
            fBarWith = Config.MIN_HISTOGRAM_BAR_WIDTH; 
        }
    }

    
    /**
     * Paints the vertical axis label.
     * @param x
     *            The x-coordinate where to draw the label.
     */
    public void paintVerticalAxisLabel(int x) {
        if (fYAxisLabel != null) {
            fAxisImage.setFont(fLabelFont);
            fAxisImage.setForeground(fLabelColor);
            fAxisImage.setBackground(fBackgroundColor);

            if (fDelta >= 1)
                fAxisImage.drawText(fYAxisLabel + " (x10^" + fDelta + ")", x + 10, fHorizontalAxisYOffset - 5);  //$NON-NLS-1$//$NON-NLS-2$
            else
                fAxisImage.drawText(fYAxisLabel, x + 10, fPadding);
        }
    }

    /**
     * Adds points to the graph and draws them to the canvas.
     * @param points
     *            The buffer of points to draw.
     * @param nbPoints
     *            The number of points in the buffer.
     */
    abstract public String formatToolTipLabel(int x, int y);

    /**
     * Method to be implemented to scale the model data to the actual screen size.
     */
    abstract public void scale();
    
    /**
     * Called for drawing elements after points are added to the graph.
     */
    abstract public void paintContent();

    /**
     * Clears the image and prepares it for redrawing.
     */
    public void clear() {
        fInitialized = false;
        fXMin = -1;
        fXMax = -1;
        fYMin = -1;
        fYMax = -1;
    }
    
    /**
     * Draw horizontal label each "nbTicks" ticks.
     * @param nbTicks
     *            The draw interval.
     */
    public void setDrawLabelEachNTicks(int nbTicks) {
        fDrawLabelEachNTicks = nbTicks;
    }

    /**
     * Sets the title of the graph.
     * @param graphTitle
     *            The title of the graph.
     */
    public void setGraphTitle(String graphTitle) {
        fGraphTitle = graphTitle;
    }

    /**
     * Sets the horizontal axis label.
     * @param xAxisLabel
     *            The horizontal axis label.
     * @param offset
     *            The horizontal axis draw offset (in pixels).
     */
    public void setXAxisLabel(String xAxisLabel, int offset) {
        fXAxisLabel = xAxisLabel;
        fXAxisLabelOffset = offset;
    }

    /**
     * Sets the vertical axis label.
     * @param yAxisLabel
     *            The vertical axis label.
     */
    public void setYAxisLabel(String yAxisLabel) {
        fYAxisLabel = yAxisLabel;
    }

    /**
     * Returns a string representing the given value. 
     * 
     * @param value
     *            The numeric value to convert to String.
     * @return The String-formatted value.
     */
    public String formatStringForHorizontalAxis(long value) {
        return String.valueOf(value);
    }

    /**
     * Returns a string representing the given value. 
     * 
     * @param value
     *            The numeric value to convert to String.
     * @return The String-formatted value.
     */
    public String formatStringForVerticalAxis(long value) {
        return String.valueOf(value);
    }
}