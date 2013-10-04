/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.synthetictraces;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Generate a kernel trace
 *
 * @author Matthew Khouzam
 */
public class LttngKernelTraceGenerator {

    private static final String metadata = "/* CTF 1.8 */ \n" +
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
    private File fPath;

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


    private static final String DIRECTORY_NAME = "synthetic-trace";
    // not using createTempFile as this is a directory
    private static final String PATH = System.getProperty("java.io.tmpdir") + File.separator + DIRECTORY_NAME;

    /**
     * Main, not always needed
     *
     * @param args
     *            args
     */
    public static void main(String[] args) {
        generateLttngKernelTrace();
    }

    /**
     * Gets the name of the trace (top directory name)
     *
     * @return the name of the trace
     */
    public static String getName() {
        return DIRECTORY_NAME;
    }

    /**
     * Get the path
     *
     * @return the path
     */
    public static String getPath() {
        final File file = new File(PATH);
        if (!file.exists()) {
            generateLttngKernelTrace();
        }
        return file.getAbsolutePath();
    }

    /**
     * Generate a trace
     */
    public static void generateLttngKernelTrace() {
        final int cpus = 25;
        LttngKernelTraceGenerator gt = new LttngKernelTraceGenerator(2l * Integer.MAX_VALUE - 100, 500000, cpus);
        gt.writeTrace(PATH);
    }

    /**
     * Make a kernel trace
     *
     * @param duration
     *            the duration of the trace
     * @param events
     *            the number of events in a trace
     * @param nbChannels
     *            the number of channels in the trace
     */
    public LttngKernelTraceGenerator(long duration, long events, int nbChannels) {
        fProcesses = Arrays.asList(sfProcesses);
        fDuration = duration;
        fNbEvents = events;
        fNbChans = nbChannels;
    }

    /**
     * Write the trace to a file
     *
     * @param path
     *            the path to write the trace to
     */
    public void writeTrace(String path) {
        fPath = new File(path);

        if (!fPath.exists()) {
            fPath.mkdir();
        } else {
            if (fPath.isFile()) {
                fPath.delete();
                fPath.mkdir();
            } else {
                // the ctf parser doesn't recurse, so we don't need to.
                final File[] listFiles = fPath.listFiles();
                for (File child : listFiles) {
                    child.delete();
                }
            }
        }

        fPath.deleteOnExit();
        File metadataFile = new File(fPath.getPath() + File.separator + "metadata");
        metadataFile.deleteOnExit();
        File[] streams = new File[fNbChans];
        FileChannel[] channels = new FileChannel[fNbChans];
        FileOutputStream fos = null;

        try {
            for (int i = 0; i < fNbChans; i++) {
                streams[i] = new File(fPath.getPath() + File.separator + "channel" + i);
                channels[i] = new FileOutputStream(streams[i]).getChannel();
                streams[i].deleteOnExit();
            }
        } catch (FileNotFoundException e) {
        }
        // determine the number of events per channel
        long evPerChan = fNbEvents / fNbChans;
        int delta = (int) (fDuration / evPerChan);
        int offsetTime = 0;
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
            for (int eventNb = 0; eventNb < evPerChan; eventNb++) {
                int ts = eventNb * delta + delta / (fNbChans + 1) * chan;

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
                    int tsBegin = ts;
                    offsetTime = ts;
                    int tsEnd = (eventNb + (PacketWriter.SIZE / EventWriter.SIZE)) * delta + 1;
                    pw.writeNewHeader(tsBegin, tsEnd, chan);
                    currentSpace = PacketWriter.CONTENT_SIZE;
                }
                EventWriter ew = new EventWriter(bb);
                int prev_state = rnd.nextInt(100);
                if (prev_state != 0) {
                    prev_state = 1;
                }
                final int shrunkenTimestamp = ts - offsetTime;
                final int tsMask = (1 << 27) - 1;
                if (shrunkenTimestamp > ((1 << 27) + tsMask)) {
                    new Object();
                    System.err.println("PROBLEM");
                }
                final int clampedTs = ts & tsMask;
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
        try {
            fos = new FileOutputStream(metadataFile);
            fos.write(metadata.getBytes());
            fos.close();
        } catch (IOException e) {
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

    private class EventWriter {
        public static final int SIZE =
                4 +  // timestamp
                16 + // prev_comm
                4 +  // prev_tid
                4 +  // prev_prio
                4 +  // prev_state
                16 + // current_comm
                4 +  // next_tid
                4;   // next_prio
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

        public void writeNewHeader(int tsBegin, int tsEnd, int cpu) {
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
            data.putLong((CONTENT_SIZE / EventWriter.SIZE * EventWriter.SIZE + HEADER_SIZE) * 8);

            // packet_size 8
            data.putLong((SIZE) * 8);

            // events_discarded 4
            data.putInt(0);

            // cpu_id 4
            data.putInt(cpu);

        }

    }



}
