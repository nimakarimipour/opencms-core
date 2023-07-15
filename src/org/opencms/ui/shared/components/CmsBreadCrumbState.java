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

package org.opencms.ui.shared.components;

import com.vaadin.shared.AbstractComponentState;

/**
 * The bread crumb state.
 *
 * <p>
 */
public class CmsBreadCrumbState extends AbstractComponentState {

  /** The serial version id. */
  private static final long serialVersionUID = -3379155393168861167L;

  /** The bread crumb entries. */
  private String[][] m_entries;

  /**
   * Returns the bread crumb entries.
   *
   * <p>
   *
   * @return the bread crumb entries
   */
  public String[][] getEntries() {

    return m_entries;
  }

  /**
   * Sets the bread crumb entries.
   *
   * <p>
   *
   * @param entries the bread crumb entries to set
   */
  public void setEntries(String[][] entries) {

    m_entries = entries;
  }
}
