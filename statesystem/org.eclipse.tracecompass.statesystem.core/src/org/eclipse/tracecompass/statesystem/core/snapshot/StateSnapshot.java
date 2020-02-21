/*******************************************************************************
 * Copyright (c) 2016, 2018 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.snapshot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;
import org.eclipse.tracecompass.internal.statesystem.core.interval.json.TmfIntervalDeserializer;
import org.eclipse.tracecompass.internal.statesystem.core.interval.json.TmfIntervalSerializer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;

/**
 * Wrapper object representing a full query, along with its corresponding
 * attributes. It allows to reconstruct an initial state from scratch.
 *
 * @author Alexandre Montplaisir
 * @author Philippe Proulx
 * @since 4.0
 */
@NonNullByDefault
public class StateSnapshot {

    private static final class AttributeAndInterval {

        @SerializedName("Path")
        @Since(1.0)
        private final List<@NonNull String> fAttribute;
        @SerializedName("Value")
        @Since(1.0)
        private final ITmfStateInterval fInterval;

        /**
         * Constructor
         *
         * @param path
         *            attribute path
         * @param value
         *            the interval
         */
        public AttributeAndInterval(List<@NonNull String> path, ITmfStateInterval value) {
            fAttribute = path;
            fInterval = value;
        }

        public List<@NonNull String> getPath() {
            return fAttribute;
        }

        public ITmfStateInterval getInterval() {
            return fInterval;
        }
    }

    /** File format version. Bump if the format changes */
    private static final int SNAPSHOT_FORMAT_VERSION = 1;

    private static final String SNAPSHOT_DIRECTORY = ".tc-states"; //$NON-NLS-1$
    private static final String FILE_SUFFIX = ".snapshot.json"; //$NON-NLS-1$

    private static final Gson GSON = Objects.requireNonNull(new GsonBuilder()
            .registerTypeAdapter(ITmfStateInterval.class, new TmfIntervalDeserializer())
            .registerTypeAdapter(ITmfStateInterval.class, new TmfIntervalSerializer())
            .create());

    @SerializedName("snapshot_format_version")
    @Since(1.0)
    private int fStateFormatVersion = SNAPSHOT_FORMAT_VERSION;

    @SerializedName("ssid")
    @Since(1.0)
    private String fSsid;

    @SerializedName("snapshot_version")
    @Since(1.0)
    private int fVersion;

    @SerializedName("timestamp")
    @Since(1.0)
    private int fTimestamp;


    @SerializedName("attributes")
    @Since(1.0)
    private Collection<AttributeAndInterval> fAttributes = Collections.emptyList();

    private final long fEndTime;

    private long fStartTime;

    /**
     * Get the format version
     *
     * @return the format
     */
    public int getStateFormatVersion() {
        return fStateFormatVersion;
    }

    /**
     * Get the state system ID
     *
     * @return the ssid
     */
    public String getSsid() {
        return fSsid;
    }

    /**
     * Get the states
     *
     * @return the states
     */
    @SuppressWarnings("null")
    public Map<List<String>, ITmfStateInterval> getStates() {
        return fAttributes.stream()
                .collect(
                        Collectors.toMap(
                                AttributeAndInterval::getPath,
                                AttributeAndInterval::getInterval));
    }

    /**
     * Clamping constructor. Builds a snapshot from a given state system and
     * timestamp.
     *
     * @param ss
     *            The state system for which to build the state dump
     * @param start
     *            The timestamp at which to query the state to dump
     *            @param end
     *            The timestamp at which the
     * @param version
     *            Version of the snapshot
     * @since 4.1
     */
    public StateSnapshot(ITmfStateSystem ss, long start, long end, int version) {
        fStartTime = start;
        fEndTime = end;
        List<ITmfStateInterval> fullQuery;
        fVersion = version;
        fSsid = String.valueOf(ss.getSSID());
        try {
            fullQuery = ss.queryFullState(start);
        } catch (StateSystemDisposedException e1) {
            fVersion = -1;
            return;
        }

        ImmutableList.Builder<@NonNull AttributeAndInterval> states = new ImmutableList.Builder<>();
        for (int quark = 0; quark < ss.getNbAttributes(); quark++) {
            String @NonNull [] fullAttributePathArray = ss.getFullAttributePathArray(quark);
            ITmfStateInterval interval = fullQuery.get(quark);
            long startTime = Math.max(interval.getStartTime(), fStartTime);
            /* Negative end time is used to prevent insertion of null future state */
            long endTime = interval.getEndTime() > fEndTime ? Long.MIN_VALUE : interval.getEndTime();
            interval = new TmfStateInterval(startTime, endTime, interval.getAttribute(), interval.getValue());
            states.add(new AttributeAndInterval(Arrays.asList(fullAttributePathArray), interval));
        }
        fAttributes = states.build();
    }

    /**
     * "Online" constructor. Builds a snapshot from a given state system and
     * timestamp.
     *
     * @param ss
     *            The state system for which to build the state dump
     * @param timestamp
     *            The timestamp at which to query the state to dump
     * @param version
     *            Version of the snapshot
     */
    public StateSnapshot(ITmfStateSystem ss, long timestamp, int version) {
        this(ss, timestamp, timestamp, version);
    }

    /**
     * Get the version of this snapshot. Can be used to consider if a snapshot
     * should be read or not if the analysis changed since it was written.
     *
     * @return The snapshot's version
     */
    public int getVersion() {
        return fVersion;
    }

    /**
     * Save this snapshot at the given location.
     *
     * @param parentPath
     *            The location where to save the snapshot file, usually in or close
     *            to its corresponding trace. It will be put under a Trace
     *            Compass-specific sub-directory.
     * @throws IOException
     *             If there are problems creating or writing to the target directory
     */
    public void write(Path parentPath) throws IOException {
        /* Create directory if it does not exist */
        Path sdPath = parentPath.resolve(SNAPSHOT_DIRECTORY);
        if (!sdPath.toFile().exists()) {
            Files.createDirectory(sdPath);
        }

        /* Create state dump file */
        String fileName = fSsid + FILE_SUFFIX;
        Path filePath = sdPath.resolve(fileName);
        if (filePath.toFile().exists()) {
            Files.delete(filePath);
        }
        Files.createFile(filePath);

        try (Writer bw = Files.newBufferedWriter(filePath, Charsets.UTF_8)) {
            String json = GSON.toJson(this);
            bw.write(json);
        }
    }

    /**
     * Retrieve a previously-saved snapshot.
     *
     * @param parentPath
     *            The expected location of the snapshot file. This is the parent
     *            path of the TC-specific subdirectory.
     * @param ssid
     *            The ID of the state system to retrieve
     * @return The corresponding de-serialized snapshot. Returns null if there are
     *         no snapshot for this state system ID (or no snapshot directory at
     *         all).
     */
    public static @Nullable StateSnapshot read(Path parentPath, String ssid) {
        /* Find the state dump directory */
        Path sdPath = parentPath.resolve(SNAPSHOT_DIRECTORY);
        if (!sdPath.toFile().isDirectory()) {
            return null;
        }

        /* Find the state dump file */
        String fileName = ssid + FILE_SUFFIX;
        Path filePath = sdPath.resolve(fileName);
        if (!filePath.toFile().exists()) {
            return null;
        }

        try (InputStreamReader in = new InputStreamReader(Files.newInputStream(filePath, StandardOpenOption.READ))) {
            BufferedReader bufReader = new BufferedReader(in);
            String json = bufReader.lines().collect(Collectors.joining("\n")); //$NON-NLS-1$
            return GSON.fromJson(json, StateSnapshot.class);
        } catch (IOException e) {
            Activator.getDefault().logError("Error reading snapshot " + parentPath + " " + ssid, e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }
}
