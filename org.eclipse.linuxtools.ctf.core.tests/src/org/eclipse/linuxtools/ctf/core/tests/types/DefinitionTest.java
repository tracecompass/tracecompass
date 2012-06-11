package org.eclipse.linuxtools.ctf.core.tests.types;

import static org.junit.Assert.assertNotNull;

import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>DefinitionTest</code> contains tests for the class
 * <code>{@link Definition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class DefinitionTest {

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(DefinitionTest.class);
    }

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        // add additional set up code here
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Since Definition is abstract, we'll minimally extend it here to
     * instantiate it.
     */
    class DefTest extends Definition {

        public DefTest(IDefinitionScope definitionScope, String fieldName) {
            super(definitionScope, fieldName);
        }

        @Override
        public void read(BitBuffer input) {
            /* Just a test, no need to implement anything */
        }

        @Override
        public IDeclaration getDeclaration() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Test
    public void testToString() {
        Definition fixture = new DefTest(null, "Hello"); //$NON-NLS-1$
        String result = fixture.toString();

        assertNotNull(result);
    }
}