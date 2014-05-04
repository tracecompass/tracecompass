package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.internal.ctf.core.event.types.composite.EventHeaderCompactDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.types.composite.EventHeaderDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.types.composite.EventHeaderLargeDeclaration;
import org.junit.Before;
import org.junit.Test;

/**
 * Event header declaration tests
 *
 * @author Matthew Khouzam
 *
 */
public class EventHeaderDeclarationTest {

    private static final int ID = 2222;
    private static final int TIMESTAMP = 1000;
    private static final int VALID_LARGE = 1;
    private static final int VALID_COMPACT = 0;

    private final List<StructDeclaration> declarations = new ArrayList<>();

    /**
     * Setup
     */
    @Before
    public void init() {
        declarations.clear();

        /**
         * do not reflow
         *
         * <pre>
         * struct event_header_compact {
         *     enum : uint5_t { compact = 0 ... 30, extended = 31 } id;
         *     variant <id> {
         *         struct {
         *             uint27_clock_monotonic_t timestamp;
         *         } compact;
         *         struct {
         *             uint32_t id;
         *             uint64_clock_monotonic_t timestamp;
         *         } extended;
         *     } v;
         * } align(8);
         * </pre>
         */

        StructDeclaration base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_5B_DECL));
        VariantDeclaration variantV = new VariantDeclaration();
        StructDeclaration compact = new StructDeclaration(8);
        compact.addField("timestamp", IntegerDeclaration.UINT_27B_DECL);
        variantV.addField("compact", compact);
        StructDeclaration large = new StructDeclaration(8);
        large.addField("id", IntegerDeclaration.UINT_32B_DECL);
        large.addField("timestamp", IntegerDeclaration.UINT_64B_DECL);
        variantV.addField("extended", large);
        base.addField("v", variantV);
        declarations.add(base);

        /**
         * Do not reflow
         *
         * <pre>
         * struct event_header_large {
         *     enum : uint16_t { compact = 0 ... 65534, extended = 65535 } id;
         *     variant <id> {
         *         struct {
         *             uint32_clock_monotonic_t timestamp;
         *         } compact;
         *         struct {
         *             uint32_t id;
         *             uint64_clock_monotonic_t timestamp;
         *         } extended;
         *     } v;
         * } align(8);
         * </pre>
         */

        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_16B_DECL));
        variantV = new VariantDeclaration();
        compact = new StructDeclaration(8);
        compact.addField("timestamp", IntegerDeclaration.UINT_32B_DECL);
        variantV.addField("compact", compact);
        large = new StructDeclaration(8);
        large.addField("id", IntegerDeclaration.UINT_32B_DECL);
        large.addField("timestamp", IntegerDeclaration.UINT_64B_DECL);
        variantV.addField("extended", large);
        base.addField("v", variantV);
        declarations.add(base);

        // bad - well, sounds nice though
        base = new StructDeclaration(8);
        base.addField("potato salad", new FloatDeclaration(8, 8, ByteOrder.BIG_ENDIAN, 8));
        base.addField("bbq ribs", new FloatDeclaration(8, 8, ByteOrder.BIG_ENDIAN, 8));
        declarations.add(base);
        // bad
        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_16B_DECL));
        base.addField("v", new FloatDeclaration(8, 8, ByteOrder.BIG_ENDIAN, 8));
        declarations.add(base);
        // bad
        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_5B_DECL));
        base.addField("v", new FloatDeclaration(8, 8, ByteOrder.BIG_ENDIAN, 8));
        declarations.add(base);
        // bad
        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_5B_DECL));
        variantV = new VariantDeclaration();
        compact = new StructDeclaration(8);
        compact.addField("timestamp", IntegerDeclaration.UINT_27B_DECL);
        variantV.addField("compact1", compact);
        large = new StructDeclaration(8);
        large.addField("id", IntegerDeclaration.UINT_32B_DECL);
        large.addField("timestamp", IntegerDeclaration.UINT_64B_DECL);
        variantV.addField("extended", large);
        base.addField("v", variantV);
        declarations.add(base);

        // bad
        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_5B_DECL));
        variantV = new VariantDeclaration();
        compact = new StructDeclaration(8);
        compact.addField("timestamp", IntegerDeclaration.UINT_27B_DECL);
        variantV.addField("compact", compact);
        large = new StructDeclaration(8);
        large.addField("id", IntegerDeclaration.UINT_32B_DECL);
        large.addField("timestamp1", IntegerDeclaration.UINT_64B_DECL);
        variantV.addField("extended", large);
        base.addField("v", variantV);
        declarations.add(base);

        // bad
        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_5B_DECL));
        variantV = new VariantDeclaration();
        compact = new StructDeclaration(8);
        compact.addField("timestamp", IntegerDeclaration.UINT_27B_DECL);
        variantV.addField("compact", compact);
        large = new StructDeclaration(8);
        large.addField("id", IntegerDeclaration.UINT_32B_DECL);
        large.addField("timestamp", new StringDeclaration());
        variantV.addField("extended", large);
        base.addField("v", variantV);
        declarations.add(base);

        // bad
        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_5B_DECL));
        variantV = new VariantDeclaration();
        compact = new StructDeclaration(8);
        compact.addField("timestamp", IntegerDeclaration.UINT_27B_DECL);
        variantV.addField("compact", compact);
        variantV.addField("surprise!", compact);
        large = new StructDeclaration(8);
        large.addField("id", IntegerDeclaration.UINT_32B_DECL);
        large.addField("timestamp", new StringDeclaration());
        variantV.addField("extended", large);
        base.addField("v", variantV);
        declarations.add(base);

        // bad
        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.UINT_16B_DECL));
        variantV = new VariantDeclaration();
        compact = new StructDeclaration(8);
        compact.addField("timestamp", IntegerDeclaration.UINT_27B_DECL);
        variantV.addField("compact", compact);
        variantV.addField("surprise!", compact);
        large = new StructDeclaration(8);
        large.addField("id", IntegerDeclaration.UINT_32B_DECL);
        large.addField("timestamp", new StringDeclaration());
        variantV.addField("extended", large);
        base.addField("v", variantV);
        declarations.add(base);
        // bad
        base = new StructDeclaration(8);
        base.addField("id", new FloatDeclaration(8, 8, ByteOrder.BIG_ENDIAN, 8));
        base.addField("v", new FloatDeclaration(8, 8, ByteOrder.BIG_ENDIAN, 8));
        declarations.add(base);
        // bad
        base = new StructDeclaration(8);
        base.addField("id", IntegerDeclaration.INT_32B_DECL);
        base.addField("timestamp", IntegerDeclaration.INT_32B_DECL);
        declarations.add(base);
        // bad
        base = new StructDeclaration(8);
        base.addField("id", new EnumDeclaration(IntegerDeclaration.INT_8_DECL));
        base.addField("timestamp", IntegerDeclaration.INT_32B_DECL);
        declarations.add(base);
    }

    /**
     * Validate a compact declaration
     */
    @Test
    public void validateCompact() {
        assertEquals(true, EventHeaderCompactDeclaration.isCompactEventHeader(declarations.get(VALID_COMPACT)));
    }

    /**
     * Fail if it validates
     */
    @Test
    public void validateCompactFail() {
        for (int i = 0; i < declarations.size(); i++) {
            if (i == VALID_COMPACT) {
                continue;
            }
            assertEquals(false, EventHeaderCompactDeclaration.isCompactEventHeader(declarations.get(i)));
        }
    }

    /**
     * Validate a large declaration
     */
    @Test
    public void validateLarge() {
        assertEquals(true, EventHeaderLargeDeclaration.isLargeEventHeader(declarations.get(VALID_LARGE)));
    }

    /**
     * Fail if it validates
     */
    @Test
    public void validateLargeFail() {
        for (int i = 0; i < declarations.size(); i++) {
            if (i == VALID_LARGE) {
                continue;
            }
            assertEquals(false, EventHeaderLargeDeclaration.isLargeEventHeader(declarations.get(i)));
        }
    }

    /**
     * Test an compact compact header
     *
     * @throws CTFReaderException
     *             if {@link BitBuffer} is null
     */
    @Test
    public void testCompactCompact() throws CTFReaderException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putInt(0x80000042);
        byte[] validCompact1 = buffer.array();

        EventHeaderCompactDeclaration decl = new EventHeaderCompactDeclaration(ByteOrder.BIG_ENDIAN);
        final ByteBuffer input = ByteBuffer.wrap(validCompact1);
        assertNotNull(input);
        EventHeaderDefinition def = decl.createDefinition(null, "bla", new BitBuffer(input));
        assertNotNull(def);
        assertEquals(16, def.getId());
        assertEquals(0x42, def.getTimestamp());
    }

    /**
     * Test an extended compact header
     *
     * @throws CTFReaderException
     *             if {@link BitBuffer} is null
     */
    @Test
    public void testCompactExtended() throws CTFReaderException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put((byte) 0xFF);
        buffer.putInt(ID);
        buffer.putLong(TIMESTAMP);
        byte[] validCompact2 = buffer.array();

        EventHeaderCompactDeclaration decl = new EventHeaderCompactDeclaration(ByteOrder.BIG_ENDIAN);
        final ByteBuffer input = ByteBuffer.wrap(validCompact2);
        assertNotNull(input);
        EventHeaderDefinition def = decl.createDefinition(null, "bla", new BitBuffer(input));
        assertNotNull(def);
        assertEquals(ID, def.getId());
        assertEquals(TIMESTAMP, def.getTimestamp());
    }

    /**
     * Test an compact large header
     *
     * @throws CTFReaderException
     *             if {@link BitBuffer} is null
     */
    @Test
    public void testLargeCompact() throws CTFReaderException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putShort((short) ID);
        buffer.putInt(TIMESTAMP);
        byte[] validLarge1 = buffer.array();

        EventHeaderLargeDeclaration decl = new EventHeaderLargeDeclaration(ByteOrder.BIG_ENDIAN);
        final ByteBuffer input = ByteBuffer.wrap(validLarge1);
        assertNotNull(input);
        EventHeaderDefinition def = decl.createDefinition(null, "bla", new BitBuffer(input));
        assertNotNull(def);
        assertEquals(ID, def.getId());
        assertEquals(TIMESTAMP, def.getTimestamp());
        assertEquals(ID, ((IntegerDefinition) def.getDefinition("id")).getValue());
        assertEquals(TIMESTAMP, ((IntegerDefinition) def.getDefinition("timestamp")).getValue());
    }

    /**
     * Test an large large header
     *
     * @throws CTFReaderException
     *             if {@link BitBuffer} is null
     */
    @Test
    public void testLargeExtended() throws CTFReaderException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putShort((short) -1);
        buffer.putInt(ID);
        buffer.putLong(TIMESTAMP);
        byte[] validLarge2 = buffer.array();

        EventHeaderLargeDeclaration decl = new EventHeaderLargeDeclaration(ByteOrder.BIG_ENDIAN);
        final ByteBuffer input = ByteBuffer.wrap(validLarge2);
        assertNotNull(input);
        EventHeaderDefinition def = decl.createDefinition(null, "bla", new BitBuffer(input));
        assertNotNull(def);
        assertEquals(ID, def.getId());
        assertEquals(TIMESTAMP, def.getTimestamp());
        assertEquals(ID, ((IntegerDefinition) def.getDefinition("id")).getValue());
        assertEquals(TIMESTAMP, ((IntegerDefinition) def.getDefinition("timestamp")).getValue());
    }

    /**
     * Test maximum sizes, make sure they don't change unannounced
     */
    @Test
    public void testMaxSizes() {
        assertEquals(112, (new EventHeaderLargeDeclaration(ByteOrder.BIG_ENDIAN)).getMaximumSize());
        assertEquals(104, (new EventHeaderCompactDeclaration(ByteOrder.BIG_ENDIAN)).getMaximumSize());
    }
}
