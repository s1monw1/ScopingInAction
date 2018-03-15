package de.swirtz.demo.scoping

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.swirtz.kotlin.scoping.Contributor
import de.swirtz.kotlin.scoping.ENDPOINT
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory


object GitHubApiCallerNextGen {
    private val LOG = LoggerFactory.getLogger("GitHubApiClientScoping-Logger")
    private val client = OkHttpClient()
    private var cachedLeadResults = mutableMapOf<String, Contributor>()
    private val mapper = jacksonObjectMapper()

    @Synchronized
    fun getKotlinContributor(name: String): Contributor {
        return cachedLeadResults[name]?.also {
            LOG.debug("return cached: $it")
        } ?: requestContributor(name)
    }

    private fun requestContributor(name: String): Contributor {
        val contributors =
            with(client) {
                val response =
                    Request.Builder().url(ENDPOINT).build().let { newCall(it).execute() }

                val responseAsString = response.use {
                    it.body()?.source()?.readByteArray()?.let { String(it) }
                            ?: throw IllegalStateException("No response from server!")
                }

                responseAsString.let {
                    LOG.debug("response from git api: $it\n")
                    mapper.readValue<Array<Contributor>>(it)
                }
            }

        return contributors.first { it.login == name }.also {
            cachedLeadResults[name] = it
            LOG.debug("found kotlin contributor: $it")
        }
    }

}
