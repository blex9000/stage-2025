package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.DeviceCommand;
import com.sinelec.stage.service.DeviceCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/commands")
@Tag(name = "Device Commands", description = "APIs for managing device commands")
public class DeviceCommandController {
    
    private final DeviceCommandService deviceCommandService;
    
    @Autowired
    public DeviceCommandController(DeviceCommandService deviceCommandService) {
        this.deviceCommandService = deviceCommandService;
    }
    
    @Operation(summary = "Get all commands", description = "Returns all device commands in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved commands",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceCommand.class)))
    })
    @GetMapping
    public ResponseEntity<List<DeviceCommand>> getAllCommands() {
        List<DeviceCommand> commands = deviceCommandService.getAllCommands();
        return ResponseEntity.ok(commands);
    }
    
    @Operation(summary = "Get command by ID", description = "Returns a device command by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved command",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceCommand.class))),
        @ApiResponse(responseCode = "404", description = "Command not found", 
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceCommand> getCommandById(
            @Parameter(description = "ID of the command to retrieve") 
            @PathVariable String id) {
        return deviceCommandService.getCommandById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get commands by device ID", 
               description = "Returns commands for a specific device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved commands")
    })
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<DeviceCommand>> getCommandsByDeviceId(
            @Parameter(description = "ID of the device") 
            @PathVariable String deviceId) {
        List<DeviceCommand> commands = deviceCommandService.getCommandsByDeviceId(deviceId);
        return ResponseEntity.ok(commands);
    }
    
    @Operation(summary = "Get commands by datasource ID", 
               description = "Returns commands for a specific datasource")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved commands")
    })
    @GetMapping("/datasource/{datasourceId}")
    public ResponseEntity<List<DeviceCommand>> getCommandsByDatasourceId(
            @Parameter(description = "ID of the datasource") 
            @PathVariable String datasourceId) {
        List<DeviceCommand> commands = deviceCommandService.getCommandsByDatasourceId(datasourceId);
        return ResponseEntity.ok(commands);
    }
    
    @Operation(summary = "Get commands by status", 
               description = "Returns commands filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved commands")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeviceCommand>> getCommandsByStatus(
            @Parameter(description = "Status of commands to filter by") 
            @PathVariable DeviceCommand.CommandStatus status) {
        List<DeviceCommand> commands = deviceCommandService.getCommandsByStatus(status);
        return ResponseEntity.ok(commands);
    }

    @Operation(summary = "Get old commands", 
               description = "Returns commands older than the specified date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved commands")
    })
    @GetMapping("/older-than")
    public ResponseEntity<List<DeviceCommand>> getOldCommands(
            @Parameter(description = "Cutoff date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date cutoffDate) {
        List<DeviceCommand> commands = deviceCommandService.getOldCommands(cutoffDate);
        return ResponseEntity.ok(commands);
    }
    
    @Operation(summary = "Create a command", 
               description = "Creates a new device command")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Command created successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceCommand.class))),
        @ApiResponse(responseCode = "400", description = "Invalid command data", 
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<DeviceCommand> createCommand(
            @Parameter(description = "Command to create") 
            @RequestBody DeviceCommand command) {
        try {
            // Generate an ID if not present (this is often a common issue)
            if (command.getId() == null || command.getId().isEmpty()) {
                command.setId(UUID.randomUUID().toString());
            }
            
            DeviceCommand createdCommand = deviceCommandService.createCommand(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCommand);
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            throw e;
        }
    }
    
    @Operation(summary = "Update command status", 
               description = "Updates the status of an existing command")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Command status updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceCommand.class))),
        @ApiResponse(responseCode = "404", description = "Command not found", 
                    content = @Content)
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<DeviceCommand> updateCommandStatus(
            @Parameter(description = "ID of the command to update") 
            @PathVariable String id, 
            @Parameter(description = "Status update data") 
            @RequestBody Map<String, DeviceCommand.CommandStatus> statusUpdate) {
        return deviceCommandService.updateCommandStatus(id, statusUpdate.get("status"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Update command result", 
               description = "Updates the result of an executed command")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Command result updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceCommand.class))),
        @ApiResponse(responseCode = "404", description = "Command not found", 
                    content = @Content)
    })
    @PutMapping("/{id}/result")
    public ResponseEntity<DeviceCommand> updateCommandResult(
            @Parameter(description = "ID of the command to update") 
            @PathVariable String id, 
            @Parameter(description = "New status for the command") 
            @RequestParam DeviceCommand.CommandStatus status,
            @Parameter(description = "Result message data") 
            @RequestBody Map<String, String> resultUpdate) {
        return deviceCommandService.updateCommandResult(id, status, resultUpdate.get("resultMessage"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 