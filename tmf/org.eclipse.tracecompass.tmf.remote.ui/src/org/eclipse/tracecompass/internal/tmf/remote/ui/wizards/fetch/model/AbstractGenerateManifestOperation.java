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
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.remote.ui.wizards.fetch.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageOperation;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;

/**
 * Abstract operation that generates the manifest based on the content of a
 * remote node or import package.
 *
 * @author Marc-Andre Laperle
 * @author Bernd Hufmann
 */
public abstract class AbstractGenerateManifestOperation extends AbstractTracePackageOperation {

    /** A pattern to find where to substitute groups in the trace name */
    protected static final Pattern GROUP_PATTERN = Pattern.compile("\\(group(\\d+)\\)"); //$NON-NLS-1$

    /** Name of metadata file of trace */
    protected static final String METADATA_FILE_NAME = "metadata"; //$NON-NLS-1$
    /** Map of pattern to trace element */
    protected Map<Pattern, TracePackageTraceElement> fTemplatePatternsToTraceElements;

    /**
     * Constructs a new trace package operation
     *
     * @param fileName
     *            the output file name
     */
    public AbstractGenerateManifestOperation(String fileName) {
        super(fileName);
    }

    /**
     * Generates regular expression patterns from the template element.
     *
     * @param templateElements
     *            input template elements
     * @return map of generated {@link Pattern} to corresponding
     *         {@link TracePackageFilesElement}
     */
    protected Map<Pattern, TracePackageTraceElement> generatePatterns(TracePackageElement[] templateElements) {
        Map<Pattern, TracePackageTraceElement> templatePatterns = new HashMap<>();
        for (TracePackageElement templateElement : templateElements) {
            if (templateElement instanceof TracePackageTraceElement) {
                TracePackageElement[] children = templateElement.getChildren();
                if (children != null) {
                    for (TracePackageElement child : children) {
                        if (child instanceof TracePackageFilesElement) {
                            TracePackageFilesElement tracePackageFilesElement = (TracePackageFilesElement) child;
                            Pattern pattern = Pattern.compile(tracePackageFilesElement.getFileName());
                            templatePatterns.put(pattern, (TracePackageTraceElement) templateElement);
                        }
                    }
                }
            }
        }
        return templatePatterns;
    }

    /**
     * Returns a matching pair of {@link Pattern} to corresponding
     * {@link TracePackageFilesElement} from given path.
     *
     * @param fullArchivePath
     *            the input path to match
     * @return a matching pair of {@link Pattern} to corresponding
     *         {@link TracePackageFilesElement}
     */
    protected Entry<Pattern, TracePackageTraceElement> getMatchingTemplateElement(IPath fullArchivePath) {
        for (Entry<Pattern, TracePackageTraceElement> entry : fTemplatePatternsToTraceElements.entrySet()) {
            // Check for CTF trace (metadata)
            if (TmfTraceType.isDirectoryTraceType(entry.getValue().getTraceType())) {
                if (matchesDirectoryTrace(fullArchivePath, entry)) {
                    return entry;
                }
            } else if (entry.getKey().matcher(fullArchivePath.toPortableString()).matches()) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Returns whether {@link Pattern} and trace type of
     * {@link TracePackageFilesElement} matches a directory trace or not.
     *
     * @param archivePath
     *            the archive path
     * @param entry
     *            the map entry of {@link Pattern} to corresponding
     *            {@link TracePackageFilesElement}
     *
     * @return <code>true</code> for directory trace else false
     */
    protected boolean matchesDirectoryTrace(IPath archivePath, Entry<Pattern, TracePackageTraceElement> entry) {
        if (archivePath.lastSegment().equals(METADATA_FILE_NAME)) {
            IPath archiveParentPath = archivePath.removeLastSegments(1);
            if (entry.getKey().matcher(archiveParentPath.toPortableString()).matches()) {
                if (TmfTraceType.isDirectoryTraceType(entry.getValue().getTraceType())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Substitute group patterns inside the trace name with the groups found in
     * the file name. This allows renaming traces in flexible ways. For example:
     *
     * Filename: folder/blah.1.txt Filename pattern: folder/blah.(\d+).txt Trace
     * name: folder-blah.(group1).txt
     *
     * Result: folder-blah.1.txt
     *
     * @param traceName
     *            the target trace name that will get it's groups substituted
     * @param fileNamePattern
     *            the file name pattern that matched the filename and contains
     *            groups to be used in the substitutions
     * @param fileName
     *            the file name in the archive
     * @return the resulting String, with the (group#) patterns substituted
     */
    protected String substituteGroups(String traceName, Pattern fileNamePattern, String fileName) {
        String newString = traceName;

        Matcher fileNameMatcher = fileNamePattern.matcher(fileName);
        fileNameMatcher.find();
        int groupCount = fileNameMatcher.groupCount();
        Matcher matcher = GROUP_PATTERN.matcher(newString);
        while (matcher.find()) {
            // Found (group#), now get the #
            if (matcher.groupCount() == 1) {
                int groupNo = Integer.parseInt(matcher.group(1));
                if (groupNo <= groupCount) {
                    String substitutedString = newString.substring(0, matcher.start()) + fileNameMatcher.group(groupNo) + newString.substring(matcher.end());
                    if (!substitutedString.equals(newString)) {
                        // Since the string changed, create a new matcher so
                        // that the next match is at a valid position
                        newString = substitutedString;
                        matcher = GROUP_PATTERN.matcher(newString);
                    }
                }
            }
        }

        return newString;
    }
}
