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

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;

/**
 * Class to add a hot spot marker.
 * 
 * @version 1.0
 * @author sveyrier
 */
public class HotSpot extends GraphNode {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The grahNode ID constant
     */
    public static final String GLYPH = "Glyph"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The execution occurrence the hot spot marker is for.
     */
    protected BasicExecutionOccurrence execOcc = null;
    /**
     * The occurrence number.
     */
    protected int occurrence = 0;
    /**
     * The marker image to display.
     */
    protected IImage image = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    
    /**
     * Default constructor
     */
    public HotSpot() {
        prefId = ISDPreferences.PREF_EXEC;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Set the marker image.
     * 
     * @param img A image to set
     */
    public void setImage(IImage img) {
        image = img;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getX()
     */
    @Override
    public int getX() {
        if (execOcc != null) {
            return execOcc.getX() - 3;
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getY()
     */
    @Override
    public int getY() {
        if (execOcc != null){
            return execOcc.getY();
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getWidth()
     */
    @Override
    public int getWidth() {
        if (execOcc != null) {
            return execOcc.getWidth() + 7;
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getHeight()
     */
    @Override
    public int getHeight() {
        if (execOcc != null) {
            return execOcc.getWidth() + 10;
        }
        return 0;
    }

    /**
     * Set the lifeline on which the execution occurrence appears.
     * 
     * @param occ the parent lifeline
     */
    public void setExecution(BasicExecutionOccurrence occ) {
        execOcc = occ;
        execOcc.addNode(this);
    }

    /**
     * Get the lifeline on which the execution occurrence appears.
     * 
     * @return - the parent lifeline
     */
    public BasicExecutionOccurrence getExecOcc() {
        return execOcc;
    }

    /**
     * Returns the occurrence number. 
     * 
     * @return the occurrence number.
     */
    public int getOccurrence() {
        return occurrence;
    }

    /**
     * Set the occurrence number.
     * 
     * @param occ A number to set.
     */
    public void setOccurrence(int occ) {
        occurrence = occ;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#draw(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC)
     */
    @Override
    public void draw(IGC context) {

        // The execution occurrence is selected
        // if the owning lifeline is selected
        if (isSelected() || (execOcc != null && execOcc.isSelected()) || (execOcc != null && execOcc.getLifeline() != null && execOcc.getLifeline().isSelected())) {
            context.setBackground(Frame.getUserPref().getBackGroundColorSelection());
            context.setForeground(Frame.getUserPref().getForeGroundColorSelection());
        } else {
            context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_EXEC));
            context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_EXEC));
        }
        context.drawImage(image, getX(), getY(), getWidth(), getHeight());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getArrayId()
     */
    @Override
    public String getArrayId() {
        return GLYPH;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#isVisible(int, int, int, int)
     */
    @Override
    public boolean isVisible(int x, int y, int width, int height) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#contains(int, int)
     */
    @Override
    public boolean contains(int _x, int _y) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        if (Frame.contains(x, y, width, height, _x, _y)) {
            return true;
        }
        return false;
    }
}
