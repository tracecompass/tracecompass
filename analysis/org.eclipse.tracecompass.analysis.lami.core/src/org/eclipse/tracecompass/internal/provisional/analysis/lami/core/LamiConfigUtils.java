/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.tracecompass.internal.analysis.lami.core.Activator;

/**
 * Utilities related to user-defined LAMI analysis configuration
 * files.
 *
 * @author Philippe Proulx
 */
public class LamiConfigUtils {

    private static final String CONFIG_DIR = "user-defined-configs"; //$NON-NLS-1$

    private LamiConfigUtils() {
    }

    /**
     * Returns the path of the directory, in the workspace, where
     * configuration files are stored.
     *
     * @return Path to configuration directory
     */
    public static Path getConfigDirPath() {
        IPath path = Activator.instance().getStateLocation();
        path = path.addTrailingSeparator().append(CONFIG_DIR);

        /* Check if directory exists, otherwise create it */
        final File dir = path.toFile();

        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }

        return checkNotNull(dir.toPath());
    }

    private static Path getConfigFilePath(String name) {
        final Path configDirPath = getConfigDirPath();
        String normName = name.replaceAll("\\s+", "-"); //$NON-NLS-1$ //$NON-NLS-2$
        normName = normName.replaceAll("[^a-zA-Z0-9_]", ""); //$NON-NLS-1$ //$NON-NLS-2$

        return checkNotNull(Paths.get(configDirPath.toString(), normName + ".properties")); //$NON-NLS-1$
    }

    /**
     * Creates a new configuration file in the configuration directory.
     *
     * @param name
     *            Name of the external analysis
     * @param command
     *            Command of the external analysis
     * @return Path of created configuration file
     * @throws IOException
     *             If the file cannot be found or read
     */
    public static Path createConfigFile(String name, String command) throws IOException {
        final Properties props = new Properties();

        props.setProperty(LamiConfigFileStrings.PROP_NAME, name);
        props.setProperty(LamiConfigFileStrings.PROP_COMMAND, command);
        final Path configFilePath = getConfigFilePath(name);

        if (Files.exists(configFilePath)) {
            throw new IOException(String.format("Configuration file \"%s\" exists", configFilePath.toString())); //$NON-NLS-1$
        }

        try (final FileOutputStream out = new FileOutputStream(configFilePath.toFile())) {
            String userName = System.getProperty("user.name"); //$NON-NLS-1$

            if (userName == null) {
                userName = "unknown user"; //$NON-NLS-1$
            }

            final Date curDate = new Date();
            final String comment = String.format("Trace Compass external analysis descriptor created by user %s on %s", userName, curDate); //$NON-NLS-1$
            props.store(out, comment);
        }

        return configFilePath;
    }

    /**
     * Removes the configuration file which corresponds to the analysis named
     * {@code name}.
     *
     * @param name
     *            Analysis name
     * @throws IOException
     *             If there was an error attempting to delete the file
     */
    public static void removeConfigFile(String name) throws IOException {
        Path configFilePath = getConfigFilePath(name);
        Files.delete(configFilePath);
    }

}
