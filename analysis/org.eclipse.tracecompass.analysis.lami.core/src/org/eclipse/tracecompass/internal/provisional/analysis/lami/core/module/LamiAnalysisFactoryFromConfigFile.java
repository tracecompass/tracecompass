/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Philippe Proulx
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

import org.eclipse.tracecompass.internal.analysis.lami.core.Activator;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.LamiConfigFileStrings;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.ShellUtils;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Factory which builds {@link LamiAnalysis} objects out of configuration
 * files.
 *
 * @author Philippe Proulx
 */
public final class LamiAnalysisFactoryFromConfigFile {

    private LamiAnalysisFactoryFromConfigFile() {}

    private static String getProperty(Properties props, String propName) throws LamiAnalysisFactoryException {
        String prop = props.getProperty(propName);

        if (prop == null) {
            throw new LamiAnalysisFactoryException(String.format("Cannot find \"%s\" property", propName)); //$NON-NLS-1$
        }

        prop = prop.trim();

        if (prop.isEmpty()) {
            throw new LamiAnalysisFactoryException(String.format("\"%s\" property cannot be empty", propName)); //$NON-NLS-1$
        }

        return prop;
    }

    /**
     * Builds a {@link LamiAnalysis} object from an input stream providing the
     * content of a configuration file.
     * <p>
     * The caller is responsible for opening and closing {@code inputStream}.
     *
     * @param inputStream
     *            Input stream for reading the configuration file; the stream is
     *            not closed by this method
     * @param isUserDefined
     *            {@code true} if the analysis to build is user-defined
     * @param appliesTo
     *            Predicate to use to check whether or not this analysis applies
     *            to a given trace
     * @return Built {@link LamiAnalysis} object
     * @throws LamiAnalysisFactoryException
     *             If something went wrong reading the input stream
     */
    public static LamiAnalysis buildFromInputStream(InputStream inputStream, boolean isUserDefined,
            Predicate<ITmfTrace> appliesTo) throws LamiAnalysisFactoryException {
        final Properties props = new Properties();

        // Load properties
        try {
            props.load(inputStream);
        } catch (IOException e) {
            throw new LamiAnalysisFactoryException(e);
        }

        // Get analysis' name and command
        final String name = getProperty(props, LamiConfigFileStrings.PROP_NAME);
        final String command = getProperty(props, LamiConfigFileStrings.PROP_COMMAND);

        // Get individual arguments from command string
        final List<String> args = ShellUtils.commandStringToArgs(command);

        return new LamiAnalysis(name, isUserDefined, appliesTo, args);
    }

    /**
     * Builds a {@link LamiAnalysis} object from a configuration file.
     *
     * @param configFilePath
     *            Configuration file path
     * @param isUserDefined
     *            {@code true} if the analysis to build is user-defined
     * @param appliesTo
     *            Predicate to use to check whether or not this analysis applies
     *            to a given trace
     * @return Built {@link LamiAnalysis} object
     * @throws LamiAnalysisFactoryException
     *             If something went wrong reading the file
     */
    public static LamiAnalysis buildFromConfigFile(Path configFilePath, boolean isUserDefined,
            Predicate<ITmfTrace> appliesTo) throws LamiAnalysisFactoryException {
        try (final FileInputStream propsStream = new FileInputStream(configFilePath.toFile())) {
            return buildFromInputStream(propsStream, isUserDefined, appliesTo);
        } catch (IOException e) {
            throw new LamiAnalysisFactoryException(e);
        }
    }

    /**
     * Builds a list of {@link LamiAnalysis} objects from a directory containing
     * configuration files.
     *
     * @param configDir
     *            Configuration directory containing the configuration files to
     *            load
     * @param isUserDefined
     *            {@code true} if the analyses to build are user-defined
     * @param appliesTo
     *            Predicate to use to check whether or not those analyses apply
     *            to a given trace
     * @return List of built {@link LamiAnalysis} objects
     * @throws LamiAnalysisFactoryException
     *             If something went wrong reading the directory
     */
    public static List<LamiAnalysis> buildFromConfigDir(Path configDir, boolean isUserDefined,
            Predicate<ITmfTrace> appliesTo) throws LamiAnalysisFactoryException {
        final List<LamiAnalysis> analyses = new ArrayList<>();

        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(configDir)) {
            for (final Path path : directoryStream) {
                try {
                    analyses.add(buildFromConfigFile(path, isUserDefined, appliesTo));
                } catch (LamiAnalysisFactoryException e) {
                    Activator.instance().logWarning(String.format("Cannot load external analysis \"%s\"", path)); //$NON-NLS-1$
                }
            }
        } catch (IOException e) {
            throw new LamiAnalysisFactoryException(e);
        }

        return analyses;
    }

}
