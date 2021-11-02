/*
 * Copyright 2020 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bjwgby.az.dep.manager.servlet;

import azkaban.ServiceProvider;
import azkaban.dep.DepService;
import azkaban.dep.bo.DepFlowRelation;
import azkaban.dep.bo.ProjectBrief;
import azkaban.dep.vo.DepFlowRelationDetail;
import azkaban.flow.Flow;
import azkaban.project.Project;
import azkaban.project.ProjectManager;
import azkaban.server.HttpRequestUtils;
import azkaban.server.session.Session;
import azkaban.user.Permission;
import azkaban.user.User;
import azkaban.utils.Props;
import azkaban.webapp.servlet.LoginAbstractAzkabanServlet;
import azkaban.webapp.servlet.Page;
import com.google.gson.JsonObject;
import com.google.inject.Injector;
import com.webank.wedatasphere.schedulis.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class DepManagerServlet extends LoginAbstractAzkabanServlet {

    private static final Logger logger = LoggerFactory.getLogger(DepManagerServlet.class.getName());
    private static final long serialVersionUID = 1L;

    public static final String CODE = "code";
    public static final String MSG = "message";
    public static final String DATA = "data";

    private DepService depService;
    private Props propsPlugin;
    private Props propsAzkaban;
    private final File webResourcesPath;

    private final String viewerName;
    private final String viewerPath;
    private ProjectManager projectManager;

    public DepManagerServlet(final Props propsPlugin) {

        this.propsPlugin = propsPlugin;
        this.viewerName = propsPlugin.getString("viewer.name");
        this.viewerPath = propsPlugin.getString("viewer.path");

        this.webResourcesPath = new File(new File(propsPlugin.getSource()).getParentFile().getParentFile(), "web");
        this.webResourcesPath.mkdirs();

        setResourceDirectory(this.webResourcesPath);

    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        Injector injector = ServiceProvider.SERVICE_PROVIDER.getInjector();
        this.depService = injector.getInstance(DepService.class);
        propsAzkaban = ServiceProvider.SERVICE_PROVIDER.getInstance(Props.class);
        this.projectManager = injector.getInstance(ProjectManager.class);

    }

    @Override
    protected void handleGet(final HttpServletRequest req, final HttpServletResponse resp,
                             final Session session) throws ServletException, IOException {

        if (hasParam(req, "ajax")) {
            handleAJAXAction(req, resp, session);
        } else {
            handleSystemPage(req, resp, session);
        }
    }

    /**
     * 数据补采历史页面
     *
     * @param req
     * @param resp
     * @param session
     * @throws ServletException
     */
    private void handleSystemPage(final HttpServletRequest req, final HttpServletResponse resp, final Session session)
            throws ServletException {
        final Page page =
                newPage(req, resp, session, "/com.bjwgby.az.dep.viewer/index.html");
        page.render();

    }

    private void handleAJAXAction(final HttpServletRequest req,
                                  final HttpServletResponse resp, final Session session) throws ServletException,
            IOException {
        final HashMap<String, Object> ret = new HashMap<>();
        final String ajaxName = getParam(req, "ajax");

        if (ajaxName.equals("searchFlowRelation")) {
            // 通过非登录页面的快速通道新增用户
            searchFlowRelation(req, resp, session, ret);
        } else if (ajaxName.equals("addFlowRelation")) {
            addFlowRelation(req, resp, session, ret);
        } else if (ajaxName.equals("searchProjectByName")) {
            searchProjectByName(req, resp, session, ret);
        } else if (ajaxName.equals("searchUserProjectByName")) {
            searchUserProjectByName(req, resp, session, ret);
        } else if (ajaxName.equals("getFlowsByProject")) {
            getFlowsByProject(req, resp, session, ret);
        }

        if (ret != null) {
            this.writeJSON(resp, ret);
        }
    }

    private void getFlowsByProject(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) {
        try {
            int projectId = getIntParam(req, "projectId");
            List<String> result = this.depService.getFlowsByProject(projectId);
            ret.put(CODE, 0);
            ret.put(DATA, result);
        } catch (ServletException e) {
            logger.error("error while getIntParam:projectId ", e);
            ret.put(CODE, 1);
            ret.put(MSG, "error while getIntParam:projectId ");
        } catch (Exception e) {
            logger.error("error while getFlowsByProject ", e);
            ret.put(CODE, 1);
            ret.put(MSG, "error while getFlowsByProject");
        }

    }

    private void searchUserProjectByName(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) {
        try {
            String searchText = getParam(req, "searchText");
            List<ProjectBrief> result = this.depService.searchUserProjectByName(searchText, session.getUser());
            ret.put(CODE, 0);
            ret.put(DATA, result);
        } catch (ServletException e) {
            logger.error("error while getParam:searchText ", e);
            ret.put(CODE, 1);
            ret.put(MSG, "error while getParam:searchText ");
        } catch (Exception e) {
            logger.error("error while searchUserProjectByName ", e);
            ret.put(CODE, 1);
            ret.put(MSG, "error while searchUserProjectByName");
        }
    }

    private void searchProjectByName(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) {
        try {
            String searchText = getParam(req, "searchText");
            List<ProjectBrief> result = this.depService.searchProjectByName(searchText);
            ret.put(CODE, 0);
            ret.put(DATA, result);
        } catch (ServletException e) {
            logger.error("error while getParam:searchText ", e);
            ret.put(CODE, 1);
            ret.put(MSG, "error while getParam:searchText ");
        } catch (Exception e) {
            logger.error("error while searchUserProjectByName ", e);
            ret.put(CODE, 1);
            ret.put(MSG, "error while searchUserProjectByName");
        }
    }

    private void addFlowRelation(HttpServletRequest req, HttpServletResponse resp, final Session session, HashMap<String, Object> ret) {
        User user = session.getUser();
        JsonObject jsonObject = HttpRequestUtils.parseRequestToJsonObject(req);
        DepFlowRelation depFlowRelation = GsonUtils.jsonToJavaObject(jsonObject, DepFlowRelation.class);

        Project depedProject = projectManager.getProject(depFlowRelation.getDependedProjectId());
        if (depedProject == null) {
            ret.put(CODE, 1);
            ret.put(MSG, "deped project does not exist");
            return;
        }

        Flow depedFlow = depedProject.getFlow(depFlowRelation.getDependedFlowId());
        if (depedFlow == null) {
            ret.put(CODE, 1);
            ret.put(MSG, "deped flow does not exist");
            return;
        }

        Project project = projectManager.getProject(depFlowRelation.getProjectId());
        if (project == null) {
            ret.put(CODE, 1);
            ret.put(MSG, " project does not exist");
            return;
        }
        if (!project.hasPermission(user, Permission.Type.ADMIN)) {
            ret.put(CODE, 1);
            ret.put(MSG, "does not have permission on project!");
            return;
        }

        Flow flow = project.getFlow(depFlowRelation.getFlowId());
        if (flow == null) {
            ret.put(CODE, 1);
            ret.put(MSG, "flow does not exist");
            return;
        }

        try {
            DepFlowRelation existedRelation = this.depService.getDepFlowRelationByLogicKey(depFlowRelation);
            if (existedRelation != null) {
                logger.warn("flowRelation exist already:{}", existedRelation);
                ret.put(CODE, 1);
                ret.put(MSG, "flowRelation exist already");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ret.put(CODE, 1);
            ret.put(MSG, "something error,pls check");
            return;
        }
        try {
            this.depService.newDepFlowRelation(depFlowRelation);
        } catch (SQLException e) {
            e.printStackTrace();
            ret.put(CODE, 1);
            ret.put(MSG, "something error,pls check");
            return;
        }

    }

    private void searchFlowRelation(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) throws ServletException {
        User user = session.getUser();
        Integer depedProjectId = getIntParam(req, "dependedProjectId");
        String depedFlowId = getParam(req, "dependedFlowId");
        Integer projectId = getIntParam(req, "projectId");
        String flowId = getParam(req, "flowId");
        String userName = user.getUserId();
        int pageNum = getIntParam(req, "pageNum", 1);
        int pageSize = getIntParam(req, "pageSize", 20);

        try {
            List<DepFlowRelationDetail> result = this.depService.searchFlowRelation(depedProjectId, depedFlowId, projectId, flowId, userName, pageNum, pageSize);
            ret.put("total", 10);
            ret.put("data", result);
        } catch (SQLException e) {
            e.printStackTrace();
            ret.put(CODE, 1);
            ret.put(MSG, "searchFlowRelation error,pls contat administrator!");
        }

    }


    @Override
    protected void handlePost(final HttpServletRequest req, final HttpServletResponse resp,
                              final Session session) throws ServletException, IOException {
        if (hasParam(req, "ajax")) {
            handleAJAXAction(req, resp, session);
        }
    }


}
