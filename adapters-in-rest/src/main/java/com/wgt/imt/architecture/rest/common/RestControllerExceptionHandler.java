package com.wgt.imt.architecture.rest.common;

import com.wgt.imt.architecture.business.common.exception.ConflictException;
import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.exception.NotFoundException;
import com.wgt.imt.architecture.rest.common.model.output.ExceptionOutput;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(basePackages = "com.wgt.imt.architecture.rest")
public class RestControllerExceptionHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @ExceptionHandler(ImtException.class)
    public ResponseEntity<ExceptionOutput> handleRestException(final HttpServletRequest request, final ImtException exception) {
        return new ResponseEntity<>(
                ExceptionOutput.builder()
                        .type(exception.getType())
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now().format(FORMATTER))
                        .path(request.getRequestURI() + Optional.ofNullable(request.getQueryString()).map(query -> "?" + query).orElse(Strings.EMPTY))
                        .build(),
                switch(exception) {
                    case ConflictException ignored -> HttpStatus.CONFLICT;
                    case NotFoundException ignored -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.BAD_REQUEST;
                }
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionOutput> handleGenericException(final HttpServletRequest request, final Exception exception) {
        return new ResponseEntity<>(
                ExceptionOutput.builder()
                        .type(exception.getClass().getSimpleName())
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now().format(FORMATTER))
                        .path(request.getRequestURI() + Optional.ofNullable(request.getQueryString()).map(query -> "?" + query).orElse(Strings.EMPTY))
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
