package fr.uge.plutus.frontend.view

import androidx.compose.runtime.Composable
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.book.BookCreationView
import fr.uge.plutus.frontend.view.book.BookSelectionView
import fr.uge.plutus.frontend.view.transaction.DisplayTransactions
import fr.uge.plutus.frontend.view.transaction.TransactionCreationView

enum class View {
    BOOK_CREATION,
    BOOK_SELECTION,

    TRANSACTION_CREATION,
    TRANSACTION_LIST,
}

@Composable
fun MainView() {
    val globalState = globalState()

    when (globalState.currentView) {
        View.BOOK_CREATION -> BookCreationView {
            globalState.currentView = View.BOOK_SELECTION
        }
        View.BOOK_SELECTION -> BookSelectionView { book ->
            globalState.currentBook = book
            globalState.currentView = View.TRANSACTION_CREATION
        }

        View.TRANSACTION_CREATION -> TransactionCreationView {
            globalState.currentView = View.TRANSACTION_LIST
            globalState.currentTransaction = null
        }
        View.TRANSACTION_LIST -> DisplayTransactions {
            globalState.currentView = View.TRANSACTION_CREATION
        }
    }
}
