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

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;

/**
 * ScrollView widget provides a scrolling area with on-demand scroll bars.
 * Overview scrollable panel can be used (@see setOverviewEnabled()).
 *
 * @author Eric Miravete
 * @version 1.0
 */
public class ScrollView extends Composite {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Scroll bar mode AUTO
     */
    public static final int AUTO = 0;
    /**
     * Scroll bar mode ALWAYS_ON
     */
    public static final int ALWAYS_ON = 1;
    /**
     * Scroll bar mode ALWAYS_OFF
     */
    public static final int ALWAYS_OFF = 2;
    /**
     * Bit mask for visible vertical scroll bar
     */
    public static final int VBAR = 0x01;
    /**
     * Bit mask for visible horizontal scroll bar
     */
    public static final int HBAR = 0x02;

    private static final int DEFAULT_H_SCROLL_INCREMENT = 10;
    private static final int DEFAULT_V_SCROLL_INCREMENT = 10;
    private static final int DEFAULT_AUTO_SCROLL_PERIOD = 75;
    private static final int DEFAULT_OVERVIEW_SIZE = 100;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Value of the contents height property.
     */
    private int fContentsHeight = 0;
    /**
     * Value of the contents width property.
     */
    private int fContentsWidth = 0;
    /**
     * Value of the contents x coordinate property
     */
    private int fContentsX = 0;
    /**
     * Value of the contents y coordinate property
     */
    private int fContentsY = 0;
    /**
     * Scroll bar mode of horizontal scroll bar.
     */
    private int fHorScrollbarMode = AUTO;
    /**
     * Scroll bar mode of vertical scroll bar.
     */
    private int fVertScrollbarMode = AUTO;
    /**
     * Increment for the horizontal scroll bar.
     */
    private int fHorScrollbarIncrement = DEFAULT_H_SCROLL_INCREMENT;
    /**
     * Increment for the vertical scroll bar.
     */
    private int fVertScrollbarIncrement = DEFAULT_V_SCROLL_INCREMENT;
    /**
     * Flag whether auto scroll is enabled or not.
     */
    private boolean fAutoScrollEnabled = true;
    /**
     * Value of the auto scroll period.
     */
    private int fAutoScrollPeriod = DEFAULT_AUTO_SCROLL_PERIOD;
    /**
     * The local paint listener reference.
     */
    private PaintListener fLocalPaintListener = null;
    /**
     * The local mouse move listener reference.
     */
    private MouseMoveListener fLocalMouseMoveListener = null;
    /**
     * The local mouse listener reference.
     */
    private MouseListener fLocalMouseListener = null;
    /**
     * The local control listener reference.
     */
    private ControlListener fLocalControlListener = null;
    /**
     * The local key listener reference.
     */
    private KeyListener fLocalKeyListener = null;
    // Canvas for vertical/horizontal Scroll Bar only ... because new ScrollBar() does works.
    /**
     * Canvas for horizontal scroll bar.
     */
    private Canvas fHorScrollBar;
    /**
     * Canvas for vertical scroll bar.
     */
    private Canvas fVertScrollBar;
    /**
     * Canvas for the view control.
     */
    private Canvas fViewControl;
    /**
     * Control used in the bottom right corner @see setCornerControl() and @see setOverviewEnabled(true)
     */
    private Control fCornerControl;
    /**
     * Size of overview widget.
     */
    private int fOverviewSize = DEFAULT_OVERVIEW_SIZE; // default size for overview
    /**
     * Timer for auto_scroll feature
     */
    private AutoScroll fAutoScroll = null;
    /**
     * TimerTask for auto_scroll feature !=null when auto scroll is running
     */
    private Timer fAutoScrollTimer = null;
    /**
     * where mouse down appear on contents area (x coordinate)
     */
    private int fMouseDownX = -1;
    /**
     * where mouse down appear on contents area (y coordinate)
     */
    private int fMousDownY = -1;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Create a ScrollView, child of composite c. Both scroll bar have the mode AUTO. Auto scroll feature is enabled
     * using a delay of 250ms. Overview feature is not enabled by default (use setOverviewEnabled()).
     *
     * @param c The parent composite
     * @param style The SWT style bits @see SWT
     */
    public ScrollView(Composite c, int style) {
        this(c, style, true);
    }

    /**
     * Create a ScrollView, child of composite c. Both scroll bar have the mode AUTO. Auto scroll feature is enabled
     * using a delay of 250ms. Overview feature is not enabled by default (use setOverviewEnabled()).
     *
     * @param c The parent composite.
     * @param style The SWT style bits @see SWT
     * @param mouseWheel Flag to force scrollView to handles mouse wheel
     */
    public ScrollView(Composite c, int style, boolean mouseWheel) {
        super(c, SWT.NONE);

        fHorScrollBar = new Canvas(this, SWT.H_SCROLL);
        if (mouseWheel) {
            // force scroll bar to get mouse wheel, those scrollbar will be hidden
            fViewControl = new Canvas(this, style | SWT.H_SCROLL | SWT.V_SCROLL);
        } else {
            fViewControl = new Canvas(this, style);
        }
        fViewControl.setBackground(getBackground());
        // hide scroll bar as their are replaced by fHorScrollBar and fVertScrollBar.
        if (mouseWheel) {
            fViewControl.getVerticalBar().setVisible(false);
            fViewControl.getHorizontalBar().setVisible(false);
        }
        fVertScrollBar = new Canvas(this, SWT.V_SCROLL);

        setLayout(new SVLayout());

        fLocalPaintListener = new PaintListener() {
            @Override
            public void paintControl(PaintEvent event) {
                // use clipping, to reduce cost of paint.
                Rectangle r = event.gc.getClipping();
                int cx = viewToContentsX(r.x);
                int cy = viewToContentsY(r.y);
                drawContents(event.gc, cx, cy, r.width, r.height);
            }
        };
        fViewControl.addPaintListener(fLocalPaintListener);

        fLocalMouseMoveListener = new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                int ox = e.x, oy = e.y;
                e.x = viewToContentsX(e.x);
                e.y = viewToContentsY(e.y);
                contentsMouseMoveEvent(e);
                e.x = ox;
                e.y = oy;
            }
        };

        fViewControl.addMouseMoveListener(fLocalMouseMoveListener);

        MouseTrackListener localMouseTrackListener = new MouseTrackListener() {
            @Override
            public void mouseEnter(MouseEvent e) {
                int ox = e.x, oy = e.y;
                e.x = viewToContentsX(e.x);
                e.y = viewToContentsY(e.y);
                contentsMouseEnter(e);
                e.x = ox;
                e.y = oy;
            }

            @Override
            public void mouseHover(MouseEvent e) {
                int ox = e.x, oy = e.y;
                e.x = viewToContentsX(e.x);
                e.y = viewToContentsY(e.y);
                contentsMouseHover(e);
                e.x = ox;
                e.y = oy;
            }

            @Override
            public void mouseExit(MouseEvent e) {
                int ox = e.x, oy = e.y;
                e.x = viewToContentsX(e.x);
                e.y = viewToContentsY(e.y);
                contentsMouseExit(e);
                e.x = ox;
                e.y = oy;
            }

        };

        fViewControl.addMouseTrackListener(localMouseTrackListener);

        fLocalMouseListener = new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                int ox = e.x, oy = e.y;
                e.x = viewToContentsX(e.x);
                e.y = viewToContentsY(e.y);
                contentsMouseDoubleClickEvent(e);
                e.x = ox;
                e.y = oy;
            }

            @Override
            public void mouseDown(MouseEvent e) {
                int ox = e.x, oy = e.y;
                e.x = viewToContentsX(e.x);
                fMouseDownX = e.x;
                e.y = viewToContentsY(e.y);
                fMousDownY = e.y;
                contentsMouseDownEvent(e);
                e.x = ox;
                e.y = oy;
            }

            @Override
            public void mouseUp(MouseEvent e) {
                int ox = e.x, oy = e.y;
                e.x = viewToContentsX(e.x);
                e.y = viewToContentsY(e.y);
                contentsMouseUpEvent(e);
                e.x = ox;
                e.y = oy;
                // here because class extending me can catch mouse Up and want to scroll...
                fMouseDownX = -1;
                fMousDownY = -1;
            }
        };
        fViewControl.addMouseListener(fLocalMouseListener);

        fLocalKeyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyPressedEvent(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keyReleasedEvent(e);
            }
        };

        fViewControl.addKeyListener(fLocalKeyListener);

        getVerticalBar().addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setContentsPos(fContentsX, getVerticalBar().getSelection());
                // need to change "hidden" vertical bar value ?
                // force focus on fViewControl so we got future mouse wheel's scroll events
                if (!fViewControl.isFocusControl()) {
                    fViewControl.setFocus();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        if (fViewControl.getVerticalBar() != null) {
            // add fViewControl hidden scrollbar listener to get mouse wheel ...
            fViewControl.getVerticalBar().addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ScrollBar b = fViewControl.getVerticalBar();
                    setContentsPos(fContentsX, b.getSelection());
                    // change "real" vertical bar selection too
                    getVerticalBar().setSelection(b.getSelection());
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
        }
        getHorizontalBar().addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setContentsPos(getHorizontalBar().getSelection(), fContentsY);
                // need to change "real" horizontal bar too ?
                // force focus on fViewControl so we got future mouse wheel's scroll events
                if (!fViewControl.isFocusControl()) {
                    fViewControl.setFocus();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        if (fViewControl.getHorizontalBar() != null) {
            fViewControl.getHorizontalBar().addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ScrollBar b = fViewControl.getHorizontalBar();
                    setContentsPos(b.getSelection(), fContentsY);
                    // change "real" vertical bar selection too
                    getHorizontalBar().setSelection(b.getSelection());
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
        }
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public boolean setFocus() {
        return fViewControl.forceFocus();
    }

    @Override
    public void setCursor(Cursor cursor) {
        fViewControl.setCursor(cursor);
    }

    @Override
    public void dispose() {
        if (fAutoScroll != null) {
            fAutoScroll.cancel();
            fAutoScroll = null;
        }
        if (fViewControl != null) {
            fViewControl.dispose();
        }
        fViewControl = null;
        if (fVertScrollBar != null) {
            fVertScrollBar.dispose();
        }
        fVertScrollBar = null;
        if (fHorScrollBar != null) {
            fHorScrollBar.dispose();
        }
        fHorScrollBar = null;
        if (fCornerControl != null) {
            Object data = fCornerControl.getData();
            if (data instanceof Overview) {
                ((Overview) data).dispose();
            }
            fCornerControl.dispose();
            fCornerControl = null;
        }
        super.dispose();
    }

    @Override
    public Rectangle getClientArea() {
        Rectangle area = fViewControl.getClientArea();
        /* Clamp the size of the returned area to 1x1 minimum */
        area.width = Math.max(area.width, 1);
        area.height = Math.max(area.height, 1);
        return area;
    }

    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        fViewControl.setBackground(c);
    }

    @Override
    public void setToolTipText(String text) {
        fViewControl.setToolTipText(text);
    }

    /**
     * Draw overview area, @see setOverviewEnabled. By default draw a rectangle corresponding to the visible area of
     * scroll view. You can redefine this method to draw the contents as drawContents does... ...in an other magnify
     * factor.
     *
     * @param gc GC to used to draw.
     * @param r Rectangle corresponding to the client area of overview.
     */
    protected void drawOverview(GC gc, Rectangle r) {
        int x = (int) (r.width * fContentsX / (float) fContentsWidth);
        int y = (int) (r.height * fContentsY / (float) fContentsHeight);
        int vw = getVisibleWidth();
        int vh = getVisibleHeight();
        int w = r.width - 1;
        if (fContentsWidth > vw) {
            w = (int) (r.width * vw / (float) fContentsWidth);
        }
        int h = r.height - 1;
        if (fContentsHeight > vh) {
            h = (int) (r.height * vh / (float) fContentsHeight);
        }

        gc.setForeground(getForeground());
        // too small rectangle ?
        if (w < 5 || h < 5) {
            // use a cross ...
            gc.drawLine(x, 0, x, r.height);
            gc.drawLine(0, y, r.width, y);
        } else {
            gc.drawRectangle(x, y, w, h);
        }
    }

    /**
     * Remove the local Listener and add the new listener.
     *
     * @param nlistener the new listener
     */
    public void replaceControlListener(ControlListener nlistener) {
        if (fLocalControlListener != null) {
            removeControlListener(fLocalControlListener);
            fLocalControlListener = null;
        }
        addControlListener(nlistener);
    }

    /**
     * Remove the local Listener and add the new listener.
     *
     * @param nlistener the new listener
     */
    public void replaceKeyListener(KeyListener nlistener) {
        if (fLocalKeyListener != null) {
            removeKeyListener(fLocalKeyListener);
            fLocalKeyListener = null;
        }
        addKeyListener(nlistener);
    }

    /**
     * Remove the local Listener and add the new listener.
     *
     * @param nlistener the new listener
     */
    public void replaceMouseListener(MouseListener nlistener) {
        if (fLocalMouseListener != null) {
            removeMouseListener(fLocalMouseListener);
            fLocalMouseListener = null;
        }
        fViewControl.addMouseListener(nlistener);
    }

    /**
     * Remove the local Listener and add the new listener.
     *
     * @param nlistener the new listener
     */
    public void replaceMouseMoveListener(MouseMoveListener nlistener) {
        if (fLocalMouseMoveListener != null) {
            removeMouseMoveListener(fLocalMouseMoveListener);
            fLocalMouseMoveListener = null;
        }
        fViewControl.addMouseMoveListener(nlistener);
    }

    /**
     * Remove the local Listener and add the new listener.
     *
     * @param nlistener the new listener
     */
    public void replacePaintListener(PaintListener nlistener) {
        if (fLocalPaintListener != null) {
            removePaintListener(fLocalPaintListener);
            fLocalPaintListener = null;
        }
        fViewControl.addPaintListener(nlistener);
    }

    /**
     * Access method for the contentsHeight property.
     *
     * @return the current value of the contentsHeight property
     */
    public int getContentsHeight() {
        return fContentsHeight;
    }

    /**
     * Access method for the contentsWidth property.
     *
     * @return the current value of the contentsWidth property
     */
    public int getContentsWidth() {
        return fContentsWidth;
    }

    /**
     * Access method for the contentsX property.
     *
     * @return the current value of the contentsX property
     */
    public int getContentsX() {
        return fContentsX;
    }

    /**
     * Access method for the contentsY property.
     *
     * @return the current value of the contentsY property
     */
    public int getContentsY() {
        return fContentsY;
    }

    /**
     * Determines if the dragAutoScroll property is true.
     *
     * @return <code>true<code> if the dragAutoScroll property is true
     */
    public boolean isDragAutoScroll() {
        return fAutoScrollEnabled;
    }

    /**
     * Sets the value of the dragAutoScroll property.
     *
     * @param aDragAutoScroll the new value of the dragAutoScroll property
     */
    public void setDragAutoScroll(boolean aDragAutoScroll) {
        fAutoScrollEnabled = aDragAutoScroll;
        if (!fAutoScrollEnabled && (fAutoScroll != null)) {
            fAutoScroll.cancel();
            fAutoScroll = null;
        }
    }

    /**
     * Change delay (in millisec) used for auto scroll feature.
     *
     * @param period new period between to auto scroll
     */
    public void setDragAutoScrollPeriod(int period) {
        fAutoScrollPeriod = Math.max(0, period);
    }

    /**
     * Return auto scroll period.
     *
     * @return The period
     */
    public int getDragAutoScrollPeriod() {
        return fAutoScrollPeriod;
    }

    /**
     * Access method for the hScrollBarMode property.
     *
     * @return the current value of the hScrollBarMode property
     */
    public int getHScrollBarMode() {
        return fHorScrollbarMode;
    }

    /**
     * Sets the value of the hScrollBarMode property.
     *
     * @param aHScrollBarMode the new value of the hScrollBarMode property
     */
    public void setHScrollBarMode(int aHScrollBarMode) {
        fHorScrollbarMode = aHScrollBarMode;
    }

    /**
     * Access method for the vScrollBarMode property.
     *
     * @return the current value of the vScrollBarMode property
     */
    public int getVScrollBarMode() {
        return fVertScrollbarMode;
    }

    /**
     * Sets the value of the vScrollBarMode property.
     *
     * @param aVScrollBarMode the new value of the vScrollBarMode property
     */
    public void setVScrollBarMode(int aVScrollBarMode) {
        fVertScrollbarMode = aVScrollBarMode;
    }

    /**
     * Return horizontal scroll bar increment, default:1
     *
     * @return The increment
     */
    public int getHScrollBarIncrement() {
        return fHorScrollbarIncrement;
    }

    /**
     * Return vertical scroll bar increment, default:1
     *
     * @return The increment
     */
    public int getVScrollBarIncrement() {
        return fVertScrollbarIncrement;
    }

    /**
     * Change horizontal scroll bar increment, minimum:1. Page increment is
     * always set to visible width.
     *
     * @param inc
     *            Increment value to set
     */
    public void setHScrollBarIncrement(int inc) {
        fHorScrollbarIncrement = Math.max(1, inc);
    }

    /**
     * Change vertical scroll bar increment, minimum:1. Page increment is always
     * set to visible height.
     *
     * @param inc
     *            Increment value to set
     */
    public void setVScrollBarIncrement(int inc) {
        fVertScrollbarIncrement = Math.max(1, inc);
    }

    /**
     * Enable or disable overview feature. Enabling overview, dispose and replace existing corner control by a button.
     * Clicking in it open overview, move mouse cursor holding button to move scroll view and release button to hide
     * overview. Tip: hold control and/or shift key while moving mouse when overview is open made fine scroll.
     *
     * @param value true to engage overview feature
     */
    public void setOverviewEnabled(boolean value) {
        if (isOverviewEnabled() == value) {
            return;
        }

        Control cc = null;
        if (value) {
            Button b = new Button(this, SWT.NONE);
            b.setText("+"); //$NON-NLS-1$
            Overview ovr = new Overview();
            ovr.useControl(b);
            b.setData(ovr);
            cc = b;
            b.setToolTipText(Messages.SequenceDiagram_OpenOverviewTooltip);
        }
        setCornerControl(cc);
    }

    /**
     * Change overview size (at ratio 1:1), default is 100
     *
     * @param size
     *            The new size
     */
    public void setOverviewSize(int size) {
        fOverviewSize = Math.abs(size);
    }

    /**
     * Returns whether the overview is enabled or not.
     *
     * @return true is overview feature is enabled
     */
    public boolean isOverviewEnabled() {
        if (fCornerControl instanceof Button) {
            Object data = ((Button) fCornerControl).getData();
            // overview alreay
            if (data instanceof Overview) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the overview size at ratio 1:1.
     *
     * @return current overview size at ratio 1:1
     */
    public int getOverviewSize() {
        return fOverviewSize;
    }

    /**
     * Returns control used to display view (might not be this object). Use this control to add/remove listener on the
     * draw area.
     *
     * @return control used to display view (might not be this object).
     */
    public Control getViewControl() {
        return fViewControl;
    }

    /**
     * Called when the mouse enter the ScrollView area
     *
     * @param e
     *            Mouse event
     */
    protected void contentsMouseExit(MouseEvent e) {
    }

    /**
     * Called when the mouse enter the ScrollView area after and system defined
     * time
     *
     * @param e
     *            Mouse event
     */
    protected void contentsMouseHover(MouseEvent e) {
    }

    /**
     * Called when the mouse enter the ScrollView area
     *
     * @param e
     *            Mouse event
     */
    protected void contentsMouseEnter(MouseEvent e) {
    }

    /**
     * Called when user double on contents area.
     *
     * @param e
     *            Mouse event
     */
    protected void contentsMouseDoubleClickEvent(MouseEvent e) {
    }

    /**
     * Called when mouse is on contents area and button is pressed.
     *
     * @param e
     *            Mouse event
     */
    protected void contentsMouseDownEvent(MouseEvent e) {
        fMouseDownX = e.x;
        fMousDownY = e.y;
    }

    /**
     * TimerTask for auto scroll feature.
     */
    protected static class AutoScroll extends TimerTask {

        /** X delta */
        private int deltaX;

        /** Y delta */
        private int deltaY;

        /** ScrollView object */
        private ScrollView scrollView;

        /**
         * Constructor.
         *
         * @param sv
         *            ScrollView object to use
         * @param dx
         *            X delta
         * @param dy
         *            Y delta
         */
        public AutoScroll(ScrollView sv, int dx, int dy) {
            scrollView = sv;
            deltaX = dx;
            deltaY = dy;
        }

        @Override
        public void run() {
            final Display display = Display.getDefault();
            if ((display == null) || display.isDisposed()) {
                return;
            }
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!scrollView.isDisposed()) {
                        scrollView.scrollBy(deltaX, deltaY);
                    }
                }
            });
        }
    }

    /**
     * Called when mouse is on contents area and mode.
     *
     * @param event
     *            Mouse event
     */
    protected void contentsMouseMoveEvent(MouseEvent event) {
        if ((event.stateMask & SWT.BUTTON_MASK) != 0) {
            if (!fAutoScrollEnabled) {
                scrollBy(-(event.x - fMouseDownX), -(event.y - fMousDownY));
                return;
            }

            int sx = 0, sy = 0;

            int vRight = getContentsX() + getVisibleWidth();
            int vBottom = getContentsY() + getVisibleHeight();

            // auto scroll... ?
            if (event.x < getContentsX()) {
                sx = (getContentsX() - event.x);
                fMouseDownX = getContentsX();
            } else if (event.x > vRight) {
                sx = -event.x + vRight;
                fMouseDownX = vRight;
            }
            if (event.y < getContentsY()) {
                sy = (getContentsY() - event.y);
                fMousDownY = getContentsY();
            } else if (event.y > vBottom) {
                sy = -event.y + vBottom;
                fMousDownY = vBottom;
            }

            if (sx != 0 || sy != 0) {
                // start auto scroll...
                if (fAutoScroll == null) {
                    if (fAutoScrollTimer == null) {
                        fAutoScrollTimer = new Timer(true);
                    }
                    fAutoScroll = new AutoScroll(this, sx, sy);
                    fAutoScrollTimer.schedule(fAutoScroll, 0, fAutoScrollPeriod);
                } else {
                    fAutoScroll.deltaX = sx;
                    fAutoScroll.deltaY = sy;
                }
            } else {
                if (fAutoScroll != null) {
                    fAutoScroll.cancel();
                    fAutoScroll = null;
                }

                scrollBy(-(event.x - fMouseDownX), -(event.y - fMousDownY));
            }
        }
    }

    /**
     * Called when mouse is on contents area and button is released
     *
     * @param event
     *            Mouse event
     */
    protected void contentsMouseUpEvent(MouseEvent event) {
        // reset auto scroll if it's engaged
        if (fAutoScroll != null) {
            fAutoScroll.cancel();
            fAutoScroll = null;
        }
    }

    /**
     * Responsible to draw contents area. At least rectangle clipX must be
     * redrawn. This rectangle is given in contents coordinates. By default, no
     * paint is produced.
     *
     * @param gc
     *            Graphics context
     * @param clipx
     *            X clip
     * @param clipy
     *            Y clip
     * @param clipw
     *            W clip
     * @param cliph
     *            H clip
     */
    protected void drawContents(GC gc, int clipx, int clipy, int clipw, int cliph) {
    }

    /**
     * Change the size of the contents area.
     *
     * @param width new width of the area.
     * @param height new height of the area.
     */
    public void resizeContents(int width, int height) {
        int localWidth = width;
        int localHeight = height;

        if (localWidth < 0) {
            localWidth = 0;
        }
        if (localHeight < 0) {
            localHeight = 0;
        }

        int oldW = fContentsWidth;
        int oldH = fContentsHeight;

        if (localWidth == oldW && localHeight == oldH) {
            return;
        }

        fContentsWidth = localWidth;
        fContentsHeight = localHeight;

        if (oldW > localWidth) {
            int s = localWidth;
            localWidth = oldW;
            oldW = s;
        }

        int visWidth = getVisibleWidth();
        int visHeight = getVisibleHeight();
        if (oldW < visWidth) {
            if (localWidth > visWidth) {
                localWidth = visWidth;
            }
            fViewControl.redraw(getContentsX() + oldW, 0, localWidth - oldW, visHeight, true);
        }

        if (oldH > localHeight) {
            int s = localHeight;
            localHeight = oldH;
            oldH = s;
        }

        if (oldH < visHeight) {
            if (localHeight > visHeight) {
                localHeight = visHeight;
            }
            fViewControl.redraw(0, getContentsY() + oldH, visWidth, localHeight - oldH, true);
        }
        if (updateScrollBarVisiblity()) {
            layout();
        } else {
            updateScrollBarsValues();
        }
    }

    // redefined for internal use ..
    @Override
    public void redraw() {
        super.redraw();
        // ..need to redraw this already:
        fViewControl.redraw();
    }

    /**
     * @param delataX The delta in X
     * @param deltaY the delta in Y
     */
    public void scrollBy(int delataX, int deltaY) {
        setContentsPos(getContentsX() + delataX, getContentsY() + deltaY);
    }

    /**
     * Scroll to ensure point(in contents coordinates) is visible.
     *
     * @param px Point's x position
     * @param py Point's y position
     */
    public void ensureVisible(int px, int py) {
        int cx = getContentsX(), cy = getContentsY();
        int right = getContentsX() + getVisibleWidth();
        int bottom = getContentsY() + getVisibleHeight();
        if (px < getContentsX()) {
            cx = px;
        } else if (px > right) {
            cx = px - getVisibleWidth();
        }
        if (py < getContentsY()) {
            cy = py;
        } else if (py > bottom) {
            cy = py - getVisibleHeight();
        }
        setContentsPos(cx, cy);
    }

    /**
     * Make rectangle (x,y,w,h, in contents coordinates) visible. if rectangle cannot be completely visible, use
     * align flags.
     *
     * @param xValue x contents coordinates of rectangle.
     * @param yValue y contents coordinates of rectangle.
     * @param width width of rectangle.
     * @param height height of rectangle.
     * @param align bit or'ed SWT flag like SWT.LEFT,RIGHT,CENTER,TOP,BOTTOM,VERTICAL used only for bigger rectangle
     *            than visible area. By default CENTER/VERTICAL
     */
    public void ensureVisible(int xValue, int yValue, int width, int height, int align) {
        ensureVisible(xValue, yValue, width, height, align, false);
    }

    /**
     * Make rectangle (xValue,yValue,width,height, in contents coordinates) visible. if rectangle cannot be completely visible, use
     * align flags.
     *
     * @param xValue x contents coordinates of rectangle.
     * @param yValue y contents coordinates of rectangle.
     * @param width width of rectangle.
     * @param height height of rectangle.
     * @param align bit or'ed SWT flag like SWT.LEFT,RIGHT,CENTER,TOP,BOTTOM,VERTICAL used only for bigger rectangle
     *            than visible area. By default CENTER/VERTICAL
     * @param forceAlign force alignment for rectangle smaller than the visible area
     */
    protected void ensureVisible(int xValue, int yValue, int width, int height, int align, boolean forceAlign) {

        int localX = xValue;
        int localY = yValue;
        int localWidth = width;
        int localHeight = height;

        if (localWidth < 0) {
            localX = localX + localWidth;
            localWidth = -localWidth;
        }
        if (localHeight < 0) {
            localY = localY + localHeight;
            localHeight = -localHeight;
        }
        int hbar = getHorizontalBarHeight();
        int vbar = getVerticalBarWidth();
        int cx = getContentsX();
        int cy = getContentsY();
        int right = getContentsX() + getVisibleWidth() - vbar;
        int bottom = getContentsY() + getVisibleHeight() - hbar;
        boolean alignH = false, alignV = false;

        if (localX < getContentsX()) {
            cx = localX;
        } else if (localX + localWidth > right) {
            cx = localX - localWidth;
        }
        if (localY < getContentsY()) {
            cy = localY;
        } else if (localY + localHeight > bottom) {
            cy = localY - localHeight;
        }

        if (localWidth > getVisibleWidth()) {
            alignH = true;
        }
        if (localHeight > getVisibleHeight()) {
            alignV = true;
        }
        // compute alignment on visible area horizontally
        if (alignH || (forceAlign && localX + localWidth > right)) {
            // use align flags
            if ((align & SWT.LEFT) != 0) {
                cx = localX;
            } else if ((align & SWT.RIGHT) != 0) {
                cx = right - localWidth;
            } else { // center
                cx = localX + (localWidth - getVisibleWidth()) / 2;
            }
        }
        // compute alignment on visible area vertically
        if (alignV || (forceAlign && localY + localHeight > bottom)) {
            // use align flags
            if ((align & SWT.TOP) != 0) {
                cy = localY;
            } else if ((align & SWT.BOTTOM) != 0) {
                cy = bottom - localHeight;
            } else { // center
                cy = localY + (localHeight - getVisibleHeight()) / 2;
            }
        }
        setContentsPos(cx, cy);
    }

    /**
     * Returns true if point is visible (expressed in contents coordinates).
     *
     * @param px Point's x position
     * @param py Point's y position
     * @return true if point is visible (expressed in contents coordinates)
     */
    public boolean isVisible(int px, int py) {
        if (px < getContentsX()) {
            return false;
        }
        if (py < getContentsY()) {
            return false;
        }
        if (px > (getContentsX() + getVisibleWidth())) {
            return false;
        }
        if (py > (getContentsY() + getVisibleHeight())) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if rectangle if partially visible.
     *
     * @param xValue x contents coordinates of rectangle.
     * @param yValue y contents coordinates of rectangle.
     * @param width width of rectangle.
     * @param height height of rectangle.
     * @return true if rectangle if partially visible.
     */
    public boolean isVisible(int xValue, int yValue, int width, int height) {
        if (xValue + width < getContentsX()) {
            return false;
        }
        if (yValue + height < getContentsY()) {
            return false;
        }
        int vr = getContentsX() + getVisibleWidth();
        int vb = getContentsY() + getVisibleHeight();
        if (xValue > vr) {
            return false;
        }
        if (yValue > vb) {
            return false;
        }
        return true;
    }

    /**
     * Returns visible part of rectangle, or null if rectangle is not visible.
     * Rectangle is expressed in contents coordinates.
     *
     * @param xValue
     *            x contents coordinates of rectangle.
     * @param yValue
     *            y contents coordinates of rectangle.
     * @param width
     *            width of rectangle.
     * @param height
     *            height of rectangle.
     * @return visible part of rectangle, or null if rectangle is not visible.
     */
    public Rectangle getVisiblePart(int xValue, int yValue, int width, int height) {
        if (xValue + width < getContentsX()) {
            return null;
        }
        if (yValue + height < getContentsY()) {
            return null;
        }
        int vr = getContentsX() + getVisibleWidth();
        int vb = getContentsY() + getVisibleHeight();
        if (xValue > vr) {
            return null;
        }
        if (yValue > vb) {
            return null;
        }
        int rr = xValue + width, rb = yValue + height;
        int nl = Math.max(xValue, getContentsX()), nt = Math.max(yValue, getContentsY()), nr = Math.min(rr, vr), nb = Math.min(rb, vb);
        return new Rectangle(nl, nt, nr - nl, nb - nt);
    }

    /**
     * Returns the visible part for given rectangle.
     *
     * @param rect A rectangle
     *
     * @return gets visible part of rectangle (or <code>null</code>)
     */
    public final Rectangle getVisiblePart(Rectangle rect) {
        if (rect == null) {
            return null;
        }
        return getVisiblePart(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Change top left position of visible area. Check if the given point is inside contents area.
     *
     * @param xValue x contents coordinates of visible area.
     * @param yValue y contents coordinates of visible area.
     * @return true if view really moves
     */
    public boolean setContentsPos(int xValue, int yValue) {
        int nx = xValue, ny = yValue;
        if (getVisibleWidth() >= getContentsWidth()) {
            nx = 0;
        } else {
            if (xValue < 0) {
                nx = 0;
            } else if (xValue + getVisibleWidth() > getContentsWidth()) {
                nx = getContentsWidth() - getVisibleWidth();
            }
        }
        if (getVisibleHeight() >= getContentsHeight()) {
            ny = 0;
        } else {
            if (yValue <= 0) {
                ny = 0;
            } else if (yValue + getVisibleHeight() > getContentsHeight()) {
                ny = getContentsHeight() - getVisibleHeight();
            }
        }
        // no move
        if (nx == fContentsX && ny == fContentsY) {
            return false;
        }
        fContentsX = nx;
        fContentsY = ny;
        updateScrollBarsValues();
        // ? find smallest area to redraw only them ?
        fViewControl.redraw();
        return true;
    }

    @Override
    public ScrollBar getVerticalBar() {
        return fVertScrollBar.getVerticalBar();
    }

    @Override
    public ScrollBar getHorizontalBar() {
        return fHorScrollBar.getHorizontalBar();
    }

    /**
     * Compute visibility of vertical/horizontal bar using given width/height and current visibility (i.e. is bar size are already in
     * for_xxx)
     * @param forWidth width of foreground
     * @param forHeight height of foreground
     * @param currHorVis The current visibility state of horizontal scroll bar
     * @param currVertvis The current visibility state of vertical scroll bar
     * @return <code>true</code> if visibility changed else <code>false</code>
     */
    public int computeBarVisibility(int forWidth, int forHeight, boolean currHorVis, boolean currVertvis) {

        int localForWidth = forWidth;
        int vis = 0x00;
        switch (fVertScrollbarMode) {
        case ALWAYS_OFF:
            break;
        case ALWAYS_ON:
            vis |= VBAR;
            break;
        case AUTO:
            if (getContentsHeight() > forHeight) {
                vis = VBAR;
                // v bar size is already in for_width.
                if (!currVertvis) {// (curr_vis&0x01)==0)
                    localForWidth -= getVerticalBarWidth();
                }
            }
            break;
        default:
            break;
        }

        switch (fHorScrollbarMode) {
        case ALWAYS_OFF:
            break;
        case ALWAYS_ON:
            vis |= HBAR;
            break;
        case AUTO:
            if (getContentsWidth() > localForWidth) {
                vis |= HBAR;
                // h bar is not in for_height
                if ((!currHorVis) && (getContentsHeight() > (forHeight - getHorizontalBarHeight()))) {// (curr_vis&0x02)==0 )
                    vis |= VBAR;
                }
            }
            break;
        default:
            break;
        }
        return vis;
    }

    /**
     * Setup scroll bars visibility.
     *
     * @return True if one of visibility changed.
     */
    protected boolean updateScrollBarVisiblity() {
        boolean change = false;

        boolean currVertVis = fVertScrollBar.getVisible();
        boolean currHorVis = fHorScrollBar.getVisible();
        int barNewVis = computeBarVisibility(getVisibleWidth(), getVisibleHeight(), currHorVis, currVertVis);
        boolean newVertVis = (barNewVis & VBAR) != 0;
        boolean newHorVis = (barNewVis & HBAR) != 0;
        if (currVertVis ^ newVertVis) { // vertsb_.getVisible() )
            fVertScrollBar.setVisible(newVertVis);
            change = true;
        }
        if (currHorVis ^ newHorVis) {
            fHorScrollBar.setVisible(newHorVis);
            change = true;
        }

        // update corner control visibility:
        if (fCornerControl != null && change) {
            boolean vis = newVertVis || newHorVis;
            if (vis ^ fCornerControl.getVisible()) {
                fCornerControl.setVisible(vis);
                change = true; // but must be already the case
            }
        }
        return change;
    }

    /**
     * Setup scroll bar using contents, visible and scroll bar mode properties.
     */
    protected void updateScrollBarsValues() {
        /* update vertical scrollbar */
        ScrollBar b = getVerticalBar();
        if (b != null) {
            b.setMinimum(0);
            b.setMaximum(getContentsHeight());
            b.setThumb(getVisibleHeight());
            b.setPageIncrement(getVisibleHeight());
            b.setIncrement(fVertScrollbarIncrement);
            b.setSelection(getContentsY());
        }

        // update "hidden" vertical bar too
        b = fViewControl.getVerticalBar();
        if (b != null) {
            b.setMinimum(0);
            b.setMaximum(getContentsHeight());
            b.setThumb(getVisibleHeight());
            b.setPageIncrement(getVisibleHeight());
            b.setIncrement(fVertScrollbarIncrement);
            b.setSelection(getContentsY());
        }

        /* update horizontal scrollbar */
        b = getHorizontalBar();
        if (b != null) {
            b.setMinimum(0);
            b.setMaximum(getContentsWidth());
            b.setThumb(getVisibleWidth());
            b.setSelection(getContentsX());
            b.setPageIncrement(getVisibleWidth());
            b.setIncrement(fHorScrollbarIncrement);
        }
        // update "hidden" horizontal bar too
        b = fViewControl.getHorizontalBar();
        if (b != null) {
            b.setMinimum(0);
            b.setMaximum(getContentsWidth());
            b.setThumb(getVisibleWidth());
            b.setSelection(getContentsX());
            b.setPageIncrement(getVisibleWidth());
            b.setIncrement(fHorScrollbarIncrement);
        }
    }

    /**
     * Change the control used in the bottom right corner (between two scrollbar), if control is null reset previous
     * corner control. This control is visible only if at least one scrollbar is visible. Given control will be disposed
     * by ScrollView, at dispose() time, at next setCornetControl() call or when calling setOverviewEnabled(). Pay
     * attention calling this reset overview feature until setOverviewEnabled(true) if called.
     * @param control The control for the overview
     */
    public void setCornerControl(Control control) {
        if (fCornerControl != null) {
            fCornerControl.dispose();
        }
        fCornerControl = control;
        if (fCornerControl != null) {
            ScrollBar vb = getVerticalBar();
            ScrollBar hb = getHorizontalBar();
            boolean vis = vb.getVisible() || hb.getVisible();
            fCornerControl.setVisible(vis);
        }
    }

    /**
     * Transform (x,y) point in widget coordinates to contents coordinates.
     *
     * @param x The x widget coordinate.
     * @param y The y widget coordinate.
     * @return org.eclipse.swt.graphics.Point with content coordinates.
     */
    public final Point viewToContents(int x, int y) {
        return new Point(viewToContentsX(x), viewToContentsY(y));
    }

    /**
     * Transform x in widget coordinates to contents coordinates
     *
     * @param x The y widget coordinate.
     * @return the x content coordinate.
     */
    public int viewToContentsX(int x) {
        return fContentsX + x;
    }

    /**
     * Transform y in widget coordinates to contents coordinates
     *
     * @param y The y widget coordinate.
     * @return the y content coordinate.
     */
    public int viewToContentsY(int y) {
        return fContentsY + y;
    }

    /**
     * Transform (x,y) point from contents coordinates, to widget coordinates.
     *
     * @param x The x content coordinate.
     * @param y The y content coordinate.
     * @return coordinates widget area as.
     */
    public final Point contentsToView(int x, int y) {
        return new Point(contentsToViewX(x), contentsToViewY(y));
    }

    /**
     * Transform X axis coordinates from contents to widgets.
     *
     * @param x contents coordinate to transform.
     * @return x coordinate in widget area
     */
    public int contentsToViewX(int x) {
        return x - fContentsX;
    }

    /**
     * Transform Y axis coordinates from contents to widgets.
     *
     * @param y contents coordinate to transform
     * @return y coordinate in widget area
     */
    public int contentsToViewY(int y) {
        return y - fContentsY;
    }

    /**
     * Return the visible height of scroll view, might be > contentsHeight
     *
     * @return the visible height of scroll view, might be > contentsHeight()
     */
    public int getVisibleHeight() {
        return fViewControl.getClientArea().height;
    }

    /**
     * Return int the visible width of scroll view, might be > contentsWidth().
     *
     * @return int the visible width of scroll view, might be > contentsWidth()
     */
    public int getVisibleWidth() {
        return fViewControl.getClientArea().width;
    }

    /**
     * Add support for arrow key, scroll the ... scroll view. But you can
     * redefine this method for your convenience.
     *
     * @param event
     *            Keyboard event
     */
    protected void keyPressedEvent(KeyEvent event) {
        switch (event.keyCode) {
        case SWT.ARROW_UP:
            scrollBy(0, -getVisibleHeight());
            break;
        case SWT.ARROW_DOWN:
            scrollBy(0, +getVisibleHeight());
            break;
        case SWT.ARROW_LEFT:
            scrollBy(-getVisibleWidth(), 0);
            break;
        case SWT.ARROW_RIGHT:
            scrollBy(+getVisibleWidth(), 0);
            break;
        default:
            break;
        }
    }

    /**
     * Redefine this method at your convenience
     *
     * @param event The key event.
     */
    protected void keyReleasedEvent(KeyEvent event) {
    }

    /**
     * Returns vertical bar width, even if bar isn't visible.
     *
     * @return vertical bar width, even if bar isn't visible
     */
    public int getVerticalBarWidth() {
        // include vertical bar width and trimming of scrollable used
        int bw = fVertScrollBar.computeTrim(0, 0, 0, 0).width;
        return bw + 1;
    }

    /**
     * Returns horizontal bar height even if bar isn't visible.
     *
     * @return horizontal bar height even if bar isn't visible
     */
    public int getHorizontalBarHeight() {
        // include horiz. bar height and trimming of scrollable used
        int bh = fHorScrollBar.computeTrim(0, 0, 0, 0).height;
        // +1 because win32 H.bar need 1 pixel canvas size to appear ! (strange no ?)
        return bh + 1;
    }

    @Override
    public Rectangle computeTrim(int x, int y, int w, int h) {
        Rectangle r = new Rectangle(x, y, w, h);
        int barVis = computeBarVisibility(w, h, false, false);
        if ((barVis & VBAR) != 0) {
            r.width += getVerticalBarWidth();
        }
        if ((barVis & HBAR) != 0) {
            r.height += getHorizontalBarHeight();
        }
        return r;
    }

    /**
     *  Internal layout for ScrollView, handle scrollbars, drawzone and corner control
     */
    protected class SVLayout extends Layout {

        private static final int DEFAULT_X = 250;
        private static final int DEFAULT_Y = 250;
        private static final int MAX_SEEK = 10;
        private static final int MIN_SEEK = 0;

        /**
         * The seek value
         */
        private int seek = 0;
        /**
         * The do-it-not flag
         */
        private boolean dontLayout = false;

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            Point p = new Point(DEFAULT_X, DEFAULT_Y);
            if (fContentsWidth < p.x) {
                p.x = fContentsWidth;
            }
            if (fContentsHeight < p.y) {
                p.y = fContentsHeight;
            }
            return p;
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            if (dontLayout) {
                return;
            }
            seek++;
            if (seek > MAX_SEEK) {
                dontLayout = true;
            }

            Point cs = composite.getSize();
            int barVis = computeBarVisibility(cs.x, cs.y, false, false);
            boolean vbVis = (barVis & VBAR) != 0;
            boolean hbVis = (barVis & HBAR) != 0;
            fVertScrollBar.setVisible(vbVis);
            fHorScrollBar.setVisible(hbVis);
            int vbw = getVerticalBarWidth();
            int hbh = getHorizontalBarHeight();
            int wb = vbVis ? vbw : 0;
            int hb = hbVis ? hbh : 0;
            int cww = 0, cwh = 0;

            if (fCornerControl != null && (vbVis || hbVis)) { // corner_control_.getVisible())
                fCornerControl.setVisible(true);
                cww = vbw;
                cwh = hbh;
                if (wb == 0) {
                    wb = vbw;
                }
                if (hb == 0) {
                    hb = hbh;
                }
            } else if (vbVis && hbVis) {
                if (fCornerControl != null) {
                    fCornerControl.setVisible(false);
                }
                cww = vbw;
                cwh = hbh;
            }
            if (vbVis || hbVis) {
                updateScrollBarsValues();
            }

            int vw = cs.x - (vbVis ? vbw : 0);
            int vh = cs.y - (hbVis ? hbh : 0);
            int vbx = cs.x - wb;
            int hby = cs.y - hb;

            fViewControl.setBounds(0, 0, vw, vh);

            if (vbVis) {
                fVertScrollBar.setBounds(vbx, 0, wb, cs.y - cwh);
            }
            if (hbVis) {
                fHorScrollBar.setBounds(0, hby, cs.x - cww, hb);
            }
            if (fCornerControl != null && fCornerControl.getVisible()) {
                fCornerControl.setBounds(vbx, hby, vbw, hbh);
            }
            updateScrollBarsValues();

            seek--;
            if (seek == MIN_SEEK) {
                dontLayout = false;
            }
        }

        boolean isDontLayout() {
            return dontLayout;
        }

        void setSontLayout(boolean dontLayout) {
            this.dontLayout = dontLayout;
        }
    }

    // static must take place here... cursor is created once.
    private volatile static Cursor fOverviewCursor;

    /** Support for click-and-see overview shell on this ScrollView */
    protected class Overview {

        private static final  int REFRESH_FREQ = 4;

        /**
         *  factor for X from real and overview sizes, for mouse move speed.
         */
        private float fOverviewFactorX;

        /**
         *  factor for Y from real and overview sizes, for mouse move speed.
         */
        private float fOverviewFactorY;
        /**
         *  shell use to show overview
         */
        private Shell fOverview;
        /**
         * save mouse X cursor location for disappear();
         */
        private int fSaveCursorX;
        /**
         * save mouse Y cursor location for disappear();
         */
        private int fSaveCursorY;

        /**
         * Apply overview support on a control. Replace existing corner_widget
         *
         * @param control
         *            The control to use
         */
        public void useControl(Control control) {
            final Point pos = control.getLocation();
            control.addMouseListener(new MouseListener() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                }

                @Override
                public void mouseDown(MouseEvent e) {
                    overviewAppear(e.x, e.y);
                }

                @Override
                public void mouseUp(MouseEvent e) {
                    overviewDisappear();
                }
            });

            control.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (overviewing()) {
                        overviewDisappear(false);
                    }
                }

            });
            control.addKeyListener(new KeyListener() {

                @Override
                public void keyPressed(KeyEvent event) {
                    if (event.keyCode == 32 && !overviewing()) {
                        overviewAppear(pos.x, pos.y);
                    } else if (event.keyCode == 32) {
                        overviewDisappear();
                    }
                    if (event.keyCode == SWT.ARROW_DOWN) {
                        overviewMove(0, 1, event);
                    }

                    if (event.keyCode == SWT.ARROW_UP) {
                        overviewMove(0, -1, event);
                    }

                    if (event.keyCode == SWT.ARROW_RIGHT) {
                        overviewMove(1, 0, event);
                    }

                    if (event.keyCode == SWT.ARROW_LEFT) {
                        overviewMove(-1, 0, event);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
            control.addMouseMoveListener(new MouseMoveListener() {
                private int refReshCount  = 0;
                @Override
                public void mouseMove(MouseEvent event) {
                    if (overviewing()) {
                        // Slow down the refresh
                        if (refReshCount % REFRESH_FREQ == 0) {
                            overviewMove(event);
                        }
                        refReshCount++;
                    }
                }
            });
        }

        /**
         * Dispose controls of overview
         */
        public void dispose() {
            if (fOverview != null) {
                fOverview.dispose();
            }
        }

        /**
         * @return true if overview is currently on screen
         */
        protected boolean overviewing() {
            return (fOverview != null && fOverview.isVisible());
        }

        /**
         * Process overview appear
         *
         * @param mx
         *            X coordinate
         * @param my
         *            Y coordinate
         */
        protected void overviewAppear(int mx, int my) {
            if (fOverview == null) {
                fOverview = new Shell(getShell(), SWT.ON_TOP | SWT.NO_BACKGROUND);
                fOverview.addPaintListener(new PaintListener() {
                    @Override
                    public void paintControl(PaintEvent e) {
                        drawOverview(e.gc, fOverview.getClientArea());
                    }
                });
            }
            // always the same..
            fOverview.setForeground(fViewControl.getForeground());

            // get location of shell (in screeen coordinates)
            Point p = toGlobalCoordinates(fCornerControl, 0, 0);
            int x = p.x;
            int y = p.y;
            int w, h;
            h = fOverviewSize;
            w = h;
            Rectangle scr = getDisplay().getBounds();
            Point ccs = fCornerControl.getSize();
            try {
                if (fContentsWidth > fContentsHeight) {
                    float ratio = fContentsHeight / (float) fContentsWidth;
                    h = (int) (w * ratio);
                    if (h < ccs.y) {
                        h = ccs.y;
                    } else if (h >= scr.height / 2) {
                        h = scr.height / 2;
                    }
                } else {
                    float ratio = fContentsWidth / (float) fContentsHeight;
                    w = (int) (h * ratio);
                    if (w < ccs.x) {
                        w = ccs.x;
                    } else if (w >= scr.width / 2) {
                        w = scr.width / 2;
                    }
                }
                fOverviewFactorX = fContentsWidth / (float) w;
                fOverviewFactorY = fContentsHeight / (float) h;
            }
            // no contents size set ?
            catch (java.lang.ArithmeticException e) {
            }

            // try pop-up on button, extending to bottom right,
            if (x <= 0) {
                x = 1;
            }
            if (y <= 0) {
                y = 1;
            }
            x = x - w + ccs.x;
            y = y - h + ccs.y;
            fOverview.setBounds(x, y, w, h);
            fOverview.setVisible(true);
            fOverview.redraw();
            // mouse cursor disappear, so set invisible mouse cursor ...
            if (fOverviewCursor == null) {
                RGB rgb[] = { new RGB(0, 0, 0), new RGB(255, 0, 0) };
                PaletteData palette = new PaletteData(rgb);
                int s = 1;
                byte src[] = new byte[s * s];
                byte msk[] = new byte[s * s];
                for (int i = 0; i < s * s; ++i) {
                    src[i] = (byte) 0xFF;
                }
                ImageData i_src = new ImageData(s, s, 1, palette, 1, src);
                ImageData i_msk = new ImageData(s, s, 1, palette, 1, msk);
                fOverviewCursor = new Cursor(null, i_src, i_msk, 0, 0);
            }
            fCornerControl.setCursor(fOverviewCursor);
            // convert to global coordinates
            p = toGlobalCoordinates(fCornerControl, mx, my);
            fSaveCursorX = p.x;
            fSaveCursorY = p.y;

            Rectangle r = fOverview.getClientArea();
            int cx = (int) (r.width * fContentsX / (float) fContentsWidth);
            int cy = (int) (r.height * fContentsY / (float) fContentsHeight);

            // cx,cy to display's global coordinates
            p = toGlobalCoordinates(fOverview.getParent(), cx, cy);
        }

        /**
         * Process disappear of overview
         */
        protected void overviewDisappear() {
            overviewDisappear(true);
        }

        /**
         * Process disappear of overview
         * @param restoreCursorLoc A flag to restore cursor location
         */
        protected void overviewDisappear(boolean restoreCursorLoc) {
            if (fOverview == null) {
                return;
            }
            fOverview.setVisible(false);
            fCornerControl.setCursor(null);
            if (restoreCursorLoc) {
                getDisplay().setCursorLocation(fSaveCursorX, fSaveCursorY);
            }
            fOverview.dispose();
            fOverview = null;
        }

        /**
         * Process mouse move in overview
         * @param event The mouse event
         */
        protected void overviewMove(MouseEvent event) {
            Point p = toGlobalCoordinates(fCornerControl, event.x, event.y);
            int dx = p.x - fSaveCursorX;
            int dy = p.y - fSaveCursorY;
            overviewMove(dx, dy, event);
        }

        /**
         * Process mouse move event when overviewing
         *
         * @param dx The x coordinates delta
         * @param dy The y coordinates delta
         * @param event The typed event
         */
        protected void overviewMove(int dx, int dy, TypedEvent event) {
            boolean ctrl = false;
            boolean shift = false;

            if (event instanceof MouseEvent) {
                MouseEvent e = (MouseEvent) event;
                getDisplay().setCursorLocation(fSaveCursorX, fSaveCursorY);
                ctrl = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;
            } else if (event instanceof KeyEvent) {
                KeyEvent e = (KeyEvent) event;
                ctrl = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;
            }

            int cx = fContentsX;
            int cy = fContentsY;
            float fx = fOverviewFactorX;
            float fy = fOverviewFactorY;

            if (ctrl && shift) {
                if ((fx * 0.25f > 1) && (fy * 0.25 > 1)) {
                    fx = fy = 1.0f;
                } else {
                    fx *= 0.1f;
                    fy *= 0.1f;
                }
            } else if (ctrl) {
                fx *= 0.5f;
                fy *= 0.5f;
            } else if (shift) {
                fx *= 0.5f;
                fy *= 0.5f;
            }
            scrollBy((int) (fx * dx), (int) (fy * dy));
            if (cx != fContentsX || cy != fContentsY) {
                fOverview.redraw();
                fOverview.update(); // draw now !
            }
        }

        /**
         * Convert overview coordinates to global coordinates.
         *
         * @param loc
         *            the control reference
         * @param x
         *            The x coordinate to convert
         * @param y
         *            The y coordinate to convert
         * @return The new converted Point
         */
        protected Point toGlobalCoordinates(Control loc, int x, int y) {
            Point p = new Point(x, y);
            for (Control c = loc; c != null; c = c.getParent()) {
                // control might have client area with 'decorations'
                int trimX = 0, trimY = 0;
                // other kind of widget with trimming ??
                if (c instanceof Scrollable) {
                    Scrollable s = (Scrollable) c;
                    Rectangle rr = s.getClientArea();
                    Rectangle tr = s.computeTrim(rr.x, rr.y, rr.width, rr.height);
                    trimX = rr.x - tr.x;
                    trimY = rr.y - tr.y;
                }
                p.x += c.getLocation().x + trimX;
                p.y += c.getLocation().y + trimY;
            }
            return p;
        }
    }
}
