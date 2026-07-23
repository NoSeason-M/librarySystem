package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reader")
public class Reader {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String readerNo;

    private Long readerTypeId;

    private Integer cardStatus;      // 0=lost, 1=normal, 2=frozen

    private Integer totalBorrowed;

    private Integer currentBorrowed;

    private BigDecimal totalFines;

    private LocalDate registerDate;

    private LocalDate expireDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
