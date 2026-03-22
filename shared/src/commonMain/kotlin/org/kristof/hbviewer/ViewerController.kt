package org.kristof.hbviewer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ViewerController(
    private val repository: ViewerStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(ViewerSnapshot.empty())
    val state: StateFlow<ViewerSnapshot> = _state.asStateFlow()

    init {
        refresh()
    }

    fun import(sourceName: String, xml: String) {
        scope.launch {
            runCatching {
                repository.importXml(sourceName, xml)
            }.onSuccess {
                refresh(selectedAccountId = _state.value.selectedAccountId)
            }.onFailure { error ->
                _state.value = _state.value.copy(errorMessage = error.message ?: "Import failed")
            }
        }
    }

    fun selectAccount(accountId: Long?) {
        refresh(selectedAccountId = accountId)
    }

    fun selectAllAccounts() {
        refresh(selectedAccountId = null)
    }

    fun search(query: String) {
        val current = _state.value
        val results = filteredResults(
            query = query,
            accountId = current.selectedAccountId,
            kindFilter = current.transactionKindFilter,
            minimumAmountText = current.minimumAmountText,
            maximumAmountText = current.maximumAmountText
        )
        _state.value = _state.value.copy(
            searchQuery = query,
            searchResults = results,
            selectedTransaction = null,
            errorMessage = null
        )
    }

    fun setTransactionKindFilter(filter: TransactionKindFilter) {
        val current = _state.value
        _state.value = current.copy(
            transactionKindFilter = filter,
            searchResults = filteredResults(
                query = current.searchQuery,
                accountId = current.selectedAccountId,
                kindFilter = filter,
                minimumAmountText = current.minimumAmountText,
                maximumAmountText = current.maximumAmountText
            ),
            selectedTransaction = null,
            errorMessage = null
        )
    }

    fun setMinimumAmount(text: String) {
        val current = _state.value
        _state.value = current.copy(
            minimumAmountText = text,
            searchResults = filteredResults(
                query = current.searchQuery,
                accountId = current.selectedAccountId,
                kindFilter = current.transactionKindFilter,
                minimumAmountText = text,
                maximumAmountText = current.maximumAmountText
            ),
            selectedTransaction = null,
            errorMessage = null
        )
    }

    fun setMaximumAmount(text: String) {
        val current = _state.value
        _state.value = current.copy(
            maximumAmountText = text,
            searchResults = filteredResults(
                query = current.searchQuery,
                accountId = current.selectedAccountId,
                kindFilter = current.transactionKindFilter,
                minimumAmountText = current.minimumAmountText,
                maximumAmountText = text
            ),
            selectedTransaction = null,
            errorMessage = null
        )
    }

    fun selectTransaction(transactionId: Long?) {
        _state.value = _state.value.copy(
            selectedTransaction = transactionId?.let(repository::getTransactionDetail),
            errorMessage = null
        )
    }

    private fun refresh(selectedAccountId: Long? = _state.value.selectedAccountId) {
        val current = _state.value
        val accounts = repository.getAccounts()
        val transactions = selectedAccountId?.let(repository::getTransactionsByAccount)
            ?: accounts.flatMap { repository.getTransactionsByAccount(it.id) }
                .sortedWith(compareByDescending<TransactionSummary> { it.dateIso }.thenByDescending { it.id })
        val searchQuery = current.searchQuery
        val searchResults = filteredResults(
            query = searchQuery,
            accountId = selectedAccountId,
            kindFilter = current.transactionKindFilter,
            minimumAmountText = current.minimumAmountText,
            maximumAmountText = current.maximumAmountText
        )

        _state.value = ViewerSnapshot(
            importSummary = repository.getImportSummary(),
            accounts = accounts,
            selectedAccountId = selectedAccountId,
            transactions = transactions,
            searchQuery = searchQuery,
            searchResults = searchResults,
            transactionKindFilter = current.transactionKindFilter,
            minimumAmountText = current.minimumAmountText,
            maximumAmountText = current.maximumAmountText,
            selectedTransaction = null,
            errorMessage = null
        )
    }

    private fun filteredResults(
        query: String,
        accountId: Long?,
        kindFilter: TransactionKindFilter,
        minimumAmountText: String,
        maximumAmountText: String
    ): List<TransactionSummary> {
        val minimumAmount = minimumAmountText.toDoubleOrNull()
        val maximumAmount = maximumAmountText.toDoubleOrNull()
        return repository.searchTransactions(
            query = query,
            accountId = accountId,
            kindFilter = kindFilter,
            minimumAmount = minimumAmount,
            maximumAmount = maximumAmount
        )
    }
}
