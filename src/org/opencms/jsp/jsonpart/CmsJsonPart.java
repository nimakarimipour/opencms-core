/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.jsonpart;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RPolyTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;

/**
 * Helper class used to translate key/value pairs from and to the format which can be processed by the
 * CmsJsonPartFilter.<p>
 */
public class CmsJsonPart {

    /** The terminator used for JSON parts. */
    public static final String END = "\u0007ENDJSONPART";

    /** Pattern used to detect the parts of the content which should be transformed to JSON. */
    private static @RUntainted Pattern FORMAT_PATTERN = Pattern.compile(
        "BEGINJSONPART\u0007(.*?)\u0007(.*?)\u0007ENDJSONPART",
        Pattern.DOTALL);

    /** The key. */
    private @RUntainted String m_key;

    /** The value. */
    private @RUntainted String m_value;

    /**
     * Creates a new instance.<p>
     *
     * @param key the key
     * @param value the value
     */
    public CmsJsonPart(@RUntainted String key, @RUntainted String value) {
        super();
        m_key = key;
        m_value = value;
    }

    /**
     * Gets the header section for a named JSON part.<p>
     *
     * @param key the JSON key for the part
     * @return the header section for the given key
     */
    public static final @RPolyTainted String getHeader(@RPolyTainted String key) {

        return "BEGINJSONPART\u0007" + key + "\u0007";
    }

    /**
     * Parses the encoded JSON parts from the given string and puts them in a list.<p>
     *
     * @param text the text containing the encoded JSON parts
     * @return the decoded JSON parts
     */
    public static List<CmsJsonPart> parseJsonParts(@RUntainted String text) {

        List<CmsJsonPart> result = Lists.newArrayList();
        Matcher matcher = FORMAT_PATTERN.matcher(text);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            CmsJsonPart part = new CmsJsonPart(key, value);
            result.add(part);
        }
        return result;
    }

    /**
     * Returns the key.<p>
     *
     * @return the key
     */
    public @RUntainted String getKey() {

        return m_key;
    }

    /**
     * Returns the value.<p>
     *
     * @return the value
     */
    public @RUntainted String getValue() {

        return m_value;
    }

}
