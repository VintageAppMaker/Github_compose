package com.psw.quick.Github_compose

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.collect

// lazy Coloumn의 하단스크롤 이벤트
@Composable
fun LazyListState.OnBottomReached(
    onLoadMore  : () -> Unit
){
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                ?: return@derivedStateOf true

            lastVisibleItem.index == layoutInfo.totalItemsCount - 1
        }
    }

    // LaunchedEffect는 값이 변경되면 호출되는 코루틴
    // 코루틴 관련 suspend 함수를 쓸 떄 사용해야 함
    LaunchedEffect(shouldLoadMore){
        snapshotFlow { shouldLoadMore.value }
            .collect {
                if (it) onLoadMore()
            }

    }
}

// compose에서는 TextStyle에 sp만 사용한다.
// 문제는 사용자가 폰트크기를 변경해도 바뀌지 말아야 할 경우
// dp를 사용해야 한다는 것이다.
// 그럴 때 사용하는 함수
@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }

