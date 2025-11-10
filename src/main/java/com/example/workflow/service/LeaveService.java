package com.example.workflow.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LeaveService {

    // In-memory store of user leave balances
    private int leaveBalance = 5;


    public boolean hasEnoughLeaves(int requested) {
        return leaveBalance >= requested;
    }

    public void deductLeaves(int days) {
        leaveBalance =  leaveBalance-days;
    }

    public int getBalance() {
        return leaveBalance;
    }
}
