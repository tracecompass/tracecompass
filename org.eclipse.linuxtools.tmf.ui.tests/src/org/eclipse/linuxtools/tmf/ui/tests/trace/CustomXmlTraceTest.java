package org.eclipse.linuxtools.tmf.ui.tests.trace;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition.InputElement;
import org.junit.Before;

/**
 * Abstract test parent
 *
 * @author Matthew Khouzam
 *
 */
public abstract class CustomXmlTraceTest {
    private CustomXmlTraceDefinition cxtd;
    /**
     * The trace to use to "validate" the xml files
     */
    private CustomXmlTrace t;
    /**
     * The path of the trace
     */
    private String path;


    /**
     * set up directories
     */
    @Before
    public void init() {
        cxtd = new CustomXmlTraceDefinition("test", new InputElement(), new ArrayList<OutputColumn>(), "s");
        t = new CustomXmlTrace(cxtd);
    }


    /**
     * @return the trace
     */
    public CustomXmlTrace getTrace() {
        return t;
    }


    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }


    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

}
