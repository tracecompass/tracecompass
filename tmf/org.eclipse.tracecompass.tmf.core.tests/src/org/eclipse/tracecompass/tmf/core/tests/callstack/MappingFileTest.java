/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.callstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.tracecompass.tmf.core.symbols.IMappingFile;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;
import org.junit.Test;

/**
 * Test the {@link IMappingFile} class
 *
 * @author Geneviève Bastien
 */
public class MappingFileTest {

    /**
     * Test the {@link IMappingFile#create(String, boolean)} method
     */
    @Test
    public void testValidMappingTextFile() {
        Path nmOutput = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack" , "nm-output-example");
        assertTrue(Files.exists(nmOutput));
        String filePath = nmOutput.toFile().getAbsolutePath();
        assertNotNull(filePath);
        IMappingFile mf = IMappingFile.create(filePath, false);
        assertNotNull(mf);
    }

    /**
     * Test the symbol returned by a mapping file obtained by nm
     */
    @Test
    public void testGettingSymbolMapFile() {
        Path nmOutput = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack" , "nm-output-example");
        assertTrue(Files.exists(nmOutput));
        String filePath = nmOutput.toFile().getAbsolutePath();
        assertNotNull(filePath);
        IMappingFile mf = IMappingFile.create(filePath, false);
        assertNotNull(mf);

        // Test hitting an exact value
        TmfResolvedSymbol symbol = mf.getSymbolEntry(Long.valueOf("601191", 16));
        assertNotNull(symbol);
        assertEquals("std::__ioinit", symbol.getSymbolName());

        // Test a value after the symbol
        symbol = mf.getSymbolEntry(Long.valueOf("601191", 16) + 4L);
        assertNotNull(symbol);
        assertEquals("std::__ioinit", symbol.getSymbolName());

        // Test a value before the beginning of the mapping
        symbol = mf.getSymbolEntry(Long.valueOf("400", 16));
        assertNull(symbol);

        // Test hitting an end symbol, it should return that symbol
        symbol = mf.getSymbolEntry(Long.valueOf("400a28", 16));
        assertNotNull(symbol);
        assertEquals("__FRAME_END__", symbol.getSymbolName());

        // Test hitting after the end symbol, it should return null
        symbol = mf.getSymbolEntry(Long.valueOf("400a28", 16) + 8L);
        assertNull(symbol);
    }

    /**
     * Test invalid files
     */
    @Test
    public void testInvalidFiles() {
        // File does not exist
        IMappingFile mf = IMappingFile.create("test", false);
        assertNull(mf);

        // File does not contain any data
        Path nmOutput = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack" , "emptyFile");
        assertTrue(Files.exists(nmOutput));
        String filePath = nmOutput.toFile().getAbsolutePath();
        assertNotNull(filePath);
        mf = IMappingFile.create(filePath, false);
        assertNull(mf);
    }
}
