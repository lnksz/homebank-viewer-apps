package org.kristof.hbviewer

interface ViewerStore {
    fun importXml(sourceName: String, xml: String): ImportSummary
    fun getImportSummary(): ImportSummary?
    fun getAccounts(): List<AccountSummary>
    fun getTransactionsByAccount(accountId: Long): List<TransactionSummary>
    fun searchTransactions(
        query: String,
        accountId: Long?,
        kindFilter: TransactionKindFilter,
        minimumAmount: Double?,
        maximumAmount: Double?
    ): List<TransactionSummary>
    fun getTransactionDetail(transactionId: Long): TransactionDetail?
}
