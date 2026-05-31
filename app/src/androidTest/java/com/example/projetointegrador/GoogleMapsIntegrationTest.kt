package com.example.projetointegrador

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoogleMapsIntegrationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(CorridaActivity::class.java)

    @Test
    fun verificarSeInterfaceDeCorridaEMapaCarregamCorretamente() {
        // Verifica se o SupportMapFragment está visível
        onView(withId(R.id.map)).check(matches(isDisplayed()))

        // Verifica se os campos de telemetria exibem os rótulos corretos
        onView(withId(R.id.txt_tempo))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Tempo:"))))

        onView(withId(R.id.txt_distancia))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Distância:"))))

        // Verifica se o botão de controle está visível
        onView(withId(R.id.btn_parar_corrida))
            .check(matches(isDisplayed()))
    }

    @Test
    fun verificarEstadoInicialDoServicoDeLocalizacao() {
        // Damos um tempo pequeno para o serviço postar os valores iniciais se necessário
        Thread.sleep(500)

        val distancia = LocationService.distanceData.value
        val duracao = LocationService.durationData.value

        // Validações com mensagens claras
        assertNotNull("O dado de distância não deve ser nulo. Verifique se o LocationService inicializa distanceData.", distancia)
        assertNotNull("O dado de duração não deve ser nulo. Verifique se o LocationService inicializa durationData.", duracao)
        
        assertTrue("A distância deve ser um valor válido (>= 0)", distancia!! >= 0.0)
        assertTrue("A duração deve ser um valor válido (>= 0)", duracao!! >= 0L)
    }
}
