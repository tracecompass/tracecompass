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
 * 2010-07-16 Yuriy Vashchuk - Heritage correction and selection window
 * 							   optimisations.
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

/**
 * <b><u>HistogramSelectedWindow</u></b>
 * <p>
 * Selection window represent the selected section of the trace in the HistogramCanvas.
 * <p>
 * The selected window have 3 important attributes : 
 * <ul>
 * <li>Its central position
 * <li>Its time width
 * <li>Its visibility (to determine if we should draw it or not)
 * </ul>
 * The dimension are then deduced from the first 2 values.
 * This mean the window is always a perfectly symetrical rectangle.
 */
public class HistogramSelectedWindow {
	
	private long timestampOfLeftPosition = 0;
	private long timestampOfCenterPosition = 0;
	private long timestampOfRightPosition = 0;
	private long windowTimeWidth = 0L;
	private int windowXPositionLeft = 0; 
	private int windowXPositionCenter = 0; 
	private int windowXPositionRight = 0; 
	private Boolean isSelectedWindowVisible = false;
	
	/**
	 * Default constructor for HistogramSelectedWindow.<p>
	 * Position and TimeWidth are set to given value.
	 * 
	 * @param newTraceContent	HistogramContent to read window's data from
	 * @param centralPosition	Central X Position of the selection window in the canvas (0 to canvasWidth)
	 * @param newWindowWidth	Time width (size) of the window. (0 or greater)
	 */
	public HistogramSelectedWindow(HistogramContent newTraceContent, long timestampOfLeftPosition, long newWindowWidth) {
		if(newTraceContent != null) {
			setWindowTimeWidth(newWindowWidth);
			setTimestampOfLeftPosition(timestampOfLeftPosition);
			setTimestampOfRightPosition(timestampOfLeftPosition + newWindowWidth);
			setTimestampOfCenterPosition(timestampOfLeftPosition + newWindowWidth / 2);
		}
	}
	
	/**
	 * Getter for the window visibility.<p>
	 * 
	 * @return true if the window is visible (will be draw), false otherwise
	 */
	public boolean getSelectedWindowVisible() {
		return isSelectedWindowVisible;
	}
	
	/**
	 * Setter for the window visibility.<p>
	 * True means the window will be draw, false that it will be hidden.
	 * 
	 * @param newIsSelectedWindowVisible	The visibility value
	 */
	public void setSelectedWindowVisible(Boolean newIsSelectedWindowVisible) {
		this.isSelectedWindowVisible = newIsSelectedWindowVisible;
	}
	
	
	/**
	 * Getter for the window time width (size)
	 * 
	 * @return Window time width (size)
	 */
	public long getWindowTimeWidth() {
		return windowTimeWidth;
	}
	
	/**
	 * Setter for the window time width (size).<p>
	 * Width need to be a time (in nanoseconds) that's coherent to the data we are looking at.
	 * 
	 * @param newWindowTimeWidth	The new time width
	 */
	public void setWindowTimeWidth(long newWindowTimeWidth) {
		this.windowTimeWidth = newWindowTimeWidth;
	}
	
	/**
	 * Getter for the timestamp of left border of the window.<p>
	 * Compute the timestamp from the HistogramContent data; may return 0 if the content data are wrong.  
	 * 
	 * @return  The left timestamp of the window, or 0 if it cannot compute it. 
	 */
	public long getTimestampOfLeftPosition() {
		return timestampOfLeftPosition;
	}
	
	/**
	 * Setter for the timestamp of left border of the window.<p>
	 * @param  timestampOfLeftPosition The left timestamp of the window. 
	 */
	public void setTimestampOfLeftPosition(long timestampOfLeftPosition) {
		this.timestampOfLeftPosition = timestampOfLeftPosition;
	}		
	
	/**
	 * Getter for the timestamp of the center of the window.<p>
	 * Compute the timestamp from the HistogramContent data; may return 0 if the content data are wrong.  
	 * 
	 * @return  The center timestamp of the window, or 0 if it cannot compute it. 
	 */
	public long getTimestampOfCenterPosition() {
		return timestampOfCenterPosition;
	}

	/**
	 * Setter for the timestamp of center border of the window.<p>
	 */
	public void setTimestampOfCenterPosition(long timestampOfCenterPosition) {
		this.timestampOfCenterPosition = timestampOfCenterPosition;
	}
	
	/**
	 * Setter for the timestamp of center border of the window.<p>
	 */
	public void setTimestampOfLeftCenterRightPositions(long timestampOfCenterPosition) {
		this.timestampOfLeftPosition = timestampOfCenterPosition - windowTimeWidth / 2;
		this.timestampOfCenterPosition = timestampOfCenterPosition;
		this.timestampOfRightPosition = timestampOfCenterPosition + windowTimeWidth / 2;
	}		
	
	/**
	 * Getter for the timestamp of right border of the window.<p>
	 * Compute the timestamp from the HistogramContent data; may return 0 if the content data are wrong.  
	 * 
	 * @return  The right timestamp of the window, or 0 if it cannot compute it. 
	 */
	public long getTimestampOfRightPosition() {
		return timestampOfRightPosition;
	}
	
	/**
	 * Setter for the timestamp of right border of the window.<p>
	 * @param  timestampOfRightPosition The right timestamp of the window. 
	 */
	public void setTimestampOfRightPosition(long timestampOfRightPosition) {
		this.timestampOfRightPosition = timestampOfRightPosition;
	}

	/**
	 * Getter for the coordinate of left border of the window.<p>
	 * 
	 * @return  The left coordinate. 
	 */
	public int getWindowXPositionLeft() {
		return windowXPositionLeft;
	}

	/**
	 * Setter for the coordinate of left border of the window.<p>
	 * @param  windowXPositionLeft The left coordinate of the window. 
	 */
	public void setWindowXPositionLeft(int windowXPositionLeft) {
		this.windowXPositionLeft = windowXPositionLeft;
	}

	/**
	 * Getter for the coordinate of center border of the window.<p>
	 * 
	 * @return  The center coordinate. 
	 */
	public int getWindowXPositionCenter() {
		return windowXPositionCenter;
	}

	/**
	 * Setter for the coordinate of center of the window.<p>
	 * @param  windowXPositionCenter The center coordinate of the window. 
	 */
	public void setWindowXPositionCenter(int windowXPositionCenter) {
		this.windowXPositionCenter = windowXPositionCenter;
	}

	/**
	 * Getter for the coordinate of right border of the window.<p>
	 * 
	 * @return  The right coordinate. 
	 */
	public int getWindowXPositionRight() {
		return windowXPositionRight;
	}

	/**
	 * Setter for the coordinate of right border of the window.<p>
	 * @param  windowXPositionRight The right coordinate of the window. 
	 */
	public void setWindowXPositionRight(int windowXPositionRight) {
		this.windowXPositionRight = windowXPositionRight;
	}
}
