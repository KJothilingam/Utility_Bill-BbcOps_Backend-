package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.Bill;
import com.BBC_Ops.BBC_Ops.Service.BillService;
import com.BBC_Ops.BBC_Ops.Utils.BillResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bills")
public class BillController {

    @Autowired
    private BillService billService;


    @GetMapping("/all")
    public List<Bill> getAllBills() {
        return billService.getAllBills();
    }

    @PostMapping("/generate")
    public ResponseEntity<BillResponse> generateBill(@RequestBody BillRequest request) {
        try {
            System.out.println("Received Request - Meter: " + request.getMeterNumber() +
                    ", Unit Consumed: " + request.getUnitConsumed() +
                    ", Month Date: " + request.getMonthDate());

            Bill bill = billService.generateBill(request.getMeterNumber(), request.getUnitConsumed(), request.getMonthDate());

            return ResponseEntity.ok(new BillResponse(true, "Bill generated successfully", bill));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new BillResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new BillResponse(false, "Error generating bill", null));
        }
    }

    // Inner class for request payload
    public static class BillRequest {
        private String meterNumber;
        private int unitConsumed;
        private Date monthDate;

        // Getters and Setters
        public String getMeterNumber() {
            return meterNumber;
        }

        public void setMeterNumber(String meterNumber) {
            this.meterNumber = meterNumber;
        }

        public int getUnitConsumed() {
            return unitConsumed;
        }

        public void setUnitConsumed(int unitConsumed) {
            this.unitConsumed = unitConsumed;
        }

        public Date getMonthDate() {
            return monthDate;
        }

        public void setMonthDate(Date monthDate) {
            this.monthDate = monthDate;
        }
    }


        /** ✅ API to manually trigger overdue check */
        @PutMapping("/update-overdue")
        public ResponseEntity<Map<String, String>> updateOverdueBills() {
            billService.updateOverdueBills();
            return ResponseEntity.ok(Map.of("message", "Overdue bills updated successfully."));
        }

    @GetMapping("/unpaid/{meterNumber}")
    public ResponseEntity<List<Bill>> getUnpaidBills(@PathVariable String meterNumber) {
        List<Bill> unpaidBills = billService.getUnpaidBillsByMeterNumber(meterNumber);
        if (unpaidBills.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(unpaidBills);
    }
}
