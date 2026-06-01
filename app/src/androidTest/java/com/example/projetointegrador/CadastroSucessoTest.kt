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
import java.util.UUID
import kotlin.random.Random

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

            val randomUuid = UUID.randomUUID().toString().replace("-", "").take(12)
            val emailDinamico = "user_$randomUuid@automacao.com"
            

            val cpfDinamico = (1..11).map { Random.nextInt(0, 10) }.joinToString("")
            

            val telefoneDinamico = "119" + (1..8).map { Random.nextInt(0, 10) }.joinToString("")

            Log.i(TAG, "DADOS ÚNICOS GERADOS PARA ESTA EXECUÇÃO:")
            Log.i(TAG, "Email: $emailDinamico")
            Log.i(TAG, "CPF: $cpfDinamico")
            Log.i(TAG, "Telefone: $telefoneDinamico")


            onView(withId(R.id.edit_nome)).perform(replaceText("Tester $randomUuid"), closeSoftKeyboard())

            onView(withId(R.id.edit_nascimento)).perform(replaceText("15/05/1990"), closeSoftKeyboard())
            
            onView(withId(R.id.edit_cpf)).perform(replaceText(cpfDinamico), closeSoftKeyboard())
            onView(withId(R.id.edit_email)).perform(replaceText(emailDinamico), closeSoftKeyboard())
            onView(withId(R.id.edit_telefone)).perform(replaceText(telefoneDinamico), closeSoftKeyboard())

            onView(withId(R.id.rb_masculino)).perform(click())
            onView(withId(R.id.edit_senha)).perform(replaceText("Senha@123"), closeSoftKeyboard())
            onView(withId(R.id.edit_confirma_senha)).perform(replaceText("Senha@123"), closeSoftKeyboard())


            onView(withId(R.id.containerButtonConfirmar)).perform(scrollTo(), click())

            Log.d(TAG, "Aguardando resposta da API e navegação para tela de Login...")
            
            var navegou = false
            for (i in 1..20) {
                Thread.sleep(2000)
                try {
                    onView(withId(R.id.txt_ir_para_cadastro)).check(matches(isDisplayed()))
                    navegou = true
                    break
                } catch (e: Throwable) {
                    Log.d(TAG, "Tentativa $i: Ainda processando cadastro ou aguardando transição...")
                }
            }

            if (!navegou) throw RuntimeException("ERRO: O aplicativo não navegou para a tela de Login. Email tentado: $emailDinamico. Verifique se os dados únicos foram aceitos pela API.")

            Log.d(TAG, "--- RESULTADO: SUCESSO - Cadastro realizado com dados inéditos ---")
        } catch (e: Throwable) {
            Log.e(TAG, "--- RESULTADO: FALHA - ${e.message} ---")
            throw e
        }
    }
}
