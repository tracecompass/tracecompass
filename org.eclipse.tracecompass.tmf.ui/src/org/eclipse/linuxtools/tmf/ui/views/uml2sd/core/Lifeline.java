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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * Lifeline is the UML2 lifeline graphical representation.<br>
 * Each lifeline owns a set of event occurrences. An event occurrence is the base element in UML2 to set an event in a
 * sequence diagram.<br>
 * Event occurrence define the drawing order of graph node along a lifeline. In this lifeline implementation, event
 * occurrences are just integer index. The event occurrences with the same value on different lifelines will correspond
 * the same y coordinate value.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class Lifeline extends GraphNode {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The life line tag.
     */
    public static final String LIFELINE_TAG = "Lifeline"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attribute
    // ------------------------------------------------------------------------
    /**
     * The lifeline position in the containing frame
     */
    private int fIndexInFrame = 0;
    /**
     * The frame where the lifeline is drawn
     */
    private Frame fFrame = null;
    /**
     * The current event occurrence created in the lifeline
     */
    private int fEventOccurrence = 0;
    /**
     * The lifeline category.
     */
    private int fCategory = -1;
    /**
     * Flag whether lifeline has time information available or not
     */
    private boolean fHasTimeInfo = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public Lifeline() {
        setColorPrefId(ISDPreferences.PREF_LIFELINE);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public int getX() {
        return Metrics.FRAME_H_MARGIN + Metrics.LIFELINE_H_MAGIN + (fIndexInFrame - 1) * Metrics.swimmingLaneWidth();
    }

    @Override
    public int getY() {
        return 2 * Metrics.FRAME_NAME_H_MARGIN + Metrics.LIFELINE_VT_MAGIN / 2 + Metrics.getFrameFontHeigth() + Metrics.getLifelineHeaderFontHeigth() + Metrics.FRAME_V_MARGIN + 2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN;
    }

    @Override
    public int getWidth() {
        return Metrics.getLifelineWidth();
    }

    @Override
    public int getHeight() {
        // Set room for two text lines
        return Metrics.getLifelineFontHeigth()/** 2 */
        + 2 * Metrics.LIFELINE_NAME_H_MARGIN;
    }

    /**
     * Set the lifeline category for this lifeline.
     *
     * @param arrayIndex the index of the category to use
     * @see Frame#setLifelineCategories(LifelineCategories[])
     */
    public void setCategory(int arrayIndex) {
        fCategory = arrayIndex;
    }

    /**
     * Gets the lifeline category for this lifeline.
     *
     * @return arrayIndex the index of the category to use
     * @since 2.0
     */
    public int getCategory() {
        return fCategory;
    }

    /**
     * Returns the tooltip text for the lifeline. It is the combination between the category name(if any) and the
     * lifeline name
     *
     * @return the tooltip text
     */
    public String getToolTipText() {
        if (fCategory >= 0) {
            LifelineCategories[] categories = fFrame.getLifelineCategories();
            if (fCategory < categories.length) {
                return categories[fCategory].getName() + " " + getName(); //$NON-NLS-1$
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the index of the first visible Execution Occurrence in the execution occurrence array.<br>
     * Execution Occurrences are Y ordered in this array
     *
     * @return the first visible Execution Occurrence
     */
    public int getExecOccurrenceDrawIndex() {
        if (!hasChildren()) {
            return 0;
        }
        if (getIndexes().get(BasicExecutionOccurrence.EXEC_OCC_TAG) != null) {
            return getIndexes().get(BasicExecutionOccurrence.EXEC_OCC_TAG).intValue();
        }
        return 0;
    }

    /**
     * Set the frame on which this lifeline must be drawn
     *
     * @param parentFrame
     *            Parent frame
     */
    protected void setFrame(Frame parentFrame) {
        fFrame = parentFrame;
        if (fHasTimeInfo) {
            fFrame.setHasTimeInfo(true);
        }
        if (fFrame.getMaxEventOccurrence() < getEventOccurrence() + 1) {
            fFrame.setMaxEventOccurrence(getEventOccurrence() + 1);
        }
    }

    /**
     * Returns the frame which this lifeline is drawn
     *
     * @return the Frame
     */
    protected Frame getFrame() {
        return fFrame;
    }

    /**
     * Set the lifeline position index in the containing frame
     *
     * @param index the lifeline X position
     */
    protected void setIndex(int index) {
        fIndexInFrame = index;
    }

    /**
     * Returns the lifeline position in de the containing frame
     *
     * @return the X position
     */
    public int getIndex() {
        return fIndexInFrame;
    }

    /**
     * Set the lifeline event occurrence to the value given in parameter This only change the current event occurrence,
     * greater event created on this lifeline are still valid and usable. This also need to inform the frame of the
     * operation mostly to store in the frame the greater event found in the diagram (used to determine the frame
     * height)
     *
     * @param eventOcc the new current event occurrence
     */
    public void setCurrentEventOccurrence(int eventOcc) {
        if ((fFrame != null) && (fFrame.getMaxEventOccurrence() < eventOcc)) {
            fFrame.setMaxEventOccurrence(eventOcc);
        }
        fEventOccurrence = eventOcc;
    }

    /**
     * Returns the last created event occurrence along the lifeline.
     *
     * @return the current event occurrence
     */
    public int getEventOccurrence() {
        return fEventOccurrence;
    }

    /**
     * Creates a new event occurrence along the lifeline.
     *
     * @return the new created event occurrence
     */
    public int getNewEventOccurrence() {
        setCurrentEventOccurrence(fEventOccurrence + 1);
        return fEventOccurrence;
    }

    /**
     * Adds the execution occurrence given in parameter to the lifeline.<br>
     * A Execution occurrence is never drawn in the frame instead it is added to a lifeline
     *
     * @param exec the execution occurrence to add
     */
    public void addExecution(BasicExecutionOccurrence exec) {
        exec.setLifeline(this);
        addNode(exec);
        if ((fFrame != null) && (fFrame.getMaxEventOccurrence() < exec.getEndOccurrence())) {
            fFrame.setMaxEventOccurrence(exec.getEndOccurrence());
        }
    }

    /**
     * Set whether lifeline has time information available or not.
     * @param value The value to set
     */
    protected void setTimeInfo(boolean value) {
        fHasTimeInfo = value;
        if ((fFrame != null) && value) {
            fFrame.setHasTimeInfo(value);
        }
    }

    /**
     * Returns true if at least one execution occurrence has time info.
     *
     * @return true if at least one execution occurrence has time info
     */
    public boolean hasTimeInfo() {
        return fHasTimeInfo;
    }

    /**
     * Returns the list of execution occurrence on this lifeline.
     *
     * @return the execution occurrence list
     */
    public List<GraphNode> getExecutions() {
        if (hasChildren()) {
            return getNodeMap().get(BasicExecutionOccurrence.EXEC_OCC_TAG);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean contains(int xValue, int yValue) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        if (fFrame == null) {
            return false;
        }
        if (GraphNode.contains(x, y, width, height, xValue, yValue)) {
            return true;
        }
        if (GraphNode.contains(x + Metrics.getLifelineWidth() / 2 - Metrics.EXECUTION_OCCURRENCE_WIDTH / 2, y + height, Metrics.EXECUTION_OCCURRENCE_WIDTH, (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * fFrame.getMaxEventOccurrence()
                + Metrics.LIFELINE_VB_MAGIN - 4, xValue, yValue)) {
            return true;
        }

        height = Metrics.getLifelineFontHeigth() + 2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN;
        int hMargin = (Metrics.LIFELINE_VT_MAGIN - height) / 2;

        if (hMargin >= 2) {
            if (fFrame.getVisibleAreaY() < y - height - hMargin) {
                if (GraphNode.contains(x - Metrics.LIFELINE_SPACING / 2 + 1, y - height - hMargin, Metrics.swimmingLaneWidth() - 2, height + 1, xValue, yValue)) {
                    return true;
                }
            } else {
                if (GraphNode.contains(x - Metrics.LIFELINE_SPACING / 2 + 1, fFrame.getVisibleAreaY(), Metrics.swimmingLaneWidth() - 2, height, xValue, yValue)) {
                    return true;
                }
            }
        }
        if (getNodeAt(xValue, yValue) != null) {
            return true;
        }
        return false;
    }

    /**
     * Returns the lifeline visibility for the given visible area
     *
     * @param vx The x coordinate of the visible area
     * @param vy The y coordinate of the visible area
     * @param vwidth The width of the visible area
     * @param vheight The height of the visible area
     * @return true if visible false otherwise
     */
    @Override
    public boolean isVisible(int vx, int vy, int vwidth, int vheight) {
        int x = getX();
        int width = getWidth();
        if (((x >= vx) && (x <= vx + vwidth)) || ((x + width >= vx) && (x <= vx))) {
            return true;
        }
        return false;
    }

    /**
     * Draws the name within the graphical context.
     *
     * @param context The graphical context.
     */
    protected void drawName(IGC context) {
        ISDPreferences pref = SDViewPref.getInstance();

        int x = getX();
        int y = getY();
        int height = Metrics.getLifelineHeaderFontHeigth() + 2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN;
        int hMargin = Metrics.LIFELINE_VT_MAGIN / 4;// (Metrics.LIFELINE_NAME_H_MARGIN)/2;

        context.setLineStyle(context.getLineSolidStyle());
        context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_LIFELINE_HEADER));
        context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_LIFELINE_HEADER));
        context.setFont(pref.getFont(ISDPreferences.PREF_LIFELINE_HEADER));
        if (hMargin >= 0) {
            if (fFrame.getVisibleAreaY() < y - height - hMargin) {
                context.fillRectangle(x - Metrics.LIFELINE_SPACING / 2 + 1, y - height - hMargin, Metrics.swimmingLaneWidth() - 2, height);
                context.drawRectangle(x - Metrics.LIFELINE_SPACING / 2 + 1, y - height - hMargin, Metrics.swimmingLaneWidth() - 2, height);
                context.setForeground(pref.getFontColor(ISDPreferences.PREF_LIFELINE_HEADER));
                context.drawTextTruncatedCentred(getName(), x + Metrics.LIFELINE_NAME_V_MARGIN - Metrics.LIFELINE_SPACING / 2 + 1, y - height - hMargin, Metrics.swimmingLaneWidth() - 2 * Metrics.LIFELINE_NAME_V_MARGIN - 2, height, true);
            } else {
                context.fillRectangle(x - Metrics.LIFELINE_SPACING / 2 + 1, fFrame.getVisibleAreaY(), Metrics.swimmingLaneWidth() - 2, height);
                context.drawRectangle(x - Metrics.LIFELINE_SPACING / 2 + 1, fFrame.getVisibleAreaY(), Metrics.swimmingLaneWidth() - 2, height);
                context.setForeground(pref.getFontColor(ISDPreferences.PREF_LIFELINE_HEADER));
                context.drawTextTruncatedCentred(getName(), x - Metrics.LIFELINE_SPACING / 2 + Metrics.LIFELINE_NAME_V_MARGIN + 1, fFrame.getVisibleAreaY(), Metrics.swimmingLaneWidth() - 2 * Metrics.LIFELINE_NAME_V_MARGIN - 2, height, true);
            }
        }
    }

    /**
     * Force the lifeline to be drawn at the given coordinate
     *
     * @param context - the context to draw into
     * @param x - the x coordinate
     * @param y - the y coordinate
     */
    public void draw(IGC context, int x, int y) {

        ISDPreferences pref = SDViewPref.getInstance();

        // Set the draw color depending if the lifeline must be selected or not
        context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
        if (isSelected()) {
            if (pref.useGradienColor()) {
                context.setGradientColor(pref.getBackGroundColor(ISDPreferences.PREF_LIFELINE));
            }
            context.setBackground(pref.getBackGroundColorSelection());
            context.setForeground(pref.getForeGroundColorSelection());
        } else {
            if (pref.useGradienColor()) {
                context.setGradientColor(pref.getBackGroundColor(ISDPreferences.PREF_LIFELINE));
                context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_FRAME));
            } else {
                context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_LIFELINE));
            }
            context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_LIFELINE));
        }
        // Store the lifeline coordinates to save some calls
        int width = getWidth();
        int height = getHeight();

        // Draw the rectangle which contain the lifeline name
        if (pref.useGradienColor()) {
            context.fillGradientRectangle(x, y, width, height / 2 - 7, true);
            context.fillRectangle(x, y + height / 2 - 8, width, +height / 2 - 5);
            context.fillGradientRectangle(x, y + height, width, -height / 2 + 6, true);
        } else {
            context.fillRectangle(x, y, width, height);
        }
        context.drawRectangle(x, y, width, height);

        if (fCategory >= 0) {
            LifelineCategories[] categories = fFrame.getLifelineCategories();
            if (fCategory < categories.length) {
                IImage image = categories[fCategory].getImage();
                if (image != null) {
                    context.drawImage(image, x, y, width, height);
                }
            }
        }

        // Draw the lifeline label into the rectangle
        // The label is truncated if it cannot fit
        IColor temp = context.getForeground();
        context.setFont(pref.getFont(ISDPreferences.PREF_LIFELINE));
        context.setForeground(pref.getFontColor(ISDPreferences.PREF_LIFELINE));
        context.drawTextTruncatedCentred(getName(), x + Metrics.LIFELINE_NAME_V_MARGIN, y, Metrics.getLifelineWidth() - 2 * Metrics.LIFELINE_NAME_V_MARGIN, height, true);

        context.setLineStyle(context.getLineDashStyle());
        context.setForeground(temp);
        int oldStyle = context.getLineStyle();

        // Now draw the lifeline vertical line
        // this line height depends on a stop assignment
        // if there is no stop the line is drawn to the bottom of the frame

        // by default set the height to reach the frame bottom
        int dashedLineEnd = y + height + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * fFrame.getMaxEventOccurrence() + Metrics.LIFELINE_VB_MAGIN;
        /*
         * if (stop != null) { dashedLineEnd = stop.getY(); }
         */

        if (isSelected()) {
            context.setForeground(pref.getBackGroundColorSelection());
            context.setLineWidth(5);
            context.drawLine(x + Metrics.getLifelineWidth() / 2, y + height, x + Metrics.getLifelineWidth() / 2, dashedLineEnd - 4);
            context.setForeground(pref.getForeGroundColorSelection());
        }

        context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
        context.drawLine(x + Metrics.getLifelineWidth() / 2, y + height, x + Metrics.getLifelineWidth() / 2, dashedLineEnd - 4);
        context.drawLine(x + Metrics.getLifelineWidth() / 2, y + height, x + Metrics.getLifelineWidth() / 2, dashedLineEnd - 4);
        context.setLineStyle(oldStyle);

        context.setLineStyle(context.getLineSolidStyle());

        if (hasFocus()) {
            drawFocus(context);
        }

        super.drawChildenNodes(context);
    }

    /**
     * Draws the select execution occurrence region using the given color
     *
     * @param context the graphical context
     * @param startEvent the region start
     * @param nbEvent the region height
     * @param color the color to use
     */
    public void highlightExecOccurrenceRegion(IGC context, int startEvent, int nbEvent, IColor color) {
        IColor backupColor = context.getBackground();
        context.setBackground(color);
        int x = getX() + Metrics.getLifelineWidth() / 2 - Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
        int y = getY() + getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * startEvent;
        int width = Metrics.EXECUTION_OCCURRENCE_WIDTH;
        int height = ((Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing())) * nbEvent;
        context.fillRectangle(x, y, width, height);
        context.setBackground(backupColor);
    }

    @Override
    public void draw(IGC context) {
        draw(context, getX(), getY());
    }

    @Override
    public String getArrayId() {
        return LIFELINE_TAG;
    }

    @Override
    public boolean positiveDistanceToPoint(int x, int y) {
        if (getX() > x - Metrics.swimmingLaneWidth()) {
            return true;
        }
        return false;
    }

    @Override
    public GraphNode getNodeAt(int x, int y) {
        int vy = 0;
        int vh = 0;
        if (getFrame() != null) {
            vy = getFrame().getVisibleAreaY();
            vh = getFrame().getVisibleAreaHeight();
        } else {
            return null;
        }
        if (getExecutions() == null) {
            return null;
        }
        for (int i = getExecOccurrenceDrawIndex(); i < getExecutions().size(); i++) {
            GraphNode node = getExecutions().get(i);
            if (node.getHeight() < 0) {
                if (node.getY() + node.getHeight() > vy + vh) {
                    break;
                }
            } else {
                if (node.getY() > vy + vh) {
                    break;
                }
            }
            if (node.contains(x, y)) {
                GraphNode internal = node.getNodeAt(x, y);
                if (internal != null) {
                    return internal;
                }
                return node;
            }
        }
        return null;
    }
}
