package org.eclipse.linuxtools.tmf.ui.tests.trace;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Malformed xml test, dangerous errors
 * @author Matthew Khouzam
 *
 */
@RunWith(Parameterized.class)
public class CustomXmlTraceBadlyFormedTest extends CustomXmlTraceTest {

    private final static String pathname = "tracesets/xml/malformed";

    /**
     * This should create the parameters to launch the project
     *
     * @return the path of the parameters
     */
    @Parameters(name = "{index}: path {0}")
    public static Collection<Object[]> getFiles() {
        File[] malformedFiles = (new File(pathname)).listFiles();
        Collection<Object[]> params = new ArrayList<Object[]>();
        for (File f : malformedFiles) {
            Object[] arr = new Object[] { f.getAbsolutePath() };
            params.add(arr);
        }
        return params;
    }

    /**
     * Test all the invalid xml files
     */
    @Test
    public void testBadlyFormed() {
        IStatus valid = getTrace().validate(null, getPath());
        if (IStatus.ERROR != valid.getSeverity()) {
            fail(valid.toString());
        }
    }

    /**
     * ctor
     *
     * @param filePath
     *            the path
     */
    public CustomXmlTraceBadlyFormedTest(String filePath) {
        this.setPath(filePath);
    }

}
