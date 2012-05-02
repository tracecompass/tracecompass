/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;

/**
 * ExecutionOccurrence is the UML2 execution occurrence graphical representation. It is a BasicExecutionOccurrence on
 * which you can customize fill and/or.
 * 
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline Lifeline for more event occurence details
 * @version 1.0 
 * @author sveyrier
 * 
 */
public class ExecutionOccurrence extends BasicExecutionOccurrence implements ITimeRange {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Set the red, green and blue value of the optional color to be used for filling the execution occurrence. 
     */
    protected int[] fillRGB;
    /**
     * Set the red, green and blue value of the optional color to be used for drawing the execution occurrence
     */
    protected int[] strokeRGB;
    /**
     * The occurrence image.
     */
    protected IImage image;
    /**
     * The top ellipses image.
     */
    protected IImage ellipsesImage;
    /**
     *  The start time stamp. 
     */
    protected ITmfTimestamp startTime;
    /**
     * The end time stamp;
     */
    protected ITmfTimestamp endTime;
    /**
     * Flag to indicate whether time information is available or not. 
     */
    protected boolean hasTime;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BasicExecutionOccurrence#setLifeline(org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline)
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
     * Set the red, green and blue value of the optional color to be used for filling the execution occurrence.
     * 
     * @param _r A value for red.
     * @param _g A green value for green.
     * @param _b A value blue.
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
     * @param _r A value for red.
     * @param _g A green value for green.
     * @param _b A value blue.
     */
    public void setStrokeColor(int _r, int _g, int _b) {
        strokeRGB = new int[3];
        strokeRGB[0] = _r;
        strokeRGB[1] = _g;
        strokeRGB[2] = _b;
    }

    /**
     * Set the corresponding image.
     * 
     * @param image_ A image to set.
     */
    public void setImage(IImage image_) {
        image = image_;
    }

    /**
     * Set the top ellipses image.
     * 
     * @param image_ A image to set.
     */
    public void setTopEllipsesImage(IImage image_) {
        ellipsesImage = image_;
    }

    /**
     * Set the time when the execution occurrence starts.
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
     * Set the time when the execution occurrence ends.
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange#getStartTime()
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return startTime;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange#getEndTime()
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return endTime;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.ITimeRange#hasTimeInfo()
     */
    @Override
    public boolean hasTimeInfo() {
        return hasTime;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BasicExecutionOccurrence#draw(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC)
     */
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BasicExecutionOccurrence#setUnselectedFillColor(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC)
     */
    @Override
    protected IColor setUnselectedFillColor(IGC context) {
        if (fillRGB != null) {
            IColor tempFillColor = context.createColor(fillRGB[0], fillRGB[1], fillRGB[2]);
            if (Frame.getUserPref().useGradienColor()) {
                context.setGradientColor(tempFillColor);
                context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_EXEC));
                context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME));
            } else {
                context.setBackground(tempFillColor);
            }
            return tempFillColor;
        } else {
            return super.setUnselectedFillColor(context);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BasicExecutionOccurrence#setUnselectedStrokeColor(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC)
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
