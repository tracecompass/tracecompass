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

package org.eclipse.tracecompass.tmf.core.tests.callstack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.tracecompass.internal.tmf.core.callstack.FunctionNameMapper;
import org.junit.Test;

/**
 * Unit tests for the {@link FunctionNameMapper} class.
 *
 * @author Alexandre Montplaisir
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
        Map<Long, String> results = FunctionNameMapper.mapFromNmTextFile(nmOutput.toFile());

        assertNotNull(results);
        assertEquals(28, results.size());
        assertNull(results.get(""));

        assertEquals("completed.7259", results.get(Long.valueOf("601190", 16)));
        assertEquals("data_start", results.get(Long.valueOf("601048", 16)));
        assertEquals("deregister_tm_clones", results.get(Long.valueOf("400690", 16)));
        assertEquals("__do_global_dtors_aux", results.get(Long.valueOf("400710", 16)));
        assertEquals("__dso_handle", results.get(Long.valueOf("601050", 16)));
        assertEquals("_DYNAMIC", results.get(Long.valueOf("600e18", 16)));
        assertEquals("_end", results.get(Long.valueOf("601198", 16)));
        assertEquals("_fini", results.get(Long.valueOf("400874", 16)));
        assertEquals("frame_dummy", results.get(Long.valueOf("400730", 16)));
        assertEquals("__FRAME_END__", results.get(Long.valueOf("400a28", 16)));
        assertEquals("_GLOBAL_OFFSET_TABLE_", results.get(Long.valueOf("601000", 16)));
        assertEquals("_GLOBAL__sub_I_main", results.get(Long.valueOf("4007ad", 16)));
        assertEquals("_init", results.get(Long.valueOf("4005d0", 16)));
        assertEquals("__init_array_end", results.get(Long.valueOf("600e08", 16)));
        assertEquals("__init_array_start", results.get(Long.valueOf("600df8", 16)));
        assertEquals("_IO_stdin_used", results.get(Long.valueOf("400880", 16)));
        assertEquals("__JCR_LIST__", results.get(Long.valueOf("600e10", 16)));
        assertEquals("__libc_csu_fini", results.get(Long.valueOf("400870", 16)));
        assertEquals("__libc_csu_init", results.get(Long.valueOf("400800", 16)));
        assertEquals("main", results.get(Long.valueOf("400756", 16)));
        assertEquals("register_tm_clones", results.get(Long.valueOf("4006d0", 16)));
        assertEquals("_start", results.get(Long.valueOf("400660", 16)));
        assertEquals("__TMC_END__", results.get(Long.valueOf("601058", 16)));
        assertEquals("Bar<int, int>* foo<int, int>(int, int)", results.get(Long.valueOf("4007c2", 16)));
        assertEquals("__static_initialization_and_destruction_0(int, int)", results.get(Long.valueOf("400770", 16)));
        assertEquals("std::cout@@GLIBCXX_3.4", results.get(Long.valueOf("601080", 16)));
        assertEquals("std::piecewise_construct", results.get(Long.valueOf("400884", 16)));
        assertEquals("std::__ioinit", results.get(Long.valueOf("601191", 16)));

    }
}
