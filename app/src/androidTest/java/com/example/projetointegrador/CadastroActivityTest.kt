package com.example.projetointegrador

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CadastroActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(CadastroActivity::class.java)

    @Test
    fun testeDeUsabilidade_preencherFormularioEExibirErroDeSenhaDivergente() {
        // Digita os dados nos campos
        onView(withId(R.id.edit_nome)).perform(typeText("Gustavo Ramos"), closeSoftKeyboard())
        onView(withId(R.id.edit_nascimento)).perform(typeText("15052000"), closeSoftKeyboard())
        onView(withId(R.id.edit_cpf)).perform(typeText("12345678900"), closeSoftKeyboard())
        onView(withId(R.id.edit_email)).perform(typeText("gustavo@teste.com"), closeSoftKeyboard())
        onView(withId(R.id.edit_telefone)).perform(typeText("11999999999"), closeSoftKeyboard())

        // Seleciona o Gênero
        onView(withId(R.id.rb_masculino)).perform(click())

        // Digita senhas diferentes para testar a validação visual
        onView(withId(R.id.edit_senha)).perform(typeText("Senha123"), closeSoftKeyboard())
        onView(withId(R.id.edit_confirma_senha)).perform(typeText("SenhaErrada"), closeSoftKeyboard())

        // Clica em confirmar (Adicionado scrollTo() para garantir que o botão esteja visível)
        onView(withId(R.id.containerButtonConfirmar)).perform(scrollTo(), click())

        // Recupera a string do recurso para validar o erro
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedError = context.getString(R.string.cadastro_erro_senhas_diferentes)

        // Verifica se o erro visual de senhas diferentes foi acionado na tela
        onView(withId(R.id.edit_confirma_senha)).check(matches(hasErrorText(expectedError)))
    }
}
