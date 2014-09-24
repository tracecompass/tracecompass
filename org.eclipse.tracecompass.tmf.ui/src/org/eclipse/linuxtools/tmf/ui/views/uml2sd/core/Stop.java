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

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * <p>
 * It is the UML2 stop graphical representation in the sequence diagram viewer.
 * This draw a cross on the lifeline. The stop y coordinate depend on the event occurrence when it appears.
 * A stop is never drawn it is assigned to a lifeline.
 * </p>
 *
 * @version 1.0
 * @author sveyrier
 */
public class Stop extends GraphNode {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The graphNode ID
     */
    public static final String STOP = "STOP"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The owning lifeline on which the stop appears
     */
    private Lifeline fLifeline = null;
    /**
     * This basically represents the time when the stop occurs on the owning Lifeline
     *
     * @see Lifeline Lifeline for more event occurence details
     */
    private int fEventOccurrence = 0;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public int getX() {
        if (fLifeline == null) {
            return 0;
        }
        return fLifeline.getX() + Metrics.getLifelineWidth() / 2 - Metrics.STOP_WIDTH / 2;
    }

    @Override
    public int getY() {
        if (fLifeline == null) {
            return 0;
        }
        return fLifeline.getY() + fLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * fEventOccurrence - Metrics.STOP_WIDTH / 2;
    }

    @Override
    public int getWidth() {
        if (fLifeline == null) {
            return 0;
        }
        return Metrics.STOP_WIDTH;
    }

    @Override
    public int getHeight() {
        if (fLifeline == null) {
            return 0;
        }
        return Metrics.STOP_WIDTH;
    }

    /**
     * Set the lifeline on which the stop must be draw
     *
     * @param theLifeline The the stop owing lifeline
     */
    public void setLifeline(Lifeline theLifeline) {
        fLifeline = theLifeline;
    }

    /**
     * Get the lifeline on which the stop must be draw
     *
     * @return the the stop owing lifeline
     * @since 2.0
     */
    public Lifeline getLifeline() {
        return fLifeline;
    }

    /**
     * Get the event occurrence when this stop appears
     *
     * @return the eventOccurence to assign to the stop
     * @since 2.0
     */
    public int getEventOccurrence() {
        return fEventOccurrence;
    }

    /**
     * Set the event occurrence when this stop appears
     *
     * @param occurrence the eventOccurence to assign to the stop
     */
    public void setEventOccurrence(int occurrence) {
        fEventOccurrence = occurrence;
    }

    @Override
    public void draw(IGC context) {

        ISDPreferences pref = SDViewPref.getInstance();

        // Set the appropriate color depending if the graph node if selected or not
        if (fLifeline.isSelected()) {
            context.setForeground(pref.getBackGroundColorSelection());
            context.setLineWidth(Metrics.SELECTION_LINE_WIDTH);
            int lastWidth = context.getLineWidth();
            context.setLineWidth(9);
            // Draw a cross on the lifeline
            context.drawLine(getX(), getY(), getX() + getWidth(), getY() + getHeight());
            context.drawLine(getX() + getWidth(), getY(), getX(), getY() + getHeight());
            // restore the context
            context.setLineWidth(lastWidth);
            context.setBackground(pref.getBackGroundColorSelection());
            context.setForeground(pref.getForeGroundColorSelection());
        } else {
            context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_LIFELINE));
            context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_LIFELINE));
        }
        int lastWidth = context.getLineWidth();
        context.setLineWidth(3);
        // Draw a cross on the lifeline
        context.drawLine(getX(), getY(), getX() + getWidth(), getY() + getHeight());
        context.drawLine(getX() + getWidth(), getY(), getX(), getY() + getHeight());
        // restore the context
        context.setLineWidth(lastWidth);
    }

    @Override
    public String getArrayId() {
        return STOP;
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }
}
