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

package org.eclipse.linuxtools.tmf.tests.component;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.component.TmfDataProvider;
import org.eclipse.linuxtools.tmf.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;

/**
 * <b><u>TmfProviderManagerTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfProviderManagerTest extends TestCase {

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
	
	public class TestProvider1 extends TmfDataProvider<TmfEvent> {
		public TestProvider1(Class<TmfEvent> type) {
			super("TestProvider1", type);
		}
		@Override
		public ITmfContext armRequest(TmfDataRequest<TmfEvent> request) {
			return null;
		}
		@Override
		public TmfEvent getNext(ITmfContext context) {
			return null;
		}
		@Override
		public boolean isCompleted(TmfDataRequest<TmfEvent> request, TmfEvent data, int nbRead) {
			return false;
		}
	}

	public class TestProvider2 extends TmfDataProvider<TmfEvent> {
		public TestProvider2(Class<TmfEvent> type) {
			super("TestProvider2", type);
		}
		@Override
		public ITmfContext armRequest(TmfDataRequest<TmfEvent> request) {
			return null;
		}
		@Override
		public TmfEvent getNext(ITmfContext context) {
			return null;
		}
		@Override
		public boolean isCompleted(TmfDataRequest<TmfEvent> request, TmfEvent data, int nbRead) {
			return false;
		}
	}

	public class TmfEvent3 extends TmfEvent {
		public TmfEvent3(TmfEvent3 other) {
			super(other);
		}
	}

	public class TestProvider3 extends TmfDataProvider<TmfEvent3> {
		public TestProvider3(Class<TmfEvent3> type) {
			super("TestProvider3", type);
		}
		@Override
		public ITmfContext armRequest(TmfDataRequest<TmfEvent3> request) {
			return null;
		}
		@Override
		public TmfEvent3 getNext(ITmfContext context) {
			return null;
		}
		@Override
		public boolean isCompleted(TmfDataRequest<TmfEvent3> request, TmfEvent3 data, int nbRead) {
			return false;
		}
	}

	// ------------------------------------------------------------------------
	// register()
	// ------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public void testRegister_0() {
		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 0);
	}

	@SuppressWarnings("unchecked")
	public void testRegister_Unregister_1() {

		// Register a single provider
		TestProvider1 testProvider1 = new TestProvider1(TmfEvent.class);

		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider1);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider1);

		// Unregister it
		testProvider1.deregister();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 0);
	}

	@SuppressWarnings("unchecked")
	public void testRegister_Unregister_2() {

		// Register 2 providers, same data type
		TestProvider1 testProvider1 = new TestProvider1(TmfEvent.class);
		TestProvider2 testProvider2 = new TestProvider2(TmfEvent.class);

		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 2);
		if (providers[0] == testProvider1) {
			assertTrue(providers[1] == testProvider2);
		}
		else {
			assertTrue(providers[1] == testProvider1);
			assertTrue(providers[0] == testProvider2);
		}

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider1);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider2);

		// Remove one
		testProvider1.deregister();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider2);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider2);

		// Remove the other
		testProvider2.deregister();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertTrue(providers.length == 0);
	}

	@SuppressWarnings("unchecked")
	public void testRegister_Unregister_3() {

		// Register 3 providers, mixed data types
		TestProvider1 testProvider1 = new TestProvider1(TmfEvent.class);
		TestProvider2 testProvider2 = new TestProvider2(TmfEvent.class);
		TestProvider3 testProvider3 = new TestProvider3(TmfEvent3.class);

		TmfDataProvider<TmfEvent>[] providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 2);
		if (providers[0] == testProvider1) {
			assertTrue(providers[1] == testProvider2);
		}
		else {
			assertTrue(providers[1] == testProvider1);
			assertTrue(providers[0] == testProvider2);
		}

		TmfDataProvider<TmfEvent3>[] providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class);
		assertTrue(providers3.length == 1);
		assertTrue(providers3[0] == testProvider3);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider1);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider2);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
		assertTrue(providers3.length == 1);
		assertTrue(providers3[0] == testProvider3);

		// Remove one
		testProvider1.deregister();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider2);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertTrue(providers.length == 1);
		assertTrue(providers[0] == testProvider2);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class);
		assertTrue(providers3.length == 1);
		assertTrue(providers3[0] == testProvider3);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
		assertTrue(providers3.length == 1);
		assertTrue(providers3[0] == testProvider3);

		// Remove another one
		testProvider2.deregister();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertTrue(providers.length == 0);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class);
		assertTrue(providers3.length == 1);
		assertTrue(providers3[0] == testProvider3);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
		assertTrue(providers3.length == 1);
		assertTrue(providers3[0] == testProvider3);

		// Remove the last one
		testProvider3.deregister();

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider1.class);
		assertTrue(providers.length == 0);

		providers = (TmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TestProvider2.class);
		assertTrue(providers.length == 0);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class);
		assertTrue(providers3.length == 0);

		providers3 = (TmfDataProvider<TmfEvent3>[]) TmfProviderManager.getProviders(TmfEvent3.class, TestProvider3.class);
		assertTrue(providers3.length == 0);
	}

}