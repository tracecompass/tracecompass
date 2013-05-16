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

package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.impl.ColorImpl;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * This class implements the graphical context for the sequence diagram widgets.
 * </p>
 *
 * @version 1.0
 * @author sveyrier
 */
public class NGC implements IGC {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The graphical context.
     */
    private GC fContext;
    /**
     * The reference to the sequence diagram view.
     */
    private SDWidget fView;
    /**
     * A reference to the last used font.
     */
    private Font fTempFont = null;
    /**
     * The color of the gradient.
     */
    private IColor fGradientColor = null;
    /**
     * The color of the background.
     */
    private IColor fBackground = null;
    /**
     * The color of the foreground.
     */
    private IColor fForeground = null;
    /**
     * The current visible y screen bounds
     */
    private int fVisibleY;
    /**
     * The current visible x screen bound.
     */
    private int fVisibleX;
    /**
     * The current yx value (view visible height - visible screen bounds)
     */
    private int yx;
    /**
     * The current xx value (view visible width - visible screen bounds)
     */
    private int xx;
    /**
     * <code>true</code> to draw with focus else <code>false</code>.
     */
    private boolean fDrawWithFocus = false;

    /**
     * The static visible screen bounds.
     */
    private static int fVisibleScreenBounds = 0;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor.
     *
     * @param scrollView A sequence diagram view reference.
     * @param gc A graphical context.
     */
    public NGC(SDWidget scrollView, GC gc) {
        fContext = gc;
        fView = scrollView;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void setLineStyle(int style) {
        fContext.setLineStyle(style);
    }

    @Override
    public int getLineStyle() {
        return fContext.getLineStyle();
    }

    @Override
    public int getContentsX() {
        return Math.round(fView.getContentsX() / fView.getZoomValue());
    }

    @Override
    public int getContentsY() {
        return Math.round(fView.getContentsY() / fView.getZoomValue());
    }

    @Override
    public int getVisibleWidth() {
        return Math.round(fView.getVisibleWidth() / fView.getZoomValue());
    }

    @Override
    public int getVisibleHeight() {
        return Math.round(fView.getVisibleHeight() / fView.getZoomValue());
    }

    /**
     * Returns the current visible y screen bounds.
     *
     * @return the current visible y screen bounds
     * @since 2.0
     */
    protected int getVisibleY() {
        return fVisibleY;
    }

    /**
     * Sets the current visible y screen bounds.
     *
     * @param visibleY
     *          the current visible y screen bounds
     * @since 2.0
     */
    protected void setVisibleY(int visibleY) {
        fVisibleY = visibleY;
    }

    /**
     * Returns the current visible x screen bound.
     *
     * @return the current visible x screen bound.
     * @since 2.0
     *
     */
    protected int getfVisibleX() {
        return fVisibleX;
    }

    /**
     * Sets the current visible x screen bound.
     *
     * @param visibleX
     *          the current visible x screen bound.
     * @since 2.0
     *
     */
    protected void setVisibleX(int visibleX) {
        fVisibleX = visibleX;
    }

    /**
     * Returns current yx value (view visible height - visible screen bounds).
     *
     * @return current yx value
     * @since 2.0
     */
    protected int getYx() {
        return yx;
    }

    /**
     * Sets current yx value (view visible height - visible screen bounds).
     *
     * @param  yx
     *       current yx value
     * @since 2.0
     */
    protected void setYx(int yx) {
        this.yx = yx;
    }

    /**
     * Returns the current xx value (view visible width - visible screen bounds)
     *
     * @return the current xx value
     * @since 2.0
     */
    protected int getXx() {
        return xx;
    }

    /**
     * Sets the current xx value (view visible width - visible screen bounds)
     *
     * @param xx
     *      the current xx value
     * @since 2.0
     */
    protected void setXx(int xx) {
        this.xx = xx;
    }

    @Override
    public int contentsToViewX(int x) {
        return fView.contentsToViewX(x);
    }

    @Override
    public int contentsToViewY(int y) {
        return fView.contentsToViewY(y);
    }

    /**
     * Get code for drawings  at given x and y position.
     *
     * @param x The x position
     * @param y The y position.
     * @return A code for top, bottom, right and left
     */
    protected byte code(int x, int y) {
        byte c = 0;
        fVisibleY = fVisibleScreenBounds;
        fVisibleX = fVisibleScreenBounds;
        yx = fView.getVisibleHeight() + fVisibleScreenBounds;
        xx = fView.getVisibleWidth() + fVisibleScreenBounds;
        if (y > yx) {
            c |= 0x01; // top
        } else if (y < fVisibleY) {
            c |= 0x02; // bottom
        }

        if (x > xx) {
            c |= 0x04; // right
        } else if (x < fVisibleX) {
            c |= 0x08; // left
        }
        return c;
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        int localX1 = x1;
        int localY1 = y1;
        int localX2 = x2;
        int localY2 = y2;

        localX1 = Math.round(localX1 * fView.getZoomValue());
        localY1 = Math.round(localY1 * fView.getZoomValue());
        localX2 = Math.round(localX2 * fView.getZoomValue());
        localY2 = Math.round(localY2 * fView.getZoomValue());
        localX1 = fView.contentsToViewX(localX1);
        localY1 = fView.contentsToViewY(localY1);
        localX2 = fView.contentsToViewX(localX2);
        localY2 = fView.contentsToViewY(localY2);

        byte code1 = code(localX1, localY1);
        byte code2 = code(localX2, localY2);
        byte codex;
        boolean draw = false;
        boolean end = false;
        int x = 0, y = 0;

        do {
            if (code1 == 0 && code2 == 0) {
                draw = true;
                end = true;
            } else if ((code1 & code2) != 0) {
                end = true;
            } else {
                codex = (code1 != 0) ? code1 : code2;
                if ((codex & 0x01) != 0) { // top
                    x = localX1 + ((localX2 - localX1) * (yx - localY1)) / (localY2 - localY1);
                    y = yx;
                } else if ((codex & 0x02) != 0) { // bottom
                    x = localX1 + ((localX2 - localX1) * (fVisibleY - localY1)) / (localY2 - localY1);
                    y = fVisibleY;
                } else if ((codex & 0x04) != 0) { // right
                    y = localY1 + ((localY2 - localY1) * (xx - localX1)) / (localX2 - localX1);
                    x = xx;
                } else if ((codex & 0x08) != 0) { // left
                    y = localY1 + ((localY2 - localY1) * (fVisibleX - localX1)) / (localX2 - localX1);
                    x = fVisibleX;
                }

                if (codex == code1) {
                    localX1 = x;
                    localY1 = y;
                    code1 = code(localX1, localY1);
                } else {
                    localX2 = x;
                    localY2 = y;
                    code2 = code(localX2, localY2);
                }
            }
        } while (!end);

        if (draw) {
            fContext.drawLine(localX1, localY1, localX2, localY2);
        }
    }

    @Override
    public void drawRectangle(int x, int y, int width, int height) {
        int localX = x;
        int localY = y;
        int localWidth = width;
        int localHeight = height;

        localX = Math.round(localX * fView.getZoomValue());
        // Workaround to avoid problems for some special cases (not very nice)
        if (localY != getContentsY()) {
            localY = Math.round(localY * fView.getZoomValue());
            localY = fView.contentsToViewY(localY);
        } else {
            localY = 0;
        }
        localWidth = Math.round(localWidth * fView.getZoomValue());
        localHeight = Math.round(localHeight * fView.getZoomValue());
        localX = fView.contentsToViewX(localX);

        if (localX < -fVisibleScreenBounds) {
            localWidth = localWidth + localX + fVisibleScreenBounds;
            localX = -fVisibleScreenBounds;
        }
        if (localY < -fVisibleScreenBounds) {
            localHeight = localHeight + localY + fVisibleScreenBounds;
            localY = -fVisibleScreenBounds;
        }
        if ((localWidth < -fVisibleScreenBounds) && (localX + localWidth < -fVisibleScreenBounds)) {
            localWidth = -fVisibleScreenBounds;
        } else if (localWidth + localX > fView.getVisibleWidth() + fVisibleScreenBounds) {
            localWidth = fView.getVisibleWidth() + fVisibleScreenBounds - localX;
        }
        if ((localHeight < -fVisibleScreenBounds) && (localY + localHeight < -fVisibleScreenBounds)) {
            localHeight = -fVisibleScreenBounds;
        } else if (localHeight + localY > fView.getVisibleHeight() + fVisibleScreenBounds) {
            localHeight = fView.getVisibleHeight() + fVisibleScreenBounds - localY;
        }
        fContext.drawRectangle(localX, localY, localWidth, localHeight);
    }

    @Override
    public void drawFocus(int x, int y, int width, int height) {
        int localX = x;
        int localY = y;
        int localWidth = width;
        int localHeight = height;

        IColor bC = getBackground();
        IColor fC = getForeground();

        if (localWidth < 0) {
            localX = localX + localWidth;
            localWidth = -localWidth;
        }

        if (localHeight < 0) {
            localY = localY + localHeight;
            localHeight = -localHeight;
        }

        localX = Math.round(localX * fView.getZoomValue());
        localY = Math.round(localY * fView.getZoomValue());
        localWidth = Math.round(localWidth * fView.getZoomValue());
        localHeight = Math.round(localHeight * fView.getZoomValue());

        setForeground(SDViewPref.getInstance().getForeGroundColorSelection());
        setBackground(SDViewPref.getInstance().getBackGroundColorSelection());

        fContext.drawFocus(fView.contentsToViewX(localX - 1), fView.contentsToViewY(localY - 1), localWidth + 3, localHeight + 3);

        setBackground(bC);
        setForeground(fC);
    }

    @Override
    public void fillPolygon(int[] points) {
        int len = (points.length / 2) * 2;
        int[] localPoint = new int[len];
        for (int i = 0; i < len; i++) {
            localPoint[i] = fView.contentsToViewX(Math.round(points[i] * fView.getZoomValue()));
            i++;
            localPoint[i] = fView.contentsToViewY(Math.round(points[i] * fView.getZoomValue()));
        }

        if (validatePolygonHeight(localPoint) <= 0) {
            return;
        }

        fContext.fillPolygon(localPoint);
    }

    @Override
    public void drawPolygon(int[] points) {
        int len = (points.length / 2) * 2;
        int[] localPoint = new int[len];
        for (int i = 0; i < len; i++) {
            localPoint[i] = fView.contentsToViewX(Math.round(points[i] * fView.getZoomValue()));
            i++;
            localPoint[i] = fView.contentsToViewY(Math.round(points[i] * fView.getZoomValue()));
        }

        if (validatePolygonHeight(localPoint) <= 0) {
            return;
        }

        fContext.drawPolygon(localPoint);
    }

    @Override
    public void fillRectangle(int x, int y, int width, int height) {
        int localX = x;
        int localY = y;
        int localWidth = width;
        int localHeight = height;

        localX = Math.round(localX * fView.getZoomValue());
        // Workaround to avoid problems for some special cases (not very nice)
        if (localY != getContentsY()) {
            localY = Math.round(localY * fView.getZoomValue());
            localY = fView.contentsToViewY(localY) + 1;
        } else {
            localY = 1;
        }
        localWidth = Math.round(localWidth * fView.getZoomValue()) - 1;
        localHeight = Math.round(localHeight * fView.getZoomValue()) - 1;
        localX = fView.contentsToViewX(localX) + 1;
        if (localX < -fVisibleScreenBounds) {
            localWidth = localWidth + localX + fVisibleScreenBounds;
            localX = -fVisibleScreenBounds;
        }
        if (localY < -fVisibleScreenBounds) {
            localHeight = localHeight + localY + fVisibleScreenBounds;
            localY = -fVisibleScreenBounds;
        }
        if ((localWidth < -fVisibleScreenBounds) && (localX + localWidth < -fVisibleScreenBounds)) {
            localWidth = -fVisibleScreenBounds;
        } else if (localWidth + localX > fView.getVisibleWidth() + fVisibleScreenBounds) {
            localWidth = fView.getVisibleWidth() + fVisibleScreenBounds - localX;
        }
        if ((localHeight < -fVisibleScreenBounds) && (localY + localHeight < -fVisibleScreenBounds)) {
            localHeight = -fVisibleScreenBounds;
        } else if (localHeight + localY > fView.getVisibleHeight() + fVisibleScreenBounds) {
            localHeight = fView.getVisibleHeight() + fVisibleScreenBounds - localY;
        }
        fContext.fillRectangle(localX, localY, localWidth, localHeight);
    }

    @Override
    public void fillGradientRectangle(int x, int y, int width, int height, boolean isVertical) {
        int localX = x;
        int localY = y;
        int localWidth = width;
        int localHeight = height;

        localX = Math.round(localX * fView.getZoomValue());
        localY = Math.round(localY * fView.getZoomValue());
        localWidth = Math.round(localWidth * fView.getZoomValue());
        localHeight = Math.round(localHeight * fView.getZoomValue());
        IColor tempColor = fForeground;
        setForeground(fGradientColor);
        localX = fView.contentsToViewX(localX);
        localY = fView.contentsToViewY(localY);

        if (localX < -fVisibleScreenBounds) {
            localWidth = localWidth + localX + fVisibleScreenBounds;
            localX = -fVisibleScreenBounds;
        }
        if (localY < -fVisibleScreenBounds) {
            localHeight = localHeight + localY + fVisibleScreenBounds;
            localY = -fVisibleScreenBounds;
        }

        if ((localWidth < -fVisibleScreenBounds) && (localX + localWidth < -fVisibleScreenBounds)) {
            localWidth = -fVisibleScreenBounds;
        } else if (localWidth + localX > fView.getVisibleWidth() + fVisibleScreenBounds) {
            localWidth = fView.getVisibleWidth() + fVisibleScreenBounds - localX;
        }
        if ((localHeight < -fVisibleScreenBounds) && (localY + localHeight < -fVisibleScreenBounds)) {
            localHeight = -fVisibleScreenBounds;
        } else if (localHeight + localY > fView.getVisibleHeight() + fVisibleScreenBounds) {
            localHeight = fView.getVisibleHeight() + fVisibleScreenBounds - localY;
        }
        if (isVertical) {
            fContext.fillGradientRectangle(localX, localY, localWidth, localHeight, isVertical);
        }
        else {
            fContext.fillGradientRectangle(localX + localWidth, localY, -localWidth, localHeight + 1, isVertical);
        }
        setForeground(tempColor);
    }

    @Override
    public int textExtent(String name) {
        return fContext.textExtent(name).x;
    }

    @Override
    public void drawText(String string, int x, int y, boolean isTrans) {
        int localX = x;
        int localY = y;

        localX = Math.round(localX * fView.getZoomValue());
        localY = Math.round(localY * fView.getZoomValue());
        fContext.drawText(string, fView.contentsToViewX(localX), fView.contentsToViewY(localY), isTrans);
        if (fDrawWithFocus) {
            Point r = fContext.textExtent(string);
            fContext.drawFocus(localX - 1, localY - 1, r.x + 2, r.y + 2);
        }
    }

    @Override
    public void drawText(String string, int x, int y) {
        int localX = x;
        int localY = y;

        localX = Math.round(localX * fView.getZoomValue());
        localY = Math.round(localY * fView.getZoomValue());
        fContext.drawText(string, fView.contentsToViewX(localX), fView.contentsToViewY(localY), true);
        if (fDrawWithFocus) {
            Point r = fContext.textExtent(string);
            fContext.drawFocus(localX - 1, localY - 1, r.x + 2, r.y + 2);
        }
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        int localX = x;
        int localY = y;
        int localWidth = width;
        int localHeight = height;

        localX = Math.round(localX * fView.getZoomValue());
        localY = Math.round(localY * fView.getZoomValue());
        localWidth = Math.round(localWidth * fView.getZoomValue());
        localHeight = Math.round(localHeight * fView.getZoomValue());
        fContext.fillOval(fView.contentsToViewX(localX), fView.contentsToViewY(localY), localWidth, localHeight);
    }

    @Override
    public IColor getBackground() {
        if ((fBackground != null) && (fBackground.getColor() instanceof Color) && (!((Color) (fBackground.getColor())).isDisposed())) {
            return fBackground;
        }
        return ColorImpl.getSystemColor(SWT.COLOR_WHITE);
    }

    @Override
    public IColor getForeground() {
        if ((fForeground != null) && (fForeground.getColor() instanceof Color) && (!((Color) (fForeground.getColor())).isDisposed())) {
            return fForeground;
        }
        return ColorImpl.getSystemColor(SWT.COLOR_WHITE);
    }

    @Override
    public void setBackground(IColor color) {
        if (color == null) {
            return;
        }
        if (color.getColor() instanceof Color) {
            fContext.setBackground((Color) color.getColor());
            fBackground = color;
        }
    }

    @Override
    public void setForeground(IColor color) {
        if (color == null) {
            return;
        }
        if (color.getColor() instanceof Color) {
            Color c = (Color) color.getColor();
            if (!c.isDisposed()) {
                fContext.setForeground(c);
                fForeground = color;
            }
        }
    }

    @Override
    public void setGradientColor(IColor color) {
        if (color == null) {
            return;
        }
        if (color.getColor() instanceof Color) {
            fGradientColor = color;
        }
    }

    @Override
    public void setLineWidth(int width) {
        if (fView.isPrinting()) {
            fContext.setLineWidth(width * 2);
        }
        else {
            fContext.setLineWidth(width);
        }
    }

    @Override
    public int getLineWidth() {
        return fContext.getLineWidth();
    }

    /**
     * Method to draw a text in rectangle. (Linux GTK Workaround)
     *
     * @param string The text to draw.
     * @param x The x position.
     * @param y The y position.
     * @param isTransparent true for transparent else false
     */
    protected void localDrawText(String string, int x, int y, boolean isTransparent) {
        Point r = fContext.textExtent(string);
        if (!isTransparent) {
            fContext.fillRectangle(x, y, r.x, r.y);
        }
        fContext.drawText(string, x, y, isTransparent);
        if ((fDrawWithFocus) && (string.length() > 1)) {
            fContext.drawFocus(x - 1, y - 1, r.x + 2, r.y + 2);
        }
    }

    @Override
    public void drawTextTruncatedCentred(String name, int xValue, int yValue, int width, int height, boolean trans) {
        int localX = xValue;
        int localY = yValue;
        int localWidth = width;
        int localHeight = height;

        Point tx = fContext.textExtent(name);
        localX = Math.round(localX * fView.getZoomValue());
        int y = 0;
        // Workaround to avoid round problems for some special cases (not very nice)
        if (localY != getContentsY()) {
            localY = Math.round(localY * fView.getZoomValue());
            y = fView.contentsToViewY(localY);
        }
        localWidth = Math.round(localWidth * fView.getZoomValue());
        localHeight = Math.round(localHeight * fView.getZoomValue());
        int x = fView.contentsToViewX(localX);
        if (tx.y > localHeight) {
            return;
        }

        // Adjust height and y
        if (y < -fVisibleScreenBounds) {
            localHeight = localHeight + y + fVisibleScreenBounds;
            y = -fVisibleScreenBounds;
        }
        if ((localHeight < -fVisibleScreenBounds) && (y + localHeight < -fVisibleScreenBounds)) {
            localHeight = -fVisibleScreenBounds;
        } else if (localHeight + y > fView.getVisibleHeight() + fVisibleScreenBounds) {
            localHeight = fView.getVisibleHeight() + fVisibleScreenBounds - y;
        }

        if (tx.x <= localWidth) {
            localDrawText(name, x + 1 + (localWidth - tx.x) / 2, y + 1 + (localHeight - tx.y) / 2, trans);
        } else {
            String nameToDisplay = name;
            for (int i = name.length() - 1; i >= 0 && fContext.textExtent(nameToDisplay).x >= localWidth; i--) {
                nameToDisplay = name.substring(0, i);
            }
            int dotCount = 0;
            for (int i = 1; i <= 3 && nameToDisplay.length() - i > 0; i++) {
                dotCount++;
            }
            nameToDisplay = nameToDisplay.substring(0, nameToDisplay.length() - dotCount);
            StringBuffer buf = new StringBuffer(nameToDisplay);
            for (int i = 0; i < dotCount; i++) {
                buf.append("."); //$NON-NLS-1$
            }
            nameToDisplay = buf.toString();
            localDrawText(nameToDisplay, x + 1 + (localWidth - fContext.textExtent(nameToDisplay).x) / 2, y + 1 + (localHeight - fContext.textExtent(nameToDisplay).y) / 2, trans);
        }
    }

    @Override
    public void drawTextTruncated(String name, int xValue, int yValue, int width, int height, boolean trans) {
        int localX = xValue;
        int localY = yValue;
        int localWidth = width;
        int localHeight = height;

        localX = Math.round(localX * fView.getZoomValue());
        localY = Math.round(localY * fView.getZoomValue());
        localWidth = Math.round(localWidth * fView.getZoomValue());
        localHeight = Math.round(localHeight * fView.getZoomValue());
        int x = fView.contentsToViewX(localX);
        int y = fView.contentsToViewY(localY);
        if (fContext.textExtent(name).x <= localWidth) {
            localDrawText(name, x + 1, y + 1 + localHeight, trans);
        } else {
            String nameToDisplay = name;
            for (int i = name.length() - 1; i >= 0 && fContext.textExtent(nameToDisplay).x >= localWidth; i--) {
                nameToDisplay = name.substring(0, i);
            }
            int dotCount = 0;
            for (int i = 1; i <= 3 && nameToDisplay.length() - i > 0; i++) {
                dotCount++;
            }
            nameToDisplay = nameToDisplay.substring(0, nameToDisplay.length() - dotCount);

            StringBuffer buf = new StringBuffer(nameToDisplay);

            for (int i = 0; i < dotCount; i++) {
                buf.append("."); //$NON-NLS-1$
            }
            nameToDisplay = buf.toString();
            localDrawText(nameToDisplay, x + 1, y + 1 + localHeight, trans);
        }
    }

    @Override
    public void drawImage(IImage image, int xValue, int yValue, int maxWith, int maxHeight) {
        int localX = xValue;
        int localY = yValue;

        Image img = null;
        if (image != null && image.getImage() instanceof Image) {
            img = (Image) image.getImage();
        } else {
            localX = Math.round(localX * fView.getZoomValue());
            localY = Math.round(localY * fView.getZoomValue());
            int x = fView.contentsToViewX(localX);
            int y = fView.contentsToViewY(localY);
            float tempZoom = fView.getZoomValue();
            int width = Math.round(maxWith * tempZoom);
            int height = Math.round(maxHeight * tempZoom);
            fContext.setBackground(fView.getDisplay().getSystemColor(SWT.COLOR_RED));
            fContext.fillRectangle(x, y, width, height);
            return;
        }
        localX = Math.round(localX * fView.getZoomValue());
        localY = Math.round(localY * fView.getZoomValue());
        int x = fView.contentsToViewX(localX);
        int y = fView.contentsToViewY(localY);
        Rectangle b = ((Image) image.getImage()).getBounds();
        int width = b.width;
        int height = b.height;
        if (width > maxWith) {
            width = maxWith;
        }
        if (height > maxHeight) {
            height = maxHeight;
        }
        float tempZoom = fView.getZoomValue();
        width = Math.round(width * tempZoom);
        height = Math.round(height * tempZoom);

        if (fView.isPrinting() && width > 0 && height > 0) {
            Image dbuffer = new Image(fView.getDisplay(), width, height);
            GC tempgc = new GC(dbuffer);
            tempgc.drawImage(img, 0, 0, b.width, b.height, 0, 0, width, height);
            Image dbuffer2 = new Image(fView.getDisplay(), dbuffer.getImageData());
            fContext.drawImage(dbuffer2, x, y);
            tempgc.dispose();
            dbuffer.dispose();
            dbuffer2.dispose();
        } else {
            fContext.drawImage(img, 0, 0, b.width, b.height, x, y, width, height);
        }
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int endAngle) {
        int localX = x;
        int localY = y;
        int localWidth = width;
        int localHeight = height;

        localX = Math.round(localX * fView.getZoomValue());
        localY = Math.round(localY * fView.getZoomValue());
        localWidth = Math.round(localWidth * fView.getZoomValue());
        localHeight = Math.round(localHeight * fView.getZoomValue());
        if (localWidth == 0 || localHeight == 0 || endAngle == 0) {
            return;
        }
        fContext.drawArc(fView.contentsToViewX(localX), fView.contentsToViewY(localY), localWidth, localHeight, startAngle, endAngle);
    }

    @Override
    public void setFont(IFont font) {
        if (font.getFont() != null && ((Font) font.getFont()).getFontData().length > 0) {
            FontData fontData = ((Font) font.getFont()).getFontData()[0];
            if (SDViewPref.getInstance().fontLinked() || fView.isPrinting()) {
                int h = Math.round(fontData.getHeight() * fView.getZoomValue());
                if (h > 0) {
                    fontData.setHeight(h);
                }
            }
            if (fTempFont != null) {
                fTempFont.dispose();
            }
            fTempFont = new Font(Display.getCurrent(), fontData);
            fContext.setFont(fTempFont);
        }
    }

    @Override
    public int getFontHeight(IFont font) {
        if (font.getFont() != null && (font.getFont() instanceof Font) && ((Font) font.getFont()).getFontData().length > 0) {
            Font toRestore = fContext.getFont();
            fContext.setFont((Font) font.getFont());
            int height = fContext.textExtent("lp").y;//$NON-NLS-1$
            fContext.setFont(toRestore);
            return height;
        }
        return 0;
    }

    /**
     * Returns the current font height.
     *
     * @return the current font height.
     */
    protected int getCurrentFontHeight() {
        return fContext.textExtent("lp").y; //$NON-NLS-1$
    }

    @Override
    public int getFontWidth(IFont font) {
        if ((font.getFont() != null) && (font.getFont() instanceof Font)) {
            Font toRestore = fContext.getFont();
            fContext.setFont((Font) font.getFont());
            int width = fContext.getFontMetrics().getAverageCharWidth();
            fContext.setFont(toRestore);
            return width;
        }
        return 0;
    }

    /**
     * Disposes all created resources.
     */
    public void dispose() {
        if (fTempFont != null) {
            fTempFont.dispose();
        }
        fTempFont = null;
        if (fContext != null) {
            fContext.dispose();
        }
        fContext = null;
    }

    @Override
    public float getZoom() {
        if (fView != null) {
            return fView.getZoomValue();
        }
        return 1;
    }

    @Override
    public int getLineDotStyle() {
        return SWT.LINE_DOT;
    }

    @Override
    public int getLineDashStyle() {
        return SWT.LINE_DASH;
    }

    @Override
    public int getLineSolidStyle() {
        return SWT.LINE_SOLID;
    }

    @Override
    public IColor createColor(int r, int g, int b) {
        return new ColorImpl(Display.getDefault(), r, g, b);
    }

    @Override
    public void setDrawTextWithFocusStyle(boolean focus) {
        fDrawWithFocus = focus;
    }

    /**
     * Returns the screen bounds.
     *
     * @return the screen bounds.
     */
    protected static int getVscreenBounds() {
        return fVisibleScreenBounds;
    }

    /**
     * Sets the visible screen bounds.
     *
     * @param vBounds the screen bounds.
     */
    protected static void setVscreenBounds(int vBounds) {
        fVisibleScreenBounds = vBounds;
    }

    /**
     * Returns the graphical context.
     *
     * @return the graphical context
     * @since 2.0
     */
    protected GC getGc() {
        return fContext;
    }

    /**
     * Returns the SD widget.
     *
     * @return the SD widget
     * @since 2.0
     */
    protected SDWidget getSDWidget() {
        return fView;
    }

    /**
     * Returns the gradient color.
     *
     * @return the gradient color
     * @since 2.0
     */
    protected IColor setGradientColor() {
        return fGradientColor;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Validates the polygon height
     *
     * @param localPoint array of points
     * @return height
     */
    private int validatePolygonHeight(int[] localPoint) {
        int i = 1;
        int max = 0;
        int min = Integer.MAX_VALUE;
        while (i < localPoint.length) {
            max = Math.abs(localPoint[i]) > Math.abs(max) ? localPoint[i] : max;
            min = Math.abs(localPoint[i]) < Math.abs(min) ? localPoint[i] : min;
            i+=2;
        }
        int height = max - min;
        if (min < -fVisibleScreenBounds) {
            height = height + min + fVisibleScreenBounds;
            min = -fVisibleScreenBounds;
        }
        if ((height < -fVisibleScreenBounds) && (min + height < -fVisibleScreenBounds)) {
            height = -fVisibleScreenBounds;
        } else if (height + min > fView.getVisibleHeight() + fVisibleScreenBounds) {
            height = fView.getVisibleHeight() + fVisibleScreenBounds - min;
        }
        return height;
    }
}
