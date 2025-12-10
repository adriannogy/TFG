package com.restaurantes.restaurantesaplicacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;


@ExtendWith(MockitoExtension.class)
public class CloudinaryFileStorageServiceImplTest {

   
    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryFileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    
    @Test
    public void store_cuandoArchivoEsValido_retornaSecureUrl() throws IOException {
        // Given 
        MultipartFile fakeFile = mock(MultipartFile.class);
        byte[] fakeBytes = "fakeimagedata".getBytes();
        when(fakeFile.getBytes()).thenReturn(fakeBytes);

        String fakeUrl = "http://cloudinary.com/fake-url.jpg";
        Map fakeResponseMap = Map.of("secure_url", fakeUrl);

        when(uploader.upload(eq(fakeBytes), eq(ObjectUtils.emptyMap())))
                .thenReturn(fakeResponseMap);

        // When
        String resultUrl = fileStorageService.store(fakeFile);

        // Then
        assertNotNull(resultUrl);
        assertEquals(fakeUrl, resultUrl);
    }

    @Test
    public void store_cuandoCloudinaryLanzaIOException_lanzaRuntimeException() throws IOException {
        // Given
        MultipartFile fakeFile = mock(MultipartFile.class);
        byte[] fakeBytes = "fakeimagedata".getBytes();
        when(fakeFile.getBytes()).thenReturn(fakeBytes);

        when(uploader.upload(eq(fakeBytes), eq(ObjectUtils.emptyMap())))
                .thenThrow(new IOException("Error de red simulado"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.store(fakeFile);
        });

        assertTrue(exception.getMessage().contains("Error al subir el archivo a Cloudinary"));
        assertTrue(exception.getCause() instanceof IOException);
    }
}