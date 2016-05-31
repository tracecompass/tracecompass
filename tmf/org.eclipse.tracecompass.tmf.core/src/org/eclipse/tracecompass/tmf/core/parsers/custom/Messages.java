/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.parsers.custom;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.core.parsers.custom.messages"; //$NON-NLS-1$

    /** @since 2.2*/
    public static String CustomExtraFieldsAspect_extraFieldsAspectHelp;
    /** @since 2.2*/
    public static String CustomExtraFieldsAspect_extraFieldsAspectName;

    public static String CustomTrace_FileNotFound;

    // TODO: These strings should not be externalized
    /** @since 2.1 */
    public static String CustomTraceDefinition_eventTypeTag;
    public static String CustomTraceDefinition_messageTag;
    public static String CustomTraceDefinition_otherTag;
    public static String CustomTraceDefinition_timestampTag;
    /** @since 2.2*/
    public static String CustomTraceDefinition_extraFieldNameTag;
    /** @since 2.2*/
    public static String CustomTraceDefinition_extraFieldValueTag;
    public static String CustomTxtTraceDefinition_action;
    public static String CustomTxtTraceDefinition_cardinality;
    public static String CustomTxtTraceDefinition_category;
    public static String CustomTxtTraceDefinition_definition;
    public static String CustomTxtTraceDefinition_definitionRootElement;
    /** @since 2.1 */
    public static String CustomTxtTraceDefinition_eventType;
    public static String CustomTxtTraceDefinition_format;
    public static String CustomTxtTraceDefinition_inputData;
    public static String CustomTxtTraceDefinition_inputLine;
    public static String CustomTxtTraceDefinition_max;
    public static String CustomTxtTraceDefinition_min;
    public static String CustomTxtTraceDefinition_name;
    public static String CustomTxtTraceDefinition_outputColumn;
    public static String CustomTxtTraceDefinition_regEx;
    /** @since 2.1 */
    public static String CustomTxtTraceDefinition_tag;
    public static String CustomTxtTraceDefinition_timestampOutputFormat;
    public static String CustomXmlTraceDefinition_action;
    public static String CustomXmlTraceDefinition_attribute;
    public static String CustomXmlTraceDefinition_category;
    public static String CustomXmlTraceDefinition_definition;
    public static String CustomXmlTraceDefinition_definitionRootElement;
    /** @since 2.1 */
    public static String CustomXmlTraceDefinition_eventType;
    public static String CustomXmlTraceDefinition_format;
    public static String CustomXmlTraceDefinition_ignoreTag;
    public static String CustomXmlTraceDefinition_inputData;
    public static String CustomXmlTraceDefinition_inputElement;
    public static String CustomXmlTraceDefinition_logEntry;
    public static String CustomXmlTraceDefinition_name;
    public static String CustomXmlTraceDefinition_outputColumn;
    /** @since 2.1 */
    public static String CustomXmlTraceDefinition_tag;
    public static String CustomXmlTraceDefinition_timestampOutputFormat;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
