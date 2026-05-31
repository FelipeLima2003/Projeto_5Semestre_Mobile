package com.example.projetointegrador

import android.util.Log
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
 * Valida o percurso do usuário do Login até a tela "Sobre Nós".
 */
@RunWith(AndroidJUnit4::class)
class UsabilityUiTest {

    // Iniciamos pela MainActivity (Login) para testar o fluxo real
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun validarPercursoCompletoDoLoginAteConsultaESubTelas() {
        val TAG = "TEST_USABILITY_FLOW"
        
        // --- 1. TELA DE LOGIN ---
        Log.d(TAG, "Passo 1: Validando interface de Login")
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()))
        
        // Testa navegação de ida e volta para o Cadastro
        onView(withId(R.id.txt_ir_para_cadastro)).perform(click())
        onView(withId(R.id.edit_nome)).check(matches(isDisplayed()))
        onView(withId(R.id.icon_voltar)).perform(click())

        // Realiza o login com credenciais válidas para poder percorrer as telas internas
        Log.d(TAG, "Passo 2: Realizando login para iniciar o percurso principal")
        onView(withId(R.id.edit_email)).perform(replaceText("gustavo11ramossantos@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.edit_senha)).perform(replaceText("gustavo123"), closeSoftKeyboard())
        onView(withId(R.id.containerButtonConfirmar)).perform(click())

        // Aguarda a autenticação e transição (MainActivity -> ConsultaActivity)
        Thread.sleep(5000) 

        // --- 2. TELA DE CONSULTA (Ponto Central) ---
        Log.d(TAG, "Passo 3: Validando chegada na tela de Consulta")
        onView(withId(R.id.titulo)).check(matches(isDisplayed()))
        
        // Testa a barra de busca
        onView(withId(R.id.searchEditText)).perform(typeText("User"), closeSoftKeyboard())

        // --- 3. PERCURSO: CONSULTA -> SOBRE NÓS ---
        Log.d(TAG, "Passo 4: Navegando para 'Sobre Nós'")
        onView(withId(R.id.containerSobreNos)).perform(click())
        
        // Valida que a tela "Sobre Nós" foi carregada
        onView(withId(R.id.titulo_sobre_nos)).check(matches(isDisplayed()))

        Log.d(TAG, "--- RESULTADO: Chegou na tela 'Sobre Nós'. Teste concluído com sucesso! ---")
    }
}
