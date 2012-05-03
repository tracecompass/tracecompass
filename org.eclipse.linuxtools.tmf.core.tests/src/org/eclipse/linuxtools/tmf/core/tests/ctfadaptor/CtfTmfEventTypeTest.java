package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class <code>CtfTmfEventTypeTest</code> contains tests for the class <code>{@link CtfTmfEventType}</code>.
 *
 * @generatedBy CodePro at 03/05/12 2:29 PM
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CtfTmfEventTypeTest {
    /**
     * Run the CtfTmfEventType(String,String,ITmfEventField) constructor test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testCtfTmfEventType_1()
        throws Exception {
        String contextId = "";
        String eventName = "";
        ITmfEventField content = new TmfEventField("", new ITmfEventField[] {});

        CtfTmfEventType result = new CtfTmfEventType(contextId, eventName, content);

        // add additional test code here
        assertNotNull(result);
        assertEquals("", result.toString());
        assertEquals("", result.getName());
        assertEquals("", result.getContext());
    }

    /**
     * Run the String toString() method test.
     *
     * @throws Exception
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Test
    public void testToString_1()
        throws Exception {
        CtfTmfEventType fixture = new CtfTmfEventType("", "", new TmfEventField("", new ITmfEventField[] {}));

        String result = fixture.toString();

        // add additional test code here
        assertEquals("", result);
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws Exception
     *         if the initialization fails for some reason
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @Before
    public void setUp()
        throws Exception {
        // add additional set up code here
    }

    /**
     * Perform post-test clean-up.
     *
     * @throws Exception
     *         if the clean-up fails for some reason
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    @After
    public void tearDown()
        throws Exception {
        // Add additional tear down code here
    }

    /**
     * Launch the test.
     *
     * @param args the command line arguments
     *
     * @generatedBy CodePro at 03/05/12 2:29 PM
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(CtfTmfEventTypeTest.class);
    }
}