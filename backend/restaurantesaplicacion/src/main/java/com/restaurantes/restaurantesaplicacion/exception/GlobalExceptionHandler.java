package com.restaurantes.restaurantesaplicacion.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.restaurantes.restaurantesaplicacion.dto.ErrorResponseDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ValoracionDuplicadaException.class)
    public ResponseEntity<ErrorResponseDTO> handleValoracionDuplicadaException(
            ValoracionDuplicadaException ex, 
            WebRequest request) {

        ErrorResponseDTO errorDetails = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Error no esperado capturado: {}", ex.getMessage(), ex);

        ErrorResponseDTO errorDetails = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ha ocurrido un error inesperado.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex, 
            WebRequest request) {

        ErrorResponseDTO errorDetails = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(), // Código 404
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException ex, WebRequest request) {
        ErrorResponseDTO errorDetails = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(), // Código 409
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        log.warn("Conflicto de datos: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(BadRequestException ex, WebRequest request) {
        ErrorResponseDTO errorDetails = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), // Código 400
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        log.warn("Petición incorrecta: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponseDTO> handleLockedException(LockedException ex, WebRequest request) {
        ErrorResponseDTO errorDetails = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(), // 403
                "Account Not Verified",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        log.warn("Intento de login en cuenta no verificada: {}", ex.getMessage());
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        ErrorResponseDTO errorDetails = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(), // 401
                "Bad Credentials",
                "Email o contraseña incorrectos",
                request.getDescription(false).replace("uri=", "")
        );
        log.warn("Intento de login fallido (credenciales incorrectas)");
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) 
public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
        MethodArgumentNotValidException ex, 
        WebRequest request) {

    
    StringBuilder sb = new StringBuilder("Errores de validación: ");
    ex.getBindingResult().getAllErrors().forEach((error) -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        sb.append(fieldName).append(": ").append(errorMessage).append("; ");
    });

    
    ErrorResponseDTO errorDetails = new ErrorResponseDTO(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(), // Código 400
            "Bad Request",
            sb.toString().trim(), 
            request.getDescription(false).replace("uri=", "")
    );
    
   
    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
}
}
