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

package org.opencms.ui.apps.unusedcontentfinder;

import org.opencms.main.CmsLog;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsFileExplorer;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsComponentState;
import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;

/**
 * Vaadin app to find unused contents.<p>
 */
public class CmsUnusedContentFinderApp extends A_CmsWorkplaceApp {

    /** The log object for this class. */
    static final Log LOG = CmsLog.getLog(CmsUnusedContentFinderApp.class);

    /** The unused content finder composite. */
    private CmsUnusedContentFinderComposite m_unusedContentFinderComposite;

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#initUI(org.opencms.ui.apps.I_CmsAppUIContext)
     */
    @Override
    public void initUI(I_CmsAppUIContext context) {

        context.addPublishButton(changed -> {
            if (m_unusedContentFinderComposite != null) {
                m_unusedContentFinderComposite.search(false);
            }
        });
        super.initUI(context);
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getBreadCrumbForState(java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getBreadCrumbForState(String state) {

        LinkedHashMap<String, String> crumbs = new LinkedHashMap<>();
        crumbs.put(
            "",
            Messages.get().getBundle(A_CmsUI.get().getLocale()).key(Messages.GUI_UNUSED_CONTENT_FINDER_TITLE_0));
        return crumbs;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getComponentForState(java.lang.String)
     */
    @Override
    protected Component getComponentForState(@RUntainted String state) {

        m_rootLayout.setMainHeightFull(true);
        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        splitPanel.setSizeFull();
        m_unusedContentFinderComposite = new CmsUnusedContentFinderComposite();
        splitPanel.setFirstComponent(m_unusedContentFinderComposite.getFormComponent());
        splitPanel.setSecondComponent(m_unusedContentFinderComposite.getResultComponent());
        splitPanel.setSplitPosition(CmsFileExplorer.LAYOUT_SPLIT_POSITION, Unit.PIXELS);
        m_infoLayout.addComponent(m_unusedContentFinderComposite.getResultFilterComponent());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(state)) {
            CmsComponentState componentState = new CmsComponentState(state);
            m_unusedContentFinderComposite.setState(componentState);
            m_unusedContentFinderComposite.search(false);
        }
        return splitPanel;
    }

    /**
     * @see org.opencms.ui.apps.A_CmsWorkplaceApp#getSubNavEntries(java.lang.String)
     */
    @Override
    protected List<NavEntry> getSubNavEntries(String state) {

        return null;
    }
}
