package org.eclipse.linuxtools.internal.lttng.jni.common;
/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

/**
 * <b><u>JniTime</u></b>
 * <p>
 * Used to store (event, trace, tracefile, ...) timestamp.
 * 
 * Mimic the behavior of the LttTime C structure.
 */
public class JniTime extends Jni_C_Constant implements Comparable<JniTime>
{
    private long time = 0;

    /**
     * Default constructor.<p>
     * 
     * Note : Time will be set to 0.
     * 
     */
    public JniTime() {
        time = 0;
    }

    /**
     * Copy constructor.
     * 
     * @param oldTime   Reference to the JniTime you want to copy.           
     */
    public JniTime(JniTime oldTime) {
        time = oldTime.getTime();
    }

    /**
     * Constructor with parameters.<p>
     * 
     * "LTT style" constructor with Seconds et Nanoseconds
     * 
     * @param newSec      Seconds of the JniTime
     * @param newNanoSec  Nanoseconds of the JniTime
     */
    public JniTime(long newSec, long newNanoSec) {
        time = (newSec * NANO) + newNanoSec;
    }

    /**
     * Constructor with parameters.<p>
     * 
     * Usual "nanosecond only" constructor.
     * 
     * @param newNanoSecTime  Time in nanoseconds
     */
    public JniTime(long newNanoSecTime) {
        time = newNanoSecTime;
    }

    /**
     * Second of the time.<p>
     * 
     * Returns seconds, i.e. multiple of 1 000 000, of the stored nanoseconds time.
     * 
     * @return Second of this time.
     */
    public long getSeconds() {
        return (time / NANO);
    }

    /**
     * Getter for the nanosecond of the time.<p>
     * 
     * Returns nanoseconds part, i.e. modulo of 1 000 000, of the stored nanoseconds time.
     * 
     * @return Nanoseconds of this time
     */
    public long getNanoSeconds() {
        return time % NANO;
    }

    /**
     * Full time, in nanoseconds.<p>
     * 
     * @return Complete time in nanoseconds
     */
    public long getTime() {
        return time;
    }
    
    /**
     * Changes the current time for this object<p>
     * 
     * @param newTime	New time to set, in nanoseconds.
     */
    public void setTime(long newTime) {
        time = newTime;
    }
    
    /* 
     * Populate this time object
     * 
     * Note: This function is called from C side.
     * 
     * @param newTime The time we want to populate
     */
    @SuppressWarnings("unused")
    private void setTimeFromC(long newTime) {
        time = newTime;
    }
    
    /**
     * Comparaison operator smaller or equal than "<=" .<p>
     * 
     * @param comparedTime  The time we want to compare with this one
     * 
     * @return true if compared time is smaller or equal, false otherwise
     */
    public boolean isSmallerOrEqual(JniTime comparedTime) {

        // NOTE : We check <= instead of just <
        // This mean the LEFT OPERAND (comparedTime) always prevails
        if (this.getTime() <= comparedTime.getTime() ) {
            return true;
        } 
        else {
            return false;
        }
    }
    
    /**
     * compareTo operator.<p>
     * 
     * @param right  The time we want to compare with this one
     * 
     * @return int of value -1, 0 or 1, as the pased argument is bigger, equal or smaller than this time
     */
    @Override
	public int compareTo(JniTime right) {
        long leftTime = this.getTime();
        long rightTime = right.getTime();
        
        if ( leftTime < rightTime ) { 
            return -1;
        }
        else if ( leftTime > rightTime ) {
            return  1;
        }
        else {
            return 0;
        }
    }
    
    /**
     * faster equals since it is called very often
     * @param other the object to the right of this
     * @return true if the times are the same, false otherwise. 
     */
	public boolean equals(JniTime other) {
		return ((other != null) && (this.time == other.time));
	}
   
    /**
     * Overridden equals for JniTime type
     * 
     * @param The object we want to compare too
     * 
     * @return true if the time is the same, false otherwise.
     */
    @Override
	public boolean equals(Object obj) {
		if (obj instanceof JniTime) {
			return (((JniTime) obj).time == this.time);
		}
		return false;
	}
    
    /**
     * Overridden hash code for JniTime type 
     * 
     */
    @Override
    public int hashCode() {
    	return this.toString().hashCode();
    }
    
    
    /**
     * toString() method.
     * <u>Intended to debug</u><p>
     * 
     * NOTE : We output the time in the same format as LTT (seconds and nanosecond separatly)
     * 
     * @return String Attributes of the object concatenated in String
     */
    @Override
    @SuppressWarnings("nls")
	public String toString() {
        String returnData = "";

        returnData += "seconds     : " + this.getSeconds() + "\n";
        returnData += "nanoSeconds : " + this.getNanoSeconds() + "\n";

        return returnData;
    }
}
