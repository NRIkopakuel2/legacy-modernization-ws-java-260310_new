package com.example.bookstore.manager;

import java.util.*;
import java.io.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.concurrent.*;

import com.example.bookstore.constant.AppConstants;
import com.example.bookstore.dao.BookDAO;
import com.example.bookstore.dao.PurchaseOrderDAO;
import com.example.bookstore.dao.PurchaseOrderItemDAO;
import com.example.bookstore.dao.ReceivingDAO;
import com.example.bookstore.dao.ReceivingItemDAO;
import com.example.bookstore.dao.StockTransactionDAO;
import com.example.bookstore.dao.SupplierDAO;
import com.example.bookstore.dao.impl.BookDAOImpl;
import com.example.bookstore.dao.impl.PurchaseOrderDAOImpl;
import com.example.bookstore.dao.impl.PurchaseOrderItemDAOImpl;
import com.example.bookstore.dao.impl.ReceivingDAOImpl;
import com.example.bookstore.dao.impl.ReceivingItemDAOImpl;
import com.example.bookstore.dao.impl.StockTransactionDAOImpl;
import com.example.bookstore.dao.impl.SupplierDAOImpl;
import com.example.bookstore.manager.BookstoreManager;
import com.example.bookstore.manager.UserManager;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.PurchaseOrder;
import com.example.bookstore.model.PurchaseOrderItem;
import com.example.bookstore.model.Receiving;
import com.example.bookstore.model.ReceivingItem;
import com.example.bookstore.model.StockTransaction;
import com.example.bookstore.model.Supplier;
import com.example.bookstore.util.CommonUtil;
import com.example.bookstore.util.DateUtil;

public class CommonHelper implements AppConstants {

    private static CommonHelper instance = new CommonHelper();

    private SupplierDAO supplierDAO = new SupplierDAOImpl();
    private PurchaseOrderDAO poDAO = new PurchaseOrderDAOImpl();
    private PurchaseOrderItemDAO poItemDAO = new PurchaseOrderItemDAOImpl();
    private ReceivingDAO receivingDAO = new ReceivingDAOImpl();
    private ReceivingItemDAO receivingItemDAO = new ReceivingItemDAOImpl();
    private BookDAO bookDAO = new BookDAOImpl();
    private StockTransactionDAO stockTxnDAO = new StockTransactionDAOImpl();

    private String lastPoNumber;
    private Map supplierCache = new HashMap();

    private CommonHelper() {
    }

    public static CommonHelper getInstance() {
        return instance;
    }

    
    public int createSupplier(String name, String contact, String email,
                              String phone, String addr1, String addr2,
                              String city, String state, String postalCode,
                              String country, String paymentTerms, String leadTimeDays) {
        try {
            if (CommonUtil.isEmpty(name)) {
                return STATUS_ERR;
            }

            Object existing = supplierDAO.findByName(name);
            if (existing != null) {
                return STATUS_DUPLICATE;
            }

            Supplier supplier = new Supplier();
            supplier.setNm(name);
            supplier.setContactPerson(contact);
            supplier.setEmail(email);
            supplier.setPhone(phone);
            supplier.setAddr1(addr1);
            supplier.setAddress_line2(addr2);
            supplier.setCity(city);
            supplier.setState(state);
            supplier.setPostalCode(postalCode);
            supplier.setCountry(CommonUtil.isEmpty(country) ? "USA" : country);
            supplier.setPaymentTerms(paymentTerms);
            supplier.setLeadTimeDays(CommonUtil.isEmpty(leadTimeDays) ? "14" : leadTimeDays);
            supplier.setMinOrderQty("1");
            supplier.setStatus(STS_ACTIVE);
            supplier.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            supplier.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            return supplierDAO.save(supplier);
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int updateSupplier(String id, String name, String contact, String email,
                              String phone, String addr1, String addr2,
                              String city, String state, String postalCode,
                              String country, String paymentTerms, String leadTimeDays) {
        try {
            Object existing = supplierDAO.findById(id);
            if (existing == null) {
                return STATUS_NOT_FOUND;
            }

            Supplier supplier = (Supplier) existing;
            supplier.setNm(name);
            supplier.setContactPerson(contact);
            supplier.setEmail(email);
            supplier.setPhone(phone);
            supplier.setAddr1(addr1);
            supplier.setAddress_line2(addr2);
            supplier.setCity(city);
            supplier.setState(state);
            supplier.setPostalCode(postalCode);
            supplier.setCountry(country);
            supplier.setPaymentTerms(paymentTerms);
            supplier.setLeadTimeDays(leadTimeDays);
            supplier.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            return supplierDAO.save(supplier);
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int deactivateSupplier(String id) {
        try {
            Object existing = supplierDAO.findById(id);
            if (existing == null) {
                return STATUS_NOT_FOUND;
            }

            Supplier supplier = (Supplier) existing;
            supplier.setStatus(STS_INACTIVE);
            supplier.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            return supplierDAO.save(supplier);
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    public List listSuppliers() {
        return supplierDAO.listAll();
    }

    public List listActiveSuppliers() {
        return supplierDAO.listActive();
    }

    public List searchSuppliers(String keyword) {
        return supplierDAO.searchByName(keyword);
    }

    public Object getSupplierById(String id) {

        if (supplierCache.containsKey(id)) {
            return supplierCache.get(id);
        }
        Object supplier = supplierDAO.findById(id);
        if (supplier != null) {
            supplierCache.put(id, supplier);
        }
        return supplier;
    }

    
    public String createPurchaseOrder(String supplierId, String createdBy, List items) {
        try {
            if (CommonUtil.isEmpty(supplierId) || items == null || items.size() == 0) {
                return null;
            }

            String poNumber = poDAO.generatePoNumber();
            lastPoNumber = poNumber;

            PurchaseOrder po = new PurchaseOrder();
            po.setPoNumber(poNumber);
            po.setSupplierId(supplierId);
            po.setOrderDt(CommonUtil.getCurrentDateStr());
            po.setStatus(PO_DRAFT);
            po.setCreatedBy(createdBy);
            po.setCrtDt(CommonUtil.getCurrentDateTimeStr());
            po.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            double subtotal = 0.0;
            for (int i = 0; i < items.size(); i++) {
                Map itemMap = (Map) items.get(i);
                String bookId = (String) itemMap.get("bookId");
                String qty = (String) itemMap.get("qty");
                String price = (String) itemMap.get("price");

                double itemTotal = CommonUtil.toDouble(price) * CommonUtil.toInt(qty);
                subtotal = subtotal + itemTotal;
            }

            double tax = subtotal * DEFAULT_TAX_RATE / 100.0;
            po.setSubtotal(subtotal);
            po.setTax(tax);
            po.setShippingCost(0.0);
            po.setTotal(subtotal + tax);

            int result = poDAO.save(po);
            if (result != STATUS_OK) {
                return null;
            }

            for (int i = 0; i < items.size(); i++) {
                try {
                    Map itemMap = (Map) items.get(i);
                    PurchaseOrderItem poItem = new PurchaseOrderItem();
                    poItem.setPurchaseOrderId(po.getId() != null ? po.getId().toString() : "");
                    poItem.setBookId((String) itemMap.get("bookId"));
                    poItem.setQtyOrdered((String) itemMap.get("qty"));
                    poItem.setQtyReceived("0");
                    poItem.setUnitPrice(CommonUtil.toDouble((String) itemMap.get("price")));
                    poItem.setLineSubtotal(CommonUtil.toDouble((String) itemMap.get("price"))
                                           * CommonUtil.toInt((String) itemMap.get("qty")));
                    poItem.setCrtDt(CommonUtil.getCurrentDateTimeStr());

                    poItemDAO.save(poItem);
                } catch (Exception e) {

                    e.printStackTrace();
                    System.out.println("Failed to save PO item " + i + ": " + e.getMessage());

                }
            }

            return po.getId() != null ? po.getId().toString() : poNumber;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public int submitPurchaseOrder(String poId, String userId) {
        try {
            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;
            if (!PO_DRAFT.equals(po.getStatus())) {
                return STATUS_ERR;
            }

            po.setStatus(PO_SUBMITTED);
            po.setSubmittedAt(CommonUtil.getCurrentDateTimeStr());
            po.setSubmittedBy(userId);
            po.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            return poDAO.save(po);
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    
    public int cancelPurchaseOrder(String poId, String reason, String userId) {
        try {
            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;
            if (PO_CLOSED.equals(po.getStatus()) || PO_CANCELLED.equals(po.getStatus())) {
                return STATUS_ERR;
            }

            po.setStatus(PO_CANCELLED);
            po.setCancellationReason(reason);
            po.setUpdDt(CommonUtil.getCurrentDateTimeStr());

            return poDAO.save(po);
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    public List listPurchaseOrders() {
        return poDAO.listAll();
    }

    public List listPurchaseOrdersByStatus(String status) {
        return poDAO.listByStatus(status);
    }

    public Object getPurchaseOrderById(String id) {
        return poDAO.findById(id);
    }

    public List getPurchaseOrderItems(String poId) {
        return poItemDAO.findByPurchaseOrderId(poId);
    }

    
    public int receiveShipment(String poId, String receivedBy, List items, String notes) {
        try {
            if (CommonUtil.isEmpty(poId) || items == null || items.size() == 0) {
                return STATUS_ERR;
            }

            if (items.size() > 50) {
                System.out.println("Large shipment: " + items.size());
            }

            Object poObj = poDAO.findById(poId);
            if (poObj == null) {
                return STATUS_NOT_FOUND;
            }

            PurchaseOrder po = (PurchaseOrder) poObj;
            if (!PO_SUBMITTED.equals(po.getStatus())
                && !PO_PARTIAL.equals(po.getStatus())) {
                return STATUS_ERR;
            }

            Receiving receiving = new Receiving();
            receiving.setPurchaseOrderId(poId);
            receiving.setReceivedDt(DateUtil.getCurrentDateStr());
            receiving.setReceivedBy(receivedBy);
            receiving.setNotes(notes);
            receiving.setCrtDt(CommonUtil.getCurrentDateTimeStr());

            int result = receivingDAO.save(receiving);
            if (result != STATUS_OK) {
                return STATUS_ERR;
            }

            try { Thread.sleep(500); } catch (InterruptedException e) { }

            try { BookstoreManager.getInstance().clearCache(); } catch (Exception ex) {  }
            try { UserManager.getInstance().logAction("SHIPMENT_RECEIVED", receivedBy, "PO received: " + po.getPoNumber()); } catch (Exception ex) {  }

            boolean allFullyReceived = true;
            boolean anyReceived = false;

            for (int i = 0; i < items.size(); i++) {
                try {
                    Map itemMap = (Map) items.get(i);
                    String poItemId = (String) itemMap.get("poItemId");
                    String qtyReceivedStr = (String) itemMap.get("qtyReceived");
                    int qtyReceived = CommonUtil.toInt(qtyReceivedStr);

                    if (qtyReceived <= 0) {
                        continue;
                    }

                    Object poItemObj = poItemDAO.findById(poItemId);
                    if (poItemObj != null) {
                        PurchaseOrderItem poItem = (PurchaseOrderItem) poItemObj;

                        ReceivingItem ri = new ReceivingItem();
                        ri.setReceivingId(receiving.getId() != null ? receiving.getId().toString() : "");
                        ri.setPoItemId(poItemId);
                        ri.setQtyReceived(qtyReceivedStr);
                        ri.setCrtDt(CommonUtil.getCurrentDateTimeStr());
                        receivingItemDAO.save(ri);

                        int prevReceived = CommonUtil.toInt(poItem.getQtyReceived());
                        poItem.setQtyReceived(String.valueOf(prevReceived + qtyReceived));
                        poItemDAO.save(poItem);

                        Object bookObj = bookDAO.findById(poItem.getBookId());
                        if (bookObj != null) {
                            Book book = (Book) bookObj;
                            int currentStock = CommonUtil.toInt(book.getQtyInStock());
                            int newStock = currentStock + qtyReceived;
                            book.setQtyInStock(String.valueOf(newStock));
                            bookDAO.save(book);

                            StockTransaction txn = new StockTransaction();
                            txn.setBookId(poItem.getBookId());
                            txn.setTxnType(TXN_RECEIVING);
                            txn.setQtyChange(String.valueOf(qtyReceived));
                            txn.setQtyAfter(String.valueOf(newStock));
                            txn.setUserId(receivedBy);
                            txn.setReason("PO Receiving: " + po.getPoNumber());
                            txn.setRefType("ORDER");
                            txn.setRefId(poId);
                            txn.setCrtDt(CommonUtil.getCurrentDateTimeStr());
                            stockTxnDAO.save(txn);
                        }

                        anyReceived = true;

                        int totalOrdered = CommonUtil.toInt(poItem.getQtyOrdered());
                        int totalReceived = prevReceived + qtyReceived;
                        if (totalReceived < totalOrdered) {
                            allFullyReceived = false;
                        }
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                    System.out.println("Failed to process receiving item " + i);
                    allFullyReceived = false;
                }
            }

            if (anyReceived) {
                if (allFullyReceived) {
                    po.setStatus(PO_RECEIVED);
                } else {
                    po.setStatus(PO_PARTIAL);
                }
                po.setUpdDt(CommonUtil.getCurrentDateTimeStr());
                poDAO.save(po);
            }

            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();

            return STATUS_ERR;
        }
    }

    
    public List getReceivingsByPo(String poId) {
        return receivingDAO.findByPurchaseOrderId(poId);
    }

    
    public List listReceivings(String page) {
        return receivingDAO.listReceivings(page);
    }

    
    public String countReceivings() {
        return receivingDAO.countReceivings();
    }

    
    public int processOrder(String supplierId, String createdBy, List items,
                            String autoSubmit, String notes) {
        try {

            if (CommonUtil.isEmpty(supplierId)) {
                System.out.println("processOrder: supplier ID is empty");
                return STATUS_ERR;
            }

            Object supplierObj = supplierDAO.findById(supplierId);
            if (supplierObj == null) {
                System.out.println("processOrder: supplier not found: " + supplierId);
                return STATUS_NOT_FOUND;
            }

            Supplier supplier = (Supplier) supplierObj;
            if (!STS_ACTIVE.equals(supplier.getStatus())) {
                System.out.println("processOrder: supplier is inactive");
                return STATUS_ERR;
            }

            if (items == null || items.size() == 0) {
                System.out.println("processOrder: no items");
                return STATUS_ERR;
            }

            for (int i = 0; i < items.size(); i++) {
                try {
                    Map itemMap = (Map) items.get(i);
                    String bookId = (String) itemMap.get("bookId");
                    String qty = (String) itemMap.get("qty");
                    String price = (String) itemMap.get("price");

                    if (CommonUtil.isEmpty(bookId)) {
                        System.out.println("processOrder: item " + i + " has no bookId");
                        return STATUS_ERR;
                    }

                    if (CommonUtil.toInt(qty) <= 0) {
                        System.out.println("processOrder: item " + i + " has invalid qty");
                        return STATUS_ERR;
                    }

                    int minQty = CommonUtil.toInt(supplier.getMinOrderQty());
                    if (CommonUtil.toInt(qty) < minQty) {
                        System.out.println("processOrder: item " + i + " below min qty " + minQty);

                    }

                    Object bookObj = bookDAO.findById(bookId);
                    if (bookObj == null) {
                        System.out.println("processOrder: book not found: " + bookId);
                        return STATUS_ERR;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return STATUS_ERR;
                }
            }

            String poId = createPurchaseOrder(supplierId, createdBy, items);
            if (poId == null) {
                System.out.println("processOrder: failed to create PO");
                return STATUS_ERR;
            }

            if (CommonUtil.isNotEmpty(notes)) {
                try {
                    Object poObj = poDAO.findById(poId);
                    if (poObj != null) {
                        PurchaseOrder po = (PurchaseOrder) poObj;
                        po.setNotes(notes);
                        poDAO.save(po);
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            if (FLG_ON.equals(autoSubmit)) {

                try { BookstoreManager.getInstance().clearCache(); } catch (Exception ex) {  }
                try { UserManager.getInstance().logAction("PO_SUBMITTED", createdBy, "PO auto-submitted"); } catch (Exception ex) {  }

                int submitResult = submitPurchaseOrder(poId, createdBy);
                if (submitResult != STATUS_OK) {
                    System.out.println("processOrder: PO created but submit failed");

                    return STATUS_WARN;
                }
            }

            System.out.println("processOrder: completed successfully, PO=" + poId);
            return STATUS_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return STATUS_ERR;
        }
    }

    public int archiveOldPOs(String beforeDate) { return 0; }

    public int recalculatePOTotals() { return STATUS_OK; }

    public List validateAllSuppliers() { return new ArrayList(); }
}
