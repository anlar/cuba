alter table SYS_CATEGORY_ATTR add DEFAULT_DECIMAL numeric(36,10);
alter table SYS_CATEGORY_ATTR add MIN_DECIMAL numeric(36,10);
alter table SYS_CATEGORY_ATTR add MAX_DECIMAL numeric(36,10);

alter table SYS_ATTR_VALUE add DECIMAL_VALUE numeric(36,10);