/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * <b><u>CreateTestFiles</u></b>
 * <p>
 * Create a number of event test files of various lengths.
 * <p>
 * Events have the following format:
 * <ul>
 * <li> [timestamp] [source] [type] [ref] [field]*
 * <li> There are NB_SOURCES sources and NB_TYPES types.
 * <li> The number of fields (0 .. NB_TYPES-1) depends on the event type.
 * </ul>
 */
public class CreateTestFiles {

    // ========================================================================
    // Constants
    // ========================================================================

	private static final String FILE_NAMES[] = { "Test-1K", "Test-10K", "Test-100K", "Test-1M"};
    private static final int    FILE_SIZES[] = {  1000,      10000,      100000,      1000000 };

    private static final int NB_SOURCES = 3;  
    private static final int NB_TYPES   = 5;  

    // ========================================================================
    // Constructors
    // ========================================================================

   /**
     * @param args
     */
    public static void main(String[] args) {
        for (int i = 0; i < FILE_SIZES.length; i++) {
            try {
                createFile(FILE_NAMES[i], FILE_SIZES[i]);
            } catch (Exception e) {
            }
        }
        System.out.println("Done.");
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /**
     * @param file
     * @param size
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void createFile(String file, int size) throws FileNotFoundException, IOException {
        DataOutputStream out;
        System.out.println("Creating " + file);
        out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        for (int i = 0; i < size; i++) {
            int sourceIndex = i % NB_SOURCES;
            int typeIndex   = i % NB_TYPES;
            out.writeLong(i);                       // Timestamp
            out.writeUTF("Source-" + sourceIndex);  // Source
            out.writeUTF("Type-"   + typeIndex);    // Type
            out.writeInt(i);                        // Reference
            for (int j = 0; j < typeIndex; j++) {
                out.writeUTF("Field-" + sourceIndex + "-" + j);
            }
        }
        out.flush();
        out.close();
    }
}
