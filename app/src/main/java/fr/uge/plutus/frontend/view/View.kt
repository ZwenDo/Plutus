package fr.uge.plutus.frontend.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.book.BookCreationView
import fr.uge.plutus.frontend.view.book.BookOverviewLoader
import fr.uge.plutus.frontend.view.book.BookSelectionView
import fr.uge.plutus.frontend.view.transaction.DisplayHeader
import fr.uge.plutus.frontend.view.transaction.DisplayTransactionDetails
import fr.uge.plutus.frontend.view.transaction.DisplayTransactions
import fr.uge.plutus.frontend.view.transaction.TransactionCreationView
import androidx.compose.ui.res.stringResource
import fr.uge.plutus.R

enum class View(
    val headerComponent: @Composable () -> Unit,
    val contentComponent: @Composable (PaddingValues) -> Unit,
    val fabComponent: @Composable (() -> Unit) = {}


) {

    BOOK_SELECTION(
        headerComponent = {
            TopAppBar(title = { Text(stringResource(id = R.string.books)) })
        },
        contentComponent = { BookSelectionView() },
        fabComponent = {
            val globalState = globalState()
            FloatingActionButton(onClick = {
                globalState.currentView = BOOK_CREATION
            }) {
                Icon(
                    Icons.Default.Add,
                    stringResource(R.string.new_book)
                )
            }
        }
    ),

    BOOK_CREATION(
        headerComponent = {
            TopAppBar(title = { Text(stringResource(R.string.new_book)) })
        },
        contentComponent = { BookCreationView() }
    ),

    BOOK_OVERVIEW(
        headerComponent = {
            val globalState = globalState()
            TopAppBar(title = { Text(stringResource(id = R.string.overview_book_name).format(globalState.currentBook!!.name)) })
        },
        contentComponent = { BookOverviewLoader() }
    ),

    TRANSACTION_CREATION(
        headerComponent = {
            TopAppBar(title = { Text(stringResource(R.string.new_transaction)) })
        },
        contentComponent = { TransactionCreationView() }
    ),

    TRANSACTION_EDIT(
        headerComponent = {
            TopAppBar(title = { Text(stringResource(R.string.edit_transaction)) })
        },
        contentComponent = { TransactionCreationView() }
    ),

    TRANSACTION_LIST(
        headerComponent = {
            val globalState = globalState()
            TopAppBar(title = { Text(stringResource(R.string.transaction_book_name).format(globalState.currentBook!!.name)) })
        },
        contentComponent = { DisplayTransactions() },
        fabComponent = {
            val globalState = globalState()
            FloatingActionButton(onClick = {
                globalState.currentView = TRANSACTION_CREATION
            }) {
                Icon(
                    Icons.Default.Add,
                    stringResource(R.string.new_transaction)
                )
            }
        }
    ),

    TRANSACTION_DETAILS(
        headerComponent = {
            val globalState = globalState()
            val currentTransaction = globalState.currentTransaction
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.transaction_details)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            globalState.currentView = TRANSACTION_LIST
                            globalState.currentTransaction = null
                        }) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                        }
                    })
                if (currentTransaction != null) {
                    DisplayHeader(currentTransaction)
                }
            }
        },
        contentComponent = {
            val globalState = globalState()
            if (globalState.currentTransaction != null) {
                DisplayTransactionDetails(globalState.currentTransaction!!)
            }
        },
        fabComponent = {
            val globalState = globalState()
            FloatingActionButton(onClick = {
                globalState.currentView = TRANSACTION_EDIT
            }) {
                Icon(
                    Icons.Default.Edit,
                    stringResource(R.string.edit)
                )
            }
        }
    )
}
