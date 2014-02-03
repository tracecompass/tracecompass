/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * This is the external interface to build or modify an existing state history.
 *
 * It extends ITmfStateSystem, so you can still use it for reading the history,
 * but it also provides write-access to it with the quark-creating and
 * state-change insertion methods.
 *
 * This should only be used by classes that need to build or modify the state
 * history. Views, etc. (who will only be reading from it) should use the
 * ITmfStateSystem interface instead.
 *
 * @author Alexandre Montplaisir
 * @version 2.0
 * @since 2.0
 */
public interface ITmfStateSystemBuilder extends ITmfStateSystem {

    /**
     * @name Read/write quark-getting methods
     */

    /**
     * Basic quark-retrieving method. Pass an attribute in parameter as an array
     * of strings, the matching quark will be returned.
     *
     * This version WILL create new attributes: if the attribute passed in
     * parameter is new in the system, it will be added and its new quark will
     * be returned.
     *
     * @param attribute
     *            Attribute given as its full path in the Attribute Tree
     * @return The quark of the attribute (which either existed or just got
     *         created)
     */
    int getQuarkAbsoluteAndAdd(String... attribute);

    /**
     * "Relative path" quark-getting method. Instead of specifying a full path,
     * if you know the path is relative to another attribute for which you
     * already have the quark, use this for better performance.
     *
     * This is useful for cases where a lot of modifications or queries will
     * originate from the same branch of the attribute tree : the common part of
     * the path won't have to be re-hashed for every access.
     *
     * This version WILL create new attributes: if the attribute passed in
     * parameter is new in the system, it will be added and its new quark will
     * be returned.
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which 'subPath' originates.
     * @param subPath
     *            "Rest" of the path to get to the final attribute
     * @return The matching quark, either if it's new of just got created.
     */
    int getQuarkRelativeAndAdd(int startingNodeQuark, String... subPath);

    /**
     * @name State-changing methods
     */

    /**
     * Modify a current "ongoing" state (instead of inserting a state change,
     * like modifyAttribute() and others).
     *
     * This can be used to update the value of a previous state change, for
     * example when we get information at the end of the state and not at the
     * beginning. (return values of system calls, etc.)
     *
     * Note that past states can only be modified while they are still in
     * memory, so only the "current state" can be updated. Once they get
     * committed to disk (by inserting a new state change) it becomes too late.
     *
     * @param newValue
     *            The new value that will overwrite the "current" one.
     * @param attributeQuark
     *            For which attribute in the system
     * @throws AttributeNotFoundException
     *             If the requested attribute is invalid
     */
    void updateOngoingState(ITmfStateValue newValue, int attributeQuark)
            throws AttributeNotFoundException;

    /**
     * Basic attribute modification method, we simply specify a new value, for a
     * given attribute, effective at the given timestamp.
     *
     * @param t
     *            Timestamp of the state change
     * @param value
     *            The State Value we want to assign to the attribute
     * @param attributeQuark
     *            Integer value of the quark corresponding to the attribute we
     *            want to modify
     * @throws TimeRangeException
     *             If the requested time is outside of the trace's range
     * @throws AttributeNotFoundException
     *             If the requested attribute quark is invalid
     * @throws StateValueTypeException
     *             If the inserted state value's type does not match what is
     *             already assigned to this attribute.
     */
    void modifyAttribute(long t, ITmfStateValue value, int attributeQuark)
            throws AttributeNotFoundException, StateValueTypeException;

    /**
     * Increment attribute method. Reads the current value of a given integer
     * attribute (this value is right now in the Transient State), and increment
     * it by 1. Useful for statistics.
     *
     * @param t
     *            Timestamp of the state change
     * @param attributeQuark
     *            Attribute to increment. If it doesn't exist it will be added,
     *            with a new value of 1.
     * @throws StateValueTypeException
     *             If the attribute already exists but is not of type Integer
     * @throws TimeRangeException
     *             If the given timestamp is invalid
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    void incrementAttribute(long t, int attributeQuark)
            throws AttributeNotFoundException, StateValueTypeException;

    /**
     * "Push" helper method. This uses the given integer attribute as a stack:
     * The value of that attribute will represent the stack depth (always >= 1).
     * Sub-attributes will be created, their base-name will be the position in
     * the stack (1, 2, etc.) and their value will be the state value 'value'
     * that was pushed to this position.
     *
     * @param t
     *            Timestamp of the state change
     * @param value
     *            State value to assign to this stack position.
     * @param attributeQuark
     *            The base attribute to use as a stack. If it does not exist if
     *            will be created (with depth = 1)
     * @throws TimeRangeException
     *             If the requested timestamp is invalid
     * @throws AttributeNotFoundException
     *             If the attribute is invalid
     * @throws StateValueTypeException
     *             If the attribute 'attributeQuark' already exists, but is not
     *             of integer type.
     */
    void pushAttribute(long t, ITmfStateValue value, int attributeQuark)
            throws AttributeNotFoundException, StateValueTypeException;

    /**
     * Antagonist of the pushAttribute(), pops the top-most attribute on the
     * stack-attribute. If this brings it back to depth = 0, the attribute is
     * kept with depth = 0. If the value is already 0, or if the attribute
     * doesn't exist, nothing is done.
     *
     * @param t
     *            Timestamp of the state change
     * @param attributeQuark
     *            Quark of the stack-attribute to pop
     * @return The state value that was popped, or 'null' if nothing was
     *         actually removed from the stack.
     * @throws AttributeNotFoundException
     *             If the attribute is invalid
     * @throws TimeRangeException
     *             If the timestamp is invalid
     * @throws StateValueTypeException
     *             If the target attribute already exists, but its state value
     *             type is invalid (not an integer)
     * @since 2.0
     */
    ITmfStateValue popAttribute(long t, int attributeQuark)
            throws AttributeNotFoundException, StateValueTypeException;

    /**
     * Remove attribute method. Similar to the above modify- methods, with value
     * = 0 / null, except we will also "nullify" all the sub-contents of the
     * requested path (a bit like "rm -rf")
     *
     * @param t
     *            Timestamp of the state change
     * @param attributeQuark
     *            Attribute to remove
     * @throws TimeRangeException
     *             If the timestamp is invalid
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    void removeAttribute(long t, int attributeQuark)
            throws AttributeNotFoundException;

    /**
     * Method to close off the History Provider. This happens for example when
     * we are done reading an off-line trace. First we close the TransientState,
     * commit it to the Provider, mark it as inactive, then we write the
     * Attribute Tree somewhere so we can reopen it later.
     *
     * @param endTime
     *            The requested End Time of the history, since it could be
     *            bigger than the timestamp of the last event or state change we
     *            have seen. All "ongoing" states will be extended until this
     *            'endTime'.
     * @throws TimeRangeException
     *             If the passed endTime doesn't make sense (for example, if
     *             it's earlier than the latest time) and the backend doesn't
     *             know how to handle it.
     */
    void closeHistory(long endTime);
}
