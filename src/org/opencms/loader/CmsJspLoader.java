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

package org.opencms.loader;

import com.google.common.base.Splitter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.flex.CmsFlexCache;
import org.opencms.flex.CmsFlexController;
import org.opencms.flex.CmsFlexRequest;
import org.opencms.flex.CmsFlexResponse;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.jsp.CmsJspTagEnableAde;
import org.opencms.jsp.jsonpart.CmsJsonPartFilter;
import org.opencms.jsp.util.CmsJspLinkMacroResolver;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsRegexSubstitution;
import org.opencms.workplace.CmsWorkplaceManager;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;

/**
 * The JSP loader which enables the execution of JSP in OpenCms.
 *
 * <p>Parameters supported by this loader:
 *
 * <dl>
 *   <dt>jsp.repository
 *   <dd>(Optional) This is the root directory in the "real" file system where generated JSPs are
 *       stored. The default is the web application path, e.g. in Tomcat if your web application is
 *       names "opencms" it would be <code>${TOMCAT_HOME}/webapps/opencms/</code>. The <code>
 *       jsp.folder</code> (see below) is added to this path. Usually the <code>jsp.repository
 *       </code> is not changed.
 *   <dt>jsp.folder
 *   <dd>(Optional) A path relative to the <code>jsp.repository</code> path where the JSPs generated
 *       by OpenCms are stored. The default is to store the generated JSP in <code>/WEB-INF/jsp/
 *       </code>. This works well in Tomcat 4, and the JSPs are not accessible directly from the
 *       outside this way, only through the OpenCms servlet. <i>Please note:</i> Some servlet
 *       environments (e.g. BEA Weblogic) do not permit JSPs to be stored under <code>/WEB-INF
 *       </code>. For environments like these, set the path to some place where JSPs can be
 *       accessed, e.g. <code>/jsp/</code> only.
 *   <dt>jsp.errorpage.committed
 *   <dd>(Optional) This parameter controls behavior of JSP error pages i.e. <code>
 *       &lt;% page errorPage="..." %&gt;</code>. If you find that these don't work in your servlet
 *       environment, you should try to change the value here. The default <code>true</code> has
 *       been tested with Tomcat 4.1 and 5.0. Older versions of Tomcat like 4.0 require a setting of
 *       <code>false</code>.
 * </dl>
 *
 * @since 6.0.0
 * @see I_CmsResourceLoader
 */
public class CmsJspLoader
    implements I_CmsResourceLoader, I_CmsFlexCacheEnabledLoader, I_CmsEventListener {

  /** Property value for "cache" that indicates that the FlexCache should be bypassed. */
  public static final String CACHE_PROPERTY_BYPASS = "bypass";

  /** Property value for "cache" that indicates that the output should be streamed. */
  public static final String CACHE_PROPERTY_STREAM = "stream";

  /** Default jsp folder constant. */
  public static final String DEFAULT_JSP_FOLDER = "/WEB-INF/jsp/";

  /** Special JSP directive tag start (<code>%&gt;</code>). */
  public static final String DIRECTIVE_END = "%>";

  /** Special JSP directive tag start (<code>&lt;%&#0040;</code>). */
  public static final String DIRECTIVE_START = "<%@";

  /** Extension for JSP managed by OpenCms (<code>.jsp</code>). */
  public static final String JSP_EXTENSION = ".jsp";

  /** Cache max age parameter name. */
  public static final String PARAM_CLIENT_CACHE_MAXAGE = "client.cache.maxage";

  /** Jsp cache size parameter name. */
  public static final String PARAM_JSP_CACHE_SIZE = "jsp.cache.size";

  /** Error page committed parameter name. */
  public static final String PARAM_JSP_ERRORPAGE_COMMITTED = "jsp.errorpage.committed";

  /** Jsp folder parameter name. */
  public static final String PARAM_JSP_FOLDER = "jsp.folder";

  /** Jsp repository parameter name. */
  public static final String PARAM_JSP_REPOSITORY = "jsp.repository";

  /** The id of this loader. */
  public static final int RESOURCE_LOADER_ID = 6;

  /** The log object for this class. */
  private static final Log LOG = CmsLog.getLog(CmsJspLoader.class);

  /** The maximum age for delivered contents in the clients cache. */
  private static long m_clientCacheMaxAge;

  /** Read write locks for jsp files. */
  private static Map<String, ReentrantReadWriteLock> m_fileLocks =
      CmsMemoryMonitor.createLRUCacheMap(10000);

  /** The directory to store the generated JSP pages in (absolute path). */
  private static @RUntainted String m_jspRepository;

  /** The directory to store the generated JSP pages in (relative path in web application). */
  private static @RUntainted String m_jspWebAppRepository;

  /** The CmsFlexCache used to store generated cache entries in. */
  private CmsFlexCache m_cache;

  /** The resource loader configuration. */
  private CmsParameterConfiguration m_configuration;

  /** Flag to indicate if error pages are marked as "committed". */
  private @RUntainted boolean m_errorPagesAreNotCommitted;

  /** The offline JSPs. */
  private Map<String, Boolean> m_offlineJsps;

  /** The online JSPs. */
  private Map<String, Boolean> m_onlineJsps;

  /** A map from taglib names to their URIs. */
  private Map<String, String> m_taglibs = new HashMap<String, String>();

  /**
   * Lock used to prevent JSP repository from being accessed while it is purged. The read lock is
   * needed for accessing the JSP repository, the write lock is needed for purging it.
   */
  private ReentrantReadWriteLock m_purgeLock = new ReentrantReadWriteLock(true);

  /**
   * The constructor of the class is empty, the initial instance will be created by the resource
   * manager upon startup of OpenCms.
   *
   * <p>
   *
   * @see org.opencms.loader.CmsResourceManager
   */
  public CmsJspLoader() {

    m_configuration = new CmsParameterConfiguration();
    OpenCms.addCmsEventListener(
        this,
        new int[] {EVENT_CLEAR_CACHES, EVENT_CLEAR_OFFLINE_CACHES, EVENT_CLEAR_ONLINE_CACHES});
    m_fileLocks = CmsMemoryMonitor.createLRUCacheMap(10000);
    initCaches(1000);
  }

  /**
   * @see
   *     org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String,
   *     java.lang.String)
   */
  public void addConfigurationParameter(String paramName, String paramValue) {

    m_configuration.add(paramName, paramValue);
    if (paramName.startsWith("taglib.")) {
      m_taglibs.put(paramName.replaceFirst("^taglib\\.", ""), paramValue.trim());
    }
  }

  /** @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent) */
  public void cmsEvent(CmsEvent event) {

    switch (event.getType()) {
      case EVENT_CLEAR_CACHES:
        m_offlineJsps.clear();
        m_onlineJsps.clear();
        return;
      case EVENT_CLEAR_OFFLINE_CACHES:
        m_offlineJsps.clear();
        return;
      case EVENT_CLEAR_ONLINE_CACHES:
        m_onlineJsps.clear();
        return;
      default:
        // do nothing
    }
  }

  /** Destroy this ResourceLoder, this is a NOOP so far. */
  public void destroy() {

    // NOOP
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#dump(org.opencms.file.CmsObject,
   *     org.opencms.file.CmsResource, java.lang.String, java.util.Locale,
   *     javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public byte[] dump(
      CmsObject cms,
      CmsResource file,
      String element,
      Locale locale,
      HttpServletRequest req,
      @RUntainted HttpServletResponse res)
      throws ServletException, IOException {

    // get the current Flex controller
    CmsFlexController controller = CmsFlexController.getController(req);
    CmsFlexController oldController = null;

    if (controller != null) {
      // for dumping we must create an new "top level" controller, save the old one to be restored
      // later
      oldController = controller;
    }

    byte[] result = null;
    try {
      // now create a new, temporary Flex controller
      controller = getController(cms, file, req, res, false, false);
      if (element != null) {
        // add the element parameter to the included request
        String[] value = new String[] {element};
        Map<String, String[]> parameters =
            Collections.singletonMap(I_CmsResourceLoader.PARAMETER_ELEMENT, value);
        controller.getCurrentRequest().addParameterMap(parameters);
      }
      Map<String, Object> attrs =
          controller.getCurrentRequest().addAttributeMap(CmsRequestUtil.getAtrributeMap(req));
      // dispatch to the JSP
      result = dispatchJsp(controller);

      // the standard context bean still references the nested request, we need to reset it to the
      // old request
      // (using the nested request is bad because it references the flex controller that is going to
      // be nulled out by removeController(), so operations
      // which use the flex controller might fail).

      CmsJspStandardContextBean standardContext =
          (CmsJspStandardContextBean) attrs.get(CmsJspStandardContextBean.ATTRIBUTE_NAME);
      if ((standardContext != null) && (req instanceof CmsFlexRequest)) {
        standardContext.updateRequestData((CmsFlexRequest) req);
      }
      // remove temporary controller
      CmsFlexController.removeController(req);
    } finally {
      if ((oldController != null) && (controller != null)) {
        // update "date last modified"
        oldController.updateDates(controller.getDateLastModified(), controller.getDateExpires());
        // reset saved controller
        CmsFlexController.setController(req, oldController);
      }
    }

    return result;
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#export(org.opencms.file.CmsObject,
   *     org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest,
   *     javax.servlet.http.HttpServletResponse)
   */
  public byte[] export(
      CmsObject cms, CmsResource resource, HttpServletRequest req, @RUntainted HttpServletResponse res)
      throws ServletException, IOException {

    // get the Flex controller
    CmsFlexController controller = getController(cms, resource, req, res, false, true);

    // dispatch to the JSP
    byte[] result = dispatchJsp(controller);

    // remove the controller from the request
    CmsFlexController.removeController(req);

    // return the contents
    return result;
  }

  /** @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration() */
  public CmsParameterConfiguration getConfiguration() {

    // return the configuration in an immutable form
    return m_configuration;
  }

  /**
   * Returns the absolute path in the "real" file system for the JSP repository toplevel directory.
   *
   * <p>
   *
   * @return The full path to the JSP repository
   */
  public String getJspRepository() {

    return m_jspRepository;
  }

  /** @see org.opencms.loader.I_CmsResourceLoader#getLoaderId() */
  public int getLoaderId() {

    return RESOURCE_LOADER_ID;
  }

  /**
   * Returns a set of root paths of files that are including the given resource using the
   * 'link.strong' macro.
   *
   * <p>
   *
   * @param cms the current cms context
   * @param resource the resource to check
   * @param referencingPaths the set of already referencing paths, also return parameter
   * @throws CmsException if something goes wrong
   */
  public void getReferencingStrongLinks(
      CmsObject cms, CmsResource resource, Set<String> referencingPaths) throws CmsException {

    CmsRelationFilter filter = CmsRelationFilter.SOURCES.filterType(CmsRelationType.JSP_STRONG);
    Iterator<CmsRelation> it = cms.getRelationsForResource(resource, filter).iterator();
    while (it.hasNext()) {
      CmsRelation relation = it.next();
      try {
        CmsResource source = relation.getSource(cms, CmsResourceFilter.DEFAULT);
        // check if file was already included
        if (referencingPaths.contains(source.getRootPath())) {
          // no need to include this file more than once
          continue;
        }
        referencingPaths.add(source.getRootPath());
        getReferencingStrongLinks(cms, source, referencingPaths);
      } catch (CmsException e) {
        if (LOG.isErrorEnabled()) {
          LOG.error(e.getLocalizedMessage(), e);
        }
      }
    }
  }

  /**
   * Return a String describing the ResourceLoader, which is (localized to the system default
   * locale) <code>"The OpenCms default resource loader for JSP"</code>.
   *
   * <p>
   *
   * @return a describing String for the ResourceLoader
   */
  public String getResourceLoaderInfo() {

    return Messages.get().getBundle().key(Messages.GUI_LOADER_JSP_DEFAULT_DESC_0);
  }

  /** @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration() */
  public void initConfiguration() {

    m_jspRepository = m_configuration.get(PARAM_JSP_REPOSITORY);
    if (m_jspRepository == null) {
      m_jspRepository = OpenCms.getSystemInfo().getWebApplicationRfsPath();
    }
    m_jspWebAppRepository = m_configuration.getString(PARAM_JSP_FOLDER, DEFAULT_JSP_FOLDER);
    if (!m_jspWebAppRepository.endsWith("/")) {
      m_jspWebAppRepository += "/";
    }
    m_jspRepository = CmsFileUtil.normalizePath(m_jspRepository + m_jspWebAppRepository);

    @RUntainted String maxAge = m_configuration.get(PARAM_CLIENT_CACHE_MAXAGE);
    if (maxAge == null) {
      m_clientCacheMaxAge = -1;
    } else {
      m_clientCacheMaxAge = Long.parseLong(maxAge);
    }

    // get the "error pages are committed or not" flag from the configuration
    m_errorPagesAreNotCommitted = m_configuration.getBoolean(PARAM_JSP_ERRORPAGE_COMMITTED, true);

    @RUntainted int cacheSize = m_configuration.getInteger(PARAM_JSP_CACHE_SIZE, -1);
    if (cacheSize > 0) {
      initCaches(cacheSize);
    }

    // output setup information
    if (CmsLog.INIT.isInfoEnabled()) {
      CmsLog.INIT.info(
          Messages.get().getBundle().key(Messages.INIT_JSP_REPOSITORY_ABS_PATH_1, m_jspRepository));
      CmsLog.INIT.info(
          Messages.get().getBundle().key(Messages.INIT_WEBAPP_PATH_1, m_jspWebAppRepository));
      CmsLog.INIT.info(
          Messages.get()
              .getBundle()
              .key(
                  Messages.INIT_JSP_REPOSITORY_ERR_PAGE_COMMOTED_1,
                  Boolean.valueOf(m_errorPagesAreNotCommitted)));
      if (m_clientCacheMaxAge > 0) {
        CmsLog.INIT.info(
            Messages.get().getBundle().key(Messages.INIT_CLIENT_CACHE_MAX_AGE_1, maxAge));
      }
      if (cacheSize > 0) {
        CmsLog.INIT.info(
            Messages.get()
                .getBundle()
                .key(Messages.INIT_JSP_CACHE_SIZE_1, String.valueOf(cacheSize)));
      }
      CmsLog.INIT.info(
          Messages.get()
              .getBundle()
              .key(Messages.INIT_LOADER_INITIALIZED_1, this.getClass().getName()));
    }
  }

  /** @see org.opencms.loader.I_CmsResourceLoader#isStaticExportEnabled() */
  public boolean isStaticExportEnabled() {

    return true;
  }

  /** @see org.opencms.loader.I_CmsResourceLoader#isStaticExportProcessable() */
  public boolean isStaticExportProcessable() {

    return true;
  }

  /** @see org.opencms.loader.I_CmsResourceLoader#isUsableForTemplates() */
  public boolean isUsableForTemplates() {

    return true;
  }

  /** @see org.opencms.loader.I_CmsResourceLoader#isUsingUriWhenLoadingTemplate() */
  public boolean isUsingUriWhenLoadingTemplate() {

    return false;
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#load(org.opencms.file.CmsObject,
   *     org.opencms.file.CmsResource, javax.servlet.http.HttpServletRequest,
   *     javax.servlet.http.HttpServletResponse)
   */
  public void load(CmsObject cms, CmsResource file, HttpServletRequest req, @RUntainted HttpServletResponse res)
      throws ServletException, IOException, CmsException {

    CmsRequestContext context = cms.getRequestContext();
    // If we load template jsp or template-element jsp (xml contents or xml pages) don't show source
    // (2nd test)
    if ((CmsHistoryResourceHandler.isHistoryRequest(req))
        && (context.getUri().equals(context.removeSiteRoot(file.getRootPath())))) {
      showSource(cms, file, req, res);
    } else {
      // load and process the JSP
      boolean streaming = false;
      boolean bypass = false;

      // read "cache" property for requested VFS resource to check for special "stream" and "bypass"
      // values
      String cacheProperty =
          cms.readPropertyObject(file, CmsPropertyDefinition.PROPERTY_CACHE, true).getValue();
      if (cacheProperty != null) {
        cacheProperty = cacheProperty.trim();
        if (CACHE_PROPERTY_STREAM.equals(cacheProperty)) {
          streaming = true;
        } else if (CACHE_PROPERTY_BYPASS.equals(cacheProperty)) {
          streaming = true;
          bypass = true;
        }
      }

      // For now, disable flex caching when the __json parameter is used
      if (CmsJsonPartFilter.isJsonRequest(req)) {
        streaming = true;
        bypass = true;
      }

      // get the Flex controller
      CmsFlexController controller = getController(cms, file, req, res, streaming, true);
      if (bypass || controller.isForwardMode()) {
        // initialize the standard contex bean to be available for all requests
        CmsJspStandardContextBean.getInstance(controller.getCurrentRequest());
        // once in forward mode, always in forward mode (for this request)
        controller.setForwardMode(true);
        // bypass Flex cache for this page, update the JSP first if necessary
        String target = updateJsp(file, controller, new HashSet<String>());
        // dispatch to external JSP
        req.getRequestDispatcher(target).forward(controller.getCurrentRequest(), res);
      } else {
        // Flex cache not bypassed, dispatch to internal JSP
        dispatchJsp(controller);
      }

      // remove the controller from the request if not forwarding
      if (!controller.isForwardMode()) {
        CmsFlexController.removeController(req);
      }
    }
  }

  /**
   * Replaces taglib attributes in page directives with taglib directives.
   *
   * <p>
   *
   * @param content the JSP source text
   * @return the transformed JSP text
   */
  @Deprecated
  public @RUntainted String processTaglibAttributes(@RUntainted String content) {

    // matches a whole page directive
    final Pattern directivePattern = Pattern.compile("(?sm)<%@\\s*page.*?%>");
    // matches a taglibs attribute and captures its values
    final Pattern taglibPattern = Pattern.compile("(?sm)taglibs\\s*=\\s*\"(.*?)\"");
    final Pattern commaPattern = Pattern.compile("(?sm)\\s*,\\s*");
    final Set<String> taglibs = new LinkedHashSet<String>();
    // we insert the marker after the first page directive
    final String marker = ":::TAGLIBS:::";
    I_CmsRegexSubstitution directiveSub =
        new I_CmsRegexSubstitution() {

          private boolean m_first = true;

          public String substituteMatch(String string, Matcher matcher) {

            @RUntainted String match = string.substring(matcher.start(), matcher.end());
            I_CmsRegexSubstitution taglibSub =
                new I_CmsRegexSubstitution() {

                  public String substituteMatch(String string1, Matcher matcher1) {

                    // values of the taglibs attribute
                    String match1 = string1.substring(matcher1.start(1), matcher1.end(1));
                    for (String taglibKey : Splitter.on(commaPattern).split(match1)) {
                      taglibs.add(taglibKey);
                    }
                    return "";
                  }
                };
            String result = CmsStringUtil.substitute(taglibPattern, match, taglibSub);
            if (m_first) {
              result += marker;
              m_first = false;
            }
            return result;
          }
        };
    @RUntainted String substituted = CmsStringUtil.substitute(directivePattern, content, directiveSub);
    // insert taglib inclusion
    substituted = substituted.replaceAll(marker, generateTaglibInclusions(taglibs));
    // remove empty page directives
    substituted = substituted.replaceAll("(?sm)<%@\\s*page\\s*%>", "");
    return substituted;
  }

  /**
   * Removes the given resources from the cache.
   *
   * <p>
   *
   * @param rootPaths the set of root paths to remove
   * @param online if online or offline
   */
  public void removeFromCache(Set<String> rootPaths, boolean online) {

    Map<String, Boolean> cache;
    if (online) {
      cache = m_onlineJsps;
    } else {
      cache = m_offlineJsps;
    }
    Iterator<String> itRemove = rootPaths.iterator();
    while (itRemove.hasNext()) {
      String rootPath = itRemove.next();
      cache.remove(rootPath);
    }
  }

  /**
   * Removes a JSP from an offline project from the RFS.
   *
   * <p>
   *
   * @param resource the offline JSP resource to remove from the RFS
   * @throws CmsLoaderException if accessing the loader fails
   */
  public void removeOfflineJspFromRepository(CmsResource resource) throws CmsLoaderException {

    String jspName = getJspRfsPath(resource, false);
    Set<String> pathSet = new HashSet<String>();
    pathSet.add(resource.getRootPath());
    ReentrantReadWriteLock lock = getFileLock(jspName);
    lock.writeLock().lock();
    try {
      removeFromCache(pathSet, false);
      File jspFile = new File(jspName);
      jspFile.delete();
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * @see org.opencms.loader.I_CmsResourceLoader#service(org.opencms.file.CmsObject,
   *     org.opencms.file.CmsResource, javax.servlet.ServletRequest, javax.servlet.ServletResponse)
   */
  public void service(CmsObject cms, CmsResource resource, ServletRequest req, ServletResponse res)
      throws ServletException, IOException, CmsLoaderException {

    CmsFlexController controller = CmsFlexController.getController(req);
    // get JSP target name on "real" file system
    String target = updateJsp(resource, controller, new HashSet<String>(8));
    // important: Indicate that all output must be buffered
    controller.getCurrentResponse().setOnlyBuffering(true);
    // initialize the standard contex bean to be available for all requests
    CmsJspStandardContextBean.getInstance(controller.getCurrentRequest());
    // dispatch to external file
    controller
        .getCurrentRequest()
        .getRequestDispatcherToExternal(cms.getSitePath(resource), target)
        .include(req, res);
  }

  /**
   * @see org.opencms.loader.I_CmsFlexCacheEnabledLoader#setFlexCache(org.opencms.flex.CmsFlexCache)
   */
  public void setFlexCache(CmsFlexCache cache) {

    m_cache = cache;
    // output setup information
    if (CmsLog.INIT.isInfoEnabled()) {
      CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ADD_FLEX_CACHE_0));
    }
  }

  /**
   * Triggers an asynchronous purge of the JSP repository.
   *
   * <p>
   *
   * @param afterPurgeAction the action to execute after purging
   */
  public void triggerPurge(final Runnable afterPurgeAction) {

    OpenCms.getExecutor()
        .execute(
            new Runnable() {

              @SuppressWarnings("synthetic-access")
              public void run() {

                try {
                  m_purgeLock.writeLock().lock();
                  for (ReentrantReadWriteLock lock : m_fileLocks.values()) {
                    lock.writeLock().lock();
                  }
                  doPurge(afterPurgeAction);
                } catch (Exception e) {
                  LOG.error("Error while purging jsp repository: " + e.getLocalizedMessage(), e);
                } finally {
                  for (ReentrantReadWriteLock lock : m_fileLocks.values()) {
                    try {
                      lock.writeLock().unlock();
                    } catch (Exception e) {
                      LOG.warn(e.getLocalizedMessage(), e);
                    }
                  }
                  m_purgeLock.writeLock().unlock();
                }
              }
            });
  }

  /**
   * Updates a JSP page in the "real" file system in case the VFS resource has changed.
   *
   * <p>Also processes the <code>&lt;%@ cms %&gt;</code> tags before the JSP is written to the real
   * FS. Also recursively updates all files that are referenced by a <code>&lt;%@ cms %&gt;</code>
   * tag on this page to make sure the file actually exists in the real FS. All <code>
   * &lt;%@ include %&gt;</code> tags are parsed and the name in the tag is translated from the
   * OpenCms VFS path to the path in the real FS. The same is done for filenames in <code>
   * &lt;%@ page errorPage=... %&gt;</code> tags.
   *
   * <p>
   *
   * @param resource the requested JSP file resource in the VFS
   * @param controller the controller for the JSP integration
   * @param updatedFiles a Set containing all JSP pages that have been already updated
   * @return the file name of the updated JSP in the "real" FS
   * @throws ServletException might be thrown in the process of including the JSP
   * @throws IOException might be thrown in the process of including the JSP
   * @throws CmsLoaderException if the resource type can not be read
   */
  public @RUntainted String updateJsp(
      CmsResource resource, CmsFlexController controller, Set<String> updatedFiles)
      throws IOException, ServletException, CmsLoaderException {

    String jspVfsName = resource.getRootPath();
    String extension;
    boolean isHardInclude;
    int loaderId = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getLoaderId();
    if ((loaderId == CmsJspLoader.RESOURCE_LOADER_ID) && (!jspVfsName.endsWith(JSP_EXTENSION))) {
      // this is a true JSP resource that does not end with ".jsp"
      extension = JSP_EXTENSION;
      isHardInclude = false;
    } else {
      // not a JSP resource or already ends with ".jsp"
      extension = "";
      // if this is a JSP we don't treat it as hard include
      isHardInclude = (loaderId != CmsJspLoader.RESOURCE_LOADER_ID);
    }

    @RUntainted String jspTargetName =
        CmsFileUtil.getRepositoryName(
            m_jspWebAppRepository,
            jspVfsName + extension,
            controller.getCurrentRequest().isOnline());

    // check if page was already updated
    if (updatedFiles.contains(jspTargetName)) {
      // no need to write the already included file to the real FS more then once
      return jspTargetName;
    }

    @RUntainted String jspPath =
        CmsFileUtil.getRepositoryName(
            m_jspRepository, jspVfsName + extension, controller.getCurrentRequest().isOnline());

    File d = new File(jspPath).getParentFile();
    if ((d == null) || (d.exists() && !(d.isDirectory() && d.canRead()))) {
      CmsMessageContainer message = Messages.get().container(Messages.LOG_ACCESS_DENIED_1, jspPath);
      LOG.error(message.key());
      // can not continue
      throw new ServletException(message.key());
    }

    if (!d.exists()) {
      // create directory structure
      d.mkdirs();
    }
    ReentrantReadWriteLock readWriteLock = getFileLock(jspVfsName);
    try {
      // get a read lock for this jsp
      readWriteLock.readLock().lock();
      @RUntainted File jspFile = new File(jspPath);
      // check if the JSP must be updated
      boolean mustUpdate = false;
      long jspModificationDate = 0;
      if (!jspFile.exists()) {
        // file does not exist in real FS
        mustUpdate = true;
        // make sure the parent folder exists
        File folder = jspFile.getParentFile();
        if (!folder.exists()) {
          boolean success = folder.mkdirs();
          if (!success) {
            LOG.error(
                org.opencms.db.Messages.get()
                    .getBundle()
                    .key(
                        org.opencms.db.Messages.LOG_CREATE_FOLDER_FAILED_1,
                        folder.getAbsolutePath()));
          }
        }
      } else {
        jspModificationDate = jspFile.lastModified();
        if (jspModificationDate < resource.getDateLastModified()) {
          // file in real FS is older then file in VFS
          mustUpdate = true;
        } else if (controller.getCurrentRequest().isDoRecompile()) {
          // recompile is forced with parameter
          mustUpdate = true;
        } else {
          // check if update is needed
          if (controller.getCurrentRequest().isOnline()) {
            mustUpdate = !m_onlineJsps.containsKey(jspVfsName);
          } else {
            mustUpdate = !m_offlineJsps.containsKey(jspVfsName);
          }
          // check strong links only if update is needed
          if (mustUpdate) {
            // update strong link dependencies
            mustUpdate = updateStrongLinks(resource, controller, updatedFiles);
          }
        }
      }
      if (mustUpdate) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(Messages.get().getBundle().key(Messages.LOG_WRITING_JSP_1, jspTargetName));
        }
        // jsp needs updating, acquire a write lock
        readWriteLock.readLock().unlock();
        readWriteLock.writeLock().lock();
        try {
          // check again if updating is still necessary as this might have happened while waiting
          // for the write lock
          if (!jspFile.exists() || (jspModificationDate == jspFile.lastModified())) {
            updatedFiles.add(jspTargetName);
            @RUntainted byte[] contents;
            String encoding;
            try {
              CmsObject cms = controller.getCmsObject();
              contents = cms.readFile(resource).getContents();
              // check the "content-encoding" property for the JSP, use system default if not found
              // on path
              encoding =
                  cms.readPropertyObject(
                          resource, CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING, true)
                      .getValue();
              if (encoding == null) {
                encoding = OpenCms.getSystemInfo().getDefaultEncoding();
              } else {
                encoding = CmsEncoder.lookupEncoding(encoding.trim(), encoding);
              }
            } catch (CmsException e) {
              controller.setThrowable(e, jspVfsName);
              throw new ServletException(
                  Messages.get().getBundle().key(Messages.ERR_LOADER_JSP_ACCESS_1, jspVfsName), e);
            }

            try {
              // parse the JSP and modify OpenCms critical directives
              contents = parseJsp(contents, encoding, controller, updatedFiles, isHardInclude);
              if (LOG.isInfoEnabled()) {
                // check for existing file and display some debug info
                LOG.info(
                    Messages.get()
                        .getBundle()
                        .key(
                            Messages.LOG_JSP_PERMCHECK_4,
                            new Object[] {
                              jspFile.getAbsolutePath(),
                              Boolean.valueOf(jspFile.exists()),
                              Boolean.valueOf(jspFile.isFile()),
                              Boolean.valueOf(jspFile.canWrite())
                            }));
              }
              // write the parsed JSP content to the real FS
              synchronized (CmsJspLoader.class) {
                // this must be done only one file at a time
                FileOutputStream fs = new FileOutputStream(jspFile);
                fs.write(contents);
                fs.close();

                // we set the modification date to (approximately) that of the VFS resource. This is
                // needed because in the Online project, the old version of a JSP
                // may be generated in the RFS JSP repository *after* the JSP has been changed, but
                // *before* it has been published, which would lead
                // to it not being updated after the changed JSP is published.

                // Note: the RFS may only support second precision for the last modification date
                jspFile.setLastModified((1 + (resource.getDateLastModified() / 1000)) * 1000);
              }
              if (controller.getCurrentRequest().isOnline()) {
                m_onlineJsps.put(jspVfsName, Boolean.TRUE);
              } else {
                m_offlineJsps.put(jspVfsName, Boolean.TRUE);
              }
              if (LOG.isInfoEnabled()) {
                LOG.info(
                    Messages.get()
                        .getBundle()
                        .key(Messages.LOG_UPDATED_JSP_2, jspTargetName, jspVfsName));
              }
            } catch (FileNotFoundException e) {
              throw new ServletException(
                  Messages.get()
                      .getBundle()
                      .key(Messages.ERR_LOADER_JSP_WRITE_1, jspFile.getName()),
                  e);
            }
          }
        } finally {
          readWriteLock.readLock().lock();
          readWriteLock.writeLock().unlock();
        }
      }

      // update "last modified" and "expires" date on controller
      controller.updateDates(jspFile.lastModified(), CmsResource.DATE_EXPIRED_DEFAULT);
    } finally {
      // m_processingFiles.remove(jspVfsName);
      readWriteLock.readLock().unlock();
    }

    return jspTargetName;
  }

  /**
   * Updates the internal jsp repository when the servlet container tries to compile a jsp file that
   * may not exist.
   *
   * <p>
   *
   * @param servletPath the servlet path, just to avoid unneeded recursive calls
   * @param request the current request
   */
  public void updateJspFromRequest(String servletPath, CmsFlexRequest request) {

    // assemble the RFS name of the requested jsp
    String jspUri = servletPath;
    String pathInfo = request.getPathInfo();
    if (pathInfo != null) {
      jspUri += pathInfo;
    }

    // check the file name
    if ((jspUri == null) || !jspUri.startsWith(m_jspWebAppRepository)) {
      // nothing to do, this kind of request are handled by the CmsJspLoader#service method
      return;
    }

    // remove prefixes
    jspUri = jspUri.substring(m_jspWebAppRepository.length());
    if (jspUri.startsWith(CmsFlexCache.REPOSITORY_ONLINE)) {
      jspUri = jspUri.substring(CmsFlexCache.REPOSITORY_ONLINE.length());
    } else if (jspUri.startsWith(CmsFlexCache.REPOSITORY_OFFLINE)) {
      jspUri = jspUri.substring(CmsFlexCache.REPOSITORY_OFFLINE.length());
    } else {
      // this is not an OpenCms jsp file
      return;
    }

    // read the resource from OpenCms
    CmsFlexController controller = CmsFlexController.getController(request);
    try {
      CmsResource includeResource;
      try {
        // first try to read the resource assuming no additional jsp extension was needed
        includeResource = readJspResource(controller, jspUri);
      } catch (CmsVfsResourceNotFoundException e) {
        // try removing the additional jsp extension
        if (jspUri.endsWith(JSP_EXTENSION)) {
          jspUri = jspUri.substring(0, jspUri.length() - JSP_EXTENSION.length());
        }
        includeResource = readJspResource(controller, jspUri);
      }
      // make sure the jsp referenced file is generated
      updateJsp(includeResource, controller, new HashSet<String>(8));
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(e.getLocalizedMessage(), e);
      }
    }
  }

  /**
   * Dispatches the current request to the OpenCms internal JSP.
   *
   * <p>
   *
   * @param controller the current controller
   * @return the content of the processed JSP
   * @throws ServletException if inclusion does not work
   * @throws IOException if inclusion does not work
   */
  protected byte[] dispatchJsp(CmsFlexController controller) throws ServletException, IOException {

    // get request / response wrappers
    CmsFlexRequest f_req = controller.getCurrentRequest();
    CmsFlexResponse f_res = controller.getCurrentResponse();
    try {
      f_req
          .getRequestDispatcher(controller.getCmsObject().getSitePath(controller.getCmsResource()))
          .include(f_req, f_res);
    } catch (SocketException e) {
      // uncritical, might happen if client (browser) does not wait until end of page delivery
      LOG.debug(
          Messages.get().getBundle().key(Messages.LOG_IGNORING_EXC_1, e.getClass().getName()), e);
    }

    byte[] result = null;
    HttpServletResponse res = controller.getTopResponse();

    if (!controller.isStreaming() && !f_res.isSuspended()) {
      try {
        // if a JSP error page was triggered the response will be already committed here
        if (!res.isCommitted() || m_errorPagesAreNotCommitted) {

          // check if the current request was done by a workplace user
          boolean isWorkplaceUser = CmsWorkplaceManager.isWorkplaceUser(f_req);

          // check if the content was modified since the last request
          if (controller.isTop()
              && !isWorkplaceUser
              && CmsFlexController.isNotModifiedSince(f_req, controller.getDateLastModified())) {
            if (f_req.getParameterMap().size() == 0) {
              // only use "expires" header on pages that have no parameters,
              // otherwise some browsers (e.g. IE 6) will not even try to request
              // updated versions of the page
              CmsFlexController.setDateExpiresHeader(
                  res, controller.getDateExpires(), m_clientCacheMaxAge);
            }
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return null;
          }

          // get the result byte array
          result = f_res.getWriterBytes();
          HttpServletRequest req = controller.getTopRequest();
          if (req.getHeader(CmsRequestUtil.HEADER_OPENCMS_EXPORT) != null) {
            // this is a non "on-demand" static export request, don't write to the response stream
            req.setAttribute(
                CmsRequestUtil.HEADER_OPENCMS_EXPORT, new Long(controller.getDateLastModified()));
          } else if (controller.isTop()) {
            // process headers and write output if this is the "top" request/response
            res.setContentLength(result.length);
            // check for preset error code
            Integer errorCode = (Integer) req.getAttribute(CmsRequestUtil.ATTRIBUTE_ERRORCODE);
            if (errorCode == null) {
              // set last modified / no cache headers only if this is not an error page
              if (isWorkplaceUser) {
                res.setDateHeader(CmsRequestUtil.HEADER_LAST_MODIFIED, System.currentTimeMillis());
                CmsRequestUtil.setNoCacheHeaders(res);
              } else {
                // set date last modified header
                CmsFlexController.setDateLastModifiedHeader(res, controller.getDateLastModified());
                if ((f_req.getParameterMap().size() == 0)
                    && (controller.getDateLastModified() > -1)) {
                  // only use "expires" header on pages that have no parameters
                  // and that are cachable (i.e. 'date last modified' is set)
                  // otherwise some browsers (e.g. IE 6) will not even try to request
                  // updated versions of the page
                  CmsFlexController.setDateExpiresHeader(
                      res, controller.getDateExpires(), m_clientCacheMaxAge);
                }
              }
              // set response status to "200 - OK" (required for static export "on-demand")
              res.setStatus(HttpServletResponse.SC_OK);
            } else {
              // set previously saved error code
              res.setStatus(errorCode.intValue());
            }
            // process the headers
            CmsFlexResponse.processHeaders(f_res.getHeaders(), res);
            res.getOutputStream().write(result);
            res.getOutputStream().flush();
          }
        }
      } catch (IllegalStateException e) {
        // uncritical, might happen if JSP error page was used
        LOG.debug(
            Messages.get().getBundle().key(Messages.LOG_IGNORING_EXC_1, e.getClass().getName()), e);
      } catch (SocketException e) {
        // uncritical, might happen if client (browser) does not wait until end of page delivery
        LOG.debug(
            Messages.get().getBundle().key(Messages.LOG_IGNORING_EXC_1, e.getClass().getName()), e);
      }
    }

    return result;
  }

  /**
   * Purges the JSP repository.<p<
   *
   * @param afterPurgeAction the action to execute after purging
   */
  protected void doPurge(Runnable afterPurgeAction) {

    if (LOG.isInfoEnabled()) {
      LOG.info(
          org.opencms.flex.Messages.get()
              .getBundle()
              .key(org.opencms.flex.Messages.LOG_FLEXCACHE_WILL_PURGE_JSP_REPOSITORY_0));
    }

    File d;
    d = new File(getJspRepository() + CmsFlexCache.REPOSITORY_ONLINE + File.separator);
    CmsFileUtil.purgeDirectory(d);

    d = new File(getJspRepository() + CmsFlexCache.REPOSITORY_OFFLINE + File.separator);
    CmsFileUtil.purgeDirectory(d);
    if (afterPurgeAction != null) {
      afterPurgeAction.run();
    }

    if (LOG.isInfoEnabled()) {
      LOG.info(
          org.opencms.flex.Messages.get()
              .getBundle()
              .key(org.opencms.flex.Messages.LOG_FLEXCACHE_PURGED_JSP_REPOSITORY_0));
    }
  }

  /**
   * Generates the taglib directives for a collection of taglib identifiers.
   *
   * <p>
   *
   * @param taglibs the taglib identifiers
   * @return a string containing taglib directives
   */
  protected @RUntainted String generateTaglibInclusions(Collection<String> taglibs) {

    StringBuffer buffer = new StringBuffer();
    for (String taglib : taglibs) {
      String uri = m_taglibs.get(taglib);
      if (uri != null) {
        buffer.append("<%@ taglib prefix=\"" + taglib + "\" uri=\"" + uri + "\" %>");
      }
    }
    return buffer.toString();
  }

  /**
   * Delivers a Flex controller, either by creating a new one, or by re-using an existing one.
   *
   * <p>
   *
   * @param cms the initial CmsObject to wrap in the controller
   * @param resource the resource requested
   * @param req the current request
   * @param res the current response
   * @param streaming indicates if the response is streaming
   * @param top indicates if the response is the top response
   * @return a Flex controller
   */
  protected CmsFlexController getController(
      CmsObject cms,
      CmsResource resource,
      HttpServletRequest req,
      @RUntainted HttpServletResponse res,
      boolean streaming,
      boolean top) {

    CmsFlexController controller = null;
    if (top) {
      // only check for existing controller if this is the "top" request/response
      controller = CmsFlexController.getController(req);
    }
    if (controller == null) {
      // create new request / response wrappers
      if (!cms.getRequestContext().getCurrentProject().isOnlineProject()
          && (CmsHistoryResourceHandler.isHistoryRequest(req)
              || CmsJspTagEnableAde.isDirectEditDisabled(req))) {
        cms.getRequestContext()
            .setAttribute(CmsGwtConstants.PARAM_DISABLE_DIRECT_EDIT, Boolean.TRUE);
      }
      controller = new CmsFlexController(cms, resource, m_cache, req, res, streaming, top);
      CmsFlexController.setController(req, controller);
      CmsFlexRequest f_req = new CmsFlexRequest(req, controller);
      CmsFlexResponse f_res = new CmsFlexResponse(res, controller, streaming, true);
      controller.push(f_req, f_res);
    } else if (controller.isForwardMode()) {
      // reset CmsObject (because of URI) if in forward mode
      controller = new CmsFlexController(cms, controller);
      CmsFlexController.setController(req, controller);
    }
    return controller;
  }

  /**
   * Initializes the caches.
   *
   * <p>
   *
   * @param cacheSize the cache size
   */
  protected void initCaches(int cacheSize) {

    m_offlineJsps = CmsMemoryMonitor.createLRUCacheMap(cacheSize);
    m_onlineJsps = CmsMemoryMonitor.createLRUCacheMap(cacheSize);
  }

  /**
   * Parses the JSP and modifies OpenCms critical directive information.
   *
   * <p>
   *
   * @param byteContent the original JSP content
   * @param encoding the encoding to use for the JSP
   * @param controller the controller for the JSP integration
   * @param updatedFiles a Set containing all JSP pages that have been already updated
   * @param isHardInclude indicated if this page is actually a "hard" include with <code>
   *     &lt;%@ include file="..." &gt;</code>
   * @return the modified JSP content
   */
  protected @RUntainted byte[] parseJsp(
      @RUntainted byte[] byteContent,
      @RUntainted String encoding,
      CmsFlexController controller,
      Set<String> updatedFiles,
      boolean isHardInclude) {

    @RUntainted String content;
    // make sure encoding is set correctly
    try {
      content = new String(byteContent, encoding);
    } catch (UnsupportedEncodingException e) {
      // encoding property is not set correctly
      LOG.error(
          Messages.get()
              .getBundle()
              .key(Messages.LOG_UNSUPPORTED_ENC_1, controller.getCurrentRequest().getElementUri()),
          e);
      try {
        encoding = OpenCms.getSystemInfo().getDefaultEncoding();
        content = new String(byteContent, encoding);
      } catch (UnsupportedEncodingException e2) {
        // should not happen since default encoding is always a valid encoding (checked during
        // system startup)
        content = new String(byteContent);
      }
    }

    // parse for special %(link:...) macros
    content = parseJspLinkMacros(content, controller);
    // parse for special <%@cms file="..." %> tag
    content = parseJspCmsTag(content, controller, updatedFiles);
    // parse for included files in tags
    content = parseJspIncludes(content, controller, updatedFiles);
    // parse for <%@page pageEncoding="..." %> tag
    content = parseJspEncoding(content, encoding, isHardInclude);
    // Processes magic taglib attributes in page directives
    content = processTaglibAttributes(content);
    // convert the result to bytes and return it
    try {
      return content.getBytes(encoding);
    } catch (UnsupportedEncodingException e) {
      // should not happen since encoding was already checked
      return content.getBytes();
    }
  }

  /**
   * Parses the JSP content for the special <code>&lt;%cms file="..." %&gt;</code> tag.
   *
   * <p>
   *
   * @param content the JSP content to parse
   * @param controller the current JSP controller
   * @param updatedFiles a set of already updated jsp files
   * @return the parsed JSP content
   */
  protected @RUntainted String parseJspCmsTag(
      @RUntainted String content, CmsFlexController controller, Set<String> updatedFiles) {

    // check if a JSP directive occurs in the file
    @RUntainted int i1 = content.indexOf(DIRECTIVE_START);
    if (i1 < 0) {
      // no directive occurs
      return content;
    }

    StringBuffer buf = new StringBuffer(content.length());
    @RUntainted int p0 = 0, i2 = 0, slen = DIRECTIVE_START.length(), elen = DIRECTIVE_END.length();

    while (i1 >= 0) {
      // parse the file and replace JSP filename references
      i2 = content.indexOf(DIRECTIVE_END, i1 + slen);
      if (i2 < 0) {
        // wrong syntax (missing end directive) - let the JSP compiler produce the error message
        return content;
      } else if (i2 > i1) {
        @RUntainted String directive = content.substring(i1 + slen, i2);
        if (LOG.isDebugEnabled()) {
          LOG.debug(
              Messages.get()
                  .getBundle()
                  .key(
                      Messages.LOG_DIRECTIVE_DETECTED_3,
                      DIRECTIVE_START,
                      directive,
                      DIRECTIVE_END));
        }

        int t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0;
        while (directive.charAt(t1) == ' ') {
          t1++;
        }
        @RUntainted String argument = null;
        if (directive.startsWith("cms", t1)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_X_DIRECTIVE_DETECTED_1, "cms"));
          }
          t2 = directive.indexOf("file", t1 + 3);
          t5 = 4;
        }

        if (t2 > 0) {
          String sub = directive.substring(t2 + t5);
          char c1 = sub.charAt(t3);
          while ((c1 == ' ') || (c1 == '=') || (c1 == '"')) {
            c1 = sub.charAt(++t3);
          }
          t4 = t3;
          while (c1 != '"') {
            c1 = sub.charAt(++t4);
          }
          if (t4 > t3) {
            argument = sub.substring(t3, t4);
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DIRECTIVE_ARG_1, argument));
          }
        }

        if (argument != null) {
          //  try to update the referenced file
          @RUntainted String jspname = updateJsp(argument, controller, updatedFiles);
          if (jspname != null) {
            directive = jspname;
            if (LOG.isDebugEnabled()) {
              LOG.debug(
                  Messages.get()
                      .getBundle()
                      .key(
                          Messages.LOG_DIRECTIVE_CHANGED_3,
                          DIRECTIVE_START,
                          directive,
                          DIRECTIVE_END));
            }
          }
          // cms directive was found
          buf.append(content.substring(p0, i1));
          buf.append(directive);
          p0 = i2 + elen;
          i1 = content.indexOf(DIRECTIVE_START, p0);
        } else {
          // cms directive was not found
          buf.append(content.substring(p0, i1 + slen));
          buf.append(directive);
          p0 = i2;
          i1 = content.indexOf(DIRECTIVE_START, p0);
        }
      }
    }
    if (i2 > 0) {
      // the content of the JSP was changed
      buf.append(content.substring(p0, content.length()));
      content = buf.toString();
    }
    return content;
  }

  /**
   * Parses the JSP content for the <code>&lt;%page pageEncoding="..." %&gt;</code> tag and ensures
   * that the JSP page encoding is set according to the OpenCms "content-encoding" property value of
   * the JSP.
   *
   * <p>
   *
   * @param content the JSP content to parse
   * @param encoding the encoding to use for the JSP
   * @param isHardInclude indicated if this page is actually a "hard" include with <code>
   *     &lt;%@ include file="..." &gt;</code>
   * @return the parsed JSP content
   */
  protected @RUntainted String parseJspEncoding(@RUntainted String content, @RUntainted String encoding, boolean isHardInclude) {

    // check if a JSP directive occurs in the file
    @RUntainted int i1 = content.indexOf(DIRECTIVE_START);
    if (i1 < 0) {
      // no directive occurs
      if (isHardInclude) {
        return content;
      }
    }

    StringBuffer buf = new StringBuffer(content.length() + 64);
    @RUntainted int p0 = 0, i2 = 0, slen = DIRECTIVE_START.length();
    boolean found = false;

    if (i1 < 0) {
      // no directive found at all, append content to buffer
      buf.append(content);
    }

    while (i1 >= 0) {
      // parse the file and set/replace page encoding
      i2 = content.indexOf(DIRECTIVE_END, i1 + slen);
      if (i2 < 0) {
        // wrong syntax (missing end directive) - let the JSP compiler produce the error message
        return content;
      } else if (i2 > i1) {
        @RUntainted String directive = content.substring(i1 + slen, i2);
        if (LOG.isDebugEnabled()) {
          LOG.debug(
              Messages.get()
                  .getBundle()
                  .key(
                      Messages.LOG_DIRECTIVE_DETECTED_3,
                      DIRECTIVE_START,
                      directive,
                      DIRECTIVE_END));
        }

        int t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0;
        while (directive.charAt(t1) == ' ') {
          t1++;
        }
        @RUntainted String argument = null;
        if (directive.startsWith("page", t1)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_X_DIRECTIVE_DETECTED_1, "page"));
          }
          t2 = directive.indexOf("pageEncoding", t1 + 4);
          t5 = 12;
          if (t2 > 0) {
            found = true;
          }
        }

        if (t2 > 0) {
          String sub = directive.substring(t2 + t5);
          char c1 = sub.charAt(t3);
          while ((c1 == ' ') || (c1 == '=') || (c1 == '"')) {
            c1 = sub.charAt(++t3);
          }
          t4 = t3;
          while (c1 != '"') {
            c1 = sub.charAt(++t4);
          }
          if (t4 > t3) {
            argument = sub.substring(t3, t4);
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DIRECTIVE_ARG_1, argument));
          }
        }

        if (argument != null) {
          // a pageEncoding setting was found, changes have to be made
          String pre = directive.substring(0, t2 + t3 + t5);
          String suf = directive.substring(t2 + t3 + t5 + argument.length());
          // change the encoding
          directive = pre + encoding + suf;
          if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get()
                    .getBundle()
                    .key(
                        Messages.LOG_DIRECTIVE_CHANGED_3,
                        DIRECTIVE_START,
                        directive,
                        DIRECTIVE_END));
          }
        }

        buf.append(content.substring(p0, i1 + slen));
        buf.append(directive);
        p0 = i2;
        i1 = content.indexOf(DIRECTIVE_START, p0);
      }
    }
    if (i2 > 0) {
      // the content of the JSP was changed
      buf.append(content.substring(p0, content.length()));
    }
    if (found) {
      content = buf.toString();
    } else if (!isHardInclude) {
      // encoding setting was not found
      // if this is not a "hard" include then add the encoding to the top of the page
      // checking for the hard include is important to prevent errors with
      // multiple page encoding settings if a template is composed from several hard included
      // elements
      // this is an issue in Tomcat 4.x but not 5.x
      StringBuffer buf2 = new StringBuffer(buf.length() + 32);
      buf2.append("<%@ page pageEncoding=\"");
      buf2.append(encoding);
      buf2.append("\" %>");
      buf2.append(buf);
      content = buf2.toString();
    }
    return content;
  }

  /**
   * Parses the JSP content for includes and replaces all OpenCms VFS path information with
   * information for the real FS.
   *
   * <p>
   *
   * @param content the JSP content to parse
   * @param controller the current JSP controller
   * @param updatedFiles a set of already updated files
   * @return the parsed JSP content
   */
  protected @RUntainted String parseJspIncludes(
      @RUntainted String content, CmsFlexController controller, Set<String> updatedFiles) {

    // check if a JSP directive occurs in the file
    @RUntainted int i1 = content.indexOf(DIRECTIVE_START);
    if (i1 < 0) {
      // no directive occurs
      return content;
    }

    StringBuffer buf = new StringBuffer(content.length());
    @RUntainted int p0 = 0, i2 = 0, slen = DIRECTIVE_START.length();

    while (i1 >= 0) {
      // parse the file and replace JSP filename references
      i2 = content.indexOf(DIRECTIVE_END, i1 + slen);
      if (i2 < 0) {
        // wrong syntax (missing end directive) - let the JSP compiler produce the error message
        return content;
      } else if (i2 > i1) {
        @RUntainted String directive = content.substring(i1 + slen, i2);
        if (LOG.isDebugEnabled()) {
          LOG.debug(
              Messages.get()
                  .getBundle()
                  .key(
                      Messages.LOG_DIRECTIVE_DETECTED_3,
                      DIRECTIVE_START,
                      directive,
                      DIRECTIVE_END));
        }

        int t1 = 0, t2 = 0, t3 = 0, t4 = 0, t5 = 0;
        while (directive.charAt(t1) == ' ') {
          t1++;
        }
        @RUntainted String argument = null;
        if (directive.startsWith("include", t1)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(
                Messages.get().getBundle().key(Messages.LOG_X_DIRECTIVE_DETECTED_1, "include"));
          }
          t2 = directive.indexOf("file", t1 + 7);
          t5 = 6;
        } else if (directive.startsWith("page", t1)) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_X_DIRECTIVE_DETECTED_1, "page"));
          }
          t2 = directive.indexOf("errorPage", t1 + 4);
          t5 = 11;
        }

        if (t2 > 0) {
          String sub = directive.substring(t2 + t5);
          char c1 = sub.charAt(t3);
          while ((c1 == ' ') || (c1 == '=') || (c1 == '"')) {
            c1 = sub.charAt(++t3);
          }
          t4 = t3;
          while (c1 != '"') {
            c1 = sub.charAt(++t4);
          }
          if (t4 > t3) {
            argument = sub.substring(t3, t4);
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DIRECTIVE_ARG_1, argument));
          }
        }

        if (argument != null) {
          // a file was found, changes have to be made
          String pre = directive.substring(0, t2 + t3 + t5);
          String suf = directive.substring(t2 + t3 + t5 + argument.length());
          // now try to update the referenced file
          @RUntainted String jspname = updateJsp(argument, controller, updatedFiles);
          if (jspname != null) {
            // only change something in case no error had occurred
            directive = pre + jspname + suf;
            if (LOG.isDebugEnabled()) {
              LOG.debug(
                  Messages.get()
                      .getBundle()
                      .key(
                          Messages.LOG_DIRECTIVE_CHANGED_3,
                          DIRECTIVE_START,
                          directive,
                          DIRECTIVE_END));
            }
          }
        }

        buf.append(content.substring(p0, i1 + slen));
        buf.append(directive);
        p0 = i2;
        i1 = content.indexOf(DIRECTIVE_START, p0);
      }
    }
    if (i2 > 0) {
      // the content of the JSP was changed
      buf.append(content.substring(p0, content.length()));
      content = buf.toString();
    }
    return content;
  }

  /**
   * Parses all jsp link macros, and replace them by the right target path.
   *
   * <p>
   *
   * @param content the content to parse
   * @param controller the request controller
   * @return the parsed content
   */
  protected @RUntainted String parseJspLinkMacros(@RUntainted String content, CmsFlexController controller) {

    CmsJspLinkMacroResolver macroResolver =
        new CmsJspLinkMacroResolver(controller.getCmsObject(), null, true);
    return macroResolver.resolveMacros(content);
  }

  /**
   * Returns the jsp resource identified by the given name, using the controllers cms context.
   *
   * <p>
   *
   * @param controller the flex controller
   * @param jspName the name of the jsp
   * @return an OpenCms resource
   * @throws CmsException if something goes wrong
   */
  protected CmsResource readJspResource(CmsFlexController controller, String jspName)
      throws CmsException {

    // create an OpenCms user context that operates in the root site
    CmsObject cms = OpenCms.initCmsObject(controller.getCmsObject());
    // we only need to change the site, but not the project,
    // since the request has already the right project set
    cms.getRequestContext().setSiteRoot("");
    // try to read the resource
    return cms.readResource(jspName);
  }

  /**
   * Delivers the plain uninterpreted resource with escaped XML.
   *
   * <p>This is intended for viewing historical versions.
   *
   * <p>
   *
   * @param cms the initialized CmsObject which provides user permissions
   * @param file the requested OpenCms VFS resource
   * @param req the servlet request
   * @param res the servlet response
   * @throws IOException might be thrown by the servlet environment
   * @throws CmsException in case of errors accessing OpenCms functions
   */
  protected void showSource(
      CmsObject cms, CmsResource file, HttpServletRequest req, HttpServletResponse res)
      throws CmsException, IOException {

    CmsResource historyResource = (CmsResource) CmsHistoryResourceHandler.getHistoryResource(req);
    if (historyResource == null) {
      historyResource = file;
    }
    CmsFile historyFile = cms.readFile(historyResource);
    String content = new String(historyFile.getContents());
    // change the content-type header so that browsers show plain text
    res.setContentLength(content.length());
    res.setContentType("text/plain");

    Writer out = res.getWriter();
    out.write(content);
    out.close();
  }

  /**
   * Updates a JSP page in the "real" file system in case the VFS resource has changed based on the
   * resource name.
   *
   * <p>Generates a resource based on the provided name and calls {@link #updateJsp(CmsResource,
   * CmsFlexController, Set)}.
   *
   * <p>
   *
   * @param vfsName the name of the JSP file resource in the VFS
   * @param controller the controller for the JSP integration
   * @param updatedFiles a Set containing all JSP pages that have been already updated
   * @return the file name of the updated JSP in the "real" FS
   */
  protected @RUntainted String updateJsp(
      @RUntainted String vfsName, CmsFlexController controller, Set<String> updatedFiles) {

    @RUntainted String jspVfsName =
        CmsLinkManager.getAbsoluteUri(vfsName, controller.getCurrentRequest().getElementRootPath());
    if (LOG.isDebugEnabled()) {
      LOG.debug(Messages.get().getBundle().key(Messages.LOG_UPDATE_JSP_1, jspVfsName));
    }
    @RUntainted String jspRfsName;
    try {
      CmsResource includeResource;
      try {
        // first try a root path
        includeResource = readJspResource(controller, jspVfsName);
      } catch (CmsVfsResourceNotFoundException e) {
        // if fails, try a site relative path
        includeResource =
            readJspResource(
                controller, controller.getCmsObject().getRequestContext().addSiteRoot(jspVfsName));
      }
      // make sure the jsp referenced file is generated
      jspRfsName = updateJsp(includeResource, controller, updatedFiles);
      if (LOG.isDebugEnabled()) {
        LOG.debug(Messages.get().getBundle().key(Messages.LOG_NAME_REAL_FS_1, jspRfsName));
      }
    } catch (Exception e) {
      jspRfsName = null;
      if (LOG.isDebugEnabled()) {
        LOG.debug(Messages.get().getBundle().key(Messages.LOG_ERR_UPDATE_1, jspVfsName), e);
      }
    }
    return jspRfsName;
  }

  /**
   * Updates all jsp files that include the given jsp file using the 'link.strong' macro.
   *
   * <p>
   *
   * @param resource the current updated jsp file
   * @param controller the controller for the jsp integration
   * @param updatedFiles the already updated files
   * @return <code>true</code> if the given JSP file should be updated due to dirty included files
   * @throws ServletException might be thrown in the process of including the JSP
   * @throws IOException might be thrown in the process of including the JSP
   * @throws CmsLoaderException if the resource type can not be read
   */
  protected boolean updateStrongLinks(
      CmsResource resource, CmsFlexController controller, Set<String> updatedFiles)
      throws CmsLoaderException, IOException, ServletException {

    int numberOfUpdates = updatedFiles.size();
    CmsObject cms = controller.getCmsObject();
    CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.JSP_STRONG);
    Iterator<CmsRelation> it;
    try {
      it = cms.getRelationsForResource(resource, filter).iterator();
    } catch (CmsException e) {
      // should never happen
      if (LOG.isErrorEnabled()) {
        LOG.error(e.getLocalizedMessage(), e);
      }
      return false;
    }
    while (it.hasNext()) {
      CmsRelation relation = it.next();
      CmsResource target = null;
      try {
        target = relation.getTarget(cms, CmsResourceFilter.DEFAULT);
      } catch (CmsException e) {
        // should never happen
        if (LOG.isErrorEnabled()) {
          LOG.error(e.getLocalizedMessage(), e);
        }
        continue;
      }
      // prevent recursive update when including the same file
      if (resource.equals(target)) {
        continue;
      }
      // update the target
      updateJsp(target, controller, updatedFiles);
    }
    // the current jsp file should be updated only if one of the included jsp has been updated
    return numberOfUpdates < updatedFiles.size();
  }

  /**
   * Returns the read-write-lock for the given jsp vfs name.
   *
   * <p>
   *
   * @param jspVfsName the jsp vfs name
   * @return the read-write-lock
   */
  private ReentrantReadWriteLock getFileLock(String jspVfsName) {

    ReentrantReadWriteLock lock = m_fileLocks.get(jspVfsName);
    if (lock == null) {
      // acquire the purge lock before adding new file lock entries
      // in case of a JSP repository purge, adding new file lock entries is blocked
      // and all present file locks will be locked for purge
      // @see #triggerPurge()
      m_purgeLock.readLock().lock();
      synchronized (m_fileLocks) {
        if (!m_fileLocks.containsKey(jspVfsName)) {
          m_fileLocks.put(jspVfsName, new ReentrantReadWriteLock(true));
        }
        lock = m_fileLocks.get(jspVfsName);
      }
      m_purgeLock.readLock().unlock();
    }
    return lock;
  }

  /**
   * Returns the RFS path for a JSP resource.
   *
   * <p>This does not check whether there actually exists a file at the returned path.
   *
   * @param resource the JSP resource
   * @param online true if the path for the online project should be returned
   * @return the RFS path for the JSP
   * @throws CmsLoaderException if accessing the resource loader fails
   */
  private String getJspRfsPath(CmsResource resource, boolean online) throws CmsLoaderException {

    String jspVfsName = resource.getRootPath();
    String extension;
    int loaderId = OpenCms.getResourceManager().getResourceType(resource.getTypeId()).getLoaderId();
    if ((loaderId == CmsJspLoader.RESOURCE_LOADER_ID) && (!jspVfsName.endsWith(JSP_EXTENSION))) {
      // this is a true JSP resource that does not end with ".jsp"
      extension = JSP_EXTENSION;
    } else {
      // not a JSP resource or already ends with ".jsp"
      extension = "";
    }
    String jspPath = CmsFileUtil.getRepositoryName(m_jspRepository, jspVfsName + extension, online);
    return jspPath;
  }
}
