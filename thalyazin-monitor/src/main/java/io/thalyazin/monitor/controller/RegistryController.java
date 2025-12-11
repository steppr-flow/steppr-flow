package io.thalyazin.monitor.controller;

import io.thalyazin.core.model.WorkflowRegistrationRequest;
import io.thalyazin.monitor.model.RegisteredWorkflow;
import io.thalyazin.monitor.service.WorkflowRegistryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for workflow registration from microservices.
 */
@RestController
@RequestMapping("/api/registry")
@RequiredArgsConstructor
@Tag(name = "Registry", description = "Workflow registration from microservices")
public class RegistryController {

    private final WorkflowRegistryService registryService;

    @Operation(summary = "Register workflows", description = "Register workflow definitions from a microservice")
    @ApiResponse(responseCode = "200", description = "Workflows registered successfully")
    @PostMapping("/workflows")
    public ResponseEntity<Void> registerWorkflows(@RequestBody WorkflowRegistrationRequest request) {
        registryService.registerWorkflows(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all registered workflows", description = "Get all workflow definitions registered by microservices")
    @ApiResponse(responseCode = "200", description = "List of registered workflows")
    @GetMapping("/workflows")
    public ResponseEntity<List<RegisteredWorkflow>> getAllWorkflows() {
        return ResponseEntity.ok(registryService.getAllWorkflows());
    }

    @Operation(summary = "Get workflow by topic", description = "Get a specific workflow definition by topic")
    @ApiResponse(responseCode = "200", description = "Workflow found")
    @ApiResponse(responseCode = "404", description = "Workflow not found")
    @GetMapping("/workflows/{topic}")
    public ResponseEntity<RegisteredWorkflow> getWorkflow(@PathVariable String topic) {
        RegisteredWorkflow workflow = registryService.getWorkflow(topic);
        if (workflow == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(workflow);
    }

    @Operation(summary = "Unregister service", description = "Unregister a service instance (called on shutdown)")
    @ApiResponse(responseCode = "200", description = "Service unregistered")
    @DeleteMapping("/services/{serviceName}/instances/{instanceId}")
    public ResponseEntity<Void> unregisterService(
            @PathVariable String serviceName,
            @PathVariable String instanceId) {
        registryService.unregisterService(serviceName, instanceId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Service heartbeat", description = "Update heartbeat for a service instance")
    @ApiResponse(responseCode = "200", description = "Heartbeat received")
    @PostMapping("/services/{serviceName}/instances/{instanceId}/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @PathVariable String serviceName,
            @PathVariable String instanceId) {
        registryService.heartbeat(serviceName, instanceId);
        return ResponseEntity.ok().build();
    }
}
