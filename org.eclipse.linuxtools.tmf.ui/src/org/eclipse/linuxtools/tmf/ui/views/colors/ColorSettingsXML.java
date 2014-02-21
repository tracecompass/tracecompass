/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated to use RGB for the tick color
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.colors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.filter.xml.TmfFilterContentHandler;
import org.eclipse.linuxtools.tmf.core.filter.xml.TmfFilterXMLWriter;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class for saving and loading of color settings to/from file.
 *
 * @version 1.0
 * @author Patrick Tasse
 *
 */
public class ColorSettingsXML {

    // XML Tags and attributes
    private static final String COLOR_SETTINGS_TAG = "COLOR_SETTINGS"; //$NON-NLS-1$
    private static final String COLOR_SETTING_TAG = "COLOR_SETTING"; //$NON-NLS-1$
    private static final String FG_TAG = "FG"; //$NON-NLS-1$
    private static final String BG_TAG = "BG"; //$NON-NLS-1$
    private static final String R_ATTR = "R"; //$NON-NLS-1$
    private static final String G_ATTR = "G"; //$NON-NLS-1$
    private static final String B_ATTR = "B"; //$NON-NLS-1$
    private static final String TICK_TAG = "TICK"; //$NON-NLS-1$
    private static final String FILTER_TAG = "FILTER"; //$NON-NLS-1$

    /**
     * Saves the given color settings to file.
     *
     * @param pathName
     *            A file name with path
     * @param colorSettings
     *            -An array of color settings to save.
     */
    public static void save(String pathName, ColorSetting[] colorSettings) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement(COLOR_SETTINGS_TAG);
            document.appendChild(rootElement);

            for (ColorSetting colorSetting : colorSettings) {
                Element colorSettingElement = document.createElement(COLOR_SETTING_TAG);
                rootElement.appendChild(colorSettingElement);

                Element fgElement = document.createElement(FG_TAG);
                colorSettingElement.appendChild(fgElement);
                RGB foreground = colorSetting.getForegroundRGB();
                fgElement.setAttribute(R_ATTR, Integer.toString(foreground.red));
                fgElement.setAttribute(G_ATTR, Integer.toString(foreground.green));
                fgElement.setAttribute(B_ATTR, Integer.toString(foreground.blue));

                Element bgElement = document.createElement(BG_TAG);
                colorSettingElement.appendChild(bgElement);
                RGB background = colorSetting.getBackgroundRGB();
                bgElement.setAttribute(R_ATTR, Integer.toString(background.red));
                bgElement.setAttribute(G_ATTR, Integer.toString(background.green));
                bgElement.setAttribute(B_ATTR, Integer.toString(background.blue));

                Element tickColorElement = document.createElement(TICK_TAG);
                colorSettingElement.appendChild(tickColorElement);
                RGB tickColor = colorSetting.getTickColorRGB();
                tickColorElement.setAttribute(R_ATTR, Integer.toString(tickColor.red));
                tickColorElement.setAttribute(G_ATTR, Integer.toString(tickColor.green));
                tickColorElement.setAttribute(B_ATTR, Integer.toString(tickColor.blue));

                if (colorSetting.getFilter() != null) {
                    Element filterElement = document.createElement(FILTER_TAG);
                    colorSettingElement.appendChild(filterElement);
                    TmfFilterXMLWriter.buildXMLTree(document, colorSetting.getFilter(), filterElement);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();

            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(pathName));
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            Activator.getDefault().logError("Error saving color xml file: " + pathName, e); //$NON-NLS-1$
        } catch (TransformerConfigurationException e) {
            Activator.getDefault().logError("Error saving color xml file: " + pathName, e); //$NON-NLS-1$
        } catch (TransformerException e) {
            Activator.getDefault().logError("Error saving color xml file: " + pathName, e); //$NON-NLS-1$
        }
    }

    /**
     * Loads color settings from file and returns it in an array.
     *
     * @param pathName
     *            A file name with path
     *
     * @return the color settings array loaded from file
     */
    public static ColorSetting[] load(String pathName) {
        if (!new File(pathName).canRead()) {
            return new ColorSetting[0];
        }
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);

        ColorSettingsContentHandler handler = new ColorSettingsContentHandler();
        try {
            XMLReader saxReader = parserFactory.newSAXParser().getXMLReader();
            saxReader.setContentHandler(handler);
            saxReader.parse(pathName);
            return handler.colorSettings.toArray(new ColorSetting[0]);
        } catch (ParserConfigurationException e) {
            Activator.getDefault().logError("Error loading color xml file: " + pathName, e); //$NON-NLS-1$
        } catch (SAXException e) {
            Activator.getDefault().logError("Error loading color xml file: " + pathName, e); //$NON-NLS-1$
        } catch (IOException e) {
            Activator.getDefault().logError("Error loading color xml file: " + pathName, e); //$NON-NLS-1$
        }
        // In case of error, dispose the partial list of color settings
        for (ColorSetting colorSetting : handler.colorSettings) {
            colorSetting.dispose();
        }
        return new ColorSetting[0];
    }

    // Helper class
    private static class ColorSettingsContentHandler extends DefaultHandler {

        private List<ColorSetting> colorSettings = new ArrayList<>(0);
        private RGB fg = new RGB(0, 0, 0);
        private RGB bg = new RGB(255, 255, 255);
        private RGB tickColor = new RGB(0, 0, 0);
        private ITmfFilterTreeNode filter;
        private TmfFilterContentHandler filterContentHandler;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            if (localName.equals(COLOR_SETTINGS_TAG)) {
                colorSettings = new ArrayList<>();
            } else if (localName.equals(COLOR_SETTING_TAG)) {
                fg = null;
                bg = null;
                filter = null;
            } else if (localName.equals(FG_TAG)) {
                int r = Integer.parseInt(attributes.getValue(R_ATTR));
                int g = Integer.parseInt(attributes.getValue(G_ATTR));
                int b = Integer.parseInt(attributes.getValue(B_ATTR));
                fg = new RGB(r, g, b);
            } else if (localName.equals(BG_TAG)) {
                int r = Integer.parseInt(attributes.getValue(R_ATTR));
                int g = Integer.parseInt(attributes.getValue(G_ATTR));
                int b = Integer.parseInt(attributes.getValue(B_ATTR));
                bg = new RGB(r, g, b);
            } else if (localName.equals(TICK_TAG)) {
                int r = Integer.parseInt(attributes.getValue(R_ATTR));
                int g = Integer.parseInt(attributes.getValue(G_ATTR));
                int b = Integer.parseInt(attributes.getValue(B_ATTR));
                tickColor = new RGB(r, g, b);
            } else if (localName.equals(FILTER_TAG)) {
                filterContentHandler = new TmfFilterContentHandler();
            } else if (filterContentHandler != null) {
                filterContentHandler.startElement(uri, localName, qName, attributes);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (localName.equals(COLOR_SETTINGS_TAG)) {
                // Nothing to do
            } else if (localName.equals(COLOR_SETTING_TAG)) {
                ColorSetting colorSetting = new ColorSetting(fg, bg, tickColor, filter);
                colorSettings.add(colorSetting);
            } else if (localName.equals(FILTER_TAG)) {
                filter = filterContentHandler.getTree();
                filterContentHandler = null;
            } else if (filterContentHandler != null) {
                filterContentHandler.endElement(uri, localName, qName);
            }
        }

    }
}
