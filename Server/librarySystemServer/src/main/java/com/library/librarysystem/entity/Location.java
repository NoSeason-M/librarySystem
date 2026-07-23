package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("location")
public class Location {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private String description;
    private String floor;
    private Integer enabled;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
