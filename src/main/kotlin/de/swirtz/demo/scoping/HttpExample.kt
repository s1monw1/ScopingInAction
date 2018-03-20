package de.swirtz.kotlin.scoping

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.swirtz.demo.scoping.GitHubApiCallerNextGen
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory

const val ENDPOINT = "https://api.github.com/repos/jetbrains/kotlin/contributors"

@JsonIgnoreProperties(ignoreUnknown = true)
data class Contributor(val login: String, val contributions: Int)

object GitHubApiCaller {
    private val LOG = LoggerFactory.getLogger("GitHubApiClient-Logger")
    private val client = OkHttpClient()
    private var cachedLeadResults = mutableMapOf<String, Contributor>()
    private val mapper = jacksonObjectMapper()

    @Synchronized
    fun getKotlinContributor(name: String): Contributor {
        val cachedLeadResult = cachedLeadResults[name]
        if (cachedLeadResult != null) {
            LOG.debug("return cached: $cachedLeadResult")
            return cachedLeadResult
        }
        val request = Request.Builder().url(ENDPOINT).build()

        val response = client.newCall(request).execute()

        val json = response.use {
            val responseBytes = it.body()?.source()?.readByteArray()
            if (responseBytes != null) {
                String(responseBytes)
            } else throw IllegalStateException("No response from server!")
        }

        LOG.debug("response from git api: $json\n")

        val contributors =
            mapper.readValue<Array<Contributor>>(json)


        val match = contributors.first { it.login == name }
        this.cachedLeadResults[name] = match
        LOG.debug("found kotlin contributor: $match")
        return match
    }
}

fun main(args: Array<String>) {
    GitHubApiCaller.getKotlinContributor("abreslav")
    GitHubApiCallerNextGen.getKotlinContributor("abreslav")
}
