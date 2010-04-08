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

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

public class HistogramContent {
	
	private Long 	startTime = 0L;
	private Long 	endTime   = 0L;
	
	private Long	intervalTime = 0L;
	private Double 	heightFactor = 0.0;
	private Long   	heighestEventCount = 0L;
	private Integer maxHeight	 = 0;
	private Double  maxDifferenceToAverage = HistogramConstant.DEFAULT_DIFFERENCE_TO_AVERAGE;
	
	private Integer	readyUpToPosition = 0;
	private Integer	fullWindowSize = 0;
	
	private Integer	averageNumberOfEvents = 0;
	
	private HistogramElement[] elementTable;
	
	public HistogramContent(int tableSize, int newWindowsSize, int newMaxHeight) {
		this(tableSize, newWindowsSize, newMaxHeight, HistogramConstant.DEFAULT_DIFFERENCE_TO_AVERAGE);
	}
	
	public HistogramContent(int tableSize, int newWindowsSize, int newMaxHeight, double newDiffToAverage) {
		fullWindowSize = newWindowsSize;
		maxHeight = newMaxHeight;
		maxDifferenceToAverage = newDiffToAverage;
		
		createNewTable(tableSize);
	}
	
	public void createNewTable(int newTableSize) {
		elementTable = new HistogramElement[newTableSize];
		
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x] = new HistogramElement();
			elementTable[x].position = x;
		}
	}
	
	public void resetContentData() {
		startTime = 0L;
		endTime = 0L;
		
		intervalTime = 0L;
		heightFactor = 0.0;
		heighestEventCount = 0L;
		
		readyUpToPosition = 0;
	}
	
	public void resetTable() {
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x].position = x;
			elementTable[x].firstIntervalTimestamp = startTime + (x*intervalTime);
			elementTable[x].intervalNbEvents = 0L;
			elementTable[x].intervalHeight = 0;
		}
	}
	
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
	
	public void printTable() {
		for ( int x=0; x<elementTable.length; x++) {
			System.out.println("X:" + x + " -> " + elementTable[x].intervalNbEvents + ":" + elementTable[x].intervalHeight);
		}
	}
	
	public void recalculateEventHeight() {
		
		if ( getHeighestEventCount() > (maxDifferenceToAverage * averageNumberOfEvents) ) {
			heightFactor = (double)maxHeight/( maxDifferenceToAverage * (double)averageNumberOfEvents);
		}
		else {
			heightFactor = (double)maxHeight/(double)getHeighestEventCount();
		}
		
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x].intervalHeight = (int)(elementTable[x].intervalNbEvents * heightFactor);
		}
	}
	
	public int getNbElement() {
		return elementTable.length;
	}
	
	public HistogramElement getElementByIndex(int index) {
		
		HistogramElement returnedElement = null;
		
		if ( (index >= 0) && (index < elementTable.length) ) {
			returnedElement = elementTable[index];
		}
		
		return returnedElement;
	}
	
	
	public HistogramElement getElementFromXPosition(int position) {
		
		HistogramElement returnedElement = null;
		
		int index = (int)(  ((double)elementTable.length)*((double)position/(double)fullWindowSize) );
		
		if ( index < 0) {
			index = 0;
		}
		else if ( index >= elementTable.length ) {
			index = (elementTable.length -1);
		}
		
		returnedElement = elementTable[index];
		
		return returnedElement;
	}
	
	public HistogramElement getElementFromTimestamp(TmfTimestamp timestamp) {
		
		HistogramElement returnedElement = null;
		
		int index = (int)( (timestamp.getValue() - startTime)/intervalTime );
		
		if ( index < 0) {
			index = 0;
		}
		
		returnedElement = elementTable[index];
		
		return returnedElement;
	}
	
	
	public HistogramElement getClosestElementByElementAndTimeInterval(HistogramElement targetElement, Long intervalToElement) {
		
		HistogramElement returnedElement = null;
		
		if ( (targetElement != null) && (intervalTime > 0) ) {
			Long elementTime = targetElement.position * intervalTime;
			
			elementTime = elementTime + intervalToElement;
			
			int newPos = (int)(elementTime / intervalTime );
			
			if ( newPos < 0 ) {
				newPos = 0;
			}
			
			if ( newPos >= elementTable.length ) {
				newPos = elementTable.length - 1;
			}
			
			returnedElement = elementTable[newPos];
		}
		
		return returnedElement;
	}
	
	public int getXPositionByPositionAndTimeInterval(int targetPosition, Long intervalToElement) {
		int returnedValue = 0;
		
		HistogramElement targetElement = getElementFromXPosition(targetPosition);
		
		if ( targetElement != null ) {
			HistogramElement newElement = getClosestElementByElementAndTimeInterval(targetElement, intervalToElement);
			
			if ( newElement != null ) {
				returnedValue = getXPositionFromElement(newElement);
			}
		}
		
		return returnedValue;
	}
	
	
	public int getXPositionFromElement(HistogramElement targetElement) {
		
		int returnedPosition = (int)( ((double)targetElement.position / (double)elementTable.length)*(double)fullWindowSize );
		
		return returnedPosition;
	}
	
	
	public int getAverageNumberOfEvents() {
		return averageNumberOfEvents;
	}
	
	public void setAverageNumberOfEvents(int newAverageNumberOfEvents) {
		this.averageNumberOfEvents = newAverageNumberOfEvents;
	}
	
	public Long getStartTime() {
		return startTime;
	}
	
	public void setStartTime(Long newStartTime) {
		this.startTime = newStartTime;
	}
	
	
	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long newEndTime) {
		this.endTime = newEndTime;
	}
	
	public Long getCompleteTimeInterval() {
		return ( endTime - startTime );
	}
	
	public Double getHeightFactor() {
		return heightFactor;
	}
	
	public void setHeightFactor(Double newheightFactor) {
		this.heightFactor = newheightFactor;
	}
	
	
	public Long getHeighestEventCount() {
		return heighestEventCount;
	}
	
	public void setHeighestEventCount(Long newheighestEventCount) {
		this.heighestEventCount = newheighestEventCount;
	}
	
	public Integer getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(Integer maxHeight) {
		this.maxHeight = maxHeight;
	}
	
	
	public Double getMaxDifferenceToAverage() {
		return maxDifferenceToAverage;
	}
	
	public void setMaxDifferenceToAverage(Double newDiffToAverage) {
		maxDifferenceToAverage = newDiffToAverage;
	}
	
	
	public int getReadyUpToPosition() {
		return readyUpToPosition;
	}

	public void setReadyUpToPosition(int newReadyUpToPosition) {
		this.readyUpToPosition = newReadyUpToPosition;
	}
	
	
	public Long getIntervalTime() {
		return intervalTime;
	}
	
	public void setIntervalTime(Long newInterval) {
		this.intervalTime = newInterval;
	}
	
}
