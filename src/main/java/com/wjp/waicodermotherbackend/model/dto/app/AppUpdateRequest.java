package com.wjp.waicodermotherbackend.model.dto.app;

import lombok.Data;

import java.io.Serializable;


/**
 * 更新应用请求参数
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
