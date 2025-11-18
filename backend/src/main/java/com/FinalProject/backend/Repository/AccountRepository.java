// đây là AccountRepository.java
package com.FinalProject.backend.Repository;

import com.FinalProject.backend.Models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    @Query(value = """
    WITH T AS (
        SELECT
            TeacherId,
            AccountId,
            FullName,
            ROW_NUMBER() OVER (PARTITION BY AccountId ORDER BY TeacherId) AS rn
        FROM Teacher
    ),
    S AS (
        SELECT
            StudentId,
            AccountId,
            FullName,
            ROW_NUMBER() OVER (PARTITION BY AccountId ORDER BY StudentId) AS rn
        FROM Student
    )
    SELECT
        a.AccountId,                                  -- 0
        a.Username,                                   -- 1
        a.PasswordHash,                               -- 2
        a.RoleId,                                     -- 3
        r.RoleName,                                   -- 4
        COALESCE(t.FullName, s.FullName) AS fullName  -- 5
    FROM Account a
    JOIN Role r ON a.RoleId = r.RoleId
    LEFT JOIN T t ON t.AccountId = a.AccountId AND t.rn = 1
    LEFT JOIN S s ON s.AccountId = a.AccountId AND s.rn = 1
    WHERE a.Username = ?1
      AND a.PasswordHash = ?2
      AND a.isDeleted = 0
    """, nativeQuery = true)
    Object login(String username, String passwordHash);

    @Query(value = """
    WITH T AS (
        SELECT
            TeacherId,
            AccountId,
            FullName,
            ROW_NUMBER() OVER (PARTITION BY AccountId ORDER BY TeacherId) AS rn
        FROM Teacher
    ),
    S AS (
        SELECT
            StudentId,
            AccountId,
            FullName,
            ROW_NUMBER() OVER (PARTITION BY AccountId ORDER BY StudentId) AS rn
        FROM Student
    )
    SELECT
        a.AccountId,                                  -- 0
        a.Username,                                   -- 1
        a.PasswordHash,                               -- 2
        a.RoleId,                                     -- 3
        r.RoleName,                                   -- 4
        COALESCE(t.FullName, s.FullName) AS fullName  -- 5
    FROM Account a
    JOIN Role r ON a.RoleId = r.RoleId
    LEFT JOIN T t ON t.AccountId = a.AccountId AND t.rn = 1
    LEFT JOIN S s ON s.AccountId = a.AccountId AND s.rn = 1
    WHERE a.AccountId = ?1
      AND a.isDeleted = 0
    """, nativeQuery = true)
    Object findById(int id);


    @Query(value = """
    WITH T AS (
        SELECT
            TeacherId,
            AccountId,
            FullName,
            Email,
            Phone,
            Address,
            DateOfBirth,
            Gender,
            ROW_NUMBER() OVER (PARTITION BY AccountId ORDER BY TeacherId) AS rn
        FROM Teacher
    ),
    S AS (
        SELECT
            StudentId,
            AccountId,
            FullName,
            Email,
            Phone,
            Address,
            DateOfBirth,
            Gender,
            ROW_NUMBER() OVER (PARTITION BY AccountId ORDER BY StudentId) AS rn
        FROM Student
    )
    SELECT
        a.AccountId AS accountId,                                   -- 0
        COALESCE(t.FullName, s.FullName) AS fullName,               -- 1
        a.Username AS username,                                     -- 2
        r.RoleName AS roleName,                                     -- 3
        COALESCE(t.Email, s.Email) AS email,                        -- 4
        t.TeacherId AS teacherId,                                   -- 5
        s.StudentId AS studentId,                                   -- 6
        COALESCE(t.Phone, s.Phone) AS phone,                        -- 7
        COALESCE(t.Address, s.Address) AS address,                  -- 8
        CONVERT(varchar(10), COALESCE(t.DateOfBirth, s.DateOfBirth), 23) AS dateOfBirth,  -- 9
        COALESCE(t.Gender, s.Gender) AS gender,                     -- 10
        (
            SELECT COUNT(*)
            FROM Fingerprint f
            WHERE f.StudentId = s.StudentId
        ) AS fingerCount                                           -- 11
    FROM Account a
    JOIN Role r ON a.RoleId = r.RoleId
    LEFT JOIN T t ON t.AccountId = a.AccountId AND t.rn = 1
    LEFT JOIN S s ON s.AccountId = a.AccountId AND s.rn = 1
    WHERE a.isDeleted = 0
    ORDER BY a.AccountId
    """, nativeQuery = true)
    List<Object[]> findAllUserTable();



    @Modifying
    @Query(value = "UPDATE Account SET isDeleted = 1 WHERE AccountId = ?1", nativeQuery = true)
    void softDeleteAccount(int accountId);


    @Query(value = """
    WITH T AS (
        SELECT
            TeacherId,
            AccountId,
            FullName,
            Email,
            Phone,
            Address,
            DateOfBirth,
            Gender,
            ROW_NUMBER() OVER (PARTITION BY AccountId ORDER BY TeacherId) AS rn
        FROM Teacher
    ),
    S AS (
        SELECT
            StudentId,
            AccountId,
            FullName,
            Email,
            Phone,
            Address,
            DateOfBirth,
            Gender,
            ROW_NUMBER() OVER (PARTITION BY AccountId ORDER BY StudentId) AS rn
        FROM Student
    )
    SELECT
        a.AccountId AS accountId,                  -- 0
        a.Username AS username,                    -- 1
        r.RoleName AS roleName,                    -- 2

        COALESCE(t.FullName, s.FullName) AS fullName,     -- 3
        COALESCE(t.Email, s.Email) AS email,               -- 4

        t.TeacherId AS teacherId,                  -- 5
        s.StudentId AS studentId,                  -- 6

        COALESCE(t.Phone, s.Phone) AS phone,       -- 7
        COALESCE(t.Address, s.Address) AS address, -- 8
        CONVERT(varchar(10), COALESCE(t.DateOfBirth, s.DateOfBirth), 23) AS dateOfBirth, -- 9
        COALESCE(t.Gender, s.Gender) AS gender,    -- 10
        (
            SELECT COUNT(*)
            FROM Fingerprint f
            WHERE f.StudentId = s.StudentId
        ) AS fingerCount                           -- 11
    FROM Account a
    JOIN Role r ON a.RoleId = r.RoleId
    LEFT JOIN T t ON t.AccountId = a.AccountId AND t.rn = 1
    LEFT JOIN S s ON s.AccountId = a.AccountId AND s.rn = 1
    WHERE a.isDeleted = 0
      AND a.AccountId = ?1
    """, nativeQuery = true)
    Object findUserDetailByAccountId(int accountId);

}
