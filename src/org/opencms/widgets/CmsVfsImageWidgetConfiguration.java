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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import java.util.ArrayList;
import java.util.List;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

/**
 * Configuration options for the VFS image widget.
 *
 * <p>The configuration options are read from the configuration String of the widget.
 *
 * <p>The configuration String has to be formatted as JSON object, with the following possible keys:
 *
 * <p>
 *
 * <ul>
 *   <li><code>class</code>: optional class implementing {@link
 *       I_CmsImageWidgetDynamicConfiguration} to dynamically configure startup parameters and
 *       format values.
 *   <li><code>formatnames</code>: list of format names to select, with pairs of selectable value
 *       and selectable text, e.g. value1:optiontext1|value2:optiontext2
 *   <li><code>formatvalues</code>: corresponding format values to the format names list, can be
 *       dynamically generated by the dynamic configuration class. The list of values should contain
 *       width and height information, with a '?' as sign for dynamic size and with an 'x' as
 *       separator for width and height. Example: ['200x?', '800x600']
 *   <li><code>scaleparams</code>: default scale parameters (no width, height or crop information
 *       should be provided!)
 *   <li><code>startup</code>: the startup folder, can be dynamically generated by the provided
 *       class, in that case, use 'dynamic' as value.
 *   <li><code>type</code>: the startup folder type, can be 'gallery' or 'category'. Can be
 *       dynamically generated by the provided class, in that case, use 'dynamic' as value.
 *   <li><code>usedescription</code>: indicates if the description input field for the image should
 *       be shown or not.
 *   <li><code>useformat</code>: indicates if the format select box for the image should be shown or
 *       not.
 * </ul>
 *
 * An example configuration can look like this:
 *
 * <p><code>{scaleparams: 'q:70,r:2,c:CCCC00', type: 'gallery', startup: '/demo_en/images/',
 * usedescription: true, useformat: true, formatnames: 'imageleft:Image left|imageright:Image right|imagetop:Image top',
 * formatvalues: ['150x?', '250x300', '?x250']}</code>
 *
 * <p>
 *
 * @since 7.5.0
 */
public class CmsVfsImageWidgetConfiguration extends CmsGalleryWidgetConfiguration {

  /** Configuration key name for the formatnames configuration. */
  public static final String CONFIG_KEY_FORMATNAMES = "formatnames";

  /** Configuration key name for the formatvalues configuration. */
  public static final String CONFIG_KEY_FORMATVALUES = "formatvalues";

  /** Configuration key name for the scaleparams configuration. */
  public static final String CONFIG_KEY_SCALEPARAMS = "scaleparams";

  /** Configuration key name for the usedescription configuration. */
  public static final String CONFIG_KEY_USEDESCRIPTION = "usedescription";

  /** Configuration key name for the useformat configuration. */
  public static final String CONFIG_KEY_USEFORMAT = "useformat";

  /** The type "category" for the initial image list to load. */
  public static final String TYPE_CATEGORY = "category";

  /** The type "gallery" for the initial image list to load. */
  public static final String TYPE_GALLERY = "gallery";

  /** The list of image format values matching the options for the format select box. */
  private List<String> m_formatValues;

  /** The scale parameters to apply to a scaled image (e.g. quality, type). */
  private String m_scaleParams;

  /**
   * The list of select options for the format select box, must contain {@link
   * CmsSelectWidgetOption} objects.
   */
  private List<CmsSelectWidgetOption> m_selectFormat;

  /** The select options for the format select box as String. */
  private String m_selectFormatString;

  /** The flag if the description field should be shown. */
  private boolean m_showDescription;

  /** The flag if the format select box should be shown. */
  private boolean m_showFormat;

  /**
   * Generates an initialized configuration for the image widget using the given configuration
   * string.
   *
   * <p>
   *
   * @param cms an initialized instance of a CmsObject
   * @param widgetDialog the dialog where the widget is used on
   * @param param the widget parameter to generate the widget for
   * @param configuration the widget configuration string
   */
  public CmsVfsImageWidgetConfiguration(
      CmsObject cms, CmsMessages widgetDialog, I_CmsWidgetParameter param, String configuration) {

    super();
    init(cms, widgetDialog, param, configuration);
  }

  /**
   * Returns the list of image format values matching the options for the format select box.
   *
   * <p>
   *
   * @return the list of image format values matching the options for the format select box
   */
  public List<String> getFormatValues() {

    return m_formatValues;
  }

  /**
   * Returns the scale parameters to apply to a scaled image (e.g. quality, type).
   *
   * <p>
   *
   * @return scale the parameters to apply to a scaled image
   */
  public String getScaleParams() {

    return m_scaleParams;
  }

  /**
   * Returns the list of select options for the format select box, must contain {@link
   * CmsSelectWidgetOption} objects.
   *
   * <p>
   *
   * @return the list of select options for the format select box
   */
  public List<CmsSelectWidgetOption> getSelectFormat() {

    return m_selectFormat;
  }

  /**
   * Returns the select options for the format select box as String.
   *
   * <p>The String has the following structure <code>
   * format name 1:localized name 1|format name 2:localized name 2|...</code>.
   *
   * <p>
   *
   * @return the select options for the format select box
   */
  public String getSelectFormatString() {

    return m_selectFormatString;
  }

  /**
   * Returns if the description field should be shown.
   *
   * <p>
   *
   * @return true if the description field should be shown, otherwise false
   */
  public boolean isShowDescription() {

    return m_showDescription;
  }

  /**
   * Returns if the format select box should be shown.
   *
   * <p>
   *
   * @return true if the format select box should be shown, otherwise false
   */
  public boolean isShowFormat() {

    return m_showFormat;
  }

  /**
   * Initializes the widget configuration using the given configuration string.
   *
   * <p>
   *
   * @param cms an initialized instance of a CmsObject
   * @param widgetDialog the dialog where the widget is used on
   * @param param the widget parameter to generate the widget for
   * @param configuration the widget configuration string
   */
  @Override
  protected void init(
      CmsObject cms, CmsMessages widgetDialog, I_CmsWidgetParameter param, String configuration) {

    if (configuration == null) {
      // no configuration String found, return
      return;
    }
    configuration = CmsMacroResolver.resolveMacros(configuration, cms, widgetDialog);
    JSONObject jsonObj = new JSONObject();
    try {
      jsonObj = new JSONObject(configuration);
    } catch (JSONException e) {
      // initialization failed
      return;
    }
    // determine the class name that fills in values dynamically
    setClassName(jsonObj.optString(CONFIG_KEY_CLASS, null));
    I_CmsImageWidgetDynamicConfiguration dynConf = null;
    if (getClassName() != null) {
      try {
        dynConf =
            (I_CmsImageWidgetDynamicConfiguration) Class.forName(getClassName()).newInstance();
      } catch (Exception e) {
        // class not found
      }
    }
    // determine if the description field should be shown
    setShowDescription(jsonObj.optBoolean(CONFIG_KEY_USEDESCRIPTION));
    // determine if the format select box should be shown
    setShowFormat(jsonObj.optBoolean(CONFIG_KEY_USEFORMAT));
    if (isShowFormat()) {
      // only parse options if the format select box should be shown
      String optionsStr = (String) jsonObj.opt(CONFIG_KEY_FORMATNAMES);
      setSelectFormatString(optionsStr);
      setSelectFormat(CmsSelectWidgetOption.parseOptions(optionsStr));
      // get the corresponding format values as well
      JSONArray formatValues = jsonObj.optJSONArray(CONFIG_KEY_FORMATVALUES);
      if (formatValues != null) {
        List<String> formatValueList = new ArrayList<String>(formatValues.length());
        for (int i = 0; i < formatValues.length(); i++) {
          formatValueList.add(formatValues.optString(i));
        }
        setFormatValues(formatValueList);
      }
      if (dynConf != null) {
        setFormatValues(
            dynConf.getFormatValues(
                cms, widgetDialog, param, getSelectFormat(), getFormatValues()));
      }
    }
    // determine the initial image list settings
    setType(jsonObj.optString(CONFIG_KEY_TYPE));
    if ((CONFIG_VALUE_DYNAMIC.equals(getType()) || CmsStringUtil.isEmpty(getType()))
        && (dynConf != null)) {
      setType(dynConf.getType(cms, widgetDialog, param));
    }
    setStartup(jsonObj.optString(CONFIG_KEY_STARTUP));
    if ((CONFIG_VALUE_DYNAMIC.equals(getStartup()) || CmsStringUtil.isEmpty(getStartup()))
        && (dynConf != null)) {
      setStartup(dynConf.getStartup(cms, widgetDialog, param));
    }
    // determine the scale parameters
    setScaleParams(jsonObj.optString(CONFIG_KEY_SCALEPARAMS));
  }

  /**
   * Sets the list of image format values matching the options for the format select box.
   *
   * <p>
   *
   * @param formatValues the list of image format values matching the options for the format select
   *     box
   */
  private void setFormatValues(List<String> formatValues) {

    m_formatValues = formatValues;
  }

  /**
   * Sets the scale parameters to apply to a scaled image (e.g. quality, type).
   *
   * <p>
   *
   * @param scaleParams the scale parameters to apply to a scaled image
   */
  private void setScaleParams(String scaleParams) {

    m_scaleParams = scaleParams;
  }

  /**
   * Sets the list of select options for the format select box, must contain {@link
   * CmsSelectWidgetOption} objects.
   *
   * <p>
   *
   * @param selectFormat the list of select options for the format select box
   */
  private void setSelectFormat(List<CmsSelectWidgetOption> selectFormat) {

    m_selectFormat = selectFormat;
  }

  /**
   * Sets the select options for the format select box as String.
   *
   * <p>
   *
   * @param formatString the select options for the format select box as String
   */
  private void setSelectFormatString(String formatString) {

    m_selectFormatString = formatString;
  }

  /**
   * Sets if the description field should be shown.
   *
   * <p>
   *
   * @param showDescription true if the description field should be shown, otherwise false
   */
  private void setShowDescription(boolean showDescription) {

    m_showDescription = showDescription;
  }

  /**
   * Sets if the format select box should be shown.
   *
   * <p>
   *
   * @param showFormat true if the format select box should be shown, otherwise false
   */
  private void setShowFormat(boolean showFormat) {

    m_showFormat = showFormat;
  }

  /**
   * Returns the values as a parameter string.
   *
   * <p>
   *
   * @return the values as a parameter string
   */
  @Override
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
