package com.example.bookstore.action;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.manager.UserManager;
import com.example.bookstore.util.CommonUtil;

public class AuditLogAction extends Action implements AppConstants {

    private String lastFilterParams;

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }
            String role = (String) session.getAttribute(ROLE);
            if (!ROLE_MANAGER.equals(role) && !ROLE_ADMIN.equals(role)) {
                return mapping.findForward(FWD_UNAUTHORIZED);
            }

            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String actionType = request.getParameter("actionType");
            String userId = request.getParameter("userId");
            String entityType = request.getParameter("entityType");
            String searchText = request.getParameter("searchText");
            String page = request.getParameter("page");

            if (CommonUtil.isEmpty(page)) {
                page = "1";
            }

            if (ROLE_MANAGER.equals(role)) {
                String currentUser = (String) session.getAttribute(USER);

                userId = currentUser;
            }

            UserManager mgr = UserManager.getInstance();
            List auditLogs = mgr.getAuditLogs(startDate, endDate, actionType,
                                               userId, entityType, searchText, page);
            String totalCount = mgr.countAuditLogs(startDate, endDate, actionType,
                                                    userId, entityType, searchText);

            lastFilterParams = startDate + "|" + endDate + "|" + actionType;

            session.setAttribute("auditLogs", auditLogs);
            session.setAttribute("auditTotalCount", totalCount);
            session.setAttribute("currentPage", page);

            session.setAttribute("filterStartDate", startDate);
            session.setAttribute("filterEndDate", endDate);
            session.setAttribute("filterActionType", actionType);
            session.setAttribute("filterUserId", userId);
            session.setAttribute("filterEntityType", entityType);
            session.setAttribute("filterSearchText", searchText);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading audit logs");
            return mapping.findForward(FWD_SUCCESS);
        }
    }
}
