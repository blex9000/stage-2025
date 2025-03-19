package com.sinelec.stage.service;

import com.sinelec.stage.domain.engine.model.DeviceCommand;
import com.sinelec.stage.repository.DeviceCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceCommandService {
    
    private final DeviceCommandRepository deviceCommandRepository;
    
    @Autowired
    public DeviceCommandService(DeviceCommandRepository deviceCommandRepository) {
        this.deviceCommandRepository = deviceCommandRepository;
    }
    
    public List<DeviceCommand> getAllCommands() {
        return deviceCommandRepository.findAll();
    }
    
    public Optional<DeviceCommand> getCommandById(String id) {
        return deviceCommandRepository.findById(id);
    }
    
    public List<DeviceCommand> getCommandsByDeviceId(String deviceId) {
        return deviceCommandRepository.findByDeviceId(deviceId);
    }
    
    public List<DeviceCommand> getCommandsByDatasourceId(String datasourceId) {
        return deviceCommandRepository.findByDatasourceId(datasourceId);
    }
    
    public List<DeviceCommand> getCommandsByStatus(DeviceCommand.CommandStatus status) {
        return deviceCommandRepository.findByStatus(status);
    }
    
    
    public List<DeviceCommand> getOldCommands(Date cutoffDate) {
        return deviceCommandRepository.findByCreatedAtBefore(cutoffDate);
    }
    
    public DeviceCommand createCommand(DeviceCommand command) {
        command.setCreatedAt(new Date());
        command.setStatus(DeviceCommand.CommandStatus.PENDING);
        return deviceCommandRepository.save(command);
    }
    
    public Optional<DeviceCommand> updateCommandStatus(String id, DeviceCommand.CommandStatus status) {
        return deviceCommandRepository.findById(id)
            .map(command -> {
                command.setStatus(status);
                
                if (status == DeviceCommand.CommandStatus.SENT) {
                    command.setSentAt(new Date());
                } else if (status == DeviceCommand.CommandStatus.COMPLETED ||
                           status == DeviceCommand.CommandStatus.FAILED) {
                    command.setCompletedAt(new Date());
                }
                
                return deviceCommandRepository.save(command);
            });
    }
    
    public Optional<DeviceCommand> updateCommandResult(String id, DeviceCommand.CommandStatus status, String resultMessage) {
        return deviceCommandRepository.findById(id)
            .map(command -> {
                command.setStatus(status);
                command.setResultMessage(resultMessage);
                command.setCompletedAt(new Date());
                return deviceCommandRepository.save(command);
            });
    }
} 