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
	
	private Integer windowCenterPosition = 0;
	private Long 	windowTimeWidth = 0L;
	
	private Boolean isSelectedWindowVisible = false;
	
	private HistogramContent histogramContent = null;
	
	/**
	 * Default constructor for HistogramSelectedWindow.<p>
	 * Position and TimeWidth are both set to 0
	 * 
	 * @param newTraceContent	HistogramContent to read window's data from
	 */
	public HistogramSelectedWindow(HistogramContent newTraceContent) {
		histogramContent = newTraceContent;
	}
	
	/**
	 * Default constructor for HistogramSelectedWindow.<p>
	 * Position and TimeWidth are set to given value.
	 * 
	 * @param newTraceContent	HistogramContent to read window's data from
	 * @param centralPosition	Central X Position of the selection window in the canvas (0 to canvasWidth)
	 * @param newWindowWidth	Time width (size) of the window. (0 or greater)
	 */
	public HistogramSelectedWindow(HistogramContent newTraceContent, Integer centralPosition, Long newWindowWidth) {
		histogramContent = newTraceContent;
		windowCenterPosition = centralPosition;
		windowTimeWidth = newWindowWidth;
	}
	
	/** 
	 * Getter for the HistogramContent used by the window.<p>
	 * 
	 * @return HistogramContent tied to this selection window.
	 */
	public HistogramContent getTraceContent() {
		return histogramContent;
	}
	
	/**
	 * Setter for the HistogramContent used by the window.<p>
	 * This need to be a valid, initialized HistogramContent;
	 * 	the data in the content are needed for positionning the window.
	 * 
	 * @param newTraceContent  A new HistogramContent
	 */
	public void setTraceContent(HistogramContent newTraceContent) {
		this.histogramContent = newTraceContent;
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
	public void setWindowTimeWidth(Long newWindowTimeWidth) {
		this.windowTimeWidth = newWindowTimeWidth;
	}
	
	
	/**
	 * Getter for the central position of the window.<p>
	 * 
	 * @return Center X position of this window on the canvas.
	 */
	public int getWindowXPositionCenter() {
		return windowCenterPosition;
	}
	
	/**
	 * Setter for the central position of the window.<p>
	 * The new position need to be valid on the canvas (0 to canvasWidth).
	 * 
	 * @param newPosCenter	The new central position.
	 */
	public void setWindowXPositionCenter(Integer newPosCenter) {
		this.windowCenterPosition = newPosCenter;
	}
	
	/**
	 * Getter for the left border of the window.<p>
	 * Compute the position from the HistogramContent data; may return 0 if the content data are wrong.  
	 * 
	 * @return The left position of the window, or 0 if it cannot compute it. 
	 */
	public Integer getWindowXPositionLeft() {
		return histogramContent.getXPositionByPositionAndTimeInterval(windowCenterPosition, -(windowTimeWidth / 2) );
	}
	
	/**
	 * Getter for the right border of the window.<p>
	 * Compute the position from the HistogramContent data; may return 0 if the content data are wrong.  
	 * 
	 * @return The right position of the window, or 0 if it cannot compute it. 
	 */
	public Integer getWindowXPositionRight() {
		return histogramContent.getXPositionByPositionAndTimeInterval(windowCenterPosition, +(windowTimeWidth / 2) );
	}
	
	/**
	 * Getter for the timestamp of left border of the window.<p>
	 * Compute the timestamp from the HistogramContent data; may return 0 if the content data are wrong.  
	 * 
	 * @return  The left timestamp of the window, or 0 if it cannot compute it. 
	 */
	public Long getTimestampLeft() {
		return histogramContent.getClosestElementFromXPosition( getWindowXPositionLeft() ).firstIntervalTimestamp;
	}
	
	/**
	 * Getter for the timestamp of the center of the window.<p>
	 * Compute the timestamp from the HistogramContent data; may return 0 if the content data are wrong.  
	 * 
	 * @return  The center timestamp of the window, or 0 if it cannot compute it. 
	 */
	public Long getTimestampCenter() {
		return histogramContent.getClosestElementFromXPosition( getWindowXPositionCenter() ).firstIntervalTimestamp;
	}
	
	/**
	 * Getter for the timestamp of right border of the window.<p>
	 * Compute the timestamp from the HistogramContent data; may return 0 if the content data are wrong.  
	 * 
	 * @return  The right timestamp of the window, or 0 if it cannot compute it. 
	 */
	public Long getTimestampRight() {
		return histogramContent.getClosestElementFromXPosition( getWindowXPositionRight() ).firstIntervalTimestamp;
	}
}
