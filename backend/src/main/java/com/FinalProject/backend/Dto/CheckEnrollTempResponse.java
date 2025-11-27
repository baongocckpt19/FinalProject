// FILE: src/main/java/com/FinalProject/backend/Dto/CheckEnrollTempResponse.java
package com.FinalProject.backend.Dto;

public class CheckEnrollTempResponse {
    private boolean found;
    private Integer sensorSlot;

    public CheckEnrollTempResponse() {}

    public CheckEnrollTempResponse(boolean found, Integer sensorSlot) {
        this.found = found;
        this.sensorSlot = sensorSlot;
    }

    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }
    public Integer getSensorSlot() { return sensorSlot; }
    public void setSensorSlot(Integer sensorSlot) { this.sensorSlot = sensorSlot; }
}
