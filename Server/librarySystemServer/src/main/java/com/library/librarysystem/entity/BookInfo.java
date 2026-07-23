package com.library.librarysystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("book_info")
public class BookInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String isbn;
    private String title;
    private String subTitle;
    private String author;
    private String translator;
    private Long publisherId;
    private Long categoryId;
    private LocalDate publishDate;
    private Integer pages;
    private BigDecimal price;
    private String binding;
    private String language;
    private String summary;
    private String coverUrl;
    private String tableOfContents;
    private Integer totalCopies;
    private Integer availableCopies;
    private Integer borrowCount;
    private BigDecimal rating;
    private Integer ratingCount;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
