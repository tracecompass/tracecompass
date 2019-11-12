/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.tracecompass.statesystem.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

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
 * @noimplement Only the internal StateSystem class should implement this
 *              interface.
 * @since 3.0
 */
public interface ITmfStateSystemBuilder extends ITmfStateSystem {

    /**
     * Special state provider version number that will tell the backend to
     * ignore the version check and open an existing file even if the versions
     * don't match.
     */
    int IGNORE_PROVIDER_VERSION = -42;

    /**
     * @name Read/write quark-getting methods
     */

    /**
     * Basic quark-retrieving method. Pass an attribute in parameter as an array
     * of strings, the matching quark will be returned.
     * <p>
     * This version WILL create new attributes: if the attribute passed in
     * parameter is new in the system, it will be added and its new quark will
     * be returned.
     * </p>
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
     * <p>
     * This is useful for cases where a lot of modifications or queries will
     * originate from the same branch of the attribute tree : the common part of
     * the path won't have to be re-hashed for every access.
     * </p>
     * <p>
     * This version WILL create new attributes: if the attribute passed in
     * parameter is new in the system, it will be added and its new quark will
     * be returned.
     * </p>
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which 'subPath' originates.
     * @param subPath
     *            "Rest" of the path to get to the final attribute
     * @return The matching quark, either if it's new of just got created.
     * @throws IndexOutOfBoundsException
     *             If the starting node quark is out of range
     */
    int getQuarkRelativeAndAdd(int startingNodeQuark, String... subPath);

    /**
     * @name State-changing methods
     */

    /**
     * Modify a current "ongoing" state (instead of inserting a state change,
     * like modifyAttribute() and others).
     * <p>
     * This can be used to update the value of a previous state change, for
     * example when we get information at the end of the state and not at the
     * beginning. (return values of system calls, etc.)
     * </p>
     * <p>
     * Note that past states can only be modified while they are still in
     * memory, so only the "current state" can be updated. Once they get
     * committed to disk (by inserting a new state change) it becomes too late.
     * </p>
     *
     * @param newValue
     *            The new value that will overwrite the "current" one.
     * @param attributeQuark
     *            For which attribute in the system
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    void updateOngoingState(@NonNull ITmfStateValue newValue, int attributeQuark);

    /**
     * Modify a current "ongoing" state (instead of inserting a state change,
     * like modifyAttribute() and others).
     * <p>
     * This can be used to update the value of a previous state change, for
     * example when we get information at the end of the state and not at the
     * beginning. (return values of system calls, etc.)
     * </p>
     * <p>
     * Note that past states can only be modified while they are still in
     * memory, so only the "current state" can be updated. Once they get
     * committed to disk (by inserting a new state change) it becomes too late.
     * </p>
     *
     * @param newValue
     *            The new value that will overwrite the "current" one.
     * @param attributeQuark
     *            For which attribute in the system
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     * @since 4.0
     */
    default void updateOngoingState(@Nullable Object newValue, int attributeQuark) {
        updateOngoingState(TmfStateValue.newValue(newValue), attributeQuark);
    }

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
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     * @throws StateValueTypeException
     *             If the inserted state value's type does not match what is
     *             already assigned to this attribute.
     */
    @Deprecated
    default void modifyAttribute(long t, @NonNull ITmfStateValue value, int attributeQuark)
            throws StateValueTypeException {
        modifyAttribute(t, value.unboxValue(), attributeQuark);
    }

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
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     * @throws StateValueTypeException
     *             If the inserted state value's type does not match what is
     *             already assigned to this attribute.
     * @since 3.1
     */
    void modifyAttribute(long t, Object value, int attributeQuark)
            throws StateValueTypeException;

    /**
     * "Push" helper method. This uses the given integer attribute as a stack:
     * The value of that attribute will represent the stack depth (always
     * {@literal >=} 1). Sub-attributes will be created, their base-name will be
     * the position in the stack (1, 2, etc.) and their value will be the state
     * value 'value' that was pushed to this position.
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
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     * @throws StateValueTypeException
     *             If the attribute 'attributeQuark' already exists, but is not
     *             of integer type.
     */
    @Deprecated
    default void pushAttribute(long t, @NonNull ITmfStateValue value, int attributeQuark)
            throws StateValueTypeException {
        pushAttribute(t, value.unboxValue(), attributeQuark);
    }

    /**
     * "Push" helper method. This uses the given integer attribute as a stack:
     * The value of that attribute will represent the stack depth (always
     * {@literal >=} 1). Sub-attributes will be created, their base-name will be
     * the position in the stack (1, 2, etc.) and their value will be the state
     * value 'value' that was pushed to this position.
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
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     * @throws StateValueTypeException
     *             If the attribute 'attributeQuark' already exists, but is not
     *             of integer type.
     * @since 3.1
     */
    void pushAttribute(long t, Object value, int attributeQuark)
            throws StateValueTypeException;

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
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     * @throws TimeRangeException
     *             If the timestamp is invalid
     * @throws StateValueTypeException
     *             If the target attribute already exists, but its state value
     *             type is invalid (not an integer)
     */
    ITmfStateValue popAttribute(long t, int attributeQuark)
            throws StateValueTypeException;

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
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     * @throws TimeRangeException
     *             If the timestamp is invalid
     * @throws StateValueTypeException
     *             If the target attribute already exists, but its state value
     *             type is invalid (not an integer)
     * @since 3.1
     */
    default Object popAttributeObject(long t, int attributeQuark)
            throws StateValueTypeException {
        return popAttribute(t, attributeQuark).unboxValue();
    }

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
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    void removeAttribute(long t, int attributeQuark);

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

    /**
     * Delete any generated files or anything that might have been created by
     * the history backend (either temporary or save files). By calling this, we
     * return to the state as it was before ever building the history.
     * <p>
     * You might not want to call automatically if, for example, you want an
     * index file to persist on disk. This could be limited to actions
     * originating from the user.
     * </p>
     *
     * @since 2.1
     */
    void removeFiles();

    /**
     * Returns the current state values we have (in the Transient State) for all the
     * attributes.
     * <p>
     * This is useful even for a StateHistorySystem, as we are guaranteed it
     * will only do a memory access and not go look on disk (and we don't even
     * have to provide a timestamp!)
     * </p>
     *
     * @return the list of transient state values
     * @since 4.3
     */
    default List<@Nullable Object> queryOngoing() {
        List<@Nullable Object> retVal = new ArrayList<>();
        for (int attr = 0; attr < getNbAttributes(); attr++) {
            retVal.add(queryOngoing(attr));
        }
        return retVal;
    }
}
