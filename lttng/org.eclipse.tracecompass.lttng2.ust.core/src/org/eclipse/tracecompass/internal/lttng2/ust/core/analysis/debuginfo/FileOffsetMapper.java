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

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.process.ProcessUtils;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;

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
            return Objects.hash(fFilePath, fBuildId, fOffset);
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
            return Objects.equals(fFilePath, other.fFilePath) &&
                    Objects.equals(fBuildId, other.fBuildId) &&
                    Objects.equals(fOffset, other.fOffset);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("fFilePath", fFilePath) //$NON-NLS-1$
                    .append("fBuildId", fBuildId) //$NON-NLS-1$
                    .append("fOffset", String.format("0x%h", fOffset)) //$NON-NLS-1$ //$NON-NLS-2$
                    .toString();
        }
    }


    /**
     * Generate the callsite from a given binary file and address offset.
     *
     * Due to function inlining, it is possible for one offset to actually have
     * multiple call sites. We will return the most precise one (inner-most) we
     * have available.
     *
     * @param file
     *            The binary file to look at
     * @param buildId
     *            The expected buildId of the binary file (is not verified at
     *            the moment)
     * @param offset
     *            The memory offset in the file
     * @return The corresponding call site
     */
    public static @Nullable TmfCallsite getCallsiteFromOffset(File file, @Nullable String buildId, long offset) {
       Iterable<Addr2lineInfo> output = getAddr2lineInfo(file, buildId, offset);
       if (output == null || Iterables.isEmpty(output)) {
           return null;
       }
       Addr2lineInfo info = Iterables.getLast(output);
       String sourceFile = info.fSourceFileName;
       Long sourceLine = info.fSourceLineNumber;

       if (sourceFile == null) {
           /* Not enough information to provide a callsite */
           return null;
       }
       return new TmfCallsite(sourceFile, sourceLine);
    }

    /**
     * Get the function/symbol name corresponding to binary file and offset.
     *
     * @param file
     *            The binary file to look at
     * @param buildId
     *            The expected buildId of the binary file (is not verified at
     *            the moment)
     * @param offset
     *            The memory offset in the file
     * @return The corresponding function/symbol name
     */
    public static @Nullable String getFunctionNameFromOffset(File file, @Nullable String buildId, long offset) {
        /*
         * TODO We are currently also using 'addr2line' to resolve function
         * names, which requires the binary's DWARF information to be available.
         * A better approach would be to use the binary's symbol table (if it is
         * not stripped), since this is usually more readily available than
         * DWARF.
         */
        Iterable<Addr2lineInfo> output = getAddr2lineInfo(file, buildId, offset);
        if (output == null || Iterables.isEmpty(output)) {
            return null;
        }
        Addr2lineInfo info = Iterables.getLast(output);
        return info.fFunctionName;
    }

    // ------------------------------------------------------------------------
    // Utility methods making use of 'addr2line'
    // ------------------------------------------------------------------------

    /**
     * Value used in addr2line output to represent unknown function names or
     * source files.
     */
    private static final String UNKNOWN_VALUE = "??"; //$NON-NLS-1$

    /**
     * Cache of all calls to 'addr2line', so that we can avoid recalling the
     * external process repeatedly.
     *
     * It is static, meaning one cache for the whole application, since the
     * symbols in a file on disk are independent from the trace referring to it.
     */
    private static final LoadingCache<FileOffset, @NonNull Iterable<Addr2lineInfo>> ADDR2LINE_INFO_CACHE;
    static {
        ADDR2LINE_INFO_CACHE = checkNotNull(CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(new CacheLoader<FileOffset, @NonNull Iterable<Addr2lineInfo>>() {
                @Override
                public @NonNull Iterable<Addr2lineInfo> load(FileOffset fo) {
                    LOGGER.fine(() -> "[FileOffsetMapper:CacheMiss] file/offset=" + fo.toString()); //$NON-NLS-1$
                    return callAddr2line(fo);
                }
            }));
    }

    private static class Addr2lineInfo {

        private final @Nullable String fSourceFileName;
        private final @Nullable Long fSourceLineNumber;
        private final @Nullable String fFunctionName;

        public Addr2lineInfo(@Nullable String sourceFileName,  @Nullable String functionName, @Nullable Long sourceLineNumber) {
            fSourceFileName = sourceFileName;
            fSourceLineNumber = sourceLineNumber;
            fFunctionName = functionName;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("fSourceFileName", fSourceFileName) //$NON-NLS-1$
                    .append("fSourceLineNumber", fSourceLineNumber) //$NON-NLS-1$
                    .append("fFunctionName", fFunctionName) //$NON-NLS-1$
                    .toString();
        }
    }

    private static @Nullable Iterable<Addr2lineInfo> getAddr2lineInfo(File file, @Nullable String buildId, long offset) {
        LOGGER.finer(() -> String.format("[FileOffsetMapper:Addr2lineRequest] file=%s, buildId=%s, offset=0x%h", //$NON-NLS-1$
                file.toString(), buildId, offset));

        if (!Files.exists((file.toPath()))) {
            LOGGER.finer(() -> "[FileOffsetMapper:RequestFailed] File not found"); //$NON-NLS-1$
            return null;
        }
        // TODO We should also eventually verify that the passed buildId matches
        // the file we are attempting to open.
        FileOffset fo = new FileOffset(checkNotNull(file.toString()), buildId, offset);

        @Nullable Iterable<Addr2lineInfo> callsites = ADDR2LINE_INFO_CACHE.getUnchecked(fo);
        LOGGER.finer(() -> String.format("[FileOffsetMapper:RequestComplete] callsites=%s", callsites)); //$NON-NLS-1$
        return callsites;
    }

    private static Iterable<Addr2lineInfo> callAddr2line(FileOffset fo) {
        String filePath = fo.fFilePath;
        long offset = fo.fOffset;

        List<Addr2lineInfo> callsites = new LinkedList<>();

        // FIXME Could eventually use CDT's Addr2line class once it implements --inlines
        List<String> command = Arrays.asList(ADDR2LINE_EXECUTABLE, "-i", "-f", "-C", "-e", filePath, "0x" + Long.toHexString(offset)); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        List<String> output = ProcessUtils.getOutputFromCommand(command);

        if (output == null) {
            /* Command returned an error */
            return Collections.EMPTY_SET;
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
                if (outputLine.equals(UNKNOWN_VALUE)) {
                    currentFunctionName = null;
                } else {
                    currentFunctionName = outputLine;
                }
            } else {
                /* This is a line indicating a call site */
                String[] elems = outputLine.split(":"); //$NON-NLS-1$
                String fileName = elems[0];
                if (fileName.equals(UNKNOWN_VALUE)) {
                    fileName = null;
                }
                Long lineNumber;
                try {
                    lineNumber = Long.valueOf(elems[1]);
                } catch (NumberFormatException e) {
                    /* Probably a '?' output, meaning unknown line number. */
                    lineNumber = null;
                }
                callsites.add(new Addr2lineInfo(fileName, currentFunctionName, lineNumber));
            }
        }

        return callsites;
    }
}
