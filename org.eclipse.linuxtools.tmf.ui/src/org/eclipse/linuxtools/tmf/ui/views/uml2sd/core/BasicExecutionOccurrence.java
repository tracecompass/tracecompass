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

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * BasicExecutionOccurrence is the UML2 execution occurrence graphical representation. It is attached to one Lifeline,
 * the event occurrence "duration" along the lifeline is defined by two event occurrences
 *
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline Lifeline for more event occurence details
 * @version 1.0
 * @author sveyrier
 *
 */
public class BasicExecutionOccurrence extends GraphNode {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The grahNode ID constant
     */
    public static final String EXEC_OCC_TAG = "Execution_Occ"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The corresponding lifeline.
     */
    protected Lifeline fLifeline = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructore
     */
    public BasicExecutionOccurrence() {
        fPrefId = ISDPreferences.PREF_EXEC;
    }

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    @Override
    public int getX() {
        if (fLifeline == null) {
            return 0;
        }
        return fLifeline.getX() + Metrics.getLifelineWidth() / 2 - Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
    }

    @Override
    public int getY() {
        if (fLifeline == null) {
            return 0;
        }
        return fLifeline.getY() + fLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * fStartEventOccurrence;
    }

    @Override
    public int getWidth() {
        if (fLifeline == null) {
            return 0;
        }
        return Metrics.EXECUTION_OCCURRENCE_WIDTH;
    }

    @Override
    public int getHeight() {
        if (fLifeline == null) {
            return 0;
        }
        return ((Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing())) * (fEndEventOccurrence - fStartEventOccurrence);
    }

    @Override
    public boolean contains(int xValue, int yValue) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        if (GraphNode.contains(x, y, width, height, xValue, yValue)) {
            return true;
        }

        if (getNodeAt(xValue, yValue) != null) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        if (super.getName() == null || super.getName().equals("")) { //$NON-NLS-1$
            return fLifeline.getToolTipText();
        }
        return super.getName();
    }

    /**
     * Set the lifeline on which the execution occurrence appears.
     *
     * @param theLifeline - the parent lifeline
     */
    public void setLifeline(Lifeline theLifeline) {
        fLifeline = theLifeline;
    }

    /**
     * Get the lifeline on which the execution occurrence appears.
     *
     * @return - the parent lifeline
     */
    public Lifeline getLifeline() {
        return fLifeline;
    }

    /**
     * Get the execution start event occurrence
     *
     * @return the start event occurrence to set
     */
    @Override
    public int getStartOccurrence() {
        return fStartEventOccurrence;
    }

    /**
     * Set the execution end event occurrence
     *
     * @return the end event occurrence to set
     */
    @Override
    public int getEndOccurrence() {
        return fEndEventOccurrence;
    }

    /**
     * Set the execution start event occurrence
     *
     * @param occurrence the start event occurrence to set
     */
    public void setStartOccurrence(int occurrence) {
        fStartEventOccurrence = occurrence;
    }

    /**
     * Set the execution end event occurrence
     *
     * @param occurrence the end event occurrence to set
     */
    public void setEndOccurrence(int occurrence) {
        fEndEventOccurrence = occurrence;
    }

    @Override
    public void draw(IGC context) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        IColor tempFillColor = null;
        IColor tempStrokeColor = null;

        ISDPreferences pref = SDViewPref.getInstance();

        // The execution occurrence is selected
        // if the owning lifeline is selected
        if (fLifeline.isSelected() || isSelected()) {
            context.setBackground(pref.getBackGroundColorSelection());
            context.setForeground(pref.getForeGroundColorSelection());
        } else {
            tempFillColor = setUnselectedFillColor(context);
        }
        if (pref.useGradienColor()) {
            context.fillGradientRectangle(x, y, width, height, false);
        } else {
            context.fillRectangle(x, y, width, height);
        }
        tempStrokeColor = setUnselectedStrokeColor(context);
        context.drawRectangle(x, y, width, height);
        if (tempFillColor != null) {
            tempFillColor.dispose();
        }
        if (tempStrokeColor != null) {
            tempStrokeColor.dispose();
        }
        if (hasFocus()) {
            drawFocus(context);
        }
        super.drawChildenNodes(context);
    }

    /**
     * Rewrite this method in your extension in order to support customized fill colors
     *
     * @param context Graphics context
     * @return IColor
     */
    protected IColor setUnselectedFillColor(IGC context) {

        ISDPreferences pref = SDViewPref.getInstance();

        if (pref.useGradienColor()) {
            context.setGradientColor(pref.getBackGroundColor(ISDPreferences.PREF_EXEC));
            context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_FRAME));
        } else {
            context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_EXEC));
        }
        return null;
    }

    /**
     * Rewrite this method in your extension in order to support customized stroke colors
     *
     * @param context Graphics context
     * @return IColor
     */
    protected IColor setUnselectedStrokeColor(IGC context) {
        context.setForeground(SDViewPref.getInstance().getForeGroundColor(ISDPreferences.PREF_EXEC));
        return null;
    }

    @Override
    public String getArrayId() {
        return EXEC_OCC_TAG;
    }

    @Override
    public boolean positiveDistanceToPoint(int x, int y) {
        if (getY() + getHeight() > y) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isVisible(int x, int y, int width, int height) {
        if ((getLifeline() != null) && (getLifeline().isVisible(x, y, width, height))) {
            int ly = getY();
            int lh = getHeight();
            if (ly >= y && ly < y + height) {
                return true;
            }
            if (ly + lh > y && ly + lh <= y + height) {
                return true;
            }
            if ((ly < y) && (ly + lh > y + height)) {
                return true;
            }
        }
        return false;
    }
}
