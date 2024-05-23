/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.search.solr;

import org.opencms.search.fields.CmsLuceneField;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.I_CmsSearchFieldMapping;

import java.util.List;
import java.util.Locale;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;

/**
 * An individual field for the Solr search index.<p>
 *
 * @since 8.5.0
 */
public class CmsSolrField extends CmsSearchField {

    /** The serial version UID. */
    private static final long serialVersionUID = -3920245109164517028L;

    /** The fields to copy the value of this field to. */
    private @RUntainted List<String> m_copyFields;

    /** The locale of this field. */
    private @RUntainted Locale m_locale;

    /** The name of the field. */
    private @RUntainted String m_targetField;

    /**
     * Public constructor.<p>
     *
     * @param luceneField the lucene field read from the configuration.
     */
    public CmsSolrField(CmsLuceneField luceneField) {

        super();
        String name = luceneField.getName();
        if (null != luceneField.getType()) {
            name = name + "_" + luceneField.getType();
        }
        setName(name);
        setDefaultValue(luceneField.getDefaultValue());

        for (I_CmsSearchFieldMapping mapping : luceneField.getMappings()) {
            addMapping(mapping);
        }
    }

    /**
     * Public constructor.<p>
     *
     * @param targetField the target field name
     * @param copyFields the field names to copy this field's value to
     * @param locale the locale
     * @param defaultValue the default value
     */
    public CmsSolrField(@RUntainted String targetField, @RUntainted List<@RUntainted String> copyFields, @RUntainted Locale locale, @RUntainted String defaultValue) {

        super(targetField, defaultValue);
        m_targetField = targetField;
        m_copyFields = copyFields;
        m_locale = locale;
    }

    /**
     * Returns the copy fields.<p>
     *
     * @return the copy fields.<p>
     */
    public @RUntainted List<String> getCopyFields() {

        return m_copyFields;
    }

    /**
     * Returns the locale of this field or <code>null</code> if the field does not have a locale.<p>
     *
     * @return the locale of this field
     */
    public @RUntainted Locale getLocale() {

        return m_locale;
    }

    /**
     * Returns the target field name.<p>
     *
     * @return the target field name
     */
    public @RUntainted String getTargetField() {

        return m_targetField;
    }

    /**
     * Sets the copy field names.<p>
     *
     * @param copyFields the field name to use as copy fields
     */
    public void setCopyFields(@RUntainted List<String> copyFields) {

        m_copyFields = copyFields;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(@RUntainted Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the target field name.<p>
     *
     * @param targetField the name to set
     */
    public void setTargetField(@RUntainted String targetField) {

        m_targetField = targetField;
    }

    /**
     * @see org.opencms.search.fields.CmsSearchField#toString()
     */
    @Override
    public @RUntainted String toString() {

        return getName()
            + "["
            + " defaultValue:"
            + getDefaultValue()
            + " targetField:"
            + getTargetField()
            + " locale:"
            + getLocale()
            + " copyFields:"
            + getCopyFields()
            + " ]";
    }
}
