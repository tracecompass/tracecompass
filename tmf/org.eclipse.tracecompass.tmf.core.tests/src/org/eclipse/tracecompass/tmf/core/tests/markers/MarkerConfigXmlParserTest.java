/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.markers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerConfigXmlParser;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSegment;
import org.eclipse.tracecompass.internal.tmf.core.markers.MarkerSet;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.SplitMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.WeightedMarker;
import org.junit.Test;

import com.google.common.collect.Range;

/**
 * Tests for class MarkerConfigXmlParser
 */
public class MarkerConfigXmlParserTest {

    private static final String XML_CONTENT =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"10\" unit=\"ms\" range=\"0..4095\" offset=\"0\">" +
            "      <submarker name=\"Submarker A-1-1\" label=\"A-1-1 %d\" id=\"submarker.a.1.1\" color=\"#000000\" range=\"0..99\">" +
            "        <submarker name=\"Submarker A-1-1-1\" label=\"A-1-1-1 %d\" id=\"submarker.a.1.1.1\" range=\"0..9\">" +
            "        </submarker>" +
            "      </submarker>" +
            "    </marker>" +
            "    <marker name=\"Marker A-2\" label=\"A-2 %d\" id=\"marker.a.2\" referenceid=\"ref.a.2\" color=\"#ff0000\" period=\"20\" unit=\"us\" range=\"0..\" offset=\"0\">" +
            "    </marker>" +
            "    <marker name=\"Marker A-3\" label=\"A-3 %d\" id=\"marker.a.3\" referenceid=\"ref.a.3\" color=\"#00ff00\" period=\"2.5\" unit=\"ns\" range=\"0..\" offset=\"0\">" +
            "      <segments name=\"Submarker A-3-1\">" +
            "        <segment label=\"A-3-1-a %d\" id=\"marker.a.3.1.a\" color=\"#aaaaaa\" length=\"1\"/>" +
            "        <segment label=\"A-3-1-b %d\" id=\"marker.a.3.1.b\" color=\"#bbbbbb\" length=\"2\"/>" +
            "      </segments>" +
            "    </marker>" +
            "  </marker-set>" +
            "  <marker-set name=\"Set B\" id=\"set.b\">" +
            "    <marker name=\"Marker B-1\" label=\"B-1 %d\" id=\"marker.b.1\" referenceid=\"ref.b.1\" color=\"#010203\" period=\"1000\" unit=\"cycles\" range=\"1..\" offset=\"5\">" +
            "      <submarker name=\"Submarker B-1-1\" label=\"B-1-1 %d\" id=\"submarker.b.1.1\" range=\"1..10\">" +
            "      </submarker>" +
            "      <submarker name=\"Submarker B-1-2\" label=\"B-1-2 %d\" id=\"submarker.b.1.2\" range=\"1..100\">" +
            "      </submarker>" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String XML_CONTENT_INVALID_PERIOD_STRING =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"abc\" unit=\"ms\" range=\"0..4095\" offset=\"0\">" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String XML_CONTENT_INVALID_PERIOD_ZERO =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"0\" unit=\"ms\" range=\"0..4095\" offset=\"0\">" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String XML_CONTENT_INVALID_PERIOD_NEGATIVE =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"-1\" unit=\"ms\" range=\"0..4095\" offset=\"0\">" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String XML_CONTENT_INVALID_UNIT =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"10\" unit=\"qwerty\" range=\"0..4095\" offset=\"0\">" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String XML_CONTENT_INVALID_RANGE =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"10\" unit=\"ms\" range=\"4095..0\" offset=\"0\">" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";
    private static final String XML_CONTENT_INVALID_OFFSET =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"10\" unit=\"ms\" range=\"0..4095\" offset=\"abc\">" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String XML_CONTENT_INVALID_SUBMARKER_RANGE =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"10\" unit=\"ms\" range=\"4095..0\" offset=\"0\">" +
            "      <submarker name=\"Submarker A-1-1\" label=\"A-1-1 %d\" id=\"submarker.a.1.1\" color=\"#000000\" range=\"0..\"/>" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String XML_CONTENT_INVALID_SEGMENT_COUNT =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"10\" unit=\"ms\" range=\"4095..0\" offset=\"0\">" +
            "      <segments name=\"Submarker A-1-1\">" +
            "      </segments>" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    private static final String XML_CONTENT_INVALID_SEGMENT_LENGTH =
            "<marker-sets>" +
            "  <marker-set name=\"Set A\" id=\"set.a\">" +
            "    <marker name=\"Marker A-1\" label=\"A-1 %d\" id=\"marker.a.1\" referenceid=\"ref.a.1\" color=\"#0000ff\" period=\"10\" unit=\"ms\" range=\"4095..0\" offset=\"0\">" +
            "      <segments name=\"Submarker A-1-1\">" +
            "        <segment label=\"A-1-1-a %d\" id=\"marker.a.1.1.a\" color=\"#aaaaaa\" length=\"0\"/>" +
            "      </segments>" +
            "    </marker>" +
            "  </marker-set>" +
            "</marker-sets>";

    /**
     * Test the parse method
     *
     * @throws IOException if an exception occurs
     */
    @Test
    public void testParse() throws IOException {
        File file = File.createTempFile("markers", "xml");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(XML_CONTENT);
            fw.flush();

            List<MarkerSet> markerSets = MarkerConfigXmlParser.parse(file.getAbsolutePath());
            assertEquals(2, markerSets.size());

            MarkerSet setA = markerSets.get(0);
            assertEquals("Set A", setA.getName());
            assertEquals("set.a", setA.getId());
            assertEquals(3, setA.getMarkers().size());

            PeriodicMarker markerA1 = (PeriodicMarker) setA.getMarkers().get(0);
            assertEquals("Marker A-1", markerA1.getName());
            assertEquals("A-1 %d", markerA1.getLabel());
            assertEquals("marker.a.1", markerA1.getId());
            assertEquals("ref.a.1", markerA1.getReferenceId());
            assertEquals("#0000ff", markerA1.getColor());
            assertEquals(10.0, markerA1.getPeriod(), 0);
            assertEquals("ms", markerA1.getUnit());
            assertEquals(Range.closed(0L, 4095L), markerA1.getRange());
            assertEquals(0, markerA1.getOffset());
            assertEquals(1, markerA1.getSubMarkers().size());

            SplitMarker submarkerA11 = (SplitMarker) markerA1.getSubMarkers().get(0);
            assertEquals("Submarker A-1-1", submarkerA11.getName());
            assertEquals("A-1-1 %d", submarkerA11.getLabel());
            assertEquals("submarker.a.1.1", submarkerA11.getId());
            assertEquals("#000000", submarkerA11.getColor());
            assertEquals(Range.closed(0L, 99L), submarkerA11.getRange());
            assertEquals(1, submarkerA11.getSubMarkers().size());

            SplitMarker submarkerA111 = (SplitMarker) submarkerA11.getSubMarkers().get(0);
            assertEquals("Submarker A-1-1-1", submarkerA111.getName());
            assertEquals("A-1-1-1 %d", submarkerA111.getLabel());
            assertEquals("submarker.a.1.1.1", submarkerA111.getId());
            assertEquals("#000000", submarkerA111.getColor());
            assertEquals(Range.closed(0L, 9L), submarkerA111.getRange());
            assertEquals(0, submarkerA111.getSubMarkers().size());

            PeriodicMarker markerA2 = (PeriodicMarker) setA.getMarkers().get(1);
            assertEquals("Marker A-2", markerA2.getName());
            assertEquals("A-2 %d", markerA2.getLabel());
            assertEquals("marker.a.2", markerA2.getId());
            assertEquals("ref.a.2", markerA2.getReferenceId());
            assertEquals("#ff0000", markerA2.getColor());
            assertEquals(20.0, markerA2.getPeriod(), 0);
            assertEquals("us", markerA2.getUnit());
            assertEquals(Range.atLeast(0L), markerA2.getRange());
            assertEquals(0, markerA2.getOffset());
            assertEquals(0, markerA2.getSubMarkers().size());

            PeriodicMarker markerA3 = (PeriodicMarker) setA.getMarkers().get(2);
            assertEquals("Marker A-3", markerA3.getName());
            assertEquals("A-3 %d", markerA3.getLabel());
            assertEquals("marker.a.3", markerA3.getId());
            assertEquals("ref.a.3", markerA3.getReferenceId());
            assertEquals("#00ff00", markerA3.getColor());
            assertEquals(2.5, markerA3.getPeriod(), 0);
            assertEquals("ns", markerA3.getUnit());
            assertEquals(Range.atLeast(0L), markerA3.getRange());
            assertEquals(0, markerA3.getOffset());
            assertEquals(1, markerA3.getSubMarkers().size());

            WeightedMarker submarkerA31 = (WeightedMarker) markerA3.getSubMarkers().get(0);
            assertEquals("Submarker A-3-1", submarkerA31.getName());
            assertEquals(0, submarkerA31.getSubMarkers().size());
            assertEquals(2, submarkerA31.getSegments().size());

            MarkerSegment submarkerA31a = submarkerA31.getSegments().get(0);
            assertEquals(null, submarkerA31a.getName());
            assertEquals("A-3-1-a %d", submarkerA31a.getLabel());
            assertEquals("marker.a.3.1.a", submarkerA31a.getId());
            assertEquals("#aaaaaa", submarkerA31a.getColor());
            assertEquals(1, submarkerA31a.getLength());
            assertEquals(0, submarkerA31a.getSubMarkers().size());

            MarkerSegment submarkerA31b = submarkerA31.getSegments().get(1);
            assertEquals(null, submarkerA31b.getName());
            assertEquals("A-3-1-b %d", submarkerA31b.getLabel());
            assertEquals("marker.a.3.1.b", submarkerA31b.getId());
            assertEquals("#bbbbbb", submarkerA31b.getColor());
            assertEquals(2, submarkerA31b.getLength());
            assertEquals(0, submarkerA31b.getSubMarkers().size());

            MarkerSet setB = markerSets.get(1);
            assertEquals("Set B", setB.getName());
            assertEquals("set.b", setB.getId());
            assertEquals(1, setB.getMarkers().size());

            PeriodicMarker markerB1 = (PeriodicMarker) setB.getMarkers().get(0);
            assertEquals("Marker B-1", markerB1.getName());
            assertEquals("B-1 %d", markerB1.getLabel());
            assertEquals("marker.b.1", markerB1.getId());
            assertEquals("ref.b.1", markerB1.getReferenceId());
            assertEquals("#010203", markerB1.getColor());
            assertEquals(1000.0, markerB1.getPeriod(), 0);
            assertEquals("cycles", markerB1.getUnit());
            assertEquals(Range.atLeast(1L), markerB1.getRange());
            assertEquals(5, markerB1.getOffset());
            assertEquals(2, markerB1.getSubMarkers().size());

            SplitMarker submarkerB11 = (SplitMarker) markerB1.getSubMarkers().get(0);
            assertEquals("Submarker B-1-1", submarkerB11.getName());
            assertEquals("submarker.b.1.1", submarkerB11.getId());
            assertEquals("#010203", submarkerB11.getColor());
            assertEquals(Range.closed(1L, 10L), submarkerB11.getRange());
            assertEquals(0, submarkerB11.getSubMarkers().size());

            SplitMarker submarkerB12 = (SplitMarker) markerB1.getSubMarkers().get(1);
            assertEquals("Submarker B-1-2", submarkerB12.getName());
            assertEquals("submarker.b.1.2", submarkerB12.getId());
            assertEquals("#010203", submarkerB12.getColor());
            assertEquals(Range.closed(1L, 100L), submarkerB12.getRange());
            assertEquals(0, submarkerB12.getSubMarkers().size());

        } finally {
            file.delete();
        }
    }

    /**
     * Test the parse method with a non-existing file
     */
    @Test
    public void testParseInvalidFile() {
        File file = new File("invalid");
        List<MarkerSet> markerSets = MarkerConfigXmlParser.parse(file.getAbsolutePath());
        assertEquals(0, markerSets.size());
    }

    /**
     * Test the parse method with invalid content
     *
     * @throws IOException if an exception occurs
     */
    @Test
    public void testParseInvalidContent() throws IOException {
        testParseInvalidContent(XML_CONTENT_INVALID_PERIOD_STRING);
        testParseInvalidContent(XML_CONTENT_INVALID_PERIOD_ZERO);
        testParseInvalidContent(XML_CONTENT_INVALID_PERIOD_NEGATIVE);
        testParseInvalidContent(XML_CONTENT_INVALID_UNIT);
        testParseInvalidContent(XML_CONTENT_INVALID_RANGE);
        testParseInvalidContent(XML_CONTENT_INVALID_OFFSET);
        testParseInvalidContent(XML_CONTENT_INVALID_SUBMARKER_RANGE);
        testParseInvalidContent(XML_CONTENT_INVALID_SEGMENT_COUNT);
        testParseInvalidContent(XML_CONTENT_INVALID_SEGMENT_LENGTH);
    }

    private static void testParseInvalidContent(String content) throws IOException {
        File file = File.createTempFile("markers", "xml");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
            fw.flush();

            List<MarkerSet> markerSets = MarkerConfigXmlParser.parse(file.getAbsolutePath());
            assertEquals(0, markerSets.size());

        } finally {
            file.delete();
        }
    }

    /**
     * Test that the definitions from the extension point are loaded.
     */
    @Test
    public void testExtensionPoint() {
        List<MarkerSet> markerSet = MarkerConfigXmlParser.getMarkerSets();
        assertEquals("Extension point markers should be loaded", 1, markerSet.size());

        MarkerSet set = markerSet.get(0);
        assertEquals("Wrong MarkerSet name", "Example", set.getName());
        assertEquals("Wrong MarkerSet Id", "example.id", set.getId());
    }
}
