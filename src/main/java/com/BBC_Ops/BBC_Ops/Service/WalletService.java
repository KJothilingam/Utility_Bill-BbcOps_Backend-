package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.Wallet;
import com.BBC_Ops.BBC_Ops.Repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private WalletRepository walletRepository;

    public Wallet findByCustomerId(Long customerId) {
        logger.info("Fetching wallet for customer ID: {}", customerId);
        Wallet wallet = walletRepository.findByCustomer_CustomerId(customerId);

        if (wallet == null) {
            logger.warn("No wallet found for customer ID: {}", customerId);
        } else {
            logger.info("Wallet found for customer ID: {}", customerId);
        }

        return wallet;
    }

    public boolean addMoneyToWallet(Long customerId, double amount, String paymentMethod) {
        logger.info("Adding amount ₹{} to {} for customer ID: {}", amount, paymentMethod, customerId);
        Wallet wallet = walletRepository.findByCustomer_CustomerId(customerId);

        if (wallet != null) {
            switch (paymentMethod.toLowerCase()) {
                case "creditcard":
                    wallet.setCreditCardBalance(wallet.getCreditCardBalance() + amount);
                    logger.info("Amount added to Credit Card. New balance: ₹{}", wallet.getCreditCardBalance());
                    break;
                case "debitcard":
                    wallet.setDebitCardBalance(wallet.getDebitCardBalance() + amount);
                    logger.info("Amount added to Debit Card. New balance: ₹{}", wallet.getDebitCardBalance());
                    break;
                case "upi":
                    wallet.setUpiBalance(wallet.getUpiBalance() + amount);
                    logger.info("Amount added to UPI. New balance: ₹{}", wallet.getUpiBalance());
                    break;
                case "wallet":
                    wallet.setWalletBalance(wallet.getWalletBalance() + amount);
                    logger.info("Amount added to Wallet. New balance: ₹{}", wallet.getWalletBalance());
                    break;
                default:
                    logger.error("Invalid payment method: {}", paymentMethod);
                    return false;
            }
            walletRepository.save(wallet);
            logger.info("Wallet updated successfully for customer ID: {}", customerId);
            return true;
        }

        logger.warn("Wallet not found for customer ID: {}", customerId);
        return false;
    }
}
