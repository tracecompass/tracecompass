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

package org.eclipse.linuxtools.internal.tmf.core.statesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * The Attribute Tree is the /proc-like filesystem used to organize attributes.
 * Each node of this tree is both like a file and a directory in the
 * "file system".
 *
 * @author alexmont
 *
 */
public final class AttributeTree {

    /* "Magic number" for attribute tree files or file sections */
    private static final int ATTRIB_TREE_MAGIC_NUMBER = 0x06EC3671;

    private final StateSystem ss;
    private final List<Attribute> attributeList;
    private final Attribute attributeTreeRoot;

    /**
     * Standard constructor, create a new empty Attribute Tree
     *
     * @param ss
     *            The StateSystem to which this AT is attached
     */
    public AttributeTree(StateSystem ss) {
        this.ss = ss;
        this.attributeList = Collections.synchronizedList(new ArrayList<Attribute>());
        this.attributeTreeRoot = new AlphaNumAttribute(null, "root", -1); //$NON-NLS-1$
    }

    /**
     * "Existing file" constructor. Builds an attribute tree from a
     * "mapping file" or mapping section previously saved somewhere.
     *
     * @param ss
     *            StateSystem to which this AT is attached
     * @param fis
     *            File stream where to read the AT information. Make sure it's
     *            sought at the right place!
     * @throws IOException
     *             If there is a problem reading from the file stream
     */
    public AttributeTree(StateSystem ss, FileInputStream fis) throws IOException {
        this(ss);
        DataInputStream in = new DataInputStream(new BufferedInputStream(fis));

        /* Message for exceptions, shouldn't be externalized */
        final String errorMessage = "The attribute tree file section is either invalid or corrupted."; //$NON-NLS-1$

        ArrayList<String[]> list = new ArrayList<>();
        byte[] curByteArray;
        String curFullString;
        String[] curStringArray;
        int res, remain, size;
        int expectedSize = 0;
        int total = 0;

        /* Read the header of the Attribute Tree file (or file section) */
        res = in.readInt(); /* Magic number */
        if (res != ATTRIB_TREE_MAGIC_NUMBER) {
            throw new IOException(errorMessage);
        }

        /* Expected size of the section */
        expectedSize = in.readInt();
        if (expectedSize < 12) {
            throw new IOException(errorMessage);
        }

        /* How many entries we have to read */
        remain = in.readInt();
        total += 12;

        /* Read each entry */
        for (; remain > 0; remain--) {
            /* Read the first byte = the size of the entry */
            size = in.readByte();
            curByteArray = new byte[size];
            res = in.read(curByteArray);
            if (res != size) {
                throw new IOException(errorMessage);
            }

            /*
             * Go buffer -> byteArray -> String -> String[] -> insert in list.
             * bleh
             */
            curFullString = new String(curByteArray);
            curStringArray = curFullString.split("/"); //$NON-NLS-1$
            list.add(curStringArray);

            /* Read the 0'ed confirmation byte */
            res = in.readByte();
            if (res != 0) {
                throw new IOException(errorMessage);
            }
            total += curByteArray.length + 2;
        }

        if (total != expectedSize) {
            throw new IOException(errorMessage);
        }

        /*
         * Now we have 'list', the ArrayList of String arrays representing all
         * the attributes. Simply create attributes the normal way from them.
         */
        for (String[] attrib : list) {
            this.getQuarkAndAdd(-1, attrib);
        }
    }

    /**
     * Tell the Attribute Tree to write itself somewhere in a file.
     *
     * @param file
     *            The file to write to
     * @param pos
     *            The position (in bytes) in the file where to write
     * @return The total number of bytes written.
     */
    public int writeSelf(File file, long pos) {
        int total = 0;
        byte[] curByteArray;

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");) { //$NON-NLS-1$
            raf.seek(pos);

            /* Write the almost-magic number */
            raf.writeInt(ATTRIB_TREE_MAGIC_NUMBER);

            /* Placeholder for the total size of the section... */
            raf.writeInt(-8000);

            /* Write the number of entries */
            raf.writeInt(this.attributeList.size());
            total += 12;

            /* Write the attributes themselves */
            for (Attribute entry : this.attributeList) {
                curByteArray = entry.getFullAttributeName().getBytes();
                if (curByteArray.length > Byte.MAX_VALUE) {
                    throw new IOException("Attribute with name \"" //$NON-NLS-1$
                            + Arrays.toString(curByteArray) + "\" is too long."); //$NON-NLS-1$
                }
                /* Write the first byte = size of the array */
                raf.writeByte((byte) curByteArray.length);

                /* Write the array itself */
                raf.write(curByteArray);

                /* Write the 0'ed byte */
                raf.writeByte((byte) 0);

                total += curByteArray.length + 2;
            }

            /* Now go back and write the actual size of this section */
            raf.seek(pos + 4);
            raf.writeInt(total);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Return the number of attributes this system as seen so far. Note that
     * this also equals the integer value (quark) the next added attribute will
     * have.
     *
     * @return The current number of attributes in the tree
     */
    public int getNbAttributes() {
        return attributeList.size();
    }

    /**
     * Get the quark for a given attribute path. No new attribute will be
     * created : if the specified path does not exist, throw an error.
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which relative queries will
     *            start. Use '-1' to start at the root node.
     * @param subPath
     *            The path to the attribute, relative to the starting node.
     * @return The quark of the specified attribute
     * @throws AttributeNotFoundException
     *             If the specified path was not found
     */
    public int getQuarkDontAdd(int startingNodeQuark, String... subPath)
            throws AttributeNotFoundException {
        assert (startingNodeQuark >= -1);

        Attribute prevNode;

        /* If subPath is empty, simply return the starting quark */
        if (subPath == null || subPath.length == 0) {
            return startingNodeQuark;
        }

        /* Get the "starting node" */
        if (startingNodeQuark == -1) {
            prevNode = attributeTreeRoot;
        } else {
            prevNode = attributeList.get(startingNodeQuark);
        }

        int knownQuark = prevNode.getSubAttributeQuark(subPath);
        if (knownQuark == -1) {
            /*
             * The attribute doesn't exist, but we have been specified to NOT
             * add any new attributes.
             */
            throw new AttributeNotFoundException();
        }
        /*
         * The attribute was already existing, return the quark of that
         * attribute
         */
        return knownQuark;
    }

    /**
     * Get the quark of a given attribute path. If that specified path does not
     * exist, it will be created (and the quark that was just created will be
     * returned).
     *
     * @param startingNodeQuark
     *            The quark of the attribute from which relative queries will
     *            start. Use '-1' to start at the root node.
     * @param subPath
     *            The path to the attribute, relative to the starting node.
     * @return The quark of the attribute represented by the path
     */
    public synchronized int getQuarkAndAdd(int startingNodeQuark, String... subPath) {
        // FIXME synchronized here is probably quite costly... maybe only locking
        // the "for" would be enough?
        assert (subPath != null && subPath.length > 0);
        assert (startingNodeQuark >= -1);

        Attribute nextNode = null;
        Attribute prevNode;

        /* Get the "starting node" */
        if (startingNodeQuark == -1) {
            prevNode = attributeTreeRoot;
        } else {
            prevNode = attributeList.get(startingNodeQuark);
        }

        int knownQuark = prevNode.getSubAttributeQuark(subPath);
        if (knownQuark == -1) {
            /*
             * The attribute was not in the table previously, and we want to add
             * it
             */
            for (String curDirectory : subPath) {
                nextNode = prevNode.getSubAttributeNode(curDirectory);
                if (nextNode == null) {
                    /* This is where we need to start adding */
                    nextNode = new AlphaNumAttribute(prevNode, curDirectory,
                            attributeList.size());
                    prevNode.addSubAttribute(nextNode);
                    attributeList.add(nextNode);
                    ss.addEmptyAttribute();
                }
                prevNode = nextNode;
            }
            /*
             * Insert an initial null value for this attribute in the state
             * system (in case the state provider doesn't set one).
             */
            final int newAttrib = attributeList.size() - 1;
            try {
                ss.modifyAttribute(ss.getStartTime(), TmfStateValue.nullValue(), newAttrib);
            } catch (TimeRangeException e) {
                /* Should not happen, we're inserting at ss's start time */
                throw new IllegalStateException(e);
            } catch (AttributeNotFoundException e) {
                /* Should not happen, we just created this attribute! */
                throw new IllegalStateException(e);
            } catch (StateValueTypeException e) {
                /* Should not happen, there is no existing state value, and the
                 * one we insert is a null value anyway. */
                throw new IllegalStateException(e);
            }

            return newAttrib;
        }
        /*
         * The attribute was already existing, return the quark of that
         * attribute
         */
        return knownQuark;
    }

    /**
     * Returns the sub-attributes of the quark passed in parameter
     *
     * @param attributeQuark
     *            The quark of the attribute to print the sub-attributes of.
     * @param recursive
     *            Should the query be recursive or not? If false, only children
     *            one level deep will be returned. If true, all descendants will
     *            be returned (depth-first search)
     * @return The list of quarks representing the children attributes
     * @throws AttributeNotFoundException
     *             If 'attributeQuark' is invalid, or if there is no attrbiute
     *             associated to it.
     */
    public List<Integer> getSubAttributes(int attributeQuark, boolean recursive)
            throws AttributeNotFoundException {
        List<Integer> listOfChildren = new ArrayList<>();
        Attribute startingAttribute;

        /* Check if the quark is valid */
        if (attributeQuark < -1 || attributeQuark >= attributeList.size()) {
            throw new AttributeNotFoundException();
        }

        /* Set up the node from which we'll start the search */
        if (attributeQuark == -1) {
            startingAttribute = attributeTreeRoot;
        } else {
            startingAttribute = attributeList.get(attributeQuark);
        }

        /* Iterate through the sub-attributes and add them to the list */
        addSubAttributes(listOfChildren, startingAttribute, recursive);

        return listOfChildren;
    }

    private void addSubAttributes(List<Integer> list, Attribute curAttribute,
            boolean recursive) {
        for (Attribute childNode : curAttribute.getSubAttributes()) {
            list.add(childNode.getQuark());
            if (recursive) {
                addSubAttributes(list, childNode, true);
            }
        }
    }

    /**
     * Get then base name of an attribute specified by a quark.
     *
     * @param quark
     *            The quark of the attribute
     * @return The (base) name of the attribute
     */
    public String getAttributeName(int quark) {
        return attributeList.get(quark).getName();
    }

    /**
     * Get the full path name of an attribute specified by a quark.
     *
     * @param quark
     *            The quark of the attribute
     * @return The full path name of the attribute
     */
    public String getFullAttributeName(int quark) {
        if (quark >= attributeList.size() || quark < 0) {
            return null;
        }
        return attributeList.get(quark).getFullAttributeName();
    }

    /**
     * Debug-print all the attributes in the tree.
     *
     * @param writer
     *            The writer where to print the output
     */
    public void debugPrint(PrintWriter writer) {
        attributeTreeRoot.debugPrint(writer);
    }

}
