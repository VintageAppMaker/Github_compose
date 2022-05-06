package com.psw.quick.Github_compose

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
                //color = Color.Transparent
                color = MaterialTheme.colors.bottomNaviBackgroundcolor()
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
        }
    }
}

@Composable
private fun CircularProgressAnimated(){
    val progressValue = 1.0f
    val infiniteTransition = rememberInfiniteTransition()

    val progressAnimationValue by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = progressValue,animationSpec = infiniteRepeatable(animation = tween(1000)))

    CircularProgressIndicator(progress = progressAnimationValue, color = progressColor)
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

    // 정보초기화
    vModel.initUserInfo()

    // UI설정
    githubListView()

    // 통신
    vModel.getUserInfo("vintageappmaker")
}


@Composable
private fun mainList(fnView : LazyListScope.()-> Unit){

    val scrollState = rememberLazyListState()
    val vModel = getMainViewModel()
    scrollState.OnBottomReached {
        if(vModel.nNextPage == vModel.IS_END_PAGE) return@OnBottomReached
        vModel.loadRepoInfoWithPage()
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
private fun getMainViewModel () : MainViewModel {
    val context = LocalContext.current
    val vModel = ( context as MainActivity).vModel
    return vModel
}

@Composable
private fun githubListView() {
    val vModel = getMainViewModel()

    // 로딩 프로그레시브 처리
    val bLoading by vModel.bProgress.collectAsState()

    // 중앙정렬
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(MaterialTheme.colors.bottomNaviBackgroundcolor()),
            verticalArrangement = Arrangement.Center

        ) {

            // 통신상태 변경
            when ( val rst = vModel.uiState.collectAsState().value) {
                // 정상완료
                is MainViewModel.UIState.Loaded ->{
                    Spacer(modifier = Modifier.height(12.dp))
                    makeHeader()
                    Spacer(modifier = Modifier.height(26.dp))
                    MakeGithubList(lst = rst.data)
                }

                // 데이터 없음
                is MainViewModel.UIState.Nodata ->{
                    Spacer(modifier = Modifier.height(12.dp))
                    makeHeader()
                    Spacer(modifier = Modifier.height(26.dp))
                    FullCenterTextView("\uD83D\uDC49 검색된 데이터가 없습니다.")
                }

                // 에러
                is MainViewModel.UIState.Error ->{
                    Spacer(modifier = Modifier.height(12.dp))
                    makeHeader()
                    Spacer(modifier = Modifier.height(26.dp))
                    FullCenterTextView("\uD83D\uDED1 ${rst.message}")
                }
            }
        }

        if (bLoading)
            CircularProgressAnimated()
    }

}

@Composable
private fun FullCenterTextView(s : String) {
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
private fun MakeGithubList(lst : List<GithubData>) {
    mainList {
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
        backgroundColor = cardBack1,
        contentColor    = cardFront1
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

            Column(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
            ){
                Text("${data.login}",
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 25.dp),
                    style = TextStyle(color = Color(0xFFFFEB3B), fontSize = dpToSp(dp = 20.dp))
                )

                Text("repositories: ${data.public_repos}",
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 25.dp),
                    style = MaterialTheme.typography.body1
                )

                Text("${data.bio}",
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 25.dp),
                    style = MaterialTheme.typography.body1
                )

                Text("followers : ${data.followers} following : ${data.following} ",
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(start = 25.dp),
                    style = MaterialTheme.typography.body1
                )

            }
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun makeRepoCard(data : Repo) {
    val context = LocalContext.current
    Card(
        onClick = {
            Intent(Intent.ACTION_VIEW, Uri.parse(data.clone_url))?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
        },
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
                            getSizeString(data.size.toLong()),
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

fun getSizeString(nSize : Long): String {
    return when {
        nSize < 1000        -> "${nSize}kb"
        nSize > 1000 * 1000 -> "${nSize / (1000 * 1000)}G"
        else                -> "${nSize /1000}mb"
    }
}

@OptIn(ExperimentalAnimationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun makeHeader() {
    val str = ""
    var searchText by remember { mutableStateOf(TextFieldValue(str, selection = TextRange(str.length))) }
    val focusRequester = remember { FocusRequester() }

    val vModel = getMainViewModel()
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(55.dp)){

        Button(modifier = Modifier.fillMaxHeight(),
            onClick = { vModel.getUserInfo(searchText.text) },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = buttonBack,
                contentColor = Color.White)
        ) {
            // 중앙정렬
            Column(
                modifier = Modifier .wrapContentWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "search",
                    fontSize = dpToSp(dp = 18.dp),
                    style = TextStyle(textAlign = TextAlign.Center)
                )
            }

        }

        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxHeight()
                .fillMaxWidth(),
            maxLines = 1,
            singleLine = true
        )

        // LaunchedEffect는 값이 변경되면 호출되는 코루틴
        LaunchedEffect(Unit) {
            //focusRequester.requestFocus()
        }
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
