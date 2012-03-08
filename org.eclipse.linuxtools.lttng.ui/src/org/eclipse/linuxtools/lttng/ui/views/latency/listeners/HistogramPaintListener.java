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
package org.eclipse.linuxtools.lttng.ui.views.latency.listeners;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.ui.views.latency.AbstractViewer;
import org.eclipse.linuxtools.lttng.ui.views.latency.HistogramViewer;
import org.eclipse.linuxtools.lttng.ui.views.latency.Messages;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramScaledData;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramUtils;
import org.eclipse.linuxtools.tmf.ui.views.histogram.IHistogramDataModel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * <b><u>HistogramPaintListener</u></b>
 * <p>
 * Histogram paint listener.
 * 
 * @author Philippe Sawicki
 */
public class HistogramPaintListener extends AbstractPaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    /**
     * Is a histogram bar so high that it is clipped from the draw area ?
     */
    private boolean fBarIsClipped = false;

    /**
     * Scaled data from data model
     */
    protected HistogramScaledData fScaledData;

    /**
     * Warning Image
     */
    protected Image fWarningImage;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    
    /**
     * Constructor.
     * @param view
     *            A reference to the listener's viewer.
     */
    public HistogramPaintListener(AbstractViewer viewer) {
        super(viewer);
        fWarningImage = AbstractUIPlugin.imageDescriptorFromPlugin(Messages.LatencyView_tmf_UI, "icons/elcl16/warning.gif").createImage(Display.getCurrent()); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    
    /**
     * Returns the histogram's bar Width.
     * @return The histogram's bar Width.
     */
    public int getBarWidth() {
        return fBarWith;
    }
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#dispose()
     */
    @Override
    public void dispose() {
        fWarningImage.dispose();
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#scale()
     */
    @Override
    public void scale() {
        // width of the plot area
        double width = getWidth();
        // height of the plot area
        double height = getHeight();

        int barWidth = getBarWidth();

        IHistogramDataModel model = ((HistogramViewer)fViewer).getModel();
        fScaledData = model.scaleTo((int)width, (int)height, barWidth);

        fYMin = 0;
        fYMax = fScaledData.fMaxValue;

        fXMin = fScaledData.getFirstBucketTime();
        fXMin = fXMin > 0 ? fXMin : 0; 
        fXMax = fScaledData.getBucketEndTime(fScaledData.fLastBucket - 1);

        // No data to display - set end time to 0 
        if (fYMax == 0) {
            fXMax = 0;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#paintVerticalTicks(int)
     */
    @Override
    public void paintVerticalTicks(int x) {
        // done in method paintVerticalAxisValues()
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#paintVerticalAxisValues(int)
     */
    @Override
    public void paintVerticalAxisValues(int x) {
        int zoomFactor = 1;

        zoomFactor = fViewer.getZoomFactor();

        if (fYMin >= 0L && fYMax != 0L) {
            fAxisImage.setForeground(fTextColor);
            fAxisImage.setBackground(fBackgroundColor);

            // Apply the zoom to the max value of the graph for the next calculations
            long yMax = fYMax / zoomFactor;

            int nbTicks = ((int)getHeight()) / MAX_HEIGHT_BETWEEN_TICKS + 1;

            Vector<Integer> values = new Vector<Integer>();
            boolean multipleSameValues = true;
            while (multipleSameValues) {
                double valueStep = (double) (yMax - fYMin) / (double) (nbTicks);

                for (int i = 0; i < nbTicks; i++) {
                    double currentValue = (double) (fYMin + i * valueStep) / (Math.pow(10, fDelta));

                    values.add((int) currentValue);
                }

                Collections.sort(values);
                boolean hasRepetition = false;
                for (int i = 1; i < values.size(); i++) {
                    if (values.get(i) == values.get(i - 1)) {
                        hasRepetition = true;
                        break;
                    }
                }

                if (hasRepetition) {
                    nbTicks--;
                    values.clear();
                } else {
                    multipleSameValues = false;

                    // Draw rectangle over the old values
                    int height = fViewer.getBounds().height - 2 * fPadding - fPaddingTop - fHorizontalAxisYOffset;
                    fAxisImage.fillRectangle(0, fPadding + fPaddingTop, fPadding + fVerticalAxisOffset, height);

                    double pixelStep = (getHeight()) / values.size() + 1;

                    for (int i = 0; i < values.size(); i++) {
                        double currentValue = values.get(i);

                        int y = (int)  (fClientArea.height - fPadding - fHorizontalAxisYOffset -  i * pixelStep);
                        String currentLabel = formatStringForVerticalAxis((long) currentValue);

                        fAxisImage.setFont(fValuesFont);

                        Point textDimensions = fAxisImage.stringExtent(currentLabel);
                        fAxisImage.drawText(currentLabel, x - textDimensions.x - 5, y - textDimensions.y / 2);
                        fAxisImage.drawLine(x - 3, y, x, y);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#paintContent()
     */
    @Override
    public void paintContent() {
        double zoomFactor = fViewer.getZoomFactor();

        // Calculate the vertical axis factor and see if it has changed
        double tmpDelta = fDelta;
        fDelta = 0;
        if (Long.toString(fYMax / (long) zoomFactor).length() > MAX_CHAR_VERTICAL_DISPLAY) {
            fDelta = Long.toString(fYMax / (long) zoomFactor).length() - MAX_CHAR_VERTICAL_DISPLAY;
        }
        if (tmpDelta != fDelta) {
            fViewer.clearBackground();
        }

        paintBackground();
        paintVerticalAxis();
        paintHorizontalAxis();

        fAxisImage.setForeground(fDataColor);
        fAxisImage.setBackground(fDataColor);

        // height of the plot area
        double height = getHeight();

        int barWidth = getBarWidth();
        
        // axisImage_.setBackground(backgroundColor_);
        // 1.a Iterate over the points, from 0 to nbPoints
        // 1.b Find the max counter value
        // 2. Assign the max value to the "yMax_" class attribute
        // 3. Draw the histogram bars using "axisImage_.fillRectangle(...)"
        boolean oneBarIsClipped = false;

        for (int i = 0; i < fScaledData.fData.length; i++) {
            double pointY = fScaledData.fData[i];

            // in pixels
            double x = fPadding + i * barWidth + fVerticalAxisOffset + 1;

            if (i == fScaledData.fData.length - 1)
                x -= 1.0;
            double barHeight = zoomFactor * ((double)(pointY - fYMin) / (double)(fYMax - fYMin)) * height;

            if (barHeight > height + 1) {
                barHeight = height;
                oneBarIsClipped = true;

                fAxisImage.drawImage(fWarningImage, 5, 3);
            }

            // Only draw the bars that have a barHeight of more than 1 pixel
            if (barHeight > 0) {
                double y = fPadding + fPaddingTop + height - barHeight;
                fAxisImage.setBackground(fDataColor);

                if (barHeight > height - 1) {
                    fAxisImage.fillRectangle((int) x, (int) y, (int) barWidth, (int) (barHeight + 1));
                } else {
                    fAxisImage.fillRectangle((int) x, (int) y, (int) barWidth, (int) (barHeight + 2));
                }
            }
        }

        if (oneBarIsClipped)
            fBarIsClipped = true;
        else
            fBarIsClipped = false;
    }
    
    /**
     * Paints the histogram horizontal axis values in engineering notation in which the exponent is a multiple of three.
     * @param value
     *            The numeric value to convert to engineering notation.
     * @return The given value formatted according to the engineering notation.
     */
    @Override
    public String formatStringForHorizontalAxis(long value) {
        DecimalFormat formatter = new DecimalFormat("##0.#E0"); //$NON-NLS-1$
        return formatter.format(value);
    }

    /**
     * Sets the bar width.
     * @param barWidth 
     *            bar width to set
     */
    public void setBarWidth(int barWidth) {
        fBarWith = barWidth;
    }
    
    /**
     * Returns "true" if a histogram bar is so high that it cannot be drawn in the draw area, "false" otherwise.
     * @return "true" if a histogram bar is so high that it cannot be drawn in the draw area, "false" otherwise.
     */
    public boolean barIsClipped() {
        return fBarIsClipped;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#formatToolTipLabel(int, int)
     */
    @Override
    public String formatToolTipLabel(int x, int y) {
        if (fScaledData != null) {

            double barWidth = getBarWidth();
            double height = getHeight();     // height of the plot area
            
            double zoomFactor = fViewer.getZoomFactor();

            int index = (int) ((x - fPadding - fVerticalAxisOffset - 1) / barWidth);

            double barHeight = 0.0;
            if (index >= 0 && index <= fScaledData.fLastBucket) {
                barHeight = (zoomFactor * height * (fScaledData.fData[index] - fYMin) / (fYMax - fYMin));
            }

            long fMouseY = (long) (height - (y - fPadding - fPaddingTop));

            // Verifying mouse pointer is over histogram bar
            if (index >= 0 && fScaledData.fLastBucket >= index && fMouseY >= 0 && fMouseY < barHeight && fMouseY < height && x >= (fVerticalAxisOffset + fPadding)) {

                fScaledData.fCurrentBucket = index;

                long startTime = fScaledData.getBucketStartTime(index);
                // negative values are possible if time values came into the model in decreasing order
                if (startTime < 0) {
                    startTime = 0;
                }
                long endTime = fScaledData.getBucketEndTime(index);
                int nbEvents = fScaledData.fData[index];

                StringBuffer buffer = new StringBuffer();
                buffer.append("Latency Range in s = ["); //$NON-NLS-1$
                buffer.append(HistogramUtils.nanosecondsToString(startTime));
                buffer.append(","); //$NON-NLS-1$
                buffer.append(HistogramUtils.nanosecondsToString(endTime));
                buffer.append("]\n"); //$NON-NLS-1$
                buffer.append("Latency count = "); //$NON-NLS-1$
                buffer.append(nbEvents);
                return buffer.toString();
            }
        }
        return ""; //$NON-NLS-1$
    }
}