/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ExecutionOccurrence.java,v 1.2 2006/09/20 20:56:25 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.ISDPreferences;

/**
 * ExecutionOccurrence is the UML2 execution occurrence graphical representation. It is a BasicExecutionOccurrence on
 * which you can customize fill and/or
 * 
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline Lifeline for more event occurence details
 * @author sveyrier
 * 
 */
public class ExecutionOccurrence extends BasicExecutionOccurrence implements ITimeRange {

    protected int[] fillRGB;
    protected int[] strokeRGB;
    protected IImage image, ellipsesImage;

    protected ITmfTimestamp startTime, endTime;
    protected boolean hasTime;

    /**
     * Set the lifeline on which the execution occurrence appears.
     * 
     * @param theLifeline - the parent lifeline
     */
    @Override
    public void setLifeline(Lifeline theLifeline) {
        super.setLifeline(theLifeline);
        if (lifeline != null && hasTime) {
            lifeline.hasTime = true;
            if (lifeline.getFrame() != null) {
                lifeline.getFrame().setHasTimeInfo(true);
            }
        }
    }

    /**
     * Set the red, green and blue value of the optional color to be used for filling the execution occurrence
     * 
     * @param _r
     * @param _g
     * @param _b
     */
    public void setFillColor(int _r, int _g, int _b) {
        fillRGB = new int[3];
        fillRGB[0] = _r;
        fillRGB[1] = _g;
        fillRGB[2] = _b;
    }

    /**
     * Set the red, green and blue value of the optional color to be used for drawing the execution occurrence
     * 
     * @param _r
     * @param _g
     * @param _b
     */
    public void setStrokeColor(int _r, int _g, int _b) {
        strokeRGB = new int[3];
        strokeRGB[0] = _r;
        strokeRGB[1] = _g;
        strokeRGB[2] = _b;
    }

    public void setImage(IImage image_) {
        image = image_;
    }

    public void setTopEllipsesImage(IImage image_) {
        ellipsesImage = image_;
    }

    /**
     * Set the time when the execution occurrence starts.<br>
     * 
     * @param time the time when the execution occurrence starts
     */
    public void setStartTime(ITmfTimestamp time) {
        startTime = time.clone();
        hasTime = true;
        if (lifeline != null) {
            lifeline.setTimeInfo(true);
        }
    }

    /**
     * Set the time when the execution occurrence ends.<br>
     * 
     * @param time the time when the execution occurrence ends
     */
    public void setEndTime(ITmfTimestamp time) {
        endTime = time.clone();
        hasTime = true;
        if (lifeline != null) {
            lifeline.setTimeInfo(true);
        }
    }

    /**
     * Returns the time when the execution occurrence starts
     * 
     * @return the time
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return startTime;
    }

    /**
     * Returns the time when the execution occurrence ends
     * 
     * @return the time
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return endTime;
    }

    @Override
    public boolean hasTimeInfo() {
        return hasTime;
    }

    @Override
    public void draw(IGC context) {
        super.draw(context);
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        if (image != null) {
            context.drawImage(image, x + width - 4, y + height - 11, 8, 11);
        }
        if (ellipsesImage != null) {
            context.drawImage(ellipsesImage, x + width, y, 40, 10);
        }
    }

    /**
     * Extension in order to support customized fill colors
     * 
     * @param context
     * @return IColor
     */
    @Override
    protected IColor setUnselectedFillColor(IGC context) {
        if (fillRGB != null) {
            IColor tempFillColor = context.createColor(fillRGB[0], fillRGB[1], fillRGB[2]);
            if (Frame.getUserPref().useGradienColor()) {
                context.setGradientColor(tempFillColor);
                context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_EXEC));
                context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME));
            } else
                context.setBackground(tempFillColor);
            return tempFillColor;
        } else {
            return super.setUnselectedFillColor(context);
        }
    }

    /**
     * Extension in order to support customized stroke colors
     * 
     * @param context
     * @return IColor
     */
    @Override
    protected IColor setUnselectedStrokeColor(IGC context) {
        if (strokeRGB != null) {
            IColor tempStrokeColor = context.createColor(strokeRGB[0], strokeRGB[1], strokeRGB[2]);
            context.setForeground(tempStrokeColor);
            return tempStrokeColor;
        } else {
            return super.setUnselectedStrokeColor(context);
        }
    }
}
