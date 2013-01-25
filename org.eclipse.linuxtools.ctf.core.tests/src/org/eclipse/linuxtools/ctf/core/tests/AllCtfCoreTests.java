package org.eclipse.linuxtools.ctf.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all of
 * the tests within its package as well as within any subpackages of its
 * package.
 *
 * @author ematkho
 * @version 1.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    CtfCorePluginTest.class,
    org.eclipse.linuxtools.ctf.core.tests.event.TestAll.class,
    org.eclipse.linuxtools.ctf.core.tests.io.TestAll.class,
    org.eclipse.linuxtools.ctf.core.tests.trace.TestAll.class,
    org.eclipse.linuxtools.ctf.core.tests.types.TestAll.class
})
public class AllCtfCoreTests {

}
