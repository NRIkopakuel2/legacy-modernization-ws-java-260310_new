package com.example.bookstore.action;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.manager.CommonHelper;

public class PurchaseOrderAction extends DispatchAction implements AppConstants {

    private String lastViewedPoId;
    private int viewCount = 0;

    
    public ActionForward supplierList(ActionMapping mapping, ActionForm form,
                                      HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        viewCount++;
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            CommonHelper helper = CommonHelper.getInstance();
            List suppliers = helper.listSuppliers();

            session.setAttribute("suppliers", suppliers);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading suppliers");
            return mapping.findForward(FWD_SUCCESS);
        }
    }

    
    public ActionForward poList(ActionMapping mapping, ActionForm form,
                                HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        viewCount++;
        try {

            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute(USER) == null) {
                return mapping.findForward(FWD_LOGIN);
            }

            CommonHelper helper = CommonHelper.getInstance();

            String statusFilter = request.getParameter("status");
            List purchaseOrders;
            if (statusFilter != null && statusFilter.trim().length() > 0) {
                purchaseOrders = helper.listPurchaseOrdersByStatus(statusFilter);
            } else {
                purchaseOrders = helper.listPurchaseOrders();
            }

            List suppliers = helper.listSuppliers();

            session.setAttribute("purchaseOrders", purchaseOrders);
            session.setAttribute("suppliers", suppliers);

            return mapping.findForward(FWD_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute(ERR, "Error loading purchase orders");
            return mapping.findForward(FWD_SUCCESS);
        }
    }
}
