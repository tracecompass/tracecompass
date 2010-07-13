/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque - Initial API and implementation
 *   
 * Modifications:
 * 2010-06-20 Yuriy Vashchuk - Selection "red square" window optimisation.
 * 							   Null pointer exception correction.
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;

/**
 * <b><u>HistogramCanvasMouseListener</u></b>
 * <p>
 * Implementation of a MouseListener for the need of the HistogramCanvas
 * <p> 
 */
public class HistogramCanvasMouseListener implements MouseMoveListener, MouseListener, MouseWheelListener 
{
	protected DelayedMouseScroll mouseScrollListener = null;
	protected HistogramCanvas parentCanvas = null;
	
	protected boolean isWindowMoving = false;
	
	/**
	 * HistogramCanvasMouseListener constructor
	 * 
	 * @param newCanvas Related canvas
	 */
	public HistogramCanvasMouseListener(HistogramCanvas newCanvas) {
		parentCanvas = newCanvas;
	}
	
	/**
	 * Function called when the mouse is moved.<p>
	 * If the mouse button is clicked, we will move the selection window.
	 * 
	 * @param event  The generated mouse event when the mouse moved.
	 */
	public void mouseMove(MouseEvent event) {
		if ( parentCanvas.getHistogramContent() != null && isWindowMoving == true ) {
			parentCanvas.setWindowCenterPosition(event.x);
		}
	}
	
	/**
	 * Function called when the mouse buttons are clicked.<p>
	 * If the button is the first one (left button), turn on the "move window" mode
	 * 
	 * @param event  The generated mouse event when the mouse button was pressed.
	 */
	public void mouseDown(MouseEvent event) {
		if ( parentCanvas.getHistogramContent() != null && event.button == 1) {
			isWindowMoving = true;
			parentCanvas.setWindowCenterPosition(event.x);
		}
	}
	
	/**
	 * Function called when the mouse buttons are released.<p>
	 * If the button is the first one (left button), turn off the "move window" mode
	 * 
	 * @param event  The generated mouse event when the mouse button was released.
	 */
	public void mouseUp(MouseEvent event) {
		if ( parentCanvas.getHistogramContent() != null && event.button == 1) {
			isWindowMoving = false;
			parentCanvas.notifyParentSelectionWindowChangedAsynchronously();
		}
	}
	
	/**
	 * Function called when the mouse perform a double-click.<p>
	 * Don't do anything yet...
	 * 
	 * @param event  The generated mouse event when the mouse double-click was issued.
	 */
	public void mouseDoubleClick(MouseEvent event) {
//		System.out.println("mouseDoubleClick");
	}
	
	/**
	 * Function called when the mouse scroll button is used.<p>
	 * Start a "ScrollListener" that will wait for more scroll clicks as they are asynchonous.
	 * After a certain delay, the parent canvas will get notified.   
	 * 
	 * @param event  The generated mouse event when the mouse scroll was spinned.
	 */
	public void mouseScrolled(MouseEvent event) {
		
		// Start a scrollListener if none exist yet and start its thread
		// Otherwise, we will just notify the one that is currenly alive...
		// Badly timed event could happen while the thread is dying but we can live with loss scroll events, I believe. 
		if ( mouseScrollListener == null ) {
			mouseScrollListener = new DelayedMouseScroll(this, HistogramConstant.FULL_WAIT_MS_TIME_BETWEEN_MOUSE_SCROLL, HistogramConstant.INTERVAL_WAIT_MS_TIME_BETWEEN_POLL );
			mouseScrollListener.start();
		}
		
		// *** NOTE ***
		// We need to refer to the "count" to know if the scroll is done backward or forward.
		// Positive count mean it is done backward (from the wall in the direction of the hand)
		// Negative count mean it is done backward (from the hand in the direction of the wall)
		if ( event.count > 0) {
			mouseScrollListener.incrementMouseScroll();
		}
		else {
			mouseScrollListener.decrementMouseScroll();
		}
	}
	
	/**
	 * Function that will be called at the end of the "wait time" for scroll events.<p>
	 * This will calculate the correct zoom time and call the canvas to resize its selection window.
	 * 
	 * @param nbMouseScroll
	 */
	public void receiveMouseScrollCount(int nbMouseScroll) {
		
		if(parentCanvas.getHistogramContent() != null) { 
		
			mouseScrollListener = null;
			
			long ajustedTime = 0;
			
			// If we received Negative scroll event, ZoomOut by ZOOM_OUT_FACTOR * the number of scroll events received.
			if ( nbMouseScroll < 0 ) {
				ajustedTime = (long)((double)parentCanvas.getSelectedWindowSize() * HistogramConstant.ZOOM_OUT_FACTOR);
				ajustedTime = ajustedTime * Math.abs(nbMouseScroll);
				ajustedTime = parentCanvas.getSelectedWindowSize() + ajustedTime;
			}
			// If we received Positive scroll event, ZoomIn by ZOOM_IN_FACTOR * the number of scroll events received.
			else {
				ajustedTime = (long)((double)parentCanvas.getSelectedWindowSize() * HistogramConstant.ZOOM_IN_FACTOR);
				ajustedTime = ajustedTime * Math.abs(nbMouseScroll);
				ajustedTime = parentCanvas.getSelectedWindowSize() - ajustedTime;
			}
			
			// Resize the canvas selection window  
			parentCanvas.resizeWindowByAbsoluteTime(ajustedTime);
		}
	}
}

/**
 * <b><u>DelayedMouseScroll Inner Class</u></b>
 * <p>
 * Asynchronous "Mouse Scroll Listener"
 * <p>
 * This class role is to wait for mouse scroll and count them during a certain delay.<p>
 * Once the time is up, it will notify the mouse listener of the number of scroll events received.<p>
 * 
 * Note that a new scroll event received will reset the wait timer.
 */
class DelayedMouseScroll extends Thread {
	
	private HistogramCanvasMouseListener mouseListener = null;
	
	private long waitTimeBetweenScroll = 0;
	private long waitTimeBetweenCheck = 0;
	
	private long lastScrollTime = 0L;
	private int nbScrollClick = 0;
	
	/**
	 * Constructor of the DelayedMouseScroll listener.<p>
	 * Object will be initialized but start() need to be called for it start listening for scroll.
	 * 
	 * @param newListener			The parent mouse listener
	 * @param newWaitFullTime		The time to wait for scroll events
	 * @param newWaitBeforeCheck	The delay between polling for scroll events
	 */
	public DelayedMouseScroll(HistogramCanvasMouseListener newListener, long newWaitFullTime, long newWaitBeforePoll) {
		
		mouseListener = newListener;
		
		// Get the current system time. 
		// This will be used to determine since how long we wait for click
		lastScrollTime = System.currentTimeMillis();
		
		waitTimeBetweenScroll = newWaitFullTime;
		waitTimeBetweenCheck = newWaitBeforePoll;
	}
	
	/**
	 * Increment the counter for the number of scroll events received.<p>
	 * This is intended to be called by the MouseListener.
	 * 
	 * Note : A new scroll event receive will reset the wait timer.
	 */
	public void incrementMouseScroll() {
		// Reset the wait timer
		lastScrollTime = System.currentTimeMillis();
		nbScrollClick++;
	}
	
	/**
	 * Decrement the counter for the number of scroll events received.<p>
	 * This is intended to be called by the MouseListener.
	 * 
	 * Note : A new scroll event receive will reset the wait timer.
	 */
	public void decrementMouseScroll() {
		// Reset the wait timer
		lastScrollTime = System.currentTimeMillis();
		nbScrollClick--;
	}
	
	/**
	 * Threaded execution method.<p>
	 * This is the real "wait" method that will wait for mouse scroll events.<p>
	 * 
	 * The function will wake every "waitTimeBetweenCheck" to check if we exhausted the timer.<p>
	 * So, the "longest" we could wait after the last event is "waitTimeBetweenScroll" + "waitTimeBetweenCheck"
	 * 
	 */
	@Override
	public void run() {
		// Check if we waited more than "waitTimeBetweenScroll"
		while ( (System.currentTimeMillis() - lastScrollTime) < waitTimeBetweenScroll ) {
			try {
				Thread.sleep(waitTimeBetweenCheck);
			}
			catch (Exception e) { }
		}
		
		// Tell the mouse listener the number of click received
		mouseListener.receiveMouseScrollCount(nbScrollClick);
	}
}
