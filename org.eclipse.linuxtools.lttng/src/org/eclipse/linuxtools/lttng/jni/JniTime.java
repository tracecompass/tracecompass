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

package org.eclipse.linuxtools.lttng.jni;

/**
 * <b><u>JniTime</u></b>
 * <p>
 * JniTime object linked to the LttTime C structure
 */
public final class JniTime extends Jni_C_Common
{
    private long time = 0;

    /**
     * Default constructor.
     *          
     */
    JniTime() {
        time = 0;
    }

    /**
     * Copy constructor.
     * 
     * @param oldTime A reference to the JniTime you want to copy.           
     */
    JniTime(JniTime oldTime) {
        time = oldTime.getTime();
    }

    /**
     * Constructor with parameters
     * <br>
     * "Ltt style" constructor with Seconds et Nanoseconds
     * 
     * @param newSec      Seconds of the JniTime
     * @param newNanoSec  Nanoseconds of the JniTime
     */
    JniTime(long newSec, long newNanoSec) {
        time = (newSec * NANO) + newNanoSec;
    }

    /**
     * Constructor with parameters
     * <br>
     * Usual "nanosecond only" constructor
     * 
     * @param newNanoSecTime  JniTime in nanoseconds
     */
    public JniTime(long newNanoSecTime) {
        time = newNanoSecTime;
    }

    /**
     * Getter for the second of the time
     * <br>
     * This only return seconds part, i.e. multiple of 1 000 000, of the stored time
     * 
     * @return long Second of the time
     */
    public long getSeconds() {
        return (time / NANO);
    }

    /**
     * Getter for the nanosecond of the time
     * <br>
     * This only nanosecondspart , i.e. modulo of 1 000 000, of the stored time
     * 
     * @return long Nanoseconds of the time
     */
    public long getNanoSeconds() {
        return time % NANO;
    }

    /**
     * Getter for the full time, in nanoseconds
     * 
     * @return The complete time in nanoseconds
     */
    public long getTime() {
        return time;
    }
     
    /**
     * Comparaison operator <=
     * 
     * @param comparedTime  The time we want to compare with this one
     * @return boolean true if the compared time is smaller or equal, false otherwise
     */
    public boolean isGivenTimeSmallerOrEqual(JniTime comparedTime) {

        // NOTE : We check <= instead of just <
        // This mean the RIGHT OPERAND (comparedTime) always prevails
        if (comparedTime.getTime() < this.getTime()) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Integer Comparaison operator
     * 
     * @param comparedTime  The time we want to compare with this one
     * @return int Integer of value -1, 0 or 1, as the pased argument is bigger, equal or smaller than this time
     */
    public int compare(JniTime comparedTime) {
        if ( comparedTime.getTime() < this.getTime() ) {
                return 1;
        }
        else if ( comparedTime.getTime() > this.getTime() ) {
                return -1;
        }
        else {
            return 0;
        }
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
     * toString() method. <u>Intended to debug</u><br>
     * <br>
     * NOTE : we output the time in the same format as LTT (seconds and nanosecond separatly)
     * 
     * @return String Attributes of the object concatenated in String
     */
    @Override
	public String toString() {
        String returnData = "";

        returnData += "seconds     : " + this.getSeconds() + "\n";
        returnData += "nanoSeconds : " + this.getNanoSeconds() + "\n";

        return returnData;
    }
}
