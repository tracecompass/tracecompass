/**********************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *********************************************************************/

package org.eclipse.linuxtools.lttng2.control.core.tests.session;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.internal.lttng2.control.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.IDomainInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.BufferType;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.impl.SessionInfo;
import org.eclipse.linuxtools.lttng2.control.core.session.SessionConfigGenerator;
import org.eclipse.linuxtools.lttng2.control.core.session.SessionConfigStrings;
import org.eclipse.linuxtools.lttng2.control.core.tests.model.impl.ModelImplFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class contains tests for the class {@link SessionConfigGenerator}.
 */
public class SessionConfigGeneratorTest {

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    /** Session files for validation */
    private static final File VALID_SESSION_FILE = new File("../org.eclipse.linuxtools.lttng2.control.core.tests/test_session_config_files/test_valid.lttng");
    private static final File INVALID_SESSION_FILE = new File("../org.eclipse.linuxtools.lttng2.control.core.tests/test_session_config_files/test_invalid.lttng");

    private static final String SESSION_FILENAME = "test_session." + SessionConfigStrings.SESSION_CONFIG_FILE_EXTENSION;
    private static final IPath SESSION_FILE_PATH = Activator.getDefault().getStateLocation().addTrailingSeparator().append(SESSION_FILENAME);
    private static final String TRACE_SESSION_PATH = "/home/user/folder";

    private static final String SESSION_NAME_1 = "session1";
    private static final String SESSION_NAME_2 = "session2";
    private static final String SESSION_NAME_3 = "session3";

    /** Session informations for generation tests */
    private ISessionInfo fValidSessionInfo = null;
    private ISessionInfo fValidSessionSnapshotInfo = null;
    private ISessionInfo fInvalidSessionInfo = null;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------
    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        /* A valid domain with shared buffer type */
        ModelImplFactory factory = new ModelImplFactory();
        IDomainInfo domain = factory.getDomainInfo1();
        domain.setBufferType(BufferType.BUFFER_SHARED);

        /* The valid sessions */
        fValidSessionInfo = new SessionInfo(SESSION_NAME_1);
        fValidSessionInfo.setSessionPath(TRACE_SESSION_PATH);
        fValidSessionInfo.setSessionState(TraceSessionState.ACTIVE);
        fValidSessionInfo.addDomain(domain);

        fValidSessionSnapshotInfo = new SessionInfo(SESSION_NAME_2);
        fValidSessionSnapshotInfo.setSessionPath(TRACE_SESSION_PATH);
        fValidSessionSnapshotInfo.setSessionState(TraceSessionState.ACTIVE);
        fValidSessionSnapshotInfo.addDomain(domain);
        fValidSessionSnapshotInfo.setSnapshotInfo(factory.getSnapshotInfo1());

        /* The invalid session contains an event with an invalid type */
        fInvalidSessionInfo = factory.getSessionInfo2();
        fInvalidSessionInfo.setName(SESSION_NAME_3);
    }

    /**
     * Delete the session file created
     */
    @After
    public void tearUp() {
        /* Tear up the file created if it exists */
        File sessionConfigurationFile = SESSION_FILE_PATH.toFile();
        if (sessionConfigurationFile.exists()) {
            sessionConfigurationFile.delete();
        }

    }

    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    /**
     * Test method for {@link SessionConfigGenerator#sessionValidate(File)}
     */
    @Test
    public void testSessionValidate() {
        File testSessionFile = VALID_SESSION_FILE;
        if ((testSessionFile == null) || !testSessionFile.exists()) {
            fail("Session test file does not exist");
        }
        IStatus status = SessionConfigGenerator.sessionValidate(testSessionFile);
        if (!status.isOK()) {
            fail(status.getMessage());
        }

        testSessionFile = INVALID_SESSION_FILE;
        if ((testSessionFile == null) || !testSessionFile.exists()) {
            fail("Session test file does not exist");
        }
        assertFalse(SessionConfigGenerator.sessionValidate(testSessionFile).isOK());
    }

    /**
     * Test method for
     * {@link SessionConfigGenerator#generateSessionConfig(Set, IPath)}
     */
    @Test
    public void testGenerateSessionConfig() {
        /* Should fail since it's empty */
        final Set<ISessionInfo> sessions = new HashSet<>();
        assertFalse(SessionConfigGenerator.generateSessionConfig(sessions, SESSION_FILE_PATH).isOK());

        /* Add a valid session and validate */
        sessions.add(fValidSessionInfo);
        assertTrue(SessionConfigGenerator.generateSessionConfig(sessions, SESSION_FILE_PATH).isOK());
        assertTrue(SessionConfigGenerator.sessionValidate(SESSION_FILE_PATH.toFile()).isOK());

        /* Add a valid snapshot session and validate */
        sessions.add(fValidSessionSnapshotInfo);
        assertTrue(SessionConfigGenerator.generateSessionConfig(sessions, SESSION_FILE_PATH).isOK());
        assertTrue(SessionConfigGenerator.sessionValidate(SESSION_FILE_PATH.toFile()).isOK());

        /* Add an invalid session */
        sessions.add(fInvalidSessionInfo);
        assertFalse(SessionConfigGenerator.generateSessionConfig(sessions, SESSION_FILE_PATH).isOK());
    }
}
