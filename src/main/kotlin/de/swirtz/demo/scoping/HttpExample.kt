package de.swirtz.kotlin.scoping

import com.google.gson.Gson
import de.swirtz.demo.scoping.GitHubApiCallerNextGen
import okhttp3.OkHttpClient
import okhttp3.Request

const val ENDPOINT = "https://api.github.com/repos/jetbrains/kotlin/contributors"

data class Contributor(val login: String, val contributions: Int)

object GitHubApiCaller {
    private val client = OkHttpClient()

    private var cachedLeadResult: Contributor? = null

    @Synchronized
    fun getKotlinContributor(name: String): Contributor {
        if (cachedLeadResult != null) {
            println("return cached: $cachedLeadResult")
            return cachedLeadResult as Contributor
        }
        val request = Request.Builder().url(ENDPOINT).build()

        val response = client.newCall(request).execute()

        val responseAsString = response.use {
            val responseBytes = it.body()?.source()?.readByteArray()
            if (responseBytes != null) {
                String(responseBytes)
            } else throw IllegalStateException("No response from server!")
        }

        println("response from git api: $responseAsString\n")

        val contributors =
            Gson().fromJson(responseAsString, Array<Contributor>::class.java)


        val match = contributors.first { it.login == name }
        this.cachedLeadResult = match
        println("found kotlin contributor: $match")
        return match
    }
}

fun main(args: Array<String>) {
    GitHubApiCaller.getKotlinContributor("abreslav")
    GitHubApiCallerNextGen.getKotlinContributor("abreslav")
}
