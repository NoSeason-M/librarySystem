package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long readerId;
    private Long bookInfoId;
    private Long bookCopyId;
    private LocalDateTime reserveDate;
    private LocalDateTime expireDate;
    private String status;          // waiting, ready, fulfilled, cancelled, expired
    private Long pickLocationId;
    private Long operatorId;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
