/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * <p>
 * This class is used to reproduce the same tooltip behavior on Windows and Linux when the mouse move hover the time
 * compression bar used to display elapsed time using a tooltip. The tooltip is composed of 2 parts, the text value and
 * below, a scale to visually locate the value in a time range (usually the minimum an maximum elapsed time in the whole
 * diagram)
 * </p>
 *
 * @version 1.0
 * @author sveyrier
 */
public class DrawableToolTip implements PaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The parent control where the tooltip must be drawn
     */
    protected Composite fParent = null;
    /**
     * The tooltip shell
     */
    protected Shell fToolTipShell = null;
    /**
     * The Time range data.
     */
    protected TmfTimeRange fMinMaxRange;
    /**
     * The current time.
     */
    protected ITmfTimestamp fCurrentValue;
    /**
     * The horizontal margin used for drawing.
     */
    private static int fHorMargin = 10;
    /**
     * The vertical margin used for drawing.
     */
    private static int fVertMargin = 10;
    /**
     * The minimum text scale margin.
     */
    private static int fTextScaleMargin = 20;
    /**
     * The length of the text scale.
     */
    private static int fScaleLength = 100;
    /**
     * The text to display
     */
    protected String fMessage;
    /**
     * The color array used to represent the 10 time range slices
     */
    protected Color[] fColors;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates a drawable tool tip instance.
     *
     * @param parent The parent composite.
     */
    public DrawableToolTip(Composite parent) {
        fParent = parent;
        fToolTipShell = new Shell(fParent.getShell(), SWT.ON_TOP);
        fToolTipShell.setLayout(new RowLayout());
        fToolTipShell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        fToolTipShell.addPaintListener(this);
        fToolTipShell.setSize(200, 50);

        fColors = new Color[10];
        fColors[0] = new Color(Display.getDefault(), 255, 229, 229);
        fColors[1] = new Color(Display.getDefault(), 255, 204, 204);
        fColors[2] = new Color(Display.getDefault(), 255, 178, 178);
        fColors[3] = new Color(Display.getDefault(), 255, 153, 153);
        fColors[4] = new Color(Display.getDefault(), 255, 127, 127);
        fColors[5] = new Color(Display.getDefault(), 255, 102, 102);
        fColors[6] = new Color(Display.getDefault(), 255, 76, 76);
        fColors[7] = new Color(Display.getDefault(), 255, 51, 51);
        fColors[8] = new Color(Display.getDefault(), 255, 25, 25);
        fColors[9] = new Color(Display.getDefault(), 255, 0, 0);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Returns the message to display.
     *
     * @return the message to display.
     */
    public String getText() {
        return fMessage;
    }

    /**
     * Returns teh current time to display.
     *
     * @return the current time to display
     */
    public String getAccessibleText() {
        return fCurrentValue.toString();
    }

    /**
     * Returns the horizontal margin.
     *
     * @return the horizontal margin.
     */
    protected static int getHorizontalMargin() {
        return fHorMargin;
    }

    /**
     * Sets the horizontal margin.
     *
     * @param margin The margin to set.
     */
    protected static void setHorizontalMargin(int margin) {
        fHorMargin = margin;
    }

    /**
     * Returns the vertical margin.
     *
     * @return the vertical margin.
     */
    protected static int getVerticalMargin() {
        return fVertMargin;
    }

    /**
     * Sets the vertical margin.
     *
     * @param margin The margin to set.
     */
    protected static void setVerticalMargin(int margin) {
        fVertMargin = margin;
    }

    /**
     * Returns the text scale margin.
     *
     * @return the text scale margin.
     */
    protected static int getTextScaleMargin() {
        return fTextScaleMargin;
    }

    /**
     * Sets the text scale margin.
     * @param textScaleMargin The margin to set.
     */
    protected static void setTestScaleMargin(int textScaleMargin) {
        fTextScaleMargin = textScaleMargin;
    }

    /**
     * Returns the scale length.
     *
     * @return the scale length.
     */
    protected static int getScaleLength() {
        return fScaleLength;
    }

    /**
     * Sets the scale length.
     *
     * @param scaleLength The scale length to set.
     */
    protected static void setScaleLength(int scaleLength) {
        fScaleLength = scaleLength;
    }

    /**
     * Display the tooltip using the given time range(min,max) the current value and the time unit The tooltip will stay
     * on screen until it is told otherwise
     *
     * @param value the current in the scale
     * @param min the scale min
     * @param max the scale max
     * @since 2.0
     */
    public void showToolTip(ITmfTimestamp value, ITmfTimestamp min, ITmfTimestamp max) {
        fMinMaxRange = new TmfTimeRange(min, max);
        fCurrentValue = value;

        int w = fToolTipShell.getBounds().width;
        int h = fToolTipShell.getBounds().height;
        Point hr = Display.getDefault().getCursorLocation();
        fToolTipShell.setBounds(hr.x, hr.y + 26, w, h);
        fToolTipShell.setVisible(true);
    }

    /**
     * Hide the tooltip.
     */
    public void hideToolTip() {
        fToolTipShell.setVisible(false);
    }

    /**
     * Disposes the system resource used by this kind of toolTips (a colors array essentially)
     */
    public void dispose() {
        for (int i = 0; i < fColors.length; i++) {
            fColors[i].dispose();
        }
    }

    @Override
    public void paintControl(PaintEvent event) {
        fMessage = SDMessages._138 + " " +  fCurrentValue.toString(); //$NON-NLS-1$
        Point size = event.gc.textExtent(fMessage);
        if (size.x < fScaleLength) {
            size.x = fScaleLength;
        }
        event.gc.drawText(fMessage, fHorMargin, fVertMargin, true);
        event.gc.drawLine(fHorMargin, fVertMargin + fTextScaleMargin + size.y, fHorMargin + fScaleLength, fVertMargin + fTextScaleMargin + size.y);

        int step = fScaleLength / 10;

        // double gr = (max - min) / 10;
        ITmfTimestamp minMaxdelta = fMinMaxRange.getEndTime().getDelta(fMinMaxRange.getStartTime());
        double gr = (minMaxdelta.getValue()) / (double) 10;

        // double delta = currentValue-min;
        ITmfTimestamp delta = fCurrentValue.getDelta(fMinMaxRange.getStartTime());
        long absDelta = Math.abs(delta.getValue());

        int colIndex = 0;
        if (gr != 0) {
            // colIndex = Math.round((float)(Math.log(1+delta)/gr));
            colIndex = Math.round((float) (absDelta / gr));
            if (colIndex > fColors.length) {
                colIndex = fColors.length;
            } else if (colIndex <= 1) {
                colIndex = 1;
            }
        } else {
            colIndex = 1;
        }

        for (int i = 0; i <= 10; i++) {
            if (i < 10) {
                event.gc.setBackground(fColors[i]);
            }
            if ((i < colIndex) && (i < 10)) {
                event.gc.fillRectangle(fHorMargin + i * step, fVertMargin + fTextScaleMargin + size.y - 5, step, 11);
            }
            if (i == 0) {
                event.gc.drawText(SDMessages._56, fHorMargin, size.y + 2 * fVertMargin + fTextScaleMargin, true);
            }
            if (i == 0) {
                int len = event.gc.textExtent(SDMessages._55).x;
                event.gc.drawText(SDMessages._55, fHorMargin + fScaleLength - len + 1, size.y + 2 * fVertMargin + fTextScaleMargin, true);
            }
            int lineWidth = 10;
            if ((i == 0) || (i == 10)) {
                lineWidth = 14;
            }
            event.gc.drawLine(fHorMargin + i * step, fVertMargin + fTextScaleMargin + size.y - lineWidth / 2, fHorMargin + i * step, fVertMargin + fTextScaleMargin + size.y + lineWidth / 2);
        }
        fToolTipShell.setSize(size.x + 2 * fHorMargin + 2, 2 * size.y + 3 * fVertMargin + fTextScaleMargin);
    }
}
