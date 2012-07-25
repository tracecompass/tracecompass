/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.component;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.core.component.TmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * <b><u>TmfProviderManagerTest</u></b>
 * <p>
 * Test suite for the TmfProviderManager class.
 */
@SuppressWarnings({ "nls" })
public class TmfProviderManagerTest extends TestCase {

	/**
	 * @param name the test anme
	 */
	public TmfProviderManagerTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ------------------------------------------------------------------------
	// Dummy Providers
	// ------------------------------------------------------------------------

	private class TestProvider1 extends TmfDataProvider<TmfEvent> {
		public TestProvider1(Class<TmfEvent> type) {
			super("TestProvider1", type);
		}
		@Override
		public ITmfContext armRequest(ITmfDataRequest<TmfEvent> request) {
			return null;
		}
		@Override
		public TmfEvent getNext(ITmfContext context) {
			return null;
		}
		@Override
		public boolean isCompleted(ITmfDataRequest<TmfEvent> request, TmfEvent data, int nbRead) {
			return false;
		}
	}

	private class TestProvider2 extends TmfDataProvider<TmfEvent> {
		public TestProvider2(Class<TmfEvent> type) {
			super("TestProvider2", type);
		}
		@Override
		public ITmfContext armRequest(ITmfDataRequest<TmfEvent> request) {
			return null;
		}
		@Override
		public TmfEvent getNext(ITmfContext context) {
			return null;
		}
		@Override
		public boolean isCompleted(ITmfDataRequest<TmfEvent> request, TmfEvent data, int nbRead) {
			return false;
		}
	}

	private class TmfEvent3 extends TmfEvent {
		@SuppressWarnings("unused")
        public TmfEvent3(TmfEvent3 other) {
			super(other);
		}
	}

	private class TestProvider3 extends TmfDataProvider<TmfEvent3> {
		public TestProvider3(Class<TmfEvent3> type) {
			super("TestProvider3", type);
		}
		@Override
		public ITmfContext armRequest(ITmfDataRequest<TmfEvent3> request) {
			return null;
		}
		@Override
		public TmfEvent3 getNext(ITmfContext context) {
			return null;
		}
		@Override
		public boolean isCompleted(ITmfDataRequest<TmfEvent3> request, TmfEvent3 data, int nbRead) {
			return false;
		}
	}

	// ------------------------------------------------------------------------
	// register/dispose
	// ------------------------------------------------------------------------

	/**
	 *
	 */
	public void testRegister_0() {
		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 0, providers.length);
	}

	/**
	 *
	 */
	public void testRegister_Unregister_1() {

		// Register a single provider
		TestProvider1 testProvider1 = new TestProvider1(TmfEvent.class);

		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider1, providers[0]);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider1, providers[0]);

		// Unregister it
		testProvider1.dispose();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 0, providers.length);
	}

	/**
	 *
	 */
	public void testRegister_Unregister_2() {

		// Register 2 providers, same data type
		TestProvider1 testProvider1 = new TestProvider1(TmfEvent.class);
		TestProvider2 testProvider2 = new TestProvider2(TmfEvent.class);

		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 2, providers.length);
		assertTrue(providers.length == 2);
		if (providers[0] == testProvider1) {
			assertEquals("getProviders", testProvider2, providers[1]);
		}
		else {
			assertEquals("getProviders", testProvider2, providers[0]);
			assertEquals("getProviders", testProvider1, providers[1]);
		}

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider1, providers[0]);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider2, providers[0]);

		// Remove one
		testProvider1.dispose();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider2, providers[0]);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider2, providers[0]);

		// Remove the other
		testProvider2.dispose();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertEquals("getProviders", 0, providers.length);
	}

	/**
	 *
	 */
	public void testRegister_Unregister_3() {

		// Register 3 providers, mixed data types
		TestProvider1 testProvider1 = new TestProvider1(TmfEvent.class);
		TestProvider2 testProvider2 = new TestProvider2(TmfEvent.class);
		TestProvider3 testProvider3 = new TestProvider3(TmfEvent3.class);

		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 2, providers.length);
		if (providers[0] == testProvider1) {
			assertEquals("getProviders", testProvider2, providers[1]);
		}
		else {
			assertEquals("getProviders", testProvider2, providers[0]);
			assertEquals("getProviders", testProvider1, providers[1]);
		}

		TmfDataProvider<TmfEvent3>[] providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class);
		assertEquals("getProviders", 1, providers3.length);
		assertEquals("getProviders", testProvider3, providers3[0]);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider1, providers[0]);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider2, providers[0]);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
		assertEquals("getProviders", 1, providers3.length);
		assertEquals("getProviders", testProvider3, providers3[0]);

		// Remove one
		testProvider1.dispose();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider2, providers[0]);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider2, providers[0]);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class);
		assertEquals("getProviders", 1, providers3.length);
		assertEquals("getProviders", testProvider3, providers3[0]);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
		assertEquals("getProviders", 1, providers3.length);
		assertEquals("getProviders", testProvider3, providers3[0]);

		// Remove another one
		testProvider2.dispose();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertEquals("getProviders", 0, providers.length);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class);
		assertTrue(providers3.length == 1);
		assertTrue(providers3[0] == testProvider3);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
		assertEquals("getProviders", 1, providers3.length);
		assertEquals("getProviders", testProvider3, providers3[0]);

		// Remove the last one
		testProvider3.dispose();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertEquals("getProviders", 0, providers.length);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertEquals("getProviders", 0, providers.length);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class);
		assertEquals("getProviders", 0, providers.length);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
		assertEquals("getProviders", 0, providers.length);
	}

	/**
	 *
	 */
	public void testGetProvider() {

		// Register 3 providers, mixed data types
		TestProvider1 testProvider1 = new TestProvider1(TmfEvent.class);
		TestProvider2 testProvider2 = new TestProvider2(TmfEvent.class);
		TestProvider3 testProvider3 = new TestProvider3(TmfEvent3.class);

		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, null);
		assertEquals("getProviders", 2, providers.length);
		if (providers[0] == testProvider1) {
			assertEquals("getProviders", testProvider2, providers[1]);
		}
		else {
			assertEquals("getProviders", testProvider2, providers[0]);
			assertEquals("getProviders", testProvider1, providers[1]);
		}

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent3.class, null);
		assertEquals("getProviders", 1, providers.length);
		assertEquals("getProviders", testProvider3, providers[0]);

		// Remove the providers
		testProvider1.dispose();
		testProvider2.dispose();
		testProvider3.dispose();
	}

}