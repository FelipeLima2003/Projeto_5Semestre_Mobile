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
            onView(withId(R.id.edit_nome)).perform(scrollTo(), clearText(), typeText("Gustavo Ramos"), closeSoftKeyboard())
            onView(withId(R.id.edit_nascimento)).perform(scrollTo(), clearText(), typeText("15/05/2000"), closeSoftKeyboard())
            onView(withId(R.id.edit_cpf)).perform(scrollTo(), clearText(), typeText("123.456.789-00"), closeSoftKeyboard())

            val emailDinamico = "teste${System.currentTimeMillis()}@teste.com"
            onView(withId(R.id.edit_email)).perform(scrollTo(), clearText(), typeText(emailDinamico), closeSoftKeyboard())

            onView(withId(R.id.edit_telefone)).perform(scrollTo(), clearText(), typeText("(11) 99999-9999"), closeSoftKeyboard())

            onView(withId(R.id.rb_masculino)).perform(scrollTo(), click())

            onView(withId(R.id.edit_senha)).perform(scrollTo(), clearText(), typeText("Senha@123"), closeSoftKeyboard())
            onView(withId(R.id.edit_confirma_senha)).perform(scrollTo(), clearText(), typeText("Senha@456"), closeSoftKeyboard())

            onView(withId(R.id.containerButtonConfirmar)).perform(scrollTo(), click())

            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val expectedError = context.getString(R.string.cadastro_erro_senhas_diferentes)

            onView(withId(R.id.edit_confirma_senha)).check(matches(hasErrorText(expectedError)))
            Log.d(TAG, "--- RESULTADO: SUCESSO ---")
        } catch (e: Throwable) {
            Log.e(TAG, "--- RESULTADO: FALHA - ${e.message} ---")
            throw e
        }
    }

    @Test
    fun testeCadastroEmailDuplicado_deveExibirAviso() {
        val TAG = "TEST_CADASTRO_DUPLICADO"
        Log.d(TAG, "--- INICIANDO TESTE: Cadastro com e-mail duplicado ---")

        try {
            val emailFixo = "duplicado@teste.com"

            onView(withId(R.id.edit_nome)).perform(scrollTo(), clearText(), typeText("Teste Duplicidade"), closeSoftKeyboard())
            onView(withId(R.id.edit_nascimento)).perform(scrollTo(), clearText(), typeText("01/01/2000"), closeSoftKeyboard())
            onView(withId(R.id.edit_cpf)).perform(scrollTo(), clearText(), typeText("000.000.000-00"), closeSoftKeyboard())

            // Garantindo que o e-mail foi digitado corretamente
            onView(withId(R.id.edit_email)).perform(scrollTo(), clearText(), typeText(emailFixo), closeSoftKeyboard())
            onView(withId(R.id.edit_email)).check(matches(withText(emailFixo)))

            onView(withId(R.id.edit_telefone)).perform(scrollTo(), clearText(), typeText("(11) 99999-9999"), closeSoftKeyboard())
            onView(withId(R.id.rb_masculino)).perform(scrollTo(), click())

            onView(withId(R.id.edit_senha)).perform(scrollTo(), clearText(), typeText("Senha@123"), closeSoftKeyboard())
            onView(withId(R.id.edit_confirma_senha)).perform(scrollTo(), clearText(), typeText("Senha@123"), closeSoftKeyboard())

            Log.d(TAG, "Clicando no botão confirmar...")
            onView(withId(R.id.containerButtonConfirmar)).perform(scrollTo(), click())

            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val expectedError = context.getString(R.string.cadastro_erro_email_duplicado)

            Log.d(TAG, "Aguardando erro de e-mail duplicado: '$expectedError'")
            var erroExibido = false
            for (i in 1..40) {
                Thread.sleep(1000)
                try {
                    onView(withId(R.id.edit_email)).check(matches(hasErrorText(expectedError)))
                    erroExibido = true
                    Log.d(TAG, "Tentativa $i: Erro de e-mail duplicado detectado!")
                    break
                } catch (e: Throwable) {
                    Log.d(TAG, "Tentativa $i: Aguardando resposta da API...")
                }
            }

            if (!erroExibido) {
                throw RuntimeException("O erro de e-mail duplicado não apareceu no campo após 40 segundos.")
            }

            Log.d(TAG, "--- RESULTADO: SUCESSO ---")
        } catch (e: Throwable) {
            Log.e(TAG, "--- RESULTADO: FALHA - ${e.message} ---")
            throw e
        }
    }
}
