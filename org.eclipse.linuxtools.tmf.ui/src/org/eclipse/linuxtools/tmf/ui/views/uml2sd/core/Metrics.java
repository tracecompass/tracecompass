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

/**
 * This class contains the metrics used to layout a sequence diagram on a view The class method are mostly used in
 * combination with the preferences
 * 
 * @version
 * @author sveyrier
 * 
 */
public class Metrics {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Space between the Frame and the top of the View This also represent the space between the frame and the bottom of
     * the View
     */
    public static final int FRAME_H_MARGIN = 10;
    /**
     * Space between the Frame and the left of the View This also represent the space between the Frame and the right of
     * the View
     */
    public static final int FRAME_V_MARGIN = 10;
    /**
     * Space between the Lifelines and the right of the Frame
     */
    public static final int LIFELINE_H_MAGIN = 23;
    /**
     * Space between the Lifelines and the bottom of the Frame
     */
    public static final int LIFELINE_VB_MAGIN = 20;
    /**
     * Space between the Lifelines and the top of the Frame
     */
    public static final int LIFELINE_VT_MAGIN = 30;// 18
    /**
     * Vertical space between the lifeline name and the rectangle which contains that name This is only for the
     * "always visible" lifeline name rectangle
     */
    public static final int LIFELINE_HEARDER_TEXT_V_MARGIN = 4;
    /**
     * Vertical spacing between messages
     */
    public static final int MESSAGES_SPACING = 30;
    /**
     * Vertical spacing between the message and its name
     */
    public static final int MESSAGES_NAME_SPACING = 10;
    /**
     * Horizontal spacing between the Frame name and its containing rectangle
     */
    public static final int FRAME_NAME_H_MARGIN = 4;
    /**
     * Vertical spacing between the Frame name and its containing rectangle
     */
    public static final int FRAME_NAME_V_MARGIN = 8;
    /**
     * Horizontal spacing between the lifeline name and its containing rectangle
     */
    public static final int LIFELINE_NAME_H_MARGIN = 14;
    /**
     * Vertical spacing between the lifeline name and its containing rectangle
     */
    public static final int LIFELINE_NAME_V_MARGIN = 20;
    /**
     * Space between the rectangles which contain the Lifelines name
     */
    public static final int LIFELINE_SPACING = 45;
    /**
     * The circle ray used to draw the circle which compose Found and Lost messages
     */
    public static final int MESSAGE_CIRCLE_RAY = 5;
    /**
     * Execution occurrence vertical width
     */
    public static final int EXECUTION_OCCURRENCE_WIDTH = 8;
    /**
     * The square width which contains the Stop representation (a cross)
     */
    public static final int STOP_WIDTH = 20;
    /**
     * The internal message width. 
     */
    public static final int INTERNAL_MESSAGE_WIDTH = 20;
    /**
     * The internal sychrounous message height.
     */
    public static final int SYNC_INTERNAL_MESSAGE_HEIGHT = 10;
    /**
     * Line width used when drawing selected GraphNode
     */
    public static final int SELECTION_LINE_WIDTH = 5;
    /**
     * Line width used when drawing non selected GraphNode
     */
    public static final int NORMAL_LINE_WIDTH = 1;
    /**
     * The internal vertical message margin
     */
    public static final int INTERNAL_MESSAGE_V_MARGIN = 10;

    /**
     * Used to sample the diagram. When the lifeline spacing is smaller than this constant when zooming out then less
     * lifelines are displayed to avoid lifelines overlapping and mainly saving some execution time
     */
    public static final int LIFELINE_SIGNIFICANT_HSPACING = 10;
    /**
     * Used to sample the diagram. When the message spacing is smaller than this constant when zooming out then less
     * message are displayed to avoid message overlapping and mainly saving some execution time
     */
    public static final int MESSAGE_SIGNIFICANT_VSPACING = 1;
    /**
     *  Message selection tolerance. Used for internal syncMessages only
     */
    public static final int MESSAGE_SELECTION_TOLERANCE = 30;
    /**
     * The focus drawing margin.
     */
    public static final int FOCUS_DRAWING_MARGIN = 10;
    
    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The lifeline font height
     */
    private static int lifelineFontHeight = 0;
    /**
     * The message font height
     */
    private static int messageFontHeight = 0;
    /**
     * The frame font height
     */
    private static int frameFontHeight = 0;
    /**
     * The lifeline header font height
     */
    private static int lifelineHeaderFontHeight = 0;
    /**
     * The lifeline font widht
     */
    private static int lifelineFontWidth = 0;
    /**
     * The lifeline width
     */
    private static int lifeLineWidth = 119;
    /**
     * The (forced) event spacing
     */
    private static int forcedEventSpacing = -1;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Set the character height used to draw the lifeline name
     * 
     * @param height the character height
     */
    static public void setLifelineFontHeight(int height) {
        lifelineFontHeight = height;
    }

    /**
     * Set the character width used to draw the lifeline name
     * 
     * @param width the character width
     */
    static public void setLifelineFontWidth(int width) {
        lifelineFontWidth = width;
    }

    /**
     * Set the character height used to draw the message name
     * 
     * @param fontHeight the character height
     */
    static public void setMessageFontHeight(int fontHeight) {
        messageFontHeight = fontHeight;
    }

    /**
     * Returns the character height used to draw the lifeline name
     * 
     * @return the character height
     */
    static public int getFrameFontHeigth() {
        return frameFontHeight;
    }

    /**
     * Set the character height used to draw the message name
     * 
     * @param fontHeight the character height
     */
    static public void setFrameFontHeight(int fontHeight) {
        frameFontHeight = fontHeight;
    }

    /**
     * Returns the character height used to draw the lifeline name
     * 
     * @return the character height
     */
    static public int getLifelineHeaderFontHeigth() {
        return lifelineHeaderFontHeight;
    }

    /**
     * Set the character height used to draw the message name
     * 
     * @param fontHeight the character height
     */
    static public void setLifelineHeaderFontHeight(int fontHeight) {
        lifelineHeaderFontHeight = fontHeight;
    }

    /**
     * Returns the character height used to draw the lifeline name
     * 
     * @return the character height
     */
    static public int getLifelineFontHeigth() {
        return lifelineFontHeight;
    }

    /**
     * Returns the character height used to draw the message name
     * 
     * @return the character height
     */
    static public int getMessageFontHeigth() {
        if (forcedEventSpacing >= 0) {
            return 0;
        }
        return messageFontHeight;
    }

    /**
     * This is the vertical space used by a Lifeline (mostly the rectangle which contain its name)
     * 
     * @return the vertical space used by a Lifeline
     */
    static public int getLifelineWidth() {
        return lifeLineWidth;
    }

    /**
     * Set the vertical space used by a Lifeline (mostly the rectangle which contain its name)
     * 
     * @param value the vertical space
     */
    static public void setLifelineWidth(int value) {
        lifeLineWidth = value;
    }

    /**
     * Returns the swimming lane width
     * 
     * @return the swimming lane width
     */
    static public int swimmingLaneWidth() {
        return getLifelineWidth() + LIFELINE_SPACING;
    }

    /**
     * Returns the character width used to draw the Lifelines name
     * 
     * @return the average character width
     */
    static public int getAverageCharWidth() {
        return lifelineFontWidth;
    }

    /**
     * Returns the message spacing.
     * 
     * @return the message spacing
     */
    static public int getMessagesSpacing() {
        if (forcedEventSpacing >= 0) {
            return forcedEventSpacing;
        } 
        return MESSAGES_SPACING;
    }

    /**
     * Sets the forced event spacing value .
     * 
     * @param eventSpacing
     */
    static public void setForcedEventSpacing(int eventSpacing) {
        forcedEventSpacing = eventSpacing;
    }

    /**
     * Gets the forced event spacing value.
     * 
     * @return forcedEventSpacing
     */
    static public int getForcedEventSpacing() {
        return forcedEventSpacing;
    }
}
