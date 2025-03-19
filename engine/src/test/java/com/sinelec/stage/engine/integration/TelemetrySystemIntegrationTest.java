package com.sinelec.stage.engine.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinelec.stage.domain.engine.model.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;
import com.sinelec.stage.engine.EngineApplication;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {EngineApplication.class}
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class TelemetrySystemIntegrationTest {

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");

    @LocalServerPort
    private int port;

    @Autowired
    private TestDataUtil testDataUtil;

    private TestRestTemplate restTemplate = new TestRestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    private String baseUrl;
    private static String deviceDefinitionId;
    private static String datasourceId;
    private static String deviceId;
    private static String commandId;
    private static String deviceStateId;

    @BeforeAll
    static void setUpMongo() {
        mongoDBContainer.start();
    }

    @BeforeEach
    public void setUpPprops() {
        baseUrl = "http://localhost:" + port + "/api";
        
        // Set MongoDB connection properties
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
        System.setProperty("spring.data.mongodb.database", "test");
    }

    @Test
    @Order(0)  // Run this before other tests
    public void testInitializeTestData() {
        // Create necessary driver definitions
        DriverDefinition modbusDriver = testDataUtil.createModbusDriverDefinition();
        assertNotNull(modbusDriver);
        assertNotNull(modbusDriver.getId());
        assertEquals("MODBUS_DRIVER", modbusDriver.getId());
    }

    @Test
    @Order(1)
    public void testCreateDeviceDefinition() {
        // Create device definition for CO sensor
        DeviceDefinition deviceDefinition = new DeviceDefinition();
        deviceDefinition.setName("CO Sensor Definition");
        deviceDefinition.setDescription("Definition for CO sensors");
        
        // Create signal definitions
        List<SignalDefinition> signals = new ArrayList<>();
        
        // CO Value signal
        SignalDefinition coValueSignal = SignalDefinition.builder()
            .id("16fe282e-ea9b-4e2b-9176-9de4789cea8b")
            .name("MIS-VALUE")
            .description("CO concentration measurement")
            .type(DataType.FLOAT)
            .unit("ppm")
            .alarmsEnabled(true)
            .required(true)
            .build();
        
        // Create alarm condition
        AlarmCondition highCoAlarm = AlarmCondition.builder()
                .id(UUID.randomUUID().toString())
                .name("High CO")
                .code("HIGH_CO")
                .description("CO level is high")
                .expression(new Expression("numericValue > 50", "CO level exceeds 50ppm"))
                .severity(AlarmSeverity.MAJOR)
                .build();
                
        coValueSignal.setAlarmConditions(List.of(highCoAlarm));
        signals.add(coValueSignal);
        
        // Maintenance alarm signal
        SignalDefinition maintenanceSignal = SignalDefinition.builder()
            .id("75dfa827-4066-480d-a9b5-e8c3115a5c47")
            .name("ALR-MANUT")
            .description("Maintenance alarm")
            .type(DataType.BOOLEAN)
            .required(true)
            .build();
            
        signals.add(maintenanceSignal);
        
        // Valid data signal
        SignalDefinition validDataSignal = SignalDefinition.builder()
            .id("34d9d760-ccff-4866-9c15-7b44ea849303")
            .name("ST-DATO-VALIDO")
            .description("Data validity status")
            .type(DataType.BOOLEAN)
            .required(true)
            .build();
            
        signals.add(validDataSignal);
        
        deviceDefinition.setSignals(signals);
        
        // POST request to create device definition
        ResponseEntity<DeviceDefinition> response = restTemplate.postForEntity(
                baseUrl + "/device-definitions", 
                deviceDefinition, 
                DeviceDefinition.class);
                
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        DeviceDefinition createdDefinition = response.getBody();
        assertNotNull(createdDefinition);
        assertNotNull(createdDefinition.getId());
        deviceDefinitionId = createdDefinition.getId();
    }
    
    @Test
    @Order(2)
    public void testCreateDatasource() {
        // Create ModBus datasource
        Datasource datasource = new Datasource();
        datasource.setName("PLC");
        datasource.setDescription("ModBus datasource");
        datasource.setDriverId("MODBUS_DRIVER");
        datasource.setActive(true);
        
        // Add configuration properties
        List<Property> properties = new ArrayList<>();
        
        // Use the builder pattern or constructor with 3 parameters
        properties.add(Property.builder().name("HOST").value("127.0.0.1").build());
        properties.add(Property.builder().name("PORT").value("1502").build());
        properties.add(Property.builder().name("KEEPALIVE").value("true").build());
        properties.add(Property.builder().name("CONNECTION_TIMEOUT").value("1000").build());
        
        datasource.setConfiguration(properties);
        
        // POST request to create datasource
        ResponseEntity<Datasource> response = restTemplate.postForEntity(
                baseUrl + "/datasources", 
                datasource, 
                Datasource.class);
                
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response);
        assertNotNull(response.getBody());
        
        Datasource createdDatasource = response.getBody();
        assertNotNull(createdDatasource);
        assertNotNull(createdDatasource.getId());
        datasourceId = createdDatasource.getId();
    }
    
    @Test
    @Order(3)
    public void testCreateDevice() {
        // Create device using the definition and datasource
        Device device = new Device();
        device.setName("CO-1");
        device.setDescription("CO Sensor #4");
        device.setLocation("Tunnel Section 4");
        device.setActive(true);
        device.setDeviceDefinitionId(deviceDefinitionId);
        device.setDatasourceId(datasourceId);
        
        // Configure signals
        List<SignalConfiguration> signalConfigs = new ArrayList<>();
        
        // CO Value signal config
        SignalConfiguration coValueConfig = new SignalConfiguration();
        coValueConfig.setSignalId("16fe282e-ea9b-4e2b-9176-9de4789cea8b");
        
        List<Property> coValueProps = new ArrayList<>();
        coValueProps.add(new Property("registerType", "HOLDING_REGISTER"));
        coValueProps.add(new Property("register", "2"));
        coValueProps.add(new Property("dataType", "TWO_BYTE_INT_SIGNED"));
        coValueConfig.setSignalProperties(coValueProps);
        signalConfigs.add(coValueConfig);
        
        // Maintenance alarm config
        SignalConfiguration maintenanceConfig = new SignalConfiguration();
        maintenanceConfig.setSignalId("75dfa827-4066-480d-a9b5-e8c3115a5c47");
        
        List<Property> maintenanceProps = new ArrayList<>();
        maintenanceProps.add(new Property("registerType", "HOLDING_REGISTER"));
        maintenanceProps.add(new Property("register", "1"));
        maintenanceProps.add(new Property("bitIndex", "11"));
        maintenanceConfig.setSignalProperties(maintenanceProps);
        signalConfigs.add(maintenanceConfig);
        
        // Valid data config
        SignalConfiguration validDataConfig = new SignalConfiguration();
        validDataConfig.setSignalId("34d9d760-ccff-4866-9c15-7b44ea849303");
        
        List<Property> validDataProps = new ArrayList<>();
        validDataProps.add(new Property("registerType", "HOLDING_REGISTER"));
        validDataProps.add(new Property("register", "1"));
        validDataProps.add(new Property("bitIndex", "10"));
        validDataConfig.setSignalProperties(validDataProps);
        signalConfigs.add(validDataConfig);
        
        device.setSignalConfigurations(signalConfigs);
        
        // POST request to create device
        ResponseEntity<Device> response = restTemplate.postForEntity(
                baseUrl + "/devices", 
                device, 
                Device.class);
                
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response);
        assertNotNull(response.getBody());
        
        Device createdDevice = response.getBody();
        assertNotNull(createdDevice);
        assertNotNull(createdDevice.getId());
        deviceId = createdDevice.getId();
    }
    
    @Test
    @Order(4)
    public void testCreateDeviceState() {
        // Create device state
        DeviceState deviceState = new DeviceState();
        deviceState.setDeviceId(deviceId);  // Make sure this is set correctly
        deviceState.setHealthStatus(HealthStatus.HEALTHY);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<DeviceState> requestEntity = new HttpEntity<>(deviceState, headers);
        
        ResponseEntity<DeviceState> response = restTemplate.exchange(
                baseUrl + "/device-states",
                HttpMethod.POST,
                requestEntity,
                DeviceState.class);
                
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        deviceStateId = response.getBody().getId();
        assertNotNull(deviceStateId);
    }

    @Test
    @Order(5)
    public void testCreateReadings() {
        // Create readings
        List<Reading> readings = new ArrayList<>();
        
        // CO Level reading
        Reading coReading = new Reading();
        coReading.setDeviceId(deviceId);
        coReading.setSignalId("16fe282e-ea9b-4e2b-9176-9de4789cea8b");  // Match exact signal ID
        coReading.setValue(5.3);  // Ensure this is a valid value type for the signal
        coReading.setNumericValue(5.3);  // Add numeric value explicitly for numeric types
        coReading.setTimestamp(new Date());
        readings.add(coReading);
        
        // Temperature reading
        Reading tempReading = new Reading();
        tempReading.setDeviceId(deviceId);
        tempReading.setSignalId("27de393f-fc0c-4f2a-a388-0fd72c78764a");  // Match exact signal ID
        tempReading.setValue(22.7);  // Ensure this is a valid value type for the signal
        tempReading.setNumericValue(22.7);  // Add numeric value explicitly for numeric types
        tempReading.setTimestamp(new Date());
        readings.add(tempReading);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<List<Reading>> requestEntity = new HttpEntity<>(readings, headers);
        
        // Ensure the URL matches exactly with your controller mapping
        ResponseEntity<List<Reading>> response = restTemplate.exchange(
                baseUrl + "/readings",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<List<Reading>>() {});
            
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }
    
    @Test
    @Order(6)
    public void testCreateDeviceCommand() {
        // Create command to write a value with explicitly set ID
        DeviceCommand command = DeviceCommand.builder()
            .id(UUID.randomUUID().toString())
            .deviceId(deviceId)
            .datasourceId(datasourceId)
            .writeValues(List.of(new DeviceCommand.WriteValue("16fe282e-ea9b-4e2b-9176-9de4789cea8b", "30.0")))
            .build();
        
        // POST request to create command
        ResponseEntity<DeviceCommand> response = restTemplate.postForEntity(
                baseUrl + "/commands", 
                command, 
                DeviceCommand.class);
                
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        DeviceCommand createdCommand = response.getBody();
        assertNotNull(createdCommand);
        assertNotNull(createdCommand.getId());
        assertEquals(DeviceCommand.CommandStatus.PENDING, createdCommand.getStatus());
        
        commandId = createdCommand.getId();
        
        // Verify command can be retrieved
        ResponseEntity<DeviceCommand> getResponse = restTemplate.getForEntity(
                baseUrl + "/commands/" + commandId,
                DeviceCommand.class);
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        
        // Update command status
        Map<String, DeviceCommand.CommandStatus> statusUpdate = new HashMap<>();
        statusUpdate.put("status", DeviceCommand.CommandStatus.COMPLETED);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, DeviceCommand.CommandStatus>> requestEntity =
                new HttpEntity<>(statusUpdate, headers);
        
        ResponseEntity<DeviceCommand> updateResponse = restTemplate.exchange(
                baseUrl + "/commands/" + commandId + "/status",
                HttpMethod.PUT,
                requestEntity,
                DeviceCommand.class);
                
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse);
        assertNotNull(updateResponse.getBody());
        assertEquals(DeviceCommand.CommandStatus.COMPLETED, updateResponse.getBody().getStatus());
    }
    
    @Test
    @Order(7)
    public void testCompleteCoSensorScenario() {
        // Test retrieving the device with all related entities
        ResponseEntity<Device> deviceResponse = restTemplate.getForEntity(
                baseUrl + "/devices/" + deviceId,
                Device.class);
                
        assertEquals(HttpStatus.OK, deviceResponse.getStatusCode());
        assertNotNull(deviceResponse);
        if (deviceResponse.getBody() != null) {
            assertEquals(deviceId, deviceResponse.getBody().getId());
        }
        
        // Test retrieving device state
        ResponseEntity<DeviceState> stateResponse = restTemplate.getForEntity(
                baseUrl + "/device-states/device/" + deviceId,
                DeviceState.class);
                
        assertEquals(HttpStatus.OK, stateResponse.getStatusCode());
        
        // Test retrieving readings - ensure path matches controller exactly
        ResponseEntity<List<Reading>> readingsResponse = restTemplate.exchange(
                baseUrl + "/readings/device/" + deviceId,  // Use just device ID without signal ID
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Reading>>() {});
                
        assertEquals(HttpStatus.OK, readingsResponse.getStatusCode());
        
        // Test update state scenario
        // ...rest of method...
    }
    
    @Test
    @Order(99)
    public void testDeleteEntities() {
        // Delete in reverse order of creation
        
        // Delete readings (just by retrieving device)
        ResponseEntity<Void> deviceDeleteResponse = restTemplate.exchange(
                baseUrl + "/devices/" + deviceId,
                HttpMethod.DELETE,
                null,
                Void.class);
                
        assertEquals(HttpStatus.NO_CONTENT, deviceDeleteResponse.getStatusCode());
        
        // Verify device is deleted
        ResponseEntity<Device> deviceGetResponse = restTemplate.getForEntity(
                baseUrl + "/devices/" + deviceId,
                Device.class);
                
        assertEquals(HttpStatus.NOT_FOUND, deviceGetResponse.getStatusCode());
        
        // Delete datasource
        ResponseEntity<Void> datasourceDeleteResponse = restTemplate.exchange(
                baseUrl + "/datasources/" + datasourceId,
                HttpMethod.DELETE,
                null,
                Void.class);
                
        assertEquals(HttpStatus.NO_CONTENT, datasourceDeleteResponse.getStatusCode());
        
        // Delete device definition
        ResponseEntity<Void> definitionDeleteResponse = restTemplate.exchange(
                baseUrl + "/device-definitions/" + deviceDefinitionId,
                HttpMethod.DELETE,
                null,
                Void.class);
                
        assertEquals(HttpStatus.NO_CONTENT, definitionDeleteResponse.getStatusCode());
    }

    @AfterAll
    static void tearDown() {
        if (mongoDBContainer != null && mongoDBContainer.isRunning()) {
            mongoDBContainer.stop();
        }
    }
} 