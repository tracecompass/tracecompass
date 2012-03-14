/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannel;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.config.TraceChannels;

/**
 * <b><u>TraceChannels</u></b>
 * <p>
 *  This models a collection of trace channels.
 * </p>
 */
public class TraceChannels implements Map<String, TraceChannel>, Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // -----------------------------------------------------------------------
    private Map<String, TraceChannel> fChannels = new HashMap<String, TraceChannel>();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return fChannels.size();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return fChannels.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return fChannels.containsKey(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return fChannels.containsValue(value);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public TraceChannel get(Object key) {
        return fChannels.get(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public TraceChannel put(String key, TraceChannel value) {
        return fChannels.put(key, value);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public TraceChannel remove(Object key) {
        return fChannels.remove(key);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends TraceChannel> m) {
        fChannels.putAll(m);
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        fChannels.clear();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        return fChannels.keySet();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#values()
     */
    @Override
    public Collection<TraceChannel> values() {
        return fChannels.values();
    }

    /*
     * (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, TraceChannel>> entrySet() {
        return fChannels.entrySet();
    }
    
    /**
     * Creates trace channels with given names, put the to the map with a default
     * trace channel object.
     * 
     * @param channelNames
     */
    public void putAll(String[] channelNames) {
        for (int i = 0; i < channelNames.length; i++) {
            fChannels.put(channelNames[i], new TraceChannel(channelNames[i]));
        }
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TraceChannels clone() {
        TraceChannels clone = null;
        try {
            clone = (TraceChannels)super.clone();

            clone.fChannels = new HashMap<String, TraceChannel>();
            
            for (Iterator<String> iterator = fChannels.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                clone.fChannels.put(key, (fChannels.get(key)).clone());
            }

        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }
}
