package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.ConnectionType;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Model.Wallet;
import com.BBC_Ops.BBC_Ops.Repository.CustomerRepository;
import com.BBC_Ops.BBC_Ops.Repository.WalletRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final WalletRepository walletRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, WalletRepository walletRepository) {
        this.customerRepository = customerRepository;
        this.walletRepository = walletRepository;
    }

    public List<Customer> getAllCustomers() {
        logger.info("Fetching all customers");
        return customerRepository.findAll();
    }

    public Map<String, Object> processCsv(MultipartFile file) {
        logger.info("Processing uploaded CSV file...");
        Map<String, Object> response = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<Customer> validCustomers = new ArrayList<>();
        List<Wallet> walletsToSave = new ArrayList<>();
        int validCount = 0;
        int rejectedCount = 0;

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] headers = csvReader.readNext();
            if (!isValidHeader(headers)) {
                logger.warn("Invalid CSV headers");
                errors.add("Invalid CSV headers. Expected: name, email, phoneNumber, address, unitConsumption, billDueDate, meterNumber, connectionType");
                response.put("success", false);
                response.put("validRecords", 0);
                response.put("rejectedRecords", 0);
                response.put("errors", errors);
                return response;
            }

            String[] record;
            int rowNum = 1;

            while ((record = csvReader.readNext()) != null) {
                rowNum++;
                try {
                    Customer customer = new Customer();
                    customer.setName(record[0]);
                    customer.setEmail(record[1]);
                    customer.setPhoneNumber(record[2]);
                    customer.setAddress(record[3]);
                    customer.setUnitConsumption(Integer.parseInt(record[4]));
                    customer.setBillDueDate(LocalDate.parse(record[5]));
                    customer.setMeterNumber(record[6]);
                    customer.setConnectionType(ConnectionType.valueOf(record[7].toUpperCase()));

                    if (customerRepository.existsByEmail(customer.getEmail()) ||
                            customerRepository.existsByPhoneNumber(customer.getPhoneNumber()) ||
                            customerRepository.existsByMeterNumber(customer.getMeterNumber())) {
                        logger.warn("Duplicate found at row {}: {}", rowNum, customer.getEmail());
                        errors.add("Duplicate record at row " + rowNum + ": " + customer.getEmail());
                        rejectedCount++;
                    } else {
                        validCustomers.add(customer);
                        validCount++;

                        Wallet wallet = new Wallet();
                        wallet.setCustomer(customer);
                        wallet.setCreditCardBalance(0);
                        wallet.setDebitCardBalance(0);
                        wallet.setWalletBalance(0);
                        wallet.setUpiBalance(0);
                        walletsToSave.add(wallet);
                    }

                } catch (Exception e) {
                    logger.error("Error processing row {}: {}", rowNum, e.getMessage());
                    errors.add("Skipping invalid row " + rowNum + ": " + String.join(",", record));
                    rejectedCount++;
                }
            }

            if (!validCustomers.isEmpty()) {
                customerRepository.saveAll(validCustomers);
                walletRepository.saveAll(walletsToSave);
            }

            response.put("success", errors.isEmpty());
            response.put("validRecords", validCount);
            response.put("rejectedRecords", rejectedCount);
            response.put("errors", errors);

            logger.info("CSV processing completed. Valid: {}, Rejected: {}", validCount, rejectedCount);

        } catch (IOException | CsvValidationException e) {
            logger.error("CSV file processing failed: {}", e.getMessage());
            response.put("success", false);
            response.put("validRecords", validCount);
            response.put("rejectedRecords", rejectedCount);
            response.put("errors", List.of("File processing error: " + e.getMessage()));
        }

        return response;
    }

    private boolean isValidHeader(String[] headers) {
        return headers != null && headers.length == 8 &&
                headers[0].equalsIgnoreCase("name") &&
                headers[1].equalsIgnoreCase("email") &&
                headers[2].equalsIgnoreCase("phoneNumber") &&
                headers[3].equalsIgnoreCase("address") &&
                headers[4].equalsIgnoreCase("unitConsumption") &&
                headers[5].equalsIgnoreCase("billDueDate") &&
                headers[6].equalsIgnoreCase("meterNumber") &&
                headers[7].equalsIgnoreCase("connectionType");
    }

    public Map<String, Object> deleteCustomer(Long customerId) {
        logger.info("Attempting to delete customer with ID {}", customerId);
        Map<String, Object> response = new HashMap<>();
        Optional<Customer> customerOpt = customerRepository.findById(customerId);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            Optional<Wallet> walletOpt = walletRepository.findByCustomer(customer);

            walletOpt.ifPresent(walletRepository::delete);
            customerRepository.deleteById(customerId);

            logger.info("Customer and wallet deleted successfully: {}", customer.getEmail());

            response.put("message", "Customer and associated wallet deleted successfully");
            response.put("customerId", customerId);
            response.put("customerName", customer.getName());
            response.put("status", true);
        } else {
            logger.warn("Customer not found with ID: {}", customerId);
            response.put("message", "Customer not found");
            response.put("customerId", customerId);
            response.put("status", false);
        }

        return response;
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        logger.info("Updating customer with ID: {}", id);
        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (existingCustomerOpt.isPresent()) {
            Customer existingCustomer = existingCustomerOpt.get();
            existingCustomer.setName(updatedCustomer.getName());
            existingCustomer.setEmail(updatedCustomer.getEmail());
            existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());
            existingCustomer.setAddress(updatedCustomer.getAddress());
            existingCustomer.setUnitConsumption(updatedCustomer.getUnitConsumption());
            existingCustomer.setBillDueDate(updatedCustomer.getBillDueDate());
            existingCustomer.setMeterNumber(updatedCustomer.getMeterNumber());
            existingCustomer.setConnectionType(updatedCustomer.getConnectionType());

            logger.info("Customer updated successfully: {}", existingCustomer.getEmail());
            return customerRepository.save(existingCustomer);
        } else {
            logger.warn("Customer not found for update with ID: {}", id);
        }
        return null;
    }

    public Map<String, Object> addCustomer(Customer customer) {
        logger.info("Adding new customer: {}", customer.getEmail());
        Map<String, Object> response = new HashMap<>();

        if (customerRepository.existsByEmail(customer.getEmail())) {
            logger.warn("Duplicate email found: {}", customer.getEmail());
            response.put("success", false);
            response.put("message", "Email already exists!");
            return response;
        }

        if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
            logger.warn("Duplicate phone number found: {}", customer.getPhoneNumber());
            response.put("success", false);
            response.put("message", "Phone number already exists!");
            return response;
        }

        if (customerRepository.existsByMeterNumber(customer.getMeterNumber())) {
            logger.warn("Duplicate meter number found: {}", customer.getMeterNumber());
            response.put("success", false);
            response.put("message", "Meter number already exists!");
            return response;
        }

        Customer savedCustomer = customerRepository.save(customer);
        Wallet newWallet = new Wallet(savedCustomer, 0.0, 0.0, 0.0, 0.0);
        walletRepository.save(newWallet);

        logger.info("Customer added successfully: {}", savedCustomer.getEmail());
        response.put("success", true);
        response.put("message", "Customer added successfully!");
        response.put("customer", savedCustomer);

        return response;
    }

    public Customer findByEmail(String email) {
        logger.info("Fetching customer by email: {}", email);
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> getCustomerById(Long customerId) {
        logger.info("Fetching customer by ID: {}", customerId);
        return customerRepository.findById(customerId);
    }
}
