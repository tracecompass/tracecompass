/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

/**
 * Utility class to get file name, function/symbol name and line number from a
 * given offset. In TMF this is represented as a {@link TmfCallsite}.
 *
 * @author Alexandre Montplaisir
 */
public final class FileOffsetMapper {

    private static final String DISCRIMINATOR = "\\(discriminator.*\\)"; //$NON-NLS-1$
    private static final String ADDR2LINE_EXECUTABLE = "addr2line"; //$NON-NLS-1$

    private FileOffsetMapper() {}

    /**
     * Generate the callsites from a given binary file and address offset.
     *
     * Due to function inlining, it is possible for one offset to actually have
     * multiple call sites. This is why we can return more than one callsite per
     * call.
     *
     * @param file
     *            The binary file to look at
     * @param offset
     *            The memory offset in the file
     * @return The list of callsites corresponding to the offset, reported from
     *         the "highest" inlining location, down to the initial definition.
     */
    public static @Nullable Iterable<TmfCallsite> getCallsiteFromOffset(File file, long offset) {
        if (!Files.exists((file.toPath()))) {
            return null;
        }
        return getCallsiteFromOffsetWithAddr2line(file, offset);
    }

    private static @Nullable Iterable<TmfCallsite> getCallsiteFromOffsetWithAddr2line(File file, long offset) {
        List<TmfCallsite> callsites = new LinkedList<>();

        // FIXME Could eventually use CDT's Addr2line class once it imlements --inlines
        List<String> output = getOutputFromCommand(Arrays.asList(
                ADDR2LINE_EXECUTABLE, "-i", "-e", file.toString(), "0x" + Long.toHexString(offset)));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

        if (output == null) {
            /* Command returned an error */
            return null;
        }

        for (String outputLine : output) {
            // Remove discriminator part, for example: /build/buildd/glibc-2.21/elf/dl-object.c:78 (discriminator 8)
            outputLine = outputLine.replaceFirst(DISCRIMINATOR, "").trim(); //$NON-NLS-1$

            String[] elems = outputLine.split(":"); //$NON-NLS-1$
            String fileName = elems[0];
            if (fileName.equals("??")) { //$NON-NLS-1$
                continue;
            }
            long lineNumber = Long.parseLong(elems[1]);

            callsites.add(new TmfCallsite(fileName, null, lineNumber));
        }

        return callsites;
    }

    private static @Nullable List<String> getOutputFromCommand(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);

            Process p = builder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));) {
                int ret = p.waitFor();
                List<String> lines = br.lines().collect(Collectors.toList());

                return (ret == 0 ? lines : null);
            }
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
