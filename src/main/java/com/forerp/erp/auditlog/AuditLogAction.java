package com.forerp.erp.auditlog;

public final class AuditLogAction {

    private AuditLogAction() {
    }

    public static final String ADMIN_LOGIN = "ADMIN_LOGIN";
    public static final String ADMIN_LOGOUT = "ADMIN_LOGOUT";

    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";

    public static final String STORE_CREATE = "STORE_CREATE";
    public static final String STORE_UPDATE = "STORE_UPDATE";

    public static final String WAREHOUSE_CREATE = "WAREHOUSE_CREATE";
    public static final String WAREHOUSE_UPDATE = "WAREHOUSE_UPDATE";

    public static final String SUPPLIER_CREATE = "SUPPLIER_CREATE";
    public static final String SUPPLIER_UPDATE = "SUPPLIER_UPDATE";

    public static final String PRODUCT_CREATE = "PRODUCT_CREATE";
    public static final String PRODUCT_UPDATE = "PRODUCT_UPDATE";
    public static final String PRODUCT_BUNDLE_CREATE = "PRODUCT_BUNDLE_CREATE";
    public static final String PRODUCT_CATEGORY_CREATE = "PRODUCT_CATEGORY_CREATE";
    public static final String PRODUCT_CATEGORY_UPDATE = "PRODUCT_CATEGORY_UPDATE";

    public static final String PURCHASE_REQUEST_CREATE = "PURCHASE_REQUEST_CREATE";
    public static final String PURCHASE_REQUEST_APPROVE = "PURCHASE_REQUEST_APPROVE";
    public static final String PURCHASE_REQUEST_REJECT = "PURCHASE_REQUEST_REJECT";
    public static final String PURCHASE_ORDER_DRAFT_CREATE = "PURCHASE_ORDER_DRAFT_CREATE";
    public static final String PURCHASE_ORDER_CONFIRM = "PURCHASE_ORDER_CONFIRM";
    public static final String PURCHASE_ORDER_CANCEL = "PURCHASE_ORDER_CANCEL";

    public static final String INBOUND_CONFIRM = "INBOUND_CONFIRM";
    public static final String INBOUND_CANCEL = "INBOUND_CANCEL";
    public static final String OUTBOUND_CONFIRM = "OUTBOUND_CONFIRM";
    public static final String OUTBOUND_CANCEL = "OUTBOUND_CANCEL";

    public static final String RETURN_CONFIRM = "RETURN_CONFIRM";
    public static final String DISCARD_CONFIRM = "DISCARD_CONFIRM";
    public static final String DISCARD_CANCEL = "DISCARD_CANCEL";

    public static final String INVENTORY_ADJUST = "INVENTORY_ADJUST";
}
