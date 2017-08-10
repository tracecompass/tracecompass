/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.symbols;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.tracecompass.internal.tmf.core.callstack.FunctionNameMapper;
import org.eclipse.tracecompass.internal.tmf.core.callstack.FunctionNameMapper.MappingType;
import org.eclipse.tracecompass.internal.tmf.core.callstack.TmfResolvedSizedSymbol;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;
import org.junit.Test;

/**
 * Unit tests for the {@link FunctionNameMapper} class.
 *
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 */
public class FunctionNameMapperTest {

    /**
     * Test the output of a text file obtained from 'nm'.
     */
    @Test
    public void testNmFile() {
        Path nmOutput = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack" , "nm-output-example");
        assertTrue(Files.exists(nmOutput));
        Map<Long, TmfResolvedSymbol> results = FunctionNameMapper.mapFromNmTextFile(nmOutput.toFile());

        assertNotNull(results);
        assertEquals(28, results.size());
        assertNull(results.get(null));

        assertSymbolString("completed.7259", "601190", results);
        assertSymbolString("data_start", "601048", results);
        assertSymbolString("deregister_tm_clones", "400690", results);
        assertSymbolString("__do_global_dtors_aux", "400710", results);
        assertSymbolString("__dso_handle", "601050", results);
        assertSymbolString("_DYNAMIC", "600e18", results);
        assertSymbolString("_end", "601198", results);
        assertSymbolString("_fini", "400874", results);
        assertSymbolString("frame_dummy", "400730", results);
        assertSymbolString("__FRAME_END__", "400a28", results);
        assertSymbolString("_GLOBAL_OFFSET_TABLE_", "601000", results);
        assertSymbolString("_GLOBAL__sub_I_main", "4007ad", results);
        assertSymbolString("_init", "4005d0", results);
        assertSymbolString("__init_array_end", "600e08", results);
        assertSymbolString("__init_array_start", "600df8", results);
        assertSymbolString("_IO_stdin_used", "400880", results);
        assertSymbolString("__JCR_LIST__", "600e10", results);
        assertSymbolString("__libc_csu_fini", "400870", results);
        assertSymbolString("__libc_csu_init", "400800", results);
        assertSymbolString("main", "400756", results);
        assertSymbolString("register_tm_clones", "4006d0", results);
        assertSymbolString("_start", "400660", results);
        assertSymbolString("__TMC_END__", "601058", results);
        assertSymbolString("Bar<int, int>* foo<int, int>(int, int)", "4007c2", results);
        assertSymbolString("__static_initialization_and_destruction_0(int, int)", "400770", results);
        assertSymbolString("std::cout@@GLIBCXX_3.4", "601080", results);
        assertSymbolString("std::piecewise_construct", "400884", results);
        assertSymbolString("std::__ioinit", "601191", results);
    }

    private static void assertSymbolString(String expected, String address, Map<Long, TmfResolvedSymbol> results) {
        TmfResolvedSymbol symbol = results.get(Long.parseUnsignedLong(address, 16));
        assertNotNull(symbol);
        assertEquals(expected, symbol.getSymbolName());
    }

    /**
     * Test the output of a text file obtained from tools like for example
     * perf-map-agent who adds a size to the symbol.
     */
    @Test
    public void testMapFileWithSize() {
        Path nmOutput = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack", "withsize-123.map");
        assertTrue(Files.exists(nmOutput));
        Map<Long, TmfResolvedSymbol> results = FunctionNameMapper.mapFromSizedTextFile(nmOutput.toFile());

        assertNotNull(results);
        assertEquals(2, results.size());
        assertNull(results.get(null));

        // Verify the symbol names
        assertSymbolString("Inner function with size", "ffeeddccbbaa0110", results);
        assertSymbolString("Outer function with size", "ffeeddccbbaa0100", results);

        // Verify the symbol size
        TmfResolvedSymbol symbol = results.get(Long.parseUnsignedLong("ffeeddccbbaa0100", 16));
        assertTrue(symbol instanceof TmfResolvedSizedSymbol);
        TmfResolvedSizedSymbol sizedSymbol = (TmfResolvedSizedSymbol) symbol;
        assertEquals(Long.parseUnsignedLong("200", 16), sizedSymbol.getLength());
        assertEquals(Long.parseUnsignedLong("ffeeddccbbaa0300", 16), sizedSymbol.getEnd());

        symbol = results.get(Long.parseUnsignedLong("ffeeddccbbaa0110", 16));
        assertTrue(symbol instanceof TmfResolvedSizedSymbol);
        sizedSymbol = (TmfResolvedSizedSymbol) symbol;
        assertEquals(Long.parseUnsignedLong("20", 16), sizedSymbol.getLength());
        assertEquals(Long.parseUnsignedLong("ffeeddccbbaa0130", 16), sizedSymbol.getEnd());

    }

    /**
     * Test the {@link FunctionNameMapper#guessMappingType(java.io.File)} method
     */
    @Test
    public void testGuessingFileType() {
        // Guess and nm output file
        Path path = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack", "nm-output-example");
        assertTrue(Files.exists(path));
        assertEquals("guess nm output", MappingType.NM, FunctionNameMapper.guessMappingType(path.toFile()));

        path = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack", "withsize-123.map");
        assertTrue(Files.exists(path));
        assertEquals("guess sized mapping", MappingType.MAP_WITH_SIZE, FunctionNameMapper.guessMappingType(path.toFile()));

        path = Paths.get("..", "..", "tmf", "org.eclipse.tracecompass.tmf.core.tests",
                "testfiles", "callstack", "emptyFile");
        assertTrue(Files.exists(path));
        assertEquals("guess sized mapping", MappingType.UNKNOWN, FunctionNameMapper.guessMappingType(path.toFile()));
    }
}
