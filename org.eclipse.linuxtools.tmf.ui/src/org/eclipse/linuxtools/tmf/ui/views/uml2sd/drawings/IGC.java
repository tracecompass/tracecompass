/**********************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings;

/**
 * Interface for a graphical context.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public interface IGC {

    /**
     * Set the current line style
     *
     * @param style the new line style
     */
    public abstract void setLineStyle(int style);

    /**
     * Returns current the line style used in the graphical context
     *
     * @return the current line style
     */
    public abstract int getLineStyle();

    /**
     * Returns the contents x coordinate that is at the upper left corner of the view
     *
     * @return the contents x coordinate
     */
    public abstract int getContentsX();

    /**
     * Returns the contents y coordinate that is at the upper left corner of the view
     *
     * @return the contents y coordinate
     */
    public abstract int getContentsY();

    /**
     * Returns the contents visible width
     *
     * @return the contents width
     */
    public abstract int getVisibleWidth();

    /**
     * Returns the contents visible height
     *
     * @return the contents height
     */
    public abstract int getVisibleHeight();

    /**
     * Translates the given contents x coordinate into view x coordinate
     *
     * @param x the x coordinate to translate
     * @return the corresponding view x coordinate
     */
    public abstract int contentsToViewX(int x);

    /**
     * Translates the given contents y coordinate into view y coordinate
     *
     * @param y the y coordinate to translate
     * @return the corresponding view y coordinate
     */
    public abstract int contentsToViewY(int y);

    /**
     * Draws a line, using the foreground color, between the points (x1, y1) and (x2, y2).
     *
     * @param x1 the first point's x coordinate
     * @param y1 the first point's y coordinate
     * @param x2 the second point's x coordinate
     * @param y2 the second point's y coordinate
     */
    public abstract void drawLine(int x1, int y1, int x2, int y2);

    /**
     * Draws the outline of the rectangle specified by the arguments, using the receiver's foreground color. The left
     * and right edges of the rectangle are at x and x + width. The top and bottom edges are at y and y + height.
     *
     * @param x the x coordinate of the rectangle to be drawn
     * @param y the y coordinate of the rectangle to be drawn
     * @param width the width of the rectangle to be drawn
     * @param height the height of the rectangle to be drawn
     */
    public abstract void drawRectangle(int x, int y, int width, int height);

    /**
     * Draws a rectangle, based on the specified arguments, which has the appearance of the platform's focus rectangle
     * if the platform supports such a notion, and otherwise draws a simple rectangle in the receiver's foreground
     * color.
     *
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public abstract void drawFocus(int x, int y, int width, int height);

    /**
     * Fills the interior of the closed polygon which is defined by the specified array of integer coordinates, using
     * the receiver's background color. The array contains alternating x and y values which are considered to represent
     * points which are the vertices of the polygon. Lines are drawn between each consecutive pair, and between the
     * first pair and last pair in the array.
     *
     * @param points an array of alternating x and y values which are the vertices of the polygon
     */
    public abstract void fillPolygon(int[] points);

    /**
     * Draws the closed polygon which is defined by the specified array of integer coordinates, using the receiver's
     * foreground color. The array contains alternating x and y values which are considered to represent points which
     * are the vertices of the polygon. Lines are drawn between each consecutive pair, and between the first pair and
     * last pair in the array.
     *
     * @param points an array of alternating x and y values which are the vertices of the polygon
     */
    public abstract void drawPolygon(int[] points);

    /**
     * Fills the interior of the rectangle specified by the arguments, using the receiver's background color.
     *
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled
     * @param height the height of the rectangle to be filled
     */
    public abstract void fillRectangle(int x, int y, int width, int height);

    /**
     * Fills the interior of the specified rectangle with a gradient sweeping from left to right or top to bottom
     * progressing from the graphical context gradient color to its background color.
     *
     * @param x the x coordinate of the rectangle to be filled
     * @param y the y coordinate of the rectangle to be filled
     * @param width the width of the rectangle to be filled, may be negative (inverts direction of gradient if
     *            horizontal)
     * @param height the height of the rectangle to be filled, may be negative (inverts direction of gradient if
     *            horizontal)
     * @param vertical if true sweeps from top to bottom, else sweeps from left to right
     */
    public abstract void fillGradientRectangle(int x, int y, int width, int height, boolean vertical);

    /**
     * Returns the given string width in pixels
     *
     * @param name the string
     * @return the string width
     */
    public abstract int textExtent(String name);

    /**
     * Draws the given string, using the receiver's current font and foreground color. Tab expansion and carriage return
     * processing are performed. If trans is true, then the background of the rectangular area where the text is being
     * drawn will not be modified, otherwise it will be filled with the receiver's background color.
     *
     * @param string the string to be drawn
     * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param trans if true the background will be transparent, otherwise it will be opaque
     */
    public abstract void drawText(String string, int x, int y, boolean trans);

    /**
     * Draws the given string, using the receiver's current font and foreground color. Tab expansion and carriage return
     * processing are performed. The background of the rectangular area where the text is being drawn will be filled
     * with the receiver's background color.
     *
     * @param string the string to be drawn
     * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
     * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
     */
    public abstract void drawText(String string, int x, int y);

    /**
     * Fills the interior of an oval, within the specified rectangular area, with the receiver's background color.
     *
     * @param x the x coordinate of the upper left corner of the oval to be filled
     * @param y the y coordinate of the upper left corner of the oval to be filled
     * @param width the width of the oval to be filled
     * @param height the width of the oval to be filled
     */
    public abstract void fillOval(int x, int y, int width, int height);

    /**
     * Returns current the background color used in the graphical context
     *
     * @return the background color
     */
    public abstract IColor getBackground();

    /**
     * Returns current the background color used in the graphical context
     *
     * @return the background color
     */
    public abstract IColor getForeground();

    /**
     * Set the graphical context foreground color
     *
     * @param color the foreground color
     */
    public abstract void setBackground(IColor color);

    /**
     * Set the graphical context background color
     *
     * @param color the background color
     */
    public abstract void setForeground(IColor color);

    /**
     * Set the color to use when filling regions using gradient. The color will progess from the given color to the
     * current background color
     *
     * @param color the gardiient color to use
     */
    public abstract void setGradientColor(IColor color);

    /**
     * Set the line width to use for drawing
     *
     * @param width the line width
     */
    public abstract void setLineWidth(int width);

    /**
     * Returns the current graphical context line width used for drawing
     *
     * @return the line width
     */
    public abstract int getLineWidth();

    /**
     * Returns the LineDotD style constant
     *
     * @return the constant value
     */
    public abstract int getLineDotStyle();

    /**
     * Returns the LineDash style constant
     *
     * @return the constant
     */
    public abstract int getLineDashStyle();

    /**
     * Returns the LineSolid style constant
     *
     * @return the constant
     */
    public abstract int getLineSolidStyle();

    /**
     * Draws the given string centered into the given rectangle. If the string cannot fit in the rectangle area, the
     * string is truncated. If trans is true, then the background of the rectangular area where the text is being drawn
     * will not be modified, otherwise it will be filled with the receiver's background color.
     *
     * @param name the string to draw
     * @param x the x coordinate of the rectangle to draw the string
     * @param y the y coordinate of the rectangle to draw the string
     * @param width the width of the rectangle to draw the string
     * @param height the height of the rectangle to draw the string
     * @param trans if true the background will be transparent, otherwise it will be opaque
     */
    public abstract void drawTextTruncatedCentred(String name, int x, int y, int width, int height, boolean trans);

    /**
     * Draws the given string into the given rectangle (left justify) If the string cannot fit in the rectangle area,
     * the string is truncated. If trans is true, then the background of the rectangular area where the text is being
     * drawn will not be modified, otherwise it will be filled with the receiver's background color.
     *
     * @param name The text to put in the rectangle
     * @param x the x coordinate of the rectangle to draw the string
     * @param y the y coordinate of the rectangle to draw the string
     * @param width the width of the rectangle to draw the string
     * @param height the height of the rectangle to draw the string
     * @param trans if true the background will be transparent, otherwise it will be opaque
     */
    public abstract void drawTextTruncated(String name, int x, int y, int width, int height, boolean trans);

    /**
     * Copies a the source image into a (potentially different sized) rectangular area in the graphical context. If the
     * source image has smaller sizes, then the source area will be stretched to fit the destination area as it is
     * copied.
     *
     * @param image the image to draw
     * @param x the x coordinate in the destination to copy to
     * @param y the y coordinate in the destination to copy to
     * @param maxWith the width in pixels of the destination rectangle
     * @param maxHeight the height in pixels of the destination rectangle
     */
    public abstract void drawImage(IImage image, int x, int y, int maxWith, int maxHeight);

    /**
     * Draws the outline of a circular or elliptical arc within the specified rectangular area. The resulting arc begins
     * at startAngle and extends for arcAngle degrees, using the current color. Angles are interpreted such that 0
     * degrees is at the 3 o'clock position. A positive value indicates a counter-clockwise rotation while a negative
     * value indicates a clockwise rotation. The center of the arc is the center of the rectangle whose origin is (x, y)
     * and whose size is specified by the width and height arguments. The resulting arc covers an area width + 1 pixels
     * wide by height + 1 pixels tall.
     *
     * @param x the x coordinate of the upper-left corner of the arc to be drawn
     * @param y the y coordinate of the upper-left corner of the arc to be drawn
     * @param width the width of the arc to be drawn
     * @param height the height of the arc to be drawn
     * @param startAngle the beginning angle
     * @param endAngle the ending angle
     */
    public abstract void drawArc(int x, int y, int width, int height, int startAngle, int endAngle);

    /**
     * Set the current font used in the graphical context
     *
     * @param font the font to use
     */
    public abstract void setFont(IFont font);

    /**
     * Returns the font height given font
     *
     * @param font The font to check for
     * @return the the font height
     */
    public abstract int getFontHeight(IFont font);

    /**
     * Returns the average character width for the given font
     *
     * @param font The font to check for
     * @return the average width
     */
    public abstract int getFontWidth(IFont font);

    /**
     * Creates a color with the given RGB values
     *
     * @param r the red component
     * @param g the green component
     * @param b the blue component
     * @return the color
     */
    public abstract IColor createColor(int r, int g, int b);

    /**
     * Returns the zoom factor applied in both x and y directions when drawing
     *
     * @return the zoom factor
     */
    public abstract float getZoom();

    /**
     * Draws text with focus style.
     *
     * @param focus <code>true</code> if item has focus else <code>false</code>
     */
    public abstract void setDrawTextWithFocusStyle(boolean focus);
}
