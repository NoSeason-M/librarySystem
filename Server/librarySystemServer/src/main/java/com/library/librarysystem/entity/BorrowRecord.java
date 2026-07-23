package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long readerId;
    private Long bookCopyId;
    private Long bookInfoId;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private Integer renewCount;
    private String status;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
