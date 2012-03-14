/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.state.resource;

import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.model.LTTngTreeNodeGeneric;
import org.eclipse.linuxtools.internal.lttng.core.state.resource.ILTTngStateResource.GlobalStateMode;

/**
 * @author alvaro
 *
 */
public class LTTngStateResource extends
		LTTngTreeNodeGeneric<LTTngStateResource> {

	// ========================================================================
	// Data
	// =======================================================================
	private GlobalStateMode fstateMode = GlobalStateMode.LTT_STATEMODE_UNKNOWN;
	private final ILttngStateContext fcontext;

	// ======================================================================+
	// Constructors
	// =======================================================================

	public LTTngStateResource(Long id, LTTngStateResource parent, String name,
			ILttngStateContext context, Object value) {
		super(id, parent, name, value);
		fcontext = context;
	}

	public LTTngStateResource(Long id, String name, ILttngStateContext context,
			Object value) {
		super(id, name, value);
		fcontext = context;
	}

	// ========================================================================
	// Methods
	// =======================================================================
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.control.LTTngStateTreeNodeGeneric#getChildren
	 * ()
	 */
	@Override
	public LTTngStateResource[] getChildren() {
		return childrenToArray(fchildren.values(), this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.resource.ILTTngStateResource#getStateMode
	 * ()
	 */
	public GlobalStateMode getStateMode() {
		return fstateMode;
	}

	/**
	 * @param stateMode
	 */
	public void setStateMode(GlobalStateMode stateMode) {
		if (stateMode != null) {
			fstateMode = stateMode;
		} else {
			TraceDebug.debug("Received input is null !"); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.resource.ILTTngStateResource#getContext
	 * ()
	 */
	public ILttngStateContext getContext() {
		return fcontext;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fcontext == null) ? 0 : fcontext.hashCode());
        result = prime * result + ((fstateMode == null) ? 0 : fstateMode.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof LTTngStateResource)) {
            return false;
        }
        LTTngStateResource other = (LTTngStateResource) obj;
        if (fcontext == null) {
            if (other.fcontext != null) {
                return false;
            }
        } else if (!fcontext.equals(other.fcontext)) {
            return false;
        }
        if (fstateMode != other.fstateMode) {
            return false;
        }
        return true;
    }

}
