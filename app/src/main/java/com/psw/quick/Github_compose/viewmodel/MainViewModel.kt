package com.psw.quick.Github_compose.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.psw.quick.Github_compose.api.IORoutine
import com.psw.quick.Github_compose.api.data.GithubData
import com.psw.quick.Github_compose.api.data.Repo
import com.psw.quick.Github_compose.datasource.Api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.Path

@SuppressLint("StaticFieldLeak")
class MainViewModel : ViewModel() {

    var bLoading : MutableLiveData<Boolean> =  MutableLiveData()
    var account  : MutableLiveData<String> =  MutableLiveData()
    var title    : MutableLiveData<String> =  MutableLiveData()
    var lst      : MutableLiveData<List<GithubData>> = MutableLiveData()

    var message   : MutableLiveData<String> = MutableLiveData()

    val FIRST_PAGE  =  1
    var nNextPage   =  FIRST_PAGE
    val IS_END_PAGE = -1 // -1이면 end

    // repo 갯수
    var totalCount = 0

    private val _uiState = MutableStateFlow<UIState>(UIState.Blank)

    // 일반적으로 외부전용으로는 읽기전용으로 StateFlow를 사용한다.
    val uiState: StateFlow<UIState> = _uiState

    init {
        //getUserInfo()
    }

    fun initUserInfo(){
        _uiState.value = UIState.Blank
    }

    fun getUserInfo(name : String ) {
        _uiState.value = UIState.Loading
        IORoutine({

            val listRepo: MutableList<GithubData> = Api.github.listRepos(name) as MutableList<GithubData>
            val response = Api.github.getUser(name)

            listRepo.add(0, response)

            _uiState.value = UIState.Loaded(
                listRepo
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

    fun loadUserInfo() {
        nNextPage = FIRST_PAGE
        bLoading.postValue(true)

        // 코투틴과 Retrofit 사용방법을 위한 예제
        // UI처리는 반드시 LiveData로 보낸다.
        // 그렇게 하지않으면 Context간의 차이로 App이 종료됨
        IORoutine({
            val u = Api.github.getUser(account.value.toString())
            if(u == null) return@IORoutine

            // 데이터처리
            var items = mutableListOf<GithubData>().apply{
                add(u as GithubData)
            }

            totalCount = u.public_repos

            // UI에 전송
            lst.postValue(items)
        }, {
            bLoading.postValue(false)
            message.postValue("$it")
        })

    }

    fun loadRepoInfo() {
        nNextPage = FIRST_PAGE
        bLoading.postValue(true)

        IORoutine({
            val l = Api.github.listRepos(account.value.toString())
            if(l == null){
                bLoading.postValue(false)
                toNextPageWithEnd(true)
                return@IORoutine
            }

            if(l.size < 1){
                bLoading.postValue(false)
                toNextPageWithEnd(true)
                return@IORoutine
            }

            toNextPageWithEnd()

            l?.forEachIndexed { index, repo ->
                repo.name = "${repo.name}"
            }

            lst.postValue(l)
            title.postValue("$totalCount repositories")
            bLoading.postValue(false)

        }, {
            bLoading.postValue(false)
            message.postValue("$it")
        })

    }

    fun loadRepoInfoWithPage() {
        bLoading.postValue(true)

        // 코루틴을 사용하지 않는방법
        Api.github.listReposWithPage(account.value.toString(), nNextPage).enqueue( object:
            Callback<List<Repo>> {

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                bLoading.postValue(false)
            }

            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                val repos = response.body()

                if(repos == null){
                    bLoading.postValue(false)
                    toNextPageWithEnd(true)
                    return
                }

                if(repos.size < 1){
                    bLoading.postValue(false)
                    toNextPageWithEnd(true)
                    return
                }

                toNextPageWithEnd()

                lst.postValue(repos)

                repos?.let{
                    it.forEachIndexed { index, repo ->   repo.name = "${repo.name}" }
                }

                title.postValue("${totalCount} repositories")
                bLoading.postValue(false)

            }

        })
    }

    private fun toNextPageWithEnd(bIsEnd : Boolean = false ){
        if(nNextPage != IS_END_PAGE) nNextPage++
        if( bIsEnd )
            nNextPage = IS_END_PAGE
    }

    // UI 상태
    sealed class UIState {
        object Blank : UIState()
        object Loading : UIState()
        class Loaded(val data: List<GithubData>) : UIState()
        class Error(val message: String) : UIState()
    }

    // UI에서 보여줄 사용자 data
//    data class GithubAccountUI(
//        var login        : String,
//        var public_repos : Int,
//        var public_gists : Int,
//        var followers    : Int,
//        var following    : Int,
//        var bio          : String,
//        var avatar_url   : String
//    )

}