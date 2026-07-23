package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("publisher")
public class Publisher {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String shortName;
    private String isbnPrefix;
    private String phone;
    private String address;
    private String website;
    private Integer sort;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
