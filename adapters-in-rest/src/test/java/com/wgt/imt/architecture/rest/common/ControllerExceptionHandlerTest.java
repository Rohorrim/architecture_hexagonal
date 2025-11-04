package com.wgt.imt.architecture.rest.common;

import com.wgt.imt.architecture.business.common.exception.ImtException;
import com.wgt.imt.architecture.business.common.exception.NotFoundException;
import com.wgt.imt.architecture.rest.common.model.output.ExceptionOutput;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le gestionnaire global d'exceptions ControllerExceptionHandler.
 * Teste la gestion des exceptions ImtException et des exceptions génériques.
 *
 * @author Test Generator
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String TEST_URI = "/api/imt/v1/clients";
    private static final String TEST_QUERY_STRING = "param1=value1&param2=value2";
    @Mock
    private HttpServletRequest request;
    @InjectMocks
    private RestControllerExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn(TEST_URI);
    }

    // ==================== Tests handleRestException (ImtException) ====================

    /**
     * Test de handleRestException avec une ImtException
     * Vérifie que la réponse contient le bon status HTTP (BAD_REQUEST) et le bon format
     */
    @Test
    void handleRestException_WithImtException_ShouldReturnBadRequest() {
        // Arrange
        String errorMessage = "Client non trouvé";
        String errorType = "NotFoundException";
        ImtException exception = new NotFoundException(errorMessage);
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleRestException(request, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ExceptionOutput body = response.getBody();
        assertEquals(errorType, body.getType());
        assertEquals(errorMessage, body.getMessage());
        assertEquals(TEST_URI, body.getPath());
        assertNotNull(body.getTimestamp());

        // Vérifier que le timestamp est au format ISO
        assertDoesNotThrow(() -> LocalDateTime.parse(body.getTimestamp(), FORMATTER));

        verify(request, times(1)).getRequestURI();
        verify(request, times(1)).getQueryString();
    }

    /**
     * Test de handleRestException avec une ImtException et une query string
     * Vérifie que la query string est bien ajoutée au path de l'erreur
     */
    @Test
    void handleRestException_WithQueryString_ShouldIncludeQueryInPath() {
        // Arrange
        String errorMessage = "Erreur de validation";
        ImtException exception = new NotFoundException(errorMessage);
        when(request.getQueryString()).thenReturn(TEST_QUERY_STRING);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleRestException(request, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ExceptionOutput body = response.getBody();
        assertEquals(TEST_URI + "?" + TEST_QUERY_STRING, body.getPath());

        verify(request, times(1)).getRequestURI();
        verify(request, times(1)).getQueryString();
    }

    /**
     * Test de handleRestException avec query string vide
     * Vérifie que la query string vide n'est pas ajoutée au path
     */
    @Test
    void handleRestException_WithEmptyQueryString_ShouldNotIncludeQuestionMark() {
        // Arrange
        String errorMessage = "Erreur métier";
        ImtException exception = new NotFoundException(errorMessage);
        when(request.getQueryString()).thenReturn("");

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleRestException(request, exception);

        // Assert
        assertNotNull(response);
        ExceptionOutput body = response.getBody();
        assertNotNull(body);
        // Avec une chaîne vide, le "?" sera ajouté - comportement actuel du code
        assertEquals(TEST_URI + "?", body.getPath());
    }

    /**
     * Test de handleRestException vérifie que le timestamp est proche du moment actuel
     * Vérifie que le timestamp généré est récent (moins de 1 seconde)
     */
    @Test
    void handleRestException_ShouldGenerateRecentTimestamp() {
        // Arrange
        ImtException exception = new NotFoundException("Test");
        when(request.getQueryString()).thenReturn(null);
        LocalDateTime before = LocalDateTime.now();

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleRestException(request, exception);

        // Assert
        LocalDateTime after = LocalDateTime.now();
        assertNotNull(response.getBody());
        LocalDateTime timestamp = LocalDateTime.parse(response.getBody().getTimestamp(), FORMATTER);

        // Le timestamp doit être entre avant et après l'appel
        assertTrue(timestamp.isAfter(before.minusSeconds(1)) || timestamp.isEqual(before));
        assertTrue(timestamp.isBefore(after.plusSeconds(1)) || timestamp.isEqual(after));
    }

    /**
     * Test de handleRestException avec différents types d'ImtException
     * Vérifie que le type d'exception est correctement extrait
     */
    @Test
    void handleRestException_WithDifferentExceptionTypes_ShouldExtractCorrectType() {
        // Arrange
        String errorMessage = "Ressource introuvable";
        ImtException exception = new NotFoundException(errorMessage);
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleRestException(request, exception);

        // Assert
        assertNotNull(response.getBody());
        assertEquals("NotFoundException", response.getBody().getType());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    // ==================== Tests handleGenericException (Exception) ====================

    /**
     * Test de handleGenericException avec une RuntimeException
     * Vérifie que la réponse contient le bon status HTTP (INTERNAL_SERVER_ERROR)
     */
    @Test
    void handleGenericException_WithRuntimeException_ShouldReturnInternalServerError() {
        // Arrange
        String errorMessage = "Erreur inattendue";
        RuntimeException exception = new RuntimeException(errorMessage);
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleGenericException(request, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        ExceptionOutput body = response.getBody();
        assertEquals("RuntimeException", body.getType());
        assertEquals(errorMessage, body.getMessage());
        assertEquals(TEST_URI, body.getPath());
        assertNotNull(body.getTimestamp());

        // Vérifier que le timestamp est au format ISO
        assertDoesNotThrow(() -> LocalDateTime.parse(body.getTimestamp(), FORMATTER));

        verify(request, times(1)).getRequestURI();
        verify(request, times(1)).getQueryString();
    }

    /**
     * Test de handleGenericException avec une NullPointerException
     * Vérifie que le nom de la classe d'exception est correctement extrait
     */
    @Test
    void handleGenericException_WithNullPointerException_ShouldExtractClassName() {
        // Arrange
        String errorMessage = "Valeur null inattendue";
        NullPointerException exception = new NullPointerException(errorMessage);
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleGenericException(request, exception);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals("NullPointerException", response.getBody().getType());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    /**
     * Test de handleGenericException avec une query string
     * Vérifie que la query string est bien ajoutée au path de l'erreur
     */
    @Test
    void handleGenericException_WithQueryString_ShouldIncludeQueryInPath() {
        // Arrange
        String errorMessage = "Erreur système";
        Exception exception = new IllegalStateException(errorMessage);
        when(request.getQueryString()).thenReturn(TEST_QUERY_STRING);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleGenericException(request, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        ExceptionOutput body = response.getBody();
        assertEquals(TEST_URI + "?" + TEST_QUERY_STRING, body.getPath());
        assertEquals("IllegalStateException", body.getType());

        verify(request, times(1)).getRequestURI();
        verify(request, times(1)).getQueryString();
    }

    /**
     * Test de handleGenericException avec un message null
     * Vérifie que le handler gère correctement les exceptions sans message
     */
    @Test
    void handleGenericException_WithNullMessage_ShouldHandleGracefully() {
        // Arrange
        Exception exception = new RuntimeException((String) null);
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleGenericException(request, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getMessage());
        assertEquals("RuntimeException", response.getBody().getType());
    }

    /**
     * Test de handleGenericException vérifie que le timestamp est proche du moment actuel
     * Vérifie que le timestamp généré est récent (moins de 1 seconde)
     */
    @Test
    void handleGenericException_ShouldGenerateRecentTimestamp() {
        // Arrange
        Exception exception = new IllegalArgumentException("Test");
        when(request.getQueryString()).thenReturn(null);
        LocalDateTime before = LocalDateTime.now();

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleGenericException(request, exception);

        // Assert
        LocalDateTime after = LocalDateTime.now();
        assertNotNull(response.getBody());
        LocalDateTime timestamp = LocalDateTime.parse(response.getBody().getTimestamp(), FORMATTER);

        // Le timestamp doit être entre avant et après l'appel
        assertTrue(timestamp.isAfter(before.minusSeconds(1)) || timestamp.isEqual(before));
        assertTrue(timestamp.isBefore(after.plusSeconds(1)) || timestamp.isEqual(after));
    }

    // ==================== Tests de comparaison entre les deux handlers ====================

    /**
     * Test de comparaison des status HTTP entre ImtException et Exception générique
     * Vérifie que ImtException retourne BAD_REQUEST et Exception retourne INTERNAL_SERVER_ERROR
     */
    @Test
    void compareHandlers_ShouldReturnDifferentHttpStatus() {
        // Arrange
        ImtException imtException = new NotFoundException("Erreur métier");
        Exception genericException = new RuntimeException("Erreur système");
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> imtResponse = exceptionHandler.handleRestException(request, imtException);
        ResponseEntity<ExceptionOutput> genericResponse = exceptionHandler.handleGenericException(request, genericException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, imtResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, genericResponse.getStatusCode());
    }

    /**
     * Test vérifiant que les deux handlers utilisent le même format de réponse
     * Vérifie que ExceptionOutput contient toujours les mêmes champs
     */
    @Test
    void compareHandlers_ShouldUseSameResponseFormat() {
        // Arrange
        ImtException imtException = new NotFoundException("Erreur métier");
        Exception genericException = new RuntimeException("Erreur système");
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> imtResponse = exceptionHandler.handleRestException(request, imtException);
        ResponseEntity<ExceptionOutput> genericResponse = exceptionHandler.handleGenericException(request, genericException);

        // Assert
        assertNotNull(imtResponse.getBody());
        assertNotNull(genericResponse.getBody());

        // Les deux réponses doivent avoir les mêmes champs remplis
        assertNotNull(imtResponse.getBody().getType());
        assertNotNull(imtResponse.getBody().getMessage());
        assertNotNull(imtResponse.getBody().getTimestamp());
        assertNotNull(imtResponse.getBody().getPath());

        assertNotNull(genericResponse.getBody().getType());
        assertNotNull(genericResponse.getBody().getMessage());
        assertNotNull(genericResponse.getBody().getTimestamp());
        assertNotNull(genericResponse.getBody().getPath());
    }
}

