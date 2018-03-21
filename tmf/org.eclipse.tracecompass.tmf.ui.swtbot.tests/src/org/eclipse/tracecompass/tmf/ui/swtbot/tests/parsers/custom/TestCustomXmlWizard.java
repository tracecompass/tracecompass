/*******************************************************************************
 * Copyright (c) 2014, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.parsers.custom;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Custom XML wizard tests
 *
 * This test will help validate the CustomXmlParserInputWizardPage
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestCustomXmlWizard extends AbstractCustomParserWizard {

    private static final String EVENT = "event";
    private static final String TRACE = "trace";
    private static final String XML_TRACE1 = "<trace>\n\t<event time=\"100\" msg=\"hello\"/>\n\t<event time=\"200\" msg=\"world\"/></trace>";
    private static final String MANAGE_CUSTOM_PARSERS_SHELL_TITLE = "Manage Custom Parsers";
    private static final String CUSTOM_XML_PARSER_SHELL_TITLE = "Custom XML Parser";
    private static final String PROJECT_NAME = "TestXML";
    private static final String CATEGORY_NAME = "Test Category";
    private static final String TRACETYPE_NAME = "Test Trace";
    private static final String EXPECTED_TEST_DEFINITION = "<Definition category=\"Test Category\" name=\"" + TRACETYPE_NAME + "\">\n" +
            "<TimeStampOutputFormat>ss</TimeStampOutputFormat>\n" +
            "<InputElement name=\"trace\">\n" +
            "<InputElement logentry=\"true\" name=\"event\">\n" +
            "<InputData action=\"0\" name=\"Ignore\" tag=\"IGNORE\"/>\n" +
            "<Attribute name=\"msg\">\n" +
            "<InputData action=\"0\" name=\"msg\" tag=\"OTHER\"/>\n" +
            "</Attribute>\n" +
            "<Attribute name=\"time\">\n" +
            "<InputData action=\"0\" name=\"time\" tag=\"OTHER\"/>\n" +
            "</Attribute>\n" +
            "</InputElement>\n" +
            "</InputElement>\n" +
            "<OutputColumn name=\"msg\" tag=\"OTHER\"/>\n" +
            "<OutputColumn name=\"time\" tag=\"OTHER\"/>\n";

    /**
     * Test to create a custom XML trace and compare the XML generated
     *
     * @throws IOException
     *             the xml file is not accessible, this is bad
     * @throws FileNotFoundException
     *             the xml file wasn't written, this is bad
     */
    @Test
    public void testNew() throws FileNotFoundException, IOException {
        File xmlFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(".metadata/.plugins/org.eclipse.tracecompass.tmf.core/custom_xml_parsers.xml").toFile();
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeNode = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        treeNode.contextMenu("Manage Custom Parsers...").click();
        fBot.shell(MANAGE_CUSTOM_PARSERS_SHELL_TITLE).activate();
        fBot.radio("XML").click();
        fBot.button("New...").click();
        fBot.shell(CUSTOM_XML_PARSER_SHELL_TITLE).activate();
        fBot.textWithLabel("Category:").setText(CATEGORY_NAME);
        fBot.textWithLabel("Trace type:").setText(TRACETYPE_NAME);
        fBot.textWithLabel("Time Stamp format:").setText("ss");

        fBot.styledText().setText(XML_TRACE1);
        fBot.buttonWithTooltip("Feeling lucky").click();

        fBot.tree().getTreeItem(TRACE).getNode(EVENT).select();
        fBot.checkBox("Log Entry").click();
        fBot.button("Next >").click();
        fBot.button("Finish").click();

        fBot.waitUntil(new CustomDefinitionHasContent(xmlFile, CATEGORY_NAME, TRACETYPE_NAME, EXPECTED_TEST_DEFINITION));
        String xmlPart = extractTestXml(xmlFile, CATEGORY_NAME, TRACETYPE_NAME);
        assertEquals(EXPECTED_TEST_DEFINITION, xmlPart);
        fBot.list().select(CATEGORY_NAME + " : " + TRACETYPE_NAME);
        fBot.button("Delete").click();
        fBot.button("Yes").click();
        fBot.button("Close").click();
        fBot.waitUntil(new CustomDefinitionHasContent(xmlFile, CATEGORY_NAME, TRACETYPE_NAME, ""));
        xmlPart = extractTestXml(xmlFile, CATEGORY_NAME, TRACETYPE_NAME);
        assertEquals("", xmlPart);

        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

}
