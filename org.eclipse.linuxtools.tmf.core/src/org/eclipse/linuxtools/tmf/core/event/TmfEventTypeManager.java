/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import java.util.HashMap;

/**
 * <b><u>TmfEventTypeManager</u></b>
 * <p>
 * The TmfEventTypeManager acts as a central repository for the available
 * event types.
 * <p>
 */
public final class TmfEventTypeManager {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The event type manager singleton
    private static TmfEventTypeManager fEventTypeManager = null;

    // The available types, per context
    private HashMap<String, HashMap<String, ITmfEventType>> fEventTypes; 

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    private TmfEventTypeManager() {
        fEventTypes = new HashMap<String, HashMap<String, ITmfEventType>>();
    }

    public static synchronized TmfEventTypeManager getInstance() {
        if (fEventTypeManager == null) {
            fEventTypeManager = new TmfEventTypeManager();
        }
        return fEventTypeManager;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Add a context:type pair to the available types
     * 
     * @param context the target context
     * @param type the type to add
     */
    public synchronized void add(String context, ITmfEventType type) {
        HashMap<String, ITmfEventType> types = fEventTypes.get(context);
        if (types == null) {
            types = new HashMap<String, ITmfEventType>();
        }
        types.put(type.getId(), type);
        fEventTypes.put(context, types);
    }

    /**
     * Return the list of currently defined contexts
     * 
     * @return the list of contexts
     */
    public synchronized String[] getContexts() {
        return fEventTypes.keySet().toArray(new String[fEventTypes.size()]);
    }

    /**
     * Return the list of types defined for a given context
     * 
     * @param context the context to look into
     * @return the list of types defined for that context
     */
    public synchronized ITmfEventType[] getTypes(String context) {
        HashMap<String, ITmfEventType> types = fEventTypes.get(context);
        if (types != null) {
            return types.values().toArray(new ITmfEventType[types.size()]);
        }
        return null;
    }

    /**
     * Return an event type
     * 
     * @param context the context to look into
     * @param typeId the type ID
     * @return the corresponding type
     */
    public synchronized ITmfEventType getType(String context, String typeId) {
        HashMap<String, ITmfEventType> types = fEventTypes.get(context);
        if (types != null) {
            return types.get(typeId);
        }
        return null;
    }

    /**
     * Remove the types associated to a context
     * 
     * @param context the context to remove
     */
    public synchronized void clear(String context) {
        fEventTypes.remove(context);
    }

    /**
     * Remove all contexts and types
     */
    public synchronized void clear() {
        fEventTypes.clear();
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventTypeManager [fEventTypes=" + fEventTypes + "]";
    }

}
