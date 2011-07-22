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
package org.eclipse.linuxtools.lttng.tests.control;



import org.eclipse.linuxtools.lttng.tests.LttngTestPreparation;

public class LTTngSyntheticEventProviderTextTest extends LttngTestPreparation {

	// ========================================================================
	// Tests
	// ========================================================================
	/**
	 * 
	 */
	public void testPlainDataRequest() {
//		// prepare
//		init();
//		TmfExperiment<LttngEvent> experiment = prepareTextExperimentToTest();
//		TmfEventRequest<LttngEvent> request = prepareEventRequest(
//				LttngEvent.class, 0, 31);
//
//		// execute
//		experiment.sendRequest(request);
//		try {
//			request.waitForCompletion();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		// finish
//		assertEquals("Unexpected eventCount", 15316, feventCount);
//		boolean expected = true;
//		assertEquals("Events received out of expected order", expected,
//				validSequence);
	}

	/**
	 * 
	 */
	public void testSyntheticEventRequest() {
//		init();
//		// make sure a synthetic event provider exists and it's registered
//		LttngSyntheticEventProvider synProvider = LttngCoreProviderFactory
//				.getEventProvider();
//
//		// make sure a TmfExperiment instance is registered as provider and
//		// selected as current
//		prepareTextExperimentToTest();
//
//		// prepare synthetic event request
//		TmfEventRequest<LttngSyntheticEvent> request = prepareEventRequest(
//				LttngSyntheticEvent.class, 0, 31);
//
//		// execute
//		synProvider.sendRequest(request);
//		try {
//			request.waitForCompletion();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//
//		// finish
//		assertEquals("Unexpected eventCount", 15316, feventCount);
	}
}
