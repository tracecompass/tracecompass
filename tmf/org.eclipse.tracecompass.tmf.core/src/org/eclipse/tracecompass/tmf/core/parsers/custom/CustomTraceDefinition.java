/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Base class for custom trace definitions.
 *
 * @author Patrick Tassé
 */
public abstract class CustomTraceDefinition {

    /** "set" action */
    public static final int ACTION_SET = 0;

    /** "append" action */
    public static final int ACTION_APPEND = 1;

    /** "append with separator" action */
    public static final int ACTION_APPEND_WITH_SEPARATOR = 2;

    /**
     * Input tag
     *
     * @since 2.1
     */
    public enum Tag {
        /** Ignore */
        IGNORE(Messages.CustomXmlTraceDefinition_ignoreTag),
        /** Timestamp */
        TIMESTAMP(TmfBaseAspects.getTimestampAspect().getName()),
        /** Event type */
        EVENT_TYPE(TmfBaseAspects.getEventTypeAspect().getName()),
        /** Message */
        MESSAGE(Messages.CustomTraceDefinition_messageTag),
        /** Extra field name
         * @since 2.2*/
        EXTRA_FIELD_NAME(Messages.CustomTraceDefinition_extraFieldNameTag),
        /** Extra field value
         * @since 2.2*/
        EXTRA_FIELD_VALUE(Messages.CustomTraceDefinition_extraFieldValueTag),
        /**
         * Extra fields
         * <p>
         * Used as output tag corresponding to the {@link #EXTRA_FIELD_NAME} and
         * {@link #EXTRA_FIELD_VALUE} input tags.
         * @since 2.2
         */
        EXTRA_FIELDS(Messages.CustomExtraFieldsAspect_extraFieldsAspectName),
        /** Other */
        OTHER(Messages.CustomTraceDefinition_otherTag);

        private final String fLabel;

        private Tag(String label) {
            fLabel = label;
        }

        @Override
        public String toString() {
            return fLabel;
        }

        /**
         * Get a tag from its label (toString).
         *
         * @param label
         *            the label
         * @return the corresponding tag, or null
         */
        public static Tag fromLabel(String label) {
            for (Tag tag : Tag.values()) {
                if (tag.toString().equals(label)) {
                    return tag;
                }
            }
            return null;
        }

        /**
         * Get a tag from its name (identifier).
         *
         * @param name
         *            the name
         * @return the corresponding tag, or null
         */
        public static Tag fromName(String name) {
            for (Tag tag : Tag.values()) {
                if (tag.name().equals(name)) {
                    return tag;
                }
            }
            return null;
        }
    }

    /**
     * Separator to use with the
     * {@link CustomTraceDefinition#ACTION_APPEND_WITH_SEPARATOR}
     *
     * @since 2.2
     */
    public static final @NonNull String SEPARATOR = " | ";  //$NON-NLS-1$

    private static final String TMF_CUSTOM_TRACE_BUILTIN_EXTENSION_ID = "org.eclipse.tracecompass.tmf.core.custom.trace"; //$NON-NLS-1$
    private static final String ATTRIBUTE_NAME_FILE = "file"; //$NON-NLS-1$
    private static final String ATTRIBUTE_NAME_TRACE_CONTENT_TYPE = "traceContentType"; //$NON-NLS-1$
    private static final String ELEMENT_NAME_CUSTOM_TRACE = "customTrace"; //$NON-NLS-1$

    /** Category of this trace definition */
    public String categoryName;

    /** Name of this trace definition */
    public String definitionName;

    /** List of output columns */
    public List<OutputColumn> outputs;

    /** Timestamp format */
    public String timeStampOutputFormat;

    /**
     * Definition of an output column
     */
    public static class OutputColumn {

        /** Tag of this input
         * @since 2.1*/
        public @NonNull Tag tag;

        /** Name of this column */
        public @NonNull String name;

        /**
         * Constructor
         *
         * @param tag
         *            Tag of this output column
         * @param name
         *            Name of this output column
         * @since 2.1
         */
        public OutputColumn(@NonNull Tag tag, @NonNull String name) {
            this.tag = tag;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Format a timestamp in this trace's current time stamp format.
     *
     * @param timestamp
     *            The timestamp to format
     * @return The same timestamp as a formatted string
     */
    public String formatTimeStamp(TmfTimestamp timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeStampOutputFormat);
        return simpleDateFormat.format(timestamp.getValue());
    }

    /**
     * Save this custom trace in the default path.
     */
    public abstract void save();

    /**
     * Save this custom trace in the supplied path.
     *
     * @param path
     *            The path to save to
     */
    public abstract void save(String path);

    /**
     * Creates a new empty entity resolver
     *
     * @return a new entity resolver
     */
    protected static EntityResolver createEmptyEntityResolver() {
        return (publicId, systemId) -> {
            String empty = ""; //$NON-NLS-1$
            ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
            return new InputSource(bais);
        };
    }

    /**
     * Creates an error handler for parse exceptions
     *
     * @return a new error handler
     */
    protected static ErrorHandler createErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void error(SAXParseException saxparseexception) throws SAXException {
                // Do nothing
            }

            @Override
            public void warning(SAXParseException saxparseexception) throws SAXException {
                // Do nothing
            }

            @Override
            public void fatalError(SAXParseException saxparseexception) throws SAXException {
                throw saxparseexception;
            }
        };
    }

    /**
     * Extract the tag and name from an XML element
     *
     * @param element
     *            the XML element
     * @param tagAttribute
     *            the tag attribute
     * @param nameAttribute
     *            the name attribute
     * @return an entry where the key is the tag and the value is the name
     * @since 2.1
     */
    protected static Entry<@NonNull Tag, @NonNull String> extractTagAndName(Element element, String tagAttribute, String nameAttribute) {
        Tag tag = Tag.fromName(element.getAttribute(tagAttribute));
        String name = element.getAttribute(nameAttribute);
        if (tag == null) {
            // Backward compatibility
            if (name.equals(Messages.CustomTraceDefinition_timestampTag)) {
                tag = Tag.TIMESTAMP;
                name = checkNotNull(Tag.TIMESTAMP.toString());
            } else if (name.equals(Messages.CustomTraceDefinition_messageTag)) {
                tag = Tag.MESSAGE;
                name = checkNotNull(Tag.MESSAGE.toString());
            } else if (name.equals(Messages.CustomXmlTraceDefinition_ignoreTag)) {
                tag = Tag.IGNORE;
                name = checkNotNull(Tag.IGNORE.toString());
            } else {
                tag = Tag.OTHER;
            }
        } else if (name.isEmpty()) {
            name = checkNotNull(tag.toString());
        }
        return new SimpleEntry<>(tag, name);
    }

    /**
     * Return the category of this trace definition
     *
     * @return The category of this trace definition
     * @since 5.1
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Return the name of this trace definition
     *
     * @return The name of this trace definition
     * @since 5.1
     */
    public String getDefinitionName() {
        return definitionName;
    }

    /**
     * Get all the custom trace definition paths contributed by extensions, for
     * a given content type (XML or Text).
     *
     * @param traceContentTypeToLoad
     *            XML or Text (extension attribute value)
     * @return the paths
     *
     * Note: This method is package-visible by design.
     */
    static final Collection<String> getExtensionDefinitionsPaths(String traceContentTypeToLoad) {
        List<String> extensionDefinitionsPaths = new ArrayList<>();
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_CUSTOM_TRACE_BUILTIN_EXTENSION_ID);
        for (IConfigurationElement element : elements) {
            if (!element.getName().equals(ELEMENT_NAME_CUSTOM_TRACE)) {
                continue;
            }

            final String traceContentType = element.getAttribute(ATTRIBUTE_NAME_TRACE_CONTENT_TYPE);
            if (!traceContentType.equals(traceContentTypeToLoad)) {
                continue;
            }

            final String filename = element.getAttribute(ATTRIBUTE_NAME_FILE);
            final String name = element.getContributor().getName();
            SafeRunner.run(new ISafeRunnable() {
                @Override
                public void run() throws IOException {
                    if (name != null) {
                        Bundle bundle = Platform.getBundle(name);
                        if (bundle != null) {
                            URL xmlUrl = bundle.getResource(filename);
                            URL locatedURL = FileLocator.toFileURL(xmlUrl);
                            extensionDefinitionsPaths.add(locatedURL.getPath());
                        }
                    }
                }

                @Override
                public void handleException(Throwable exception) {
                    // Handled sufficiently in SafeRunner
                }
            });

        }
        return extensionDefinitionsPaths;
    }

}
