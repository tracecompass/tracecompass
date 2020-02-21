/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.Nullable;

/**
 * System call lookup helper, reads a map of system call names and their file
 * locations. System calls are ABIs and should remain static in location when
 * they are created.
 *
 * @author Matthew Khouzam
 */
public final class SyscallLookup {

    private static final String SYSCALL_TSV_PATH = "res/syscalls.tsv"; //$NON-NLS-1$

    private static @Nullable SyscallLookup sInstance = null;

    private final Map<String, String> fComponents = new HashMap<>();
    private final Map<String, String> fFiles = new HashMap<>();

    /**
     * Get the instance of the System call lookup.
     * @return the instance
     */
    public static SyscallLookup getInstance() {
        SyscallLookup instance = sInstance;
        if (instance == null) {
            instance = create();
            sInstance = instance;
        }
        return instance;
    }


    @SuppressWarnings("null")
    private static SyscallLookup create() {
        try {
            IPath path = Activator.getDefault().getAbsolutePath(new Path(SYSCALL_TSV_PATH));
            if (path != null) {
                File file = path.toFile();
                if (!file.exists()) {
                    Activator.getDefault().logWarning("Syscall names not available!"); //$NON-NLS-1$
                    return new SyscallLookup(Collections.emptyList());
                }
                return new SyscallLookup(FileUtils.readLines(file, StandardCharsets.UTF_8.name()));
            }
        } catch (IOException e) {
            Activator.getDefault().logError("Failed to read file", e); //$NON-NLS-1$
        }
        return new SyscallLookup(Collections.emptyList());
    }

    private SyscallLookup(@Nullable List<@Nullable String> lines) {
        if (lines != null) {
            for (String line : lines) {
                if (line == null || line.startsWith("#")) { //$NON-NLS-1$
                    continue;
                }
                String[] components = line.split("\t"); //$NON-NLS-1$
                if (components.length > 2) {
                    String name = String.valueOf(components[0]);
                    fComponents.put(name, String.valueOf(components[1]));
                    fFiles.put(name, String.valueOf(components[2]));
                }
            }
        }
    }

    /**
     * Get the component of the system call
     *
     * @param name
     *            system call
     * @return the component name or {@link StringUtils#EMPTY} if not found
     */
    public String getComponent(String name) {
        return fComponents.getOrDefault(name, StringUtils.EMPTY);
    }

    /**
     * Get the file of the system call
     *
     * @param name
     *            system call
     * @return the file name or {@link StringUtils#EMPTY} if not found
     */
    public String getFile(String name) {
        return fFiles.getOrDefault(name, StringUtils.EMPTY);
    }
}
