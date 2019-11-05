/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.cli.core.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.cli.core.parser.ICliParser;
import org.eclipse.tracecompass.internal.tmf.cli.core.Activator;

/**
 * A class accessing the configuration elements for the CLI action.
 *
 * @author Geneviève Bastien
 */
public final class CliParserConfigElement {

    /** Extension point ID */
    private static final String TMF_CLI_ACTION_TYPE_ID = "org.eclipse.tracecompass.tmf.cli.parser.extension"; //$NON-NLS-1$

    /** Extension point element 'action' */
    private static final String PARSER_ELEMENT = "parser"; //$NON-NLS-1$

    /** Extension point element 'class' */
    private static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    /** Extension point element 'class' */
    private static final String PRIORITY_ATTR = "priority"; //$NON-NLS-1$

    private static @Nullable CliParserConfigElement INSTANCE = null;

    private final List<ICliParser> fCliParsers = new ArrayList<>();

    /**
     * Constructor
     */
    private CliParserConfigElement() {
        populateCliActionList();
    }

    /**
     * Get the instance of this class
     *
     * @return The instance of the class
     */
    public static CliParserConfigElement getInstance() {
        CliParserConfigElement instance = INSTANCE;
        if (instance == null) {
            instance = new CliParserConfigElement();
            INSTANCE = instance;
        }
        return instance;
    }

    private static final Comparator<PrioritizedParser> COMPARATOR = Objects.requireNonNull(Comparator.comparingInt(PrioritizedParser::getPriority).thenComparing((o1, o2) -> (o1 == o2) ? 0 : 1));

    private static class PrioritizedParser {
        private final int fPriority;
        private final ICliParser fParser;

        public PrioritizedParser(ICliParser parser, int priority) {
            fPriority = priority;
            fParser = parser;
        }

        public int getPriority() {
            return fPriority;
        }

    }

    private void populateCliActionList() {
        // Add the parsers sorted by priority in a set
        Set<PrioritizedParser> parsers = new TreeSet<>(COMPARATOR);
        if (fCliParsers.isEmpty()) {
            // Populate the analysis module list
            IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_CLI_ACTION_TYPE_ID);
            for (IConfigurationElement ce : config) {
                String elementName = ce.getName();
                if (elementName.equals(PARSER_ELEMENT)) {
                    try {
                        ICliParser parser = Objects.requireNonNull((ICliParser) ce.createExecutableExtension(CLASS_ATTR));
                        String attribute = ce.getAttribute(PRIORITY_ATTR);
                        int priority = 5;
                        try {
                            priority = Integer.parseInt(attribute);
                        } catch(NumberFormatException e) {
                            // Nothing to do, fallback to the value of 5
                        }
                        parsers.add(new PrioritizedParser(parser, priority));
                    } catch (CoreException e) {
                        Activator.getInstance().logError("Error creation cli action object: ", e); //$NON-NLS-1$
                    }
                }
            }
            // Insert ordered parsers in the parser list
            for (PrioritizedParser parser : parsers) {
                fCliParsers.add(parser.fParser);
            }
        }
    }

    /**
     * Get the CLI actions advertised through the extension point, sorted by
     * their priority
     *
     * @return A collection of cli parsers
     */
    public Collection<ICliParser> getParsers() {
        return fCliParsers;
    }

}
