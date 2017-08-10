/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.symbols;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.callstack.MappingFile;
import org.eclipse.tracecompass.internal.tmf.core.callstack.SizedMappingFile;
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
    public void testValidTextFile() {
        IMappingFile mf = getMappingFile("nm-output-example");
        assertNotNull("global symbol file", mf);
        assertTrue(mf instanceof MappingFile);
        assertTrue(mf.getPid() < 0);

        // Test a file name ending by number without '-'
        mf = getMappingFile("symbol123.map");
        assertNotNull("Global symbol numbered file", mf);
        assertTrue(mf.getPid() < 0);

        // Test a file name with process ID in the name
        mf = getMappingFile("symbol-123.map");
        assertNotNull("Symbol file with pid", mf);
        assertTrue(mf instanceof MappingFile);
        assertEquals(123, mf.getPid());

        // Use the symbol-123.map file for another process
        Path nmOutput = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack" , "symbol-123.map");
        assertTrue(Files.exists(nmOutput));
        String filePath = nmOutput.toFile().getAbsolutePath();
        assertNotNull(filePath);
        mf = IMappingFile.create(filePath, false, 2);
        assertNotNull("Symbol file for different pid", mf);
        assertEquals(2, mf.getPid());

        // Use the file with sizes instead of nm output
        mf = getMappingFile("withsize-123.map");
        assertNotNull("Symbol file with sizes and pid", mf);
        assertTrue(mf instanceof SizedMappingFile);
        assertEquals(123, mf.getPid());
    }

    private static @Nullable IMappingFile getMappingFile(String fileName) {
        Path nmOutput = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack" , fileName);
        assertTrue(Files.exists(nmOutput));
        String filePath = nmOutput.toFile().getAbsolutePath();
        assertNotNull(filePath);
        return IMappingFile.create(filePath, false);
    }

    /**
     * Get the test mapping files. These can be used by symbol providers.
     *
     * @return The list of MappingFile
     */
    public static @NonNull List<@NonNull IMappingFile> getMappingFiles() {
        List<@NonNull IMappingFile> list = new ArrayList<>();
        IMappingFile mf = getMappingFile("nm-output-example");
        if (mf != null) {
            list.add(mf);
        }
        mf = getMappingFile("symbol-123.map");
        if (mf != null) {
            list.add(mf);
        }
        mf = getMappingFile("withsize-123.map");
        if (mf != null) {
            list.add(mf);
        }
        return list;

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
        mf = getMappingFile("emptyFile");
        assertNull(mf);
    }

    /**
     * Test the symbols returned by a mapping file obtained by perf-map-agent
     */
    @Test
    public void testGettingSymbolWithSize() {
        Path nmOutput = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack" , "withsize-123.map");
        assertTrue(Files.exists(nmOutput));
        String filePath = nmOutput.toFile().getAbsolutePath();
        assertNotNull(filePath);
        IMappingFile mf = IMappingFile.create(filePath, false);
        assertNotNull(mf);

        // Test a value not within range
        TmfResolvedSymbol symbol = mf.getSymbolEntry(Long.parseUnsignedLong("ff00000000000100", 16));
        assertNull(symbol);

        // Test a value of the outer function, before inner
        symbol = mf.getSymbolEntry(Long.parseUnsignedLong("ffeeddccbbaa0102", 16));
        assertNotNull(symbol);
        assertEquals("Outer function with size", symbol.getSymbolName());

        // Test a value of the inner function
        symbol = mf.getSymbolEntry(Long.parseUnsignedLong("ffeeddccbbaa0120", 16));
        assertNotNull(symbol);
        assertEquals("Inner function with size", symbol.getSymbolName());

        // Test a value within the outer function but after the inner
        symbol = mf.getSymbolEntry(Long.parseUnsignedLong("ffeeddccbbaa0200", 16));
        assertNotNull(symbol);
        assertEquals("Outer function with size", symbol.getSymbolName());

        // Test a value after the outer function
        symbol = mf.getSymbolEntry(Long.parseUnsignedLong("ffeeddccbbaa0400", 16));
        assertNull(symbol);
    }

}
