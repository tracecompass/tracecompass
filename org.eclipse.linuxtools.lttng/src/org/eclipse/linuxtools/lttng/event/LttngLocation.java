package org.eclipse.linuxtools.lttng.event;


public class LttngLocation implements Cloneable {
	
	private final static Long DEFAULT_LAST_TIME = -1L;
	private final static Long DEFAULT_CURR_TIME =  0L;
	
	private Long lastReadTime = null;
	private Long currentTime = null;
	
	
	
	public LttngLocation() {
		this(DEFAULT_LAST_TIME, DEFAULT_CURR_TIME);
	}
	
	public LttngLocation(LttngLocation oldLocation) {
		this(oldLocation.lastReadTime, oldLocation.currentTime);
	}
	
	public LttngLocation(Long newCurrentTimestamp) {
		this(DEFAULT_LAST_TIME, newCurrentTimestamp);
	}
	
	public LttngLocation(Long newLastReadTime, Long newCurrentTimestamp) {
		lastReadTime = newLastReadTime;
		currentTime = newCurrentTimestamp;
	}
	
	@Override
	public LttngLocation clone() {
		
		LttngLocation newLocation = null;
		
		try {
			newLocation = (LttngLocation)super.clone();
			
			// *** IMPORTANT ***
			// Basic type in java are immutable!
			// Thus, using assignation ("=") on basic type is VALID.
			newLocation.currentTime  = this.currentTime;
			newLocation.lastReadTime = this.lastReadTime;
		} 
		catch (CloneNotSupportedException e) {
			System.out.println("Cloning failed with : " + e.getMessage());
		}

		return newLocation;
	}
	
	
	public void resetLocation() {
		resetLocation(DEFAULT_CURR_TIME);
	}
	
	public void resetLocation(Long newCurrentTimestamp) {
		lastReadTime = DEFAULT_LAST_TIME;
		lastReadTime = DEFAULT_CURR_TIME;
	}
	
	
	
	public Long getLastReadTime() {
		return lastReadTime;
	}
	
	public void setLastReadTime(Long newLastReadTime) {
		this.lastReadTime = newLastReadTime;
	}
	
	public Long getCurrentTime() {
		return currentTime;
	}
	
	public void setCurrentTime(Long newCurrentTime) {
		this.currentTime = newCurrentTime;
	}
	
	
	public String toString() {
		return "\tLttngLocation[ Last : " + lastReadTime + "  Current : " + currentTime + " ]";
	}
	
}
