package com.library.librarysystem.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BookExportRow {
    @ExcelProperty("Title") private String title;
    @ExcelProperty("Author") private String author;
    @ExcelProperty("ISBN") private String isbn;
    @ExcelProperty("Publisher") private String publisher;
    @ExcelProperty("Category") private String category;
    @ExcelProperty("Total Copies") private Integer totalCopies;
    @ExcelProperty("Available Copies") private Integer availableCopies;
    @ExcelProperty("Borrow Count") private Integer borrowCount;
    @ExcelProperty("Price") private Double price;
    @ExcelProperty("Language") private String language;
    @ExcelProperty("Binding") private String binding;
    @ExcelProperty("Pages") private Integer pages;
}
