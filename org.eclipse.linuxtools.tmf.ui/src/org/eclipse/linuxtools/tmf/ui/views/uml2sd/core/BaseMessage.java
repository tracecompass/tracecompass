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
 * The base UML2 syncMessages implementation.<br>
 * This abstract class only define one event occurrence to attach to the message.<br>
 * Usually a message has two event occurrences attached, one for both ends. But some syncMessages(like synchronous
 * syncMessages) only need one event occurrence to represent the time when they appear. Others kind of message
 * representations (like asynchronous syncMessages) will be responsible to define the missing second eventOccurrence
 * property.<br>
 * <br>
 *
 * @see Lifeline Lifeline for more event occurence details
 * @version 1.0
 * @author sveyrier
 */
public abstract class BaseMessage extends GraphNode {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The lifeline which send the message
     */
    private Lifeline fStartLifeline = null;
    /**
     * The lifeline which receive the message
     */
    private Lifeline fEndLifeline = null;
    /**
     * The visiblitiy flag.
     */
    private boolean fVisible = true;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public int getX() {
        // returns the exact x coordinate
        return getX(false);
    }

    @Override
    public int getY() {
        /*
         * Note: lifeline.getY() return the y coordinate of the top left corner of the rectangle which contain the
         * lifeline name getHeight return the height of this rectangle The message y coordinate is then relative to this
         * position depending of its eventOccurrence Space between syncMessages is constant
         */
        if ((fStartLifeline != null) && (fEndLifeline != null)) {
            /*
             * Regular message, both ends are attached to a lifeline
             */
            return fEndLifeline.getY() + fEndLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * getEndOccurrence();

        }
        /*
         * UML2 lost message kind
         */
        if (fStartLifeline != null) {
            return fStartLifeline.getY() + fStartLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * getEndOccurrence();
        }

        /*
         * UML2 found message kind
         */
        if (fEndLifeline != null) {
            return fEndLifeline.getY() + fEndLifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * getEndOccurrence();
        }
        // return 0 by default
        return 0;
    }

    @Override
    public int getWidth() {
        // Returns the exact width
        return getWidth(false);
    }

    @Override
    public int getHeight() {
        return 0;
    }

    /**
     * Returns the graph node x coordinate.<br>
     * Depending on the quick parameter a approximative or exact value is return.<br>
     * The approximative value does not take into account if both message ends are connected to a Lifeline Execution
     * Occurrence.<br>
     * Execution occurrence on a lifeline increase the vertical line width which represent the lifeline, this directly
     * affect the message x coordinate and width.<br>
     * <br>
     * This method is typically used to faster execute none graphical operation like tooltip lookup.<br>
     * <br>
     *
     * @param quick true to get an approximative value<br>
     *            false to get the exact x value<br>
     * @return the graph node x coordinate
     */
    protected int getX(boolean quick) {
        int x = 0;
        int activationWidth = Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
        if ((fStartLifeline != null) && (fEndLifeline != null)) {
            x = fStartLifeline.getX() + Metrics.getLifelineWidth() / 2;
        } else {
            if (fStartLifeline != null) {
                x = fStartLifeline.getX() + Metrics.getLifelineWidth() / 2;
            }

            if (fEndLifeline != null) {
                x = fEndLifeline.getX() - Metrics.LIFELINE_SPACING / 2;
            }
        }

        if (quick) {
            return x;
        }

        if ((fStartLifeline != null) && (fEndLifeline != null) && (fStartLifeline.getX() > fEndLifeline.getX())) {
            activationWidth = -activationWidth;
        }

        if (isMessageStartInActivation(getEndOccurrence())) {
            x = x + activationWidth;
        }

        return x;
    }

    /**
     * Returns the graph node width.<br>
     * Depending on the quick parameter a approximative or exact value is returned.<br>
     * The approximative value does not take into account if both message ends are connected to a Lifeline Execution
     * Occurrence.<br>
     * Execution occurrence on a lifeline increase the vertical line width which represent the lifeline, this directly
     * affect the message x coordinate and width.<br>
     * <br>
     * This method is typically used to faster execute none graphical operation like tooltip lookup.<br>
     * <br>
     *
     * @param quick true to get an approximative value<br>
     *            false to get the exact x value
     * @return the graph node width
     */
    protected int getWidth(boolean quick) {
        int width = 0;
        int activationWidth = Metrics.EXECUTION_OCCURRENCE_WIDTH / 2;
        if ((fStartLifeline != null) && (fEndLifeline != null)) {
            if (fStartLifeline == fEndLifeline) {
                width = Metrics.INTERNAL_MESSAGE_WIDTH + Metrics.EXECUTION_OCCURRENCE_WIDTH;
            } else {
                width = fEndLifeline.getX() + Metrics.getLifelineWidth() / 2 - getX(true);
            }
        } else {
            if (fStartLifeline != null) {
                width = Metrics.swimmingLaneWidth() / 2;
            }
            if (fEndLifeline != null) {
                width = Metrics.swimmingLaneWidth() / 2;
            }
        }

        if (quick) {
            return width;
        }

        if ((fStartLifeline != null) && (fEndLifeline != null) && (fStartLifeline.getX() > fEndLifeline.getX())) {
            activationWidth = -activationWidth;
        }

        if (isMessageStartInActivation(getEndOccurrence())) {
            width = width - activationWidth;
        }

        if (isMessageEndInActivation(getEndOccurrence())) {
            width = width - activationWidth;
        }

        return width;
    }

    @Override
    public boolean isVisible(int x, int y, int width, int height) {
        // ***Common*** syncMessages visibility
        // draw the message only if at least one end is visible
        if (fEndLifeline != null && (fEndLifeline.isVisible(x, y, width, height)) || (fStartLifeline != null && fStartLifeline.isVisible(x, y, width, height))) {
            return true;
        }
        // In this case it can be a message which cross the whole visible area
        else if (fEndLifeline != null && (!fEndLifeline.isVisible(x, y, width, height)) && (fStartLifeline != null && !fStartLifeline.isVisible(x, y, width, height))) {
            if (fEndLifeline.getX() > x + width && fStartLifeline.getX() < x) {
                return true;
            } else if (fStartLifeline.getX() > x + width && fEndLifeline.getX() < x) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the visibility value.
     *
     * @param value The visibility to set.
     */
    public void setVisible(boolean value) {
        fVisible = value;
    }

    /**
     * @return the visibility value.
     */
    public boolean isVisible() {
        return fVisible;
    }

    /**
     * Set the lifeline from which this message has been sent.
     *
     * @param lifeline - the message sender
     */
    public void setStartLifeline(Lifeline lifeline) {
        fStartLifeline = lifeline;
    }

    /**
     * Returns the lifeline from which this message has been sent.
     *
     * @return the message sender
     */
    public Lifeline getStartLifeline() {
        return fStartLifeline;
    }

    /**
     * Returns the lifeline which has received this message.
     *
     * @return the message receiver
     */
    public Lifeline getEndLifeline() {
        return fEndLifeline;
    }

    /**
     * Set the lifeline which has receive this message.
     *
     * @param lifeline the message receiver
     */
    public void setEndLifeline(Lifeline lifeline) {
        fEndLifeline = lifeline;
    }

    /**
     * Set the event occurrence when this message occurs.<br>
     *
     * @param occurrence the event occurrence to assign to this message.<br>
     * @see Lifeline Lifeline for more event occurence details
     */
    protected void setEventOccurrence(int occurrence) {
        setEndOccurrence(occurrence);
    }

    /**
     * Returns the event occurence when is message occurs.<br>
     *
     * @return the event occurrence assigned to this message.<br>
     * @see Lifeline Lifeline for more event occurence details
     */
    public int getEventOccurrence() {
        return getEndOccurrence();
    }

    /**
     * Determines if the given eventOccurence occurs on a executionOccurence owned by the sending lifeline.<br>
     * WARNING: this method will return a valid result only for execution occurrences which are visible in the View.<br>
     * As consequence this method is only used for drawing purpose, especially to determine the exact message x
     * coordinate and width.<br>
     *
     * @see BaseMessage#getX(boolean)
     * @param event the event occurrence to test
     * @return true if occurs on a execution occurrence owned by the sending lifeine, false otherwise
     */
    protected boolean isMessageStartInActivation(int event) {
        boolean inActivation = false;
        if ((fStartLifeline != null) && (fStartLifeline.getExecutions() != null)) {
            // int acIndex=startLifeline.getExecOccurrenceDrawIndex();
            // acIndex = first visible execution occurrence
            // for drawing speed reason with only search on the visivle subset
            int thisY = getY();
            for (int i = 0; i < fStartLifeline.getExecutions().size(); i++) {
                BasicExecutionOccurrence toDraw = (BasicExecutionOccurrence) fStartLifeline.getExecutions().get(i);
                if ((event >= toDraw.getStartOccurrence()) && (event <= toDraw.getEndOccurrence())) {
                    inActivation = true;
                }
                // if we are outside the visible area we stop right now
                // This works because execution occurrences are ordered along the Y axis
                if (toDraw.getY() > thisY) {
                    break;
                }
            }
        }
        return inActivation;
    }

    /**
     * Determines if the given event occurrence occurs on a execution occurrence owned by the receiving lifeline.<br>
     * WARNING: this method will return a valid result only for execution occurrences which are visible in the View.<br>
     * As consequence this method is only used for drawing purpose, especially to determine the exact message x
     * coordinate and width.<br>
     *
     * @see BaseMessage#getX(boolean)
     * @param event the event occurrence to test
     * @return true if occurs on a execution occurrence owned by the receiving lifeline, false otherwise
     */
    protected boolean isMessageEndInActivation(int event) {
        boolean inActivation = false;
        if ((fEndLifeline != null) && (fEndLifeline.getExecutions() != null)) {
            // acIndex = first visible execution occurrence
            // for drawing speed reason with only search on the visivle subset
            for (int i = 0; i < fEndLifeline.getExecutions().size(); i++) {
                BasicExecutionOccurrence toDraw = (BasicExecutionOccurrence) fEndLifeline.getExecutions().get(i);
                if ((event >= toDraw.getStartOccurrence()) && (event <= toDraw.getEndOccurrence())) {
                    inActivation = true;
                }
                // if we are outside the visible area we stop right now
                // This works because execution occurrences are ordered along the Y axis
                if (toDraw.getY() > getY()) {
                    break;
                }
            }
        }
        return inActivation;
    }

    @Override
    public boolean contains(int xValue, int yValue) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        // Used to create a rectangle which contains the message label to allow selection when clicking the label
        int tempHeight = Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth();

        // Is it a self message?
        if (fStartLifeline == fEndLifeline) {
            /*
             * Rectangle.contains(x,y, width, height) does not works with negative height or width We check here if the
             * rectangle width is negative.
             */
            if (getName().length() * Metrics.getAverageCharWidth() > Metrics.swimmingLaneWidth() - Metrics.EXECUTION_OCCURRENCE_WIDTH / 2 + -Metrics.INTERNAL_MESSAGE_WIDTH) {
                if (GraphNode.contains(x + Metrics.INTERNAL_MESSAGE_WIDTH + 10, y, Metrics.swimmingLaneWidth() - Metrics.EXECUTION_OCCURRENCE_WIDTH / 2 + -Metrics.INTERNAL_MESSAGE_WIDTH, Metrics.getMessageFontHeigth(), xValue, yValue)) {
                    return true;
                }
            } else {
                if (GraphNode.contains(x + Metrics.INTERNAL_MESSAGE_WIDTH + 10, y, getName().length() * Metrics.getAverageCharWidth(), Metrics.getMessageFontHeigth(), xValue, yValue)) {
                    return true;
                }
            }

            // Test if the point is in part 1 of the self message
            // see: "private void drawMessage (NGC context)" method for self message drawing schema
            if (GraphNode.contains(x, y - Metrics.MESSAGE_SELECTION_TOLERANCE / 2, Metrics.INTERNAL_MESSAGE_WIDTH / 2, Metrics.MESSAGE_SELECTION_TOLERANCE, xValue, yValue)) {
                return true;
            }

            // Test if the point is in part 3 of the self message
            if (GraphNode.contains(x + Metrics.INTERNAL_MESSAGE_WIDTH - Metrics.MESSAGE_SELECTION_TOLERANCE / 2, y, Metrics.MESSAGE_SELECTION_TOLERANCE, height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT, xValue, yValue)) {
                return true;
            }

            // Test if the point is in part 5 of the self message
            if (GraphNode.contains(x, y + height - Metrics.MESSAGE_SELECTION_TOLERANCE / 2 + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT, Metrics.INTERNAL_MESSAGE_WIDTH / 2, Metrics.MESSAGE_SELECTION_TOLERANCE, xValue, yValue)) {
                return true;
            }

            // false otherwise
            return false;
        }
        if (GraphNode.contains(x, y - tempHeight, width, tempHeight, xValue, yValue)) {
            return true;
        }
        // false otherwise
        return false;
    }

    /**
     * Method to draw the message using the graphical context.
     *
     * @param context A graphical context to draw in.
     */
    protected void drawMessage(IGC context) {
        int fX = 0;
        int fY = 0;
        int fW = 0;
        int fH = 0;

        // temporary store the coordinates to avoid more methods calls
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        ISDPreferences pref = SDViewPref.getInstance();

        // UML2 found message (always drawn from left to right)
        // or UML2 lost message (always drawn from left to right)
        if ((fStartLifeline == null || fEndLifeline == null) && fStartLifeline != fEndLifeline) {
            // Draw the message label above the message and centered
            // The label is truncated if it cannot fit between the two message end
            // 2*Metrics.MESSAGES_NAME_SPACING = space above the label + space below the label
            IColor temp = context.getForeground();
            context.setForeground(pref.getFontColor(getColorPrefId()));
            context.drawTextTruncatedCentred(getName(), x, y - Metrics.getMessageFontHeigth() - 2 * Metrics.MESSAGES_NAME_SPACING, width, 2 * Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth(), !isSelected());
            context.setForeground(temp);
            int margin = 0;
            if (fEndLifeline == null) {
                margin = Metrics.MESSAGE_CIRCLE_RAY;
            }

            // Draw the message main line
            context.drawLine(x, y, x + width, y + height);
            // Draw the two little lines which make a arrow part of the message
            Double xt = Double.valueOf(Math.cos(0.75) * 7);
            Double yt = Double.valueOf(Math.sin(0.75) * 7);
            if (context.getLineStyle() == context.getLineSolidStyle()) {
                IColor backcolor = context.getBackground();
                context.setBackground(context.getForeground());
                int[] points = { x + width - margin, y + height, x + width - xt.intValue() - margin, y + height - yt.intValue(), x + width - xt.intValue() - margin, y + height + yt.intValue(), x + width - margin, y + height };
                context.fillPolygon(points);
                context.drawPolygon(points);
                context.setBackground(backcolor);
            } else {
                int currentStyle = context.getLineStyle();
                int currentWidth = context.getLineWidth();
                context.setLineWidth(currentWidth + 2);
                context.setLineStyle(context.getLineSolidStyle());
                context.drawLine(x + width - xt.intValue() - margin, y + height - yt.intValue(), x + width - margin, y + height);
                context.drawLine(x + width - xt.intValue() - margin, y + height + yt.intValue(), x + width - margin, y + height);
                context.setLineStyle(currentStyle);
                context.setLineWidth(currentWidth);
            }
            IColor storedColor = context.getBackground();
            context.setBackground(context.getForeground());

            // Draw a circle at the message end (endLifeline side)
            int ray = Metrics.MESSAGE_CIRCLE_RAY;
            if (context.getLineWidth() != Metrics.NORMAL_LINE_WIDTH) {
                ray = ray + Metrics.SELECTION_LINE_WIDTH - Metrics.NORMAL_LINE_WIDTH;
            }
            if (fStartLifeline == null) {
                context.fillOval(x - ray, y - ray, ray * 2, ray * 2);
            } else {
                context.fillOval(x + width - ray, y + height - ray, ray * 2, ray * 2);
            }
            context.setBackground(storedColor);
            context.setForeground(pref.getFontColor(getColorPrefId()));
            fX = x;
            fY = y - yt.intValue();
            fW = width;
            fH = height + 2 * yt.intValue();
        }
        // it is self message (always drawn at the left side of the owning lifeLifeline)
        else if (fStartLifeline != null && fEndLifeline != null && fStartLifeline == fEndLifeline) {
            /*
             * Self syncMessages are drawn in 5 parts 1 -----------+ + 2 + | | | 3 | + 5 + 4 -----------+
             */
            int tempy = Metrics.INTERNAL_MESSAGE_WIDTH / 2;
            if (Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT <= Metrics.INTERNAL_MESSAGE_WIDTH) {
                tempy = Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT / 2;
            }

            // Part 1
            context.drawLine(x, y, x + Metrics.INTERNAL_MESSAGE_WIDTH / 2, y);
            // Part 3
            context.drawLine(x + Metrics.INTERNAL_MESSAGE_WIDTH, y + tempy, x + Metrics.INTERNAL_MESSAGE_WIDTH, y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT - tempy);
            // Part 5
            context.drawLine(x, y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT, x + Metrics.INTERNAL_MESSAGE_WIDTH / 2, y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT);

            Double xt = Double.valueOf(Math.cos(0.75) * 7);
            Double yt = Double.valueOf(Math.sin(0.75) * 7);

            fX = x;
            fY = y;
            fW = Metrics.INTERNAL_MESSAGE_WIDTH;
            fH = height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT;

            // Draw the two little lines which make a arrow part of the message
            if (context.getLineStyle() == context.getLineSolidStyle()) {
                IColor backcolor = context.getBackground();
                context.setBackground(context.getForeground());
                int[] points = { x, y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT, x + xt.intValue(), y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT + yt.intValue(), x + xt.intValue(),
                        y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT - yt.intValue(), x, y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT };
                context.fillPolygon(points);
                context.drawPolygon(points);
                context.setBackground(backcolor);
            } else {
                int currentStyle = context.getLineStyle();
                int currentWidth = context.getLineWidth();
                context.setLineWidth(currentWidth + 2);
                context.setLineStyle(context.getLineSolidStyle());
                context.drawLine(x + xt.intValue(), y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT + yt.intValue(), x, y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT);
                context.drawLine(x + xt.intValue(), y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT - yt.intValue(), x, y + height + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT);
                context.setLineStyle(currentStyle);
                context.setLineWidth(currentWidth);
            }

            // Part 2
            context.drawArc(x, y, Metrics.INTERNAL_MESSAGE_WIDTH, 2 * tempy, 0, 90);
            // Part 4
            context.drawArc(x, y + Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT, Metrics.INTERNAL_MESSAGE_WIDTH, -2 * tempy, 0, -90);

            // Draw the message label above the message and centered
            // The label is truncated if it cannot fit between the two message end
            // 2*Metrics.MESSAGES_NAME_SPACING = space above the label + space below the label

            // the space available for the text is sorter if are drawing internal message on the last lifeline
            context.setForeground(pref.getFontColor(getColorPrefId()));
            if (fStartLifeline.getIndex() == fStartLifeline.getFrame().getHorizontalIndex()) {
                context.drawTextTruncated(getName(), x + width + Metrics.INTERNAL_MESSAGE_V_MARGIN / 2, y, Metrics.swimmingLaneWidth() / 2 - Metrics.EXECUTION_OCCURRENCE_WIDTH + -Metrics.INTERNAL_MESSAGE_WIDTH, +Metrics.MESSAGES_NAME_SPACING
                        - Metrics.getMessageFontHeigth(), !isSelected());
            } else {
                context.drawTextTruncated(getName(), x + width + Metrics.INTERNAL_MESSAGE_V_MARGIN / 2, y, Metrics.swimmingLaneWidth() - Metrics.EXECUTION_OCCURRENCE_WIDTH + -Metrics.INTERNAL_MESSAGE_WIDTH,
                        +Metrics.MESSAGES_NAME_SPACING - Metrics.getMessageFontHeigth(), !isSelected());
            }
        }
        // it is regular message
        else if (fStartLifeline != null && fEndLifeline != null) {
            // Draw the message main line
            context.drawLine(x, y, x + width, y + height);

            int spaceBTWStartEnd = fEndLifeline.getX() - fStartLifeline.getX();

            double a = height;
            double b = width;
            double angle = Math.atan(a / b);
            // Compute the coordinates of the two little lines which make the arrow part of the message
            int sign = 1;
            if (spaceBTWStartEnd < 0) {
                sign = -1;
            }
            Double x1 = Double.valueOf(sign * Math.cos(angle - 0.75) * 7);
            Double y1 = Double.valueOf(sign * Math.sin(angle - 0.75) * 7);
            Double x2 = Double.valueOf(sign * Math.cos(angle + 0.75) * 7);
            Double y2 = Double.valueOf(sign * Math.sin(angle + 0.75) * 7);

            fX = getX();
            fY = y + height - y2.intValue();
            fW = getWidth();
            fH = y2.intValue() - y1.intValue() + 1;
            if (fW < 0) {
                fW = -fW;
                fX = fX - fW;
            }

            if (fH < 0) {
                fH = -fH;
                fY = fY - fH;
            }

            // Draw the two little lines which make a arrow part of the message
            if (context.getLineStyle() == context.getLineSolidStyle()) {
                IColor backcolor = context.getBackground();
                context.setBackground(context.getForeground());
                int[] points = { x + width - x1.intValue(), y + height - y1.intValue(), x + width, y + height, x + width - x2.intValue(), y + height - y2.intValue(), x + width - x1.intValue(), y + height - y1.intValue() };
                context.fillPolygon(points);
                context.drawPolygon(points);
                context.setBackground(backcolor);
            } else {
                int currentStyle = context.getLineStyle();
                int currentWidth = context.getLineWidth();
                context.setLineWidth(currentWidth + 2);
                context.setLineStyle(context.getLineSolidStyle());
                context.drawLine(x + width - x1.intValue(), y + height - y1.intValue(), x + width, y + height);
                context.drawLine(x + width - x2.intValue(), y + height - y2.intValue(), x + width, y + height);
                context.setLineStyle(currentStyle);
                context.setLineWidth(currentWidth);
            }

            // Draw the message label above the message and centered
            // The label is truncated if it cannot fit between the two message end
            // 2*Metrics.MESSAGES_NAME_SPACING = space above the label + space below the label
            context.setForeground(pref.getFontColor(getColorPrefId()));
            if (spaceBTWStartEnd > 0) {
                context.drawTextTruncatedCentred(getName(), x, y + height / 2 - (2 * Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth()), width, 2 * Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth(), !isSelected());
            } else {
                context.drawTextTruncatedCentred(getName(), x + width, y + height / 2 - (2 * Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth()), -width, 2 * Metrics.MESSAGES_NAME_SPACING + +Metrics.getMessageFontHeigth(), !isSelected());
            }
        }
    }

    @Override
    public void draw(IGC context) {
        if (!isVisible()) {
            return;
        }

        // Draw it selected?*/
        if (isSelected()) {
            ISDPreferences pref = SDViewPref.getInstance();
            /*
             * Draw it twice First time, bigger inverting selection colors Second time, regular drawing using selection
             * colors This create the highlight effect
             */
            context.setForeground(pref.getBackGroundColorSelection());
            context.setLineWidth(Metrics.SELECTION_LINE_WIDTH);
            drawMessage(context);
            context.setBackground(pref.getBackGroundColorSelection());
            context.setForeground(pref.getForeGroundColorSelection());
            // Second drawing is done after
        }
        context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
        if (hasFocus()) {
            context.setDrawTextWithFocusStyle(true);
        }
        drawMessage(context);
        int oldStyle = context.getLineStyle();
        if (hasFocus()) {
            context.setDrawTextWithFocusStyle(false);
            drawFocus(context);
        }
        // restore the context
        context.setLineStyle(oldStyle);
    }

    /**
     * Determine if two messages are identical. This default implementation considers that overlapping messages with
     * same coordinates are identical.
     *
     * @param message - the message to compare with
     * @return true if identical false otherwise
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#isSameAs(org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode)
     */
    @Override
    public boolean isSameAs(GraphNode message) {
        if (message == null) {
            return false;
        }
        if (!(message instanceof BaseMessage)) {
            return super.isSameAs(message);
        }
        return ((getX() == message.getX()) && (getY() == message.getY()) && (getWidth() == message.getWidth()) && (getHeight() == message.getHeight()));
    }

    /**
     * Method drawRot.
     *
     * @param x A x coordinate
     * @param y A y coordinate
     * @param w A width
     * @param h A height
     * @param context A graphical context
     */
    public void drawRot(int x, int y, int w, int h, IGC context) {
        double angleA = Math.atan2(getHeight(), getWidth());
        double cosA = Math.cos(angleA);
        double sinA = Math.sin(angleA);

        int gx = getX();
        int gy = getY();

        int localHeight = h;
        localHeight = localHeight / 2;

        double cw = Math.sqrt(w * w + getHeight() * getHeight());

        int x1 = Math.round((float) ((x - gx) * cosA - (y - gy) * sinA));
        int y1 = Math.round((float) ((x - gx) * sinA + (y - gy) * cosA));

        int x2 = Math.round((float) (cw * cosA - (y - gy) * sinA));
        int y2 = Math.round((float) (cw * sinA + (y - gy) * cosA));

        int x3 = Math.round((float) (cw * cosA - (localHeight) * sinA));
        int y3 = Math.round((float) (cw * sinA + (localHeight) * cosA));

        int x4 = Math.round((float) ((x - gx) * cosA - (localHeight) * sinA));
        int y4 = Math.round((float) ((x - gx) * sinA + (localHeight) * cosA));

        int[] points = { x1 + getX(), y1 + getY(), x2 + getX(), y2 + getY(), x3 + getX(), y3 + getY(), x4 + getX(), y4 + getY() };
        context.drawPolygon(points);
    }

    @Override
    public void drawFocus(IGC context) {

        ISDPreferences pref = SDViewPref.getInstance();

        if ((fStartLifeline != fEndLifeline) && (getStartOccurrence() == getEndOccurrence())) {
            context.setLineStyle(context.getLineDotStyle());
            context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
            context.setBackground(pref.getBackGroundColorSelection());
            context.setForeground(pref.getForeGroundColorSelection());
            context.drawFocus(getX(), getY() - 3, getWidth(), getHeight() + 6);
        } else if ((fStartLifeline == fEndLifeline) && (getStartOccurrence() == getEndOccurrence())) {
            context.drawFocus(getX(), getY() - 3, getWidth(), Metrics.SYNC_INTERNAL_MESSAGE_HEIGHT + 6);
        } else if ((fStartLifeline != fEndLifeline) && (getStartOccurrence() != getEndOccurrence())) {
            context.setLineStyle(context.getLineDotStyle());
            context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
            context.setBackground(pref.getBackGroundColor(ISDPreferences.PREF_LIFELINE_HEADER));
            context.setForeground(pref.getForeGroundColor(ISDPreferences.PREF_LIFELINE_HEADER));
            drawRot(getX(), getY() - 5, getWidth(), 10, context);
        } else {
            super.drawFocus(context);
        }
    }
}
