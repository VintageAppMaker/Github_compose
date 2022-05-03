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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.psw.quick.Github_compose.ui.theme.*
import com.psw.quick.Github_compose.viewmodel.MainViewModel

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

            val navController = rememberNavController()

            GithubComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController) }) {
                    Navigation(navController = navController)
                }
            }

            //vModel.getUserInfo()

        }

    }
}

@Composable
fun Navigation(navController: NavHostController) {
    val vModel = getMainViewModel()
    val context = LocalContext.current

    NavHost(navController, startDestination = NavigationItem.tab1.route) {
        composable(NavigationItem.tab1.route) {
            setUpTab1(context, vModel)
        }
        composable(NavigationItem.tab2.route) {
            setUpTab2(navController, vModel)
        }
        composable(NavigationItem.tab3.route) {
            setUpTab3(navController, vModel)
        }
    }
}

@Composable
private fun setUpTab3(
    navController: NavHostController,
    vModel: MainViewModel
) {
    BackHandler {
        navController.navigate(NavigationItem.tab2.route) {
            popUpTo(0) {
                inclusive = true
            }
        }
    } // onBackPressed

    vModel.initUserInfo()
    tab3UI()
}

@Composable
private fun setUpTab2(
    navController: NavHostController,
    vModel: MainViewModel
) {
    BackHandler {
        navController.navigate(NavigationItem.tab1.route) {
            popUpTo(0) {
                inclusive = true
            }
        }
    } // onBackPressed

    vModel.initUserInfo()
    tab2UI()
}

@Composable
private fun setUpTab1(
    context: Context,
    vModel: MainViewModel
) {
    BackHandler {
        (context as MainActivity)?.apply {
            finish()
        }
    } // onBackPressed

    vModel.initUserInfo()
    tab1UI()
}


@Composable
fun mainListUI(fnView : LazyListScope.()-> Unit){

    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    val act = ( context as MainActivity )

    var cnt =  remember {
        act.count
    }

    // call the extension function
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
fun tab1UI() {
    val vModel = getMainViewModel()
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colors.bottomNaviBackgroundcolor()),
        verticalArrangement = Arrangement.Center

    ) {
        makeHeaderUI()
        Spacer(modifier = Modifier.height(26.dp))

        when ( val rst = vModel.uiState.collectAsState().value) {
            is MainViewModel.UIState.Loaded ->{
                Text(
                    text = rst.data.toString(),
                    color = MaterialTheme.colors.headerForegroundcolor(),
                    fontSize = dpToSp(dp = 30.dp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(14.dp)
                )
            }
        }

        makeListUI()
    }

}

@Composable
fun tab2UI() {
    val vModel = getMainViewModel()
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Green),
        verticalArrangement = Arrangement.Center

    ) {
        makeHeaderUI()
        Spacer(modifier = Modifier.height(26.dp))

        when ( val rst = vModel.uiState.collectAsState().value) {
            is MainViewModel.UIState.Loaded ->{
                Text(
                    text = rst.data.toString(),
                    color = MaterialTheme.colors.headerForegroundcolor(),
                    fontSize = dpToSp(dp = 30.dp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(14.dp)
                )
            }
        }
        makeListUI()
    }

}

@Composable
fun tab3UI() {
    val vModel = getMainViewModel()

    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.Magenta),
        verticalArrangement = Arrangement.Center

    ) {
        makeHeaderUI()
        Spacer(modifier = Modifier.height(26.dp))

        when ( val rst = vModel.uiState.collectAsState().value) {
            is MainViewModel.UIState.Loaded ->{
                Text(
                    text = rst.data.toString(),
                    color = MaterialTheme.colors.headerForegroundcolor(),
                    fontSize = dpToSp(dp = 30.dp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(14.dp)
                )
            }
        }
        makeListUI()
    }

}

@Composable
private fun makeListUI() {
    val lst : MutableList<Int> = mutableListOf()
    (0..10).forEach { lst.add(it) }
    mainListUI {
        items(lst){
            number ->
            Card(
                modifier = Modifier
                    // The space between each card and the other
                    .padding(10.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.medium,
                elevation = 5.dp,
                backgroundColor = MaterialTheme.colors.surface
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 25.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "\uD83C\uDF3F  Plants in Cosmetics #${number}",
                        style = MaterialTheme.typography.body1
                    )
                }
            }
        }

        item{
            Spacer(modifier = Modifier.padding(30.dp))
        }
    }
}
@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }

@Composable
private fun makeHeaderUI() {

    val vModel = getMainViewModel()

    Box(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colors.headerBackgroundcolor()
            )
            .clickable(onClick = {
                vModel.getUserInfo()
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

    val navController = rememberNavController()

    GithubComposeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.bottomNaviBackgroundcolor()
        ) {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }) {
                Navigation(navController = navController)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem.tab1,
        NavigationItem.tab2,
        NavigationItem.tab3
    )

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.bottomNaviBackgroundcolor(),
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val navPrevStackEntry =  navController.previousBackStackEntry
        navPrevStackEntry?.apply {
            val name = this.destination?.route
            name
        }
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                label = { Text(text = item.title) },
                selectedContentColor   = MaterialTheme.colors.bottomNaviForegroundcolor(),
                unselectedContentColor = MaterialTheme.colors.bottomNaviForegroundcolor().copy(0.4f),
                alwaysShowLabel = true,
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object tab1 : NavigationItem("tab1", android.R.drawable.ic_menu_camera ,  "tab1")
    object tab2 : NavigationItem("tab2", android.R.drawable.ic_btn_speak_now, "tab2")
    object tab3 : NavigationItem("tab3", android.R.drawable.ic_input_add,      "tab3")
}