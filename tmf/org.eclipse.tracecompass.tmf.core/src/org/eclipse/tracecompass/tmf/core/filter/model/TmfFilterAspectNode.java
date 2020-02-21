/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfEventFieldAspect;

/**
 * Base class for filter nodes which use event aspects
 *
 * @author Patrick Tasse
 */
public abstract class TmfFilterAspectNode extends TmfFilterTreeNode implements ITmfFilterWithNot {

    /** event aspect attribute name */
    public static final String EVENT_ASPECT_ATTR = "eventaspect"; //$NON-NLS-1$
    /** trace type id attribute name */
    public static final String TRACE_TYPE_ID_ATTR = "tracetypeid"; //$NON-NLS-1$
    /** field attribute name */
    public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
    /** special case trace type id for base aspects */
    public static final String BASE_ASPECT_ID = "BASE.ASPECT.ID"; //$NON-NLS-1$

    /** event aspect */
    protected ITmfEventAspect<?> fEventAspect;

    private String fTraceTypeId;
    private boolean fNot;

    /**
     * @param parent the parent node
     */
    public TmfFilterAspectNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    /**
     * @return The event aspect of this filter
     */
    public ITmfEventAspect<?> getEventAspect() {
        return fEventAspect;
    }

    /**
     * @param aspect
     *            The event aspect to assign to this filter
     */
    public void setEventAspect(ITmfEventAspect<?> aspect) {
        fEventAspect = aspect;
    }

    /**
     * @return The trace type id from which the event aspect belongs, or a
     *         special case id
     * @see #BASE_ASPECT_ID
     */
    public String getTraceTypeId() {
        return fTraceTypeId;
    }

    /**
     * @param traceTypeId
     *            The trace type id from which the event aspect belongs, or a
     *            special case id
     * @see #BASE_ASPECT_ID
     */
    public void setTraceTypeId(String traceTypeId) {
        fTraceTypeId = traceTypeId;
    }

    /**
     * @param explicit
     *            true if the string representation should explicitly include
     *            the trace type id that can differentiate it from other aspects
     *            with the same name
     *
     * @return The string representation of the event aspect
     */
    public String getAspectLabel(boolean explicit) {
        if (fEventAspect == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuilder sb = new StringBuilder(fEventAspect.getName());
        if (explicit) {
            sb.append('[');
            sb.append(fTraceTypeId);
            sb.append(']');
        }
        if (fEventAspect instanceof TmfEventFieldAspect) {
            String field = ((TmfEventFieldAspect) fEventAspect).getFieldPath();
            if (field != null && !field.isEmpty()) {
                if (field.charAt(0) != '/') {
                    sb.append('/');
                }
                sb.append(field);
            }
        }
        return sb.toString();
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterAspectNode clone = (TmfFilterAspectNode) super.clone();
        clone.setEventAspect(fEventAspect);
        clone.setTraceTypeId(fTraceTypeId);
        return clone;
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
