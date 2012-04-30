/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * This is the base class for the StateHistorySystem. It contains all the
 * current-state-updating methods.
 * 
 * It's not abstract, as it can be used by itself: in this case, no History tree
 * will be built underneath (no information will be saved to disk) and it will
 * only be able to respond to queries to the current, latest time.
 * 
 * (See IStateSystemQuerier and IStateSystemBuilder for the Javadoc.)
 * 
 * @author alexmont
 * 
 */
public class StateSystem {

    /* References to the inner structures */
    protected AttributeTree attributeTree;
    protected TransientState transState;

    /**
     * Constructor. No configuration needed!
     */
    public StateSystem() {
        attributeTree = new AttributeTree(this);

        /* This will tell the builder to discard the intervals */
        transState = new TransientState(null);
    }

    /**
     * @name Quark-retrieving methods
     */

    public int getQuarkAbsolute(String... attribute)
            throws AttributeNotFoundException {
        return attributeTree.getQuarkDontAdd(-1, attribute);
    }

    public int getQuarkAbsoluteAndAdd(String... attribute) {
        return attributeTree.getQuarkAndAdd(-1, attribute);
    }

    public int getQuarkRelative(int startingNodeQuark, String... subPath)
            throws AttributeNotFoundException {
        return attributeTree.getQuarkDontAdd(startingNodeQuark, subPath);
    }

    public int getQuarkRelativeAndAdd(int startingNodeQuark, String... subPath) {
        return attributeTree.getQuarkAndAdd(startingNodeQuark, subPath);
    }

    public List<Integer> getSubAttributes(int quark, boolean recursive)
            throws AttributeNotFoundException {
        return attributeTree.getSubAttributes(quark, recursive);
    }

    public List<Integer> getQuarks(String... pattern) {
        List<Integer> quarks = new LinkedList<Integer>();
        List<String> prefix = new LinkedList<String>();
        List<String> suffix = new LinkedList<String>();
        boolean split = false;
        String[] prefixStr;
        String[] suffixStr;
        List<Integer> directChildren;
        int startingAttribute;

        /* Fill the "prefix" and "suffix" parts of the pattern around the '*' */
        for (String entry : pattern) {
            if (entry.equals("*")) { //$NON-NLS-1$
                if (split) {
                    /*
                     * Split was already true? This means there was more than
                     * one wildcard. This is not supported, return an empty
                     * list.
                     */
                    return quarks;
                }
                split = true;
                continue;
            }

            if (split) {
                suffix.add(entry);
            } else {
                prefix.add(entry);
            }
        }
        prefixStr = prefix.toArray(new String[prefix.size()]);
        suffixStr = suffix.toArray(new String[suffix.size()]);

        /*
         * If there was no wildcard, we'll only return the one matching
         * attribute, if there is one.
         */
        if (split == false) {
            int quark;
            try {
                quark = getQuarkAbsolute(prefixStr);
            } catch (AttributeNotFoundException e) {
                /* It's fine, we'll just return the empty List */
                return quarks;
            }
            quarks.add(quark);
            return quarks;
        }

        try {
            if (prefix.size() == 0) {
                /*
                 * If 'prefix' is empty, this means the wildcard was the first
                 * element. Look for the root node's sub-attributes.
                 */
                startingAttribute = -1;
            } else {
                startingAttribute = getQuarkAbsolute(prefixStr);
            }
            directChildren = attributeTree.getSubAttributes(startingAttribute,
                    false);
        } catch (AttributeNotFoundException e) {
            /* That attribute path did not exist, return the empty array */
            return quarks;
        }

        /*
         * Iterate of all the sub-attributes, and only keep those who match the
         * 'suffix' part of the initial pattern.
         */
        for (int childQuark : directChildren) {
            int matchingQuark;
            try {
                matchingQuark = getQuarkRelative(childQuark, suffixStr);
            } catch (AttributeNotFoundException e) {
                continue;
            }
            quarks.add(matchingQuark);
        }

        return quarks;
    }

    /**
     * @name External methods related to insertions in the history -
     */

    public void modifyAttribute(long t, ITmfStateValue value, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException,
            StateValueTypeException {
        transState.processStateChange(t, value, attributeQuark);
    }

    public void incrementAttribute(long t, int attributeQuark)
            throws StateValueTypeException, TimeRangeException,
            AttributeNotFoundException {
        int prevValue = queryOngoingState(attributeQuark).unboxInt();
        /* prevValue should be == 0 if the attribute wasn't existing before */
        modifyAttribute(t, TmfStateValue.newValueInt(prevValue + 1),
                attributeQuark);
    }

    public void pushAttribute(long t, ITmfStateValue value, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException,
            StateValueTypeException {
        Integer stackDepth = 0;
        int subAttributeQuark;
        ITmfStateValue previousSV = transState.getOngoingStateValue(attributeQuark);

        if (previousSV.isNull()) {
            /*
             * If the StateValue was null, this means this is the first time we
             * use this attribute. Leave stackDepth at 0.
             */
        } else if (previousSV.getType() == 0) {
            /* Previous value was an integer, all is good, use it */
            stackDepth = previousSV.unboxInt();
        } else {
            /* Previous state of this attribute was another type? Not good! */
            throw new StateValueTypeException();
        }

        if (stackDepth >= 10) {
            /*
             * Limit stackDepth to 10, to avoid having Attribute Trees grow out
             * of control due to buggy insertions
             */
            String message = "Stack limit reached, not pushing"; //$NON-NLS-1$
            throw new AttributeNotFoundException(message);
        }

        stackDepth++;
        subAttributeQuark = getQuarkRelativeAndAdd(attributeQuark,
                stackDepth.toString());

        modifyAttribute(t, TmfStateValue.newValueInt(stackDepth),
                attributeQuark);
        modifyAttribute(t, value, subAttributeQuark);
    }

    public void popAttribute(long t, int attributeQuark)
            throws AttributeNotFoundException, TimeRangeException,
            StateValueTypeException {
        Integer stackDepth;
        int subAttributeQuark;
        ITmfStateValue previousSV = transState.getOngoingStateValue(attributeQuark);

        if (previousSV.isNull()) {
            /* Same as if stackDepth == 0, see below */
            return;
        }
        if (previousSV.getType() != 0) {
            /*
             * The existing value was a string, this doesn't look like a valid
             * stack attribute.
             */
            throw new StateValueTypeException();
        }

        stackDepth = previousSV.unboxInt();

        if (stackDepth == 0) {
            /*
             * Trying to pop an empty stack. This often happens at the start of
             * traces, for example when we see a syscall_exit, without having
             * the corresponding syscall_entry in the trace. Just ignore
             * silently.
             */
            return;
        }

        if (stackDepth < 0) {
            /* This on the other hand should not happen... */
            String message = "A top-level stack attribute " + //$NON-NLS-1$
                    "cannot have a negative integer value."; //$NON-NLS-1$
            throw new StateValueTypeException(message);
        }

        /* The attribute should already exist... */
        subAttributeQuark = getQuarkRelative(attributeQuark,
                stackDepth.toString());

        stackDepth--;
        modifyAttribute(t, TmfStateValue.newValueInt(stackDepth),
                attributeQuark);
        removeAttribute(t, subAttributeQuark);
    }

    public void removeAttribute(long t, int attributeQuark)
            throws TimeRangeException, AttributeNotFoundException {
        assert (attributeQuark >= 0);
        List<Integer> childAttributes;

        /*
         * "Nullify our children first, recursively. We pass 'false' because we
         * handle the recursion ourselves.
         */
        childAttributes = attributeTree.getSubAttributes(attributeQuark, false);
        for (Integer childNodeQuark : childAttributes) {
            assert (attributeQuark != childNodeQuark);
            removeAttribute(t, childNodeQuark);
        }
        /* Nullify ourselves */
        try {
            transState.processStateChange(t, TmfStateValue.nullValue(),
                    attributeQuark);
        } catch (StateValueTypeException e) {
            /*
             * Will not happen since we're inserting null values only, but poor
             * compiler has no way of knowing this...
             */
            e.printStackTrace();
        }
    }

    /**
     * @name "Current" query/update methods -
     */

    public ITmfStateValue queryOngoingState(int attributeQuark)
            throws AttributeNotFoundException {
        return transState.getOngoingStateValue(attributeQuark);
    }

    public void updateOngoingState(ITmfStateValue newValue, int attributeQuark)
            throws AttributeNotFoundException {
        transState.changeOngoingStateValue(attributeQuark, newValue);
    }

    public String getAttributeName(int attributeQuark) {
        return attributeTree.getAttributeName(attributeQuark);
    }

    public String getFullAttributePath(int attributeQuark) {
        return attributeTree.getFullAttributeName(attributeQuark);
    }

    /**
     * Print out the contents of the inner structures.
     * 
     * @param writer
     *            The PrintWriter in which to print the output
     */
    public void debugPrint(PrintWriter writer) {
        attributeTree.debugPrint(writer);
        transState.debugPrint(writer);
    }

}