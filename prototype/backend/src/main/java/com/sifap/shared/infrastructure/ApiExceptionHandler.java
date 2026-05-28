package com.sifap.shared.infrastructure;

import com.sifap.beneficiary.domain.BeneficiaryNotFoundException;
import com.sifap.beneficiary.domain.DependentLimitReachedException;
import com.sifap.beneficiary.domain.DependentNotAllowedException;
import com.sifap.beneficiary.domain.InvalidCpfException;
import com.sifap.payment.application.DiscountCapExceededException;
import com.sifap.payment.application.DuplicatePaymentException;
import com.sifap.payment.application.PaymentNotFoundException;
import com.sifap.payment.application.PaymentStatusTransitionException;
import com.sifap.payment.infrastructure.BatchExecutionNotFoundException;
import com.sifap.payment.infrastructure.BatchRequestConflictException;
import com.sifap.program.domain.ProgramNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ DuplicatePaymentException.class, DiscountCapExceededException.class,
            DependentLimitReachedException.class, DependentNotAllowedException.class,
            BatchRequestConflictException.class, PaymentStatusTransitionException.class })
    public ProblemDetail handleConflict(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({ BeneficiaryNotFoundException.class, ProgramNotFoundException.class,
            BatchExecutionNotFoundException.class, PaymentNotFoundException.class })
    public ProblemDetail handleNotFound(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class,
            InvalidCpfException.class,
            IllegalArgumentException.class })
    public ProblemDetail handleBadRequest(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}