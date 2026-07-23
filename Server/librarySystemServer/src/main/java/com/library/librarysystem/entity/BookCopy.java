package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("book_copy")
public class BookCopy {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;
    private String barcode;
    private Long locationId;
    private String status;  // in, borrowed, reserved, maintenance, lost, discarded
    private java.math.BigDecimal price;
    private java.time.LocalDate purchaseDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
