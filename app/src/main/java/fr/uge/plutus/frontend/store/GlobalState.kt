package fr.uge.plutus.frontend.store

import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Transaction


object GlobalState {
    var currentBook: Book? = null
    var currentTransaction: Transaction? = null
}