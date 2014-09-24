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
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.Messages;
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
    // Constants
    // ------------------------------------------------------------------------
    private static final int HORIZONTAL_MARGIN = 10;
    private static final int VERTICAL_MARGIN = 10;
    private static final int TEXT_SCALE_MARGIN = 20;
    private static final int SCALE_LENGTH = 100;
    private static final int SHELL_WIDTH = 200;
    private static final int SHELL_HEIGHT = 50;
    private static final int NUMBER_STEPS = 10;
    private static final int BASE_RED_VALUE = 255;
    private static final int BASE_GREEN_BLUE_VALUE = 225;
    private static final int COLOR_STEP = 25;
    private static final int BOUNDS_Y_OFFSET = 26;
    private static final int RECTANGLE_HEIGHT = 11;
    private static final int DEFAULT_LINE_WIDTH = 10;
    private static final int BORDER_LINE_WIDTH = 14;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The tooltip shell
     */
    private Shell fToolTipShell = null;
    /**
     * The Time range data.
     */
    private TmfTimeRange fMinMaxRange;
    /**
     * The current time.
     */
    private ITmfTimestamp fCurrentValue;
    /**
     * The horizontal margin used for drawing.
     */
    private static int fHorMargin = HORIZONTAL_MARGIN;
    /**
     * The vertical margin used for drawing.
     */
    private static int fVertMargin = VERTICAL_MARGIN;
    /**
     * The minimum text scale margin.
     */
    private static int fTextScaleMargin = TEXT_SCALE_MARGIN;
    /**
     * The length of the text scale.
     */
    private static int fScaleLength = SCALE_LENGTH;
    /**
     * The text to display
     */
    private String fMessage;
    /**
     * The color array used to represent the 10 time range slices
     */
    private Color[] fColors;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Creates a drawable tool tip instance.
     *
     * @param parent The parent composite.
     */
    public DrawableToolTip(Composite parent) {
        fToolTipShell = new Shell(parent.getShell(), SWT.ON_TOP);
        fToolTipShell.setLayout(new RowLayout());
        fToolTipShell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        fToolTipShell.addPaintListener(this);
        fToolTipShell.setSize(SHELL_WIDTH, SHELL_HEIGHT);

        fColors = new Color[NUMBER_STEPS];
        int greenBlue = BASE_GREEN_BLUE_VALUE;
        final int step = COLOR_STEP;
        for (int i = 0; i < fColors.length; i++) {
            fColors[i] = new Color(Display.getDefault(), BASE_RED_VALUE, greenBlue, greenBlue);
            greenBlue -= step;
        }
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
        fToolTipShell.setBounds(hr.x, hr.y + BOUNDS_Y_OFFSET, w, h);
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
        fMessage = Messages.SequenceDiagram_Delta + " " +  fCurrentValue.toString(); //$NON-NLS-1$
        Point size = event.gc.textExtent(fMessage);
        if (size.x < fScaleLength) {
            size.x = fScaleLength;
        }
        event.gc.drawText(fMessage, fHorMargin, fVertMargin, true);
        event.gc.drawLine(fHorMargin, fVertMargin + fTextScaleMargin + size.y, fHorMargin + fScaleLength, fVertMargin + fTextScaleMargin + size.y);

        int step = fScaleLength / NUMBER_STEPS;

        ITmfTimestamp minMaxdelta = fMinMaxRange.getEndTime().getDelta(fMinMaxRange.getStartTime());
        double gr = (minMaxdelta.getValue()) / (double) NUMBER_STEPS;

        ITmfTimestamp delta = fCurrentValue.getDelta(fMinMaxRange.getStartTime());
        long absDelta = Math.abs(delta.getValue());

        int colIndex = 0;
        if (gr != 0) {
            colIndex = Math.round((float) (absDelta / gr));
            if (colIndex > fColors.length) {
                colIndex = fColors.length;
            } else if (colIndex <= 1) {
                colIndex = 1;
            }
        } else {
            colIndex = 1;
        }

        for (int i = 0; i <= NUMBER_STEPS; i++) {
            if (i < NUMBER_STEPS) {
                event.gc.setBackground(fColors[i]);
            }
            if ((i < colIndex) && (i < NUMBER_STEPS)) {
                event.gc.fillRectangle(fHorMargin + i * step, fVertMargin + fTextScaleMargin + size.y - 5, step, RECTANGLE_HEIGHT);
            }
            if (i == 0) {
                event.gc.drawText(Messages.SequenceDiagram_Min, fHorMargin, size.y + 2 * fVertMargin + fTextScaleMargin, true);
            }
            if (i == 0) {
                int len = event.gc.textExtent(Messages.SequenceDiagram_Max).x;
                event.gc.drawText(Messages.SequenceDiagram_Max, fHorMargin + fScaleLength - len + 1, size.y + 2 * fVertMargin + fTextScaleMargin, true);
            }
            int lineWidth = DEFAULT_LINE_WIDTH;
            if ((i == 0) || (i == NUMBER_STEPS)) {
                lineWidth = BORDER_LINE_WIDTH;
            }
            event.gc.drawLine(fHorMargin + i * step, fVertMargin + fTextScaleMargin + size.y - lineWidth / 2, fHorMargin + i * step, fVertMargin + fTextScaleMargin + size.y + lineWidth / 2);
        }
        fToolTipShell.setSize(size.x + 2 * fHorMargin + 2, 2 * size.y + 3 * fVertMargin + fTextScaleMargin);
    }
}
