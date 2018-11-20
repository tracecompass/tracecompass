/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.xml;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.tracecompass.common.core.xml.XmlUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Test XmlUtils
 *
 * @author Matthew Khouzam
 */
public class TestTransform {

    /**
     * XML Denial of service attack. When an XML parser tries to resolve the
     * external entities included within the following code, it will cause the
     * application to start consuming all of the available memory until the
     * process crashes. This is an example XML document with an embedded DTD
     * schema including the attack
     *
     * Source: https://www.owasp.org/index.php/XML_Security_Cheat_Sheet
     */
    private static final String BILLION_LAUGH_ATTACK = "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE lolz [\n" +
            "<!ENTITY lol \"lol\">\n" +
            "<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
            "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
            "<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
            "<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
            "<!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
            "<!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
            "<!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
            "<!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
            "]>\n" +
            "<lolz>&lol9;</lolz>";

    /**
     * When the definition of an element A is another element B, and that
     * element B is defined as element A, that schema describes a circular
     * reference between elements.
     *
     * source:
     * https://www.owasp.org/index.php/XML_Security_Cheat_Sheet#Recursive_Entity_Reference
     */
    private static final String RECURSIVE_ENTITY_REFERENCE = "<!DOCTYPE A [\n" +
            " <!ELEMENT A ANY>\n" +
            " <!ENTITY A \"<A>&B;</A>\"> \n" +
            " <!ENTITY B \"&A;\">\n" +
            "]>\n" +
            "<A>&A;</A>";

    /**
     * Injection attack
     *
     * source: http://ws-attacks.org/XML_External_Entity_DOS
     */
    private static final String XML_INJECTION_ATTACK = "<?xml version=\"1.0\"?>\n" +
            "<!DOCTYPE order [\n" +
            "<!ELEMENT foo ANY >\n" +
            "<!ENTITY xxe SYSTEM \"file:///dev/random\" >\n" +
            "]>\n" +
            "<soap:Envelope xmlns:soap=\"http://www.w3.org/2001/12/soap-envelope\" soap:encodingStyle=\"http://www.w3.org/2001/12/soap-encoding\">\n" +
            "  <soap:Body xmlns:m=\"http://www.example.org/order\">\n" +
            "     <foo>&xxe;</foo>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>\n";

    private static void testExploit(String attackVector) throws SAXException, IOException, ParserConfigurationException, TransformerException {
        Transformer newSafeTransformer = XmlUtils.newSecureTransformer();
        assertNotNull(newSafeTransformer);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(attackVector)));
        newSafeTransformer.setOutputProperty(OutputKeys.METHOD, "xml");
        newSafeTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        newSafeTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StreamResult output = new StreamResult(new StringWriter());
        newSafeTransformer.transform(new DOMSource(document), output);
    }

    /**
     * Test against a Billion laughs attack
     *
     * @throws TransformerException
     *             Problem transforming xml, should not happen
     * @throws ParserConfigurationException
     *             Problem configuring transformer, should not happen
     * @throws IOException
     *             permission issue, should not happen
     * @throws SAXException
     *             xml issue, should happen, this is parsing an attack
     */
    @Test(expected = SAXException.class)
    public void testBillionLaughs() throws TransformerException, SAXException, IOException, ParserConfigurationException {
        testExploit(BILLION_LAUGH_ATTACK);
    }

    /**
     * Test against a Recursive Entity Reference
     *
     * @throws TransformerException
     *             Problem transforming xml, should not happen
     * @throws ParserConfigurationException
     *             Problem configuring transformer, should not happen
     * @throws IOException
     *             permission issue, should not happen
     * @throws SAXException
     *             xml issue, should happen, this is parsing an attack
     */
    @Test(expected = SAXException.class)
    public void testRecursiveEntityReference() throws TransformerException, SAXException, IOException, ParserConfigurationException {
        testExploit(RECURSIVE_ENTITY_REFERENCE);
    }

    /**
     * Test against a XML Injection Attacks
     *
     * @throws TransformerException
     *             Problem transforming xml, should not happen
     * @throws ParserConfigurationException
     *             Problem configuring transformer, should not happen
     * @throws IOException
     *             permission issue, should happen, this is parsing an attack
     * @throws SAXException
     *             xml issue, should not happen
     */
    @Ignore
    @Test(expected = IOException.class)
    public void testXmlInjection() throws SAXException, IOException, ParserConfigurationException, TransformerException {
        testExploit(XML_INJECTION_ATTACK);
    }

}
