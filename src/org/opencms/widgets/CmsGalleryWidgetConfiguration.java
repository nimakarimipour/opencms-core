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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

/**
 * Configuration options for the gallery widget (e.g. DownloadGalleryWidget).
 *
 * <p>The configuration options are read from the configuration String of the widget. For nested XML
 * schemas the configuration String must be defined inside the nested content.
 *
 * <p>The configuration String has to be formatted as JSON object, with the following possible keys:
 *
 * <p>
 *
 * <ul>
 *   <li><code>class</code>: optional class implementing {@link
 *       I_CmsGalleryWidgetDynamicConfiguration} to dynamically configure startup parameters and
 *       format values.
 *   <li><code>startup</code>: the startup folder, can be dynamically generated by the provided
 *       class, in that case, use 'dynamic' as value.
 *   <li><code>type</code>: the startup folder type, can be 'gallery' or 'category'. Can be
 *       dynamically generated by the provided class, in that case, use 'dynamic' as value.
 * </ul>
 *
 * Example configurations can look like this:
 *
 * <p><code>{type: 'gallery', startup: '/demo_en/images/'}</code>
 *
 * <p><code>{type: 'category', startup: 'wurstsorten/kochwurst/'}</code>
 *
 * <p>
 *
 * @since 7.5.0
 */
public class CmsGalleryWidgetConfiguration {

  /** Configuration key name for the class configuration. */
  public static final String CONFIG_KEY_CLASS = "class";

  /** Configuration key name for the gallery types configuration. */
  public static final String CONFIG_KEY_GALLERYTYPES = "gallerytypes";

  /** Configuration key name for the startup configuration. */
  public static final String CONFIG_KEY_STARTUP = "startup";

  /** Configuration key name for the type configuration. */
  public static final String CONFIG_KEY_TYPE = "type";

  /** Configuration value name for a dynamic configuration. */
  public static final String CONFIG_VALUE_DYNAMIC = "dynamic";

  /**
   * The optional class name for generating dynamic configurations, must implement {@link
   * I_CmsGalleryWidgetDynamicConfiguration}.
   */
  protected String m_className;

  /** The required information for the initial item list to load. */
  protected String m_startup;

  /** The type of the initial item list to load, either gallery or category. */
  protected String m_type;

  /** The configured gallery types. */
  private String m_galleryTypes;

  /**
   * Generates an initialized configuration for the gallery item widget using the given
   * configuration string.
   *
   * <p>
   *
   * @param cms an initialized instance of a CmsObject
   * @param widgetDialog the dialog where the widget is used on
   * @param param the widget parameter to generate the widget for
   * @param configuration the widget configuration string
   */
  public CmsGalleryWidgetConfiguration(
      CmsObject cms, CmsMessages widgetDialog, I_CmsWidgetParameter param, String configuration) {

    init(cms, widgetDialog, param, configuration);
  }

  /**
   * Default constructor.
   *
   * <p>
   */
  protected CmsGalleryWidgetConfiguration() {

    // empty constructor is required for class registration
  }

  /**
   * Returns the optional class name for generating dynamic configurations, must implement {@link
   * I_CmsGalleryWidgetDynamicConfiguration}.
   *
   * <p>
   *
   * @return the optional class name for generating dynamic configurations
   */
  public String getClassName() {

    return m_className;
  }

  /**
   * Returns the configured gallery types.
   *
   * <p>
   *
   * @return the configured gallery types
   */
  public String getGalleryTypes() {

    return m_galleryTypes;
  }

  /**
   * Returns the required information for the initial item list to load.
   *
   * <p>If a gallery should be shown, the path to the gallery must be specified, for a category the
   * category path (e.g wurstsorten/kochwurst/).
   *
   * <p>
   *
   * @return the required information for the initial item list to load
   */
  public String getStartup() {

    return m_startup;
  }

  /**
   * Returns the type of the initial item list to load, either gallery or category.
   *
   * <p>
   *
   * @return the type of the initial image list to load
   */
  public String getType() {

    return m_type;
  }

  /**
   * Initializes the widget configuration using the given configuration string.
   *
   * <p>
   *
   * @param cms an initialized instance of a CmsObject
   * @param messages the dialog messages
   * @param param the widget parameter to generate the widget for
   * @param configuration the widget configuration string
   */
  protected void init(
      CmsObject cms, CmsMessages messages, I_CmsWidgetParameter param, String configuration) {

    if (configuration == null) {
      // no configuration String found, return
      return;
    }
    configuration = CmsMacroResolver.resolveMacros(configuration, cms, messages);
    JSONObject jsonObj = new JSONObject();
    try {
      jsonObj = new JSONObject(configuration);
    } catch (JSONException e) {
      // initialization failed
      return;
    }
    // determine the class name that fills in values dynamically
    setClassName(jsonObj.optString(CONFIG_KEY_CLASS, null));
    I_CmsGalleryWidgetDynamicConfiguration dynConf = null;
    if (getClassName() != null) {
      try {
        dynConf =
            (I_CmsGalleryWidgetDynamicConfiguration) Class.forName(getClassName()).newInstance();
      } catch (Exception e) {
        // class not found
      }
    }
    // determine the initial item list settings
    setType(jsonObj.optString(CONFIG_KEY_TYPE));
    if ((CONFIG_VALUE_DYNAMIC.equals(getType()) || CmsStringUtil.isEmpty(getType()))
        && (dynConf != null)) {
      setType(dynConf.getType(cms, messages, param));
    }
    setGalleryTypes(jsonObj.optString(CONFIG_KEY_GALLERYTYPES, null));
    setStartup(jsonObj.optString(CONFIG_KEY_STARTUP));
    if ((CONFIG_VALUE_DYNAMIC.equals(getStartup()) || CmsStringUtil.isEmpty(getStartup()))
        && (dynConf != null)) {
      setStartup(dynConf.getStartup(cms, messages, param));
    }
  }

  /**
   * Sets the optional class name for generating dynamic configurations, must implement {@link
   * I_CmsGalleryWidgetDynamicConfiguration}.
   *
   * <p>
   *
   * @param className the optional class name for generating dynamic configurations
   */
  protected void setClassName(String className) {

    m_className = className;
  }

  /**
   * Sets the configured gallery types.
   *
   * <p>
   *
   * @param galleryTypes the configured gallery types
   */
  protected void setGalleryTypes(String galleryTypes) {

    m_galleryTypes = galleryTypes;
  }

  /**
   * Sets the required information for the initial item list to load.
   *
   * <p>If a gallery should be shown, the path to the gallery must be specified, for a category the
   * category path.
   *
   * <p>
   *
   * @param startup the required information for the initial item list to load
   */
  protected void setStartup(String startup) {

    m_startup = startup;
  }

  /**
   * Sets the type of the initial item list to load, either gallery or category.
   *
   * <p>
   *
   * @param type the type of the initial item list to load
   */
  protected void setType(String type) {

    m_type = type;
  }

  /**
   * Returns the values as a parameter string.
   *
   * <p>
   *
   * @return the values as a parameter string
   */
  public String getConfigString() {

    String result = "";
    if (m_startup != null) {
      result += "&startup=" + m_startup;
    }
    if (m_type != null) {
      result += "&type=" + m_type;
    }

    return result;
  }
}
