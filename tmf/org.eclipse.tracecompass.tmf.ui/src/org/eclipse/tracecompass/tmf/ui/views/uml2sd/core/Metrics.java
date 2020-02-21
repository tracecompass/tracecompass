/**********************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.uml2sd.core;

/**
 * This class contains the metrics used to layout a sequence diagram on a view The class method are mostly used in
 * combination with the preferences
 *
 * @version 1.0
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
    private static int fLifelineFontHeight = 0;
    /**
     * The message font height
     */
    private static int fMessageFontHeight = 0;
    /**
     * The frame font height
     */
    private static int fFrameFontHeight = 0;
    /**
     * The lifeline header font height
     */
    private static int fLifelineHeaderFontHeight = 0;
    /**
     * The lifeline font widht
     */
    private static int fLifelineFontWidth = 0;
    /**
     * The lifeline width
     */
    private static int fLifeLineWidth = 119;
    /**
     * The (forced) event spacing
     */
    private static int fForcedEventSpacing = -1;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * Hide private constructor
     */
    private Metrics() {
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /**
     * Set the character height used to draw the lifeline name
     *
     * @param height the character height
     */
    public static void setLifelineFontHeight(int height) {
        fLifelineFontHeight = height;
    }

    /**
     * Set the character width used to draw the lifeline name
     *
     * @param width the character width
     */
    public static void setLifelineFontWidth(int width) {
        fLifelineFontWidth = width;
    }

    /**
     * Set the character height used to draw the message name
     *
     * @param fontHeight the character height
     */
    public static void setMessageFontHeight(int fontHeight) {
        fMessageFontHeight = fontHeight;
    }

    /**
     * Returns the character height used to draw the lifeline name
     *
     * @return the character height
     */
    public static int getFrameFontHeigth() {
        return fFrameFontHeight;
    }

    /**
     * Set the character height used to draw the message name
     *
     * @param fontHeight the character height
     */
    public static void setFrameFontHeight(int fontHeight) {
        fFrameFontHeight = fontHeight;
    }

    /**
     * Returns the character height used to draw the lifeline name
     *
     * @return the character height
     */
    public static int getLifelineHeaderFontHeigth() {
        return fLifelineHeaderFontHeight;
    }

    /**
     * Set the character height used to draw the message name
     *
     * @param fontHeight the character height
     */
    public static void setLifelineHeaderFontHeight(int fontHeight) {
        fLifelineHeaderFontHeight = fontHeight;
    }

    /**
     * Returns the character height used to draw the lifeline name
     *
     * @return the character height
     */
    public static int getLifelineFontHeigth() {
        return fLifelineFontHeight;
    }

    /**
     * Returns the character height used to draw the message name
     *
     * @return the character height
     */
    public static int getMessageFontHeigth() {
        if (fForcedEventSpacing >= 0) {
            return 0;
        }
        return fMessageFontHeight;
    }

    /**
     * This is the vertical space used by a Lifeline (mostly the rectangle which contain its name)
     *
     * @return the vertical space used by a Lifeline
     */
    public static int getLifelineWidth() {
        return fLifeLineWidth;
    }

    /**
     * Set the vertical space used by a Lifeline (mostly the rectangle which contain its name)
     *
     * @param value the vertical space
     */
    public static void setLifelineWidth(int value) {
        fLifeLineWidth = value;
    }

    /**
     * Returns the swimming lane width
     *
     * @return the swimming lane width
     */
    public static int swimmingLaneWidth() {
        return getLifelineWidth() + LIFELINE_SPACING;
    }

    /**
     * Returns the character width used to draw the Lifelines name
     *
     * @return the average character width
     */
    public static int getAverageCharWidth() {
        return fLifelineFontWidth;
    }

    /**
     * Returns the message spacing.
     *
     * @return the message spacing
     */
    public static int getMessagesSpacing() {
        if (fForcedEventSpacing >= 0) {
            return fForcedEventSpacing;
        }
        return MESSAGES_SPACING;
    }

    /**
     * Sets the forced event spacing value .
     *
     * @param eventSpacing
     *            The spacing value
     */
    public static void setForcedEventSpacing(int eventSpacing) {
        fForcedEventSpacing = eventSpacing;
    }

    /**
     * Gets the forced event spacing value.
     *
     * @return forcedEventSpacing
     */
    public static int getForcedEventSpacing() {
        return fForcedEventSpacing;
    }
}
