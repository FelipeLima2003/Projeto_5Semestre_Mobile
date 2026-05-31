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
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class CadastroSucessoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(CadastroActivity::class.java)

    @Before
    fun setup() {
        RetrofitClient.clearAuthToken()
    }

    @Test
    fun testeCadastroUsuarioValido_deveNavegarParaLogin() {
        val TAG = "TEST_CADASTRO_SUCESSO"
        Log.d(TAG, "--- INICIANDO TESTE: Cadastro de usuário válido ---")

        try {
            Log.d(TAG, "Passo 1: Preenchendo dados válidos")
            onView(withId(R.id.edit_nome)).perform(replaceText("User Success"), closeSoftKeyboard())
            onView(withId(R.id.edit_nascimento)).perform(replaceText("10102000"), closeSoftKeyboard())
            onView(withId(R.id.edit_cpf)).perform(replaceText("14792610077"), closeSoftKeyboard())

            // Email dinâmico para evitar erro de duplicidade
            val emailDinamico = "success${System.currentTimeMillis()}@teste.com"
            onView(withId(R.id.edit_email)).perform(replaceText(emailDinamico), closeSoftKeyboard())

            onView(withId(R.id.edit_telefone)).perform(replaceText("11999998888"), closeSoftKeyboard())

            Log.d(TAG, "Passo 2: Selecionando gênero masculino")
            onView(withId(R.id.rb_masculino)).perform(click())

            Log.d(TAG, "Passo 3: Digitando senhas fortes iguais")
            onView(withId(R.id.edit_senha)).perform(replaceText("Senha@123"), closeSoftKeyboard())
            onView(withId(R.id.edit_confirma_senha)).perform(replaceText("Senha@123"), closeSoftKeyboard())

            Log.d(TAG, "Passo 4: Clicando em confirmar")
            onView(withId(R.id.containerButtonConfirmar)).perform(scrollTo(), click())

            Log.d(TAG, "Passo 5: Aguardando navegação")

            var navegou = false
            for (i in 1..20) {
                Thread.sleep(2000)
                try {
                    onView(withId(R.id.txt_ir_para_cadastro)).check(matches(isDisplayed()))
                    navegou = true
                    break
                } catch (e: Throwable) {
                    Log.d(TAG, "Aguardando resposta da API... (${i * 2}s)")
                }
            }

            if (!navegou) throw RuntimeException("O aplicativo não navegou para a tela de Login.")

            Log.d(TAG, "--- RESULTADO: SUCESSO ---")
        } catch (e: Throwable) {
            Log.e(TAG, "--- RESULTADO: FALHA - ${e.message} ---")
            throw e
        }
    }
}