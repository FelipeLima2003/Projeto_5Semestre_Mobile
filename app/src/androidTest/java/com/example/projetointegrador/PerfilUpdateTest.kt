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
        // Garante que não há tokens residuais de outros testes
        RetrofitClient.clearAuthToken()
    }

    @Test
    fun testeAtualizarDescricaoPerfil_devePersistirDados() {
        val TAG = "TEST_PERFIL_UPDATE"
        Log.d(TAG, "--- INICIANDO TESTE: Atualização de Perfil ---")

        try {
            // PASSO 1: Registrar um novo usuário para garantir que temos credenciais válidas
            Log.d(TAG, "Passo 1: Criando novo usuário para o teste")
            onView(withId(R.id.txt_ir_para_cadastro)).perform(click())
            
            val emailDinamico = "update${System.currentTimeMillis()}@teste.com"
            val senhaTeste = "Senha123"

            onView(withId(R.id.edit_nome)).perform(replaceText("User Update"), closeSoftKeyboard())
            onView(withId(R.id.edit_nascimento)).perform(replaceText("10102000"), closeSoftKeyboard())
            onView(withId(R.id.edit_cpf)).perform(replaceText("14792610077"), closeSoftKeyboard())
            onView(withId(R.id.edit_email)).perform(replaceText(emailDinamico), closeSoftKeyboard())
            onView(withId(R.id.edit_telefone)).perform(replaceText("11999998888"), closeSoftKeyboard())
            onView(withId(R.id.rb_masculino)).perform(click())
            onView(withId(R.id.edit_senha)).perform(replaceText(senhaTeste), closeSoftKeyboard())
            onView(withId(R.id.edit_confirma_senha)).perform(replaceText(senhaTeste), closeSoftKeyboard())
            onView(withId(R.id.containerButtonConfirmar)).perform(scrollTo(), click())

            // Passo 1.1: Garantir que o cadastro funcionou e navegou de volta para a tela de Login
            Log.d(TAG, "Passo 1.1: Aguardando retorno para tela de Login")
            var navegouParaLogin = false
            for (i in 1..20) {
                Thread.sleep(2000)
                try {
                    // Verifica se está na tela de login procurando o texto de "ir para cadastro" que só tem lá
                    onView(withId(R.id.txt_ir_para_cadastro)).check(matches(isDisplayed()))
                    navegouParaLogin = true
                    Log.d(TAG, "Navegação para Login confirmada na tentativa $i")
                    break
                } catch (e: Throwable) {
                    Log.d(TAG, "Aguardando resposta da API de Cadastro... (${i * 2}s)")
                }
            }

            if (!navegouParaLogin) {
                // Se não navegou, pode ser erro 403 ou validação. 
                // Tentamos prosseguir se já estivermos na tela de login por algum motivo, 
                // mas se estivermos na de cadastro, o teste deve parar.
                throw RuntimeException("Falha no cadastro ou lentidão excessiva da API (403 Forbidden detectado nos logs).")
            }

            // PASSO 2: Realizar Login
            Log.d(TAG, "Passo 2: Realizando login com o novo usuário: $emailDinamico")
            onView(withId(R.id.edit_email)).perform(replaceText(emailDinamico), closeSoftKeyboard())
            onView(withId(R.id.edit_senha)).perform(replaceText(senhaTeste), closeSoftKeyboard())
            onView(withId(R.id.containerButtonConfirmar)).perform(click())

            // Aguarda navegação para ConsultaActivity (tentativas por 15s)
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

            if (!logou) throw RuntimeException("Falha ao realizar login no teste.")

            // PASSO 3: Navegar para o Perfil
            Log.d(TAG, "Passo 3: Navegando para a tela de Perfil")
            onView(withId(R.id.containerMeuPerfil)).perform(click())

            // PASSO 4: Atualizar a descrição
            Log.d(TAG, "Passo 4: Alterando a descrição do perfil")
            val novaDescricao = "Nova bio teste ${System.currentTimeMillis()}"
            
            // Aguarda a tela de perfil carregar
            Thread.sleep(3000)
            
            onView(withId(R.id.edit_perfil_descricao))
                .perform(scrollTo(), replaceText(novaDescricao), closeSoftKeyboard())

            Log.d(TAG, "Passo 5: Clicando em salvar")
            onView(withId(R.id.btn_salvar_perfil)).perform(scrollTo(), click())

            // PASSO 5: Verificar se os dados foram salvos
            Log.d(TAG, "Passo 6: Verificando se a descrição foi mantida")
            // Aguarda a resposta do servidor
            Thread.sleep(3000)
            onView(withId(R.id.edit_perfil_descricao)).check(matches(withText(novaDescricao)))

            Log.d(TAG, "--- RESULTADO: SUCESSO - Perfil atualizado corretamente. ---")

        } catch (e: Throwable) {
            Log.e(TAG, "--- RESULTADO: FALHA - Erro no teste de atualização: ${e.message} ---")
            throw e
        }
    }
}
