/*******************************************************************************
 * Copyright (c) 2010, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * Philippe Sawicki (INF4990.A2010@gmail.com) - Initial API and implementation 
 * Mathieu Denis (mathieu.denis55@gmail.com) - Refactored code
 * Bernd Hufmann - Changed implemented interface to MouseTraceListener
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.latency.listeners;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;

/**
 *  <b><u>AbstractMouseListener</u></b>
 * <p>
 * AbstractMouseListener, base class for the canvas mouse listener.
 * 
 * @author Philippe Sawicki
 */
public abstract class AbstractMouseTrackListener implements MouseTrackListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Mouse x-coordinate.
     */
    protected int fMouseX;
    /**
     * Mouse y-coordinate.
     */
    protected int fMouseY;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.MouseTrackListener#mouseEnter(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseEnter(MouseEvent event) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.MouseTrackListener#mouseExit(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseExit(MouseEvent event) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.swt.events.MouseTrackListener#mouseHover(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseHover(MouseEvent event) {
        fMouseX = event.x;
        fMouseY = event.y;
        display();
    }

    /**
     * Tooltip display callback.
     */
    protected abstract void display();

}