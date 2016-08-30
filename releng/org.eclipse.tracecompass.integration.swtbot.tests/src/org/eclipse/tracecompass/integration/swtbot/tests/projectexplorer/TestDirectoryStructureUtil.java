/******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.projectexplorer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;

/**
 * Util class to create directory structures detailed in chapter 3 of the
 * projectView test
 *
 * @author Matthew Khouzam
 */
public class TestDirectoryStructureUtil {

    private static final String CUSTOM_TEXT_PARSER_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<CustomTxtTraceDefinitionList>"
            + "<Definition name=\"TmfGeneric\">"
            + "<TimeStampOutputFormat>yyyy-MM-dd HH:mm:ss.SSS</TimeStampOutputFormat>"
            + "<InputLine><Cardinality max=\"2147483647\" min=\"0\"/>"
            + "<RegEx>\\s*\\[(\\d*\\.\\d*)\\]\\s*\\[TID=(\\d*)\\]\\s*\\[(SIG|CMP|EVT|REQ)\\]\\s*(.*)</RegEx>"
            + "<InputData action=\"0\" format=\"ss.SSS\" name=\"Time Stamp\"/>"
            + "<InputData action=\"0\" format=\"\" name=\"Thread ID\"/>"
            + "<InputData action=\"0\" format=\"\" name=\"Type\"/>"
            + "<InputData action=\"0\" format=\"\" name=\"Message\"/>"
            + "</InputLine><OutputColumn name=\"Time Stamp\"/>"
            + "<OutputColumn name=\"Thread ID\"/>"
            + "<OutputColumn name=\"Type\"/>"
            + "<OutputColumn name=\"Message\"/>"
            + "</Definition>"
            + "</CustomTxtTraceDefinitionList>";

    private static final String CUSTOM_XML_PARSER_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<CustomXMLTraceDefinitionList><Definition name=\"Custom XML Log\">"
            + "<TimeStampOutputFormat>yyyy-MM-dd HH:mm:ss.SSS</TimeStampOutputFormat>"
            + "<InputElement name=\"Log\"><InputElement logentry=\"true\" name=\"Record\">"
            + "<InputData action=\"0\" format=\"\" name=\"Ignore\"/>"
            + "<Attribute name=\"number\"><InputData action=\"0\" format=\"\" name=\"Rec Num\"/>"
            + "</Attribute><InputElement name=\"Time\">"
            + "<InputData action=\"0\" format=\"'year:'yyyy | 'month:'MM | 'day:'dd | 'hour:'HH | 'minute:'mm | 'second:'ss\" name=\"Time Stamp\"/></InputElement>"
            + "<InputElement name=\"Content\"><InputData action=\"0\" format=\"\" name=\"Message\"/></InputElement>"
            + "</InputElement></InputElement><OutputColumn name=\"Time Stamp\"/>"
            + "<OutputColumn name=\"Rec Num\"/><OutputColumn name=\"Message\"/>"
            + "</Definition></CustomXMLTraceDefinitionList>";

    private static final String CUSTOM_TEXT_LAST_LINE = "[1371742192.049] [TID=001] [SIG] Sig=TmfEndSynchSignal Target=(end)\n";
    private static final String CUSTOM_TEXT_CONTENT = "[1371742192.034] [TID=001] [SIG] Sig=TmfStartSynchSignal Target=(start)\n" +
            "[1371742192.048] [TID=001] [SIG] Sig=TmfStartSynchSignal Target=(end)\n" +
            "[1371742192.048] [TID=001] [SIG] Sig=TmfStartSynchSignal Target=(start)\n" +
            "[1371742192.048] [TID=001] [SIG] Sig=TmfStartSynchSignal Target=(end)\n" +
            "[1371742192.048] [TID=001] [SIG] Sig=TmfTimestampFormatUpdateSignal Target=(start)\n" +
            "[1371742192.048] [TID=001] [SIG] Sig=TmfTimestampFormatUpdateSignal Target=(end)\n" +
            "[1371742192.049] [TID=001] [SIG] Sig=TmfTimestampFormatUpdateSignal Target=(start)\n" +
            "[1371742192.049] [TID=001] [SIG] Sig=TmfTimestampFormatUpdateSignal Target=(end)\n" +
            "[1371742192.049] [TID=001] [SIG] Sig=TmfEndSynchSignal Target=(start)\n" +
            CUSTOM_TEXT_LAST_LINE;

    private static final String CUSTOM_XML_PARSER_LAST_LINE = ""
            + "<Record number = \"6\">" + "<Time>" + "<year> 2011 </year> <month> 10 </month> <day> 26 </day>" + "<hour> 22 </hour> <minute> 18 </minute> <second> 32 </second>" + "</Time>" + "<Content>" + "<Message>This is the message</Message>"
            + "<Level>The Log Level</Level>" + "</Content>" + "</Record>";
    private static final String CUSTOM_XML_CONTENT = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
            + "<!DOCTYPE Log SYSTEM \"ExampleXMLLog.dtd\">\r\n\r\n<Log>" + "<LogCreated>" + "<LogName> Example XML Log </LogName>" + "<Time>" + "<year> 2011 </year> <month> 10 </month> <day> 26 </day>"
            + "<hour> 22 </hour> <minute> 0 </minute> <second> 25 </second>" + "</Time>" + "</LogCreated>"
            + ""
            + "<Record number = \"1\">" + "<Time>" + "<year> 2011 </year> <month> 10 </month> <day> 26 </day>" + "<hour> 22 </hour> <minute> 1 </minute> <second> 20 </second>" + "</Time>" + "<Content>" + "<Message>This is the message</Message>"
            + "<Level>The Log Level</Level>" + "</Content>" + "</Record>"
            + ""
            + "<Record number = \"2\">" + "<Time>" + "<year> 2011 </year> <month> 10 </month> <day> 26 </day>" + "<hour> 22 </hour> <minute> 2 </minute> <second> 11 </second>" + "</Time>" + "<Content>" + "<Message>This is the message</Message>"
            + "<Level>The Log Level</Level>" + "" + "</Content>" + "</Record>"
            + ""
            + "<Record number = \"3\">" + "<Time>" + "<year> 2011 </year> <month> 10 </month> <day> 26 </day>" + "<hour> 22 </hour> <minute> 3 </minute> <second> 17 </second>" + "</Time>" + "<Content>"
            + "<Message>This is the message</Message>" + "<Level>The Log Level</Level>" + "</Content>" + "</Record>"
            + ""
            + "<Record number = \"4\">" + "<Time>" + "<year> 2011 </year> <month> 10 </month> <day> 26 </day>" + "<hour> 22 </hour> <minute> 4 </minute> <second> 30 </second>" + "</Time>"
            + "<Content>" + "<Message>This is the message</Message>" + "<Level>The Log Level</Level>" + "</Content>" + "</Record>"
            + ""
            + "<Record number = \"5\">" + "<Time>" + "<year> 2011 </year> <month> 10 </month> <day> 26 </day>" + "<hour> 22 </hour> <minute> 5 </minute> <second> 17 </second>" + "</Time>" + "<Content>" + "<Message>This is the message</Message>"
            + "<Level>The Log Level</Level>" + "</Content>" + "</Record>"
            + CUSTOM_XML_PARSER_LAST_LINE;

    private static final String UNRECOGNIZED_LOG_CONTENT = "Hi mom!";

    private TestDirectoryStructureUtil() {
        // do nothing
    }

    /**
     * Generate a directory structure as follows
     *
     * <pre>
     * parentDir
     *  ├── customParsers
     *  │   ├── ExampleCustomTxtParser.xml
     *  │   └── ExampleCustomXmlParser.xml
     *  └── import
     *      ├── z-clashes
     *      │   ├── ExampleCustomTxt.log
     *      │   ├── ExampleCustomXml.xml
     *      │   ├── kernel-overlap-testing
     *      │   │   ├── stream
     *      │   │   └── metadata
     *      │   ├── simple_server-thread1
     *      │   │   ├── stream
     *      │   │   └── metadata
     *      │   ├── simple_server-thread2
     *      │   │   ├── stream
     *      │   │   └── metadata
     *      │   └── ust-overlap-testing
     *      │       ├── stream
     *      │       └── metadata
     *      ├── empty
     *      ├── ExampleCustomTxt.log
     *      ├── ExampleCustomXml.xml
     *      ├── kernel-overlap-testing
     *      │   ├── stream
     *      │   └── metadata
     *      ├── simple_server-thread1
     *      │   ├── metadata
     *      │   └── stream
     *      ├── simple_server-thread2
     *      │   ├── metadata
     *      │   └── stream
     *      ├── unrecognized.log
     *      └── ust-overlap-testing
     *          ├── stream
     *          └── metadata
     * </pre>
     *
     * @param parentDir
     *            the directory to use as the parent
     * @return the structure detailed above
     * @throws IOException
     *             out of space or permission problem
     */
    public static File generateTraceStructure(File parentDir) throws IOException {
        File parent = (parentDir == null) ? File.createTempFile("Traces", "") : parentDir;
        if (!parent.isDirectory()) {
            parent.delete();
            parent.mkdir();
        }
        File customParser = createDir(parent, "customParsers");
        createFile(customParser, "ExampleCustomTxtParser.xml", CUSTOM_TEXT_PARSER_CONTENT);
        createFile(customParser, "ExampleCustomXmlParser.xml", CUSTOM_XML_PARSER_CONTENT);
        File importDir = createDir(parent, "import");
        createDir(importDir, "empty");
        createFile(importDir, "ExampleCustomTxt.log", CUSTOM_TEXT_CONTENT);
        createFile(importDir, "ExampleCustomXml.xml", CUSTOM_XML_CONTENT);
        createFile(importDir, "unrecognized.log", UNRECOGNIZED_LOG_CONTENT);
        // Using the z- prefix so that the traces in this folder are imported
        // last by the import wizard
        final String CLASHES_DIR_NAME = "z-clashes";
        File theClash = createDir(importDir, CLASHES_DIR_NAME);

        // We're making the clash version of each trace slightly different in content to help differentiate them
        createFile(theClash, "ExampleCustomTxt.log", CUSTOM_TEXT_CONTENT + CUSTOM_TEXT_LAST_LINE);
        createFile(theClash, "ExampleCustomXml.xml", CUSTOM_XML_CONTENT + CUSTOM_XML_PARSER_LAST_LINE);

        LttngTraceGenerator kernelGenerator = new LttngTraceGenerator(1000, 1000, 1);
        LttngTraceGenerator ustGenerator = new LttngTraceGenerator(1000, 1000, 1, false);
        kernelGenerator.writeTrace(new File(importDir.getAbsolutePath() + File.separator + "kernel-overlap-testing"));
        ustGenerator.writeTrace(new File(importDir.getAbsolutePath() + File.separator + "ust-overlap-testing"));
        ustGenerator.writeTrace(new File(importDir.getAbsolutePath() + File.separator + "simple_server-thread1"));
        ustGenerator.writeTrace(new File(importDir.getAbsolutePath() + File.separator + "simple_server-thread2"));

        kernelGenerator = new LttngTraceGenerator(1000, 1001, 1);
        ustGenerator = new LttngTraceGenerator(1000, 1001, 1, false);
        kernelGenerator.writeTrace(new File(importDir.getAbsolutePath() + File.separator + CLASHES_DIR_NAME + File.separator + "kernel-overlap-testing"));
        ustGenerator.writeTrace(new File(importDir.getAbsolutePath() + File.separator + CLASHES_DIR_NAME + File.separator + "ust-overlap-testing"));
        ustGenerator.writeTrace(new File(importDir.getAbsolutePath() + File.separator + CLASHES_DIR_NAME + File.separator + "simple_server-thread1"));
        ustGenerator.writeTrace(new File(importDir.getAbsolutePath() + File.separator + CLASHES_DIR_NAME + File.separator + "simple_server-thread2"));

        assertTrue(parent.listFiles().length > 0);

        return parent;
    }

    private static File createDir(File parent, String name) {
        File child = new File(parent.getAbsolutePath() + File.separator + name);
        child.mkdir();
        return child;
    }

    private static File createFile(File parent, String name, String content) throws FileNotFoundException {
        File child = new File(parent.getAbsolutePath() + File.separator + name);
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(child))) {
            pw.write(content);
        }
        return child;
    }
}
