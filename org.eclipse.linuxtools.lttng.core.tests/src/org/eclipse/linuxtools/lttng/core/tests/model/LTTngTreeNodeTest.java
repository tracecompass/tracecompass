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
package org.eclipse.linuxtools.lttng.core.tests.model;

import junit.framework.TestCase;

import org.eclipse.linuxtools.lttng.core.model.LTTngTreeNode;

/**
 * @author alvaro
 *
 */
@SuppressWarnings("nls")
public class LTTngTreeNodeTest extends TestCase {
	// =======================================================================
	// Data
	// =======================================================================
	LTTngTreeNode node10;
	LTTngTreeNode node20;
	LTTngTreeNode node30;
	LTTngTreeNode node40;
	LTTngTreeNode node50;
	LTTngTreeNode node60;

	LTTngTreeNode node15;
	LTTngTreeNode node25;
	LTTngTreeNode node35;
	LTTngTreeNode node45;
	LTTngTreeNode node55;
	LTTngTreeNode node65;
	LTTngTreeNode node67;

	// =======================================================================
	// Preparation and Finish
	// =======================================================================
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Create state resources and assign a parent
		node10 = new LTTngTreeNode(10L, null, "node10", this);
		node20 = new LTTngTreeNode(20L, node10, "node20", this);
		node30 = new LTTngTreeNode(30L, node20, "node30", this);
		node40 = new LTTngTreeNode(40L, node30, "node40", this);
		node50 = new LTTngTreeNode(50L, node40, "node50", this);
		node60 = new LTTngTreeNode(60L, node50, "node60", this);
		
		//Adding first children
		node10.addChild(node20);
		node20.addChild(node30);
		node30.addChild(node40);
		node40.addChild(node50);
		node50.addChild(node60);
		
		//create additional nodes
		node15 = new LTTngTreeNode(15L, node10, "node15", this);
		node25 = new LTTngTreeNode(25L, node20, "node25", this);
		node35 = new LTTngTreeNode(35L, node30, "node35", this);
		node45 = new LTTngTreeNode(45L, node40, "node45", this);
		node55 = new LTTngTreeNode(55L, node50, "node55", this);
		node65 = new LTTngTreeNode(65L, node60, "node65", this);
		node67 = new LTTngTreeNode(67L, node60, "node67", this);
		
		
		// Add children to instances
		node10.addChild(node15);
		node20.addChild(node25);
		node30.addChild(node35);
		node40.addChild(node45);
		node50.addChild(node55);
		node60.addChild(node65);
		node60.addChild(node67);

	}

	// =======================================================================
	// Methods
	// =======================================================================
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNode#getChildren()}.
	 */
	public void testGetChildren() {
		LTTngTreeNode[] childrensOf60 = node60.getChildren();
		assertNotNull(childrensOf60);

		int size = childrensOf60.length;
		assertEquals(2, size);

		LTTngTreeNode child65 = childrensOf60[0];
		LTTngTreeNode child67 = childrensOf60[1];

		assertNotNull(child65);
		assertNotNull(child67);

		assertEquals("node65", child65.getName());
		assertEquals("node67", child67.getName());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getId()}.
	 */
	public void testGetId() {
		assertEquals(15L, node15.getId().longValue());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getType()}.
	 */
	public void testGetType() {
		assertEquals(this.getClass(), node15.getNodeType());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getChildByName(java.lang.String)}.
	 */
	public void testGetChildByName() {
		LTTngTreeNode child65 = node60.getChildByName("node65");
		LTTngTreeNode child67 = node60.getChildByName("node67");
		assertNotNull(child65);
		assertNotNull(child67);

		assertEquals("node65", child65.getName());
		assertEquals("node67", child67.getName());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#removeChild(org.eclipse.linuxtools.lttng.core.model.ILTTngTreeNode)}.
	 */
	public void testRemoveChild() {
		// Verify node20
		LTTngTreeNode[] childrensOf20 = node20.getChildren();
		assertNotNull(childrensOf20);

		int size = childrensOf20.length;
		assertEquals(2, size);

		LTTngTreeNode child25 = childrensOf20[0];
		LTTngTreeNode child30 = childrensOf20[1];

		assertNotNull(child25);
		assertNotNull(child30);

		assertEquals("node25", child25.getName());
		assertEquals("node30", child30.getName());

		// Remove a child with unusual values.
		node20.removeChild(null);
		node20.removeChild(node60);

		// Remove a valid child
		node20.removeChild(node30);

		// Verify consistency
		childrensOf20 = node20.getChildren();
		assertNotNull(childrensOf20);

		size = childrensOf20.length;
		assertEquals(1, size);

		child25 = childrensOf20[0];

		assertNotNull(child25);

		assertEquals("node25", child25.getName());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getChildById(java.lang.Integer)}.
	 */
	public void testGetChildById() {
		LTTngTreeNode child65 = node60.getChildById(65L);
		LTTngTreeNode child67 = node60.getChildById(67L);
		assertNotNull(child65);
		assertNotNull(child67);

		assertEquals("node65", child65.getName());
		assertEquals("node67", child67.getName());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getParent()}.
	 */
	public void testGetParent() {
		assertEquals(node60, node67.getParent());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#setParent(org.eclipse.linuxtools.lttng.core.model.ILTTngTreeNode)}.
	 */
	public void testSetParent() {
		node30.removeChild(node35);
		node60.addChild(node35);
		node35.setParent(node60);

		assertEquals(node60, node35.getParent());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#hasChildren()}.
	 */
	public void testHasChildren() {
		assertEquals(true, node10.hasChildren());

		node10.removeChild(node15);
		node10.removeChild(node20);

		assertEquals(false, node10.hasChildren());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getName()}.
	 */
	public void testGetName() {
		assertEquals("node40", node40.getName());
	}

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getPath()}.
	 */
	public void testGetPath() {
		String path = node60.getPath();
		assertEquals("/node10/node20/node30/node40/node50/node60", path);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getAdapter()}
	 * .
	 */
	public void testGetAdapter() {
		Object value = node60.getAdapter(this.getClass());
		assertEquals("Unexpected Adapter reference", this, value);
	}

	/**
	 * Test method for
	 * {@link org.eclipse.linuxtools.lttng.core.model.LTTngTreeNodeGeneric#getAttibute()}
	 * .
	 */
	public void testGetAttribute() {
		Long lval = Long.valueOf(10L);
		node60.addAttribute("attr1", "Value1");
		node60.addAttribute("attr2", lval);
		node60.addAttribute("attr3", node50);

		assertEquals("Value1", node60.getAttribute("attr1", String.class));
		assertEquals(lval, node60.getAttribute("attr2", Long.class));
		assertEquals(node50, node60.getAttribute("attr3", LTTngTreeNode.class));
		assertEquals(null, node60.getAttribute("attr1", LTTngTreeNode.class));
	}

}
