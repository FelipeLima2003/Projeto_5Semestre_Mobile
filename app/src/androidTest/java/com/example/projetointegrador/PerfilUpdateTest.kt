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
class PerfilUpdateTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {

        RetrofitClient.clearAuthToken()
    }

    @Test
    fun testeAtualizarDescricaoPerfil_devePersistirDados() {
        val TAG = "TEST_PERFIL_UPDATE"
        Log.d(TAG, "--- INICIANDO TESTE: Atualização de Perfil (Login Direto) ---")

        val emailTeste = "gustavo11ramossantos@gmail.com"
        val senhaTeste = "gustavo123"

        try {
            Log.d(TAG, "Passo 1: Realizando login com o usuário: $emailTeste")
            onView(withId(R.id.edit_email)).perform(replaceText(emailTeste), closeSoftKeyboard())
            onView(withId(R.id.edit_senha)).perform(replaceText(senhaTeste), closeSoftKeyboard())
            onView(withId(R.id.containerButtonConfirmar)).perform(click())

            var logou = false
            for (i in 1..10) {
                Thread.sleep(2000)
                try {
                    onView(withId(R.id.containerMeuPerfil)).check(matches(isDisplayed()))
                    logou = true
                    break
                } catch (e: Throwable) {
                    Log.d(TAG, "Aguardando login... (${i * 2}s)")
                }
            }

            if (!logou) throw RuntimeException("Falha ao realizar login. Verifique se o usuário $emailTeste existe.")

            Log.d(TAG, "Passo 2: Navegando para a tela de Perfil")
            onView(withId(R.id.containerMeuPerfil)).perform(click())

            Log.d(TAG, "Passo 3: Alterando a descrição do perfil")
            val novaDescricao = "Corredor entusiasta buscando superar limites. Atualizado em: ${System.currentTimeMillis()}"

            Thread.sleep(2000)

            onView(withId(R.id.edit_perfil_descricao))
                .perform(scrollTo(), replaceText(novaDescricao), closeSoftKeyboard())

            Log.d(TAG, "Passo 4: Clicando no botão salvar")
            onView(withId(R.id.btn_salvar_perfil)).perform(scrollTo(), click())

            Log.d(TAG, "Passo 5: Verificando se a descrição foi salva e exibida")
            Thread.sleep(3000)

            onView(withId(R.id.edit_perfil_descricao))
                .check(matches(withText(novaDescricao)))

            Log.d(TAG, "--- RESULTADO: SUCESSO - Perfil atualizado com sucesso para $emailTeste. ---")

        } catch (e: Throwable) {
            Log.e(TAG, "--- RESULTADO: FALHA - Erro ao atualizar perfil: ${e.message} ---")
            throw e
        }
    }
}
