package com.cs3560.library.dao;

import com.cs3560.library.model.Loan;
import java.time.LocalDate;
import java.util.List;

public interface LoanDao extends CrudDao<Loan> {
    List<Loan> findByStudentId(String broncoId);
    List<Loan> findActiveLoans();
    List<Loan> findOverdueLoans();
    List<Loan> findLoansByDateRange(LocalDate startDate, LocalDate endDate);
    List<Loan> findLoansByStudentAndDateRange(String broncoId, LocalDate startDate, LocalDate endDate);
    Loan returnLoan(Long loanId);
} 