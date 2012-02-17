/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: DrawableToolTip.java,v 1.3 2008/01/24 02:29:01 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
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
 * This class is used to reproduce the same tooltip behavior on Windows and Linux when the mouse move hover the time
 * compression bar used to display elapsed time using a tooltip. The tooltip is composed of 2 parts, the text value and
 * below, a scale to visually locate the value in a time range (usually the min an max elapsed time in the whole
 * diagram)
 * 
 * @author sveyrier
 */
public class DrawableToolTip implements PaintListener {

    /**
     * The parent control where the tooltip must be drawn
     */
    protected Composite parent = null;
    /**
     * The tooltip shell
     */
    protected Shell toolTipShell = null;
    /**
     * Time range data
     */
    protected TmfTimeRange minMaxRange;
    protected ITmfTimestamp currentValue;

    private static int H_MARGIN = 10;
    private static int V_MARGIN = 10;

    private static int TEXT_SCALE_MARGIN = 20;
    private static int SCALE_LENGTH = 100;

    protected String msg;

    /**
     * The color array used to represent the 10 time range slices
     */
    protected Color[] col;

    public DrawableToolTip(Composite _parent) {
        parent = _parent;
        toolTipShell = new Shell(parent.getShell(), SWT.ON_TOP);
        toolTipShell.setLayout(new RowLayout());
        toolTipShell.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        toolTipShell.addPaintListener(this);
        toolTipShell.setSize(200, 50);

        col = new Color[10];
        col[0] = new Color(Display.getDefault(), 255, 229, 229);
        col[1] = new Color(Display.getDefault(), 255, 204, 204);
        col[2] = new Color(Display.getDefault(), 255, 178, 178);
        col[3] = new Color(Display.getDefault(), 255, 153, 153);
        col[4] = new Color(Display.getDefault(), 255, 127, 127);
        col[5] = new Color(Display.getDefault(), 255, 102, 102);
        col[6] = new Color(Display.getDefault(), 255, 76, 76);
        col[7] = new Color(Display.getDefault(), 255, 51, 51);
        col[8] = new Color(Display.getDefault(), 255, 25, 25);
        col[9] = new Color(Display.getDefault(), 255, 0, 0);
    }

    /**
     * Display the tooltip using the given time range(min,max) the current value and the time unit The tooltip will stay
     * on screen until it is told otherwise
     * 
     * @param _value the current in the scale
     * @param _min the scale min
     * @param _max the scale max
     * @param unit the scale unit
     */
    public void showToolTip(ITmfTimestamp _value, ITmfTimestamp min, ITmfTimestamp max) {
        minMaxRange = new TmfTimeRange(min.clone(), max.clone());
        currentValue = _value.clone();

        int w = toolTipShell.getBounds().width;
        int h = toolTipShell.getBounds().height;
        Point hr = Display.getDefault().getCursorLocation();
        toolTipShell.setBounds(hr.x, hr.y + 26, w, h);
        toolTipShell.setVisible(true);
    }

    /**
     * Hide the tooltip
     */
    public void hideToolTip() {
        toolTipShell.setVisible(false);
    }

    /**
     * Draw the tooltip text on the control widget when a paint event is received
     */
    @Override
    public void paintControl(PaintEvent event) {
        msg = SDMessages._138 + " " +  currentValue.toString(); //$NON-NLS-1$
        Point size = event.gc.textExtent(msg);
        if (size.x < SCALE_LENGTH)
            size.x = SCALE_LENGTH;
        event.gc.drawText(msg, H_MARGIN, V_MARGIN, true);
        event.gc.drawLine(H_MARGIN, V_MARGIN + TEXT_SCALE_MARGIN + size.y, H_MARGIN + SCALE_LENGTH, V_MARGIN + TEXT_SCALE_MARGIN + size.y);

        int step = SCALE_LENGTH / 10;

        // double gr = (max - min) / 10;
        ITmfTimestamp minMaxdelta = (TmfTimestamp) minMaxRange.getEndTime().getDelta(minMaxRange.getStartTime());
        double gr = (minMaxdelta.getValue()) / (double) 10;

        // double delta = currentValue-min;
        ITmfTimestamp delta = (TmfTimestamp) currentValue.getDelta(minMaxRange.getStartTime());
        long absDelta = Math.abs(delta.getValue());
        
        int colIndex = 0;
        if (gr != 0) {
            // colIndex = Math.round((float)(Math.log(1+delta)/gr));
            colIndex = Math.round((float) (absDelta / gr));
            if (colIndex > col.length)
                colIndex = col.length;
            else if (colIndex <= 1)
                colIndex = 1;
        } else
            colIndex = 1;

        for (int i = 0; i <= 10; i++) {
            if (i < 10)
                event.gc.setBackground(col[i]);
            if ((i < colIndex) && (i < 10))
                event.gc.fillRectangle(H_MARGIN + i * step, V_MARGIN + TEXT_SCALE_MARGIN + size.y - 5, step, 11);
            if (i == 0)
                event.gc.drawText(SDMessages._56, H_MARGIN, size.y + 2 * V_MARGIN + TEXT_SCALE_MARGIN, true);
            if (i == 0) {
                int len = event.gc.textExtent(SDMessages._55).x;
                event.gc.drawText(SDMessages._55, H_MARGIN + SCALE_LENGTH - len + 1, size.y + 2 * V_MARGIN + TEXT_SCALE_MARGIN, true);
            }
            int lineWidth = 10;
            if ((i == 0) || (i == 10))
                lineWidth = 14;
            event.gc.drawLine(H_MARGIN + i * step, V_MARGIN + TEXT_SCALE_MARGIN + size.y - lineWidth / 2, H_MARGIN + i * step, V_MARGIN + TEXT_SCALE_MARGIN + size.y + lineWidth / 2);
        }
        toolTipShell.setSize(size.x + 2 * H_MARGIN + 2, 2 * size.y + 3 * V_MARGIN + TEXT_SCALE_MARGIN);
    }

    public String getText() {
        return msg;
    }

    public String getAccessibleText() {
        return currentValue.toString();
    }

    /**
     * Dispose the system resource used by this kind of toolTips (a colors array essentially)
     * 
     */
    public void dispose() {
        for (int i = 0; i < col.length; i++)
            col[i].dispose();
    }
    
    protected static int getHorizontalMargin() {
        return H_MARGIN;
    }

    protected static void setHorizontalMargin(int margin) {
        H_MARGIN = margin;
    }

    protected static int getVerticalMargin() {
        return V_MARGIN;
    }

    protected static void setVerticalMargin(int margin) {
        V_MARGIN = margin;
    }

    protected static int getTestScaleMargin() {
        return TEXT_SCALE_MARGIN;
    }

    protected static void setTestScaleMargin(int testScaleMargin) {
        TEXT_SCALE_MARGIN = testScaleMargin;
    }

    protected static int getScaleLength() {
        return SCALE_LENGTH;
    }

    protected static void setScaleLength(int scaleLength) {
        SCALE_LENGTH = scaleLength;
    }

}
