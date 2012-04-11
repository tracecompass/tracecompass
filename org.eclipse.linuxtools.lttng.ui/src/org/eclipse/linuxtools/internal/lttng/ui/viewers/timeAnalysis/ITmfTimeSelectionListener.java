/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis;

import java.util.EventListener;

/**
 * Implemented by any user of TsfTm in order to be notified of available events,
 * upon registration
 * 
 */
public interface ITmfTimeSelectionListener extends EventListener {
	public void tsfTmProcessSelEvent(TmfTimeSelectionEvent event);
}
