/*******************************************************************************
 * Copyright (c) 2010, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Filter node for an trace type
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterTraceTypeNode extends TmfFilterTreeNode implements ITmfFilterWithNot {

    /** tracetype node name */
    public static final String NODE_NAME = "TRACETYPE"; //$NON-NLS-1$
    /** type attribute name */
    public static final String TYPE_ATTR = "type"; //$NON-NLS-1$
    /** name attribute name */
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$

    private String fTraceTypeId;
    private Class<? extends ITmfTrace> fTraceClass;
    private String fName;
    private boolean fNot;

    /**
     * @param parent the parent node
     */
    public TmfFilterTraceTypeNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    /**
     * @return the trace type id
     */
    public String getTraceTypeId() {
        return fTraceTypeId;
    }

    /**
     * @param traceTypeId the trace type id
     */
    public void setTraceTypeId(String traceTypeId) {
        this.fTraceTypeId = traceTypeId;
    }

    /**
     * @return the trace class
     */
    public Class<? extends ITmfTrace> getTraceClass() {
        return fTraceClass;
    }

    /**
     * @param traceClass the trace class
     */
    public void setTraceClass(Class<? extends ITmfTrace> traceClass) {
        this.fTraceClass = traceClass;
    }

    /**
     * @return the category and trace type name
     */
    public String getName() {
        return fName;
    }

    /**
     * @param name the category and trace type name
     */
    public void setName(String name) {
        this.fName = name;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        boolean match = false;
        ITmfTrace trace = event.getTrace();
        if (trace.getClass().equals(fTraceClass)) {
            if (fTraceTypeId != null) {
                if (fTraceTypeId.equals(trace.getTraceTypeId())) {
                    match = true;
                }
            } else {
                match = true;
            }
        }
        if (match ^ isNot()) {
            // There should be at most one child
            for (ITmfFilterTreeNode node : getChildren()) {
                if (!node.matches(event)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> getValidChildren() {
        if (getChildrenCount() == 0) {
            return super.getValidChildren();
        }
        return new ArrayList<>(0); // only one child allowed
    }

    @Override
    public String toString(boolean explicit) {
        StringBuilder buf = new StringBuilder();
        buf.append("TraceType is "); //$NON-NLS-1$
        if (isNot()) {
            buf.append("not "); //$NON-NLS-1$
        }
        buf.append(fName);
        if (explicit) {
            buf.append('[');
            buf.append(fTraceTypeId);
            buf.append(']');
        }
        if (getChildrenCount() > 0) {
            buf.append(" and "); //$NON-NLS-1$
        }
        if (getChildrenCount() > 1) {
            buf.append("( "); //$NON-NLS-1$
        }
        for (int i = 0; i < getChildrenCount(); i++) {
            ITmfFilterTreeNode node = getChildren()[i];
            buf.append(node.toString(explicit));
            if (i < getChildrenCount() - 1) {
                buf.append(" and "); //$NON-NLS-1$
            }
        }
        if (getChildrenCount() > 1) {
            buf.append(" )"); //$NON-NLS-1$
        }
        return buf.toString();
    }

    @Override
    public boolean isNot() {
        return fNot;
    }

    @Override
    public void setNot(boolean not) {
        fNot = not;
    }
}
