package com.blogspot.koacervate.update100Kinto10M.mapper;

import com.blogspot.koacervate.update100Kinto10M.domain.Customer;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper {

    @Select("SELECT COUNT(ID) FROM CUSTOMER")
    int count();

    static final String SQL_INSERT_CUSTOMER_TEMP = "INSERT INTO CUSTOMER_TEMP (ID, FIRST_NAME, LAST_NAME, EMAIL, COUNTRY, APP_INSTALL) VALUES(#{id}, #{firstName}, #{lastName}, #{email}, #{country}, #{appInstall})";
    @Insert(SQL_INSERT_CUSTOMER_TEMP)
    int insertTemp(Customer c);

    @Select("TRUNCATE TABLE CUSTOMER_TEMP")
    void truncateTemp();

}