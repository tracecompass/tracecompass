/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 **********************************************************************/

package org.eclipse.linuxtools.lttng2.control.ui.tests.model.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponentChangedListener;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceControlRoot;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.LTTngControlService;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.LTTngControlServiceConstants;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.LTTngControlServiceMI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * The class <code>TraceControlComponentTest</code> contains tests for the class
 * <code>{@link TraceControlComponent}</code>.
 */
public class TraceControlComponentTest {

    /**
     * Run the TraceControlComponent(String) constructor test.
     */
    @Test
    public void testTraceControlComponent_1() {

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
     */
    @Test
    public void testTraceControlComponent_2() {
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
     */
    @Test
    public void testAddAndGetChild1() {
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
     */
    @Test
    public void testAddAndGetChild2() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        ITraceControlComponent component = null;

        fixture.addChild(component);
        assertFalse(fixture.hasChildren());
    }

    /**
     * Run the void addComponentListener(ITraceControlComponentChangedListener) method test.
     */
    @Test
    public void testAddComponentListener_1() {
        TraceControlComponent fixture = new TraceControlComponent("", (ITraceControlComponent) null);
        fixture.setToolTip("");

        ListenerValidator validator = new ListenerValidator();
        fixture.addComponentListener(validator);

        TraceControlRoot root = new TraceControlRoot();
        fixture.addChild(root);
        assertTrue(validator.isAddedCalled());

        fixture.removeChild(root);
        assertTrue(validator.isRemovedCalled());

        fixture.fireComponentChanged(fixture);
        assertTrue(validator.isChangedCalled());
    }

    /**
     * Run the boolean containsChild(String) method test.
     */
    @Test
    public void testContainsChild_1() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        String name = "node";

        boolean result = fixture.containsChild(name);

        assertEquals(false, result);
    }

    /**
     * Run the boolean containsChild(String) method test.
     */
    @Test
    public void testContainsChild_2() {
        TraceControlComponent fixture = new TraceControlComponent("name", new TraceControlRoot());
        fixture.setToolTip("");

        boolean result = fixture.containsChild(TraceControlRoot.TRACE_CONTROL_ROOT_NAME);

        assertEquals(false, result);
    }

    /**
     * Run the void fireCompenentAdded(ITraceControlComponent,ITraceControlComponent) method test.
     * Run the void fireCompenentRemoved(ITraceControlComponent,ITraceControlComponent) method test.
     * Run the void fireCompenentChanged(ITraceControlComponent) method test
     */
    @Test
    public void testFireCompenentUpdated() {
        ITraceControlComponent parent = new TraceControlRoot();

        TraceControlComponent fixture = new TraceControlComponent("node", parent);
        fixture.setToolTip("");

        ITraceControlComponent component = new TraceControlComponent("child");
        fixture.addChild(component);

        ListenerValidator validator = new ListenerValidator();
        fixture.addComponentListener(validator);

        fixture.fireComponentAdded(parent, component);
        assertTrue(validator.isAddedCalled());
        assertEquals(parent.getName(), validator.getSavedParent().getName());
        assertEquals(component.getName(), validator.getSavedChild().getName());

        validator.initialize();

        fixture.fireComponentRemoved(parent, component);
        assertTrue(validator.isRemovedCalled());
        assertEquals(parent.getName(), validator.getSavedParent().getName());
        assertEquals(component.getName(), validator.getSavedChild().getName());

        validator.initialize();
        fixture.fireComponentChanged(fixture);
        assertTrue(validator.isChangedCalled());
        assertEquals(fixture.getName(), validator.getSavedComponent().getName());
    }

    /**
     * Run the Object getAdapter(Class) method test.
     */
    @Test
    public void testGetAdapter() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        Class<Object> adapter = Object.class;

        Object result = fixture.getAdapter(adapter);

        assertEquals(null, result);
    }

    /**
     * Run the ITraceControlComponent[] getChildren() method test.
     */
    @Test
    public void testGetChildren_1() {
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
     * Run the ILttngControlService getControlService()/setControlService()
     * method test.
     *
     * @throws ExecutionException
     *             Would fail the test
     */
    @Test
    public void testGetAndSetControlService_1() throws ExecutionException {

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

        service = new LTTngControlServiceMI(proxy.createCommandShell(), LTTngControlServiceMI.class.getResource(LTTngControlServiceConstants.MI_XSD_FILENAME));
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
     */
    @Test
    public void testGetImage_1() {
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
     */
    @Test
    public void testHasChildren_1() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());

        boolean result = fixture.hasChildren();

        assertTrue(result);
    }

    /**
     * Run the boolean hasChildren() method test.
     */
    @Test
    public void testHasChildren_2() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");

        boolean result = fixture.hasChildren();

        assertFalse(result);
    }

    /**
     * Run the void removeAllChildren() method test.
     */
    @Test
    public void testRemoveAllChildren_2() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");

        fixture.addChild(new TraceControlRoot());
        fixture.addChild(new TraceControlComponent("child"));

        fixture.removeAllChildren();
        assertFalse(fixture.hasChildren());
    }

    /**
     * Run the void removeChild(ITraceControlComponent) method test.
     */
    @Test
    public void testRemoveChild_1() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        TraceControlComponent child = new TraceControlComponent("child", fixture);

        fixture.addChild(child);
        fixture.removeChild(child);
        assertFalse(fixture.hasChildren());
    }

    /**
     * Run the void removeChild(ITraceControlComponent) method test.
     */
    @Test
    public void testRemoveChild_2() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        ITraceControlComponent component = null;

        fixture.removeChild(component);
        assertTrue(fixture.hasChildren());
    }

    /**
     * Run the void removeComponentListener(ITraceControlComponentChangedListener) method test.
     */
    @Test
    public void testRemoveComponentListener_1() {
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

        fixture.fireComponentChanged(fixture);
        assertFalse(validator.isChangedCalled());
    }

    /**
     * Run the void removeComponentListener(ITraceControlComponentChangedListener) method test.
     */
    @Test
    public void testRemoveComponentListener_2() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        ITraceControlComponentChangedListener listener = new ControlView();

        fixture.removeComponentListener(listener);

    }

    /**
     * Run the void setChildren(List<ITraceControlComponent>)/ITraceControlComponent[] getChildren() method test.
     */
    @Test
    public void testGetAndSetChildren() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        List<ITraceControlComponent> children = new LinkedList<>();
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
     */
    @Test
    public void testGetAndSetName() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("");
        fixture.addChild(new TraceControlRoot());
        String name = "node";

        fixture.setName(name);
        assertEquals(name,fixture.getName());

    }

    /**
     * Run the void ITraceControlComponent getParent()/setParent(ITraceControlComponent) method tests.
     */
    @Test
    public void testGetAndSetParent() {
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
     */
    @Test
    public void testGetAndSetTargetNodeState_1() {
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
     */
    @Test
    public void testGetSndSetToolTip() {
        TraceControlComponent fixture = new TraceControlComponent("", new TraceControlRoot());
        fixture.setToolTip("This is a tooltip");
        fixture.addChild(new TraceControlRoot());

        String result = fixture.getToolTip();

        assertEquals("This is a tooltip", result);
    }
}