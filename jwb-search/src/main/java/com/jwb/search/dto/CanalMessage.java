package com.jwb.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description: 阿里Canal中间件消息接收实体类
 */
@NoArgsConstructor // 无参构造器  必须!!!
@Data
public class CanalMessage<T> {
    @JsonProperty("data")
    private List<T> data;

    @JsonProperty("database")
    private String database;

    @JsonProperty("es")
    private Long es;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("isDdl")
    private Boolean isDdl;

    @JsonProperty("mysqlType")
    private Object mysqlType;

    @JsonProperty("old")
    private List<T> old;

    @JsonProperty("pkNames")
    private List<String> pkNames;

    @JsonProperty("sql")
    private String sql;

    @JsonProperty("sqlType")
    private Object sqlType;

    @JsonProperty("table")
    private String table;

    @JsonProperty("ts")
    private Long ts;

    @JsonProperty("type")
    private String type;
}


