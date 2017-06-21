/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class containing some utilities methods that can be used by analyses
 * extending the XML schema
 *
 * @author Geneviève Bastien
 * @since 2.2
 */
public final class TmfXmlUtils {

    private TmfXmlUtils() {

    }

    /**
     * Get the XML children element of an XML element, but only those of a
     * certain type
     *
     * @param parent
     *            The parent element to get the children from
     * @param elementTag
     *            The tag of the elements to return
     * @return The list of children {@link Element} of the parent
     */
    public static List<@NonNull Element> getChildElements(Element parent, String elementTag) {
        /* get the state providers and find the corresponding one */
        NodeList nodes = parent.getElementsByTagName(elementTag);
        List<@NonNull Element> childElements = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (node.getParentNode().equals(parent)) {
                childElements.add(node);
            }
        }
        return childElements;
    }

    /**
     * Return the node element corresponding to the requested type in the file.
     *
     * TODO: Nothing prevents from having duplicate type -> id in a same file.
     * That should not be allowed. If you want an element with the same ID as
     * another one, it should be in a different file and we should check it at
     * validation time.
     *
     * @param filePath
     *            The absolute path to the XML file
     * @param elementType
     *            The type of top level element to search for
     * @param elementId
     *            The ID of the desired element
     * @return The XML element or <code>null</code> if not found
     */
    public static Element getElementInFile(String filePath, @NonNull String elementType, @NonNull String elementId) {

        if (filePath == null) {
            return null;
        }

        IPath path = new Path(filePath);
        File file = path.toFile();
        if (file == null || !file.exists() || !file.isFile() || !XmlUtils.xmlValidate(file).isOK()) {
            return null;
        }

        try {
            Document doc = XmlUtils.getDocumentFromFile(file);

            /* get the state providers and find the corresponding one */
            NodeList nodes = doc.getElementsByTagName(elementType);
            Element foundNode = null;

            for (int i = 0; i < nodes.getLength(); i++) {
                Element node = (Element) nodes.item(i);
                String id = node.getAttribute(TmfXmlStrings.ID);
                if (id.equals(elementId)) {
                    foundNode = node;
                }
            }
            return foundNode;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }

    }

    /**
     * Return the ITmfStateValue.Type corresponding to the given type name.
     *
     * @param typeName
     *              The ITmfStateValue.Type name.
     *
     * @return The ITmfStateValue.Type
     */
     public static ITmfStateValue.@NonNull Type getTmfStateValueByName(@NonNull String typeName){

        ITmfStateValue.Type type;
        switch (typeName) {
        case TmfXmlStrings.TYPE_STRING:
            type = ITmfStateValue.Type.STRING;
            break;
        case TmfXmlStrings.TYPE_INT:
            type = ITmfStateValue.Type.INTEGER;
            break;
        case TmfXmlStrings.TYPE_LONG:
            type = ITmfStateValue.Type.LONG;
            break;
        case TmfXmlStrings.TYPE_DOUBLE:
            type = ITmfStateValue.Type.DOUBLE;
            break;
        case TmfXmlStrings.TYPE_CUSTOM:
            type = ITmfStateValue.Type.CUSTOM;
            break;
        case TmfXmlStrings.TYPE_NULL:
            type = ITmfStateValue.Type.NULL;
            break;
        default:
            throw new IllegalArgumentException("The given type name \"" + typeName  //$NON-NLS-1$
                    + "\" does not correspond to any ITmfStateValue.Type"); //$NON-NLS-1$
        }
        return type;
    }

     /**
      * Factory constructor for Object state values
      *
      * @param objValue
      *            The object value to contain
      * @return The newly-created TmfStateValue object
      * @since 3.0
      */
    public static @NonNull TmfStateValue newTmfStateValueFromObject(@Nullable Object objValue) {
        TmfStateValue value = TmfStateValue.nullValue();
        if (objValue instanceof String) {
            value = TmfStateValue.newValueString((String) objValue);
        } else if (objValue instanceof Long) {
            value = TmfStateValue.newValueLong((Long) objValue);
        } else if (objValue instanceof Integer) {
            value = TmfStateValue.newValueInt((Integer) objValue);
        } else if (objValue instanceof Double) {
            value = TmfStateValue.newValueDouble((Double) objValue);
        }
        return value;
    }

    /**
     * Factory constructor for Object state values with a forced type.
     *
     * @param objValue
     *            The object value to contain
     * @param forcedType
     *            The forced type
     * @return The newly-created TmfStateValue object
     * @since 3.0
     */
    public static @NonNull TmfStateValue newTmfStateValueFromObjectWithForcedType(@Nullable Object objValue, ITmfStateValue.@NonNull Type forcedType) {
        if (objValue == null) {
            return TmfStateValue.nullValue();
        }
        TmfStateValue value = TmfStateValue.nullValue();
        if (objValue instanceof String) {
            String fieldString = (String) objValue;
            switch (forcedType) {
            case INTEGER:
                value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                break;
            case LONG:
                value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
                break;
            case DOUBLE:
                value = TmfStateValue.newValueDouble(Double.parseDouble(fieldString));
                break;
            case CUSTOM:
                throw new IllegalStateException("Custom type cannot be forced"); //$NON-NLS-1$
            case STRING:
            case NULL:
            default:
                value = TmfStateValue.newValueString(fieldString);
                break;
            }
        } else if (objValue instanceof Long) {
            Long fieldLong = (Long) objValue;
            switch (forcedType) {
            case INTEGER:
                value = TmfStateValue.newValueInt(fieldLong.intValue());
                break;
            case STRING:
                value = TmfStateValue.newValueString(fieldLong.toString());
                break;
            case DOUBLE:
                value = TmfStateValue.newValueDouble(fieldLong.doubleValue());
                break;
            case CUSTOM:
                throw new IllegalStateException("Custom type cannot be forced"); //$NON-NLS-1$
            case LONG:
            case NULL:
            default:
                value = TmfStateValue.newValueLong(fieldLong);
                break;
            }
        } else if (objValue instanceof Integer) {
            Integer fieldInteger = (Integer) objValue;
            switch (forcedType) {
            case LONG:
                value = TmfStateValue.newValueLong(fieldInteger.longValue());
                break;
            case STRING:
                value = TmfStateValue.newValueString(fieldInteger.toString());
                break;
            case DOUBLE:
                value = TmfStateValue.newValueDouble(fieldInteger.doubleValue());
                break;
            case CUSTOM:
                throw new IllegalStateException("Custom type cannot be forced"); //$NON-NLS-1$
            case INTEGER:
            case NULL:
            default:
                value = TmfStateValue.newValueInt(fieldInteger);
                break;
            }
        } else if (objValue instanceof Double) {
            Double fieldDouble = (Double) objValue;
            switch (forcedType) {
            case LONG:
                value = TmfStateValue.newValueLong(fieldDouble.longValue());
                break;
            case STRING:
                value = TmfStateValue.newValueString(fieldDouble.toString());
                break;
            case INTEGER:
                value = TmfStateValue.newValueInt(fieldDouble.intValue());
                break;
            case CUSTOM:
                throw new IllegalStateException("Custom type cannot be forced"); //$NON-NLS-1$
            case DOUBLE:
            case NULL:
            default:
                value = TmfStateValue.newValueDouble(fieldDouble);
                break;
            }
        }
        return value;
    }
}
