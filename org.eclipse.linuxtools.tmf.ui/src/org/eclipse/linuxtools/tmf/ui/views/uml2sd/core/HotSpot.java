/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: HotSpot.java,v 1.2 2006/09/20 20:56:27 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.ISDPreferences;

/**
 * @author sveyrier
 */
public class HotSpot extends GraphNode {

    protected BasicExecutionOccurrence execOcc = null;
    protected int occurrence = 0;
    protected IImage image = null;

    /**
     * The grahNode ID constant
     */
    public static final String GLYPH = "Glyph"; //$NON-NLS-1$

    public HotSpot() {
        prefId = ISDPreferences.PREF_EXEC;
    }

    public void setImage(IImage img) {
        image = img;
    }

    @Override
    public int getX() {
        if (execOcc != null)
            return execOcc.getX() - 3;
        else
            return 0;

    }

    @Override
    public int getY() {
        if (execOcc != null)
            return execOcc.getY();
        else
            return 0;
    }

    @Override
    public int getWidth() {
        if (execOcc != null)
            return execOcc.getWidth() + 7;
        else
            return 0;
    }

    @Override
    public int getHeight() {
        if (execOcc != null)
            return execOcc.getWidth() + 10;
        else
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

    public int getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(int occ) {
        occurrence = occ;
    }

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

    @Override
    public String getArrayId() {
        return GLYPH;
    }

    @Override
    public boolean isVisible(int x, int y, int width, int height) {
        return true;
    }

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
