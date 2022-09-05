package com.bjwgby.az.dep.manager.servlet;

import azkaban.ServiceProvider;
import azkaban.dep.exception.CycleDepRelationException;
import azkaban.dep.DepFlowInstance;
import azkaban.dep.DepService;
import azkaban.dep.bo.DepFlowRelation;
import azkaban.dep.bo.ProjectBrief;
import azkaban.dep.vo.DepFlowInstanceDetail;
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
import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.google.inject.Injector;
import com.webank.wedatasphere.schedulis.common.utils.GsonUtils;
import org.apache.commons.io.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
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
            searchFlowRelation(req, resp, session, ret);
        } else if (ajaxName.equals("addFlowRelation")) {
            addFlowRelation(req, resp, session, ret);
        } else if (ajaxName.equals("searchProjectByName")) {
            searchProjectByName(req, resp, session, ret);
        } else if (ajaxName.equals("searchUserProjectByName")) {
            searchUserProjectByName(req, resp, session, ret);
        } else if (ajaxName.equals("getFlowsByProject")) {
            getFlowsByProject(req, resp, session, ret);
        } else if (ajaxName.equals("deleteFlowRelation")) {
            deleteFlowRelation(req, resp, session, ret);
        } else if (ajaxName.equals("searchFlowInstance")) {
            searchFlowInstance(req, resp, session, ret);
        } else if (ajaxName.equals("redoFlowInstance")) {
            redoFlowInstance(req, resp, session, ret);
        } else if (ajaxName.equals("loginUserInfo")) {
            loginUserInfo(req, resp, session, ret);
        }

        if (ret != null) {
//            this.writeJSON(resp, ret);
            this.writeJsonResult(resp, ret);
        }
    }

    private void loginUserInfo(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) {

        User user = session.getUser();
        if (user == null) {
            this.returnError(1, "no userInfo,pls login first", ret);
            return;
        }
        ret.put(CODE, 0);
        ret.put(DATA, user);
        return;

    }

    private void deleteFlowRelation(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) throws ServletException {

        if (!HttpRequestUtils.hasParam(req, "id")) {
            this.returnError(1, "pls specify id", ret);
            return;
        }
        int id = HttpRequestUtils.getIntParam(req, "id");

        try {
            DepFlowRelation relation = this.depService.getDepFlowRelationByKey(id);

            if (relation == null) {
                this.returnError(1, "relation not found!", ret);
                return;
            }

            Project project = this.projectManager.getProject(relation.getProjectId());

            if (project == null) {
                this.returnError(1, "project not found", ret);
                return;
            }

            User user = session.getUser();
            if (!project.hasPermission(user, Permission.Type.SCHEDULE)) {
                this.returnError(1, "no permission", ret);
                return;
            }


            int effectRowNum = this.depService.deleteFlowRelationById(id);
            logger.info("delete dep flow relation:{},effectRowNum:{}", relation, effectRowNum);
            ret.put(CODE, 0);
            ret.put(MSG, effectRowNum + " row delete");
        } catch (SQLException e) {
            logger.error("delete flow relation failed,id:" + id, e);
            returnError(1, "Delete flow relation failed", ret);
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
            returnError(1, "deped project does not exist", ret);
            return;
        }

        Flow depedFlow = depedProject.getFlow(depFlowRelation.getDependedFlowId());
        if (depedFlow == null) {
            returnError(1, "deped flow does not exist", ret);
            return;
        }

        Project project = projectManager.getProject(depFlowRelation.getProjectId());
        if (project == null) {
            returnError(1, " project does not exist", ret);
            return;
        }
        if (!(project.hasPermission(user, Permission.Type.SCHEDULE))) {
            returnError(1, "does not have permission on project!", ret);
            return;
        }

        Flow flow = project.getFlow(depFlowRelation.getFlowId());
        if (flow == null) {
            returnError(1, "flow does not exist", ret);

            return;
        }

        try {
            DepFlowRelation existedRelation = this.depService.getDepFlowRelationByLogicKey(depFlowRelation);
            if (existedRelation != null) {
                logger.warn("flowRelation exist already:{}", existedRelation);
                returnError(1, "flowRelation exist already", ret);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            returnError(1, "something error,pls check", ret);
            return;
        }
        try {
            depFlowRelation.setCreateUser(user.getUserId());
            this.depService.newDepFlowRelation(depFlowRelation);
            ret.put(CODE, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            returnError(1, "something error,pls check", ret);
            return;
        } catch (CycleDepRelationException e) {
            e.printStackTrace();
            returnError(1, "cycle dep relation found!", ret);
            return;
        }

    }

    private void searchFlowRelation(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) throws ServletException {
        User user = session.getUser();
        Integer depedProjectId = HttpRequestUtils.hasParam(req, "dependedProjectId") ? getIntParam(req, "dependedProjectId") : null;
        String depedFlowId = getParam(req, "dependedFlowId", null);
        Integer projectId = HttpRequestUtils.hasParam(req, "projectId") ? getIntParam(req, "projectId") : null;
        String flowId = getParam(req, "flowId", null);
        String userName = user.getUserId();
        int pageNum = getIntParam(req, "pageNum", 1);
        int pageSize = getIntParam(req, "pageSize", 20);

        try {
            if (projectId != null) {
                Project project = this.projectManager.getProject(projectId);


                if (project == null) {
                    this.returnError(1, "project not found", ret);
                    return;
                }

                if (!project.hasPermission(user, Permission.Type.SCHEDULE)) {
                    this.returnError(1, "no permission", ret);
                    return;
                }
            }

            List<DepFlowRelationDetail> result = this.depService.searchFlowRelation(depedProjectId, depedFlowId, projectId, flowId, userName, pageNum, pageSize);
            int total = this.depService.searchFlowRelationCount(depedProjectId, depedFlowId, projectId, flowId, userName);
            ret.put("total", total);
            ret.put("data", result);
            ret.put(CODE, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            ret.put(CODE, 1);
            ret.put(MSG, "searchFlowRelation error,pls contat administrator!");
        }

    }

    private void searchFlowInstance(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) throws ServletException {
        User user = session.getUser();


        Integer projectId = HttpRequestUtils.hasParam(req, "projectId") ? getIntParam(req, "projectId") : null;
        String flowId = getParam(req, "flowId", null);

        List<Integer> statuses = this.getIntParamList(req, "statuses[]", null);
        String startTimeId = getParam(req, "startTimeId", null);
        String endTimeId = getParam(req, "endTimeId", null);
        String userName = user.getUserId();
        int pageNum = getIntParam(req, "pageNum", 1);
        int pageSize = getIntParam(req, "pageSize", 20);

        try {
            if (projectId != null) {
                Project project = this.projectManager.getProject(projectId);


                if (project == null) {
                    this.returnError(1, "project not found", ret);
                    return;
                }
            }


            List<DepFlowInstanceDetail> result = this.depService.searchFlowInstance(projectId, flowId, statuses, startTimeId, endTimeId, userName, pageNum, pageSize);
            int total = this.depService.searchFlowInstanceCount(projectId, flowId, statuses, startTimeId, endTimeId, userName);
            ret.put("total", total);
            ret.put("data", result);
            ret.put(CODE, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            ret.put(CODE, 1);
            ret.put(MSG, "searchFlowRelation error,pls contat administrator!");
        }
    }

    private void redoFlowInstance(HttpServletRequest req, HttpServletResponse resp, Session session, HashMap<String, Object> ret) {
        User user = session.getUser();
        DepFlowInstance instance = this.getJsonObject(req, DepFlowInstance.class);

        Project project = projectManager.getProject(instance.getProjectId());
        if (project == null) {
            returnError(1, " project does not exist", ret);
            return;
        }
        if (!project.hasPermission(user, Permission.Type.EXECUTE)) {
            returnError(1, "does not have permission on project!", ret);
            return;
        }

        Flow flow = project.getFlow(instance.getFlowId());
        if (flow == null) {
            returnError(1, "flow does not exist", ret);

            return;
        }

        try {
            this.depService.checkCanRedoFlowInstance(instance);
        } catch (Exception e) {
            logger.error("check failed,can`t redo,pls check", e);
            returnError(1, e.getMessage(), ret);
            return;
        }
        try {
            int effectRowNum = this.depService.redoDepFlowInstance(instance);
            ret.put(CODE, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            returnError(1, "something error,pls check", ret);
            return;
        }
    }

    @Override
    protected void handlePost(final HttpServletRequest req, final HttpServletResponse resp,
                              final Session session) throws ServletException, IOException {
        if (hasParam(req, "ajax")) {
            handleAJAXAction(req, resp, session);
        }
    }

    private void returnError(int code, String msg, Map<String, Object> ret) {
        ret.put(CODE, code);
        ret.put(MSG, msg);
    }

    private List<Integer> getIntParamList(final HttpServletRequest req, String paramName, List<Integer> defaultVal) {
        List<Integer> result = null;
        String[] statusesStr = req.getParameterValues(paramName);
        if (statusesStr != null && statusesStr.length > 0) {
            result = new ArrayList<>(statusesStr.length);
            for (String statusStr : statusesStr) {
                result.add(Integer.valueOf(statusStr));
            }

        }
        return result;
    }

    private void writeJsonResult(final HttpServletResponse resp, Object obj) throws IOException {
        resp.setContentType(JSON_MIME_TYPE);
        String result = JSON.toJSONString(obj);
        resp.getWriter().write(result);
        resp.flushBuffer();

    }


    private <T> T getJsonObject(final HttpServletRequest req, Class<T> clazz) {
        T result = null;
        try {

            ServletInputStream is = req.getInputStream();
            result = JSON.parseObject(is, Charsets.UTF_8, clazz);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

}
