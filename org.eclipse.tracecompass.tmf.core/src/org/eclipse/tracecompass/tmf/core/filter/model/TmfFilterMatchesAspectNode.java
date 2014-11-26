/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.filter.model;

import java.util.regex.Pattern;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * Filter node that matches on an event aspect.
 *
 * @author Alexandre Montplaisir
 */
public final class TmfFilterMatchesAspectNode extends TmfFilterMatchesNode {

    /** Name/ID of this node */
    public static final String NODE_NAME = "MATCHES_ASPECT"; //$NON-NLS-1$

    private ITmfEventAspect fEventAspect;

    /**
     * Constructor
     *
     * @param parent
     *            The parent node
     */
    public TmfFilterMatchesAspectNode(ITmfFilterTreeNode parent) {
        super(parent);
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public boolean matches(ITmfEvent event) {
        Pattern pattern = getPattern();
        boolean isNot = isNot();

        if (pattern == null || event == null) {
            return false ^ isNot;
        }
        String value = fEventAspect.resolve(event).toString();
        return pattern.matcher(value).matches() ^ isNot;
    }

    /**
     * @return The event aspect of this filter
     */
    public ITmfEventAspect getEventAspect() {
        return fEventAspect;
    }

    /**
     * @param aspect
     *            The event aspect to assign to this filter
     */
    public void setEventAspect(ITmfEventAspect aspect) {
        this.fEventAspect = aspect;
    }

    @Override
    public ITmfFilterTreeNode clone() {
        TmfFilterMatchesAspectNode clone = (TmfFilterMatchesAspectNode) super.clone();
        clone.fEventAspect = fEventAspect;
        return clone;
    }

    @Override
    public String toString() {
        return fEventAspect.getName() + (isNot() ? " not" : "") + " matches \"" + getRegex() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fEventAspect == null) ? 0 : fEventAspect.hashCode());
        return result;
    }

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
        TmfFilterMatchesAspectNode other = (TmfFilterMatchesAspectNode) obj;
        if (fEventAspect == null) {
            if (other.fEventAspect != null) {
                return false;
            }
        } else if (!fEventAspect.equals(other.fEventAspect)) {
            return false;
        }
        return true;
    }


}
