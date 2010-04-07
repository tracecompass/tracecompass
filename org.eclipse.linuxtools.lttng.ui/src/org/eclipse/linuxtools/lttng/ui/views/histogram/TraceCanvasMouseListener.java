package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;

public class TraceCanvasMouseListener implements MouseMoveListener, MouseListener, MouseWheelListener 
{
	private static long MS_WAIT_TIME_BETWEEN_MOUSE_SCROLL = 500;
	
	private DelayedMouseScroll mouseScrollListener = null;
	private TraceCanvas parentCanvas = null;
	
	private boolean isWindowMoving = false;
	
	public TraceCanvasMouseListener(TraceCanvas newCanvas) {
		parentCanvas = newCanvas;
	}
	
	public void mouseMove(MouseEvent e) {
		if ( isWindowMoving == true ) {
			parentCanvas.positionWindow(e.x);
		}
	}
	
	public void mouseDown(MouseEvent e) {
		isWindowMoving = true;
		parentCanvas.positionWindow(e.x);
	}
	
	public void mouseUp(MouseEvent e) {
		isWindowMoving = false;
		parentCanvas.notifyTimeWindowChanged();
	}
	
	public void mouseDoubleClick(MouseEvent e) {
		System.out.println("mouseDoubleClick");
	}
	
	public void mouseScrolled(MouseEvent e) {
		
		if ( mouseScrollListener == null ) {
			mouseScrollListener = new DelayedMouseScroll(this, MS_WAIT_TIME_BETWEEN_MOUSE_SCROLL);
			mouseScrollListener.start();
		}
			
		if ( e.count > 0) {
			mouseScrollListener.incrementMouseScroll();
		}
		else {
			mouseScrollListener.decrementMouseScroll();
		}
	}
	
	public void receiveMouseScrollCount(int nbMouseScroll) {
		mouseScrollListener = null;
		parentCanvas.resizeWindowByFactor(nbMouseScroll);
		parentCanvas.redrawAsynchronously();
	}
	
}

class DelayedMouseScroll extends Thread {
	
	private static long MS_WAIT_TIME_BETWEEN_CLICK_CHECK = 100;
	
	private TraceCanvasMouseListener mouseListener = null;
	
	private long waitTimeBetweenScroll = 0;
	
	private long lastScrollTime = 0L;
	private int nbScrollClick = 0;
	
	public DelayedMouseScroll(TraceCanvasMouseListener newListener, long newWaitTime) {
		
		lastScrollTime = System.currentTimeMillis();
		waitTimeBetweenScroll = newWaitTime;
		
		mouseListener = newListener;
	}
	
	public void incrementMouseScroll() {
		lastScrollTime = System.currentTimeMillis();
		nbScrollClick++;
	}
	
	public void decrementMouseScroll() {
		lastScrollTime = System.currentTimeMillis();
		nbScrollClick--;
	}
	
	public void run() {
		while ( (System.currentTimeMillis() - lastScrollTime) < waitTimeBetweenScroll ) {
			try {
				Thread.sleep(MS_WAIT_TIME_BETWEEN_CLICK_CHECK);
			}
			catch (Exception e) { }
		}
		
		mouseListener.receiveMouseScrollCount(nbScrollClick);
	}
}
