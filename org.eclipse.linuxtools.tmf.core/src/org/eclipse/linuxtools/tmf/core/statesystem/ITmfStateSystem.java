/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * This is the read-only interface to the generic state system. It contains all
 * the read-only quark-getting methods, as well as the history-querying ones.
 *
 * @author Alexandre Montplaisir
 * @version 2.0
 * @since 2.0
 */
public interface ITmfStateSystem {

    /**
     * Get the ID of this state system.
     *
     * @return The state system's ID
     * @since 3.0
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
     * @since 3.0
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
     *
     * @since 3.0
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
     * @since 3.0
     */
    boolean waitUntilBuilt(long timeout);

    /**
     * Notify the state system that the trace is being closed, so it should
     * clean up, close its files, etc.
     */
    void dispose();

    /**
     * Return the current total amount of attributes in the system. This is also
     * equal to the quark that will be assigned to the next attribute that's
     * created.
     *
     * @return The current number of attributes in the system
     */
    int getNbAttributes();

    /**
     * @name Read-only quark-getting methods
     */

    /**
     * Basic quark-retrieving method. Pass an attribute in parameter as an array
     * of strings, the matching quark will be returned.
     *
     * This version will NOT create any new attributes. If an invalid attribute
     * is requested, an exception will be thrown.
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
     * "Relative path" quark-getting method. Instead of specifying a full path,
     * if you know the path is relative to another attribute for which you
     * already have the quark, use this for better performance.
     *
     * This is useful for cases where a lot of modifications or queries will
     * originate from the same branch of the attribute tree : the common part of
     * the path won't have to be re-hashed for every access.
     *
     * This version will NOT create any new attributes. If an invalid attribute
     * is requested, an exception will be thrown.
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which 'subPath' originates.
     * @param subPath
     *            "Rest" of the path to get to the final attribute
     * @return The matching quark, if it existed
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    int getQuarkRelative(int startingNodeQuark, String... subPath)
            throws AttributeNotFoundException;

    /**
     * Return the sub-attributes of the target attribute, as a List of quarks.
     *
     * @param quark
     *            The attribute of which you want to sub-attributes. You can use
     *            "-1" here to specify the root node.
     * @param recursive
     *            True if you want all recursive sub-attributes, false if you
     *            only want the first level.
     * @return A List of integers, matching the quarks of the sub-attributes.
     * @throws AttributeNotFoundException
     *             If the quark was not existing or invalid.
     */
    List<Integer> getSubAttributes(int quark, boolean recursive)
            throws AttributeNotFoundException;

    /**
     * Return the sub-attributes of the target attribute, as a List of quarks,
     * similarly to {@link #getSubAttributes(int, boolean)}, but with an added
     * regex pattern to filter on the return attributes.
     *
     * @param quark
     *            The attribute of which you want to sub-attributes. You can use
     *            "-1" here to specify the root node.
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
     * @since 3.0
     */
    List<Integer> getSubAttributes(int quark, boolean recursive, String pattern)
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
    List<Integer> getQuarks(String... pattern);

    /**
     * Return the name assigned to this quark. This returns only the "basename",
     * not the complete path to this attribute.
     *
     * @param attributeQuark
     *            The quark for which we want the name
     * @return The name of the quark
     */
    String getAttributeName(int attributeQuark);

    /**
     * This returns the slash-separated path of an attribute by providing its
     * quark
     *
     * @param attributeQuark
     *            The quark of the attribute we want
     * @return One single string separated with '/', like a filesystem path
     */
    String getFullAttributePath(int attributeQuark);

    /**
     * @name Query methods
     */

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
    ITmfStateValue queryOngoingState(int attributeQuark)
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
    List<ITmfStateInterval> queryFullState(long t)
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
    ITmfStateInterval querySingleState(long t, int attributeQuark)
            throws AttributeNotFoundException, StateSystemDisposedException;

    /**
     * Convenience method to query attribute stacks (created with
     * pushAttribute()/popAttribute()). This will return the interval that is
     * currently at the top of the stack, or 'null' if that stack is currently
     * empty. It works similarly to querySingleState().
     *
     * To retrieve the other values in a stack, you can query the sub-attributes
     * manually.
     *
     * @param t
     *            The timestamp of the query
     * @param stackAttributeQuark
     *            The top-level stack-attribute (that was the target of
     *            pushAttribute() at creation time)
     * @return The interval that was at the top of the stack, or 'null' if the
     *         stack was empty.
     * @throws StateValueTypeException
     *             If the target attribute is not a valid stack attribute (if it
     *             has a string value for example)
     * @throws AttributeNotFoundException
     *             If the attribute was simply not found
     * @throws TimeRangeException
     *             If the given timestamp is invalid
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     * @since 2.0
     */
    ITmfStateInterval querySingleStackTop(long t, int stackAttributeQuark)
            throws  AttributeNotFoundException, StateSystemDisposedException;

    /**
     * Return a list of state intervals, containing the "history" of a given
     * attribute between timestamps t1 and t2. The list will be ordered by
     * ascending time.
     *
     * Note that contrary to queryFullState(), the returned list here is in the
     * "direction" of time (and not in the direction of attributes, as is the
     * case with queryFullState()).
     *
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            Target end time of the query. If t2 is greater than the end of
     *            the trace, we will return what we have up to the end of the
     *            history.
     * @return The List of state intervals that happened between t1 and t2
     * @throws TimeRangeException
     *             If t1 is invalid, or if t2 <= t1
     * @throws AttributeNotFoundException
     *             If the requested quark does not exist in the model.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    List<ITmfStateInterval> queryHistoryRange(int attributeQuark, long t1, long t2)
            throws AttributeNotFoundException, StateSystemDisposedException;

    /**
     * Return the state history of a given attribute, but with at most one
     * update per "resolution". This can be useful for populating views (where
     * it's useless to have more than one query per pixel, for example). A
     * progress monitor can be used to cancel the query before completion.
     *
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            Target end time of the query. If t2 is greater than the end of
     *            the trace, we will return what we have up to the end of the
     *            history.
     * @param resolution
     *            The "step" of this query
     * @param monitor
     *            A progress monitor. If the monitor is canceled during a query,
     *            we will return what has been found up to that point. You can
     *            use "null" if you do not want to use one.
     * @return The List of states that happened between t1 and t2
     * @throws TimeRangeException
     *             If t1 is invalid, if t2 <= t1, or if the resolution isn't
     *             greater than zero.
     * @throws AttributeNotFoundException
     *             If the attribute doesn't exist
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     * @since 2.0
     */
    List<ITmfStateInterval> queryHistoryRange(int attributeQuark,
            long t1, long t2, long resolution, IProgressMonitor monitor)
            throws AttributeNotFoundException, StateSystemDisposedException;
}
