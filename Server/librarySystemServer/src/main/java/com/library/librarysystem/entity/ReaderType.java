package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("reader_type")
public class ReaderType {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;        // STUDENT, TEACHER, STAFF, EXTERNAL

    private Integer maxBorrow;

    private Integer borrowDays;

    private Integer renewCount;

    private Integer renewDays;

    private BigDecimal overdueFineRate;

    private Integer reservationKeepHours;

    private Integer canBorrow;

    private Integer status;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
