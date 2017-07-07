/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.snapshot.StateSnapshot;
import org.junit.Test;

/**
 * Tests for {@link StateSnapshot}.
 *
 * @author Alexandre Montplaisir
 */
public class SnapshotTest {

    /**
     * Test that a manually-written state snapshot is serialized then read
     * correctly.
     *
     * Double check by populating a state system with the values read.
     *
     * @throws IOException
     *             Failed to create the file
     */
    @Test
    public void testSimpleSnapshot() throws IOException {
        Path dir = null;
        try {
            dir = checkNotNull(Files.createTempDirectory("ss-serialization-test"));
            String ssid = "test-ssid";
            final int version = 0;

            IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend(ssid, 4);
            ITmfStateSystemBuilder ssb = StateSystemFactory.newStateSystem(backend);

            List<@NonNull List<@NonNull String>> attributes = Arrays.asList(Arrays.asList("Threads"),
                    Arrays.asList("Threads", "1000"),
                    Arrays.asList("Threads", "1000", "Status"),
                    Arrays.asList("Threads", "2000"),
                    Arrays.asList("Threads", "2000", "Status"),
                    Arrays.asList("Threads", "2000", "PPID"),
                    Arrays.asList("Threads", "2000", "air pressure"));

            Map<@NonNull List<@NonNull String>, @Nullable Object> expected = new LinkedHashMap<>();
            expected.put(attributes.get(0), null);
            expected.put(attributes.get(1), null);
            expected.put(attributes.get(2), null);
            expected.put(attributes.get(3), null);
            expected.put(attributes.get(4), null);
            expected.put(attributes.get(5), null);
            expected.put(attributes.get(6), null);
            populateSs(ssb, expected, 0);
            expected.put(attributes.get(2), "Running");
            expected.put(attributes.get(4), 1);
            expected.put(attributes.get(5), 1000L);
            expected.put(attributes.get(6), 101.3);
            populateSs(ssb, expected, 5);
            populateSs(ssb, expected, 10);
            ssb.closeHistory(100);
            StateSnapshot snapshot = new StateSnapshot(ssb, 6, version);
            snapshot.write(dir);

            StateSnapshot results = StateSnapshot.read(dir, ssid);
            assertNotNull(results);

            assertEquals(version, results.getVersion());
            assertEquals(ssid, results.getSsid());
            Map<List<String>, Object> resultStates = new LinkedHashMap<>();
            for (Entry<@NonNull List<@NonNull String>, @NonNull ITmfStateInterval> result : results.getStates().entrySet()) {
                ITmfStateInterval value = result.getValue();
                assertNotNull(String.valueOf(result.getKey()), value);
                resultStates.put(result.getKey(), value.getValue());
            }
            assertEquals(expected, resultStates);

        } finally {
            if (dir != null) {
                FileUtils.deleteQuietly(dir.toFile());
            }
        }
    }

    private static void populateSs(ITmfStateSystemBuilder ssb, Map<@NonNull List<@NonNull String>, @Nullable Object> expected, long start) {
        for (Entry<@NonNull List<@NonNull String>, @Nullable Object> entry : expected.entrySet()) {
            List<@NonNull String> key = Objects.requireNonNull(entry.getKey());
            int quark = ssb.getQuarkAbsoluteAndAdd(key.toArray(new String[0]));
            ssb.modifyAttribute(start, entry.getValue(), quark);
        }
    }
}
