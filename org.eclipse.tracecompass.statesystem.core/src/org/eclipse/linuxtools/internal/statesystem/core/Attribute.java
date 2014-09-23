/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.statesystem.core;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * An Attribute is a "node" in the Attribute Tree. It represents a smallest
 * unit of the model which can be in a particular state at a given time.
 *
 * It is abstract, as different implementations can provide different ways to
 * access sub-attributes
 *
 * @author alexmont
 *
 */
public final class Attribute {

    private final Attribute parent;
    private final String name;
    private final int quark;

    /** The sub-attributes (<basename, attribute>) of this attribute */
    private final Map<String, Attribute> subAttributes;

    /**
     * Constructor
     *
     * @param parent
     *            The parent attribute of this one. Can be 'null' to represent
     *            this attribute is the root node of the tree.
     * @param name
     *            Base name of this attribute
     * @param quark
     *            The integer representation of this attribute
     */
    public Attribute(Attribute parent, String name, int quark) {
        this.parent = parent;
        this.quark = quark;
        this.name = name;
        this.subAttributes = Collections.synchronizedMap(new LinkedHashMap<String, Attribute>());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get the quark (integer representation) of this attribute.
     *
     * @return The quark of this attribute
     */
    public int getQuark() {
        return quark;
    }

    /**
     * Get the name of this attribute.
     *
     * @return The name of this attribute
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of child attributes below this one.
     *
     * @return The child attributes.
     */
    public Iterable<Attribute> getSubAttributes() {
        return ImmutableList.copyOf(subAttributes.values());
    }

    /**
     * Get the matching quark for a given path-of-strings
     *
     * @param path
     *            The path we are looking for, *relative to this node*.
     * @return The matching quark, or -1 if that attribute does not exist.
     */
    public int getSubAttributeQuark(String... path) {
        return this.getSubAttributeQuark(path, 0);
    }

    /**
     * Other method to search through the attribute tree, but instead of
     * returning the matching quark we return the AttributeTreeNode object
     * itself. It can then be used as new "root node" for faster queries on the
     * tree.
     *
     * @param path
     *            The target path, *relative to this node*
     * @return The Node object matching the last element in the path, or "null"
     *         if that attribute does not exist.
     */
    public Attribute getSubAttributeNode(String... path) {
        return this.getSubAttributeNode(path, 0);
    }

    /**
     * "Inner" part of the previous public method, which is used recursively. To
     * avoid having to copy sub-arrays to pass down, we just track where we are
     * at with the index parameter. It uses getSubAttributeNode(), whose
     * implementation is left to the derived classes.
     */
    private int getSubAttributeQuark(String[] path, int index) {
        Attribute targetNode = this.getSubAttributeNode(path, index);
        if (targetNode == null) {
            return -1;
        }
        return targetNode.getQuark();
    }

    /**
     * Get the parent attribute of this attribute
     *
     * @return The parent attribute
     */
    public Attribute getParentAttribute() {
        return this.parent;
    }

    /**
     * Get the parent quark of this attribute
     *
     * @return The quark of the parent attribute
     */
    public int getParentAttributeQuark() {
        return this.parent.getQuark();
    }

    /* The methods how to access children are left to derived classes */

    /**
     * Add a sub-attribute to this attribute
     *
     * @param newSubAttribute The new attribute to add
     */
    public void addSubAttribute(Attribute newSubAttribute) {
        if (newSubAttribute == null || newSubAttribute.getName() == null) {
            throw new IllegalArgumentException();
        }
        subAttributes.put(newSubAttribute.getName(), newSubAttribute);
    }

    /**
     * Get a sub-attribute from this node's sub-attributes
     *
     * @param path
     *            The *full* path to the attribute
     * @param index
     *            The index in 'path' where this attribute is located
     *            (indicating where to start searching).
     * @return The requested attribute
     */
    private Attribute getSubAttributeNode(String[] path, int index) {
        final Attribute nextNode = subAttributes.get(path[index]);

        if (nextNode == null) {
            /* We don't have the expected child => the attribute does not exist */
            return null;
        }
        if (index == path.length - 1) {
            /* It's our job to process this request */
            return nextNode;
        }

        /* Pass on the rest of the path to the relevant child */
        return nextNode.getSubAttributeNode(path, index + 1);
    }

    /**
     * Return a String array composed of the full (absolute) path representing
     * this attribute
     *
     * @return
     */
    private String[] getFullAttribute() {
        LinkedList<String> list = new LinkedList<>();
        Attribute curNode = this;

        /* Add recursive parents to the list, but stop at the root node */
        while (curNode.parent != null) {
            list.addFirst(curNode.getName());
            curNode = curNode.parent;
        }

        return list.toArray(new String[0]);
    }

    /**
     * Return the absolute path of this attribute, as a single slash-separated
     * String.
     *
     * @return The full name of this attribute
     */
    public String getFullAttributeName() {
        String[] array = this.getFullAttribute();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < array.length - 1; i++) {
            buf.append(array[i]);
            buf.append('/');
        }
        buf.append(array[array.length - 1]);
        return buf.toString();
    }

    @Override
    public String toString() {
        return getFullAttributeName() + " (" + quark + ')'; //$NON-NLS-1$
    }

    private int curDepth;

    private void attributeNodeToString(PrintWriter writer, Attribute currentNode) {
        writer.println(currentNode.getName() + " (" + currentNode.quark + ')'); //$NON-NLS-1$
        curDepth++;

        for (Attribute nextNode : currentNode.getSubAttributes()) {
            /* Skip printing 'null' entries */
            if (nextNode == null) {
                continue;
            }
            for (int j = 0; j < curDepth - 1; j++) {
                writer.print("  "); //$NON-NLS-1$
            }
            writer.print("  "); //$NON-NLS-1$
            attributeNodeToString(writer, nextNode);
        }
        curDepth--;
        return;
    }

    /**
     * Debugging method to print the contents of this attribute
     *
     * @param writer
     *            PrintWriter where to write the information
     */
    public void debugPrint(PrintWriter writer) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("------------------------------"); //$NON-NLS-1$
        writer.println("Attribute tree: (quark)\n"); //$NON-NLS-1$
        curDepth = 0;
        attributeNodeToString(writer, this);
        writer.print('\n');
    }
}
