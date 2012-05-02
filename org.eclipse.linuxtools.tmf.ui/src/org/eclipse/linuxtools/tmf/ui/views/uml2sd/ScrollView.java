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

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
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
    // Attributes
    // ------------------------------------------------------------------------

    // Value for scroll bar mode, default is AUTO 
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
    /**
     * Value of the contents_height_ property.
     */
    protected int contents_height_ = 0;
    /**
     * Value of the contents_width_ property.
     */
    protected int contents_width_ = 0;
    /**
     * Value of the contents_x_ property 
     */
    protected int contents_x_ = 0;
    /**
     * Value of the contents_y_ property 
     */
    protected int contents_y_ = 0;
    /**
     * Scroll bar mode of vertical scroll bar. 
     */    
    protected int h_scrollbar_mode_ = AUTO;
    /**
     * Scroll bar mode of horizontal scroll bar. 
     */    
    protected int v_scrollbar_mode_ = AUTO;
    /**
     * Increment for the horizontal scroll bar.
     */
    protected int h_scrollbar_increment_ = 10;
    /**
     * Increment for the vertical scroll bar.
     */
    protected int v_scrollbar_increment_ = 10;
    /**
     * Flag whether auto scroll is enabled or not.
     */
    protected boolean auto_scroll_enabled_ = true;
    /**
     * Value of the auto scroll period.
     */
    protected int auto_scroll_period_ = 75;

    /**
     * The local paint listener reference.
     */
    protected PaintListener localPaintListener = null;
    /**
     * The local mouse move listener reference.
     */
    protected MouseMoveListener localMouseMoveListener = null;
    /**
     * The local mouse listener reference.
     */
    protected MouseListener localMouseListener = null;
    /**
     * The local control listener reference.
     */
    protected ControlListener localControlListener = null;
    /**
     * The local key listener reference.
     */
    protected KeyListener localKeyListener = null;
    // Canvas for vertical/horizontal Scroll Bar only ... because new ScrollBar() does works.
    /**
     * Canvas for horizontal scroll bar.
     */
    protected Canvas horzsb_;
    /**
     * Canvas for vertical scroll bar.
     */
    protected Canvas vertsb_;
    /**
     * Canvas for the view control.
     */
    protected Canvas viewcontrol_;
    /**
     * Control used in the bottom right corner @see setCornerControl() and @see setOverviewEnabled(true) 
     */
    protected Control corner_control_;
    /**
     * Size of overview widget.
     */
    protected int overview_size_ = 100; // default size for overview

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
        super(c, SWT.NONE); // style&(~(SWT.H_SCROLL|SWT.V_SCROLL)));

        horzsb_ = new Canvas(this, SWT.H_SCROLL);
        if (mouseWheel) {
            // force scroll bar to get mouse wheel, those scrollbar will be hidden
            viewcontrol_ = new Canvas(this, style | SWT.H_SCROLL | SWT.V_SCROLL);
        } else {
            viewcontrol_ = new Canvas(this, style);
        }
        viewcontrol_.setBackground(getBackground());
        // hide scroll bar as their are replaced by vertsb_ and horzsb_.
        if (mouseWheel) {
            viewcontrol_.getVerticalBar().setVisible(false);
            viewcontrol_.getHorizontalBar().setVisible(false);
        }
        vertsb_ = new Canvas(this, SWT.V_SCROLL);
        // make vertsb_ able to receive mouse wheel
        // doesnot help as we can't set a MouseListener on vertsb_.getVerticalBar()
        // to set focus on viewcontrol_
        // vertsb_.addKeyListener( new KeyAdapter() {});

        setLayout(new SVLayout());

        localPaintListener = new PaintListener() {
            @Override
            public void paintControl(PaintEvent event) {
                // use clipping, to reduce cost of paint.
                Rectangle r = event.gc.getClipping();
                int cx = viewToContentsX(r.x);
                int cy = viewToContentsY(r.y);
                drawContents(event.gc, cx, cy, r.width, r.height);
            }
        };
        viewcontrol_.addPaintListener(localPaintListener);

        localMouseMoveListener = new MouseMoveListener() {
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

        viewcontrol_.addMouseMoveListener(localMouseMoveListener);

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

        viewcontrol_.addMouseTrackListener(localMouseTrackListener);

        localMouseListener = new MouseListener() {
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
                e.x = mouse_down_x_ = viewToContentsX(e.x);
                e.y = mouse_down_y_ = viewToContentsY(e.y);
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
                // here because class extenting me can catch mouse Up and want to scroll...
                mouse_down_x_ = mouse_down_y_ = -1;
            }
        };
        viewcontrol_.addMouseListener(localMouseListener);

        localKeyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                keyPressedEvent(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keyReleasedEvent(e);
            }
        };

        viewcontrol_.addKeyListener(localKeyListener);

        getVerticalBar().addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setContentsPos(contents_x_, getVerticalBar().getSelection());
                // need to change "hidden" vertical bar value ?
                // force focus on viewcontrol_ so we got future mouse wheel's scroll events
                if (!viewcontrol_.isFocusControl()) {
                    viewcontrol_.setFocus();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        if (viewcontrol_.getVerticalBar() != null)
            // add viewcontrol hidden scrollbar listener to get mouse wheel ...
            viewcontrol_.getVerticalBar().addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ScrollBar b = viewcontrol_.getVerticalBar();
                    setContentsPos(contents_x_, b.getSelection());
                    // change "real" vertical bar selection too
                    getVerticalBar().setSelection(b.getSelection());
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
        getHorizontalBar().addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setContentsPos(getHorizontalBar().getSelection(), contents_y_);
                // need to change "real" horizontal bar too ?
                // force focus on viewcontrol_ so we got future mouse wheel's scroll events
                if (!viewcontrol_.isFocusControl()) {
                    viewcontrol_.setFocus();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        if (viewcontrol_.getHorizontalBar() != null)
            viewcontrol_.getHorizontalBar().addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ScrollBar b = viewcontrol_.getHorizontalBar();
                    setContentsPos(b.getSelection(), contents_y_);
                    // change "real" vertical bar selection too
                    getHorizontalBar().setSelection(b.getSelection());
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                }
            });
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Composite#setFocus()
     */
    @Override
    public boolean setFocus() {
        return viewcontrol_.forceFocus();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setCursor(org.eclipse.swt.graphics.Cursor)
     */
    @Override
    public void setCursor(Cursor cursor) {
        viewcontrol_.setCursor(cursor);
    }

    /*
     * Dispose controls used in scroll view
     * 
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    @Override
    public void dispose() {
        if (auto_scroll_ != null) {
            auto_scroll_.cancel();
            auto_scroll_ = null;
        }
        if (viewcontrol_ != null) {
            viewcontrol_.dispose();
        }
        viewcontrol_ = null;
        if (vertsb_ != null) {
            vertsb_.dispose();
        }
        vertsb_ = null;
        if (horzsb_ != null) {
            horzsb_.dispose();
        }
        horzsb_ = null;
        if (corner_control_ != null) {
            Object data = corner_control_.getData();
            if (data instanceof Overview) {
                ((Overview) data).dispose();
            }
            corner_control_.dispose();
            corner_control_ = null;
        }
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Composite#getClientArea()
     */
    @Override
    public Rectangle getClientArea() {
        return viewcontrol_.getClientArea();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        viewcontrol_.setBackground(c);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
     */
    @Override
    public void setToolTipText(String text) {
        viewcontrol_.setToolTipText(text);
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
        int x = (int) (r.width * contents_x_ / (float) contents_width_);
        int y = (int) (r.height * contents_y_ / (float) contents_height_);
        int vw = getVisibleWidth();
        int vh = getVisibleHeight();
        int w = r.width - 1;
        if (contents_width_ > vw) {
            w = (int) (r.width * vw / (float) contents_width_);
        }
        int h = r.height - 1;
        if (contents_height_ > vh) {
            h = (int) (r.height * vh / (float) contents_height_);
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
        if (localControlListener != null) {
            removeControlListener(localControlListener);
            localControlListener = null;
        }
        addControlListener(nlistener);
    }

    /**
     * Remove the local Listener and add the new listener.
     * 
     * @param nlistener the new listener
     */
    public void replaceKeyListener(KeyListener nlistener) {
        if (localKeyListener != null) {
            removeKeyListener(localKeyListener);
            localKeyListener = null;
        }
        addKeyListener(nlistener);
    }

    /**
     * Remove the local Listener and add the new listener.
     * 
     * @param nlistener the new listener
     */
    public void replaceMouseListener(MouseListener nlistener) {
        if (localMouseListener != null) {
            removeMouseListener(localMouseListener);
            localMouseListener = null;
        }
        viewcontrol_.addMouseListener(nlistener);
    }

    /**
     * Remove the local Listener and add the new listener.
     * 
     * @param nlistener the new listener
     */
    public void replaceMouseMoveListener(MouseMoveListener nlistener) {
        if (localMouseMoveListener != null) {
            removeMouseMoveListener(localMouseMoveListener);
            localMouseMoveListener = null;
        }
        viewcontrol_.addMouseMoveListener(nlistener);
    }

    /**
     * Remove the local Listener and add the new listener.
     * 
     * @param nlistener the new listener
     */
    public void replacePaintListener(PaintListener nlistener) {
        if (localPaintListener != null) {
            removePaintListener(localPaintListener);
            localPaintListener = null;
        }
        viewcontrol_.addPaintListener(nlistener);
    }

    /**
     * Access method for the contentsHeight property.
     * 
     * @return the current value of the contentsHeight property
     */
    public int getContentsHeight() {
        return contents_height_;
    }

    /**
     * Access method for the contentsWidth property.
     * 
     * @return the current value of the contentsWidth property
     */
    public int getContentsWidth() {
        return contents_width_;
    }

    /**
     * Access method for the contentsX property.
     * 
     * @return the current value of the contentsX property
     */
    public int getContentsX() {
        return contents_x_;
    }

    /**
     * Access method for the contentsY property.
     * 
     * @return the current value of the contentsY property
     */
    public int getContentsY() {
        return contents_y_;
    }

    /**
     * Determines if the dragAutoScroll property is true.
     * 
     * @return <code>true<code> if the dragAutoScroll property is true
     */
    public boolean getDragAutoScroll() {
        return auto_scroll_enabled_;
    }

    /**
     * Sets the value of the dragAutoScroll property.
     * 
     * @param aDragAutoScroll the new value of the dragAutoScroll property
     */
    public void setDragAutoScroll(boolean aDragAutoScroll) {
        auto_scroll_enabled_ = aDragAutoScroll;
        if (!auto_scroll_enabled_ && (auto_scroll_ != null)) {
            auto_scroll_.cancel();
            auto_scroll_ = null;
        }
    }

    /**
     * Change delay (in millisec) used for auto scroll feature.
     * 
     * @param _period new period between to auto scroll
     */
    public void setDragAutoScrollPeriod(int _period) {
        auto_scroll_period_ = Math.max(0, _period);
    }

    /**
     * Return auto scroll period.
     */
    public int getDragAutoScrollPeriod() {
        return auto_scroll_period_;
    }

    /**
     * Access method for the hScrollBarMode property.
     * 
     * @return the current value of the hScrollBarMode property
     */
    public int getHScrollBarMode() {
        return h_scrollbar_mode_;
    }

    /**
     * Sets the value of the hScrollBarMode property.
     * 
     * @param aHScrollBarMode the new value of the hScrollBarMode property
     */
    public void setHScrollBarMode(int aHScrollBarMode) {
        h_scrollbar_mode_ = aHScrollBarMode;
    }

    /**
     * Access method for the vScrollBarMode property.
     * 
     * @return the current value of the vScrollBarMode property
     */
    public int getVScrollBarMode() {
        return v_scrollbar_mode_;
    }

    /**
     * Sets the value of the vScrollBarMode property.
     * 
     * @param aVScrollBarMode the new value of the vScrollBarMode property
     */
    public void setVScrollBarMode(int aVScrollBarMode) {
        v_scrollbar_mode_ = aVScrollBarMode;
    }

    /**
     * Return horizontal scroll bar increment, default:1
     */
    public int getHScrollBarIncrement() {
        return h_scrollbar_increment_;
    }

    /**
     * Return vertical scroll bar increment, default:1
     */
    public int getVScrollBarIncrement() {
        return v_scrollbar_increment_;
    }

    /**
     * Change horizontal scroll bar increment, minimum:1. Page increment is always set to visible width.
     */
    public void setHScrollBarIncrement(int _inc) {
        h_scrollbar_increment_ = Math.max(1, _inc);
    }

    /**
     * Change vertical scroll bar increment, minimum:1. Page increment is always set to visible height.
     */
    public void setVScrollBarIncrement(int _inc) {
        v_scrollbar_increment_ = Math.max(1, _inc);
    }

    /**
     * Enable or disable overview feature. Enabling overview, dispose and replace existing corner control by a button.
     * Clicking in it open overview, move mouse cursor holding button to move scroll view and release button to hide
     * overview. Tip: hold control and/or shift key while moving mouse when overview is open made fine scroll.
     * 
     * @param _b true to engage overview feature
     */
    public void setOverviewEnabled(boolean _b) {
        if (getOverviewEnabled() == _b) {
            return;
        }

        Control cc = null;
        if (_b) {
            Button b = new Button(this, SWT.NONE);
            b.setText("+"); //$NON-NLS-1$
            Overview ovr = new Overview();
            ovr.useControl(b);
            b.setData(ovr);
            cc = b;
            b.setToolTipText(SDMessages._78);
        }
        setCornerControl(cc);
    }

    /**
     * Change overview size (at ratio 1:1), default is 100
     */
    public void setOverviewSize(int _size) {
        overview_size_ = Math.abs(_size);
    }

    /**
     * Returns whether the overview is enabled or not.
     * 
     * @return true is overview feature is enabled
     */
    public boolean getOverviewEnabled() {
        if (corner_control_ instanceof Button) {
            Object data = ((Button) corner_control_).getData();
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
        return overview_size_;
    }

    /**
     * Returns control used to display view (might not be this object). Use this control to add/remove listener on the
     * draw area.
     * 
     * @return control used to display view (might not be this object). 
     */
    public Control getViewControl() {
        return viewcontrol_;
    }

    /**
     * Called when the mouse enter the ScrollView area
     * 
     * @param e
     */
    protected void contentsMouseExit(MouseEvent e) {
    }

    /**
     * Called when the mouse enter the ScrollView area after and system defined time
     * 
     * @param e
     */
    protected void contentsMouseHover(MouseEvent e) {
    }

    /**
     * Called when the mouse enter the ScrollView area
     * 
     * @param e
     */
    protected void contentsMouseEnter(MouseEvent e) {
    }

    /**
     * Called when user double on contents area.
     * 
     * @param e
     */
    protected void contentsMouseDoubleClickEvent(MouseEvent e) {
    }

    /**
     * Called when mouse is on contents area and button is pressed.
     * 
     * @param e
     */
    protected void contentsMouseDownEvent(MouseEvent e) {
        mouse_down_x_ = e.x;
        mouse_down_y_ = e.y;
    }

    /** where mouse down appear on contents area */
    protected int mouse_down_x_ = -1, mouse_down_y_ = -1;

    /** TimerTask for auto scroll feature. */
    protected static class AutoScroll extends TimerTask {
        public int dx_, dy_;
        public ScrollView sv_;

        public AutoScroll(ScrollView _sv, int _dx, int _dy) {
            sv_ = _sv;
            dx_ = _dx;
            dy_ = _dy;
        }

        @Override
        public void run() {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    sv_.scrollBy(dx_, dy_);
                }
            });
        }
    }

    /** Timer for auto_scroll feature */
    protected AutoScroll auto_scroll_ = null;
    /** TimerTask for auto_scroll feature !=null when auto scroll is running */
    protected Timer auto_scroll_timer_ = null;

    /**
     * Called when mouse is on contents area and mode.
     * 
     * @param _event
     */
    protected void contentsMouseMoveEvent(MouseEvent _event) {
        if ((_event.stateMask & SWT.BUTTON_MASK) != 0) {
            if (!auto_scroll_enabled_) {
                scrollBy(-(_event.x - mouse_down_x_), -(_event.y - mouse_down_y_));
                return;
            }

            int sx = 0, sy = 0;

            int v_right = getContentsX() + getVisibleWidth();
            int v_bottom = getContentsY() + getVisibleHeight();

            // auto scroll... ?
            if (_event.x < getContentsX()) {
                sx = (getContentsX() - _event.x);
                mouse_down_x_ = getContentsX();
            } else if (_event.x > v_right) {
                sx = -_event.x + v_right;
                mouse_down_x_ = v_right;
            }
            if (_event.y < getContentsY()) {
                sy = (getContentsY() - _event.y);
                mouse_down_y_ = getContentsY();
            } else if (_event.y > v_bottom) {
                sy = -_event.y + v_bottom;
                mouse_down_y_ = v_bottom;
            }

            if (sx != 0 || sy != 0) {
                // start auto scroll...
                if (auto_scroll_ == null) {
                    if (auto_scroll_timer_ == null) {
                        auto_scroll_timer_ = new Timer(true);
                    }
                    auto_scroll_ = new AutoScroll(this, sx, sy);
                    auto_scroll_timer_.schedule(auto_scroll_, 0, auto_scroll_period_);
                } else {
                    auto_scroll_.dx_ = sx;
                    auto_scroll_.dy_ = sy;
                }
            } else {
                if (auto_scroll_ != null) {
                    auto_scroll_.cancel();
                    auto_scroll_ = null;
                }

                scrollBy(-(_event.x - mouse_down_x_), -(_event.y - mouse_down_y_));
            }
        }
    }

    /**
     * Called when mouse is on contents area and button is released
     * 
     * @param _event
     */
    protected void contentsMouseUpEvent(MouseEvent _event) {
        // reset auto scroll if it's engaged
        if (auto_scroll_ != null) {
            auto_scroll_.cancel();
            auto_scroll_ = null;
        }
    }

    /**
     * Responsible to draw contents area. At least rectangle clipX must be redrawn. This rectangle is given in contents
     * coordinates. By default, no paint is produced.
     * 
     * @param gc
     * @param clipx
     * @param clipy
     * @param clipw
     * @param cliph
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
        // System.out.println("SV--resizeContents("+_w+","+_h+" ) {");
        if (width < 0) {
            width = 0;
        }
        if (height < 0) {
            height = 0;
        }

        int oldW = contents_width_;
        int oldH = contents_height_;

        if (width == oldW && height == oldH) {
            return;
        }

        // System.out.println("RESIZE-CONTENTS("+_w+","+_h+")");
        contents_width_ = width;
        contents_height_ = height;

        if (oldW > width) {
            int s = width;
            width = oldW;
            oldW = s;
        }

        int vis_width = getVisibleWidth();
        int vis_height = getVisibleHeight();
        if (oldW < vis_width) {
            if (width > vis_width) {
                width = vis_width;
            }
            viewcontrol_.redraw(getContentsX() + oldW, 0, width - oldW, vis_height, true);
        }

        if (oldH > height) {
            int s = height;
            height = oldH;
            oldH = s;
        }

        if (oldH < vis_height) {
            if (height > vis_height) {
                height = vis_height;
            }
            viewcontrol_.redraw(0, getContentsY() + oldH, vis_width, height - oldH, true);
        }
        if (updateScrollBarVisiblity()) {
            layout();
        } else {
            updateScrollBarsValues();
        }
        // System.out.println("SV--resizeContents() }");
    }

    // redefined for internal use ..
    @Override
    public void redraw() {
        super.redraw();
        // ..need to redraw this already:
        viewcontrol_.redraw();
    }

    /**
     * @param _dx
     * @param _dy
     */
    public void scrollBy(int _dx, int _dy) {
        setContentsPos(getContentsX() + _dx, getContentsY() + _dy);
    }

    /**
     * Scroll to ensure point(in contents coordinates) is visible.
     */
    public void ensureVisible(int _px, int _py) {
        int cx = getContentsX(), cy = getContentsY();
        int right = getContentsX() + getVisibleWidth();
        int bottom = getContentsY() + getVisibleHeight();
        if (_px < getContentsX()) {
            cx = _px;
        } else if (_px > right) {
            cx = _px - getVisibleWidth();
        }
        if (_py < getContentsY()) {
            cy = _py;
        } else if (_py > bottom) {
            cy = _py - getVisibleHeight();
        }
        setContentsPos(cx, cy);
    }

    /**
     * Make rectangle (_x,_y,_w,_h, in contents coordinates) visible. if rectangle cannot be completely visible, use
     * _align flags.
     * 
     * @param _x x contents coordinates of rectangle.
     * @param _y y contents coordinates of rectangle.
     * @param _w width of rectangle.
     * @param _h height of rectangle.
     * @param _align bit or'ed SWT flag like SWT.LEFT,RIGHT,CENTER,TOP,BOTTOM,VERTICAL used only for bigger rectangle
     *            than visible area. By default CENTER/VERTICAL
     */
    public void ensureVisible(int _x, int _y, int _w, int _h, int _align) {
        ensureVisible(_x, _y, _w, _h, _align, false);
    }

    /**
     * Make rectangle (_x,_y,_w,_h, in contents coordinates) visible. if rectangle cannot be completely visible, use
     * _align flags.
     * 
     * @param _x x contents coordinates of rectangle.
     * @param _y y contents coordinates of rectangle.
     * @param _w width of rectangle.
     * @param _h height of rectangle.
     * @param _align bit or'ed SWT flag like SWT.LEFT,RIGHT,CENTER,TOP,BOTTOM,VERTICAL used only for bigger rectangle
     *            than visible area. By default CENTER/VERTICAL
     * @param forceAlign force alignment for rectangle smaller than the visible area
     */
    protected void ensureVisible(int _x, int _y, int _w, int _h, int _align, boolean forceAlign) {
        if (_w < 0) {
            _x = _x + _w;
            _w = -_w;
        }
        if (_h < 0) {
            _y = _y + _h;
            _h = -_h;
        }
        int hbar = getHorizontalBarHeight();
        int vbar = getVerticalBarWidth();
        int cx = getContentsX(), cy = getContentsY();
        int right = getContentsX() + getVisibleWidth() - vbar;
        int bottom = getContentsY() + getVisibleHeight() - hbar;
        boolean align_h = false, align_v = false;

        if (_x < getContentsX()) {
            cx = _x;
        } else if (_x + _w > right) {
            cx = _x - _w;
        }
        if (_y < getContentsY()) {
            cy = _y;
        } else if (_y + _h > bottom) {
            cy = _y - _h;
        }

        if (_w > getVisibleWidth()) {
            align_h = true;
        }
        if (_h > getVisibleHeight()) {
            align_v = true;
        }
        // compute alignment on visible area horizontally
        if (align_h || (forceAlign && _x + _w > right)) {
            // use _align flags
            if ((_align & SWT.LEFT) != 0) {
                cx = _x;
            } else if ((_align & SWT.RIGHT) != 0) {
                cx = right - _w;
            } else { // center
                cx = _x + (_w - getVisibleWidth()) / 2;
            }
        }
        // compute alignment on visible area vertically
        if (align_v || (forceAlign && _y + _h > bottom)) {
            // use _align flags
            if ((_align & SWT.TOP) != 0) {
                cy = _y;
            } else if ((_align & SWT.BOTTOM) != 0) {
                cy = bottom - _h;
            } else { // center
                cy = _y + (_h - getVisibleHeight()) / 2;
            }
        }
        setContentsPos(cx, cy);
    }

    /**
     * Returns true if point is visible (expressed in contents coordinates).
     * 
     * @return true if point is visible (expressed in contents coordinates)
     */
    public boolean isVisible(int _px, int _py) {
        if (_px < getContentsX()) {
            return false;
        }
        if (_py < getContentsY()) {
            return false;
        }
        if (_px > (getContentsX() + getVisibleWidth())) {
            return false;
        }
        if (_py > (getContentsY() + getVisibleHeight())) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if rectangle if partially visible.
     * 
     * @return true if rectangle if partially visible.
     */
    public boolean isVisible(int _x, int _y, int _w, int _h) {
        if (_x + _w < getContentsX()) {
            return false;
        }
        if (_y + _h < getContentsY()) {
            return false;
        }
        int vr = getContentsX() + getVisibleWidth();
        int vb = getContentsY() + getVisibleHeight();
        if (_x > vr) {
            return false;
        }
        if (_y > vb) {
            return false;
        }
        return true;
    }

    /**
     * Returns visible part of rectangle, or null if rectangle is not visible. Rectangle is expressed in contents
     *         coordinates.
     * @return visible part of rectangle, or null if rectangle is not visible. 
     */
    public Rectangle getVisiblePart(int _x, int _y, int _w, int _h) {
        if (_x + _w < getContentsX()) {
            return null;
        }
        if (_y + _h < getContentsY()) {
            return null;
        }
        int vr = getContentsX() + getVisibleWidth();
        int vb = getContentsY() + getVisibleHeight();
        if (_x > vr) {
            return null;
        }
        if (_y > vb) {
            return null;
        }
        int rr = _x + _w, rb = _y + _h;
        int nl = Math.max(_x, getContentsX()), nt = Math.max(_y, getContentsY()), nr = Math.min(rr, vr), nb = Math.min(rb, vb);
        Rectangle c = new Rectangle(nl, nt, nr - nl, nb - nt);
        return c;
    }

    /**
     * Returns the visible part for given rectangle. 
     * 
     * @param _r A rectangle
     * 
     * @return gets visible part of rectangle (or <code>null</code>)
     */
    public final Rectangle getVisiblePart(Rectangle _r) {
        if (_r == null) {
            return null;
        }
        return getVisiblePart(_r.x, _r.y, _r.width, _r.height);
    }

    /**
     * Change top left position of visible area. Check if the given point is inside contents area.
     * 
     * @param _x x contents coordinates of visible area.
     * @param _y y contents coordinates of visible area.
     * @return true if view really moves
     */
    public boolean setContentsPos(int _x, int _y) {
        int nx = _x, ny = _y;
        if (getVisibleWidth() >= getContentsWidth()) {
            nx = 0;
        } else {
            if (_x < 0) {
                nx = 0;
            } else if (_x + getVisibleWidth() > getContentsWidth()) {
                nx = getContentsWidth() - getVisibleWidth();
            }
        }
        if (getVisibleHeight() >= getContentsHeight()) {
            ny = 0;
        } else {
            if (_y <= 0) {
                ny = 0;
            } else if (_y + getVisibleHeight() > getContentsHeight()) {
                ny = getContentsHeight() - getVisibleHeight();
            }
        }
        // no move
        if (nx == contents_x_ && ny == contents_y_) {
            return false;
        }
        contents_x_ = nx;
        contents_y_ = ny;
        updateScrollBarsValues();
        // ? find smallest area to redraw only them ?
        viewcontrol_.redraw();
        return true;
    }

    // redefined to return our vertical bar
    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Scrollable#getVerticalBar()
     */
    @Override
    public ScrollBar getVerticalBar() {
        return vertsb_.getVerticalBar();
    }

    // redefined to return out horizontal bar
    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Scrollable#getHorizontalBar()
     */
    @Override
    public ScrollBar getHorizontalBar() {
        return horzsb_.getHorizontalBar();
    }

    /**
     * Compute visibility of vertical/horizontal bar using given width/height and current visibility (i.e. is bar size are already in
     * for_xxx)
     * @param for_width width of foreground
     * @param for_height height of foreground
     * @param curr_h_vis The current visibility state of horizontal scroll bar
     * @param curr_v_vis The current visibility state of vertical scroll bar
     * @return <code>true</code> if visibility changed else <code>false</code> 
     */
    public int computeBarVisibility(int for_width, int for_height, boolean curr_h_vis, boolean curr_v_vis) {
        int vis = 0x00;
        switch (v_scrollbar_mode_) {
        case ALWAYS_OFF:
            break;
        case ALWAYS_ON:
            vis |= VBAR;
            break;
        case AUTO:
            if (getContentsHeight() > for_height) {
                vis = VBAR;
                // v bar size is already in for_width.
                if (!curr_v_vis) {// (curr_vis&0x01)==0)
                    for_width -= getVerticalBarWidth();
                }
            }
            break;
        }

        switch (h_scrollbar_mode_) {
        case ALWAYS_OFF:
            break;
        case ALWAYS_ON:
            vis |= HBAR;
            break;
        case AUTO:
            if (getContentsWidth() > for_width) {
                vis |= HBAR;
                // h bar is not in for_height
                if (!curr_h_vis)// (curr_vis&0x02)==0 )
                {
                    if (getContentsHeight() > for_height - getHorizontalBarHeight()) {
                        vis |= VBAR;
                    }
                }
            }
            break;
        }
        return vis;
    }

    /**
     * setup scroll bars visibility, return true if one of visibility changed.
     */
    protected boolean updateScrollBarVisiblity() {
        boolean change = false;

        boolean curr_v_vis = vertsb_.getVisible();
        boolean curr_h_vis = horzsb_.getVisible();
        int bar_new_vis = computeBarVisibility(getVisibleWidth(), getVisibleHeight(), curr_h_vis, curr_v_vis);
        boolean new_v_vis = (bar_new_vis & VBAR) != 0;
        boolean new_h_vis = (bar_new_vis & HBAR) != 0;
        // System.out.println("SV--updateScrollBarVis old, h:"+curr_h_vis+" v:"+curr_v_vis+" new="+bar_new_vis);
        if (curr_v_vis ^ new_v_vis) // vertsb_.getVisible() )
        {
            vertsb_.setVisible(new_v_vis);
            change = true;
        }
        if (curr_h_vis ^ new_h_vis) {
            horzsb_.setVisible(new_h_vis);
            change = true;
        }

        // update corner control visibility:
        if (corner_control_ != null && change) {
            boolean vis = new_v_vis || new_h_vis;
            if (vis ^ corner_control_.getVisible()) {
                corner_control_.setVisible(vis);
                change = true; // but must be already the case
            }
        }
        return change;
    }

    /**
     * Setup scroll bar using contents, visible and scroll bar mode properties.
     */
    protected void updateScrollBarsValues() {
        // System.out.println("UPDATE-SCROLL-BAR-VALUES");
        /* update vertical scrollbar */
        ScrollBar b = getVerticalBar();
        if (b != null) {
            b.setMinimum(0);
            b.setMaximum(getContentsHeight());
            b.setThumb(getVisibleHeight());
            b.setPageIncrement(getVisibleHeight());
            b.setIncrement(v_scrollbar_increment_);
            b.setSelection(getContentsY());
        }

        // update "hidden" vertical bar too
        b = viewcontrol_.getVerticalBar();
        if (b != null) {
            b.setMinimum(0);
            b.setMaximum(getContentsHeight());
            b.setThumb(getVisibleHeight());
            b.setPageIncrement(getVisibleHeight());
            b.setIncrement(v_scrollbar_increment_);
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
            b.setIncrement(h_scrollbar_increment_);
        }
        // update "hidden" horizontal bar too
        b = viewcontrol_.getHorizontalBar();
        if (b != null) {
            b.setMinimum(0);
            b.setMaximum(getContentsWidth());
            b.setThumb(getVisibleWidth());
            b.setSelection(getContentsX());
            b.setPageIncrement(getVisibleWidth());
            b.setIncrement(h_scrollbar_increment_);
        }
    }

    /**
     * Change the control used in the bottom right corner (between two scrollbar), if control is null reset previous
     * corner control. This control is visible only if at least one scrollbar is visible. Given control will be disposed
     * by ScrollView, at dispose() time, at next setCornetControl() call or when calling setOverviewEnabled(). Pay
     * attention calling this reset overview feature until setOverviewEnabled(true) if called.
     * @param _w The control for the overview 
     */
    public void setCornerControl(Control _w) {
        if (corner_control_ != null) {
            corner_control_.dispose();
        }
        corner_control_ = _w;
        if (corner_control_ != null) {
            ScrollBar vb = getVerticalBar();
            ScrollBar hb = getHorizontalBar();
            boolean vis = vb.getVisible() || hb.getVisible();
            corner_control_.setVisible(vis);
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
        Point p = new Point(viewToContentsX(x), viewToContentsY(y));
        return p;
    }

    /** 
     * Transform x in widget coordinates to contents coordinates
     * 
     * @param _x The y widget coordinate.
     * @return the x content coordinate. 
     */
    public int viewToContentsX(int _x) {
        return contents_x_ + _x;
    }

    /** 
     * Transform y in widget coordinates to contents coordinates
     *  
     * @param _y The y widget coordinate.
     * @return the y content coordinate. 
     */
    public int viewToContentsY(int _y) {
        return contents_y_ + _y;
    }

    /**
     * Transform (x,y) point from contents coordinates, to widget coordinates.
     * 
     * @param x The x content coordinate.
     * @param y The y content coordinate.
     * @return coordinates widget area as.
     */
    public final Point contentsToView(int x, int y) {
        Point p = new Point(contentsToViewX(x), contentsToViewY(y));
        return p;
    }

    /**
     * Transform X axis coordinates from contents to widgets.
     * 
     * @param _x contents coordinate to transform.
     * @return x coordinate in widget area
     */
    public int contentsToViewX(int _x) {
        return _x - contents_x_;
    }

    /**
     * Transform Y axis coordinates from contents to widgets.
     * 
     * @param _y contents coordinate to transform
     * @return y coordinate in widget area
     */
    public int contentsToViewY(int _y) {
        return _y - contents_y_;
    }

    /**
     * Return the visible height of scroll view, might be > contentsHeight
     * 
     * @return the visible height of scroll view, might be > contentsHeight()
     */
    public int getVisibleHeight() {
        Rectangle r = viewcontrol_.getClientArea();
        return r.height;
    }

    /**
     * Return int the visible width of scroll view, might be > contentsWidth().
     * 
     * @return int the visible width of scroll view, might be > contentsWidth()
     */
    public int getVisibleWidth() {
        Rectangle r = viewcontrol_.getClientArea();
        return r.width;
    }

    /**
     * Add support for arrow key, scroll the ... scroll view. But you can redefine this method for your convenience.
     */
    protected void keyPressedEvent(KeyEvent _e) {
        switch (_e.keyCode) {
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
        }
    }

    /** 
     * Redefine this method at your convenience
     * @param _e The key event.
     */
    protected void keyReleasedEvent(KeyEvent _e) {
    }

    /**
     * Called when ScrollView view is resized.
     */
    protected void viewResized() {
        // System.out.println("SV--viewResizeEvent()");
        // scroll contents x,y if visible area bigger than contents...
        if (!setContentsPos(getContentsX(), getContentsY())) {
            // if no scroll done, scroll bar must be updated.
            /*
             * if( ocx==getContentsX() && ocy==getContentsY() ) { updateScrollBars(); }
             */
        }
    }

    /**
     * Returns vertical bar width, even if bar isn't visible.
     * 
     * @return vertical bar width, even if bar isn't visible 
     */
    public int getVerticalBarWidth() {
        // include vertical bar width and trimming of scrollable used
        int bw = vertsb_.computeTrim(0, 0, 0, 0).width;
        return bw + 1;
    }

    /**
     * Returns horizontal bar height even if bar isn't visible.
     * 
     * @return horizontal bar height even if bar isn't visible 
     */
    public int getHorizontalBarHeight() {
        // include horiz. bar height and trimming of scrollable used
        int bh = horzsb_.computeTrim(0, 0, 0, 0).height;
        // +1 because win32 H.bar need 1 pixel canvas size to appear ! (strange no ?)
        return bh + 1;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.widgets.Scrollable#computeTrim(int, int, int, int)
     */
    @Override
    public Rectangle computeTrim(int x, int y, int w, int h) {
        Rectangle r = new Rectangle(x, y, w, h);
        int bar_vis = computeBarVisibility(w, h, false, false);
        if ((bar_vis & VBAR) != 0) {
            r.width += getVerticalBarWidth();
        }
        if ((bar_vis & HBAR) != 0) {
            r.height += getHorizontalBarHeight();
        }
        return r;
    }

    /**
     *  Internal layout for ScrollView, handle scrollbars, drawzone and corner control 
     */
    protected class SVLayout extends Layout {
        int seek = 0;
        boolean dont_layout = false;
        
        /*
         * (non-Javadoc)
         * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
         */
        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            Point p = new Point(250, 250);
            if (contents_width_ < p.x) {
                p.x = contents_width_;
            }
            if (contents_height_ < p.y) {
                p.y = contents_height_;
            }
            return p;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
         */
        @Override
        protected void layout(Composite composite, boolean flushCache) {
            if (dont_layout) {
                return;
            }
            seek++;
            if (seek > 10) {
                dont_layout = true;
            }

            // System.out.println(">>> SV.layout() {");
            Point cs = composite.getSize();
            int bar_vis = computeBarVisibility(cs.x, cs.y, false, false);
            boolean vb_vis = (bar_vis & VBAR) != 0;
            boolean hb_vis = (bar_vis & HBAR) != 0;
            vertsb_.setVisible(vb_vis);
            horzsb_.setVisible(hb_vis);
            int vbw = getVerticalBarWidth();
            int hbh = getHorizontalBarHeight();
            int wb = vb_vis ? vbw : 0;
            int hb = hb_vis ? hbh : 0;
            int cww = 0, cwh = 0;
            // System.out.println("SV-LAYOUT H.vis="+hb_vis+" V.vis="+vb_vis);

            if (corner_control_ != null && (vb_vis || hb_vis)) { // corner_control_.getVisible())
                corner_control_.setVisible(true);
                cww = vbw;
                cwh = hbh;
                if (wb == 0)
                    wb = vbw;
                if (hb == 0)
                    hb = hbh;
            } else if (vb_vis && hb_vis) {
                if (corner_control_ != null) {
                    corner_control_.setVisible(false);
                }
                cww = vbw;
                cwh = hbh;
            }
            if (vb_vis || hb_vis) {
                updateScrollBarsValues();
            }

            int vw = cs.x - (vb_vis ? vbw : 0);
            int vh = cs.y - (hb_vis ? hbh : 0);
            int vbx = cs.x - wb;
            int hby = cs.y - hb;
            Rectangle rc = viewcontrol_.getClientArea();
            int old_width = rc.width;
            int old_height = rc.height;
            // provoque pas un viewResize ???
            viewcontrol_.setBounds(0, 0, vw, vh);
            boolean do_view_resize = false;
            rc = viewcontrol_.getClientArea();
            if (old_width != rc.width || old_height != rc.height) {
                // area size change, so visibleWidth()/Height() change too
                // so scrollbars visibility ma change too..
                // so need an other layout !
                /*
                 * if( updateScrollBarVisiblity() ) { layout( composite, flushCache);
                 * System.out.println("<<< SV.layout() } (recursive)"); return ; }
                 */
                do_view_resize = true;
            }
            if (vb_vis) {
                vertsb_.setBounds(vbx, 0, wb, cs.y - cwh);
            }
            if (hb_vis) {
                horzsb_.setBounds(0, hby, cs.x - cww, hb);
            }
            if (corner_control_ != null && corner_control_.getVisible()) {
                corner_control_.setBounds(vbx, hby, vbw, hbh);
            }
            updateScrollBarsValues();
            if (do_view_resize) {
                // System.out.println(" -layout do_view_resize old="+old_width+"x"+old_height+" new="+viewcontrol_.getClientArea());
                viewResized();
            }
            // System.out.println("<<< SV.layout() }");
            seek--;
            if (seek == 0) {
                dont_layout = false;
            }
        }
    }

    // static must take place here... cursor is created once.
    static Cursor overview_cursor_;

    /** Support for click-and-see overview shell on this ScrollView */
    protected class Overview {
        // factors from real and overview sizes, for mouse move speed.
        protected float overview_factor_x_, overview_factor_y_;
        // shell use to show overview
        protected Shell overview;
        // save mouse cursor location for disappear();
        protected int save_cursor_x, save_cursor_y;

        /** apply overview support on a control. Replace existing corner_widget */
        public void useControl(Control _c) {
            final Point pos = _c.getLocation();
            _c.addMouseListener(new MouseListener() {
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

            _c.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (overviewing())
                        overviewDisappear(false);
                }

            });
            _c.addKeyListener(new KeyListener() {

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.keyCode == 32 && !overviewing()) {
                        overviewAppear(pos.x, pos.y);
                    } else if (e.keyCode == 32) {
                        overviewDisappear();
                    }
                    if (e.keyCode == SWT.ARROW_DOWN) {
                        overviewMove(0, 1, e);
                    }

                    if (e.keyCode == SWT.ARROW_UP) {
                        overviewMove(0, -1, e);
                    }

                    if (e.keyCode == SWT.ARROW_RIGHT) {
                        overviewMove(1, 0, e);
                    }

                    if (e.keyCode == SWT.ARROW_LEFT) {
                        overviewMove(-1, 0, e);
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }
            });
            _c.addMouseMoveListener(new MouseMoveListener() {
                private int refReshCount  = 0;
                @Override
                public void mouseMove(MouseEvent e) {
                    if (overviewing()) {
                        // Slow down the refresh
                        if (refReshCount % 4 == 0) {
                            overviewMove(e);
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
            if (overview != null) {
                overview.dispose();
            }
        }

        /** 
         * @return true if overview is currently on screen
         */
        protected boolean overviewing() {
            return (overview != null && overview.isVisible());
        }

        /** 
         * Process overview appear
         */
        protected void overviewAppear(int mx, int my) {
            if (overview == null) {
                overview = new Shell(getShell(), SWT.ON_TOP | SWT.NO_BACKGROUND);
                overview.addPaintListener(new PaintListener() {
                    @Override
                    public void paintControl(PaintEvent e) {
                        drawOverview(e.gc, overview.getClientArea());
                    }
                });
            }
            // always the same..
            // overview.setBackground( viewcontrol_.getBackground() );
            overview.setForeground(viewcontrol_.getForeground());

            // get location of shell (in screeen coordinates)
            Point p = toGlobalCoordinates(corner_control_, 0, 0);
            int x = p.x;
            int y = p.y;
            int w, h;
            w = h = overview_size_;
            Rectangle scr = getDisplay().getBounds();
            Point ccs = corner_control_.getSize();
            try {
                if (contents_width_ > contents_height_) {
                    float ratio = contents_height_ / (float) contents_width_;
                    h = (int) (w * ratio);
                    if (h < ccs.y) {
                        h = ccs.y;
                    } else if (h >= scr.height / 2) {
                        h = scr.height / 2;
                    }
                } else {
                    float ratio = contents_width_ / (float) contents_height_;
                    w = (int) (h * ratio);
                    if (w < ccs.x) {
                        w = ccs.x;
                    } else if (w >= scr.width / 2) {
                        w = scr.width / 2;
                    }
                }
                overview_factor_x_ = contents_width_ / (float) w;
                overview_factor_y_ = contents_height_ / (float) h;
            }
            // no contents size set ?
            catch (java.lang.ArithmeticException e) {
            }

            // try pop-up on button, extending to bottom right,
            // if outside screen, extend pop-up to top left
            // if( x+w > scr.width ) x = scr.width-w; //x += corner_control_.getSize().x-w;
            // if( y+h > scr.height ) y = scr.height-h;//y += corner_control_.getSize().y-h;
            if (x <= 0) {
                x = 1;
            }
            if (y <= 0) {
                y = 1;
            }
            x = x - w + ccs.x;
            y = y - h + ccs.y;
            overview.setBounds(x, y, w, h);
            overview.setVisible(true);
            overview.redraw();
            // mouse cursor disappear, so set invisible mouse cursor ...
            if (overview_cursor_ == null) {
                RGB rgb[] = { new RGB(0, 0, 0), new RGB(255, 0, 0) };
                PaletteData pal_ = new PaletteData(rgb);
                int s = 1;
                byte src[] = new byte[s * s];
                byte msk[] = new byte[s * s];
                for (int i = 0; i < s * s; ++i)
                    src[i] = (byte) 0xFF;
                ImageData i_src = new ImageData(s, s, 1, pal_, 1, src);
                ImageData i_msk = new ImageData(s, s, 1, pal_, 1, msk);
                overview_cursor_ = new Cursor(null, i_src, i_msk, 0, 0);
            }
            corner_control_.setCursor(overview_cursor_);
            // convert to global coordinates
            p = toGlobalCoordinates(corner_control_, mx, my);
            save_cursor_x = p.x;
            save_cursor_y = p.y;

            Rectangle r = overview.getClientArea();
            int cx = (int) (r.width * contents_x_ / (float) contents_width_);
            int cy = (int) (r.height * contents_y_ / (float) contents_height_);

            // cx,cy to display's global coordinates
            p = toGlobalCoordinates(overview.getParent(), cx, cy);
            cx = p.x;
            cy = p.y;

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
            if (overview == null)
                return;
            overview.setVisible(false);
            corner_control_.setCursor(null);
            if (restoreCursorLoc) {
                getDisplay().setCursorLocation(save_cursor_x, save_cursor_y);
            }
            overview.dispose();
            overview = null;
        }

        /**
         * Process mouse move in overview
         * @param event The mouse event
         */
        protected void overviewMove(MouseEvent event) {
            Point p = toGlobalCoordinates(corner_control_, event.x, event.y);
            int dx = p.x - save_cursor_x;
            int dy = p.y - save_cursor_y;
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
                getDisplay().setCursorLocation(save_cursor_x, save_cursor_y);
                ctrl = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;
            } else if (event instanceof KeyEvent) {
                KeyEvent e = (KeyEvent) event;
                ctrl = (e.stateMask & SWT.CONTROL) != 0;
                shift = (e.stateMask & SWT.SHIFT) != 0;
            }

            int cx = contents_x_;
            int cy = contents_y_;
            float fx = overview_factor_x_;
            float fy = overview_factor_y_;

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
            if (cx != contents_x_ || cy != contents_y_) {
                overview.redraw();
                overview.update(); // draw now !
            }
        }

        /**
         * Convert overview coordinates to global coordinates.
         * 
         * @param _loc the control reference
         * @param _x The x coordinate to convert
         * @param _y The y coordinate to convert
         * @return
         */
        protected Point toGlobalCoordinates(Control _loc, int _x, int _y) {
            Point p = new Point(_x, _y);
            for (Control c = _loc; c != null; c = c.getParent()) {
                // control might have client area with 'decorations'
                int trim_x = 0, trim_y = 0;
                // other kind of widget with trimming ??
                if (c instanceof Scrollable) {
                    Scrollable s = (Scrollable) c;
                    Rectangle rr = s.getClientArea();
                    Rectangle tr = s.computeTrim(rr.x, rr.y, rr.width, rr.height);
                    trim_x = rr.x - tr.x;
                    trim_y = rr.y - tr.y;
                }
                p.x += c.getLocation().x + trim_x;
                p.y += c.getLocation().y + trim_y;
            }
            return p;
        }
    }
}
