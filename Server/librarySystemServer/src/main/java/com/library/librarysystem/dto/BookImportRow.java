package com.library.librarysystem.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class BookImportRow {
    @ExcelProperty("ISBN") private String isbn;
    @ExcelProperty("Title") private String title;
    @ExcelProperty("Author") private String author;
    @ExcelProperty("Publisher") private String publisher;
    @ExcelProperty("Category") private String category;
    @ExcelProperty("Publish Date") private String publishDate;
    @ExcelProperty("Price") private String price;
    @ExcelProperty("Barcode") private String barcode;
    @ExcelProperty("Location") private String location;
    @ExcelProperty("Copies") private String copies;
    @ExcelProperty("Language") private String language;
    @ExcelProperty("Binding") private String binding;
    @ExcelProperty("Pages") private String pages;
    @ExcelProperty("Summary") private String summary;
}
