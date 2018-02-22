package de.swirtz.demo.scoping

import com.google.gson.Gson
import de.swirtz.kotlin.scoping.Contributor
import de.swirtz.kotlin.scoping.ENDPOINT
import okhttp3.OkHttpClient
import okhttp3.Request

object GitHubApiCallerNextGen {
    private var cachedLeadResult: Contributor? = null

    @Synchronized
    fun getKotlinContributor(name: String): Contributor {
        return cachedLeadResult?.also {
            println("return cached: $it")
        } ?: requestContributor(name)
    }

    private fun requestContributor(name: String): Contributor {
        val contributors =
            with(OkHttpClient()) {
                val response = Request.Builder().url(ENDPOINT).build().let { newCall(it).execute() }

                val responseAsString = response.use {
                    it.body()?.source()?.readByteArray()?.let { String(it) }
                            ?: throw IllegalStateException("No response from server!")
                }

                responseAsString.let {
                    println("response from git api: $it\n")
                    Gson().fromJson(it, Array<Contributor>::class.java)
                }
            }

        return contributors.first { it.login == name }.also {
            cachedLeadResult = it
            println("found kotlin contributor: $it")
        }

    }

}
