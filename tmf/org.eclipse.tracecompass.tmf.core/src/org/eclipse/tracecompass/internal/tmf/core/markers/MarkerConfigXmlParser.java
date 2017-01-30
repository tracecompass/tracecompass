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

package org.eclipse.tracecompass.internal.tmf.core.markers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.markers.Marker.PeriodicMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.SplitMarker;
import org.eclipse.tracecompass.internal.tmf.core.markers.SubMarker.WeightedMarker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * XML Parser for periodic marker configuration
 */
public class MarkerConfigXmlParser {

    /** Default marker configuration file URL */
    private static final URL DEFAULT_MARKER_CONFIG_URL = MarkerConfigXmlParser.class.getResource("/templates/markers.xml"); //$NON-NLS-1$
    /** Marker configuration file name */
    public static final IPath MARKER_CONFIG_PATH = Activator.getDefault().getStateLocation().addTrailingSeparator().append("markers.xml"); //$NON-NLS-1$
    /** Marker configuration file */
    private static final File MARKER_CONFIG_FILE = MARKER_CONFIG_PATH.toFile();
    /** Marker configuration schema URL */
    private static final URL MARKER_CONFIG_SCHEMA_URL = MarkerConfigXmlParser.class.getResource("/schema/markers.xsd"); //$NON-NLS-1$
    /** Marker configuration schema path */
    private static final IPath MARKER_CONFIG_SCHEMA_PATH = Activator.getDefault().getStateLocation().addTrailingSeparator().append("markers.xsd"); //$NON-NLS-1$
    /** Marker configuration schema file */
    private static final File MARKER_CONFIG_SCHEMA_FILE = MARKER_CONFIG_SCHEMA_PATH.toFile();
    /** Default marker label */
    private static final String DEFAULT_LABEL = "%d"; //$NON-NLS-1$

    private static final @NonNull String ELLIPSIS = ".."; //$NON-NLS-1$

    /**
     * Get the marker sets from the marker configuration file.
     *
     * @return the list of marker sets
     */
    public static @NonNull List<MarkerSet> getMarkerSets() {
        if (!MARKER_CONFIG_FILE.exists()) {
            return Collections.EMPTY_LIST;
        }
        return parse(MARKER_CONFIG_FILE.getAbsolutePath());
    }

    /**
     * Initialize the marker configuration file in the plug-in state location.
     */
    public static void initMarkerSets() {
        if (!MARKER_CONFIG_FILE.exists()) {
            try {
                File defaultConfigFile = new File(FileLocator.toFileURL(DEFAULT_MARKER_CONFIG_URL).toURI());
                Files.copy(defaultConfigFile.toPath(), MARKER_CONFIG_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (URISyntaxException | IOException e) {
                Activator.logError("Error copying " + DEFAULT_MARKER_CONFIG_URL + " to " + MARKER_CONFIG_FILE.getAbsolutePath(), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (!MARKER_CONFIG_SCHEMA_FILE.exists()) {
            try {
                File schemaFile = new File(FileLocator.toFileURL(MARKER_CONFIG_SCHEMA_URL).toURI());
                Files.copy(schemaFile.toPath(), MARKER_CONFIG_SCHEMA_FILE.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (URISyntaxException | IOException e) {
                Activator.logError("Error copying " + MARKER_CONFIG_SCHEMA_URL + " to " + MARKER_CONFIG_SCHEMA_FILE.getAbsolutePath(), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * Parse a periodic marker configuration file
     *
     * @param path
     *            the path to the configuration file
     * @return the list of marker sets
     */
    public static @NonNull List<MarkerSet> parse(String path) {

        List<MarkerSet> markerSets = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            dbf.setSchema(schemaFactory.newSchema(MARKER_CONFIG_SCHEMA_URL));
            DocumentBuilder db = dbf.newDocumentBuilder();

            // The following catches xml parsing exceptions
            db.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }

                @Override
                public void warning(SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }

                @Override
                public void fatalError(SAXParseException saxparseexception) throws SAXException {
                    throw saxparseexception;
                }
            });

            File file = new File(path);
            if (!file.canRead()) {
                return markerSets;
            }
            Document doc = db.parse(file);

            Element root = doc.getDocumentElement();
            if (!root.getNodeName().equals(IMarkerConstants.MARKER_SETS)) {
                return markerSets;
            }

            NodeList markerSetsList = root.getElementsByTagName(IMarkerConstants.MARKER_SET);
            for (int i = 0; i < markerSetsList.getLength(); i++) {
                try {
                    Element markerSetElem = (Element) markerSetsList.item(i);
                    String name = markerSetElem.getAttribute(IMarkerConstants.NAME);
                    String id = markerSetElem.getAttribute(IMarkerConstants.ID);
                    MarkerSet markerSet = new MarkerSet(name, id);
                    List<Marker> markers = getMarkers(markerSetElem);
                    for (Marker marker : markers) {
                        markerSet.addMarker(marker);
                    }
                    markerSets.add(markerSet);
                } catch (IllegalArgumentException e) {
                    Activator.logError("Error parsing " + path, e); //$NON-NLS-1$
                }
            }
            return markerSets;

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError("Error parsing " + path, e); //$NON-NLS-1$
        }
        return markerSets;
    }

    private static List<Marker> getMarkers(Element markerSet) {
        List<Marker> markers = new ArrayList<>();
        NodeList markerList = markerSet.getElementsByTagName(IMarkerConstants.MARKER);
        for (int i = 0; i < markerList.getLength(); i++) {
            Element markerElem = (Element) markerList.item(i);
            String name = markerElem.getAttribute(IMarkerConstants.NAME);
            String label = parseLabel(markerElem.getAttribute(IMarkerConstants.LABEL));
            String id = markerElem.getAttribute(IMarkerConstants.ID);
            String referenceId = markerElem.getAttribute(IMarkerConstants.REFERENCE_ID);
            String color = markerElem.getAttribute(IMarkerConstants.COLOR);
            double period = parsePeriod(markerElem.getAttribute(IMarkerConstants.PERIOD));
            String unit = parseUnit(markerElem.getAttribute(IMarkerConstants.UNIT));
            Range<Long> range = parseRange(markerElem.getAttribute(IMarkerConstants.RANGE));
            long offset = parseOffset(markerElem.getAttribute(IMarkerConstants.OFFSET));
            RangeSet<Long> indexRange = parseRangeSet(markerElem.getAttribute(IMarkerConstants.INDEX));
            PeriodicMarker marker = new PeriodicMarker(name, label, id, referenceId, color, period, unit, range, offset, indexRange);
            parseSubMarkers(markerElem, marker);
            markers.add(marker);
        }
        return markers;
    }

    private static void parseSubMarkers(Element marker, Marker parent) {
        NodeList nodeList = marker.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals(IMarkerConstants.SUBMARKER)) {
                    Element subMarkerElem = (Element) node;
                    String name = subMarkerElem.getAttribute(IMarkerConstants.NAME);
                    String label = parseLabel(subMarkerElem.getAttribute(IMarkerConstants.LABEL));
                    String id = subMarkerElem.getAttribute(IMarkerConstants.ID);
                    String color = subMarkerElem.getAttribute(IMarkerConstants.COLOR);
                    if (color.isEmpty()) {
                        color = parent.getColor();
                    }
                    String rangeAttr = subMarkerElem.getAttribute(IMarkerConstants.RANGE);
                    Range<Long> range = parseRange(rangeAttr);
                    if (!range.hasLowerBound() || !range.hasUpperBound()) {
                        throw new IllegalArgumentException("Unsupported unbound range: " + range); //$NON-NLS-1$
                    }
                    RangeSet<Long> indexRange = parseRangeSet(subMarkerElem.getAttribute(IMarkerConstants.INDEX));
                    SplitMarker subMarker = new SplitMarker(name, label, id, color, range, indexRange);
                    parent.addSubMarker(subMarker);
                    parseSubMarkers(subMarkerElem, subMarker);
                } else if (node.getNodeName().equals(IMarkerConstants.SEGMENTS)) {
                    Element segmentsElem = (Element) node;
                    String name = segmentsElem.getAttribute(IMarkerConstants.NAME);
                    WeightedMarker subMarker = new WeightedMarker(name);
                    parent.addSubMarker(subMarker);
                    parseSegments(segmentsElem, subMarker);
                    parseSubMarkers(segmentsElem, subMarker);
                }
            }
        }
    }

    private static void parseSegments(Element marker, WeightedMarker parent) {
        NodeList nodeList = marker.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(IMarkerConstants.SEGMENT)) {
                Element segmentElem = (Element) node;
                String label = parseLabel(segmentElem.getAttribute(IMarkerConstants.LABEL));
                String id = segmentElem.getAttribute(IMarkerConstants.ID);
                String color = segmentElem.getAttribute(IMarkerConstants.COLOR);
                String lengthAttr = segmentElem.getAttribute(IMarkerConstants.LENGTH);
                int length = Integer.parseInt(lengthAttr);
                if (length <= 0) {
                    throw new IllegalArgumentException("Unsupported length: " + lengthAttr); //$NON-NLS-1$
                }
                MarkerSegment segment = new MarkerSegment(label, id, color, length);
                parent.addSegment(segment);
                parseSubMarkers(segmentElem, segment);
            }
        }
    }

    private static String parseLabel(String labelAttr) {
        if (labelAttr.isEmpty()) {
            return DEFAULT_LABEL;
        }
        return labelAttr;
    }

    private static double parsePeriod(String periodAttr) {
        double period = Double.parseDouble(periodAttr);
        if (period <= 0) {
            throw new IllegalArgumentException("Unsupported period: " + periodAttr); //$NON-NLS-1$
        }
        return period;
    }

    private static String parseUnit(String unitAttr) {
        if (Arrays.asList(IMarkerConstants.MS, IMarkerConstants.US, IMarkerConstants.NS, IMarkerConstants.CYCLES).contains(unitAttr)) {
            return unitAttr;
        }
        throw new IllegalArgumentException("Unsupported unit: " + unitAttr); //$NON-NLS-1$
    }

    private static Range<Long> parseRange(String rangeAttr) {
        int index = rangeAttr.indexOf(ELLIPSIS);
        if (index > 0) {
            long min = Long.parseLong(rangeAttr.substring(0, index));
            index += ELLIPSIS.length();
            if (index < rangeAttr.length()) {
                long max = Long.parseLong(rangeAttr.substring(index));
                return Range.closed(min, max);
            }
            return Range.atLeast(min);
        }
        if (index == 0) {
            index += ELLIPSIS.length();
            if (index < rangeAttr.length()) {
                long max = Long.parseLong(rangeAttr.substring(index));
                return Range.atMost(max);
            }
            return Range.all();
        }
        if (!rangeAttr.isEmpty()) {
            long val = Long.parseLong(rangeAttr);
            return Range.singleton(val);
        }
        return Range.atLeast(0L);
    }

    private static RangeSet<Long> parseRangeSet(String rangeSetAttr) {
        if (rangeSetAttr.isEmpty()) {
            return ImmutableRangeSet.of(Range.all());
        }
        RangeSet<Long> rangeSet = TreeRangeSet.create();
        String[] ranges = rangeSetAttr.split(","); //$NON-NLS-1$
        if (ranges.length == 0) {
            rangeSet.add(Range.all());
        } else {
            for (String range : ranges) {
                rangeSet.add(parseRange(range));
            }
        }
        return rangeSet;
    }

    private static long parseOffset(String offset) {
        if (offset.isEmpty()) {
            return 0L;
        }
        return Long.parseLong(offset);
    }
}
