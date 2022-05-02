package com.psw.quick.Github_compose.api

import com.psw.quick.Github_compose.api.data.Repo
import com.psw.quick.Github_compose.api.data.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubService {

    // 코루틴 내에서 간편하게 사용하기 위한 suspend를 이용한 API
    @GET("/users/{user}")
    suspend fun getUser(@Path("user") user: String): User

    // 코루틴 내에서 간편하게 사용하기 위한 suspend를 이용한 API
    @GET("/users/{user}/repos")
    suspend fun listRepos(@Path("user") user: String): List<Repo>

    // 일반적인 API
    @GET("/users/{user}/repos")
    fun listReposWithPage(@Path("user") user: String, @Query("page") page : Int): Call<List<Repo>>

}

fun IORoutine(fnProcess: suspend CoroutineScope.() -> Unit, fnError : suspend CoroutineScope.(e :Exception)->Unit){
    CoroutineScope(Dispatchers.IO).launch {
        try{
            fnProcess()
        }
        catch (e: Exception){
            fnError(e)
        }
    }
}