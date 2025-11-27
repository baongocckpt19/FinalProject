//// package com.FinalProject.backend.Repository;
//
//package com.FinalProject.backend.Repository;
//
//import com.FinalProject.backend.Models.FingerprintTemp;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface FingerprintTempRepository extends JpaRepository<FingerprintTemp, Integer> {
//
//    Optional<FingerprintTemp> findBySessionCode(String sessionCode);
//
//    void deleteBySessionCode(String sessionCode);
//}
