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
        Path nmOutput = Paths.get("testfiles", "callstack" , "nm-output-example");
        assertTrue(Files.exists(nmOutput));
        Map<String, String> results = FunctionNameMapper.mapFromNmTextFile(nmOutput.toFile());

        assertNotNull(results);
        assertEquals(28, results.size());
        assertNull(results.get(""));

        assertEquals("completed.7259", results.get("601190"));
        assertEquals("data_start", results.get("601048"));
        assertEquals("deregister_tm_clones", results.get("400690"));
        assertEquals("__do_global_dtors_aux", results.get("400710"));
        assertEquals("__dso_handle", results.get("601050"));
        assertEquals("_DYNAMIC", results.get("600e18"));
        assertEquals("_end", results.get("601198"));
        assertEquals("_fini", results.get("400874"));
        assertEquals("frame_dummy", results.get("400730"));
        assertEquals("__FRAME_END__", results.get("400a28"));
        assertEquals("_GLOBAL_OFFSET_TABLE_", results.get("601000"));
        assertEquals("_GLOBAL__sub_I_main", results.get("4007ad"));
        assertEquals("_init", results.get("4005d0"));
        assertEquals("__init_array_end", results.get("600e08"));
        assertEquals("__init_array_start", results.get("600df8"));
        assertEquals("_IO_stdin_used", results.get("400880"));
        assertEquals("__JCR_LIST__", results.get("600e10"));
        assertEquals("__libc_csu_fini", results.get("400870"));
        assertEquals("__libc_csu_init", results.get("400800"));
        assertEquals("main", results.get("400756"));
        assertEquals("register_tm_clones", results.get("4006d0"));
        assertEquals("_start", results.get("400660"));
        assertEquals("__TMC_END__", results.get("601058"));
        assertEquals("Bar<int, int>* foo<int, int>(int, int)", results.get("4007c2"));
        assertEquals("__static_initialization_and_destruction_0(int, int)", results.get("400770"));
        assertEquals("std::cout@@GLIBCXX_3.4", results.get("601080"));
        assertEquals("std::piecewise_construct", results.get("400884"));
        assertEquals("std::__ioinit", results.get("601191"));
    }
}
