/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.remote.ui.tests.fetch;

import java.io.File;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.tmf.remote.ui.tests.TmfRemoteUITestPlugin;

/**
 * Abstract class to test reading and writing profiles files.
 */
public class AbstractRemoteImportProfilesIOTest {

    /** Valid profile path */
    protected static final Path VALID_PROFILE_PATH = new Path(
            "resources/valid_profile.xml"); //$NON-NLS-1$

    AbstractRemoteImportProfilesIOTest() {
        super();
    }

    /**
     * Get profiles file
     *
     * @param profilePath
     *            the profiles file path
     * @return the profiles file
     * @throws Exception
     *             if there is a failure getting the file
     */
    protected static File getProfilesFile(IPath profilePath) throws Exception {
        File file = new File(FileLocator.toFileURL(
                FileLocator.find(TmfRemoteUITestPlugin.getDefault().getBundle(),
                        profilePath, null)).toURI());
        return file;
    }
}