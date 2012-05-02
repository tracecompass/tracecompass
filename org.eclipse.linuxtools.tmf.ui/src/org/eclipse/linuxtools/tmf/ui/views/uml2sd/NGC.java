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
package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
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
    protected GC context;
    /**
     * The reference to the sequence diagram view.
     */
    protected SDWidget view;
    /**
     * A reference to the last used font.
     */
    protected Font tempFont = null;
    /**
     * The color of the gradient.
     */
    protected IColor gradientColor = null;
    /**
     * The color of the background.
     */
    protected IColor backGround = null;
    /**
     * The color of the foreground.
     */
    protected IColor foreGround = null;
    /**
     * The current vertical screen bounds.
     */
    protected int y_;
    /**
     * The current horizontal screen bounds.
     */
    protected int x_;
    /**
     * The current yx value (view visible height - visible screen bounds) 
     */
    protected int yx;
    /**
     * The current xx value (view visible width - visible screen bounds) 
     */
    protected int xx;
    /**
     * <code>true</code> to draw with focus else <code>false</code>. 
     */
    protected boolean drawWithFocus = false;

    /**
     * The static visible screen bounds.
     */
    private static int vscreen_bounds = 0;
    
    
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
        context = gc;
        view = scrollView;
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#setLineStyle(int)
     */
    @Override
    public void setLineStyle(int style) {
        context.setLineStyle(style);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getLineStyle()
     */
    @Override
    public int getLineStyle() {
        return context.getLineStyle();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getContentsX()
     */
    @Override
    public int getContentsX() {
        return Math.round(view.getContentsX() / view.zoomValue);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getContentsY()
     */
    @Override
    public int getContentsY() {
        return Math.round(view.getContentsY() / view.zoomValue);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getVisibleWidth()
     */
    @Override
    public int getVisibleWidth() {
        return Math.round(view.getVisibleWidth() / view.zoomValue);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getVisibleHeight()
     */
    @Override
    public int getVisibleHeight() {
        return Math.round(view.getVisibleHeight() / view.zoomValue);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#contentsToViewX(int)
     */
    @Override
    public int contentsToViewX(int x) {
        return view.contentsToViewX(x);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#contentsToViewY(int)
     */
    @Override
    public int contentsToViewY(int y) {
        return view.contentsToViewY(y);
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
        y_ = vscreen_bounds;
        x_ = vscreen_bounds;
        yx = view.getVisibleHeight() + vscreen_bounds;
        xx = view.getVisibleWidth() + vscreen_bounds;
        if (y > yx) {
            c |= 0x01; // top
        } else if (y < y_) {
            c |= 0x02; // bottom
        }
        
        if (x > xx) {
            c |= 0x04; // right
        } else if (x < x_) {
            c |= 0x08; // left
        }
        return c;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawLine(int, int, int, int)
     */
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        x1 = Math.round(x1 * view.zoomValue);
        y1 = Math.round(y1 * view.zoomValue);
        x2 = Math.round(x2 * view.zoomValue);
        y2 = Math.round(y2 * view.zoomValue);
        x1 = view.contentsToViewX(x1);
        y1 = view.contentsToViewY(y1);
        x2 = view.contentsToViewX(x2);
        y2 = view.contentsToViewY(y2);

        byte code1 = code(x1, y1);
        byte code2 = code(x2, y2);
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
                    x = x1 + ((x2 - x1) * (yx - y1)) / (y2 - y1);
                    y = yx;
                } else if ((codex & 0x02) != 0) { // bottom
                    x = x1 + ((x2 - x1) * (y_ - y1)) / (y2 - y1);
                    y = y_;
                } else if ((codex & 0x04) != 0) { // right
                    y = y1 + ((y2 - y1) * (xx - x1)) / (x2 - x1);
                    x = xx;
                } else if ((codex & 0x08) != 0) { // left
                    y = y1 + ((y2 - y1) * (x_ - x1)) / (x2 - x1);
                    x = x_;
                }

                if (codex == code1) {
                    x1 = x;
                    y1 = y;
                    code1 = code(x1, y1);
                } else {
                    x2 = x;
                    y2 = y;
                    code2 = code(x2, y2);
                }
            }
        } while (!end);

        if (draw) {
            context.drawLine(x1, y1, x2, y2);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawRectangle(int, int, int, int)
     */
    @Override
    public void drawRectangle(int x, int y, int width, int height) {
        x = Math.round(x * view.zoomValue);
        // Workaround to avoid problems for some special cases (not very nice)
        if (y != getContentsY()) {
            y = Math.round(y * view.zoomValue);
            y = view.contentsToViewY(y);
        } else
            y = 0;
        width = Math.round(width * view.zoomValue);
        height = Math.round(height * view.zoomValue);
        x = view.contentsToViewX(x);

        if (x < -vscreen_bounds) {
            width = width + x + vscreen_bounds;
            x = -vscreen_bounds;
        }
        if (y < -vscreen_bounds) {
            height = height + y + vscreen_bounds;
            y = -vscreen_bounds;
        }
        if ((width < -vscreen_bounds) && (x + width < -vscreen_bounds)) {
            width = -vscreen_bounds;
        } else if (width + x > view.getVisibleWidth() + vscreen_bounds) {
            width = view.getVisibleWidth() + vscreen_bounds - x;
        }
        if ((height < -vscreen_bounds) && (y + height < -vscreen_bounds)) {
            height = -vscreen_bounds;
        } else if (height + y > view.getVisibleHeight() + vscreen_bounds) {
            height = view.getVisibleHeight() + vscreen_bounds - y;
        }
        context.drawRectangle(x, y, width, height);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawFocus(int, int, int, int)
     */
    @Override
    public void drawFocus(int x, int y, int width, int height) {
        IColor bC = getBackground();
        IColor fC = getForeground();

        if (width < 0) {
            x = x + width;
            width = -width;
        }

        if (height < 0) {
            y = y + height;
            height = -height;
        }

        x = Math.round(x * view.zoomValue);
        y = Math.round(y * view.zoomValue);
        width = Math.round(width * view.zoomValue);
        height = Math.round(height * view.zoomValue);

        setForeground(Frame.getUserPref().getForeGroundColorSelection());
        setBackground(Frame.getUserPref().getBackGroundColorSelection());

        context.drawFocus(view.contentsToViewX(x - 1), view.contentsToViewY(y - 1), width + 3, height + 3);

        setBackground(bC);
        setForeground(fC);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#fillPolygon(int[])
     */
    @Override
    public void fillPolygon(int[] points) {
        int len = (points.length / 2) * 2;
        int[] localPoint = new int[len];
        for (int i = 0; i < len; i++) {
            localPoint[i] = view.contentsToViewX(Math.round(points[i] * view.zoomValue));
            i++;
            localPoint[i] = view.contentsToViewY(Math.round(points[i] * view.zoomValue));
        }
        
        if (validatePolygonHeight(localPoint) <= 0)
            return;
        
        context.fillPolygon(localPoint);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawPolygon(int[])
     */
    @Override
    public void drawPolygon(int[] points) {
        int len = (points.length / 2) * 2;
        int[] localPoint = new int[len];
        for (int i = 0; i < len; i++) {
            localPoint[i] = view.contentsToViewX(Math.round(points[i] * view.zoomValue));
            i++;
            localPoint[i] = view.contentsToViewY(Math.round(points[i] * view.zoomValue));
        }
        
        if (validatePolygonHeight(localPoint) <= 0) {
            return;
        }

        context.drawPolygon(localPoint);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#fillRectangle(int, int, int, int)
     */
    @Override
    public void fillRectangle(int x, int y, int width, int height) {
        x = Math.round(x * view.zoomValue);
        // Workaround to avoid problems for some special cases (not very nice)
        if (y != getContentsY()) {
            y = Math.round(y * view.zoomValue);
            y = view.contentsToViewY(y) + 1;
        } else {
            y = 1;
        }
        width = Math.round(width * view.zoomValue) - 1;
        height = Math.round(height * view.zoomValue) - 1;
        x = view.contentsToViewX(x) + 1;
        if (x < -vscreen_bounds) {
            width = width + x + vscreen_bounds;
            x = -vscreen_bounds;
        }
        if (y < -vscreen_bounds) {
            height = height + y + vscreen_bounds;
            y = -vscreen_bounds;
        }
        if ((width < -vscreen_bounds) && (x + width < -vscreen_bounds)) {
            width = -vscreen_bounds;
        } else if (width + x > view.getVisibleWidth() + vscreen_bounds) {
            width = view.getVisibleWidth() + vscreen_bounds - x;
        }
        if ((height < -vscreen_bounds) && (y + height < -vscreen_bounds)) {
            height = -vscreen_bounds;
        } else if (height + y > view.getVisibleHeight() + vscreen_bounds) {
            height = view.getVisibleHeight() + vscreen_bounds - y;
        }
        context.fillRectangle(x, y, width, height);

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#fillGradientRectangle(int, int, int, int, boolean)
     */
    @Override
    public void fillGradientRectangle(int x, int y, int width, int height, boolean vertical) {
        x = Math.round(x * view.zoomValue);
        y = Math.round(y * view.zoomValue);
        width = Math.round(width * view.zoomValue);
        height = Math.round(height * view.zoomValue);
        IColor tempColor = foreGround;
        setForeground(gradientColor);
        x = view.contentsToViewX(x);
        y = view.contentsToViewY(y);

        if (x < -vscreen_bounds) {
            width = width + x + vscreen_bounds;
            x = -vscreen_bounds;
        }
        if (y < -vscreen_bounds) {
            height = height + y + vscreen_bounds;
            y = -vscreen_bounds;
        }

        if ((width < -vscreen_bounds) && (x + width < -vscreen_bounds)) {
            width = -vscreen_bounds;
        } else if (width + x > view.getVisibleWidth() + vscreen_bounds) {
            width = view.getVisibleWidth() + vscreen_bounds - x;
        }
        if ((height < -vscreen_bounds) && (y + height < -vscreen_bounds)) {
            height = -vscreen_bounds;
        } else if (height + y > view.getVisibleHeight() + vscreen_bounds) {
            height = view.getVisibleHeight() + vscreen_bounds - y;
        }
        if (vertical) {
            context.fillGradientRectangle(x, y, width, height, vertical);
        }
        else {
            context.fillGradientRectangle(x + width, y, -width, height + 1, vertical);
        }
        setForeground(tempColor);
    }

    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#textExtent(java.lang.String)
     */
    @Override
    public int textExtent(String name) {
        return ((Point) (context.textExtent(name))).x;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawText(java.lang.String, int, int, boolean)
     */
    @Override
    public void drawText(String string, int x, int y, boolean trans) {
        x = Math.round(x * view.zoomValue);
        y = Math.round(y * view.zoomValue);
        context.drawText(string, view.contentsToViewX(x), view.contentsToViewY(y), trans);
        if (drawWithFocus) {
            Point r = context.textExtent(string);
            context.drawFocus(x - 1, y - 1, r.x + 2, r.y + 2);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawText(java.lang.String, int, int)
     */
    @Override
    public void drawText(String string, int x, int y) {
        x = Math.round(x * view.zoomValue);
        y = Math.round(y * view.zoomValue);
        context.drawText(string, view.contentsToViewX(x), view.contentsToViewY(y), true);
        if (drawWithFocus) {
            Point r = context.textExtent(string);
            context.drawFocus(x - 1, y - 1, r.x + 2, r.y + 2);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#fillOval(int, int, int, int)
     */
    @Override
    public void fillOval(int x, int y, int width, int height) {
        x = Math.round(x * view.zoomValue);
        y = Math.round(y * view.zoomValue);
        width = Math.round(width * view.zoomValue);
        height = Math.round(height * view.zoomValue);
        context.fillOval(view.contentsToViewX(x), view.contentsToViewY(y), width, height);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getBackground()
     */
    @Override
    public IColor getBackground() {
        if ((backGround != null) && (backGround.getColor() instanceof Color) && (!((Color) (backGround.getColor())).isDisposed())) {
            return backGround;
        }
        return ColorImpl.getSystemColor(SWT.COLOR_WHITE);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getForeground()
     */
    @Override
    public IColor getForeground() {
        if ((foreGround != null) && (foreGround.getColor() instanceof Color) && (!((Color) (foreGround.getColor())).isDisposed())) {
            return foreGround;
        }
        return ColorImpl.getSystemColor(SWT.COLOR_WHITE);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#setBackground(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor)
     */
    @Override
    public void setBackground(IColor color) {
        if (color == null) {
            return;
        }
        if (color.getColor() instanceof Color) {
            context.setBackground((Color) color.getColor());
            backGround = color;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#setForeground(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor)
     */
    @Override
    public void setForeground(IColor color) {
        if (color == null) {
            return;
        }
        if (color.getColor() instanceof Color) {
            Color c = (Color) color.getColor();
            if (!c.isDisposed()) {
                context.setForeground(c);
                foreGround = color;
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#setGradientColor(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor)
     */
    @Override
    public void setGradientColor(IColor color) {
        if (color == null) {
            return;
        }
        if (color.getColor() instanceof Color) {
            gradientColor = color;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#setLineWidth(int)
     */
    @Override
    public void setLineWidth(int width) {
        if (view.isPrinting()) {
            context.setLineWidth(width * 2);
        }
        else {
            context.setLineWidth(width);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getLineWidth()
     */
    @Override
    public int getLineWidth() {
        return context.getLineWidth();
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
        Point r = context.textExtent(string);
        if (!isTransparent) {
            context.fillRectangle(x, y, r.x, r.y);
        }
        context.drawText(string, x, y, isTransparent);
        if ((drawWithFocus) && (string.length() > 1)) {
            context.drawFocus(x - 1, y - 1, r.x + 2, r.y + 2);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawTextTruncatedCentred(java.lang.String, int, int, int, int, boolean)
     */
    @Override
    public void drawTextTruncatedCentred(String name, int _x, int _y, int width, int height, boolean trans) {
        Point tx = context.textExtent(name);
        _x = Math.round(_x * view.zoomValue);
        int y = 0;
        // Workaround to avoid round problems for some special cases (not very nice)
        if (_y != getContentsY()) {
            _y = Math.round(_y * view.zoomValue);
            y = view.contentsToViewY(_y);
        }
        width = Math.round(width * view.zoomValue);
        height = Math.round(height * view.zoomValue);
        int x = view.contentsToViewX(_x);
        if (tx.y > height) {
            return;
        }
        
        // Adjust height and y
        if (y < -vscreen_bounds) {
            height = height + y + vscreen_bounds;
            y = -vscreen_bounds;
        }
        if ((height < -vscreen_bounds) && (y + height < -vscreen_bounds)) {
            height = -vscreen_bounds;
        } else if (height + y > view.getVisibleHeight() + vscreen_bounds) {
            height = view.getVisibleHeight() + vscreen_bounds - y;
        }
        
        if (tx.x <= width) {
            localDrawText(name, x + 1 + (width - tx.x) / 2, y + 1 + (height - tx.y) / 2, trans);
        } else {
            String nameToDisplay = name;
            for (int i = name.length() - 1; i >= 0 && context.textExtent(nameToDisplay).x >= width; i--) {
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
            localDrawText(nameToDisplay, x + 1 + (width - context.textExtent(nameToDisplay).x) / 2, y + 1 + (height - context.textExtent(nameToDisplay).y) / 2, trans);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawTextTruncated(java.lang.String, int, int, int, int, boolean)
     */
    @Override
    public void drawTextTruncated(String name, int _x, int _y, int width, int height, boolean trans) {
        _x = Math.round(_x * view.zoomValue);
        _y = Math.round(_y * view.zoomValue);
        width = Math.round(width * view.zoomValue);
        height = Math.round(height * view.zoomValue);
        int x = view.contentsToViewX(_x);
        int y = view.contentsToViewY(_y);
        if (context.textExtent(name).x <= width) {
            localDrawText(name, x + 1, y + 1 + height, trans);
        } else {
            String nameToDisplay = name;
            for (int i = name.length() - 1; i >= 0 && context.textExtent(nameToDisplay).x >= width; i--) {
                nameToDisplay = name.substring(0, i);
            }
            int dotCount = 0;
            for (int i = 1; i <= 3 && nameToDisplay.length() - i > 0; i++)
                dotCount++;
            nameToDisplay = nameToDisplay.substring(0, nameToDisplay.length() - dotCount);

            StringBuffer buf = new StringBuffer(nameToDisplay);

            for (int i = 0; i < dotCount; i++) {
                buf.append("."); //$NON-NLS-1$
            }
            nameToDisplay = buf.toString();
            localDrawText(nameToDisplay, x + 1, y + 1 + height, trans);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawImage(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IImage, int, int, int, int)
     */
    @Override
    public void drawImage(IImage image, int _x, int _y, int maxWith, int maxHeight) {
        Image img = null;
        if (image != null && image.getImage() instanceof Image)
            img = (Image) image.getImage();
        else {
            _x = Math.round(_x * view.zoomValue);
            _y = Math.round(_y * view.zoomValue);
            int x = view.contentsToViewX(_x);
            int y = view.contentsToViewY(_y);
            float tempZoom = view.zoomValue;
            int width = Math.round(maxWith * tempZoom);
            int height = Math.round(maxHeight * tempZoom);
            context.setBackground(view.getDisplay().getSystemColor(SWT.COLOR_RED));
            context.fillRectangle(x, y, width, height);
            return;
        }
        _x = Math.round(_x * view.zoomValue);
        _y = Math.round(_y * view.zoomValue);
        int x = view.contentsToViewX(_x);
        int y = view.contentsToViewY(_y);
        Rectangle b = ((Image) image.getImage()).getBounds();
        int width = b.width;
        int height = b.height;
        if (width > maxWith) {
            width = maxWith;
        }
        if (height > maxHeight) {
            height = maxHeight;
        }
        float tempZoom = view.zoomValue;
        width = Math.round(width * tempZoom);
        height = Math.round(height * tempZoom);

        if (view.printing && width > 0 && height > 0) {
            Image dbuffer = new Image(view.getDisplay(), width, height);
            GC tempgc = new GC(dbuffer);
            tempgc.drawImage(img, 0, 0, b.width, b.height, 0, 0, width, height);
            Image dbuffer2 = new Image(view.getDisplay(), dbuffer.getImageData());
            context.drawImage(dbuffer2, x, y);
            tempgc.dispose();
            dbuffer.dispose();
            dbuffer2.dispose();
        } else {
            context.drawImage(img, 0, 0, b.width, b.height, x, y, width, height);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#drawArc(int, int, int, int, int, int)
     */
    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int endAngle) {
        x = Math.round(x * view.zoomValue);
        y = Math.round(y * view.zoomValue);
        width = Math.round(width * view.zoomValue);
        height = Math.round(height * view.zoomValue);
        if (width == 0 || height == 0 || endAngle == 0) {
            return;
        }
        context.drawArc(view.contentsToViewX(x), view.contentsToViewY(y), width, height, startAngle, endAngle);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#setFont(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont)
     */
    @Override
    public void setFont(IFont font) {
        if (font.getFont() != null && ((Font) font.getFont()).getFontData().length > 0) {
            FontData fontData = ((Font) font.getFont()).getFontData()[0];
            if (SDViewPref.getInstance().fontLinked() || view.printing) {
                int h = Math.round(fontData.getHeight() * view.zoomValue);
                if (h > 0) {
                    fontData.setHeight(h);
                }
            }
            if (tempFont != null) {
                tempFont.dispose();
            }
            tempFont = new Font(Display.getCurrent(), fontData);
            context.setFont(tempFont);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getFontHeight(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont)
     */
    @Override
    public int getFontHeight(IFont font) {
        if (font.getFont() != null && (font.getFont() instanceof Font) && ((Font) font.getFont()).getFontData().length > 0) {
            Font toRestore = context.getFont();
            context.setFont((Font) font.getFont());
            int height = context.textExtent("lp").y;//$NON-NLS-1$
            context.setFont(toRestore);
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
        return context.textExtent("lp").y; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getFontWidth(org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IFont)
     */
    @Override
    public int getFontWidth(IFont font) {
        if ((font.getFont() != null) && (font.getFont() instanceof Font)) {
            Font toRestore = context.getFont();
            context.setFont((Font) font.getFont());
            int width = context.getFontMetrics().getAverageCharWidth();
            context.setFont(toRestore);
            return width;
        }
        return 0;
    }

    /**
     * Disposes all created resources.
     */
    public void dispose() {
        if (tempFont != null) {
            tempFont.dispose();
        }
        tempFont = null;
        if (context != null) {
            context.dispose();
        }
        context = null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getZoom()
     */
    @Override
    public float getZoom() {
        if (view != null) {
            return view.zoomValue;
        } else {
            return 1;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getLineDotStyle()
     */
    @Override
    public int getLineDotStyle() {
        return SWT.LINE_DOT;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getLineDashStyle()
     */
    @Override
    public int getLineDashStyle() {
        return SWT.LINE_DASH;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#getLineSolidStyle()
     */
    @Override
    public int getLineSolidStyle() {
        return SWT.LINE_SOLID;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#createColor(int, int, int)
     */
    @Override
    public IColor createColor(int r, int g, int b) {
        return new ColorImpl(Display.getDefault(), r, g, b);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC#setDrawTextWithFocusStyle(boolean)
     */
    @Override
    public void setDrawTextWithFocusStyle(boolean focus) {
        drawWithFocus = focus;
    }
    
    /**
     * Returns the screen bounds.
     * 
     * @return the screen bounds.
     */
    protected static int getVscreenBounds() {
        return vscreen_bounds;
    }

    /**
     * Sets the visible screen bounds.
     * 
     * @param vBounds the screen bounds.
     */
    protected static void setVscreenBounds(int vBounds) {
        vscreen_bounds = vBounds;
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
        if (min < -vscreen_bounds) {
            height = height + min + vscreen_bounds;
            min = -vscreen_bounds;
        }
        if ((height < -vscreen_bounds) && (min + height < -vscreen_bounds)) {
            height = -vscreen_bounds;
        } else if (height + min > view.getVisibleHeight() + vscreen_bounds) {
            height = view.getVisibleHeight() + vscreen_bounds - min;
        }
        return height;
    }
}
