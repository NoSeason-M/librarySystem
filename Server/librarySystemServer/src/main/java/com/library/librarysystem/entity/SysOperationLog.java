package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private String operation;
    private String module;
    private String requestIp;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    private String result;
    private Long duration;
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
