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

package org.opencms.ade.contenteditor.shared.rpc;

import org.opencms.acacia.shared.CmsEntity;
import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.CmsEditHandlerData;
import org.opencms.ade.contenteditor.shared.CmsSaveResult;
import org.opencms.gwt.CmsRpcException;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;

/**
 * The content editor service interface.<p>
 */
public interface I_CmsContentService extends org.opencms.acacia.shared.rpc.I_CmsContentService {

    /** The content definition dictionary name. */
    String DICT_CONTENT_DEFINITION = "com_alkacon_acacia_shared_ContentDefinition";

    /** The back-link parameter. */
    String PARAM_BACKLINK = "backlink";

    /**
     * Calls the editor change handlers.<p>
     *
     * @param entityId the edited entity id
     * @param editedLocaleEntity the edited entity
     * @param skipPaths the locale synchronization value paths to skip
     * @param changedScopes the changed content value paths
     *
     * @return the changed content
     *
     * @throws CmsRpcException in case anything goes wrong
     */
    CmsContentDefinition callEditorChangeHandlers(
        @RUntainted String entityId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths,
        Collection<String> changedScopes)
    throws CmsRpcException;

    /**
     * Copies the given source locale to the target locales.<p>
     *
     * @param locales the target locales
     * @param sourceLocale the source locale
     *
     * @throws CmsRpcException if something goes wrong
     */
    void copyLocale(@RUntainted Collection<@RUntainted String> locales, CmsEntity sourceLocale) throws CmsRpcException;

    /**
     * Loads the content definition for a given entity.<p>
     *
     * @param entityId the entity id/URI
     * @param clientId the container element client id if available
     * @param editedLocaleEntity the edited locale entity
     * @param skipPaths the paths to skip during locale synchronization
     * @param settingPresets the presets for container element settings
     *
     * @return the content definition
     *
     * @throws Exception if something goes wrong processing the request
     */
    CmsContentDefinition loadDefinition(
        @RUntainted String entityId,
        String clientId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths,
        Map<String, String> settingPresets)
    throws Exception;

    /**
     * Loads the content definition for a given entity.<p>
     *
     * @param entityId the entity id/URI
     * @param clientId the container element client id if available
     * @param newLink the new link
     * @param modelFileId  the optional model file id
     * @param editContext the container page currently being edited (may be null)
     * @param mainLocale the main language to copy in case the element language node does not exist yet
     * @param mode the content creation mode
     * @param postCreateHandler the post-create handler class name
     * @param editHandlerData the edit handler data, if an edit handler is used for creating a new element; null otherwise
     * @param settingPresets the presets for container element settings
     * @param editorStylesheet the path of the editor style sheet (may be null)
     *
     * @return the content definition
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContentDefinition loadInitialDefinition(
        @RUntainted String entityId,
        String clientId,
        @RUntainted String newLink,
        CmsUUID modelFileId,
        @RUntainted String editContext,
        @RUntainted String mainLocale,
        String mode,
        String postCreateHandler,
        CmsEditHandlerData editHandlerData,
        Map<String, String> settingPresets,
        @RUntainted String editorStylesheet)
    throws CmsRpcException;

    /**
     * Loads new entity definition.<p>
     * This will load the entity representation of a new locale node.<p>
     *
     * @param entityId the entity id/URI
     * @param clientId the container element client id if available
     * @param editedLocaleEntity the edited locale entity
     * @param skipPaths the paths to skip during locale synchronization
     * @param settingPresets the presets for container element settings
     *
     * @return the content definition
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsContentDefinition loadNewDefinition(
        @RUntainted String entityId,
        String clientId,
        CmsEntity editedLocaleEntity,
        Collection<String> skipPaths,
        Map<String, String> settingPresets)
    throws CmsRpcException;

    /**
     * Returns the content definition of the resource requested through parameter 'resource'.<p>
     *
     * @return the content definition
     *
     * @throws CmsRpcException if something goes wrong
     */
    CmsContentDefinition prefetch() throws CmsRpcException;

    /**
     * Saves and deletes the given entities. Returns the editor save result information.<p>
     *
     * @param lastEditedEntity the last edited entity
     * @param clientId the container element client id if available
     * @param deletedEntities the entity id's to delete
     * @param skipPaths the paths to skip during locale synchronization
     * @param lastEditedLocale the last edited locale
     * @param clearOnSuccess  <code>true</code> to unlock resource after saving
     *
     * @return the editor save result information
     *
     * @throws CmsRpcException if something goes wrong processing the request
     */
    CmsSaveResult saveAndDeleteEntities(
        CmsEntity lastEditedEntity,
        String clientId,
        @RUntainted List<@RUntainted String> deletedEntities,
        Collection<String> skipPaths,
        @RUntainted String lastEditedLocale,
        boolean clearOnSuccess)
    throws CmsRpcException;

    /**
     * Saves a value in an existing XML content.<p>
     *
     * @param contentId the structure id of the content
     * @param contentPath the xpath of the value to set
     * @param locale the locale in which to set the value
     * @param value the value to set
     *
     * @return not used
     * @throws CmsRpcException if something goes wrong
     */
    String saveValue(@RUntainted String contentId, String contentPath, @RUntainted String locale, @RUntainted String value) throws CmsRpcException;
}
