//// package com.FinalProject.backend.Models;
//
//package com.FinalProject.backend.Models;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//import lombok.Data;
//
//@Entity
//@Table(name = "FingerprintTemp")
//@Data
//public class FingerprintTemp {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "TempId")
//    private Integer tempId;
//
//    @Column(name = "SessionCode", nullable = false, unique = true)
//    private String sessionCode;
//
//    @Column(name = "SensorSlot")
//    private Integer sensorSlot; // null khi mới tạo session
//
//    @Column(name = "CreatedAt", nullable = false)
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    // getters & setters
//}
