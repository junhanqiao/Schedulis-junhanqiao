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
import azkaban.dep.vo.DepFlowRelationDetail;
import azkaban.server.session.Session;
import azkaban.user.User;
import azkaban.utils.Props;
import azkaban.webapp.servlet.LoginAbstractAzkabanServlet;
import azkaban.webapp.servlet.Page;
import com.google.inject.Injector;
import com.webank.wedatasphere.schedulis.common.i18nutils.LoadJsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepManagerServlet extends LoginAbstractAzkabanServlet {

    private static final Logger logger = LoggerFactory.getLogger(DepManagerServlet.class.getName());
    private static final long serialVersionUID = 1L;
    private DepService depService;
    private Props propsPlugin;
    private Props propsAzkaban;
    private final File webResourcesPath;

    private final String viewerName;
    private final String viewerPath;

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
        } else if (ajaxName.equals("fetch")) {
            fetchHistoryData(req, resp, ret);
        } else if (ajaxName.equals("user_role")) {
            ajaxGetUserRole(req, resp, session, ret);
        } else if (ajaxName.equals("loadWebankUserSelectData")) {
            ajaxLoadWebankUserSelectData(req, resp, session, ret);
        } else if (ajaxName.equals("findSystemUserPage")) {
            ajaxFindSystemUserPage(req, resp, session, ret);
        }

        if (ret != null) {
            this.writeJSON(resp, ret);
        }
    }

    private void searchFlowRelation(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) throws ServletException {
        User user = session.getUser();
        Integer depedProjectId=getIntParam(req,"depedProjectId");
        String depedFlowId=getParam(req,"depedFlowId");
        Integer projectId=getIntParam(req,"projectId");
        String flowId=getParam(req,"flowId");
        String userName = user.getUserId();
        int pageNum=getIntParam(req,"pageNum",1);
        int pageSize=getIntParam(req,"pageSize",20);

        try {
            List<DepFlowRelationDetail> result = this.depService.searchFlowRelation(depedProjectId, depedFlowId, projectId, flowId, userName, pageNum, pageSize);
            ret.put("total",10);
            ret.put("data",result);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 加载 SystemServlet 中的异常信息等国际化资源
     * @return
     */
    private Map<String, String> loadSystemServletI18nData() {
        String languageType = LoadJsonUtils.getLanguageType();
        Map<String, String> dataMap;
        if (languageType.equalsIgnoreCase("zh_CN")) {
            dataMap = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/az-webank-system-manager-zh_CN.json",
                    "com.webank.wedatasphere.schedulis.system.servlet.SystemServlet");
        }else {
            dataMap = LoadJsonUtils.transJson("/com.webank.wedatasphere.schedulis.i18n.conf/az-webank-system-manager-en_US.json",
                    "com.webank.wedatasphere.schedulis.system.servlet.SystemServlet");
        }
        return dataMap;
    }


    /**
     * 通过非登录页面的快速通道新增用户
     *
     * @param req
     * @param resp
     * @param ret
     * @throws ServletException
     */
    private void ajaxAddSystemUserViaFastTrack(final HttpServletRequest req, final HttpServletResponse resp,
                                               final Session session, final HashMap<String, Object> ret) throws ServletException {
        String userId;
        if (hasParam(req, "userId")) {
            userId = getParam(req, "userId");
        } else {
            userId = null;
        }
        final String password = getParam(req, "password");
        String tempRoleId = getParam(req, "roleId");
        int roleId;
        if (StringUtils.isNotBlank(tempRoleId)) {
            roleId = Integer.valueOf(tempRoleId);
        } else {
            roleId = 0;
        }
        String proxyUser = getParam(req, "proxyUser");
        String tempDepartmentId = getParam(req, "departmentId");
        int departmentId;
        if (StringUtils.isNotBlank(tempDepartmentId)) {
            departmentId = Integer.valueOf(getParam(req, "departmentId"));
        } else {
            departmentId = -1;
        }

    }


    private void fetchHistoryData(final HttpServletRequest req,
                                  final HttpServletResponse resp, final HashMap<String, Object> ret)
            throws ServletException {
    }

    //返回当前用户的角色列表
    private void ajaxGetUserRole(final HttpServletRequest req,
                                 final HttpServletResponse resp, final Session session, final HashMap<String, Object> ret) {
        final String[] userRoles = session.getUser().getRoles().toArray(new String[0]);
        ret.put("userRoles", userRoles);
    }


    @Override
    protected void handlePost(final HttpServletRequest req, final HttpServletResponse resp,
                              final Session session) throws ServletException, IOException {
        if (hasParam(req, "ajax")) {
            handleAJAXAction(req, resp, session);
        }
    }

    public static class PageSelection {

        private final int page;
        private final int size;
        private final boolean disabled;
        private boolean selected;

        public PageSelection(final int page, final int size, final boolean disabled,
                             final boolean selected) {
            this.page = page;
            this.size = size;
            this.disabled = disabled;
            this.setSelected(selected);
        }

        public int getPage() {
            return this.page;
        }

        public int getSize() {
            return this.size;
        }

        public boolean getDisabled() {
            return this.disabled;
        }

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(final boolean selected) {
            this.selected = selected;
        }
    }

    private void ajaxLoadWebankUserSelectData(final HttpServletRequest req, final HttpServletResponse resp
            , final Session session, final HashMap<String, Object> ret)
            throws ServletException {

        String searchName = req.getParameter("serach");
        int pageNum = getIntParam(req, "page");
        int pageSize = getIntParam(req, "pageSize");


        List<Map<String, Object>> webankUserSelectList = new ArrayList<>();

        JSONObject items = new JSONObject();

        try {


        } catch (Exception e) {
            e.printStackTrace();
        }

        ret.put("page", pageNum);
        ret.put("webankUserList", webankUserSelectList);

    }


    private void ajaxFindSystemUserPage(final HttpServletRequest req, final HttpServletResponse resp,
                                        final Session session, final HashMap<String, Object> ret)
            throws ServletException {
        int start = Integer.valueOf(getParam(req, "start"));
        final int pageSize = Integer.valueOf(getParam(req, "pageSize"));
        final String searchterm = getParam(req, "searchterm").trim();

        Map<String, String> dataMap = loadSystemServletI18nData();

        int total = 0;


    }


}
