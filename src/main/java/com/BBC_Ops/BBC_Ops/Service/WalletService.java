package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.Wallet;
import com.BBC_Ops.BBC_Ops.Repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public Wallet findByCustomerId(Long customerId) {
        return walletRepository.findByCustomer_CustomerId(customerId);
    }

    public boolean addMoneyToWallet(Long customerId, double amount, String paymentMethod) {
        Wallet wallet = walletRepository.findByCustomer_CustomerId(customerId);

        if (wallet != null) {
            switch (paymentMethod) {
                case "creditCard":
                    wallet.setCreditCardBalance(wallet.getCreditCardBalance() + amount);
                    break;
                case "debitCard":
                    wallet.setDebitCardBalance(wallet.getDebitCardBalance() + amount);
                    break;
                case "upi":
                    wallet.setUpiBalance(wallet.getUpiBalance() + amount);
                    break;
                case "wallet":
                    wallet.setWalletBalance(wallet.getWalletBalance() + amount);
                    break;
                default:
                    return false;
            }
            walletRepository.save(wallet);
            return true;
        }
        return false;
    }
}
