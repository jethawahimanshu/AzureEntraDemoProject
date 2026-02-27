package com.example.b2cdemo.repository;

import com.example.b2cdemo.domain.entity.Product;
import com.example.b2cdemo.domain.enums.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByCategory(ProductCategory category, Pageable pageable);
}
