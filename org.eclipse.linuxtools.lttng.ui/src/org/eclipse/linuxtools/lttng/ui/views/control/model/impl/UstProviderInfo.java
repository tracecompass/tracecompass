/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.lttng.ui.views.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo;

/**
 * <b><u>UstProviderInfo</u></b>
 * <p>
 * Implementation of the Ust Provider interface (IUstProviderInfo) to store UST 
 * provider related data. 
 * </p>
 */
public class UstProviderInfo extends TraceInfo implements IUstProviderInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The process ID of the UST provider.
     */
    private int fPid = 0;
    /**
     * List of event information.
     */
    private List<IBaseEventInfo> fEvents = new ArrayList<IBaseEventInfo>(); 
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of UST provider
     */
    public UstProviderInfo(String name) {
        super(name);
    }
    
    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public UstProviderInfo(UstProviderInfo other) {
        super(other);
        fPid = other.fPid;
        for (Iterator<IBaseEventInfo> iterator = other.fEvents.iterator(); iterator.hasNext();) {
            IBaseEventInfo event = iterator.next();
            if (event instanceof BaseEventInfo) {
                fEvents.add(new BaseEventInfo((BaseEventInfo)event));
            } else {
                fEvents.add(event);
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo#getPid()
     */
    @Override
    public int getPid() {
        return fPid;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo#setPid(int)
     */
    @Override
    public void setPid(int pid) {
        fPid = pid;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo#getEvents()
     */
    @Override
    public IBaseEventInfo[] getEvents() {
        return fEvents.toArray(new IBaseEventInfo[fEvents.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo#setEvents(java.util.List)
     */
    @Override
    public void setEvents(List<IBaseEventInfo> events) {
        for (Iterator<IBaseEventInfo> iterator = events.iterator(); iterator.hasNext();) {
            IBaseEventInfo eventInfo = (IBaseEventInfo) iterator.next();
            fEvents.add(eventInfo);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.IUstProviderInfo#addEvent(org.eclipse.linuxtools.lttng.ui.views.control.model.IBaseEventInfo)
     */
    @Override
    public void addEvent(IBaseEventInfo event) {
        fEvents.add(event);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceInfo#formatString()
     */
    @SuppressWarnings("nls")
    @Override
    public String formatString() {
        StringBuffer output = new StringBuffer();
        //PID: 9379 - Name: /home/user/git/lttng-ust/tests/hello.cxx/.libs/lt-hello
        output.append("\nPID: ");
        output.append(fPid);
        output.append(" - Name: ");
        output.append(getName());
        for (Iterator<IBaseEventInfo> iterator = fEvents.iterator(); iterator.hasNext();) {
            IBaseEventInfo event = (IBaseEventInfo) iterator.next();
            output.append(event.formatString());
        }
        output.append("\n");

        return output.toString();
    }

    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceInfo#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fEvents == null) ? 0 : fEvents.hashCode());
        result = prime * result + fPid;
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceInfo#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UstProviderInfo other = (UstProviderInfo) obj;
        if (fEvents == null) {
            if (other.fEvents != null) {
                return false;
            }
        } else if (!fEvents.equals(other.fEvents)) {
            return false;
        }
        if (fPid != other.fPid) {
            return false;
        }
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceInfo#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[EventInfo(");
            output.append(super.toString());
            output.append(",PID=");
            output.append(fPid);
            output.append(",Events=");
            if (fEvents.isEmpty()) {
                output.append("None");
            } else {
                for (Iterator<IBaseEventInfo> iterator = fEvents.iterator(); iterator.hasNext();) {
                    IBaseEventInfo event = (IBaseEventInfo) iterator.next();
                    output.append(event.toString());
                }
            }
            output.append(")]");
            return output.toString();
    }

 
}
