package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.tmf.trace.ITmfLocation;


public class LttngLocation implements ITmfLocation<LttngTimestamp>, Comparable<LttngLocation> {
	
	private final static long DEFAULT_CURR_TIME =  0L;
	
	private boolean isLastOperationParse = false ;
	private boolean isLastOperationReadNext = false;
	private boolean isLastOperationSeek = false;
	
	private LttngTimestamp operationTime = null;
	
	public LttngLocation() {
		this( DEFAULT_CURR_TIME );
	}
	
	public LttngLocation(long newCurrentTimestampValue) {
		isLastOperationParse = false;
		isLastOperationReadNext = false;
		isLastOperationSeek = false;
		operationTime = new LttngTimestamp(newCurrentTimestampValue);
	}
	
	public LttngLocation(LttngTimestamp newCurrentTimestamp) {
		isLastOperationParse = false;
		isLastOperationReadNext = false;
		isLastOperationSeek = false;
		operationTime = new LttngTimestamp(newCurrentTimestamp);
	}
	
	
	public LttngLocation(LttngLocation oldLocation) {
		this.isLastOperationParse = oldLocation.isLastOperationParse;
		this.isLastOperationReadNext = oldLocation.isLastOperationReadNext;
		this.isLastOperationSeek = oldLocation.isLastOperationSeek;
		this.operationTime = oldLocation.operationTime;
	}
	
	@Override
	public LttngLocation clone() {
		
		LttngLocation newLocation = null;
		
		try {
			newLocation = (LttngLocation)super.clone();
			
			// *** IMPORTANT ***
			// Basic type in java are immutable!
			// Thus, using assignation ("=") on basic type is VALID.
			newLocation.isLastOperationParse = this.isLastOperationParse;
			newLocation.isLastOperationReadNext = this.isLastOperationReadNext;
			newLocation.isLastOperationSeek = this.isLastOperationSeek;
			
			// For other type, we need to create a new timestamp
			newLocation.operationTime  = new LttngTimestamp( this.operationTime );
		} 
		catch (CloneNotSupportedException e) {
			System.out.println("Cloning failed with : " + e.getMessage()); //$NON-NLS-1$
		}

		return newLocation;
	}
	
	public LttngTimestamp getOperationTime() {
		return operationTime;
	}
	
	public long getOperationTimeValue() {
		return operationTime.getValue();
	}
	
	public void setOperationTime(LttngTimestamp newOperationTime) {
		this.operationTime.setValue(newOperationTime.getValue());
	}
	
	public void setOperationTime(Long newOperationTimeValue) {
		this.operationTime.setValue(newOperationTimeValue);
	}
	
	
	public void setLastOperationParse() {
		isLastOperationParse = true;
		isLastOperationReadNext  = false;
		isLastOperationSeek  = false;
	}

	public boolean isLastOperationParse() {
		return isLastOperationParse;
	}
	
	
	public void setLastOperationReadNext() {
		isLastOperationParse = false;
		isLastOperationReadNext  = true;
		isLastOperationSeek  = false;
	}

	public boolean isLastOperationReadNext() {
		return isLastOperationReadNext;
	}
	
	
	public void setLastOperationSeek() {
		isLastOperationParse = false;
		isLastOperationReadNext  = false;
		isLastOperationSeek  = true;
	}

	public boolean isLastOperationSeek() {
		return isLastOperationSeek;
	}
	
	public void resetLocationState() {
		isLastOperationParse = false;
		isLastOperationReadNext = false;
		isLastOperationSeek = false;
	}
	
	// ------------------------------------------------------------------------
	// Object
	// ------------------------------------------------------------------------

	@Override
	public boolean equals(Object other) {
    	if (!(other instanceof LttngLocation)) {
    		return false;
    	}
    	LttngLocation o = (LttngLocation) other;
    	return (operationTime.equals(o.operationTime)) && (isLastOperationParse == o.isLastOperationParse) &&
    		   (isLastOperationReadNext == o.isLastOperationReadNext) && (isLastOperationSeek == o.isLastOperationSeek);
	}
	
	@Override
	public String toString() {
//		return "\tLttngLocation[ P/R/S : "  + isLastOperationParse + "/" + isLastOperationReadNext + "/" + isLastOperationSeek + "  Current : " + operationTime + " ]";
		return operationTime.toString();
	}
	
	// ------------------------------------------------------------------------
	// ITmfLocation
	// ------------------------------------------------------------------------

	@Override
	public void setLocation(LttngTimestamp location) {
		operationTime  = (LttngTimestamp)location;
	}

	@Override
	public LttngTimestamp getLocation() {
		return new LttngTimestamp ( operationTime );
	}

	@Override
	public int compareTo(LttngLocation o) {
		return operationTime.compareTo(o.operationTime);
	}
	
}
