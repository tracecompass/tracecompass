/*******************************************************************************
 * Copyright (c) 2013, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Marc-Andre Laperle - Move generation to traces folder
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.shared;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.eclipse.tracecompass.ctf.core.tests.CtfCoreTestPlugin;

/**
 * Generate a lttng trace (kernel or ust)
 *
 * @author Matthew Khouzam
 */
public class LttngTraceGenerator {

    private static final String metadataKernel = "/* CTF 1.8 */ \n" +
            "typealias integer { size = 8; align = 8; signed = false; } := uint8_t;\n" +
            "typealias integer { size = 16; align = 8; signed = false; } := uint16_t;\n" +
            "typealias integer { size = 32; align = 8; signed = false; } := uint32_t;\n" +
            "typealias integer { size = 64; align = 8; signed = false; } := uint64_t;\n" +
            "typealias integer { size = 32; align = 8; signed = false; } := unsigned long;\n" +
            "typealias integer { size = 5; align = 1; signed = false; } := uint5_t;\n" +
            "typealias integer { size = 27; align = 1; signed = false; } := uint27_t;\n" +
            "\n" +
            "trace {\n" +
            "   major = 1;\n" +
            "   minor = 8;\n" +
            "   uuid = \"11111111-1111-1111-1111-111111111111\";\n" +
            "   byte_order = le;\n" +
            "   packet.header := struct {\n" +
            "       uint32_t magic;\n" +
            "       uint8_t  uuid[16];\n" +
            "       uint32_t stream_id;\n" +
            "   };\n" +
            "};\n" +
            "\n" +
            "env {\n" +
            "   hostname = \"synthetic-host\";\n" +
            "   domain = \"kernel\";\n" +
            "   sysname = \"FakeLinux\";\n" +
            "   kernel_release = \"1.0\";\n" +
            "   kernel_version = \"Fake Os Synthetic Trace\";\n" +
            "   tracer_name = \"lttng-modules\";\n" +
            "   tracer_major = 2;\n" +
            "   tracer_minor = 1;\n" +
            "   tracer_patchlevel = 0;\n" +
            "};\n" +
            "\n" +
            "clock {\n" +
            "   name = monotonic;\n" +
            "   uuid = \"bbff68f0-c633-4ea1-92cd-bd11024ec4de\";\n" +
            "   description = \"Monotonic Clock\";\n" +
            "   freq = 1000000000; /* Frequency, in Hz */\n" +
            "   /* clock value offset from Epoch is: offset * (1/freq) */\n" +
            "   offset = 1368000272650993664;\n" +
            "};\n" +
            "\n" +
            "typealias integer {\n" +
            "   size = 27; align = 1; signed = false;\n" +
            "   map = clock.monotonic.value;\n" +
            "} := uint27_clock_monotonic_t;\n" +
            "\n" +
            "typealias integer {\n" +
            "   size = 32; align = 8; signed = false;\n" +
            "   map = clock.monotonic.value;\n" +
            "} := uint32_clock_monotonic_t;\n" +
            "\n" +
            "typealias integer {\n" +
            "   size = 64; align = 8; signed = false;\n" +
            "   map = clock.monotonic.value;\n" +
            "} := uint64_clock_monotonic_t;\n" +
            "\n" +
            "struct packet_context {\n" +
            "   uint64_clock_monotonic_t timestamp_begin;\n" +
            "   uint64_clock_monotonic_t timestamp_end;\n" +
            "   uint64_t content_size;\n" +
            "   uint64_t packet_size;\n" +
            "   unsigned long events_discarded;\n" +
            "   uint32_t cpu_id;\n" +
            "};\n" +
            "\n" +
            "struct event_header_compact {\n" +
            "   enum : uint5_t { compact = 0 ... 30, extended = 31 } id;\n" +
            "   variant <id> {\n" +
            "       struct {\n" +
            "           uint27_clock_monotonic_t timestamp;\n" +
            "       } compact;\n" +
            "       struct {\n" +
            "           uint32_t id;\n" +
            "           uint64_clock_monotonic_t timestamp;\n" +
            "       } extended;\n" +
            "   } v;\n" +
            "} align(8);\n" +
            "\n" +
            "struct event_header_large {\n" +
            "   enum : uint16_t { compact = 0 ... 65534, extended = 65535 } id;\n" +
            "   variant <id> {\n" +
            "       struct {\n" +
            "           uint32_clock_monotonic_t timestamp;\n" +
            "       } compact;\n" +
            "       struct {\n" +
            "           uint32_t id;\n" +
            "           uint64_clock_monotonic_t timestamp;\n" +
            "       } extended;\n" +
            "   } v;\n" +
            "} align(8);\n" +
            "\n" +
            "stream {\n" +
            "   id = 0;\n" +
            "   event.header := struct event_header_compact;\n" +
            "   packet.context := struct packet_context;\n" +
            "};\n" +
            "\n" +
            "event {\n" +
            "   name = sched_switch;\n" +
            "   id = 0;\n" +
            "   stream_id = 0;\n" +
            "   fields := struct {\n" +
            "       integer { size = 8; align = 8; signed = 1; encoding = UTF8; base = 10; } _prev_comm[16];\n" +
            "       integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _prev_tid;\n" +
            "       integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _prev_prio;\n" +
            "       integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _prev_state;\n" +
            "       integer { size = 8; align = 8; signed = 1; encoding = UTF8; base = 10; } _next_comm[16];\n" +
            "       integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _next_tid;\n" +
            "       integer { size = 32; align = 8; signed = 1; encoding = none; base = 10; } _next_prio;\n" +
            "   };\n" +
            "};\n" +
            "\n";

    private final List<String> fProcesses;
    private final long fDuration;
    private final long fNbEvents;
    private final int fNbChans;

    private final String metadata;

    private static final String[] sfProcesses = {
            "IDLE",
            "gnuplot",
            "starcraft 2:pt3",
            "bash",
            "smash",
            "thrash",
            "fireball",
            "Half-life 3",
            "ST: The game"
    };

    private static final String TRACES_DIRECTORY = "traces";
    private static final String TRACE_NAME = "synthetic-trace";

    /**
     * Main, not always needed
     *
     * @param args
     *            args
     */
    public static void main(String[] args) {
        // not using createTempFile as this is a directory
        String path = CtfCoreTestPlugin.getTemporaryDirPath() + File.separator + TRACE_NAME;
        generateLttngTrace(new File(path));
    }

    /**
     * Gets the name of the trace (top directory name)
     *
     * @return the name of the trace
     */
    public static String getName() {
        return TRACE_NAME;
    }

    /**
     * Get the path
     *
     * @return the path
     */
    public static String getPath() {
        CtfCoreTestPlugin plugin = CtfCoreTestPlugin.getDefault();
        if (plugin == null) {
            return null;
        }
        Path tracePath = Paths.get("..", "..", "ctf", "org.eclipse.tracecompass.ctf.core.tests", TRACES_DIRECTORY, TRACE_NAME);
        tracePath = tracePath.toAbsolutePath();
        File file = tracePath.toFile();
        generateLttngTrace(file);
        return file.getAbsolutePath();
    }

    /**
     * Generate a trace
     *
     * @param file
     *            the file to write the trace to
     */
    public static void generateLttngTrace(File file) {
        final int cpus = 25;
        LttngTraceGenerator gt = new LttngTraceGenerator(Integer.MAX_VALUE / 8L, 250000 / 8L, cpus);
        gt.writeTrace(file);
    }

    /**
     * Make a lttng trace
     *
     * @param duration
     *            the duration of the trace
     * @param events
     *            the number of events in a trace
     * @param nbChannels
     *            the number of channels in the trace
     */
    public LttngTraceGenerator(long duration, long events, int nbChannels) {
        this(duration, events, nbChannels, true);
    }

    /**
     * Make a lttng trace
     *
     * @param duration
     *            the duration of the trace
     * @param events
     *            the number of events in a trace
     * @param nbChannels
     *            the number of channels in the trace
     * @param isKernel
     *            true for kernel, false for ust
     */
    public LttngTraceGenerator(long duration, long events, int nbChannels, boolean isKernel) {
        fProcesses = Arrays.asList(sfProcesses);
        fDuration = duration;
        fNbEvents = events;
        fNbChans = nbChannels;
        metadata = isKernel ? metadataKernel : getMetadataUST();
    }

    /**
     * Write the trace to a file
     *
     * @param file
     *            the file to write the trace to
     */
    public void writeTrace(File file) {

        if (file.exists()) {
            deleteDirectory(file);
        }
        file.mkdir();

        File metadataFile = new File(file.getPath() + File.separator + "metadata");
        File[] streams = new File[fNbChans];
        FileChannel[] channels = new FileChannel[fNbChans];

        try {
            for (int i = 0; i < fNbChans; i++) {
                streams[i] = new File(file.getPath() + File.separator + "channel" + i);
                channels[i] = new FileOutputStream(streams[i]).getChannel();
            }
        } catch (FileNotFoundException e) {
        }
        // determine the number of events per channel
        long evPerChan = fNbEvents / fNbChans;
        final int evPerPacket = PacketWriter.CONTENT_SIZE / EventWriter.SIZE;
        long delta = (int) (fDuration / evPerChan);
        long offsetTime = 0;
        Random rndLost = new Random(1337);
        for (int chan = 0; chan < fNbChans; chan++) {
            int currentSpace = 0;
            ByteBuffer bb = ByteBuffer.allocate(65536);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            Random rnd = new Random(1337);
            int rnd0 = rnd.nextInt(fProcesses.size());
            String prevComm = fProcesses.get(rnd0);
            int prevPID = rnd0 + chan * fProcesses.size();
            if (rnd0 == 0) {
                prevPID = 0;
            }
            int prevPrio = 0;
            int prevPos = -1;
            int discarded = 0;
            int discardedTotal = 0;
            for (int eventNb = 0; eventNb < evPerChan; eventNb++) {
                if (EventWriter.SIZE > currentSpace) {
                    eventNb += discarded;
                }
                long ts = eventNb * delta + delta / (fNbChans + 1) * chan;

                int pos = rnd.nextInt((int) (fProcesses.size() * 1.5));
                if (pos >= fProcesses.size()) {
                    pos = 0;
                }
                while (pos == prevPos) {
                    pos = rnd.nextInt((int) (fProcesses.size() * 1.5));
                    if (pos >= fProcesses.size()) {
                        pos = 0;
                    }
                }
                String nextComm = fProcesses.get(pos);
                int nextPID = pos + fProcesses.size() * chan;
                if (pos == 0) {
                    nextPID = 0;
                }
                int nextPrio = 0;
                if (EventWriter.SIZE > currentSpace) {
                    // pad to end
                    for (int i = 0; i < currentSpace; i++) {
                        bb.put((byte) 0x00);
                    }
                    // write new packet
                    PacketWriter pw = new PacketWriter(bb);
                    long tsBegin = ts;
                    offsetTime = ts;
                    int eventCount = Math.min(evPerPacket, (int) evPerChan - eventNb);
                    discarded = rndLost.nextInt(10 * fNbChans) == 0 ? rndLost.nextInt(evPerPacket) : 0;
                    discarded = Math.min(discarded, (int) evPerChan - eventNb - eventCount);
                    discardedTotal += discarded;
                    long tsEnd = (eventNb + eventCount + discarded) * delta;
                    pw.writeNewHeader(tsBegin, tsEnd, chan, eventCount, discardedTotal);
                    currentSpace = PacketWriter.CONTENT_SIZE;
                }
                EventWriter ew = new EventWriter(bb);
                int prev_state = rnd.nextInt(100);
                if (prev_state != 0) {
                    prev_state = 1;
                }
                final long shrunkenTimestamp = ts - offsetTime;
                final int tsMask = (1 << 27) - 1;
                if (shrunkenTimestamp > ((1 << 27) + tsMask)) {
                    /* allow only one compact timestamp overflow per packet */
                    throw new IllegalStateException("Invalid timestamp overflow:" + shrunkenTimestamp);
                }
                final int clampedTs = (int) (ts & tsMask);
                int evSize = ew.writeEvent(clampedTs, prevComm, prevPID, prevPrio, prev_state, nextComm, nextPID, nextPrio);
                currentSpace -= evSize;
                prevComm = nextComm;
                prevPID = nextPID;
                prevPrio = nextPrio;
                if (bb.position() > 63000) {
                    writeToDisk(channels, chan, bb);
                }
            }
            for (int i = 0; i < currentSpace; i++) {
                bb.put((byte) 0x00);
            }
            writeToDisk(channels, chan, bb);
            try {
                channels[chan].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = new FileOutputStream(metadataFile);) {
            fos.write(metadata.getBytes());
        } catch (IOException e) {
        }
    }

    private static void deleteDirectory(File directory) {
        try {
            Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // If a file failed to delete, it's more useful to throw
                    // this instead
                    if (exc != null) {
                        throw exc;
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToDisk(FileChannel[] channels, int chan, ByteBuffer bb) {
        try {
            bb.flip();
            channels[chan].write(bb);
            bb.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getMetadataUST() {
        String metadata = metadataKernel.replace("\"kernel\"", "\"ust\"");
        return metadata.replace("lttng-modules", "lttng-ust");
    }

    private class EventWriter {
        public static final int SIZE = 4 + // timestamp
                16 + // prev_comm
                4 + // prev_tid
                4 + // prev_prio
                4 + // prev_state
                16 + // current_comm
                4 + // next_tid
                4; // next_prio
        private final ByteBuffer data;

        public EventWriter(ByteBuffer bb) {
            data = bb;
        }

        public int writeEvent(int ts, String prev_comm, int prev_tid, int prev_prio, int prev_state, String next_comm, int next_tid, int next_prio) {
            byte[] bOut = new byte[16];
            byte[] bIn = new byte[16];
            byte[] temp = prev_comm.getBytes();
            for (int i = 0; i < Math.min(temp.length, 16); i++) {
                bOut[i] = temp[i];
            }
            temp = next_comm.getBytes();
            for (int i = 0; i < Math.min(temp.length, 16); i++) {
                bIn[i] = temp[i];
            }

            int timestamp = ts << 5;

            data.putInt(timestamp);
            data.put(bOut);
            data.putInt(prev_tid);
            data.putInt(prev_prio);
            data.putInt(prev_state);
            data.put(bIn);
            data.putInt(next_tid);
            data.putInt(next_prio);
            return SIZE;
        }

    }

    private class PacketWriter {
        private static final int SIZE = 4096;
        private static final int HEADER_SIZE = 64;
        private static final int CONTENT_SIZE = SIZE - HEADER_SIZE;

        private final ByteBuffer data;

        public PacketWriter(ByteBuffer bb) {
            data = bb;
        }

        public void writeNewHeader(long tsBegin, long tsEnd, int cpu, int eventCount, int discarded) {
            final int magicLE = 0xC1FC1FC1;
            byte uuid[] = {
                    0x11, 0x11, 0x11, 0x11,
                    0x11, 0x11, 0x11, 0x11,
                    0x11, 0x11, 0x11, 0x11,
                    0x11, 0x11, 0x11, 0x11 };
            // packet header

            // magic number 4
            data.putInt(magicLE);
            // uuid 16
            data.put(uuid);
            // stream ID 4
            data.putInt(0);

            // packet context
            // timestamp_begin 8
            data.putLong(tsBegin);

            // timestamp_end 8
            data.putLong(tsEnd);

            // content_size 8
            data.putLong((eventCount * EventWriter.SIZE + HEADER_SIZE) * 8);

            // packet_size 8
            data.putLong((SIZE) * 8);

            // events_discarded 4
            data.putInt(discarded);

            // cpu_id 4
            data.putInt(cpu);

        }

    }

}
