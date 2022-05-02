package com.psw.quick.Github_compose.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.psw.quick.Github_compose.api.IORoutine
import com.psw.quick.Github_compose.datasource.Api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

@SuppressLint("StaticFieldLeak")
class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UIState>(UIState.Blank)

    // 일반적으로 외부전용으로는 읽기전용으로 StateFlow를 사용한다.
    val uiState: StateFlow<UIState> = _uiState

    init {
        //getUserInfo()
    }

    fun initUserInfo(){
        _uiState.value = UIState.Blank
    }

    fun getUserInfo() {
        _uiState.value = UIState.Loading
        IORoutine({
            val response = Api.github.getUser("vintageappmaker")
            _uiState.value = UIState.Loaded(
                GithubAccountUI(
                    login = response.login,
                    public_repos = response.public_repos,
                    public_gists = response.public_gists,
                    followers    = response.followers,
                    following    = response.following,
                    bio          = response.bio,
                    avatar_url   = response.avatar_url
                )
            )
        }, {
            e ->
            if (e is HttpException) {
                when (e.code()){
                    500 -> {
                        UIState.Error(
                            "500 Error"
                        )}

                    else -> {
                        UIState.Error(
                            "${e.code()} Error"
                        )
                    }
                }

            } else {
                UIState.Error(
                    "Unknown Error"
                )
            }
        })

    }

    // UI 상태
    sealed class UIState {
        object Blank : UIState()
        object Loading : UIState()
        class Loaded(val data: GithubAccountUI) : UIState()
        class Error(val message: String) : UIState()
    }

    // UI에서 보여줄 사용자 data
    data class GithubAccountUI(
        var login        : String,
        var public_repos : Int,
        var public_gists : Int,
        var followers    : Int,
        var following    : Int,
        var bio          : String,
        var avatar_url   : String
    )

}