package com.sclera.rule_engine.syslog.config;


import org.hibernate.dialect.MySQLDialect;

public class MySQLCustomDialect extends MySQLDialect {

    @Override
    public String getTableTypeString() {
        return "ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_general_ci";
    }
}


