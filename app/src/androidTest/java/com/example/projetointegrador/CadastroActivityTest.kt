package com.example.projetointegrador

import android.util.Log
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
        val TAG = "TEST_CADASTRO"
        Log.d(TAG, "--- INICIANDO TESTE: Preencher formulário e validar erro de senha divergente ---")

        try {
            Log.d(TAG, "Passo 1: Preenchendo campos de identificação (Nome, Nascimento, CPF, Email, Telefone)")
            onView(withId(R.id.edit_nome)).perform(typeText("Gustavo Ramos"), closeSoftKeyboard())
            onView(withId(R.id.edit_nascimento)).perform(typeText("15052000"), closeSoftKeyboard())
            onView(withId(R.id.edit_cpf)).perform(typeText("12345678900"), closeSoftKeyboard())
            onView(withId(R.id.edit_email)).perform(typeText("gustavo@teste.com"), closeSoftKeyboard())
            onView(withId(R.id.edit_telefone)).perform(typeText("11999999999"), closeSoftKeyboard())

            Log.d(TAG, "Passo 2: Selecionando gênero masculino")
            onView(withId(R.id.rb_masculino)).perform(click())

            Log.d(TAG, "Passo 3: Digitando senhas divergentes")
            onView(withId(R.id.edit_senha)).perform(typeText("Senha123"), closeSoftKeyboard())
            onView(withId(R.id.edit_confirma_senha)).perform(typeText("SenhaErrada"), closeSoftKeyboard())

            Log.d(TAG, "Passo 4: Clicando em confirmar")
            onView(withId(R.id.containerButtonConfirmar)).perform(scrollTo(), click())

            Log.d(TAG, "Passo 5: Validando se a mensagem de erro aparece no campo 'Confirmar Senha'")
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val expectedError = context.getString(R.string.cadastro_erro_senhas_diferentes)

            onView(withId(R.id.edit_confirma_senha)).check(matches(hasErrorText(expectedError)))

            Log.d(TAG, "--- RESULTADO: SUCESSO - O erro de senhas diferentes foi exibido corretamente. ---")
        } catch (e: Throwable) {
            Log.e(TAG, "--- RESULTADO: FALHA - Ocorreu um erro durante o teste: ${e.message} ---")
            throw e
        }
    }
}
