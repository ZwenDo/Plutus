package fr.uge.plutus.backend

inline fun <reified T> inlinedAssertThrows(exceptionClass: Class<T>, block: () -> Unit): T {
    try {
        block()
    } catch (e: Throwable) {
        if (e is T) {
            return e
        }
        throw e
    }
    throw AssertionError("Expected an exception of type ${exceptionClass.name} to be thrown, but was completed successfully.")
}