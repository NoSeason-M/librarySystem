package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fine_record")
public class FineRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long readerId;
    private Long borrowRecordId;
    private String fineType;       // overdue, damage, lost
    private BigDecimal amount;
    private Integer paid;
    private LocalDateTime paidDate;
    private Long operatorId;
    private Integer waive;
    private String waiveReason;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
