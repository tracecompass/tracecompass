/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson and others.
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

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;


/**
 * This is the read-only interface to the generic state system. It contains all
 * the read-only quark-getting methods, as well as the history-querying ones.
 *
 * @author Alexandre Montplaisir
 * @noimplement Only the internal StateSystem class should implement this
 *              interface.
 */
public interface ITmfStateSystem {

    /** Root attribute quark
     * @since 2.0*/
    int ROOT_ATTRIBUTE = -1;
    /** Invalid attribute quark
     * @since 2.0*/
    int INVALID_ATTRIBUTE = -2;

    /**
     * Get the ID of this state system.
     *
     * @return The state system's ID
     */
    String getSSID();

    /**
     * Return the start time of this history. It usually matches the start time
     * of the original trace.
     *
     * @return The history's registered start time
     */
    long getStartTime();

    /**
     * Return the current end time of the history.
     *
     * @return The current end time of this state history
     */
    long getCurrentEndTime();

    /**
     * Check if the construction of this state system was cancelled or not. If
     * false is returned, it can mean that the building was finished
     * successfully, or that it is still ongoing. You can check independently
     * with {@link #waitUntilBuilt()} if it is finished or not.
     *
     * @return If the construction was cancelled or not. In true is returned, no
     *         queries should be run afterwards.
     */
    boolean isCancelled();

    /**
     * While it's possible to query a state history that is being built,
     * sometimes we might want to wait until the construction is finished before
     * we start doing queries.
     *
     * This method blocks the calling thread until the history back-end is done
     * building. If it's already built (ie, opening a pre-existing file) this
     * should return immediately.
     *
     * You should always check with {@link #isCancelled()} if it is safe to
     * query this state system before doing queries.
     */
    void waitUntilBuilt();

    /**
     * Wait until the state system construction is finished. Similar to
     * {@link #waitUntilBuilt()}, but we also specify a timeout. If the timeout
     * elapses before the construction is finished, the method will return.
     *
     * The return value determines if the return was due to the construction
     * finishing (true), or the timeout elapsing (false).
     *
     * This can be useful, for example, for a component doing queries
     * periodically to the system while it is being built.
     *
     * @param timeout
     *            Timeout value in milliseconds
     * @return True if the return was due to the construction finishing, false
     *         if it was because the timeout elapsed. Same logic as
     *         {@link java.util.concurrent.CountDownLatch#await(long, java.util.concurrent.TimeUnit)}
     */
    boolean waitUntilBuilt(long timeout);

    /**
     * Notify the state system that the trace is being closed, so it should
     * clean up, close its files, etc.
     */
    void dispose();

    // ------------------------------------------------------------------------
    // Read-only quark-getting methods
    // ------------------------------------------------------------------------

    /**
     * Return the current total amount of attributes in the system. This is also
     * equal to the quark that will be assigned to the next attribute that's
     * created.
     *
     * @return The current number of attributes in the system
     */
    int getNbAttributes();

    /**
     * Basic quark-retrieving method. Pass an attribute in parameter as an array
     * of strings, the matching quark will be returned.
     * <p>
     * This version will NOT create any new attributes. If an invalid attribute
     * is requested, an exception will be thrown.
     * <p>
     * If it is expected that the requested attribute might be absent, it is
     * recommended to use {@link #optQuarkAbsolute(String...)} instead.
     *
     * @param attribute
     *            Attribute given as its full path in the Attribute Tree
     * @return The quark of the requested attribute, if it existed.
     * @throws AttributeNotFoundException
     *             This exception is thrown if the requested attribute simply
     *             did not exist in the system.
     */
    int getQuarkAbsolute(String... attribute)
            throws AttributeNotFoundException;

    /**
     * Quark-retrieving method for an optional attribute that may or may not be
     * present. Pass an attribute in parameter as an array of strings, if it
     * exists, the matching quark will be returned.
     * <p>
     * This version will NOT create any new attributes. If an attribute that
     * does not exist is requested, {@link #INVALID_ATTRIBUTE} will be returned.
     *
     * @param attribute
     *            Attribute given as its full path in the Attribute Tree
     * @return The quark of the requested attribute, or
     *         {@link #INVALID_ATTRIBUTE} if it does not exist.
     * @since 2.0
     */
    int optQuarkAbsolute(String... attribute);

    /**
     * "Relative path" quark-getting method. Instead of specifying a full path,
     * if you know the path is relative to another attribute for which you
     * already have the quark, use this for better performance.
     * <p>
     * This is useful for cases where a lot of modifications or queries will
     * originate from the same branch of the attribute tree : the common part of
     * the path won't have to be re-hashed for every access.
     * <p>
     * This version will NOT create any new attributes. If an invalid attribute
     * is requested, an exception will be thrown.
     * <p>
     * If it is expected that the requested sub-attribute might be absent, it is
     * recommended to use {@link #optQuarkRelative(int, String...)} instead.
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which 'subPath' originates.
     * @param subPath
     *            "Rest" of the path to get to the final attribute
     * @return The matching quark, if it existed
     * @throws IndexOutOfBoundsException
     *             If the starting node quark is out of range
     * @throws AttributeNotFoundException
     *             If the sub-attribute does not exist
     */
    int getQuarkRelative(int startingNodeQuark, String... subPath)
            throws AttributeNotFoundException;

    /**
     * "Relative path" quark-getting method for an optional attribute that may
     * or may not be present. Instead of specifying a full path, if you know the
     * path is relative to another attribute for which you already have the
     * quark, use this for better performance.
     * <p>
     * This is useful for cases where a lot of modifications or queries will
     * originate from the same branch of the attribute tree : the common part of
     * the path won't have to be re-hashed for every access.
     * <p>
     * This version will NOT create any new attributes. If a sub-attribute that
     * does not exist is requested, {@link #INVALID_ATTRIBUTE} will be returned.
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which 'subPath' originates.
     * @param subPath
     *            "Rest" of the path to get to the final attribute
     * @return The quark of the requested sub-attribute, or
     *         {@link #INVALID_ATTRIBUTE} if it does not exist.
     * @throws IndexOutOfBoundsException
     *             If the starting node quark is out of range
     * @since 2.0
     */
    int optQuarkRelative(int startingNodeQuark, String... subPath);

    /**
     * Return the sub-attributes of the target attribute, as a List of quarks.
     *
     * @param quark
     *            The attribute of which you want to sub-attributes. You can use
     *            {@link #ROOT_ATTRIBUTE} here to specify the root node.
     * @param recursive
     *            True if you want all recursive sub-attributes, false if you
     *            only want the first level.
     * @return A List of integers, matching the quarks of the sub-attributes.
     * @throws AttributeNotFoundException
     *             If the quark was not existing or invalid.
     */
    @NonNull List<@NonNull Integer> getSubAttributes(int quark, boolean recursive)
            throws AttributeNotFoundException;

    /**
     * Return the sub-attributes of the target attribute, as a List of quarks,
     * similarly to {@link #getSubAttributes(int, boolean)}, but with an added
     * regex pattern to filter on the return attributes.
     *
     * @param quark
     *            The attribute of which you want to sub-attributes. You can use
     *            {@link #ROOT_ATTRIBUTE} here to specify the root node.
     * @param recursive
     *            True if you want all recursive sub-attributes, false if you
     *            only want the first level. Note that the returned value will
     *            be flattened.
     * @param pattern
     *            The regular expression to match the attribute base name.
     * @return A List of integers, matching the quarks of the sub-attributes
     *         that match the regex. An empty list is returned if there is no
     *         matching attribute.
     * @throws AttributeNotFoundException
     *             If the 'quark' was not existing or invalid.
     */
    @NonNull List<@NonNull Integer> getSubAttributes(int quark, boolean recursive, String pattern)
            throws AttributeNotFoundException;

    /**
     * Batch quark-retrieving method. This method allows you to specify a path
     * pattern which includes a wildcard "*" somewhere. It will check all the
     * existing attributes in the attribute tree and return those who match the
     * pattern.
     *
     * For example, passing ("Threads", "*", "Exec_mode") will return the list
     * of quarks for attributes "Threads/1000/Exec_mode",
     * "Threads/1500/Exec_mode", and so on, depending on what exists at this
     * time in the attribute tree.
     *
     * If no wildcard is specified, the behavior is the same as
     * getQuarkAbsolute() (except it will return a List with one entry). This
     * method will never create new attributes.
     *
     * Only one wildcard "*" is supported at this time.
     *
     * @param pattern
     *            The array of strings representing the pattern to look for. It
     *            should ideally contain one entry that is only a "*".
     * @return A List of attribute quarks, representing attributes that matched
     *         the pattern. If no attribute matched, the list will be empty (but
     *         not null).
     */
    @NonNull List<@NonNull Integer> getQuarks(String... pattern);

    /**
     * Return the name assigned to this quark. This returns only the "basename",
     * not the complete path to this attribute.
     *
     * @param attributeQuark
     *            The quark for which we want the name
     * @return The name of the quark
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    @NonNull String getAttributeName(int attributeQuark);

    /**
     * This returns the slash-separated path of an attribute by providing its
     * quark
     *
     * @param attributeQuark
     *            The quark of the attribute we want
     * @return One single string separated with '/', like a filesystem path
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    @NonNull String getFullAttributePath(int attributeQuark);

    /**
     * Return the full attribute path, as an array of strings representing each
     * element.
     *
     * @param attributeQuark
     *            The quark of the attribute we want.
     * @return The array of path elements
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     * @since 1.0
     */
    String @NonNull [] getFullAttributePathArray(int attributeQuark);

    /**
     * Returns the parent quark of the attribute.
     *
     * @param attributeQuark
     *            The quark of the attribute
     * @return Quark of the parent attribute or {@link #ROOT_ATTRIBUTE} if root
     *         quark or no parent.
     * @throws IndexOutOfBoundsException
     *             If the attribute quark is out of range
     */
    int getParentAttributeQuark(int attributeQuark);

    // ------------------------------------------------------------------------
    // Query methods
    // ------------------------------------------------------------------------

    /**
     * Returns the current state value we have (in the Transient State) for the
     * given attribute.
     *
     * This is useful even for a StateHistorySystem, as we are guaranteed it
     * will only do a memory access and not go look on disk (and we don't even
     * have to provide a timestamp!)
     *
     * @param attributeQuark
     *            For which attribute we want the current state
     * @return The State value that's "current" for this attribute
     * @throws AttributeNotFoundException
     *             If the requested attribute is invalid
     */
    @NonNull ITmfStateValue queryOngoingState(int attributeQuark)
            throws AttributeNotFoundException;

    /**
     * Get the start time of the current ongoing state, for the specified
     * attribute.
     *
     * @param attribute
     *            Quark of the attribute
     * @return The current start time of the ongoing state
     * @throws AttributeNotFoundException
     *             If the attribute is invalid
     */
    long getOngoingStartTime(int attribute)
            throws AttributeNotFoundException;

    /**
     * Load the complete state information at time 't' into the returned List.
     * You can then get the intervals for single attributes by using
     * List.get(n), where 'n' is the quark of the attribute.
     *
     * On average if you need around 10 or more queries for the same timestamps,
     * use this method. If you need less than 10 (for example, running many
     * queries for the same attributes but at different timestamps), you might
     * be better using the querySingleState() methods instead.
     *
     * @param t
     *            We will recreate the state information to what it was at time
     *            t.
     * @return The List of intervals, where the offset = the quark
     * @throws TimeRangeException
     *             If the 't' parameter is outside of the range of the state
     *             history.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    @NonNull List<@NonNull ITmfStateInterval> queryFullState(long t)
            throws StateSystemDisposedException;

    /**
     * Singular query method. This one does not update the whole stateInfo
     * vector, like queryFullState() does. It only searches for one specific
     * entry in the state history.
     *
     * It should be used when you only want very few entries, instead of the
     * whole state (or many entries, but all at different timestamps). If you do
     * request many entries all at the same time, you should use the
     * conventional queryFullState() + List.get() method.
     *
     * @param t
     *            The timestamp at which we want the state
     * @param attributeQuark
     *            Which attribute we want to get the state of
     * @return The StateInterval representing the state
     * @throws TimeRangeException
     *             If 't' is invalid
     * @throws AttributeNotFoundException
     *             If the requested quark does not exist in the model
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    @NonNull ITmfStateInterval querySingleState(long t, int attributeQuark)
            throws AttributeNotFoundException, StateSystemDisposedException;
}