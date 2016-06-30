/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.SourceCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Utility class to get file name, function/symbol name and line number from a
 * given offset. In TMF this is represented as a {@link TmfCallsite}.
 *
 * @author Alexandre Montplaisir
 */
public final class FileOffsetMapper {

    private static final Logger LOGGER = TraceCompassLog.getLogger(FileOffsetMapper.class);

    private static final String DISCRIMINATOR = "\\(discriminator.*\\)"; //$NON-NLS-1$
    private static final String ADDR2LINE_EXECUTABLE = "addr2line"; //$NON-NLS-1$

    private static final long CACHE_SIZE = 1000;

    private FileOffsetMapper() {}

    /**
     * Class representing an offset in a specific file
     */
    private static class FileOffset {

        private final String fFilePath;
        private final @Nullable String fBuildId;
        private final long fOffset;

        public FileOffset(String filePath, @Nullable String buildId, long offset) {
            fFilePath = filePath;
            fBuildId = buildId;
            fOffset = offset;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(fFilePath, fBuildId, fOffset);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            FileOffset other = (FileOffset) obj;
            return Objects.equal(fFilePath, other.fFilePath) &&
                    Objects.equal(fBuildId, other.fBuildId) &&
                    Objects.equal(fOffset, other.fOffset);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("fFilePath", fFilePath) //$NON-NLS-1$
                    .add("fBuildId", fBuildId) //$NON-NLS-1$
                    .add("fOffset", String.format("0x%h", fOffset)) //$NON-NLS-1$ //$NON-NLS-2$
                    .toString();
        }
    }

    /**
     * Cache of all calls to 'addr2line', so that we can avoid recalling the
     * external process repeatedly.
     *
     * It is static, meaning one cache for the whole application, since the
     * symbols in a file on disk are independent from the trace referring to it.
     */
    private static final LoadingCache<FileOffset, @Nullable Iterable<SourceCallsite>> CALLSITE_CACHE;
    static {
        CALLSITE_CACHE = checkNotNull(CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(new CacheLoader<FileOffset, @Nullable Iterable<SourceCallsite>>() {
                @Override
                public @Nullable Iterable<SourceCallsite> load(FileOffset fo) {
                    LOGGER.fine(() -> "[FileOffsetMapper:CacheMiss] file/offset=" + fo.toString()); //$NON-NLS-1$
                    return getCallsiteFromOffsetWithAddr2line(fo);
                }
            }));
    }

    /**
     * Generate the callsites from a given binary file and address offset.
     *
     * Due to function inlining, it is possible for one offset to actually have
     * multiple call sites. This is why we can return more than one callsite per
     * call.
     *
     * @param file
     *            The binary file to look at
     * @param buildId
     *            The expected buildId of the binary file (is not verified at
     *            the moment)
     * @param offset
     *            The memory offset in the file
     * @return The list of callsites corresponding to the offset, reported from
     *         the "highest" inlining location, down to the initial definition.
     */
    public static @Nullable Iterable<SourceCallsite> getCallsiteFromOffset(File file, @Nullable String buildId, long offset) {
        LOGGER.finer(() -> String.format("[FileOffsetMapper:Request] file=%s, buildId=%s, offset=0x%h", //$NON-NLS-1$
                file.toString(), buildId, offset));

        if (!Files.exists((file.toPath()))) {
            LOGGER.finer(() -> "[FileOffsetMapper:RequestFailed] File not found"); //$NON-NLS-1$
            return null;
        }
        // TODO We should also eventually verify that the passed buildId matches
        // the file we are attempting to open.
        FileOffset fo = new FileOffset(checkNotNull(file.toString()), buildId, offset);

        Iterable<SourceCallsite> callsites = CALLSITE_CACHE.getUnchecked(fo);
        LOGGER.finer(() -> String.format("[FileOffsetMapper:RequestComplete] callsites=%s", callsites)); //$NON-NLS-1$
        return callsites;
    }

    private static @Nullable Iterable<SourceCallsite> getCallsiteFromOffsetWithAddr2line(FileOffset fo) {
        String filePath = fo.fFilePath;
        long offset = fo.fOffset;

        List<SourceCallsite> callsites = new LinkedList<>();

        // FIXME Could eventually use CDT's Addr2line class once it implements --inlines
        List<String> output = getOutputFromCommand(Arrays.asList(
                ADDR2LINE_EXECUTABLE, "-i", "-f", "-C", "-e", filePath, "0x" + Long.toHexString(offset)));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

        if (output == null) {
            /* Command returned an error */
            return null;
        }

        /*
         * When passing the -f flag, the output alternates between function
         * names and file/line location.
         */
        boolean oddLine = false; // We flip at the start, first loop will be odd
        String currentFunctionName = null;
        for (String outputLine : output) {
            /* Flip the boolean for the following line */
            oddLine = !oddLine;

            // Remove discriminator part, for example: /build/buildd/glibc-2.21/elf/dl-object.c:78 (discriminator 8)
            outputLine = outputLine.replaceFirst(DISCRIMINATOR, "").trim(); //$NON-NLS-1$

            if (oddLine) {
                /* This is a line indicating the function name */
                currentFunctionName = outputLine;
            } else {
                /* This is a line indicating a call site */
                String[] elems = outputLine.split(":"); //$NON-NLS-1$
                String fileName = elems[0];
                if (fileName.equals("??")) { //$NON-NLS-1$
                    continue;
                }
                try {
                    long lineNumber = Long.parseLong(elems[1]);
                    callsites.add(new SourceCallsite(fileName, currentFunctionName, lineNumber));

                } catch (NumberFormatException e) {
                    /*
                     * Probably a '?' output, meaning unknown line number.
                     * Ignore this entry.
                     */
                    continue;
                }
            }
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
