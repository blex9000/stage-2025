package com.sinelec.stage.domain.engine.model;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.data.mongodb.core.timeseries.Granularity;

import java.util.Date;
import java.util.Calendar;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Document(collection = "readings")
@TimeSeries(
    timeField = "timestamp",
    metaField = "metaId",
    granularity = Granularity.SECONDS
)
@CompoundIndexes({
    @CompoundIndex(name = "ds_device_signal_time_idx",
                  def = "{'datasourceId': 1, 'deviceId': 1, 'signalId': 1, 'timestamp': -1}"),
    @CompoundIndex(name = "time_bucket_idx", 
                  def = "{'datasourceId': 1,'deviceId': 1, 'year': 1, 'month': 1, 'day': 1, 'hour': 1}")
})
public class Reading {
    @Id
    private String id;
    @NonNull
    private String datasourceId;
    @NonNull
    private String deviceId;
    @NonNull
    private String signalId;
    
    // Meta-field for time-series optimization (deviceId + signalId)
    @Field("metaId")
    private String metaId;
    
    // Used as the timeField in the time-series collection
    private Date timestamp;
    
    private Object value;
    
    // Adding numeric representation for faster aggregations
    private Double numericValue;    
    
    // Time bucket fields for faster queries and aggregations
    private int year;
    private int month;
    private int day;
    private int hour;
    
    // Alarm information
    private boolean inAlarm;
    private String alarmMessage;
    private AlarmSeverity alarmSeverity;
    
    // Data quality fields
    @Builder.Default
    private boolean valid = true;

    // Raw value before conversion
    private Object rawValue;

    private void setMetaId(String datasourceId, String deviceId, String signalId) {
        if (datasourceId != null && deviceId != null && signalId != null) {
            this.metaId = datasourceId +":" + deviceId + ":" + signalId;
        }
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        
        // Populate time buckets for faster queries
        if (timestamp != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(timestamp);
            this.year = cal.get(Calendar.YEAR);
            this.month = cal.get(Calendar.MONTH) + 1; // Calendar months are 0-based
            this.day = cal.get(Calendar.DAY_OF_MONTH);
            this.hour = cal.get(Calendar.HOUR_OF_DAY);
        }
    }
    
    public void setValue(Object value) {
        this.value = value;
        
        // Try to extract numeric value for aggregations
        if (value instanceof Number) {
            this.numericValue = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                this.numericValue = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                // Not a numeric string, leave numericValue as null
            }
        }
    }

    /**
     * Convert value to the specified type if possible
     */
    public <T> T getValueAs(Class<T> type) {
        if (value == null) {
            return null;
        }
        
        if (type.isAssignableFrom(value.getClass())) {
            return type.cast(value);
        }
        
        // Handle String conversion cases
        if (value instanceof String) {
            String strValue = (String) value;
            if (type == Integer.class) {
                return type.cast(Integer.parseInt(strValue));
            } else if (type == Double.class) {
                return type.cast(Double.parseDouble(strValue));
            } else if (type == Boolean.class) {
                return type.cast(Boolean.parseBoolean(strValue));
            }
        }
        
        // Handle numeric conversions
        if (value instanceof Number) {
            Number numValue = (Number) value;
            if (type == Integer.class) {
                return type.cast(numValue.intValue());
            } else if (type == Double.class) {
                return type.cast(numValue.doubleValue());
            } else if (type == Long.class) {
                return type.cast(numValue.longValue());
            }
        }
        
        return null; // Unable to convert
    }
}