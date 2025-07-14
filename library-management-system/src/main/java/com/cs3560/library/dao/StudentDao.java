package com.cs3560.library.dao;

import com.cs3560.library.model.Student;
import com.cs3560.library.model.Loan;
import java.util.List;
import java.util.Optional;

public interface StudentDao extends CrudDao<Student> {
    Optional<Student> findByBroncoId(String broncoId);
    List<Student> findByName(String name);
    List<Loan> findActiveLoans(String broncoId);
    List<Loan> findOverdueLoans(String broncoId);
    int countActiveLoans(String broncoId);
    boolean hasOverdueLoans(String broncoId);
} 