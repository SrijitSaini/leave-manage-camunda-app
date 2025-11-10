package com.example.workflow.delegate;

import com.example.workflow.service.LeaveService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("checkLeaveBalance")
public class CheckLeaveBalanceDelegate implements JavaDelegate {

    private final LeaveService leaveService;

    public CheckLeaveBalanceDelegate(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Integer days = (Integer) execution.getVariable("leaveDays");

        boolean enoughLeaves = leaveService.hasEnoughLeaves(days);

        if (enoughLeaves) {
            // Deduct leaves and continue to Manager Approval
            leaveService.deductLeaves(days);
            execution.setVariable("approved", null); // decision pending
        } else {
            // Reject immediately
            execution.setVariable("approved", false);
        }

        execution.setVariable("enoughLeaves", enoughLeaves);
    }
}
