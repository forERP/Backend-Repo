package com.forerp.erp.auditlog;

public final class AuditLogTargetType {

    private AuditLogTargetType() {
    }

    public static final String USER = "USER";
    public static final String STORE = "STORE";
    public static final String WAREHOUSE = "WAREHOUSE";
    public static final String SUPPLIER = "SUPPLIER";
    public static final String PRODUCT = "PRODUCT";
    public static final String PRODUCT_CATEGORY = "PRODUCT_CATEGORY";
    public static final String PURCHASE_REQUEST = "PURCHASE_REQUEST";
    public static final String PURCHASE_ORDER = "PURCHASE_ORDER";
    public static final String INBOUND = "INBOUND";
    public static final String OUTBOUND = "OUTBOUND";
    public static final String RETURN = "RETURN";
    public static final String DISCARD = "DISCARD";
    public static final String INVENTORY = "INVENTORY";
}
