package com.sinelec.stage.engine.integration;

import com.sinelec.stage.domain.engine.model.*;
import com.sinelec.stage.domain.engine.driver.Driver;
import com.sinelec.stage.engine.core.Engine;
import com.sinelec.stage.engine.core.Engines;
import com.sinelec.stage.engine.core.DriverRegistry;
import com.sinelec.stage.service.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(properties = {
    "app.engine.poll.interval=2000" // Set polling interval to 2 seconds for testing
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class EngineIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(EngineIntegrationTest.class);
    
    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"))
            .withStartupTimeout(Duration.ofMinutes(2))
            .withStartupAttempts(3);
            
    @Autowired
    private DriverRegistry driverRegistry;
    
    @Autowired
    private Engines engines;
    
    @Autowired
    private DatasourceService datasourceService;
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private DeviceStateService deviceStateService;
    
    @Autowired
    private ReadingService readingService;
    
    @Autowired
    private DriverDefinitionService driverDefinitionService;
    
    @Autowired
    private TestDataUtil testDataUtil;
    
    private static String datasourceId;
    private static String deviceId;
    private static String driverDefinitionId = "MOCK_DRIVER";
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        if (mongoDBContainer.isRunning()) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
            registry.add("spring.data.mongodb.database", () -> "test");
            logger.info("MongoDB container is running at: {}", mongoDBContainer.getReplicaSetUrl());
        } else {
            logger.error("MongoDB container failed to start. Tests will likely fail.");
        }
    }
    
    @BeforeAll
    static void setUpMongo() {
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
            assertTrue(mongoDBContainer.isRunning(), "MongoDB container must be running for tests");
            logger.info("MongoDB container started successfully");
        }
    }
    
    @BeforeEach
    void setUp() {
        assertTrue(mongoDBContainer.isRunning(), "MongoDB container must be running for tests");
    }
    
    @Test
    @Order(1)
    public void testSetupTestEnvironment() {
        // Setup test data (skipping extensive assertions since these are tested elsewhere)
        
        // 1. Create test device definition with signals
        DeviceDefinition deviceDefinition = testDataUtil.createTestDeviceDefinition();
        assertNotNull(deviceDefinition.getId(), "Device definition should be created");
        
        // 2. Create test datasource using MockDriver
        Datasource datasource = new Datasource();
        datasource.setName("Test");
        datasource.setDescription("Test datasource");
        datasource.setDriverId(driverDefinitionId);
        datasource.setActive(true);
        datasource.setConfiguration(List.of(
            new Property("HOST", "localhost"),
            new Property("PORT", "12345")
        ));
        
        Datasource createdDatasource = datasourceService.createDatasource(datasource);
        assertNotNull(createdDatasource, "Datasource should be created");
        datasourceId = createdDatasource.getId();
        
        // 3. Create test device
        Device device = new Device();
        device.setName("Engine Test Device");
        device.setDeviceDefinitionId(deviceDefinition.getId());
        device.setDatasourceId(datasourceId);
        device.setActive(true);
        
        // Add signal configurations for the device
        List<SignalConfiguration> signalConfigs = new ArrayList<>();
        
        // Add temperature signal config
        SignalConfiguration tempConfig = new SignalConfiguration();
        tempConfig.setSignalId("temp-signal-id");
        
        // Create properties instead of using non-existent methods
        List<Property> tempProperties = new ArrayList<>();
        tempProperties.add(new Property("ADDRESS", "40001"));
        tempProperties.add(new Property("SCALE_FACTOR", "1.0"));
        tempProperties.add(new Property("READ_ONLY", "false"));
        tempConfig.setSignalProperties(tempProperties);
        
        signalConfigs.add(tempConfig);
        
        // Add status signal config
        SignalConfiguration statusConfig = new SignalConfiguration();
        statusConfig.setSignalId("status-signal-id");
        
        // Create properties for status signal
        List<Property> statusProperties = new ArrayList<>();
        statusProperties.add(new Property("ADDRESS", "10001"));
        statusProperties.add(new Property("READ_ONLY", "true"));
        statusConfig.setSignalProperties(statusProperties);
        
        signalConfigs.add(statusConfig);
        
        device.setSignalConfigurations(signalConfigs);
        
        Device createdDevice = deviceService.createDevice(device);
        assertNotNull(createdDevice, "Device should be created");
        deviceId = createdDevice.getId();
        
        logger.info("Test environment setup complete with datasource ID: {} and device ID: {}", 
                datasourceId, deviceId);
    }
    
    @Test
    @Order(2)
    public void testDirectDriverFunctionality() {
        // Test driver functionality directly (without engine)
        
        // 1. Get a driver instance
        Driver driver = driverRegistry.createDriver(driverDefinitionId);
        assertNotNull(driver, "Should be able to create driver instance");
        
        // 2. Initialize and connect the driver
        Datasource datasource = datasourceService.getDatasourceById(datasourceId).orElseThrow();
        driver.initialize(datasource);
        boolean connected = driver.connect();
        assertTrue(connected, "Driver should connect successfully");
        
        // 3. Create read commands for testing
        List<DeviceCommand> readCommands = new ArrayList<>();
        DeviceCommand readCommand = new DeviceCommand();
        readCommand.setDeviceId(deviceId);
        readCommand.setDatasourceId(datasourceId);
        
        // Add signal definitions to read
        Device device = deviceService.getDeviceById(deviceId).orElseThrow();
        DeviceDefinition deviceDef = deviceService.getDeviceDefinitionById(
                device.getDeviceDefinitionId()).orElseThrow();
        
        readCommand.setSignalDefinitions(deviceDef.getSignals());
        readCommands.add(readCommand);
        
        // 4. Execute read
        try {
            List<Reading> readings = driver.read(readCommands);
            assertNotNull(readings, "Readings should be returned");
            assertFalse(readings.isEmpty(), "Readings should not be empty");
            
            // Validate readings
            for (Reading reading : readings) {
                assertNotNull(reading.getDeviceId(), "Reading should have device ID");
                assertNotNull(reading.getSignalId(), "Reading should have signal ID");
                assertNotNull(reading.getTimestamp(), "Reading should have timestamp");
                assertNotNull(reading.getValue(), "Reading should have value");
            }
            
            logger.info("Driver read test successful, got {} readings", readings.size());
        } catch (Exception e) {
            fail("Driver read should not throw exception: " + e.getMessage());
        }
        
        // 5. Test write functionality
        DeviceCommand writeCommand = new DeviceCommand();
        writeCommand.setId(UUID.randomUUID().toString());
        writeCommand.setDeviceId(deviceId);
        writeCommand.setDatasourceId(datasourceId);
        writeCommand.setCommandType(DeviceCommand.CommandType.WRITE);
        DeviceCommand.WriteValue writeValue = new DeviceCommand.WriteValue("temp-signal-id", "25.5");
        writeCommand.getWriteValues().add(writeValue);
        writeCommand.setCreatedAt(new Date());
        
        try {
            driver.write(List.of(writeCommand));
            logger.info("Driver write test successful");
        } catch (Exception e) {
            fail("Driver write should not throw exception: " + e.getMessage());
        }
        
        // 6. Disconnect
        driver.disconnect();
        assertFalse(driver.isConnected(), "Driver should be disconnected");
    }
    
    @Test
    @Order(3)
    public void testDirectEngineFunctionality() {
        // Test engine functionality directly (without engines manager)
        
        // 1. Create engine components
        Datasource datasource = datasourceService.getDatasourceById(datasourceId).orElseThrow();
        List<Device> devices = deviceService.getDevicesByDatasourceId(datasourceId);
        Driver driver = driverRegistry.createDriver(driverDefinitionId);
        
        // 2. Create engine instance
        Engine engine = new Engine(
                datasource,
                devices,
                driver,
                deviceService,
                deviceStateService,
                readingService,
                driverDefinitionService);
        
        // 3. Start engine
        boolean started = engine.start();
        assertTrue(started, "Engine should start successfully");
        assertTrue(engine.isRunning(), "Engine should be running");
        assertTrue(engine.isConnected(), "Engine should be connected");
        
        // 4. Test poll
        List<Reading> readings = engine.poll();
        assertNotNull(readings, "Engine poll should return readings");
        assertFalse(readings.isEmpty(), "Readings list should not be empty");
        
        // Verify readings
        for (Reading reading : readings) {
            assertEquals(deviceId, reading.getDeviceId(), "Reading should have correct device ID");
            assertNotNull(reading.getSignalId(), "Reading should have signal ID");
            assertNotNull(reading.getValue(), "Reading should have value");
        }
        
        // 5. Test write
        DeviceCommand command = new DeviceCommand();
        command.setId(UUID.randomUUID().toString());
        command.setDeviceId(deviceId);
        command.setDatasourceId(datasourceId);
        command.setCommandType(DeviceCommand.CommandType.WRITE);
        DeviceCommand.WriteValue writeValue = new DeviceCommand.WriteValue("temp-signal-id", "25.5");
        command.getWriteValues().add(writeValue);
        command.setCreatedAt(new Date());
        
        boolean writeResult = engine.write(command);
        assertTrue(writeResult, "Engine write should succeed");
        
        // 6. Verify device state was created/updated
        Optional<DeviceState> stateOpt = deviceStateService.getDeviceStateByDeviceId(deviceId);
        assertTrue(stateOpt.isPresent(), "Device state should exist");
        DeviceState state = stateOpt.get();
        assertNotNull(state.getSignalStates(), "Signal states should exist");
        assertTrue(state.getSignalStates().size() > 0, "Should have signal states");
        
        // 7. Stop engine
        engine.stop();
        assertFalse(engine.isRunning(), "Engine should not be running after stop");
        
        logger.info("Direct engine test successful");
    }
    
    @Test
    @Order(4)
    public void testEnginesManagerFunctionality() {
        // Test engines manager functionality
        try {
            // 1. Stop all engines first and wait to ensure clean state
            for (String activeEngine : engines.getActiveEngineIds()) {
                engines.stopEngine(activeEngine);
            }
            
            // Wait for engines to fully stop
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                List<String> activeEngines = engines.getActiveEngineIds();
                assertFalse(activeEngines.contains(datasourceId), 
                        "Datasource engine should be fully stopped before starting test");
            });
            
            // 2. Start engines with a more robust approach
            engines.loadAndStartEngines();
            
            // Wait and verify engine is running
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                List<String> activeEngines = engines.getActiveEngineIds();
                assertTrue(activeEngines.contains(datasourceId), 
                        "Datasource engine should be active");
                
                Map<String, Boolean> status = engines.getAllEngineStatus();
                assertTrue(status.getOrDefault(datasourceId, false), 
                        "Engine should be running and connected");
            });
            
            // 3. Test manual polling with retries
            List<Reading> readings = null;
            for (int attempt = 0; attempt < 3; attempt++) {
                readings = engines.pollEngine(datasourceId);
                if (readings != null && !readings.isEmpty()) {
                    break;
                }
                logger.info("Polling attempt {} yielded no readings, will retry...", attempt + 1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            assertNotNull(readings, "Polling should return readings");
            assertFalse(readings.isEmpty(), "Readings should not be empty");
            
            // 4. Check reading storage
            await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                List<Reading> storedReadings = readingService.getReadingsByDeviceId(deviceId);
                assertFalse(storedReadings.isEmpty(), "Readings should be stored in database");
                
                // Log what we found for debugging
                logger.info("Found {} readings for device {}", storedReadings.size(), deviceId);
                
                // Check each signal has at least one reading
                long tempReadings = storedReadings.stream()
                        .filter(r -> r.getSignalId().equals("temp-signal-id"))
                        .count();
                        
                long statusReadings = storedReadings.stream()
                        .filter(r -> r.getSignalId().equals("status-signal-id"))
                        .count();
                        
                logger.info("Found {} temperature readings and {} status readings", 
                        tempReadings, statusReadings);
                        
                assertTrue(tempReadings > 0, "Should have temperature readings");
                assertTrue(statusReadings > 0, "Should have status readings");
            });
            
            // 5. Test write command via engines manager
            DeviceCommand command = new DeviceCommand();
            command.setId(UUID.randomUUID().toString());
            command.setDeviceId(deviceId);
            command.setDatasourceId(datasourceId);
            command.setCommandType(DeviceCommand.CommandType.WRITE);
            DeviceCommand.WriteValue writeValue = new DeviceCommand.WriteValue("temp-signal-id", "25.5");
            command.getWriteValues().add(writeValue);
            command.setCreatedAt(new Date());
            
            boolean result = engines.writeCommand(command);
            assertTrue(result, "Command execution should succeed");
            
            // 6. Test engine control with better verification
            Map<String, Object> stats = engines.getEngineStatistics(datasourceId);
            assertNotNull(stats, "Engine statistics should be available");
            assertEquals(datasourceId, stats.get("datasourceId"), "Statistics should match datasource");
            
            // *** Modified restart approach ***
            // Instead of stop+restart, use a direct restart approach which might be more reliable
            logger.info("Testing engine restart...");
            boolean restarted = false;
            
            for (int attempt = 0; attempt < 3; attempt++) {
                logger.info("Restart attempt {} for engine {}", attempt + 1, datasourceId);
                
                // First ensure engine is stopped
                engines.stopEngine(datasourceId);
                
                // Wait for engine to completely stop
                await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                    List<String> activeEngines = engines.getActiveEngineIds();
                    assertFalse(activeEngines.contains(datasourceId), 
                            "Datasource engine should be fully stopped before restart");
                });
                
                // Now try to restart
                restarted = engines.restartEngine(datasourceId);
                
                if (restarted) {
                    logger.info("Successfully restarted engine on attempt {}", attempt + 1);
                    break;
                } else {
                    logger.warn("Restart attempt {} failed, will retry...", attempt + 1);
                    // Get datasource info for debugging
                    Optional<Datasource> datasource = datasourceService.getDatasourceById(datasourceId);
                    logger.info("Datasource exists: {}", datasource.isPresent());
                    if (datasource.isPresent()) {
                        logger.info("Datasource details: id={}, name={}, driverId={}, active={}",
                                datasource.get().getId(), 
                                datasource.get().getName(),
                                datasource.get().getDriverId(),
                                datasource.get().isActive());
                        
                        // Try updating the datasource to ensure it's active
                        if (!datasource.get().isActive()) {
                            logger.info("Datasource not active, activating it");
                            datasource.get().setActive(true);
                            datasourceService.updateDatasource(datasourceId, datasource.get());
                        }
                    }
                    
                    // Brief pause before retry
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            
            assertTrue(restarted, "Engine should restart successfully after multiple attempts");
            
            // Verify engine is connected
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                Map<String, Object> engineStats = engines.getEngineStatistics(datasourceId);
                assertTrue((Boolean)engineStats.get("connected"), 
                        "Engine should be connected after restart");
            });
            
            // 7. Verify all engines status
            Map<String, Boolean> allStatus = engines.getAllEngineStatus();
            assertNotNull(allStatus, "Engine status map should be available");
            assertTrue(allStatus.containsKey(datasourceId), "Status map should contain our engine");
            assertTrue(allStatus.get(datasourceId), "Our engine should be running");
            
            logger.info("Engines manager test successful");
        } catch (Exception e) {
            logger.error("Exception during engine manager test", e);
            fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    @Order(5)
    public void testDevicePollingAndStates() {
        // Add test isolation - don't rely on previous test state
        try {
            // Ensure engine is running for this test regardless of previous test
            logger.info("Ensuring engine is running for device polling test");
            
            // Stop any existing engines first
            for (String activeEngine : engines.getActiveEngineIds()) {
                engines.stopEngine(activeEngine);
            }
            
            // Wait to ensure clean state
            await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                List<String> activeEngines = engines.getActiveEngineIds();
                assertFalse(activeEngines.contains(datasourceId), 
                        "All engines should be stopped before test");
            });
            
            // Start fresh
            logger.info("Starting engines for device polling test");
            engines.loadAndStartEngines();
            
            // Make sure engine is running and connected before polling device
            await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
                List<String> activeEngines = engines.getActiveEngineIds();
                boolean engineRunning = activeEngines.contains(datasourceId);
                
                // If engine isn't running, log details and try to restart
                if (!engineRunning) {
                    logger.warn("Engine not running, attempting to start it");
                    engines.loadAndStartEngines();
                }
                
                assertTrue(activeEngines.contains(datasourceId), 
                        "Engine must be running for device polling");
                
                Map<String, Object> stats = engines.getEngineStatistics(datasourceId);
                assertTrue((Boolean)stats.getOrDefault("connected", false), 
                        "Engine must be connected for device polling");
            });
            
            // Poll the device with retries - sometimes first poll after restart can be empty
            List<Reading> readings = null;
            for (int attempt = 0; attempt < 3; attempt++) {
                logger.info("Attempting to poll device {} (attempt {})", deviceId, attempt + 1);
                readings = engines.pollDevices(Collections.singletonList(deviceId));
                
                if (readings != null && !readings.isEmpty()) {
                    logger.info("Successfully polled device with {} readings", readings.size());
                    break;
                }
                
                logger.info("Poll attempt {} yielded no readings, will retry...", attempt + 1);
                try {
                    // Perform a direct poll to warm up the system
                    engines.pollEngine(datasourceId);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            assertNotNull(readings, "Device polling should return readings");
            
            // Log what we got for debugging
            if (readings.isEmpty()) {
                logger.error("No readings returned when polling device {}", deviceId);
                // Let's check if the device exists and is properly configured
                deviceService.getDeviceById(deviceId).ifPresent(device -> {
                    logger.info("Device exists: id={}, name={}, definitionId={}, datasourceId={}, active={}",
                            device.getId(), device.getName(), device.getDeviceDefinitionId(), 
                            device.getDatasourceId(), device.isActive());
                    
                    if (device.getSignalConfigurations() != null) {
                        logger.info("Device has {} signal configurations", 
                                device.getSignalConfigurations().size());
                    } else {
                        logger.warn("Device has no signal configurations!");
                    }
                });
            }
            
            assertFalse(readings.isEmpty(), "Readings from device poll should not be empty");
            
            // 2. Check device state
            Optional<DeviceState> stateOpt = deviceStateService.getDeviceStateByDeviceId(deviceId);
            assertTrue(stateOpt.isPresent(), "Device state should exist after polling");
            DeviceState state = stateOpt.get();
            
            // 3. Verify state properties
            assertNotNull(state.getSignalStates(), "Signal states should exist");
            assertTrue(state.getSignalStates().size() > 0, "Should have signal states");
            assertEquals(HealthStatus.HEALTHY, state.getHealthStatus(), "Device should be healthy");
            assertTrue(state.isConnected(), "Device should be connected");
            
            // 4. Verify signal states
            boolean hasTemperatureState = state.getSignalStates().stream()
                    .anyMatch(s -> s.getSignalId().equals("temp-signal-id"));
            boolean hasStatusState = state.getSignalStates().stream()
                    .anyMatch(s -> s.getSignalId().equals("status-signal-id"));
                    
            assertTrue(hasTemperatureState, "Should have temperature signal state");
            assertTrue(hasStatusState, "Should have status signal state");
            
            // 5. Check last readings
            for (SignalState signalState : state.getSignalStates()) {
                assertNotNull(signalState.getLastReading(), "Signal state should have last reading");
                assertEquals(deviceId, signalState.getLastReading().getDeviceId(), 
                        "Reading should have correct device ID");
                assertEquals(signalState.getSignalId(), signalState.getLastReading().getSignalId(),
                        "Reading should have correct signal ID");
            }
            
            logger.info("Device polling and state test successful");
        } catch (Exception e) {
            logger.error("Exception during device polling test", e);
            fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    @AfterAll
    static void tearDown() {
        if (mongoDBContainer != null && mongoDBContainer.isRunning()) {
            logger.info("Stopping MongoDB container");
            mongoDBContainer.stop();
        }
    }
} 