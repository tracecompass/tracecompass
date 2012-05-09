package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void testCtfTmfEventType_1() {
        String contextId = ""; //$NON-NLS-1$
        String eventName = ""; //$NON-NLS-1$
        ITmfEventField content = new TmfEventField("", new ITmfEventField[] {}); //$NON-NLS-1$

        CtfTmfEventType result = new CtfTmfEventType( eventName, content);

        // add additional test code here
        assertNotNull(result);
        assertEquals("", result.toString()); //$NON-NLS-1$
        assertEquals("", result.getName()); //$NON-NLS-1$
        assertEquals("Ctf Event", result.getContext()); //$NON-NLS-1$
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
        CtfTmfEventType fixture = new CtfTmfEventType("", new TmfEventField("", new ITmfEventField[] {})); //$NON-NLS-1$ //$NON-NLS-2$

        String result = fixture.toString();

        // add additional test code here
        assertEquals("", result); //$NON-NLS-1$
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