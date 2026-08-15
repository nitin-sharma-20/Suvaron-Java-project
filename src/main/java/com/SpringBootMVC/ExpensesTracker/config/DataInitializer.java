package com.SpringBootMVC.ExpensesTracker.config;

import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Role;
import com.SpringBootMVC.ExpensesTracker.repository.CategoryRepository;
import com.SpringBootMVC.ExpensesTracker.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;

    public DataInitializer(RoleRepository roleRepository, CategoryRepository categoryRepository) {
        this.roleRepository = roleRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role standardRole = new Role();
            standardRole.setName("ROLE_STANDARD");
            roleRepository.save(standardRole);
        }

        List<String> defaultCategories = Arrays.asList(
                "groceries",
                "Utilities(bills)",
                "transportation",
                "dining out",
                "entertainment",
                "shopping",
                "travel",
                "education"
        );

        for (String catName : defaultCategories) {
            if (categoryRepository.findByName(catName) == null) {
                Category cat = new Category();
                cat.setName(catName);
                categoryRepository.save(cat);
            }
        }
    }
}
