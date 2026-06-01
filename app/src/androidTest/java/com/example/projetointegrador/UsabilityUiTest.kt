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


@RunWith(AndroidJUnit4::class)
class UsabilityUiTest {


    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun validarPercursoCompletoDoLoginAteConsultaESubTelas() {
        val TAG = "TEST_USABILITY_FLOW"
        

        Log.d(TAG, "Passo 1: Validando interface de Login")
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()))
        

        onView(withId(R.id.txt_ir_para_cadastro)).perform(click())
        onView(withId(R.id.edit_nome)).check(matches(isDisplayed()))
        onView(withId(R.id.icon_voltar)).perform(click())


        Log.d(TAG, "Passo 2: Realizando login para iniciar o percurso principal")
        onView(withId(R.id.edit_email)).perform(replaceText("gustavo11ramossantos@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.edit_senha)).perform(replaceText("gustavo123"), closeSoftKeyboard())
        onView(withId(R.id.containerButtonConfirmar)).perform(click())


        Thread.sleep(5000) 


        Log.d(TAG, "Passo 3: Validando chegada na tela de Consulta")
        onView(withId(R.id.titulo)).check(matches(isDisplayed()))
        

        onView(withId(R.id.searchEditText)).perform(typeText("User"), closeSoftKeyboard())


        Log.d(TAG, "Passo 4: Navegando para 'Sobre Nós'")
        onView(withId(R.id.containerSobreNos)).perform(click())
        

        onView(withId(R.id.titulo_sobre_nos)).check(matches(isDisplayed()))

        Log.d(TAG, "--- RESULTADO: Chegou na tela 'Sobre Nós'. Teste concluído com sucesso! ---")
    }
}
