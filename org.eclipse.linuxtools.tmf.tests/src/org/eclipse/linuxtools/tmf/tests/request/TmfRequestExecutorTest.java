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

package org.eclipse.linuxtools.tmf.tests.request;

import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.request.TmfRequestExecutor;

/**
 * <b><u>TmfRequestExecutorTest</u></b>
 *
 * Test suite for the TmfRequestExecutor class.
 */
public class TmfRequestExecutorTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfRequestExecutorTest(String name) {
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
	// Constructors
	// ------------------------------------------------------------------------

	/**
	 * Test method for {@link org.eclipse.linuxtools.tmf.request.TmfRequestExecutor#TmfRequestExecutor()}.
	 */
	public void testTmfRequestExecutor() {
		TmfRequestExecutor executor = new TmfRequestExecutor();
		assertTrue("TmfRequestExecutor", executor != null);
		assertTrue("TmfRequestExecutor", executor instanceof TmfRequestExecutor);
//		assertEquals("toString", "[TmfRequestExecutor(DelegatedExecutorService)]", executor.toString());

		assertEquals("nbPendingRequests", 0, executor.getNbPendingRequests());
		assertFalse("isShutdown",   executor.isShutdown());
		assertFalse("isTerminated", executor.isTerminated());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.tmf.request.TmfRequestExecutor#TmfRequestExecutor(java.util.concurrent.ExecutorService)}.
	 */
	public void testTmfRequestExecutorExecutorService() {
		TmfRequestExecutor executor = new TmfRequestExecutor(Executors.newCachedThreadPool());
		assertTrue("TmfRequestExecutor", executor != null);
		assertTrue("TmfRequestExecutor", executor instanceof TmfRequestExecutor);
//		assertEquals("toString", "[TmfRequestExecutor(ThreadPoolExecutor)]", executor.toString());

		assertEquals("nbPendingRequests", 0, executor.getNbPendingRequests());
		assertFalse("isShutdown",   executor.isShutdown());
		assertFalse("isTerminated", executor.isTerminated());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.tmf.request.TmfRequestExecutor#stop()}.
	 */
	public void testStop() {
		TmfRequestExecutor executor = new TmfRequestExecutor();
		executor.stop();
		assertEquals("nbPendingRequests", 0, executor.getNbPendingRequests());
		assertTrue("isShutdown",   executor.isShutdown());
		assertTrue("isTerminated", executor.isTerminated());
	}

//	/**
//	 * Test method for {@link org.eclipse.linuxtools.tmf.request.TmfRequestExecutor#execute(java.lang.Runnable)}.
//	 */
//	public void testExecute() {
//		fail("Not yet implemented");
//	}

}
