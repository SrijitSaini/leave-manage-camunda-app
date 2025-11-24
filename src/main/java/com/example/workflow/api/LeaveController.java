package com.example.workflow.api;

import com.example.workflow.service.LeaveService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final LeaveService leaveService;
    private static final Logger logger = LoggerFactory.getLogger(LeaveController.class);

    public LeaveController(RuntimeService runtimeService, TaskService taskService, LeaveService leaveService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> startLeave(@RequestBody StartLeaveRequest req) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("leaveDays", req.getLeaveDays());

        logger.info("Leave request for {} days received", req.getLeaveDays());

        var processInstance = runtimeService.startProcessInstanceByKey("leave_approval_process", vars);

        return ResponseEntity.ok(Map.of(
                "processInstanceId", processInstance.getId(),
                "message", "Leave request submitted"
        ));
    }

    @GetMapping("/tasks")
    public List<TaskDto> listTasks(@RequestParam(name="assignee", required = false) String assignee) {
        var query = taskService.createTaskQuery();
        if (assignee != null) query = query.taskAssignee(assignee);
        List<Task> tasks = query.list();
        List<TaskDto> dtos = new ArrayList<>();
        for (Task t : tasks) {
            dtos.add(new TaskDto(t.getId(), t.getName(), t.getAssignee(), t.getProcessInstanceId()));
        }
        return dtos;
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<String> completeTask(@PathVariable("taskId") String taskId, @RequestBody CompleteTaskRequest req) {
        boolean approved = req.isApproved();

        logger.info("Manager's decision: {}", approved);

        // Get the task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ResponseEntity.badRequest().body("Task not found");
        }

        String processInstanceId = task.getProcessInstanceId();

        // Fetch leave days
        Integer days = (Integer) runtimeService.getVariable(processInstanceId, "leaveDays");

        // ✔ If manager APPROVES → deduct leaves here (correct place)
        if (approved) {
            leaveService.deductLeaves(days);
        }

        Map<String, Object> vars = Map.of("approved", approved);
        taskService.complete(taskId, vars);
        return ResponseEntity.ok(
                approved ? "Leave approved and leaves deducted" : "Leave rejected"
        );
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance() {
        int balance = leaveService.getBalance();
        logger.info("current balance is: {}", balance);
        return ResponseEntity.ok(Map.of(
                "balance", balance
        ));
    }

    // DTOs
    public static class StartLeaveRequest {
        private int leaveDays;

        public int getLeaveDays() { return leaveDays; }
        public void setLeaveDays(int leaveDays) { this.leaveDays = leaveDays; }
    }

    public static class CompleteTaskRequest {
        private boolean approved;
        public boolean isApproved() { return approved; }
        public void setApproved(boolean approved) { this.approved = approved; }
    }

    public record TaskDto(String id, String name, String assignee, String processInstanceId) {}
}
