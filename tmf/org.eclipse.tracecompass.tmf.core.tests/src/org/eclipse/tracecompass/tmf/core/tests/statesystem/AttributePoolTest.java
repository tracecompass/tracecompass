/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.statesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfAttributePool.QueueType;
import org.junit.After;
import org.junit.Test;

/**
 * Test the {@link TmfAttributePool} class
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class AttributePoolTest {

    private static final long START_TIME = 1000L;
    private static final String DUMMY_STRING = "test";
    private static final Object VALUE = 2;

    private ITmfStateSystemBuilder fStateSystem;

    /**
     * Initialize the state system
     */
    public AttributePoolTest() {
        IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend(DUMMY_STRING, START_TIME);
        fStateSystem = StateSystemFactory.newStateSystem(backend);
    }

    /**
     * Clean-up
     */
    @After
    public void tearDown() {
        fStateSystem.dispose();
    }

    /**
     * Test the constructor of the attribute pool
     */
    @Test
    public void testConstructorGood() {
        // Create an attribute pool with ROOT_QUARK as base
        TmfAttributePool pool = new TmfAttributePool(fStateSystem, ITmfStateSystem.ROOT_ATTRIBUTE);
        assertNotNull(pool);

        int quark = fStateSystem.getQuarkAbsoluteAndAdd(DUMMY_STRING);
        pool = new TmfAttributePool(fStateSystem, quark);
    }

    /**
     * Test the constructor with an invalid attribute
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBad() {
        new TmfAttributePool(fStateSystem, ITmfStateSystem.INVALID_ATTRIBUTE);
    }

    /**
     * Test the constructor with an invalid positive attribute
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorBad2() {
        new TmfAttributePool(fStateSystem, 3);
    }

    /**
     * Test attributes using only one level of children
     */
    @Test
    public void testSimplePool() {
        TmfAttributePool pool = new TmfAttributePool(fStateSystem, ITmfStateSystem.ROOT_ATTRIBUTE);
        assertNotNull(pool);

        /* Get some attributes */
        Integer available = pool.getAvailable();
        assertNotNull(available);
        Integer available2 = pool.getAvailable();
        assertNotNull(available2);
        assertNotEquals(available, available2);

        /* Verify the names of the attributes */
        assertEquals("0", fStateSystem.getAttributeName(available));
        assertEquals("1", fStateSystem.getAttributeName(available2));

        /* Modify them */
        fStateSystem.modifyAttribute(START_TIME + 10, VALUE, available);
        fStateSystem.modifyAttribute(START_TIME + 10, VALUE, available2);

        /* Recycle one and make sure it is set to null */
        pool.recycle(available, START_TIME + 20);
        ITmfStateValue value = fStateSystem.queryOngoingState(available);
        assertEquals(TmfStateValue.nullValue(), value);

        /* Get a new one and make sure it is reusing the one just recycled */
        Integer available3 = pool.getAvailable();
        assertEquals(available, available3);

        /* Get a new attribute and make sure it is different from both other used attributes */
        Integer available4 = pool.getAvailable();
        assertNotEquals(available3, available4);
        assertNotEquals(available2, available4);

        /* Recycle available attributes, in reverse order and see how they are returned */
        pool.recycle(available4, START_TIME + 30);
        pool.recycle(available2, START_TIME + 30);
        pool.recycle(available3, START_TIME + 30);

        Integer available5 = pool.getAvailable();
        assertEquals(available4, available5);

        Integer available6 = pool.getAvailable();
        assertEquals(available2, available6);

        Integer available7 = pool.getAvailable();
        assertEquals(available3, available7);
    }

    /**
     * Test attributes with sub-trees
     */
    @Test
    public void testPoolWithSubTree() {
        TmfAttributePool pool = new TmfAttributePool(fStateSystem, ITmfStateSystem.ROOT_ATTRIBUTE);
        assertNotNull(pool);

        /* Get some attributes */
        Integer available = pool.getAvailable();
        assertNotNull(available);

        /* Add children and set values for them */
        try {
            Integer child1 = fStateSystem.getQuarkRelativeAndAdd(available, "child1");
            Integer child2 = fStateSystem.getQuarkRelativeAndAdd(available, "child2");
            fStateSystem.modifyAttribute(START_TIME + 10, VALUE, available);
            fStateSystem.modifyAttribute(START_TIME + 10, VALUE, child1);
            fStateSystem.modifyAttribute(START_TIME + 10, VALUE, child2);

            pool.recycle(available, START_TIME + 20);

            ITmfStateValue value = fStateSystem.queryOngoingState(available);
            assertEquals(TmfStateValue.nullValue(), value);
            value = fStateSystem.queryOngoingState(child1);
            assertEquals(TmfStateValue.nullValue(), value);
            value = fStateSystem.queryOngoingState(child2);
            assertEquals(TmfStateValue.nullValue(), value);
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test pool with priority queue
     */
    @Test
    public void testPriorityPool() {
        TmfAttributePool pool = new TmfAttributePool(fStateSystem, ITmfStateSystem.ROOT_ATTRIBUTE, QueueType.PRIORITY);
        assertNotNull(pool);

        /* Get some attributes */
        Integer available = pool.getAvailable();
        assertNotNull(available);
        Integer available2 = pool.getAvailable();
        assertNotNull(available2);
        assertNotEquals(available, available2);

        /* Verify the names of the attributes */
        assertEquals("0", fStateSystem.getAttributeName(available));
        assertEquals("1", fStateSystem.getAttributeName(available2));

        /* Recycle on */
        pool.recycle(available, START_TIME + 20);

        /* Get a new one and make sure it is reusing the one just recycled */
        Integer available3 = pool.getAvailable();
        assertEquals(available, available3);

        /* Get a new attribute and make sure it is different from both other used attributes */
        Integer available4 = pool.getAvailable();
        assertNotEquals(available3, available4);
        assertNotEquals(available2, available4);

        /* Recycle available attributes, in reverse order and see how they are returned */
        pool.recycle(available4, START_TIME + 30);
        pool.recycle(available2, START_TIME + 30);
        pool.recycle(available3, START_TIME + 30);

        Integer available5 = pool.getAvailable();
        assertEquals(available3, available5);

        Integer available6 = pool.getAvailable();
        assertEquals(available2, available6);

        Integer available7 = pool.getAvailable();
        assertEquals(available4, available7);
    }

    /**
     * Test recycling the root attribute
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRecycleWrongQuark() {
        TmfAttributePool pool = new TmfAttributePool(fStateSystem, ITmfStateSystem.ROOT_ATTRIBUTE);
        assertNotNull(pool);

        pool.recycle(ITmfStateSystem.ROOT_ATTRIBUTE, START_TIME + 10);
    }

    /**
     * Test recycling one of the children
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRecycleChildQuark() {
        TmfAttributePool pool = new TmfAttributePool(fStateSystem, ITmfStateSystem.ROOT_ATTRIBUTE);
        assertNotNull(pool);

        /* Get some attributes */
        Integer available = pool.getAvailable();
        assertNotNull(available);

        /* Add children and set values for them */
        try {
            Integer child1 = fStateSystem.getQuarkRelativeAndAdd(available, "child1");
            pool.recycle(child1, START_TIME + 10);
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
    }
}
