/*
 * Copyright 2012 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package azkaban.webapp.servlet;

import azkaban.Constants;
import azkaban.executor.*;
import azkaban.executor.ExecutionOptions.FailureAction;
import azkaban.flow.Flow;
import azkaban.flow.FlowUtils;
import azkaban.flow.Node;
import azkaban.flowtrigger.FlowTriggerService;
import azkaban.flowtrigger.TriggerInstance;
import azkaban.project.Project;
import azkaban.project.ProjectManager;
import azkaban.scheduler.Schedule;
import azkaban.scheduler.ScheduleManager;
import azkaban.scheduler.ScheduleManagerException;
import azkaban.server.HttpRequestUtils;
import azkaban.server.session.Session;
import azkaban.sla.SlaOption;
import azkaban.user.Permission;
import azkaban.user.Permission.Type;
import azkaban.user.User;
import azkaban.user.UserManagerException;
import azkaban.utils.ExternalLinkUtils;
import azkaban.utils.FileIOUtils;
import azkaban.utils.FileIOUtils.LogData;
import azkaban.utils.Pair;
import azkaban.utils.Props;
import azkaban.webapp.AzkabanWebServer;
import azkaban.webapp.WebMetrics;
import azkaban.webapp.plugin.PluginRegistry;
import azkaban.webapp.plugin.ViewerPlugin;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.schedulis.common.executor.ExecutionCycle;
import com.webank.wedatasphere.schedulis.common.i18nutils.LoadJsonUtils;
import com.webank.wedatasphere.schedulis.common.log.LogFilterEntity;
import com.webank.wedatasphere.schedulis.common.system.SystemManager;
import com.webank.wedatasphere.schedulis.common.system.SystemUserManagerException;
import com.webank.wedatasphere.schedulis.common.system.common.TransitionService;
import com.webank.wedatasphere.schedulis.common.system.entity.WtssUser;
import com.webank.wedatasphere.schedulis.common.user.SystemUserManager;
import com.webank.wedatasphere.schedulis.common.utils.AlertUtil;
import com.webank.wedatasphere.schedulis.common.utils.GsonUtils;
import com.webank.wedatasphere.schedulis.common.utils.LogErrorCodeFilterUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static azkaban.ServiceProvider.SERVICE_PROVIDER;


public class ReExeDepFlowServlet extends LoginAbstractAzkabanServlet {

    private static final Logger logger = LoggerFactory.getLogger(ReExeDepFlowServlet.class.getName());
    private static final long serialVersionUID = 1L;
    private WebMetrics webMetrics;
    private ProjectManager projectManager;
    private FlowTriggerService flowTriggerService;
    private ExecutorManagerAdapter executorManagerAdapter;
    private ScheduleManager scheduleManager;
    private TransitionService transitionService;
    private AlerterHolder alerterHolder;


    //历史补采停止集合
    private Map<String, String> repeatStopMap = new HashMap<>();

    private SystemManager systemManager;


    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        final AzkabanWebServer server = (AzkabanWebServer) getApplication();
        this.projectManager = server.getProjectManager();
        this.executorManagerAdapter = server.getExecutorManager();
        this.scheduleManager = server.getScheduleManager();
        this.transitionService = server.getTransitionService();
        this.flowTriggerService = server.getFlowTriggerService();
        // TODO: reallocf fully guicify
        this.webMetrics = SERVICE_PROVIDER.getInstance(WebMetrics.class);
        this.alerterHolder = server.getAlerterHolder();
        Props props = executorManagerAdapter.getAzkabanProps();
        this.systemManager = transitionService.getSystemManager();

    }

    @Override
    protected void handleGet(final HttpServletRequest req, final HttpServletResponse resp,
                             final Session session) throws ServletException, IOException {

        handleExecutionFlowPageByExecId(req, resp, session);
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp, Session session) throws ServletException, IOException {

    }


    /**
     * 读取executingflowpage.vm及其子页面的国际化资源数据
     * @return
     */
    private Map<String, Map<String, String>> loadExecutingflowpageI18nData() {
        Map<String, Map<String, String>> dataMap = new HashMap<>();
        String languageType = LoadJsonUtils.getLanguageType();
        Map<String, String> executingflowpageMap;
        Map<String, String> subPageMap1;
        Map<String, String> subPageMap2;
        Map<String, String> subPageMap3;
        Map<String, String> subPageMap4;
        if (languageType.equalsIgnoreCase("zh_CN")) {
            // 添加国际化标签
            executingflowpageMap = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-zh_CN.json",
                "azkaban.webapp.servlet.velocity.executingflowpage.vm");

            subPageMap1 = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-zh_CN.json",
                "azkaban.webapp.servlet.velocity.nav.vm");

            subPageMap2 = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-zh_CN.json",
                "azkaban.webapp.servlet.velocity.flow-schedule-ecution-panel.vm");

            subPageMap3 = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-zh_CN.json",
                "azkaban.webapp.servlet.velocity.messagedialog.vm");

            subPageMap4 = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-zh_CN.json",
                "azkaban.webapp.servlet.velocity.flowgraphview.vm");
        }else {
            // 添加国际化标签
            executingflowpageMap = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-en_US.json",
                "azkaban.webapp.servlet.velocity.executingflowpage.vm");

            subPageMap1 = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-en_US.json",
                "azkaban.webapp.servlet.velocity.nav.vm");

            subPageMap2 = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-en_US.json",
                "azkaban.webapp.servlet.velocity.flow-schedule-ecution-panel.vm");

            subPageMap3 = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-en_US.json",
                "azkaban.webapp.servlet.velocity.messagedialog.vm");

            subPageMap4 = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-en_US.json",
                "azkaban.webapp.servlet.velocity.flowgraphview.vm");
        }

        dataMap.put("executingflowpage.vm", executingflowpageMap);
        dataMap.put("nav.vm", subPageMap1);
        dataMap.put("flow-schedule-ecution-panel.vm", subPageMap2);
        dataMap.put("messagedialog.vm", subPageMap3);
        dataMap.put("flowgraphview.vm", subPageMap4);

        return dataMap;
    }

    /**
     * 加载ExecutorServlet中的异常信息等国际化资源
     * @return
     */
    private Map<String, String> loadExecutorServletI18nData() {
        String languageType = LoadJsonUtils.getLanguageType();
        Map<String, String> dataMap;
        if (languageType.equalsIgnoreCase("zh_CN")) {
            dataMap = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-zh_CN.json",
                "azkaban.webapp.servlet.ExecutorServlet");
        }else {
            dataMap = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/azkaban-web-server-en_US.json",
                "azkaban.webapp.servlet.ExecutorServlet");
        }
        return dataMap;
    }



    private void addExternalLinkLabel(final HttpServletRequest req, final Page page) {
        final Props props = getApplication().getServerProps();
        final String execExternalLinkURL = ExternalLinkUtils.getExternalAnalyzerOnReq(props, req);

        if (execExternalLinkURL.length() > 0) {
            page.add("executionExternalLinkURL", execExternalLinkURL);
            logger.debug("Added an External analyzer to the page");
            logger.debug("External analyzer url: " + execExternalLinkURL);

            final String execExternalLinkLabel =
                props.getString(Constants.ConfigurationKeys.AZKABAN_SERVER_EXTERNAL_ANALYZER_LABEL, "External Analyzer");
            page.add("executionExternalLinkLabel", execExternalLinkLabel);
            logger.debug("External analyzer label set to : " + execExternalLinkLabel);
        }
    }

    private void handleExecutionFlowPageByExecId(final HttpServletRequest req, final HttpServletResponse resp,
        final Session session) throws ServletException, IOException {
        final Page page = newPage(req, resp, session, "azkaban/webapp/servlet/velocity/reExeDepFlowPage.vm");
        final User user = session.getUser();
        final int execId = getIntParam(req, "execid");
        //当前节点的NestedId,如果查看整个工作流,则是空
        final String nodeNestedId = getParam(req, "nodeNestedId", "");
        final String depInstId = getParam(req, "depInstId", "");
        page.add("execid", execId);
        page.add("triggerInstanceId", "-1");
        page.add("loginUser", user.getUserId());
        page.add("nodeNestedId", nodeNestedId);
        page.add("depInstId", depInstId);

        // 加载国际化资源
        Map<String, Map<String, String>> dataMap = loadExecutingflowpageI18nData();
        dataMap.forEach((vm, data) -> data.forEach(page::add));

        ExecutableFlow flow = null;
        try {
            flow = this.executorManagerAdapter.getExecutableFlow(execId);
            if (flow == null) {
                page.add("errorMsg", "Error loading executing flow " + execId + " not found.");
                page.render();
                return;
            }
        } catch (final ExecutorManagerException e) {
            page.add("errorMsg", "Error loading executing flow: " + e.getMessage());
            page.render();
            return;
        }

        final int projectId = flow.getProjectId();
        final Project project = getProjectPageByPermission(page, projectId, user, Type.READ);
        if (project == null) {
            page.render();
            return;
        }

        addExternalLinkLabel(req, page);

        page.add("projectId", project.getId());
        page.add("projectName", project.getName());
        page.add("flowid", flow.getFlowId());

        final Permission perm = this.getPermissionObject(project, user, Type.ADMIN);

        final boolean adminPerm = perm.isPermissionSet(Type.ADMIN);

        if (perm.isPermissionSet(Type.EXECUTE) || adminPerm) {
            page.add("execPerm", true);
        } else {
            page.add("execPerm", false);
        }
        if (perm.isPermissionSet(Type.SCHEDULE) || adminPerm) {
            page.add("schedulePerm", true);
        } else {
            page.add("schedulePerm", false);
        }
        String languageType = LoadJsonUtils.getLanguageType();
        page.add("currentlangType", languageType);
        page.render();
    }

    protected Project getProjectPageByPermission(final Page page, final int projectId,
        final User user, final Type type) {
        final Project project = this.projectManager.getProject(projectId);

        Map<String, String> dataMap = loadExecutorServletI18nData();

        if (project == null) {
            page.add("errorMsg", dataMap.get("program") + project + dataMap.get("notExist"));
        } else if (!hasPermission(project, user, type)) {
            page.add("errorMsg", "User " + user.getUserId() + " doesn't have " + type.name()
                + " permissions on " + project.getName());
        } else {
            return project;
        }

        return null;
    }


    private Permission getPermissionObject(final Project project, final User user,
        final Type type) {
        final Permission perm = project.getCollectivePermission(user);

        for (final String roleName : user.getRoles()) {
            if (roleName.equals("admin") || systemManager.isDepartmentMaintainer(user)) {
                perm.addPermission(Type.ADMIN);
            }
        }

        return perm;
    }


}
