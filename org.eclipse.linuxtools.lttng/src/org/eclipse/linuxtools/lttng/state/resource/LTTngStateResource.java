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
package org.eclipse.linuxtools.lttng.state.resource;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNodeGeneric;
import org.eclipse.linuxtools.lttng.state.resource.ILTTngStateResource.GlobalStateMode;

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
			TraceDebug.debug("Received input is null !");
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

}
