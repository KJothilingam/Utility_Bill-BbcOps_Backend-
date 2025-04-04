package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.Employee;
import com.BBC_Ops.BBC_Ops.Repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee findByEmail(String email) {
        logger.info("Fetching employee by email: {}", email);
        Employee employee = employeeRepository.findByEmail(email);
        if (employee == null) {
            logger.warn("No employee found with email: {}", email);
        } else {
            logger.debug("Employee found: {}", employee);
        }
        return employee;
    }

    public List<Employee> getAllEmployees() {
        logger.info("Fetching all employees...");
        List<Employee> employees = employeeRepository.findAll();
        logger.debug("Total employees fetched: {}", employees.size());
        return employees;
    }

    public Optional<Employee> getEmployeeById(Long id) {
        logger.info("Fetching employee by ID: {}", id);
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()) {
            logger.debug("Employee found: {}", employee.get());
        } else {
            logger.warn("No employee found with ID: {}", id);
        }
        return employee;
    }

    public Employee createEmployee(Employee employee) {
        logger.info("Creating new employee: {}", employee);
        Employee savedEmployee = employeeRepository.save(employee);
        logger.debug("Employee created successfully: {}", savedEmployee);
        return savedEmployee;
    }

    public void deleteEmployee(Long id) {
        logger.info("Deleting employee with ID: {}", id);
        employeeRepository.deleteById(id);
        logger.debug("Employee with ID {} deleted", id);
    }

    public Employee findById(Long id) {
        logger.info("Finding employee by ID: {}", id);
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            logger.warn("Employee not found with ID: {}", id);
        } else {
            logger.debug("Employee found: {}", employee);
        }
        return employee;
    }

    public Employee saveEmployee(Employee employee) {
        logger.info("Saving employee: {}", employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        logger.debug("Employee saved successfully: {}", updatedEmployee);
        return updatedEmployee;
    }
}
