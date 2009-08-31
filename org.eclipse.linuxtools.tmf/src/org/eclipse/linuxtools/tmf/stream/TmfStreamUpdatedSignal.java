/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.stream;

import org.eclipse.linuxtools.tmf.signal.TmfSignal;

/**
 * <b><u>TmfStreamUpdatedEvent</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfStreamUpdatedSignal extends TmfSignal {

	private final ITmfEventStream fEventStream;
	
	public TmfStreamUpdatedSignal(Object source, ITmfEventStream stream) {
		super(source);
		fEventStream = stream;
	}

	public ITmfEventStream getEventStream() {
		return fEventStream;
	}
}
