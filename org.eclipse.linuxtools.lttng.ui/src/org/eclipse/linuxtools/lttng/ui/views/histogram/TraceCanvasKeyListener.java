package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

public class TraceCanvasKeyListener  implements KeyListener 
{
	private static Integer BASIC_DISPLACEMENT_FACTOR = 1;
	private static Integer FAST_DISPLACEMENT_MULTIPLE = 10;
	
	private TraceCanvas parentCanvas = null;
	private boolean isShiftPressed = false;
	
	public TraceCanvasKeyListener(TraceCanvas newCanvas) {
		parentCanvas = newCanvas;
	}
	
	public void keyPressed(KeyEvent e) {
		
		switch (e.keyCode) {
			case SWT.SHIFT:
				isShiftPressed = true;
				break;
			case SWT.ARROW_LEFT:
				moveWindowPosition(BASIC_DISPLACEMENT_FACTOR * -1);
				break;
			case SWT.ARROW_RIGHT:
				moveWindowPosition(BASIC_DISPLACEMENT_FACTOR);
				break;
			default:
				break;
		}
	}
	
	public void keyReleased(KeyEvent e) {
		
		switch (e.keyCode) {
			case SWT.SHIFT:
				isShiftPressed = false;
				break;
			default:
				break;
		}
	}
	
	public void moveWindowPosition(int displacementFactor) {
		
		if ( isShiftPressed == true ) {
			displacementFactor = displacementFactor * FAST_DISPLACEMENT_MULTIPLE;
		}
		
		parentCanvas.slideWindow(displacementFactor);
	}
	
}
