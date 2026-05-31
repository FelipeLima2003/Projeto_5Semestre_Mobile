package com.example.projetointegrador

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes de Usabilidade e Navegação.
 * Valida o percurso do usuário desde o Login até a tela de Consulta.
 */
@RunWith(AndroidJUnit4::class)
class UsabilityUiTest {

    // Iniciamos pela MainActivity (Login) para testar o fluxo inicial
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun validarPercursoDoUsuarioAteConsulta() {
        // --- 1. TELA DE LOGIN ---
        // Verifica se os campos estão visíveis e guiam o usuário
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_senha)).check(matches(isDisplayed()))
        onView(withId(R.id.containerButtonConfirmar)).check(matches(isClickable()))

        // --- 2. NAVEGAÇÃO PARA CADASTRO ---
        // Simula o usuário indo para a tela de registro
        onView(withId(R.id.txt_ir_para_cadastro)).perform(click())

        // Verifica usabilidade na tela de Cadastro
        onView(withId(R.id.edit_nome)).check(matches(isDisplayed()))
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()))
        
        // Usabilidade: Testa a facilidade de retorno ao Login
        onView(withId(R.id.icon_voltar)).perform(click())

        // Confirma retorno à tela de login
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()))

        // --- 3. TELA DE CONSULTA ---
        // Lançamos a ConsultaActivity diretamente para validar o destino final do percurso principal
        // Isso contorna a necessidade de login real com API no teste de interface
        val intent = Intent(ApplicationProvider.getApplicationContext(), ConsultaActivity::class.java).apply {
            putExtra("LOGGED_IN_USER_ID", 1) // Simula ID do usuário logado
        }
        
        ActivityScenario.launch<ConsultaActivity>(intent).use {
            // Verifica se o título da tela e o campo de busca estão acessíveis
            onView(withId(R.id.titulo)).check(matches(withText(R.string.consulta_titulo)))
            onView(withId(R.id.searchEditText)).check(matches(isDisplayed()))
            onView(withId(R.id.searchEditText)).check(matches(withHint(R.string.consulta_hint_busca)))

            // Verifica se a lista de usuários é exibida corretamente
            onView(withId(R.id.recyclerViewUsuarios)).check(matches(isDisplayed()))

            // Valida usabilidade dos botões de navegação inferior (Sobre Nós, Meu Perfil, Eventos)
            // Devem estar visíveis e permitir interação rápida
            onView(withId(R.id.containerSobreNos)).check(matches(isDisplayed())).check(matches(isClickable()))
            onView(withId(R.id.containerMeuPerfil)).check(matches(isDisplayed())).check(matches(isClickable()))
            onView(withId(R.id.containerEventos)).check(matches(isDisplayed())).check(matches(isClickable()))

            // Simula uma interação de busca para validar a resposta da UI
            onView(withId(R.id.searchEditText)).perform(typeText("Gustavo"), closeSoftKeyboard())
            
            // Testa o botão de voltar da barra superior da consulta
            onView(withId(R.id.iconVoltar)).perform(click())
        }
    }
}
