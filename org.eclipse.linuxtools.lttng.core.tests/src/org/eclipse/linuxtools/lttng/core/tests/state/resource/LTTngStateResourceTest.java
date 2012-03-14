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
package org.eclipse.linuxtools.lttng.core.tests.state.resource;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.lttng.core.state.resource.ILttngStateContext;
import org.eclipse.linuxtools.internal.lttng.core.state.resource.LTTngStateResource;
import org.eclipse.linuxtools.internal.lttng.core.state.resource.ILTTngStateResource.GlobalStateMode;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * For inherited methods see: LTTngTreeNodeTest
 * 
 * @author alvaro
 * 
 */
@SuppressWarnings("nls")
public class LTTngStateResourceTest extends TestCase {
	// =======================================================================
	// Data
	// =======================================================================

	// Common context for all tests
	ILttngStateContext context;

	LTTngStateResource node10;
	LTTngStateResource node20;
	LTTngStateResource node30;
	LTTngStateResource node40;
	LTTngStateResource node50;
	LTTngStateResource node60;

	LTTngStateResource node15;
	LTTngStateResource node25;
	LTTngStateResource node35;
	LTTngStateResource node45;
	LTTngStateResource node55;
	LTTngStateResource node65;
	LTTngStateResource node67;

	// ========================================================================
	// Preparations and Finish
	// =======================================================================
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = getContext();
		// Create state resources and assign a parent
		node10 = new LTTngStateResource(10L, "node10", context, this);
		node20 = new LTTngStateResource(20L, node10, "node20", context, this);
		node30 = new LTTngStateResource(30L, node20, "node30", context, this);
		node40 = new LTTngStateResource(40L, node30, "node40", context, this);
		node50 = new LTTngStateResource(50L, node40, "node50", context, this);
		node60 = new LTTngStateResource(60L, node50, "node60", context, this);
		
		//Adding first children
		node10.addChild(node20);
		node20.addChild(node30);
		node30.addChild(node40);
		node40.addChild(node50);
		node50.addChild(node60);
		
		//create additional nodes
		node15 = new LTTngStateResource(15L, node10, "node15", context, this);
		node25 = new LTTngStateResource(25L, node20, "node25", context, this);
		node35 = new LTTngStateResource(35L, node30, "node35", context, this);
		node45 = new LTTngStateResource(45L, node40, "node45", context, this);
		node55 = new LTTngStateResource(55L, node50, "node55", context, this);
		node65 = new LTTngStateResource(65L, node60, "node65", context, this);
		node67 = new LTTngStateResource(67L, node60, "node67", context, this);
		
		// Add more children to instances
		node10.addChild(node15);
		node20.addChild(node25);
		node30.addChild(node35);
		node40.addChild(node45);
		node50.addChild(node55);
		node60.addChild(node65);
		node60.addChild(node67);
	}

	/**
	 * @return
	 */
	private ILttngStateContext getContext() {
		return new ILttngStateContext() {

			@Override
			public TmfTimeRange getTraceTimeWindow() {
				return null;
			}

			@Override
			public ITmfTrace<?> getTraceIdRef() {
				return null;
			}

			@Override
			public String getTraceId() {
				return "Test Trace";
			}

			@Override
			public int getNumberOfCpus() {
				return 1;
			}

			@Override
			public TmfTimeRange getExperimentTimeWindow() {
				return null;
			}

			@Override
			public String getExperimentName() {
				return "Test Experiment";
			}
			
			@Override
            public long getIdentifier() {
                return 0;
            }
		};
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ========================================================================
	// Methods
	// =======================================================================
	/**
	 * Test method for
	 * {@link org.eclipse.linuxtools.internal.lttng.core.state.resource.LTTngStateResource#getChildren()}
	 * .
	 */
	public void testGetChildren() {
		LTTngStateResource[] childrensOf60 = node60.getChildren();
		assertNotNull(childrensOf60);

		int size = childrensOf60.length;
		assertEquals(2, size);

		LTTngStateResource child65 = childrensOf60[0];
		LTTngStateResource child67 = childrensOf60[1];

		assertNotNull(child65);
		assertNotNull(child67);

		assertEquals("node65", child65.getName());
		assertEquals("node67", child67.getName());
	}

	/**
	 * Test method for
	 * {@link org.eclipse.linuxtools.internal.lttng.core.state.resource.LTTngStateResource#getStateMode()}
	 */
	public void testStateMode() {
		// check default
		assertEquals("unknown", node60.getStateMode().getInName());

		// check setting
		node60.setStateMode(GlobalStateMode.LTT_STATEMODE_WAIT_FORK);
		assertEquals("waitfork", node60.getStateMode().getInName());

	}
	
	/**
	 * Test method for
	 * {@link org.eclipse.linuxtools.internal.lttng.core.state.resource.LTTngStateResource#getContext()}
	 */
	public void testContext() {
		assertEquals(context, node60.getContext());
	}
}
