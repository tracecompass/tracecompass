package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

public class HistogramContent {
	
	final static double MAX_DIFFERENCE_TO_AVERAGE = 2.0;
	
	private Long 	startTime = 0L;
	private Long 	endTime   = 0L;
	
	private Long	intervalTime = 0L;
	private Double 	heightFactor = 0.0;
	private Long   	heighestEventCount = 0L;
	private Integer maxHeight	 = 0;

	private Integer	readyUpToPosition = 0;
	private Integer	fullWindowSize = 0;
	
	private Integer	averageNumberOfEvents = 0;
	
	private HistogramElement[] elementTable;
	
	public HistogramContent(int tableSize, int newWindowsSize, int newMaxHeight) {
		fullWindowSize = newWindowsSize;
		maxHeight = newMaxHeight;
		
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
		
		cleanTable();
	}
	
	public void cleanTable() {
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x].position = x;
			elementTable[x].firstIntervalTimestamp = 0L;
			elementTable[x].intervalNbEvents = 0L;
			elementTable[x].intervalHeight = 0;
			elementTable[x].isInSelectedWindow = false;
		}
	}
	
	public void printTable() {
		for ( int x=0; x<elementTable.length; x++) {
			System.out.println("X:" + x + " -> " + elementTable[x].intervalNbEvents + ":" + elementTable[x].intervalHeight);
		}
	}
	
	public void recalculateEventHeight() {
		
		if ( getHeighestEventCount() > (MAX_DIFFERENCE_TO_AVERAGE * averageNumberOfEvents) ) {
			heightFactor = (double)maxHeight/( MAX_DIFFERENCE_TO_AVERAGE * (double)averageNumberOfEvents);
		}
		else {
			heightFactor = (double)maxHeight/(double)getHeighestEventCount();
		}
		
		for ( int x=0; x<elementTable.length; x++) {
			elementTable[x].intervalHeight = (int)(elementTable[x].intervalNbEvents * heightFactor);
		}
	}
	
	/*
	// *** VERIFY ***
	// We don't need to expose the table, do we ?
	public HistogramElement[] getElementTable() {
		return elementTable;
	}
	*/
	
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
		
		/*
		System.out.println("position " + position);
		System.out.println("fullWindowSize " + fullWindowSize);
		System.out.println("index " + index);
		*/
		
		returnedElement = elementTable[index];
		
		return returnedElement;
	}
	
	public HistogramElement getElementFromTimestamp(TmfTimestamp timestamp) {
		
		HistogramElement returnedElement = null;
		
		int index = (int)( (timestamp.getValue() - startTime)/intervalTime );
		
		if ( index < 0) {
			index = 0;
		}
		
		/*
		System.out.println("timestamp " + timestamp);
		System.out.println("intervalTime " + intervalTime);
		System.out.println("index " + index);
		*/
		
		returnedElement = elementTable[index];
		
		return returnedElement;
	}
	
	
	public HistogramElement getClosestElementByTimeInterval(HistogramElement targetElement, Long intervalToElement) {
		
		HistogramElement returnedElement = null;
		
		if ( targetElement != null) {
			Long elementTime = targetElement.position * intervalTime;
			
			elementTime = elementTime + intervalToElement;
			
			int newPos = (int)(elementTime / intervalTime );
			
			if ( newPos < 0 ) {
				newPos = 0;
			}
			
			if ( newPos >= elementTable.length ) {
				newPos = elementTable.length - 1;
			}
			
			/*
			System.out.println("targetElement.position " + targetElement.position);
			System.out.println("intervalTime " + intervalTime);
			System.out.println("intervalToElement " + intervalToElement);
			System.out.println("elementTime " + elementTime);
			System.out.println("newPos " + newPos);
			*/
			
			returnedElement = elementTable[newPos];
		}
		
		return returnedElement;
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
