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
 * <b><u>HistogramContent</u></b>
 * <p>
 * This class hold the content that will be used to draw the Histograms.
 * <p>
 */
public class HistogramContent {
	
	private Long 	startTime = 0L;
	private Long 	endTime   = 0L;
	
	private Long	intervalTime = 0L;
	private Double 	heightFactor = 0.0;
	private Long   	heighestEventCount = 0L;
	private Integer maxHeight	 = 0;
	
	// This value is used to calculate at which point we should "cut" bar that are too tall.
	// Default value is large enought so that no bar should be cut
	private Double  maxDifferenceToAverage = HistogramConstant.DEFAULT_DIFFERENCE_TO_AVERAGE;
	
	private Integer	readyUpToPosition = 0;
	private Integer	canvasFullSize = 0;
	
	private Integer	averageNumberOfEvents = 0;
	
	private Long selectedEventTimeInWindow = 0L;
	
	private HistogramElement[] elementTable;
	
	/**
	 * Default constructor for the HistogramContent.
	 * 
	 * @param tableSize			The size ofthe element table that will be created.
	 * @param newCanvasSize		The full size of the canvas. Used for positionning; need to be consistent with canvas.
	 * @param newMaxHeight		The maximum height of a bar, usually same as the height of the canvas.
	 */
	public HistogramContent(int tableSize, int newCanvasSize, int newMaxHeight) {
		this(tableSize, newCanvasSize, newMaxHeight, HistogramConstant.DEFAULT_DIFFERENCE_TO_AVERAGE);
	}
	
	/**
	 * Default constructor for the HistogramContent.
	 * 
	 * @param tableSize			The size ofthe element table that will be created.
	 * @param newCanvasSize		The full size of the canvas. Used for positionning; need to be consistent with canvas.
	 * @param newMaxHeight		The maximum height of a bar, usually same as the height of the canvas.
	 * @param newDiffToAverage  This value at which point we "cut" bar that are too tall.
	 */
	public HistogramContent(int tableSize, int newCanvasSize, int newMaxHeight, double newDiffToAverage) {
		canvasFullSize = newCanvasSize;
		maxHeight = newMaxHeight;
		maxDifferenceToAverage = newDiffToAverage;
		
		// Create a new element table from the above value
		// The table will not get initialized until resetTable() is called. 
		createNewTable(tableSize);
	}
	
	/**
	 * Create a new table to hold the content element.<p>
	 * Note that the table is not initialized (and so unusable) until resetTable() is called.
	 * 
	 * @param newTableSize	The size (number of element) of the table.
	 */
	public void createNewTable(int newTableSize) {
		elementTable = new HistogramElement[newTableSize];
		
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x] = new HistogramElement();
			elementTable[x].position = x;
		}
	}
	
	/**
	 * Reset all HistogramContent attributes, but keep the elements table untouched.<p>
	 */
	public void clearContentData() {
		startTime = 0L;
		endTime = 0L;
		
		intervalTime = 0L;
		heightFactor = 0.0;
		heighestEventCount = 0L;
		
		readyUpToPosition = 0;
	}
	
	/**
	 * Reset the data in the elements table.<p>
	 * NOTE : For this to be consistent and usuable, "startTime", "endTime" and "intervalTime" need to be set already.
	 */
	public void resetTable() {
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x].position = x;
			elementTable[x].firstIntervalTimestamp = startTime + (x*intervalTime);
			elementTable[x].intervalNbEvents = 0L;
			elementTable[x].intervalHeight = 0;
		}
	}
	
	/**
	 * Reset the data in the elements table.<p>
	 * NOTE : For this to be consistent and usuable, "startTime" and "intervalTime" need to be set.
	 */
	public void resetTable(Long newStartTime, Long newEndTime, Long newIntervalTime) {
		
		startTime = newStartTime;
		endTime = newEndTime;
		intervalTime = newIntervalTime;
		
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x].position = x;
			elementTable[x].firstIntervalTimestamp = startTime + (x*intervalTime);
			elementTable[x].intervalNbEvents = 0L;
			elementTable[x].intervalHeight = 0;
		}
	}
	
	
	/**
	 * Clear (zeroed) the data in the elements table.<p>
	 * NOTE : Unlike reset, this does not recalculate the content, 
	 *			so it should be done either by hand or by calling reset table after.
	 */
	public void clearTable() {
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x].position = x;
			elementTable[x].firstIntervalTimestamp = 0L;
			elementTable[x].intervalNbEvents = 0L;
			elementTable[x].intervalHeight = 0;
		}
	}
	
	/**
	 * Print all HistogramContent attributes, but the elements table.
	 */
	public void printContentInfo() {
		System.out.println("startTime          : " + startTime);
		System.out.println("endTime            : " + endTime );
		System.out.println();
		System.out.println("intervalTime       : " + intervalTime);
		System.out.println("heightFactor       : " + heightFactor);
		System.out.println("heighestEventCount : " + heighestEventCount);
		System.out.println();
		System.out.println("readyUpToPosition  : " + readyUpToPosition);
	}
	
	/**
	 * Print the data in the elements table.<p>
	 */
	public void printTable() {
		for ( int x=0; x<elementTable.length; x++) {
			System.out.println("X:" + x + " -> " + elementTable[x].intervalNbEvents + ":" + elementTable[x].intervalHeight + " (" + elementTable[x].firstIntervalTimestamp + ")");
		}
	}
	
	/**
	 * Getter for the timestamp of the selected event in the window.<p>
	 * 
	 * @return	The time of the event.
	 */
	public Long getSelectedEventTimeInWindow() {
		return selectedEventTimeInWindow;
	}
	
	/**
	 * Setter for the timestamp of the selected event in the window.<p>
	 * 
	 * This allow to pinpoint a certain event or position in the window.
	 * Set to 0 or lower to ignore.
	 * 
	 * @param newPosition The new event time.
	 */
	public void setSelectedEventTimeInWindow(Long newTime) {
		this.selectedEventTimeInWindow = newTime;
	}
	
	/**
	 * Get an element in the table by its index.<p>
	 * Null is returned if the index is out of range.<p>
	 * Note that you can get an element past "readyUpToPosition", the index is NOT tested against it. 
	 * 
	 * @param index	The index of the element (0 < index < nbElement)
	 * 
	 * @return The element found or null if the index is wrong.
	 */
	public HistogramElement getElementByIndex(int index) {
		HistogramElement returnedElement = null;
		
		if ( (index >= 0) && (index < elementTable.length) ) {
			returnedElement = elementTable[index];
		}
		
		return returnedElement;
	}
	
	/**
	 * Return the closest element to a X position on the canvas.<p>
	 * Note : canvasFullSize need to be set correctly here, otherwise unexpected element might be returned.<p>
	 * <p>
	 * NOTE : This <b>ALWAYS</b> return an element; 
	 * 		If calculation lead outside the table, the first or the last element will be returned.  
	 * 
	 * @param position  The X position we are looking at (0 < pos < canvasWidth) 
	 * 
	 * @return	The <i>closest</i> element found.
	 */
	public HistogramElement getClosestElementFromXPosition(int position) {
		
		int index = (int)(  ((double)elementTable.length)*((double)position/(double)canvasFullSize) );
		
		// If we are out of bound, return the closest border (first or last element)
		if ( index < 0) {
			index = 0;
		}
		else if ( index >= elementTable.length ) {
			index = (elementTable.length -1);
		}
		
		return elementTable[index];
	}
	
	/**
	 * Return the closest element's timestamp to a X position on the canvas.<p>
	 * Note : canvasFullSize need to be set correctly here, otherwise unexpected timestamp might be returned.<p>
	 * <p>
	 * NOTE : This <b>ALWAYS</b> return a timestamp; 
	 * 		If calculation lead outside the table, the first or the last timestamp will be returned.  
	 * 
	 * @param position  The X position we are looking at (0 < pos < canvasWidth) 
	 * 
	 * @return	The <i>closest</i> timestamp found.
	 */
	public Long getClosestTimestampFromXPosition(int position) {
		return getClosestElementFromXPosition(position).firstIntervalTimestamp;
	}
	
	/**
	 * Return the X position (relative to the canvas) of a certain element.<p>
	 * Note : canvasFullSize need to be set correctly here, otherwise unexpected element might be returned.<p>
	 * 
	 * NOTE : This <b>ALWAYS</b> return an element; 
	 * 		If calculation lead outside the table, the first or the last element will be returned.
	 * 
	 * @param targetElement		The element we are looking to find the position 
	 * 
	 * @return					The <i>closest</i> found element. 
	 */
	public int getXPositionFromElement(HistogramElement targetElement) {
		return (int)( ((double)targetElement.position / (double)elementTable.length)*(double)canvasFullSize );
	}
	
	/**
	 * Return the closest element to a timestamp (long) given.<p>
	 * Note : startTime and intervalTime need to be set correctly here, otherwise unexpected element might be returned.<p>
	 * <p>
	 * NOTE : This <b>ALWAYS</b> return an element; 
	 * 		If calculation lead outside the table, the first or the last element will be returned.  
	 * 
	 * @param timestamp  The timestamp (in nanosecond, as long) of the element we are looking for (startTime < timestamp < endTime) 
	 * 
	 * @return	The <i>closest</i> element found.
	 */
	public HistogramElement getClosestElementFromTimestamp(long timestamp) {
		int index = (int)( (timestamp - startTime)/intervalTime );
		
		// If we are out of bound, return the closest border (first or last element)
		if ( index < 0) {
			index = 0;
		}
		else if ( index >= elementTable.length ) {
			index = (elementTable.length -1);
		}
		
		return elementTable[index];
	}
	
	/**
	 * Return the closest X position to a timestamp (long) given.<p>
	 * Note : startTime and intervalTime need to be set correctly here, otherwise unexpected position might be returned.<p>
	 * <p>
	 * NOTE : This <b>ALWAYS</b> return a position; 
	 * 		If calculation lead outside the table, the first or the last position will be returned.  
	 * 
	 * @param timestamp  The timestamp (in nanosecond, as long) of the element we are looking for (startTime < timestamp < endTime) 
	 * 
	 * @return	The <i>closest</i> position found.
	 */
	public Integer getClosestXPositionFromTimestamp(long timestamp) {
		return getXPositionFromElement(getClosestElementFromTimestamp(timestamp));
	}
	
	/**
	 * Return the closest element to an element and a time interval to this element.<p>
	 * The time interval can be negative or positive (before or after the element).
	 * 
	 * Note : IntervalTime and StartTime need to be set correctly here, otherwise unexpected result might be returned.<p>
	 * 
	 * @param targetElement			The element we compare the interval with.
	 * @param intervalToElement		Time negative or positive time interval (in nanosecond) to this element. 
	 * 
	 * @return	The <i>closest</i> found element, or null if given data are wrong.
	 */
	public HistogramElement getClosestElementByElementAndTimeInterval(HistogramElement targetElement, Long intervalToElement) {
		
		// Get the timestamp of the target element
		// This should always be valid as long the table is initialized
		Long elementTime = targetElement.firstIntervalTimestamp;
		elementTime = elementTime + intervalToElement;
		
		return getClosestElementFromTimestamp(elementTime);
	}
	
	/**
	 * Return the closest element to an element's timestamp (as long) and a time interval to this element.<p>
	 * The time interval can be negative or positive (before or after the element).
	 * 
	 * Note : IntervalTime and StartTime need to be set correctly here, otherwise unexpected result might be returned.<p>
	 * 
	 * @param timestamp				The timestamp (in nanoseconds, as long) of the element we want to compare from.
	 * @param intervalToElement		Time negative or positive time interval (in nanosecond) to this element. 
	 * 
	 * @return	The <i>closest</i> found element, or null if given data are wrong.
	 */
	public int getClosestElementByTimestampAndTimeInterval(Long timestamp, Long intervalToElement) {
		HistogramElement targetElement = getClosestElementFromTimestamp(timestamp);
		HistogramElement newElement = getClosestElementByElementAndTimeInterval(targetElement, intervalToElement);
		
		return getXPositionFromElement(newElement);
	}
	
	/**
	 * Return the closest element to an element's position and a time interval to this element.<p>
	 * The time interval can be negative or positive (before or after the element).
	 * 
	 * Note : IntervalTime and StartTime need to be set correctly here, otherwise unexpected result might be returned.<p>
	 * 
	 * @param targetPosition		The position (relative to the canvas) of the element we want to compare from.
	 * @param intervalToElement		Time negative or positive time interval (in nanosecond) to this element.
	 * 
	 * @return	The <i>closest</i> found element, or null if given data are wrong.
	 */
	public int getXPositionByPositionAndTimeInterval(int targetPosition, Long intervalToElement) {
		HistogramElement targetElement = getClosestElementFromXPosition(targetPosition);
		HistogramElement newElement = getClosestElementByElementAndTimeInterval(targetElement, intervalToElement);
		
		return  getXPositionFromElement(newElement);
	}
	
	/**
	 * Getter for the number of element.<p>
	 * The same as the value of tableSize given at construction.
	 * 
	 * @return The number of element in the elements table.
	 */
	public int getNbElement() {
		return elementTable.length;
	}
	
	/**
	 * Getter for the average number of events by interval in the content.<p>
	 * 
	 * Note : Might be set externally (instead of calculated internally), so consistency with the content is not guarantee.
	 * 
	 * @return	Average number of events we currently use in
	 */
	public int getAverageNumberOfEvents() {
		return averageNumberOfEvents;
	}
	
	/**
	 * Setter for averageNumberOfEvents.<p>
	 * 
	 * Note : this is used in some drawing calculation so make sure this number make sense.
	 * Note : you might want to call recalculateEventHeight() if you change this.
	 * 
	 * @param newAverageNumberOfEvents	The new average number of events to use.
	 */
	public void setAverageNumberOfEvents(int newAverageNumberOfEvents) {
		this.averageNumberOfEvents = newAverageNumberOfEvents;
	}
	
	/**
	 * Recalculate the average number of events by time interval.<p>
	 * 
	 * Note : This run over all the element so this is quite cpu intensive, use with care. 
	 */
	public void recalculateAverageNumberOfEvents() {
		
		int nbInterval = 0;
		int totalNbEvents = 0;
		
		// Go over the element up to readyUpToPosition (further position might not be ready)
		for ( int x=0; x<readyUpToPosition; x++) {
			// Skip the empty interval if we were asked to do so.
			if ( HistogramConstant.SKIP_EMPTY_INTERVALS_WHEN_CALCULATING_AVERAGE ) {
				if ( elementTable[x].intervalNbEvents > 0 ) {
	    			nbInterval++;
				}
	    	}
	    	else {
	    		nbInterval++;
	    	}
			
			totalNbEvents += elementTable[x].intervalNbEvents;
		}
		// Calculate the average here
		averageNumberOfEvents = totalNbEvents / nbInterval;
	}
	
	/**
	 * Getter for the start time of the content.<p>
	 * 
	 * @return The start time we currently use.
	 */
	public Long getStartTime() {
		return startTime;
	}
	
	/**
	 * Setter for the start time of the content.<p>
	 * Note : You probably want to call "resetTable()" if you change this, otherwise data might be inconsistent.
	 * 
	 * @param newStartTime	the new start time
	 */
	public void setStartTime(Long newStartTime) {
		this.startTime = newStartTime;
	}
	
	
	/**
	 * Getter for the end time of the content.<p>
	 * 
	 * @return The end time we currently use.
	 */
	public Long getEndTime() {
		return endTime;
	}
	
	// *** TODO ***
	// Implement a way to "compress" a table if the endtime change.
	// That way, the end time could change without having to reset the table data.
	/**
	 * Setter for the end time of the content.<p>
	 * Note : You probably want to call "resetTable()" if you change this, otherwise data might be inconsistent.
	 * 
	 * @param newStartTime	the new end time
	 */
	public void setEndTime(Long newEndTime) {
		this.endTime = newEndTime;
	}
	
	/**
	 * Getter for the complete time interval of the content.<p>
	 * Note : This return "endTime" minus "startTime", unlike getReadyTimeInterval() it won't check the actual time of elements.  
	 * 
	 * @return	The complete time interval
	 */
	public Long getCompleteTimeInterval() {
		return ( endTime - startTime );
	}
	
	/**
	 * Getter for the time interval for the element between first and readyUpToPosition<p>
	 * Note : This return element[readyPosition].time - element[first].time , not the full interval like getCompleteTimeInterval()
	 * 
	 * @return	The time interval of the position that are ready.
	 */
	public Long getReadyTimeInterval() {
		return ( elementTable[readyUpToPosition].firstIntervalTimestamp - elementTable[0].firstIntervalTimestamp );
	}
	
	/**
	 * Getter for the height factor of the bar.<p>
	 * Note : height =  "nb events in interval" * heightFactor
	 * 
	 * @return	Height factor currently used. 
	 */
	public Double getHeightFactor() {
		return heightFactor;
	}
	
	/**
	 * Recalculate the height of each bar in the elements table.<p>
	 * Need to be called at least once, or when value of "maxHeight", "heighestEventCount" or "averageNumberOfEvents" changes.
	 */
	public void recalculateEventHeight() {
		
		// Recalculate the new HeightFactor for the element; 
		//		the highest bar will get "maxHeight" and other bar a fraction of it.
		// If a maxDifferenceToAverage exist, this is considered here 
		if ( heighestEventCount > (maxDifferenceToAverage * averageNumberOfEvents) ) {
			heightFactor = (double)maxHeight/( maxDifferenceToAverage * (double)averageNumberOfEvents);
		}
		else {
			heightFactor = (double)maxHeight/(double)heighestEventCount;
		}
		
		// Recalculate the height of the bars up to "readyUpToPosition"
		for ( int x=0; x<readyUpToPosition; x++) {
			elementTable[x].intervalHeight = (int)(elementTable[x].intervalNbEvents * heightFactor);
		}
	}
	
	// *** TODO ***
	// It is not possible for now to resize the canvas without creating a new content.
	// We need to implement this somehow.
	/**
	 * Getter for the full size of the canvas.<p>
	 * This is used for the positionnal calculation so should be consistent with the real canvas size.
	 * 
	 * @return	Size of the canvas we currently use.
	 */
	public Integer getCanvasFullSize() {
		return canvasFullSize;
	}
	
	/**
	 * Getter for the heighest event count recorded so far for an interval.<p>
	 * 
	 * Note : Might be set externally (instead of calculated internally), so consistency with the content is not guarantee.
	 * 
	 * @return Current heighestEventCount
	 */
	public Long getHeighestEventCount() {
		return heighestEventCount;
	}
	
	/**
	 * Setter for setHeighestEventCount.<p>
	 * 
	 * Note : this is used in some drawing calculation so make sure this number make sense.
	 * Note : you might want to call recalculateEventHeight() if you change this.
	 * 
	 * @param newHeighestEventCount	Heighest event count for a single interval.
	 */
	public void setHeighestEventCount(Long newHeighestEventCount) {
		this.heighestEventCount = newHeighestEventCount;
	}
	
	/**
	 * Recalculate the heightest event count for a single time interval.<p>
	 * 
	 * Note : This run over all the element so this is quite cpu intensive, use with care. 
	 */
	public void recalculateHeighestEventCount() {
		// Go over the element up to readyUpToPosition (further position might not be ready)
		for ( int x=0; x<readyUpToPosition; x++) {
			if ( elementTable[x].intervalNbEvents > heighestEventCount ) {
				this.heighestEventCount = elementTable[x].intervalNbEvents;
			}
		}
	}
	
	/**
	 * Getter for the max height of a bar in the content.<p>
	 * 
	 * @return	maximum height for a bar we currently use.
	 */
	public Integer getMaxHeight() {
		return maxHeight;
	}
	
	/**
	 * Setter for maxHeight.<p>
	 * 
	 * Note : this is used in some drawing calculation so make sure this number make sense.
	 * Note : you might want to call recalculateEventHeight() if you change this.
	 * 
	 * @param maxHeight	The new maximum height for a bar to use.
	 */
	public void setMaxHeight(Integer maxHeight) {
		this.maxHeight = maxHeight;
	}
	
	/**
	 * Getter for the max difference to the average height a bar can have.<p>
	 * This determine at which point a bar too tall is "cut". Set a very large value (like 1000.0) to ignore.
	 * 
	 * @return	maximum difference to the average we currently use.
	 */
	public Double getMaxDifferenceToAverage() {
		return maxDifferenceToAverage;
	}
	
	/**
	 * Setter for the max difference to the average height a bar can have.<p>
	 * This determine at which point a bar too tall is "cut". Set a very large value (like 1000.0) to ignore.
	 * 
	 * Note : this is used in some drawing calculation so make sure this number make sense.
	 * Note : you might want to call recalculateEventHeight() if you change this.
	 * 
	 * @param newDiffToAverage	The new maximum difference to the average to use.
	 */
	public void setMaxDifferenceToAverage(Double newDiffToAverage) {
		maxDifferenceToAverage = newDiffToAverage;
	}
	
	
	/**
	 * Getter for the interval time of each interval.<p>
	 * This is usually "(EndTime - StartTime) / NbElement"
	 * 
	 * @return	Currently used interval time.
	 */
	public Long getIntervalTime() {
		return intervalTime;
	}
	
	
	/**
	 * Setter for the interval time of each interval.<p>
	 * 
	 * Note : this is used in some drawing calculation so make sure this number make sense.
	 * Note : you migth want to call resetTable() to to fill the element's table again if you change this.
	 * 
	 * @return New interval time.
	 */
	public void setIntervalTime(Long newInterval) {
		this.intervalTime = newInterval;
	}
	
	
	/**
	 * Getter for readyUpToPosition.<p>
	 * This should tell to which point the content is filled, calculated and ready to use.
	 * 
	 * @return Last position processed so far.
	 */
	public int getReadyUpToPosition() {
		return readyUpToPosition;
	}
	
	/**
	 * Setter for readyUpToPosition.<p>
	 * Set a new point (position) up to where the content is filled, calculated and ready to use. 
	 * 
	 * @param newReadyUpToPosition	The new position to use.
	 */
	public void setReadyUpToPosition(int newReadyUpToPosition) {
		this.readyUpToPosition = newReadyUpToPosition;
	}
	
}
