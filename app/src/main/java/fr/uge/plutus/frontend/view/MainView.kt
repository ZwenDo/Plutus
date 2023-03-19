package fr.uge.plutus.frontend.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.uge.plutus.frontend.store.GlobalState
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.store.initGlobalState
import fr.uge.plutus.frontend.view.book.BookCreationView
import fr.uge.plutus.frontend.view.book.BookSelectionView
import fr.uge.plutus.frontend.view.transaction.DisplayTransactions
import fr.uge.plutus.frontend.view.transaction.TransactionCreationView

private enum class View {
    BOOK_CREATION,
    BOOK_SELECTION,

    TRANSACTION_CREATION,
    TRANSACTION_LIST,
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainView() {
    val globalState = initGlobalState()
    var currentView by remember { mutableStateOf(View.BOOK_CREATION) }

    when (currentView) {
        View.BOOK_CREATION -> BookCreationView {
            currentView = View.BOOK_SELECTION
        }
        View.BOOK_SELECTION -> BookSelectionView { book ->
            globalState.currentBook = book
            currentView = View.TRANSACTION_CREATION
        }

        View.TRANSACTION_CREATION -> TransactionCreationView {
            currentView = View.TRANSACTION_LIST
        }
        View.TRANSACTION_LIST -> DisplayTransactions {
            currentView = View.TRANSACTION_CREATION
        }
    }
}
