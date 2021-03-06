package com.psw.quick.Github_compose.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.psw.quick.Github_compose.api.IORoutine
import com.psw.quick.Github_compose.api.data.GithubData
import com.psw.quick.Github_compose.api.data.Repo
import com.psw.quick.Github_compose.datasource.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
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
    private val _uiState = MutableStateFlow<UIState>(UIState.Nodata)
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
        _uiState.value = UIState.Nodata
    }

    fun getUserInfo(name : String ) {
        _uiState.value = UIState.Loading
        bProgress.value = true

        nNextPage = FIRST_PAGE

        // 처음 검색한 이름 저장
        sUserName = name
        IORoutine({

            val listRepo: MutableList<GithubData> = Api.github.listRepos(name) as MutableList<GithubData>
            listRepo?.let{
                it.forEachIndexed { index, repo ->
                    if(repo is Repo) repo.name = "${index}.${repo.name}"
                }
            }
            val userInfo = Api.github.getUser(name)
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
                    404 -> {
                        // 사용자 정보가 없을 경우
                        val body = e.response()?.errorBody()?.string()
                        val jsonObject = JSONObject(body)
                        var msg = jsonObject.optString("message")
                        if (msg == "Not Found") _uiState.value = UIState.Nodata
                    }
                    else -> {
                        // 정의되지 않은 에러처리
                        val message = e.response()?.errorBody()?.string() ?: "unknwon"
                        val str = "${e.code()} : $message"
                        _uiState.value = UIState.Error(
                            str
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

        Api.github.listReposWithPage(sUserName, ++nNextPage).enqueue( object:

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
                    it.forEachIndexed { index, repo ->   repo.name = "${lstMain.size -1  + index}.${repo.name}" }
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
        object Nodata : UIState()
        object Loading : UIState()
        class Loaded(val data: List<GithubData>) : UIState()
        class Error(val message: String) : UIState()
    }

}