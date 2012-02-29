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

package org.eclipse.linuxtools.lttng.core.tests.state;

import junit.framework.TestCase;

/**
 * @author alvaro
 * 
 */
public class TestStateManager extends TestCase {

	/**
	 * TODO: Not used for the time being, for experiment selection test cases
	 * for package state.experiment
	 */
	public void testSetTraceSelection() {
//		String logName = "traceset/trace-15316events_nolost_newformat";
		
//		LTTngTrace testStream = null;
//		try {
//			testStream = new LTTngTrace(logName, true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		if (testStream != null) {
//		    LTTngTrace[] streamList = new LTTngTrace[1];
//			streamList[0] = testStream;
			// TmfExperiment<LttngEvent> newExp = new
			// TmfExperiment<LttngEvent>(LttngEvent.class, logName, streamList);
			
			//Get the Test StateManager
			// IStateTraceManager manager = StateManagerFactoryTestSupport
			// .getManager(testStream);
			//Start execution.
			// manager.experimentUpdated(new TmfExperimentUpdatedSignal(this,
			// newExp, null), true);
			
			//Print events not handled.
			// Set<String> notHandledEvents = manager.getEventsNotHandled();
			// StringBuilder sb = new StringBuilder();
			// for (String event : notHandledEvents) {
			// sb.append("\n" + event);
			// }
			// TraceDebug.debug("Events not Handled: " + sb.toString());
//		}
	}
}
