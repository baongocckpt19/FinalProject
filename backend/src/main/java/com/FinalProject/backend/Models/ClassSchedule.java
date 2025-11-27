//ĐÂY LÀ FILE CLASSSCHEDULE TRONG MODELS
package com.FinalProject.backend.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "ClassSchedule")
public class ClassSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScheduleId")
    private Integer scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClassId", nullable = false)
    private Clazz clazz; // ánh xạ sang entity Clazz

    @Column(name = "ScheduleDate", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "Room")
    private String room;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "IsDeleted", nullable = false)
    private Boolean isDeleted = false;
}
