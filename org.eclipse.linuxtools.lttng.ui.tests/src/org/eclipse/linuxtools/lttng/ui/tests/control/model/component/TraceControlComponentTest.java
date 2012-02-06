/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.tests.control.model.component;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.lttng.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.lttng.ui.tests.control.model.impl.ListenerValidator;
import org.eclipse.linuxtools.lttng.ui.views.control.ControlView;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceControlComponentChangedListener;
import org.eclipse.linuxtools.lttng.ui.views.control.model.TargetNodeState;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceControlComponent;
import org.eclipse.linuxtools.lttng.ui.views.control.model.impl.TraceControlRoot;
import org.eclipse.linuxtools.lttng.ui.views.control.service.ILttngControlService;
import org.eclipse.linuxtools.lttng.ui.views.control.service.LTTngControlService;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;

/**
 * The class <code>TraceControlComponentTest</code> contains tests for the class <code>{@link TraceControlComponent}</code>.
 *
 */
@SuppressWarnings("nls")
public class TraceControlComponentTest extends TestCase {
    
    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------
    
    // ------------------------------------------------------------------------
    // Static methods
    // ------------------------------------------------------------------------

    /**
     * Returns test setup used when executing test case stand-alone.
     * @return Test setup class 
     */
    public static Test suite() {
        return new ModelImplTestSetup(new TestSuite(TraceControlComponentTest.class));
    }

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Perform pre-test initialization.
     *
     * @throws Exception
     *         if the initialization fails for some reason
     *
     */
    @Override
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception
     *         if the clean-up fails for some reason
     *
     */
    @Override
    @After
    public void tearDown()  throws Exception {
    }
    
    /**
     * Run the TraceControlComponent(String) constructor test.
     */
    public void testTraceControlComponent_1()
        throws Exception {
        
        String name = "node";

        TraceControlComponent result = new TraceControlComponent(name);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(null, result.getParent());
        assertEquals(false, result.hasChildren());
        assertEquals(null, result.getImage());
        assertEquals(null, result.getControlService());
        assertEquals(null, result.getToolTip());
    }

    /**
     * Run the TraceControlComponent(String,ITraceControlComponent) constructor test.
     *
     */
    public void testTraceControlComponent_2()
        throws Exception {
        String name = "node";

        ITraceControlComponent parent = new TraceControlRoot();
        TraceControlComponent result = new TraceControlComponent(name, parent);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(false, result.hasChildren());
        assertEquals(null, result.getImage());
        assertEquals(null, result.getControlService());
        assertEquals(null, result.getToolTip());
    }

    /**
     * Run the void addChild(ITraceControlComponent) method test.
     *
     * @throws Exception
     *
     */
    public void testAddAndGetChild1()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("node", new TraceControlRoot());
        fixture.setToolTip("This is the test node");
        fixture.addChild(new TraceControlRoot());
        ITraceControlComponent component = new TraceControlRoot();
        fixture.addChild(component);
        
        ITraceControlComponent child = fixture.getChild(TraceControlRoot.TRACE_CONTROL_ROOT_NAME);
        assertNotNull(child);
        assertEquals(TraceControlRoot.TRACE_CONTROL_ROOT_NAME, child.getName());
    }

    /**
     * Run the void addChild(ITraceControlComponent) method test.
     *
     * @throws Exception
     *
     */
    public void testAddAndGetChild2()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        ITraceControlComponent component = null;

        fixture.addChild(component);
        assertFalse(fixture.hasChildren());
    }

    /**
     * Run the void addComponentListener(ITraceControlComponentChangedListener) method test.
     *
     * @throws Exception
     *
     */
    
    public void testAddComponentListener_1()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", (ITraceControlComponent) null);
        fixture.setToolTip("");

        ListenerValidator validator = new ListenerValidator();
        fixture.addComponentListener(validator);

        TraceControlRoot root = new TraceControlRoot();
        fixture.addChild(root);
        assertTrue(validator.isAddedCalled());
        
        fixture.removeChild(root);
        assertTrue(validator.isRemovedCalled());

        fixture.fireCompenentChanged(fixture);
        assertTrue(validator.isChangedCalled());
    }

    /**
     * Run the boolean containsChild(String) method test.
     *
     * @throws Exception
     *
     */
    public void testContainsChild_1()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        String name = "node";

        boolean result = fixture.containsChild(name);

        assertEquals(false, result);
    }

    /**
     * Run the boolean containsChild(String) method test.
     *
     * @throws Exception
     *
     */
    public void testContainsChild_2()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("name", new TraceControlRoot());
        fixture.setToolTip("");

        boolean result = fixture.containsChild(TraceControlRoot.TRACE_CONTROL_ROOT_NAME);

        assertEquals(false, result);
    }

    /**
     * Run the void fireCompenentAdded(ITraceControlComponent,ITraceControlComponent) method test.
     * Run the void fireCompenentRemoved(ITraceControlComponent,ITraceControlComponent) method test.
     * Run the void fireCompenentChanged(ITraceControlComponent) method test
     *
     * @throws Exception
     *
     */
    
    public void testFireCompenentUpdated()
        throws Exception {
        ITraceControlComponent parent = new TraceControlRoot();
        
        TraceControlComponent fixture = new TraceControlComponent("node", parent);
        fixture.setToolTip("");
        
        ITraceControlComponent component = new TraceControlComponent("child");
        fixture.addChild(component);

        ListenerValidator validator = new ListenerValidator();
        fixture.addComponentListener(validator);

        fixture.fireCompenentAdded(parent, component);
        assertTrue(validator.isAddedCalled());
        assertEquals(parent.getName(), validator.getSavedParent().getName());
        assertEquals(component.getName(), validator.getSavedChild().getName());
        
        validator.initialize();
        
        fixture.fireCompenentRemoved(parent, component);
        assertTrue(validator.isRemovedCalled());
        assertEquals(parent.getName(), validator.getSavedParent().getName());
        assertEquals(component.getName(), validator.getSavedChild().getName());

        validator.initialize();
        fixture.fireCompenentChanged(fixture);
        assertTrue(validator.isChangedCalled());
        assertEquals(fixture.getName(), validator.getSavedComponent().getName());
    }

    /**
     * Run the Object getAdapter(Class) method test.
     *
     * @throws Exception
     *
     */
    
    public void testGetAdapter()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        Class<Object> adapter = Object.class;

        Object result = fixture.getAdapter(adapter);

        assertEquals(null, result);
    }

    /**
     * Run the ITraceControlComponent[] getChildren() method test.
     *
     * @throws Exception
     *
     */
    public void testGetChildren_1()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());

        ITraceControlComponent[] result = fixture.getChildren();

        assertNotNull(result);
        assertEquals(1, result.length);
        assertNotNull(result[0]);
        assertEquals("trace_control_root", result[0].getName());
        assertEquals(null, result[0].getParent());
        assertEquals(false, result[0].hasChildren());
        assertEquals(null, result[0].getImage());
        assertEquals(null, result[0].getControlService());
        assertEquals(null, result[0].getToolTip());
    }

    /**
     * Run the ILttngControlService getControlService()/setControlService() method test.
     *
     * @throws Exception
     *
     */
    public void testGetAndSetControlService_1()
        throws Exception {
        
        TraceControlComponent parent = new TraceControlComponent("parent") {
            ILttngControlService fService = null;
            
            @Override
            public void setControlService(ILttngControlService service ) {
                fService = service;
            }
            
            @Override
            public ILttngControlService getControlService() {
                return fService;
            }
        };
        
        TraceControlComponent fixture = new TraceControlComponent("", parent);
        parent.addChild(fixture);
        fixture.setToolTip("");
        TraceControlComponent child = new TraceControlComponent("child", fixture);
        fixture.addChild(child);
        
        ILttngControlService result = fixture.getControlService();
        assertEquals(null, result);
        
        TestRemoteSystemProxy proxy = new TestRemoteSystemProxy();
        ILttngControlService service = new LTTngControlService(proxy.createCommandShell());
        fixture.setControlService(service);
        result = fixture.getControlService();
        assertNotNull(service);
        assertEquals(service, result);
        
        result = fixture.getChildren()[0].getControlService();
        assertNotNull(service);
        assertEquals(service, result);
    }

    /**
     * Run the Image getImage() method test.
     *
     * @throws Exception
     *
     */
    public void testGetImage_1()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());

        Image result = fixture.getImage();
        assertEquals(null, result);
        
        fixture.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER));
        assertNotNull(fixture.getImage());
    }

    /**
     * Run the boolean hasChildren() method test.
     *
     * @throws Exception
     *
     */
    public void testHasChildren_1()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());

        boolean result = fixture.hasChildren();

        assertTrue(result);
    }

    /**
     * Run the boolean hasChildren() method test.
     *
     * @throws Exception
     *
     */
    public void testHasChildren_2()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");

        boolean result = fixture.hasChildren();

        assertFalse(result);
    }

    /**
     * Run the void removeAllChildren() method test.
     *
     * @throws Exception
     *
     */
    public void testRemoveAllChildren_2()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");

        fixture.addChild(new TraceControlRoot());
        fixture.addChild(new TraceControlComponent("child"));
        
        fixture.removeAllChildren();
        assertFalse(fixture.hasChildren());
    }

    /**
     * Run the void removeChild(ITraceControlComponent) method test.
     *
     * @throws Exception
     *
     */
    public void testRemoveChild_1()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        TraceControlComponent child = new TraceControlComponent("child", fixture);
        
        fixture.addChild(child);
        fixture.removeChild(child);
        assertFalse(fixture.hasChildren());
    }

    /**
     * Run the void removeChild(ITraceControlComponent) method test.
     *
     * @throws Exception
     *
     */
    
    public void testRemoveChild_2()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        ITraceControlComponent component = null;

        fixture.removeChild(component);
        assertTrue(fixture.hasChildren());
    }

    /**
     * Run the void removeComponentListener(ITraceControlComponentChangedListener) method test.
     *
     * @throws Exception
     *
     */
    public void testRemoveComponentListener_1()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", (ITraceControlComponent) null);
        fixture.setToolTip("");
        
        ListenerValidator validator = new ListenerValidator();
        fixture.addComponentListener(validator);

        // Remove listener and check that validator is not called anymore
        validator.initialize();
        fixture.removeComponentListener(validator);
        TraceControlRoot root = new TraceControlRoot();
        fixture.addChild(root);
        assertFalse(validator.isAddedCalled());
        
        fixture.removeChild(root);
        assertFalse(validator.isRemovedCalled());

        fixture.fireCompenentChanged(fixture);
        assertFalse(validator.isChangedCalled());
    }

    /**
     * Run the void removeComponentListener(ITraceControlComponentChangedListener) method test.
     *
     * @throws Exception
     *
     */
    public void testRemoveComponentListener_2()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        ITraceControlComponentChangedListener listener = new ControlView();

        fixture.removeComponentListener(listener);

    }

    /**
     * Run the void setChildren(List<ITraceControlComponent>)/ITraceControlComponent[] getChildren() method test.
     * 
     *
     * @throws Exception
     *
     */
    public void testGetAndSetChildren()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        List<ITraceControlComponent> children = new LinkedList<ITraceControlComponent>();
        children.add(new TraceControlComponent("child1"));
        children.add(new TraceControlComponent("child2"));

        fixture.setChildren(children);
        
        ITraceControlComponent[] result = fixture.getChildren();
        assertEquals(2, result.length);
        assertEquals("child1", result[0].getName());
        assertEquals("child2", result[1].getName());
    }

    /**
     * Run the void String getName()/setName(String) method tests.
     *
     * @throws Exception
     *
     */
    public void testGetAndSetName()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        String name = "node";

        fixture.setName(name);
        assertEquals(name,fixture.getName());

    }

    /**
     * Run the void ITraceControlComponent getParent()/setParent(ITraceControlComponent) method tests.
     *
     * @throws Exception
     *
     */
    public void testGetAndSetParent()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        ITraceControlComponent parent = new TraceControlRoot();
        parent.addChild(fixture);

        fixture.setParent(parent);
        ITraceControlComponent retrievedParent = fixture.getParent();
        assertNotNull(retrievedParent);
        assertEquals(parent.getName(), retrievedParent.getName());
        assertEquals(TraceControlRoot.TRACE_CONTROL_ROOT_NAME, retrievedParent.getName());
        assertEquals(null, retrievedParent.getParent());
        assertEquals(true, retrievedParent.hasChildren());
    }

    /**
     * Run the void TargetNodeState getTargetNodeState()/etTargetNodeState(TargetNodeState) method tests.
     *
     * @throws Exception
     *
     */
    public void testGetAndSetTargetNodeState_1()
        throws Exception {
        
        TraceControlComponent parent = new TraceControlComponent("parent") {
            private TargetNodeState fState;
            
            @Override
            public void setTargetNodeState(TargetNodeState state ) {
                fState = state;
            }
            
            @Override
            public TargetNodeState getTargetNodeState() {
                return fState;
            }
        };
        
        TraceControlComponent fixture = new TraceControlComponent("", parent);
        parent.addChild(fixture);
        
        fixture.setToolTip("");
        TargetNodeState state = TargetNodeState.CONNECTED;

        fixture.setTargetNodeState(state);
        TargetNodeState result = fixture.getTargetNodeState();
        
        assertNotNull(result);
        assertEquals(state, result);
        // Check also parent
        assertEquals(state, fixture.getParent().getTargetNodeState());
        assertEquals("CONNECTED", result.name());
        assertEquals("CONNECTED", result.toString());
        assertEquals(2, result.ordinal());

        fixture.setTargetNodeState(TargetNodeState.DISCONNECTED);
        result = fixture.getTargetNodeState();
        assertNotNull(result);
        assertEquals("DISCONNECTED", result.name());
        assertEquals("DISCONNECTED", result.toString());
        assertEquals(0, result.ordinal());
        
        state = TargetNodeState.CONNECTING;

        fixture.setTargetNodeState(state);
        result = fixture.getTargetNodeState();
        assertNotNull(result);
        assertEquals("CONNECTING", result.name());
        assertEquals("CONNECTING", result.toString());
        assertEquals(3, result.ordinal());
        
        fixture.setTargetNodeState(TargetNodeState.DISCONNECTING);
        result = fixture.getTargetNodeState();
        assertNotNull(result);
        assertEquals("DISCONNECTING", result.name());
        assertEquals("DISCONNECTING", result.toString());
        assertEquals(1, result.ordinal());

    }

    /**
     * Run the void setToolTip(String) method test.
     *
     * @throws Exception
     *
     
     */
    
    public void testGetSndSetToolTip()
        throws Exception {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("This is a tooltip");
        fixture.addChild(new TraceControlRoot());

        String result = fixture.getToolTip();

        assertEquals("This is a tooltip", result);
    }
}