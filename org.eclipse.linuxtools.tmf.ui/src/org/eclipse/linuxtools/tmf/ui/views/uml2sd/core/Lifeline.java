/**********************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.ISDPreferences;

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
    protected int indexInFrame = 0;
    /**
     * The frame where the lifeline is drawn
     */
    protected Frame frame = null;
    /**
     * The current event occurrence created in the lifeline
     */
    protected int eventOccurrence = 0;
    /**
     * The lifeline category.
     */
    protected int category = -1;
    /**
     * Flag whether lifeline has time information available or not
     */
    protected boolean hasTime = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public Lifeline() {
        prefId = ISDPreferences.PREF_LIFELINE;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getX()
     */
    @Override
    public int getX() {
        return Metrics.FRAME_H_MARGIN + Metrics.LIFELINE_H_MAGIN + (indexInFrame - 1) * Metrics.swimmingLaneWidth();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getY()
     */
    @Override
    public int getY() {
        return 2 * Metrics.FRAME_NAME_H_MARGIN + Metrics.LIFELINE_VT_MAGIN / 2 + Metrics.getFrameFontHeigth() + Metrics.getLifelineHeaderFontHeigth() + Metrics.FRAME_V_MARGIN + 2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getWidth()
     */
    @Override
    public int getWidth() {
        return Metrics.getLifelineWidth();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getHeight()
     */
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
        category = arrayIndex;
    }

    /**
     * Returns the tooltip text for the lifeline. It is the combination between the category name(if any) and the
     * lifeline name
     * 
     * @return the tooltip text
     */
    public String getToolTipText() {
        if (category >= 0) {
            LifelineCategories[] categories = frame.getLifelineCategories();
            if (category < categories.length) {
                return categories[category].getName() + " " + getName(); //$NON-NLS-1$
            } else {
                return ""; //$NON-NLS-1$
            }
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Returns the index of the first visible Execution Occurrence in the execution occurrence array.<br>
     * Execution Occurrences are Y ordered in this array
     * 
     * @return the first visible Execution Occurrence
     */
    public int getExecOccurrenceDrawIndex() {
        if (!hasChilden) {
            return 0;
        }
        if (indexes.get(BasicExecutionOccurrence.EXEC_OCC_TAG) != null) {
            return ((Integer) indexes.get(BasicExecutionOccurrence.EXEC_OCC_TAG)).intValue();
        }
        return 0;
    }

    /**
     * Set the frame on which this lifeline must be drawn
     * 
     * @param parentFrame
     */
    protected void setFrame(Frame parentFrame) {
        frame = parentFrame;
        if (hasTime) {
            frame.setHasTimeInfo(true);
        }
        if (frame.getMaxEventOccurrence() < getEventOccurrence() + 1) {
            frame.setMaxEventOccurrence(getEventOccurrence() + 1);
        }
    }

    /**
     * Returns the frame which this lifeline is drawn
     * 
     * @return the Frame
     */
    protected Frame getFrame() {
        return frame;
    }

    /**
     * Set the lifeline position index in the containing frame
     * 
     * @param index the lifeline X position
     */
    protected void setIndex(int index) {
        indexInFrame = index;
    }

    /**
     * Returns the lifeline position in de the containing frame
     * 
     * @return the X position
     */
    public int getIndex() {
        return indexInFrame;
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
        if ((frame != null) && (frame.getMaxEventOccurrence() < eventOcc)) {
            frame.setMaxEventOccurrence(eventOcc);
        }
        eventOccurrence = eventOcc;
    }

    /**
     * Returns the last created event occurrence along the lifeline.
     * 
     * @return the current event occurrence
     */
    public int getEventOccurrence() {
        return eventOccurrence;
    }

    /**
     * Creates a new event occurrence along the lifeline.
     * 
     * @return the new created event occurrence
     */
    public int getNewEventOccurrence() {
        setCurrentEventOccurrence(eventOccurrence + 1);
        return eventOccurrence;
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
        if ((frame != null) && (frame.getMaxEventOccurrence() < exec.endEventOccurrence)) {
            frame.setMaxEventOccurrence(exec.endEventOccurrence);
        }
    }

    /**
     * Set whether lifeline has time information available or not.
     * @param value The value to set
     */
    protected void setTimeInfo(boolean value) {
        hasTime = value;
        if ((frame != null) && (value == true)) {
            frame.setHasTimeInfo(value);
        }
    }

    /**
     * Returns true if at least one execution occurrence has time info.
     * 
     * @return true if at least one execution occurrence has time info
     */
    public boolean hasTimeInfo() {
        return hasTime;
    }

    /**
     * Returns the list of execution occurrence on this lifeline.
     * 
     * @return the execution occurrence list
     */
    public List<GraphNode> getExecutions() {
        if (hasChilden) {
            return (List<GraphNode>) nodes.get(BasicExecutionOccurrence.EXEC_OCC_TAG);
        }
        return new ArrayList<GraphNode>();
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

        if (frame == null) {
            return false;
        }
        if (Frame.contains(x, y, width, height, _x, _y)) {
            return true;
        }
        if (Frame.contains(x + Metrics.getLifelineWidth() / 2 - Metrics.EXECUTION_OCCURRENCE_WIDTH / 2, y + height, Metrics.EXECUTION_OCCURRENCE_WIDTH, (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * frame.getMaxEventOccurrence()
                + Metrics.LIFELINE_VB_MAGIN - 4, _x, _y)) {
            return true;
        }

        height = Metrics.getLifelineFontHeigth() + 2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN;
        int hMargin = (Metrics.LIFELINE_VT_MAGIN - height) / 2;

        if (hMargin >= 2) {
            if (frame.getVisibleAreaY() < y - height - hMargin) {
                if (Frame.contains(x - Metrics.LIFELINE_SPACING / 2 + 1, y - height - hMargin, Metrics.swimmingLaneWidth() - 2, height + 1, _x, _y)) {
                    return true;
                }
            } else {
                if (Frame.contains(x - Metrics.LIFELINE_SPACING / 2 + 1, frame.getVisibleAreaY(), Metrics.swimmingLaneWidth() - 2, height, _x, _y)) {
                    return true;
                }
            }
        }
        if (getNodeAt(_x, _y) != null) {
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
        int x = getX();
        int y = getY();
        int height = Metrics.getLifelineHeaderFontHeigth() + 2 * Metrics.LIFELINE_HEARDER_TEXT_V_MARGIN;
        int hMargin = Metrics.LIFELINE_VT_MAGIN / 4;// (Metrics.LIFELINE_NAME_H_MARGIN)/2;

        context.setLineStyle(context.getLineSolidStyle());
        context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_LIFELINE_HEADER));
        context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_LIFELINE_HEADER));
        context.setFont(Frame.getUserPref().getFont(ISDPreferences.PREF_LIFELINE_HEADER));
        if (hMargin >= 0) {
            if (frame.getVisibleAreaY() < y - height - hMargin) {
                context.fillRectangle(x - Metrics.LIFELINE_SPACING / 2 + 1, y - height - hMargin, Metrics.swimmingLaneWidth() - 2, height);
                context.drawRectangle(x - Metrics.LIFELINE_SPACING / 2 + 1, y - height - hMargin, Metrics.swimmingLaneWidth() - 2, height);
                context.setForeground(Frame.getUserPref().getFontColor(ISDPreferences.PREF_LIFELINE_HEADER));
                context.drawTextTruncatedCentred(getName(), x + Metrics.LIFELINE_NAME_V_MARGIN - Metrics.LIFELINE_SPACING / 2 + 1, y - height - hMargin, Metrics.swimmingLaneWidth() - 2 * Metrics.LIFELINE_NAME_V_MARGIN - 2, height, true);
            } else {
                context.fillRectangle(x - Metrics.LIFELINE_SPACING / 2 + 1, frame.getVisibleAreaY(), Metrics.swimmingLaneWidth() - 2, height);
                context.drawRectangle(x - Metrics.LIFELINE_SPACING / 2 + 1, frame.getVisibleAreaY(), Metrics.swimmingLaneWidth() - 2, height);
                context.setForeground(Frame.getUserPref().getFontColor(ISDPreferences.PREF_LIFELINE_HEADER));
                context.drawTextTruncatedCentred(getName(), x - Metrics.LIFELINE_SPACING / 2 + Metrics.LIFELINE_NAME_V_MARGIN + 1, frame.getVisibleAreaY(), Metrics.swimmingLaneWidth() - 2 * Metrics.LIFELINE_NAME_V_MARGIN - 2, height, true);
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
        // Set the draw color depending if the lifeline must be selected or not
        context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
        if (isSelected()) {
            if (Frame.getUserPref().useGradienColor()) {
                context.setGradientColor(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_LIFELINE));
            }
            context.setBackground(Frame.getUserPref().getBackGroundColorSelection());
            context.setForeground(Frame.getUserPref().getForeGroundColorSelection());
        } else {
            if (Frame.getUserPref().useGradienColor()) {
                context.setGradientColor(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_LIFELINE));
                context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME));
            } else {
                context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_LIFELINE));
            }
            context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_LIFELINE));
        }
        // Store the lifeline coordinates to save some calls
        int width = getWidth();
        int height = getHeight();

        // Draw the rectangle which contain the lifeline name
        if (Frame.getUserPref().useGradienColor()) {
            context.fillGradientRectangle(x, y, width, height / 2 - 7, true);
            context.fillRectangle(x, y + height / 2 - 8, width, +height / 2 - 5);
            context.fillGradientRectangle(x, y + height, width, -height / 2 + 6, true);
        } else {
            context.fillRectangle(x, y, width, height);
        }
        context.drawRectangle(x, y, width, height);

        if (category >= 0) {
            LifelineCategories[] categories = frame.getLifelineCategories();
            if (category < categories.length) {
                IImage image = categories[category].getImage();
                if (image != null) {
                    context.drawImage(image, x, y, width, height);
                }
            }
        }

        // Draw the lifeline label into the rectangle
        // The label is truncated if it cannot fit
        IColor temp = context.getForeground();
        context.setFont(Frame.getUserPref().getFont(ISDPreferences.PREF_LIFELINE));
        context.setForeground(Frame.getUserPref().getFontColor(ISDPreferences.PREF_LIFELINE));
        context.drawTextTruncatedCentred(getName(), x + Metrics.LIFELINE_NAME_V_MARGIN, y, Metrics.getLifelineWidth() - 2 * Metrics.LIFELINE_NAME_V_MARGIN, height, true);

        context.setLineStyle(context.getLineDashStyle());
        context.setForeground(temp);
        int oldStyle = context.getLineStyle();

        // Now draw the lifeline vertical line
        // this line height depends on a stop assignment
        // if there is no stop the line is drawn to the bottom of the frame

        // by default set the height to reach the frame bottom
        int dashedLineEnd = y + height + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * frame.getMaxEventOccurrence() + Metrics.LIFELINE_VB_MAGIN;
        /*
         * if (stop != null) { dashedLineEnd = stop.getY(); }
         */

        if (isSelected()) {
            context.setForeground(Frame.getUserPref().getBackGroundColorSelection());
            context.setLineWidth(5);
            context.drawLine(x + Metrics.getLifelineWidth() / 2, y + height, x + Metrics.getLifelineWidth() / 2, dashedLineEnd - 4);
            context.setForeground(Frame.getUserPref().getForeGroundColorSelection());
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

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#draw(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC)
     */
    @Override
    public void draw(IGC context) {
        draw(context, getX(), getY());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getArrayId()
     */
    @Override
    public String getArrayId() {
        return LIFELINE_TAG;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#positiveDistanceToPoint(int, int)
     */
    @Override
    public boolean positiveDistanceToPoint(int x, int y) {
        if (getX() > x - Metrics.swimmingLaneWidth())
            return true;
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getNodeAt(int, int)
     */
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
            GraphNode node = (GraphNode) getExecutions().get(i);
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
