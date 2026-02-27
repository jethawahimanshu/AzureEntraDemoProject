package com.example.b2cdemo.service;

import com.example.b2cdemo.dto.request.ProductRequest;
import com.example.b2cdemo.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {

    Page<ProductResponse> findAll(Pageable pageable);

    ProductResponse findById(UUID id);

    ProductResponse create(ProductRequest request);

    ProductResponse update(UUID id, ProductRequest request);

    void delete(UUID id);
}
