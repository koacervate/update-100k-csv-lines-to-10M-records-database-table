package com.blogspot.koacervate.update100Kinto10M.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class Customer implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6035051854308748012L;

    private int id;

    private String firstName;

    private String lastName;

    private String email;

    private String country;

    private boolean appInstall;
}