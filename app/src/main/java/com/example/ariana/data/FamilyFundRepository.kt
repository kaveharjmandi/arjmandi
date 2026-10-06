package com.example.ariana.data

import android.content.Context
import com.example.ariana.model.*
import com.example.ariana.util.JalaliCalendar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class FamilyFundRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val dataFile: File
        get() = File(context.filesDir, "family_fund_data.json")

    private val _fundData = MutableStateFlow(FamilyFundData())
    val fundData: StateFlow<FamilyFundData> = _fundData.asStateFlow()

    private val _syncStatus = MutableStateFlow("synced") // "synced", "syncing", "offline"
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val SUPABASE_URL = "https://ltzczluleawwtqrpyadd.supabase.co/rest/v1/app_state"
        private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx0emN6bHVsZWF3d3RxcnB5YWRkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODYyMzU5MDMsImV4cCI6MjEwMTgxMTkwM30.zr5lEIZ4JsfdZewo8GYs4PGoymRkTl29PWdaZJHMZa0"
        private const val ROW_ID = "family"
    }

    init {
        loadInitialData()
        fetchFromSupabase()
    }

    private fun loadInitialData() {
        try {
            if (dataFile.exists()) {
                val content = dataFile.readText()
                val loaded = json.decodeFromString<FamilyFundData>(content)
                _fundData.value = loaded
            } else {
                // Read bundled asset
                val assetContent = context.assets.open("family_fund_data.json").bufferedReader().use { it.readText() }
                val loaded = json.decodeFromString<FamilyFundData>(assetContent)
                _fundData.value = loaded
                dataFile.writeText(assetContent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun fetchFromSupabase() {
        repoScope.launch {
            _syncStatus.value = "syncing"
            try {
                val url = URL("$SUPABASE_URL?id=eq.$ROW_ID&select=data")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("apikey", SUPABASE_KEY)
                conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                if (conn.responseCode in 200..299) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = json.parseToJsonElement(responseText).jsonArray
                    if (jsonArray.isNotEmpty()) {
                        val rowObj = jsonArray[0].jsonObject
                        val dataObj = rowObj["data"]
                        if (dataObj != null) {
                            val remoteData = json.decodeFromJsonElement<FamilyFundData>(dataObj)
                            _fundData.value = remoteData
                            saveLocalData(remoteData)
                            _syncStatus.value = "synced"
                            return@launch
                        }
                    }
                }
                _syncStatus.value = "synced"
            } catch (e: Exception) {
                e.printStackTrace()
                _syncStatus.value = "offline"
            }
        }
    }

    private fun pushToSupabase(data: FamilyFundData) {
        repoScope.launch {
            _syncStatus.value = "syncing"
            try {
                val url = URL(SUPABASE_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("apikey", SUPABASE_KEY)
                conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Prefer", "resolution=merge-duplicates")
                conn.doOutput = true
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                val dataElement = json.encodeToJsonElement(data)
                val payload = buildJsonObject {
                    put("id", ROW_ID)
                    put("data", dataElement)
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    put("updated_at", sdf.format(Date()))
                }.toString()

                conn.outputStream.use { os ->
                    os.write(payload.toByteArray(Charsets.UTF_8))
                }

                if (conn.responseCode in 200..299) {
                    _syncStatus.value = "synced"
                } else {
                    _syncStatus.value = "offline"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _syncStatus.value = "offline"
            }
        }
    }

    private fun saveLocalData(data: FamilyFundData) {
        try {
            val content = json.encodeToString(data)
            dataFile.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun update(transform: (FamilyFundData) -> FamilyFundData) {
        withContext(Dispatchers.IO) {
            val current = _fundData.value
            val updated = transform(current)
            _fundData.value = updated
            saveLocalData(updated)
            pushToSupabase(updated)
        }
    }

    suspend fun addTransaction(tx: Transaction) {
        update { current ->
            val list = current.transactions.toMutableList()
            list.add(0, tx)
            current.copy(transactions = list)
        }
    }

    suspend fun editTransaction(tx: Transaction) {
        update { current ->
            val list = current.transactions.map { if (it.id == tx.id) tx else it }
            current.copy(transactions = list)
        }
    }

    suspend fun deleteTransaction(txId: String) {
        update { current ->
            val list = current.transactions.filterNot { it.id == txId }
            current.copy(transactions = list)
        }
    }

    suspend fun addCheque(cheque: Cheque) {
        update { current ->
            val list = current.cheques.toMutableList()
            list.add(0, cheque)
            current.copy(cheques = list)
        }
    }

    suspend fun updateChequeStatus(chequeId: String, status: String) {
        update { current ->
            val list = current.cheques.map {
                if (it.id == chequeId) it.copy(status = status) else it
            }
            current.copy(cheques = list)
        }
    }

    suspend fun deleteCheque(chequeId: String) {
        update { current ->
            val list = current.cheques.filterNot { it.id == chequeId }
            current.copy(cheques = list)
        }
    }

    suspend fun addGoal(goal: Goal) {
        update { current ->
            val list = current.goals.toMutableList()
            list.add(goal)
            current.copy(goals = list)
        }
    }

    suspend fun adjustGoalSavings(goalId: String, delta: Long) {
        update { current ->
            val list = current.goals.map {
                if (it.id == goalId) it.copy(saved = (it.saved + delta).coerceAtLeast(0)) else it
            }
            current.copy(goals = list)
        }
    }

    suspend fun deleteGoal(goalId: String) {
        update { current ->
            val list = current.goals.filterNot { it.id == goalId }
            current.copy(goals = list)
        }
    }

    suspend fun addLoan(loan: Loan) {
        update { current ->
            val list = current.loans.toMutableList()
            list.add(loan)
            current.copy(loans = list)
        }
    }

    suspend fun payLoanInstallment(loanId: String) {
        update { current ->
            val list = current.loans.map {
                if (it.id == loanId) it.copy(paidCount = (it.paidCount + 1).coerceAtMost(it.installmentCount)) else it
            }
            current.copy(loans = list)
        }
    }

    suspend fun deleteLoan(loanId: String) {
        update { current ->
            val list = current.loans.filterNot { it.id == loanId }
            current.copy(loans = list)
        }
    }

    suspend fun addLease(lease: Lease) {
        update { current ->
            val list = current.leases.toMutableList()
            list.add(lease)
            current.copy(leases = list)
        }
    }

    suspend fun payLeaseRent(leaseId: String) {
        update { current ->
            val list = current.leases.map {
                if (it.id == leaseId) it.copy(paidMonths = it.paidMonths + 1) else it
            }
            current.copy(leases = list)
        }
    }

    suspend fun deleteLease(leaseId: String) {
        update { current ->
            val list = current.leases.filterNot { it.id == leaseId }
            current.copy(leases = list)
        }
    }

    suspend fun addDebt(debt: Debt) {
        update { current ->
            val list = current.debts.toMutableList()
            list.add(debt)
            current.copy(debts = list)
        }
    }

    suspend fun settleDebt(debtId: String) {
        update { current ->
            val list = current.debts.map {
                if (it.id == debtId) {
                    val p = DebtPayment(
                        id = "dpm_" + UUID.randomUUID().toString().take(8),
                        date = JalaliCalendar.todayIso(),
                        amount = it.remainingAmount,
                        createdAt = System.currentTimeMillis()
                    )
                    it.copy(payments = it.payments + p)
                } else it
            }
            current.copy(debts = list)
        }
    }

    suspend fun deleteDebt(debtId: String) {
        update { current ->
            val list = current.debts.filterNot { it.id == debtId }
            current.copy(debts = list)
        }
    }

    suspend fun addPersonTransfer(pt: PersonTransfer) {
        update { current ->
            val list = current.personTransfers.toMutableList()
            list.add(pt)
            current.copy(personTransfers = list)
        }
    }

    suspend fun exportJson(): String {
        return json.encodeToString(_fundData.value)
    }

    suspend fun importJson(jsonContent: String): Boolean {
        return try {
            val imported = json.decodeFromString<FamilyFundData>(jsonContent)
            _fundData.value = imported
            saveLocalData(imported)
            pushToSupabase(imported)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
