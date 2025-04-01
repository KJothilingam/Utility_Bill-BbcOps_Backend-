package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.ConnectionType;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Model.Wallet;
import com.BBC_Ops.BBC_Ops.Repository.CustomerRepository;
import com.BBC_Ops.BBC_Ops.Repository.WalletRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final WalletRepository walletRepository;  // Add this line

    @Autowired
    public CustomerService(CustomerRepository customerRepository, WalletRepository walletRepository) {
        this.customerRepository = customerRepository;
        this.walletRepository = walletRepository;  // Inject the WalletRepository
    }


    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Map<String, Object> processCsv(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<Customer> validCustomers = new ArrayList<>();

        int validCount = 0;
        int rejectedCount = 0;

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            String[] headers = csvReader.readNext();
            if (!isValidHeader(headers)) {
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
                        errors.add("Duplicate record at row " + rowNum + ": " + customer.getEmail());
                        rejectedCount++;
                    } else {
                        validCustomers.add(customer);
                        validCount++;
                    }

                } catch (Exception e) {
                    errors.add("Skipping invalid row " + rowNum + ": " + String.join(",", record));
                    rejectedCount++;
                }
            }

            if (!validCustomers.isEmpty()) {
                customerRepository.saveAll(validCustomers);
            }

            response.put("success", errors.isEmpty());
            response.put("validRecords", validCount);
            response.put("rejectedRecords", rejectedCount);
            response.put("errors", errors);

        } catch (IOException | CsvValidationException e) {
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
        Map<String, Object> response = new HashMap<>();

        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // Fetch the Wallet linked to the Customer using the updated method
            Optional<Wallet> walletOpt = walletRepository.findByCustomer(customer); // Updated query

            if (walletOpt.isPresent()) {
                // Delete the Wallet
                walletRepository.delete(walletOpt.get());
            }

            // Now delete the Customer
            customerRepository.deleteById(customerId);

            response.put("message", "Customer and associated wallet deleted successfully");
            response.put("customerId", customerId);
            response.put("customerName", customer.getName());
            response.put("status", true);
        } else {
            response.put("message", "Customer not found");
            response.put("customerId", customerId);
            response.put("status", false);
        }

        return response;
    }


//    public Map<String, Object> deleteCustomer(Long customerId) {
//        Map<String, Object> response = new HashMap<>();
//
//        Optional<Customer> customerOpt = customerRepository.findById(customerId);
//        if (customerOpt.isPresent()) {
//            Customer customer = customerOpt.get();
//            customerRepository.deleteById(customerId);
//
//            response.put("message", "Customer deleted successfully");
//            response.put("customerId", customerId);
//            response.put("customerName", customer.getName());
//            response.put("status", true);
//        } else {
//            response.put("message", "Customer not found");
//            response.put("customerId", customerId);
//            response.put("status", false);
//        }
//
//        return response;
//    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Optional<Customer> existingCustomerOpt = customerRepository.findById(id);
        if (existingCustomerOpt.isPresent()) {
            Customer existingCustomer = existingCustomerOpt.get();

            // Updating fields
            existingCustomer.setName(updatedCustomer.getName());
            existingCustomer.setEmail(updatedCustomer.getEmail());
            existingCustomer.setPhoneNumber(updatedCustomer.getPhoneNumber());
            existingCustomer.setAddress(updatedCustomer.getAddress());
            existingCustomer.setUnitConsumption(updatedCustomer.getUnitConsumption());
            existingCustomer.setBillDueDate(updatedCustomer.getBillDueDate());
            existingCustomer.setMeterNumber(updatedCustomer.getMeterNumber());
            existingCustomer.setConnectionType(updatedCustomer.getConnectionType());

            // Save and return the updated customer
            return customerRepository.save(existingCustomer);
        }
        return null;
    }

//    public Map<String, Object> addCustomer(Customer customer) {
//        Map<String, Object> response = new HashMap<>();
//
//        if (customerRepository.existsByEmail(customer.getEmail())) {
//            response.put("success", false);
//            response.put("message", "Email already exists!");
//            return response;
//        }
//
//        if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
//            response.put("success", false);
//            response.put("message", "Phone number already exists!");
//            return response;
//        }
//
//        if (customerRepository.existsByMeterNumber(customer.getMeterNumber())) {
//            response.put("success", false);
//            response.put("message", "Meter number already exists!");
//            return response;
//        }
//
//        Customer savedCustomer = customerRepository.save(customer);
//        response.put("success", true);
//        response.put("message", "Customer added successfully!");
//        response.put("customer", savedCustomer);
//
//        return response;
//    }

    public Map<String, Object> addCustomer(Customer customer) {
        Map<String, Object> response = new HashMap<>();

        // Check if the email already exists
        if (customerRepository.existsByEmail(customer.getEmail())) {
            response.put("success", false);
            response.put("message", "Email already exists!");
            return response;
        }

        // Check if the phone number already exists
        if (customerRepository.existsByPhoneNumber(customer.getPhoneNumber())) {
            response.put("success", false);
            response.put("message", "Phone number already exists!");
            return response;
        }

        // Check if the meter number already exists
        if (customerRepository.existsByMeterNumber(customer.getMeterNumber())) {
            response.put("success", false);
            response.put("message", "Meter number already exists!");
            return response;
        }

        // Save the new customer
        Customer savedCustomer = customerRepository.save(customer);

        // Create a new wallet with zero balances for the new customer
        Wallet newWallet = new Wallet(savedCustomer, 0.0, 0.0, 0.0, 0.0);
        walletRepository.save(newWallet);  // Save the wallet into the wallet repository

        response.put("success", true);
        response.put("message", "Customer added successfully!");
        response.put("customer", savedCustomer);

        return response;
    }


    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    // Fetch customer by ID
    public Optional<Customer> getCustomerById(Long customerId) {
        return customerRepository.findById(customerId);
    }

}
