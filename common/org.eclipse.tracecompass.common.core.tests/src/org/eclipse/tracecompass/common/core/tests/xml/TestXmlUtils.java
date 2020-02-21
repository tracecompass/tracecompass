/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.tracecompass.common.core.xml.XmlUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Test XmlUtils
 *
 * @author Matthew Khouzam
 */
public class TestXmlUtils {

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

    /**
     * XXE Schema attack
     *
     * source:
     * https://depthsecurity.com/blog/exploitation-xml-external-entity-xxe-injection
     */
    private static final String SCHEMA_XXE_ATTACK = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            "<!DOCTYPE foo [ <!ELEMENT foo ANY >\n" +
            "<!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>\n" +
            "<creds>\n" +
            "    <user>&xxe;</user>\n" +
            "    <pass>mypass</pass>\n" +
            "</creds>";

    /**
     * Disclosing the password attack
     *
     * source:
     * https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing
     */
    private static final String GET_PASSWORD_ATTACK = " <?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            " <!DOCTYPE foo [" +
            "   <!ELEMENT foo ANY >" +
            "   <!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]><foo>&xxe;</foo>" +
            " <?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            " <!DOCTYPE foo [  \n" +
            "   <!ELEMENT foo ANY >\n" +
            "   <!ENTITY xxe SYSTEM \"file:///etc/shadow\" >]><foo>&xxe;</foo>\n" +
            "\n" +
            " <?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            " <!DOCTYPE foo [  \n" +
            "   <!ELEMENT foo ANY >\n" +
            "   <!ENTITY xxe SYSTEM \"file:///c:/boot.ini\" >]><foo>&xxe;</foo>\n" +
            "\n" +
            " <?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            " <!DOCTYPE foo [  \n" +
            "   <!ELEMENT foo ANY >\n" +
            "   <!ENTITY xxe SYSTEM \"http://www.attacker.com/text.txt\" >]><foo>&xxe;</foo>";

    /**
     * Safe schema, https://www.w3schools.com/XML/schema_howto.asp
     */
    private static String SAFE_SCHEMA = "<?xml version=\"1.0\"?>\n" +
            "<xs:schema xmlns:xs=\"" + XMLConstants.W3C_XML_SCHEMA_NS_URI + "\" >" +
            "<xs:element name=\"note\"> " +
            "  <xs:complexType> " +
            "    <xs:sequence> " +
            "      <xs:element name=\"to\" type=\"xs:string\"/> " +
            "      <xs:element name=\"from\" type=\"xs:string\"/> " +
            "      <xs:element name=\"heading\" type=\"xs:string\"/> " +
            "      <xs:element name=\"body\" type=\"xs:string\"/> " +
            "    </xs:sequence> " +
            "  </xs:complexType> " +
            "</xs:element> " +
            "</xs:schema>";

    /**
     * Safe document, https://www.w3schools.com/XML/schema_howto.asp
     */
    private static String SAFE_DOCUMENT = "<?xml version=\"1.0\"?>" +
            "<note>" +
            "  <to>Tove</to>" +
            "  <from>Jani</from>" +
            "  <heading>Reminder</heading>" +
            "  <body>Don't forget me this weekend!</body>" +
            "</note> ";

    private static void testExploit(String attackVector) throws SAXException, IOException, ParserConfigurationException, TransformerException {
        Transformer newSafeTransformer = XmlUtils.newSecureTransformer();
        assertNotNull(newSafeTransformer);
        DocumentBuilderFactory dbf = XmlUtils.newSafeDocumentBuilderFactory();
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

    /**
     * Test schema safety
     *
     * @throws SAXException
     *             failed to parse, should happen as this is an attack
     */
    @Test(expected = SAXException.class)
    public void testNewSafeSchemaFactory() throws SAXException {
        SchemaFactory schemaFactory = XmlUtils.newSafeSchemaFactory();
        assertNotNull(schemaFactory);
        Schema schema = XmlUtils.newSafeSchemaFactory().newSchema(new StreamSource(new StringReader(SCHEMA_XXE_ATTACK)));
        assertNotNull(schema);
    }

    /**
     * Test a safe validation
     *
     * @throws SAXException
     *             the file failed. Should not happen
     * @throws IOException
     *             the file could not be read, cannot happen.
     */
    @Test
    public void testSafeValidate() throws SAXException, IOException {
        Schema newSafeSchema = XmlUtils.newSafeSchemaFactory().newSchema(new StreamSource(new StringReader(SAFE_SCHEMA)));
        assertNotNull(newSafeSchema);
        XmlUtils.safeValidate(newSafeSchema, new StreamSource(new StringReader(SAFE_DOCUMENT)));
    }

    /**
     * Test an invalid validation
     *
     * @throws SAXException
     *             the file failed. Should happen
     * @throws IOException
     *             the file could not be read, cannot happen.
     */
    @Test(expected = SAXException.class)
    public void testAttackValidate() throws SAXException, IOException {
        Schema newSafeSchema = XmlUtils.newSafeSchemaFactory().newSchema(new StreamSource(new StringReader(SAFE_SCHEMA)));
        assertNotNull(newSafeSchema);
        XmlUtils.safeValidate(newSafeSchema, new StreamSource(new StringReader(GET_PASSWORD_ATTACK)));
    }

    /**
     * Test the SAX parser factory
     *
     * @throws SAXException
     *             the file could not parse
     * @throws ParserConfigurationException
     *             the parser was badly configured, should not happen
     * @throws IOException
     *             permission issue, should not happen
     */
    @Test
    public void testNewSafeSaxParserFactory() throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory parserFactory = XmlUtils.newSafeSaxParserFactory();
        assertNotNull(parserFactory);
        XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();
        assertNotNull(xmlReader);
        xmlReader.parse(new InputSource(new StringReader(SAFE_DOCUMENT)));
    }

    /**
     * Test newSafeXmlStreamReader, simple test to make sure it still works.
     *
     * @throws XMLStreamException
     *             the stream cannot be read
     */
    @Test
    public void newSafeXmlStreamReader() throws XMLStreamException {
        XMLStreamReader reader = XmlUtils.newSafeXmlStreamReader(new ByteArrayInputStream(SAFE_DOCUMENT.getBytes(StandardCharsets.UTF_8)));
        assertNotNull(reader);
        assertEquals(1, reader.next());
    }
}
