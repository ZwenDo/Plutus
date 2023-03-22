package fr.uge.plutus.frontend.component.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.uge.plutus.R
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.store.initGlobalState
import fr.uge.plutus.frontend.view.View
import fr.uge.plutus.ui.theme.Purple200
import fr.uge.plutus.ui.theme.Purple500


@Composable
fun Navbar() {
    val globalState = globalState()

    Row(
        Modifier
            .background(Purple500)
            .padding(8.dp)
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavbarButton(
            icon = R.drawable.bullet_list,
            isActive = globalState.currentView == View.BOOK_SELECTION,
            disabled = false
        ) {
            globalState.currentView = View.BOOK_SELECTION
        }

        NavbarButton(
            icon = R.drawable.book,
            isActive = globalState.currentView == View.BOOK_OVERVIEW,
            disabled = globalState.currentBook == null
        ) {
            globalState.currentView = View.BOOK_OVERVIEW
        }

        NavbarButton(
            icon = R.drawable.attach_money,
            isActive = globalState.currentView == View.TRANSACTION_LIST,
            disabled = globalState.currentBook == null
        ) {
            globalState.currentView = View.TRANSACTION_LIST
        }
    }
}

@Composable
fun NavbarButton(icon: Int, isActive: Boolean, disabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20))
            .aspectRatio(1f)
            .fillMaxHeight()
            .background(when {
                disabled -> Color.Gray
                isActive -> Purple200
                else -> Color.White
            })
            .clickable(onClick = if (disabled) ({}) else onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(icon),
            modifier = Modifier.fillMaxSize(),
            tint = when {
                disabled -> Color.White.copy(alpha = .5f)
                isActive -> Color.White
                else -> Purple500
            },
            contentDescription = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavbarPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        initGlobalState().currentView = View.BOOK_SELECTION
        Column(Modifier
            .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Navbar()
        }
    }
}