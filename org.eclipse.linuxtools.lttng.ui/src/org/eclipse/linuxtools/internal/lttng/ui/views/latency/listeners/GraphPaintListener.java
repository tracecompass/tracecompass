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

import java.text.DecimalFormat;

import org.eclipse.linuxtools.internal.lttng.ui.views.latency.AbstractViewer;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.GraphViewer;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.model.Config;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.model.GraphScaledData;
import org.eclipse.linuxtools.internal.lttng.ui.views.latency.model.IGraphDataModel;
import org.eclipse.linuxtools.tmf.ui.views.distribution.model.BaseDistributionData;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramUtils;

/**
 * <b><u>GraphPaintListener</u></b>
 * <p>
 * Graph paint listener.
 * 
 * @author Philippe Sawicki
 */
public class GraphPaintListener extends AbstractPaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Scaled data from data model
     */
    protected GraphScaledData fScaledData;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     * @param viewer
     *            A reference to the listener's viewer.
     */
    public GraphPaintListener(AbstractViewer view) {
        super(view);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

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

         IGraphDataModel model = ((GraphViewer)fViewer).getModel();
         fScaledData = model.scaleTo((int)width, (int)height, fBarWith);

         fXMin = fScaledData.getHorFirstBucketTime() > 0 ? fScaledData.getHorFirstBucketTime() : 0;
         fXMax = 0;
         if (fScaledData.getHorLastBucket() > 0) {
             fXMax = fScaledData.getHorBucketEndTime(fScaledData.getHorNbBuckets() - 1);
         }
         
         fYMin = fScaledData.getVerFirstBucketTime() > 0 ? fScaledData.getVerFirstBucketTime() : 0;
         fYMax = 0;
         if (fScaledData.getVerLastBucket() > 0) {
             fYMax = fScaledData.getVerBucketEndTime(fScaledData.getVerNbBuckets() - 1);
         }
     }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#paintContent()
     */
    @Override
    public void paintContent() {
        if (fXMin >= 0 && fXMax >= 0 && fYMin >= 0 && fYMax >= 0 && fScaledData != null) {
            
            fAxisImage.setForeground(fDataColor);
            fAxisImage.setBackground(fDataColor);

            double height = getHeight();

            int xLen = fScaledData.getHorNbBuckets();
            int yLen = fScaledData.getVerNbBuckets();

            int barWidth = fScaledData.getBarWidth();
            
            for (int i = 0; i < xLen; i++) {
                for (int j = 0; j < yLen; j++) {
                    if (fScaledData.getEventCount(i, j) > 0) {

                        double x = fPadding + i * barWidth + fVerticalAxisOffset + 1;
                        double y = fPadding + fPaddingTop + height - j * barWidth;

                        // Avoid over-drawing background area
                        int yBarWidth = fBarWith;
                        if (y - yBarWidth < fPadding + fPaddingTop) {
                            yBarWidth = (int) (y - fPadding - fPaddingTop);
                        }
                        int xBarWidth = fBarWith;
                        if(x + xBarWidth > fClientArea.width - fPadding - fPaddingRight) {
                            xBarWidth =  (int)(fClientArea.width - fPadding - fPaddingRight - x);    
                        }
                        fAxisImage.fillRectangle((int) x, (int) y - (int) yBarWidth, (int) xBarWidth, (int) yBarWidth);
                    }
                }
            }
            
            if (fScaledData.isCurrentEventTimeValid()) {
                // Draw vertical line
                int index = fScaledData.getHorBucketIndex(fScaledData.getCurrentEventTime());

                int x = fPadding + index * barWidth + fVerticalAxisOffset + 1;
                fAxisImage.setForeground(fCurrentEventColor);
                fAxisImage.setBackground(fCurrentEventColor);
                fAxisImage.drawLine(x, fPadding + fPaddingTop, x, fClientArea.height - fPadding - fHorizontalAxisYOffset);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#formatStringForVerticalAxis(long)
     */
    @Override
    public String formatStringForVerticalAxis(long value) {
        DecimalFormat formatter = new DecimalFormat("0.0E0"); //$NON-NLS-1$
        return formatter.format(value);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#formatStringForHorizontalAxis(long)
     */
    @Override
    public String formatStringForHorizontalAxis(long value) {
        return HistogramUtils.nanosecondsToString(value);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.latency.listeners.AbstractPaintListener#formatToolTipLabel(int, int)
     */
    @Override
    public String formatToolTipLabel(int x, int y) {

        int index = getIndexFromHorizontalValue(x);
        int yIndex = getIndexFromVerticalValue(y);

        if (index != BaseDistributionData.OUT_OF_RANGE_BUCKET && yIndex != BaseDistributionData.OUT_OF_RANGE_BUCKET) {
            if (fScaledData.getEventCount(index, yIndex) > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("Time Range in s = ["); //$NON-NLS-1$
                // TODO change Utility
                long startTime = fScaledData.getHorBucketStartTime(index) > 0 ? fScaledData.getHorBucketStartTime(index) : 0;
                buffer.append(HistogramUtils.nanosecondsToString(startTime));
                buffer.append(","); //$NON-NLS-1$
                buffer.append(HistogramUtils.nanosecondsToString(fScaledData.getHorBucketEndTime(index)));
                buffer.append("]\n"); //$NON-NLS-1$
                buffer.append("Latency Range in s = ["); //$NON-NLS-1$
                long yStartTime = fScaledData.getVerBucketStartTime(yIndex) > 0 ? fScaledData.getVerBucketStartTime(yIndex) : 0;
                buffer.append(HistogramUtils.nanosecondsToString(yStartTime));
                buffer.append(","); //$NON-NLS-1$
                buffer.append(HistogramUtils.nanosecondsToString(fScaledData.getVerBucketEndTime(yIndex)));
                buffer.append("]\n"); //$NON-NLS-1$
                buffer.append("Latency count = "); //$NON-NLS-1$
                buffer.append(fScaledData.getEventCount(index, yIndex));
                return buffer.toString();
            }
        }
        return ""; //$NON-NLS-1$
    }
    
    public int getIndexFromHorizontalValue(int x) {
        if (fScaledData != null) {
            double barWidth = fScaledData.getBarWidth();
            
            int index = (int) ((x - fPadding - fVerticalAxisOffset - 1) / barWidth);
            if ((index >= 0) && (fScaledData.getHorNbBuckets() > index)) {
                return index;
            }
        }
        return BaseDistributionData.OUT_OF_RANGE_BUCKET;
    }
    
    public int getIndexFromVerticalValue(int y) {
        if (fScaledData != null) {
            double barWidth = fScaledData.getBarWidth();
            double height = getHeight();     // height of the plot area
            
            int index = (int) ((height - (y - fPadding - fPaddingTop)) / barWidth);
            if (index >= 0 && fScaledData.getVerNbBuckets() > index) {
                return index;
            }
        }
        return BaseDistributionData.OUT_OF_RANGE_BUCKET;
    }

    public long getCurrentTimeFromHorizontalValue(int x) {
        if (fXMin >= 0 && fXMax >= 0) {
            int index = getIndexFromHorizontalValue(x);
            if (index != BaseDistributionData.OUT_OF_RANGE_BUCKET) {
                return fScaledData.getHorBucketStartTime(index);
            }
        }
        return Config.INVALID_EVENT_TIME;
    }
}