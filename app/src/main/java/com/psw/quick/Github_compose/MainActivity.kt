package com.psw.quick.Github_compose

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.psw.quick.Github_compose.api.data.GithubData
import com.psw.quick.Github_compose.api.data.Repo
import com.psw.quick.Github_compose.api.data.User
import com.psw.quick.Github_compose.ui.theme.*
import com.psw.quick.Github_compose.viewmodel.MainViewModel
import com.skydoves.landscapist.glide.GlideImage

class MainActivity : ComponentActivity() {

    var count : Int = 0

    lateinit var vModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vModel = ViewModelProvider(this).get(MainViewModel::class.java)
        setUpUI()
    }

    @Composable
    private fun setSystemBarColor() {
        val systemUiController = rememberSystemUiController()
        if (isSystemInDarkTheme()) {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent
            )
        } else {
            systemUiController.setSystemBarsColor(
                color = MaterialTheme.colors.bottomNaviBackgroundcolor()
            )
        }
    }

    private fun setUpUI() {
        setContent {
            setSystemBarColor()

            GithubComposeTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ){
                    mainView(context = this, vModel = vModel)
                }
            }
            //vModel.getUserInfo()
        }
    }
}



@Composable
private fun mainView(
    context: Context,
    vModel: MainViewModel
) {
    BackHandler {
        (context as MainActivity)?.apply {
            finish()
        }
    } // onBackPressed

    // 초기화
    vModel.initUserInfo()

    // UI
    githubListView()

    // 통신
    vModel.getUserInfo("vintageappmaker")
}


@Composable
fun mainListUI(fnView : LazyListScope.()-> Unit){

    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    val act = ( context as MainActivity )

    var cnt =  remember {
        act.count
    }

    scrollState.OnBottomReached {
        Toast.makeText(context, "LazyColumn end ${cnt++}", Toast.LENGTH_LONG).show()
    }

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentPadding = PaddingValues(16.dp),
        state = scrollState

    ) {

        fnView()
    }
}

@Composable
fun getMainViewModel () : MainViewModel {
    val context = LocalContext.current
    val vModel = ( context as MainActivity).vModel
    return vModel
}

@Composable
fun githubListView() {
    val vModel = getMainViewModel()
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colors.bottomNaviBackgroundcolor()),
        verticalArrangement = Arrangement.Center

    ) {

        // 통신상태 변경
        when ( val rst = vModel.uiState.collectAsState().value) {
            is MainViewModel.UIState.Loaded ->{
                makeHeader()
                Spacer(modifier = Modifier.height(26.dp))
                makeGithubList(lst = rst.data)
            }

            else -> {
                // 전체크기의 글자 중앙정렬시
                fullCenterTextView("통신중...")
            }
        }

    }

}

@Composable
private fun fullCenterTextView(s : String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            s,
            fontSize = dpToSp(dp = 30.dp),
            style = TextStyle(textAlign = TextAlign.Center)
        )
    }
}

@Composable
private fun makeGithubList(lst : List<GithubData>) {
    mainListUI {
        items(lst){ data ->
            when(data){
                is User -> {
                    makeUserCard(data)
                }

                is Repo ->{
                    makeRepoCard(data)
                }
            }
        }

        item{
            Spacer(modifier = Modifier.padding(30.dp))
        }
    }
}

@Composable
private fun makeUserCard(data : User) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = MaterialTheme.shapes.medium,
        elevation = 5.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 25.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            GlideImage(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .width(80.dp)
                    .height(80.dp)
                    .clip(CircleShape),
                imageModel = "${data.avatar_url}",
                contentScale = ContentScale.Crop)

            Text("${data.bio}",
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(start = 25.dp),
                style = MaterialTheme.typography.body1
            )
        }
    }
}

private fun makeStar(n : Int) : String{
    if (n == 0) return "\uD83D\uDE36"
    if (n >  0) return "⭐ X ${n}"
    var s = ""
    (1..n).forEach { s = s + "⭐" }
    return s
}
@Composable
private fun makeRepoCard(data : Repo) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        shape = RoundedCornerShape(12.dp), //MaterialTheme.shapes.medium,
        elevation = 5.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 25.dp),
            ) {

            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()) {
                Text(
                    "■️  이름",
                    Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)
                        .padding(start = 16.dp),
                    style = MaterialTheme.typography.body1
                )

                Text(
                    text = "${data.name}",
                    Modifier
                        .padding(end = 16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically),
                    style = TextStyle(fontSize = 26.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()) {

                Text(
                    "■️ ️ 설명",
                    Modifier
                        .width(100.dp)
                        .padding(start = 16.dp),
                    style = MaterialTheme.typography.body1
                )

                Text(
                    "${data.description ?: "없음"}",
                    Modifier.padding(end = 16.dp),
                    style = MaterialTheme.typography.body1
                )
            }
            
            Spacer(modifier = Modifier.height(25.dp))

            Row(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()) {

                Text(
                    "■️  기타",
                    Modifier
                        .width(100.dp)
                        .padding(start = 16.dp),
                    style = MaterialTheme.typography.body1
                )

                Column() {
                    Row() {
                        Text(
                            "star",
                            Modifier
                                .width(50.dp),
                            style = MaterialTheme.typography.body1
                        )

                        Text(
                            "${makeStar( data.stargazers_count)} ",
                            Modifier
                                .padding(end = 16.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.body1
                        )
                    }

                    Row() {
                        Text(
                            "size",
                            Modifier
                                .width(50.dp),
                            style = MaterialTheme.typography.body1
                        )

                        Text(
                            "${data.size}kb",
                            Modifier
                                .padding(end = 16.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.body1
                        )
                    }

                }
            }
        }

    }
}


@Composable
private fun makeHeader() {

    Box(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colors.headerBackgroundcolor()
            )
            .clickable(onClick = {

            })
    ) {

        Text(
            text = "Header",
            color = MaterialTheme.colors.headerForegroundcolor(),
            fontSize = dpToSp(dp = 30.dp),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(14.dp)
        )
    }
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, name = "Light mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark mode")
@Composable
fun DefaultPreview() {

    val vModel = getMainViewModel()
    GithubComposeTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ){
            mainView(context = LocalContext.current, vModel = vModel)
        }
    }
}
