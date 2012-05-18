/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.core.tests.control;

import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.core.tests.LttngTestPreparation;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

@SuppressWarnings("nls")
public class LTTngSyntheticEventProviderTest extends LttngTestPreparation {
	// ========================================================================
	// Tests
	// ========================================================================

	public void testPlainDataRequest() {
		// prepare
		init();
		TmfExperiment<LttngEvent> experiment = prepareExperimentToTest();
		TmfEventRequest<LttngEvent> request = prepareEventRequest(
				LttngEvent.class, 0, 31);

		// execute
		experiment.sendRequest(request);
		try {
			request.waitForCompletion();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// finish
		assertEquals("Unexpected eventCount", 15316, feventCount);
		boolean expected = true;
		assertEquals("Events received out of expected order", expected,
				validSequence);
	}

	public void testSyntheticEventRequest() {
//        System.out.println("testSyntheticEventRequest: test removed");
//		init();
//		// Create a new Experiment manager context
//		IStateExperimentManager expManager = prepareExperimentContext(false);
//
//		// make sure a TmfExperiment instance is registered as provider and
//		// selected as current
//		TmfExperiment<LttngEvent> experiment = prepareExperimentToTest();
//
//		// experiment selected, build experiment selection context and trigger
//		// check point creation
//		expManager.experimentSelected_prep(experiment);
//		// builds check points in parallel
//		expManager.experimentSelected(this, experiment);
//
//		// Obtain the singleton event provider
//		LttngSyntheticEventProvider synProvider = LttngCoreProviderFactory
//				.getEventProvider();
//
//		// prepare synthetic event requests
//		boolean printExpectedEvents = false;
//		TmfEventRequest<LttngSyntheticEvent> request1 = prepareEventRequest(LttngSyntheticEvent.class, 5, 9,
//				printExpectedEvents); /* 2001 events */
//		TmfEventRequest<LttngSyntheticEvent> request2 = prepareEventRequest(LttngSyntheticEvent.class, 11, 13,
//				printExpectedEvents); /* 1001 events */
//
//		// execute
//		synProvider.sendRequest(request1);
//		try {
//			request1.waitForCompletion();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.out.println("EventCount " + feventCount);
//
//		synProvider.sendRequest(request2);
//		try {
//			request2.waitForCompletion();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		System.out.println("EventCount " + feventCount);
//
//		// finish
//		assertEquals("Unexpected eventCount", 3002, feventCount);
	}

}