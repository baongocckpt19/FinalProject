// FILE: src/main/java/com/FinalProject/backend/Models/DeviceFingerprintSlot.java
package com.FinalProject.backend.Models;

import jakarta.persistence.*;

@Entity
@Table(
        name = "DeviceFingerprintSlot",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"DeviceId", "SensorSlot"})
        }
)
public class DeviceFingerprintSlot {

    @EmbeddedId
    private DeviceFingerprintSlotId id;

    @Column(name = "SensorSlot", nullable = false)
    private Integer sensorSlot;

    public DeviceFingerprintSlotId getId() {
        return id;
    }
    public void setId(DeviceFingerprintSlotId id) {
        this.id = id;
    }
    public Integer getSensorSlot() {
        return sensorSlot;
    }
    public void setSensorSlot(Integer sensorSlot) {
        this.sensorSlot = sensorSlot;
    }
}
