package com.example.projetointegrador

import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ServiceTestRule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class LocationServiceTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    // 5. Validação das API do Google Maps (Localização)
    @Test
    @Throws(TimeoutException::class)
    fun testeIniciarLocationServiceComSucesso() {
        val serviceIntent = Intent(
            ApplicationProvider.getApplicationContext(),
            LocationService::class.java
        )

        // Tenta fazer o bind no serviço de localização
        val binder: IBinder = serviceRule.bindService(serviceIntent)

        // Valida se o serviço iniciou corretamente e o binder não é nulo
        assertNotNull("O serviço de localização deveria ter iniciado", binder)

        // Valida se as variáveis estáticas do LiveData foram inicializadas
        assertNotNull(LocationService.locationData)
        assertNotNull(LocationService.distanceData)
    }
}
