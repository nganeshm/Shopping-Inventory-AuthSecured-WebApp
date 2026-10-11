package com.app.product.service;

import com.app.product.dto.ProductImportResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImportService {

    ProductImportResponse importCsv(MultipartFile file);
}
