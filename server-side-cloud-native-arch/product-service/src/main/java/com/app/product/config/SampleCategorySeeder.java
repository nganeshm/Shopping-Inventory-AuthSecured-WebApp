package com.app.product.config;

import com.app.product.entity.Category;
import com.app.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The sample product CSV refers to these category names. Insert any that
 * are not already in the categories table so the import can resolve them.
 */
@Component
@RequiredArgsConstructor
public class SampleCategorySeeder implements ApplicationRunner {

    static final List<String> CATEGORY_NAMES = List.of(
            "Electronics", "Laptops", "Accessories", "Audio", "Storage");

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String name : CATEGORY_NAMES) {
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(Category.builder()
                        .name(name)
                        .description(name)
                        .build());
            }
        }
    }
}
