package com.psw.quick.Github_compose.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.psw.quick.Github_compose.api.IORoutine
import com.psw.quick.Github_compose.api.data.GithubData
import com.psw.quick.Github_compose.api.data.Repo
import com.psw.quick.Github_compose.datasource.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

@SuppressLint("StaticFieldLeak")
class MainViewModel : ViewModel() {

    val FIRST_PAGE  =  1
    var nNextPage   =  FIRST_PAGE
    val IS_END_PAGE = -1 // -1이면 end

    // 통신관련
    // 일반적으로 외부전용으로는 읽기전용으로 StateFlow를 사용한다.
    private val _uiState = MutableStateFlow<UIState>(UIState.Idle)
    val uiState: StateFlow<UIState> = _uiState

    // 화면관련
    val bProgress = MutableStateFlow(false)

    // 이전검색 사용자 이름
    var sUserName = ""

    // 누적된 list
    lateinit var lstMain : MutableList<GithubData>

    init {
        //getUserInfo()
    }

    fun initUserInfo(){
        _uiState.value = UIState.Idle
    }

    fun getUserInfo(name : String ) {
        _uiState.value = UIState.Loading
        bProgress.value = true

        nNextPage = FIRST_PAGE

        // 처음 검색한 이름 저장
        sUserName = name
        IORoutine({

            val listRepo: MutableList<GithubData> = Api.github.listRepos(name) as MutableList<GithubData>
            val userInfo = Api.github.getUser(name)

            // 레포지토리 전체개수 정보
            userInfo.git_count = listRepo.size

            listRepo.add(0, userInfo)

            // 누적한다.
            lstMain = listRepo

            _uiState.value = UIState.Loaded(
                listRepo
            )

            bProgress.value = false


        }, {
            e ->

            bProgress.value = false

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


    fun loadRepoInfoWithPage() {

        bProgress.value = true

        Api.github.listReposWithPage(sUserName, nNextPage++).enqueue( object:

            Callback<List<Repo>> {

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                UIState.Error(
                    "Unknown Error"
                )

                bProgress.value = false
            }

            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                val repos = response.body()

                if(repos == null){
                    toNextPageWithEnd(true)
                    return
                }

                if(repos.size < 1){
                    toNextPageWithEnd(true)
                    return
                }

                toNextPageWithEnd()

                repos?.let{
                    it.forEachIndexed { index, repo ->   repo.name = "${repo.name}" }
                }

                lstMain.addAll(repos)

                _uiState.value = UIState.Loaded(
                    lstMain
                )

                bProgress.value = false

            }

        })
    }

    private fun toNextPageWithEnd(bIsEnd : Boolean = false ){
        if(nNextPage != IS_END_PAGE) nNextPage++
        if( bIsEnd ){
            nNextPage = IS_END_PAGE
            bProgress.value = false
        }
    }

    // UI 상태
    sealed class UIState {
        object Idle : UIState()
        object Loading : UIState()
        class Loaded(val data: List<GithubData>) : UIState()
        class Error(val message: String) : UIState()
    }

}