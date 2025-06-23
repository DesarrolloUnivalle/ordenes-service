package com.tienda.ordenes;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrdenesApplicationTest {

    @Test
    void main_deberiaEjecutarseSinErrores() {
        OrdenesApplication.main(new String[]{});
    }

    @Test
    void customOpenAPI_deberiaRetornarOpenAPIValido() {
        OrdenesApplication app = new OrdenesApplication(); // Crear instancia de la aplicación
        OpenAPI openAPI = app.customOpenAPI();

        assertNotNull(openAPI);
        assertEquals("Ordenes API", openAPI.getInfo().getTitle());
        assertEquals("1.0", openAPI.getInfo().getVersion()); // Verifica que la versión sea correcta
    }
}
