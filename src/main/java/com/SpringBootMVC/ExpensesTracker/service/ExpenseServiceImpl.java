package com.SpringBootMVC.ExpensesTracker.service;

import com.SpringBootMVC.ExpensesTracker.DTO.ExpenseDTO;
import com.SpringBootMVC.ExpensesTracker.DTO.FilterDTO;
import com.SpringBootMVC.ExpensesTracker.entity.Category;
import com.SpringBootMVC.ExpensesTracker.entity.Expense;
import com.SpringBootMVC.ExpensesTracker.repository.ExpenseRepository;
import com.SpringBootMVC.ExpensesTracker.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {
    ExpenseRepository expenseRepository;
    ClientService clientService;
    CategoryService categoryService;
    CategoryRepository categoryRepository;
    EntityManager entityManager;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository, ClientService clientService
            , CategoryService categoryService, CategoryRepository categoryRepository, EntityManager entityManager) {
        this.expenseRepository = expenseRepository;
        this.clientService = clientService;
        this.categoryService = categoryService;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
    }


    @Override
    public Expense findExpenseById(int id) {
        return expenseRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void save(ExpenseDTO expenseDTO) {
        System.out.println(expenseDTO);
        Expense expense = new Expense();
        expense.setAmount(expenseDTO.getAmount());
        expense.setDateTime(expenseDTO.getDateTime());
        expense.setDescription(expenseDTO.getDescription());
        expense.setClient(clientService.findClientById(expenseDTO.getClientId()));
        Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
        if (category == null && expenseDTO.getCategory() != null) {
            category = new Category();
            category.setName(expenseDTO.getCategory());
            category = categoryRepository.save(category);
        }
        expense.setCategory(category);
        expenseRepository.save(expense);
    }

    @Override
    public void update(ExpenseDTO expenseDTO) {
        Expense existingExpense = expenseRepository.findById(expenseDTO.getExpenseId()).orElse(null);
        if (existingExpense != null) {
            existingExpense.setAmount(expenseDTO.getAmount());
            existingExpense.setDateTime(expenseDTO.getDateTime());
            existingExpense.setDescription(expenseDTO.getDescription());
            Category category = categoryService.findCategoryByName(expenseDTO.getCategory());
            if (category == null && expenseDTO.getCategory() != null) {
                category = new Category();
                category.setName(expenseDTO.getCategory());
                category = categoryRepository.save(category);
            }
            existingExpense.setCategory(category);
            expenseRepository.save(existingExpense);
        }
    }

    @Override
    public List<Expense> findAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public List<Expense> findAllExpensesByClientId(int id) {
        return expenseRepository.findByClientId(id);
    }

    @Override
    public void deleteExpenseById(int id) {
        expenseRepository.deleteById(id);
    }

    @Override
    public List<Expense> findFilterResult(FilterDTO filter, int clientId) {
        StringBuilder queryStr = new StringBuilder("select e from Expense e where e.client.id = :clientId");

        if (filter.getCategory() != null && !"all".equalsIgnoreCase(filter.getCategory())) {
            queryStr.append(" AND lower(e.category.name) = lower(:category)");
        }
        if (filter.getFrom() > 0) {
            queryStr.append(" AND e.amount >= :from");
        }
        if (filter.getTo() > 0) {
            queryStr.append(" AND e.amount <= :to");
        }
        if (filter.getYear() != null && !"all".equalsIgnoreCase(filter.getYear())) {
            queryStr.append(" AND CAST(SUBSTRING(e.dateTime, 1, 4) AS INTEGER) = :year");
        }
        if (filter.getMonth() != null && !"all".equalsIgnoreCase(filter.getMonth())) {
            queryStr.append(" AND CAST(SUBSTRING(e.dateTime, 6, 2) AS INTEGER) = :month");
        }

        TypedQuery<Expense> expenseTypedQuery = entityManager.createQuery(queryStr.toString(), Expense.class);
        expenseTypedQuery.setParameter("clientId", clientId);

        if (filter.getCategory() != null && !"all".equalsIgnoreCase(filter.getCategory())) {
            expenseTypedQuery.setParameter("category", filter.getCategory());
        }
        if (filter.getFrom() > 0) {
            expenseTypedQuery.setParameter("from", filter.getFrom());
        }
        if (filter.getTo() > 0) {
            expenseTypedQuery.setParameter("to", filter.getTo());
        }
        if (filter.getYear() != null && !"all".equalsIgnoreCase(filter.getYear())) {
            try {
                expenseTypedQuery.setParameter("year", Integer.parseInt(filter.getYear()));
            } catch (NumberFormatException ignored) {}
        }
        if (filter.getMonth() != null && !"all".equalsIgnoreCase(filter.getMonth())) {
            try {
                expenseTypedQuery.setParameter("month", Integer.parseInt(filter.getMonth()));
            } catch (NumberFormatException ignored) {}
        }

        return expenseTypedQuery.getResultList();
    }




}
