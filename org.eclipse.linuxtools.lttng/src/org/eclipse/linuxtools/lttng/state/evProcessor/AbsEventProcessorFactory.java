/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.state.evProcessor;

/**
 * @author alvaro
 * 
 */
public abstract class AbsEventProcessorFactory {
	/**
	 * 
	 * @return The Event Handler for received event before the State data model
	 *         is updated.
	 */
	public abstract IEventProcessing getBeforeProcessor(String eventType);

	/**
	 * 
	 * @return The Event Handler for received event after the State data model
	 *         is updated.
	 */
	public abstract IEventProcessing getAfterProcessor(String eventType);

	/**
	 * 
	 * @return The Event Handler after the complete read request is completed,
	 *         needed e.g. to draw the last state
	 */
	public abstract IEventProcessing getfinishProcessor();

	/**
	 * 
	 * @return The Event Handler for received event in charge to update the
	 *         state. Only one handler is expected so other factories must not
	 *         override this method.
	 */
	public IEventProcessing getStateUpdaterProcessor(String eventType) {
		return null;
	}

}
