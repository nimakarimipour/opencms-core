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

package org.opencms.db;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;

/**
 * The settings of the OpenCms memory monitor.<p>
 *
 * @since 6.0.0
 */
public class CmsCacheSettings {

    /** The size of the memory monitor's cache for ACLs. */
    private @RUntainted int m_aclCacheSize;

    /** The name of the class to generate cache keys. */
    private @RUntainted String m_cacheKeyGenerator;

    /** The size of the memory monitor's cache for groups. */
    private @RUntainted int m_groupCacheSize;

    /** The size of the memory monitor's cache for organizational units. */
    private @RUntainted int m_orgUnitCacheSize = -1; // this configuration entry is optional

    /** The size of the memory monitor's cache for permission checks. */
    private @RUntainted int m_permissionCacheSize;

    /** The size of the memory monitor's cache for projects. */
    private @RUntainted int m_projectCacheSize;

    /** The size of the memory monitor's cache for project resources. */
    private @RUntainted int m_projectResourcesCacheSize = -1; // this configuration entry is optional

    /** The size of the memory monitor's cache for properties. */
    private @RUntainted int m_propertyCacheSize;

    /** The size of the memory monitor's cache for property lists. */
    private @RUntainted int m_propertyListsCacheSize = -1; // this configuration entry is optional

    /** The size of the memory monitor's cache for resources. */
    private @RUntainted int m_resourceCacheSize;

    /** The size of the memory monitor's cache for lists of resources. */
    private @RUntainted int m_resourcelistCacheSize;

    /** The size of the memory monitor's cache for roles. */
    private @RUntainted int m_rolesCacheSize = -1; // this configuration entry is optional

    /** The size of the memory monitor's cache for users. */
    private @RUntainted int m_userCacheSize;

    /** The size of the memory monitor's cache for user/group relations. */
    private @RUntainted int m_userGroupsCacheSize;

    /**
     * Default constructor.<p>
     */
    public CmsCacheSettings() {

        super();
    }

    /**
     * Returns the size of the memory monitor's cache for ACLs.<p>
     *
     * @return the size of the memory monitor's cache for ACLs
     */
    public @RUntainted int getAclCacheSize() {

        return m_aclCacheSize;
    }

    /**
     * Returns the name of the class to generate cache keys.<p>
     *
     * @return the name of the class to generate cache keys
     */
    public @RUntainted String getCacheKeyGenerator() {

        return m_cacheKeyGenerator;
    }

    /**
     * Returns the size of the memory monitor's cache for organizational units.<p>
     *
     * Might be <code>-1</code> if configuration entry is missing.<p>
     *
     * @return the size of the memory monitor's cache for organizational units
     */
    public int getConfiguredOrgUnitCacheSize() {

        return m_orgUnitCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for project resources.<p>
     *
     * Might be <code>-1</code> if configuration entry is missing.<p>
     *
     * @return the size of the memory monitor's cache for project resources
     */
    public int getConfiguredProjectResourcesCacheSize() {

        return m_projectResourcesCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for property lists.<p>
     *
     * Might be <code>-1</code> if configuration entry is missing.<p>
     *
     * @return the size of the memory monitor's cache for property lists
     */
    public int getConfiguredPropertyListsCacheSize() {

        return m_propertyListsCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for roles.<p>
     *
     * Might be <code>-1</code> if configuration entry is missing.<p>
     *
     * @return the size of the memory monitor's cache for roles
     */
    public int getConfiguredRolesCacheSize() {

        return m_rolesCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for groups.<p>
     *
     * @return the size of the memory monitor's cache for groups
     */
    public @RUntainted int getGroupCacheSize() {

        return m_groupCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for organizational units.<p>
     *
     * @return the size of the memory monitor's cache for organizational units
     */
    public @RUntainted int getOrgUnitCacheSize() {

        if (m_orgUnitCacheSize < 0) {
            return getGroupCacheSize();
        }
        return m_orgUnitCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for permission checks.<p>
     *
     * @return the size of the memory monitor's cache for permission checks
     */
    public @RUntainted int getPermissionCacheSize() {

        return m_permissionCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for projects.<p>
     *
     * @return the size of the memory monitor's cache for projects
     */
    public @RUntainted int getProjectCacheSize() {

        return m_projectCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for project resources.<p>
     *
     * @return the size of the memory monitor's cache for project resources
     */
    public @RUntainted int getProjectResourcesCacheSize() {

        if (m_propertyListsCacheSize < 0) {
            return getProjectCacheSize();
        }
        return m_projectResourcesCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for properties.<p>
     *
     * @return the size of the memory monitor's cache for properties
     */
    public @RUntainted int getPropertyCacheSize() {

        return m_propertyCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for property lists.<p>
     *
     * @return the size of the memory monitor's cache for property lists
     */
    public @RUntainted int getPropertyListsCacheSize() {

        if (m_propertyListsCacheSize < 0) {
            return getPropertyCacheSize();
        }
        return m_propertyListsCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for resources.<p>
     *
     * @return the size of the memory monitor's cache for resources
     */
    public @RUntainted int getResourceCacheSize() {

        return m_resourceCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for lists of resources.<p>
     *
     * @return the size of the memory monitor's cache for lists of resources
     */
    public @RUntainted int getResourcelistCacheSize() {

        return m_resourcelistCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for roles.<p>
     *
     * @return the size of the memory monitor's cache for roles
     */
    public @RUntainted int getRolesCacheSize() {

        if (m_rolesCacheSize < 0) {
            return getPermissionCacheSize();
        }
        return m_rolesCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for users.<p>
     *
     * @return the size of the memory monitor's cache for users
     */
    public @RUntainted int getUserCacheSize() {

        return m_userCacheSize;
    }

    /**
     * Returns the size of the memory monitor's cache for user/group relations.<p>
     *
     * @return the size of the memory monitor's cache for user/group relations
     */
    public @RUntainted int getUserGroupsCacheSize() {

        return m_userGroupsCacheSize;
    }

    /**
     * Sets the size of the memory monitor's cache for ACLs.<p>
     *
     * @param size the size of the memory monitor's cache for ACLs
     */
    public void setAclCacheSize(@RUntainted String size) {

        m_aclCacheSize = getIntValue(size, 1024);
    }

    /**
     * Sets the name of the class to generate cache keys.<p>
     *
     * @param classname the name of the class to generate cache keys
     */
    public void setCacheKeyGenerator(@RUntainted String classname) {

        m_cacheKeyGenerator = classname;
    }

    /**
     * Sets the size of the memory monitor's cache for groups.<p>
     *
     * @param size the size of the memory monitor's cache for groups
     */
    public void setGroupCacheSize(@RUntainted String size) {

        m_groupCacheSize = getIntValue(size, 64);
    }

    /**
     * Sets the size of the memory monitor's cache for organizational units.<p>
     *
     * @param size the size of the memory monitor's cache for organizational units
     */
    public void setOrgUnitCacheSize(@RUntainted String size) {

        m_orgUnitCacheSize = getIntValue(size, 64);
    }

    /**
     * Sets the size of the memory monitor's cache for permission checks.<p>
     *
     * @param size the size of the memory monitor's cache for permission checks
     */
    public void setPermissionCacheSize(@RUntainted String size) {

        m_permissionCacheSize = getIntValue(size, 1024);
    }

    /**
     * Sets the size of the memory monitor's cache for projects.<p>
     *
     * @param size the size of the memory monitor's cache for projects
     */
    public void setProjectCacheSize(@RUntainted String size) {

        m_projectCacheSize = getIntValue(size, 32);
    }

    /**
     * Sets the size of the memory monitor's cache for project resources.<p>
     *
     * @param size the size of the memory monitor's cache for project resources
     */
    public void setProjectResourcesCacheSize(@RUntainted String size) {

        m_projectResourcesCacheSize = getIntValue(size, -1);
    }

    /**
     * Sets the size of the memory monitor's cache for properties.<p>
     *
     * @param size the size of the memory monitor's cache for properties
     */
    public void setPropertyCacheSize(@RUntainted String size) {

        m_propertyCacheSize = getIntValue(size, 128);
    }

    /**
     * Sets the size of the memory monitor's cache for property lists.<p>
     *
     * @param size the size of the memory monitor's cache for property lists
     */
    public void setPropertyListsCacheSize(@RUntainted String size) {

        m_propertyListsCacheSize = getIntValue(size, -1);
    }

    /**
     * Sets the size of the memory monitor's cache for resources.<p>
     *
     * @param size the size of the memory monitor's cache for resources
     */
    public void setResourceCacheSize(@RUntainted String size) {

        m_resourceCacheSize = getIntValue(size, 8192);
    }

    /**
     * Sets the size of the memory monitor's cache for lists of resources.<p>
     *
     * @param size the size of the memory monitor's cache for lists of resources
     */
    public void setResourcelistCacheSize(@RUntainted String size) {

        m_resourcelistCacheSize = getIntValue(size, 256);
    }

    /**
     * Sets the size of the memory monitor's cache for roles.<p>
     *
     * @param size the size of the memory monitor's cache for roles
     */
    public void setRolesCacheSize(@RUntainted String size) {

        m_rolesCacheSize = getIntValue(size, 8192);
    }

    /**
     * Sets the size of the memory monitor's cache for users.<p>
     *
     * @param size the size of the memory monitor's cache for users
     */
    public void setUserCacheSize(@RUntainted String size) {

        m_userCacheSize = getIntValue(size, 64);
    }

    /**
     * Sets the size of the memory monitor's cache for user/group relations.<p>
     *
     * @param size the size of the memory monitor's cache for user/group relations
     */
    public void setUserGroupsCacheSize(@RUntainted String size) {

        m_userGroupsCacheSize = getIntValue(size, 256);
    }

    /**
     * Turns a string into an int.<p>
     *
     * @param str the string to be converted
     * @param defaultValue a default value to be returned in case the string could not be parsed or the parsed int value is <= 0
     * @return the int value of the string
     */
    private @RUntainted int getIntValue(@RUntainted String str, @RUntainted int defaultValue) {

        try {
            int intValue = Integer.parseInt(str);
            return (intValue > 0) ? intValue : defaultValue;
        } catch (NumberFormatException e) {
            // intentionally left blank
        }
        return defaultValue;
    }
}
