package com.example.projetointegrador

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServiceIntegrationTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    // 1. Cadastro de usuário válido
    @Test
    fun `teste cadastro de usuario valido retorna sucesso`() = runBlocking {
        // Prepara a resposta simulada da API (HTTP 200 OK)
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("""{"id": 1, "mensagem": "Sucesso"}""")
        mockWebServer.enqueue(mockResponse)

        val request = CadastroRequest("Gustavo", "01/01/2000", "123.456.789-00", "teste@teste.com", "11999999999", Genero.MASCULINO, "senha123", null)

        val response = apiService.cadastrar(request)

        assertTrue(response.isSuccessful)
        assertEquals(200, response.code())
    }

    // 2. Cadastro com email duplicado
    @Test
    fun `teste cadastro com email duplicado retorna erro 409`() = runBlocking {
        // Prepara a resposta simulada da API indicando conflito (HTTP 409)
        val mockResponse = MockResponse()
            .setResponseCode(409)
            .setBody("""{"erro": "Email já cadastrado"}""")
        mockWebServer.enqueue(mockResponse)

        val request = CadastroRequest("Gustavo", "01/01/2000", "123.456.789-00", "duplicado@teste.com", "11999999999", Genero.MASCULINO, "senha123", null)

        val response = apiService.cadastrar(request)

        assertFalse(response.isSuccessful)
        assertEquals(409, response.code())
    }

    // 3. Atualizar dados Válidos
    @Test
    fun `teste atualizar descricao retorna sucesso`() = runBlocking {
        // Prepara a resposta de sucesso para PUT (HTTP 200 ou 204)
        val mockResponse = MockResponse()
            .setResponseCode(200)
        mockWebServer.enqueue(mockResponse)

        val request = UpdateDescricaoRequest("Nova descrição do perfil")
        val response = apiService.updateDescricao(1, request)

        assertTrue(response.isSuccessful)
    }

    // 4. Desempenho (Tempo de resposta da API)
    @Test
    fun `teste desempenho da chamada de usuarios`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val startTime = System.currentTimeMillis()
        apiService.getUsuarios()
        val endTime = System.currentTimeMillis()

        val duration = endTime - startTime
        // Afirma que a requisição (mesmo mockada) leva menos de 500ms
        assertTrue("A requisição demorou muito: ${duration}ms", duration < 500)
    }
}