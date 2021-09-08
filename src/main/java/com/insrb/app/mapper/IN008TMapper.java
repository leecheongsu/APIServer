package com.insrb.app.mapper;

public interface IN008TMapper {
    void insert(
            String uuid,
            String upwd,
            String name,
            String mobile,
            String address,
            String ulevel,
            String comname,
            String businessnum,
            String gacode,
            int account_status,
            String prod_code
    );
}
