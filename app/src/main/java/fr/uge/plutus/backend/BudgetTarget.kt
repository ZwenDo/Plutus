package fr.uge.plutus.backend

import androidx.room.*
import java.util.UUID

enum class TimePeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

@Entity(
    tableName = "budget_targets",
)
data class BudgetTarget(
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "time_period") val timePeriod: TimePeriod,
    @PrimaryKey val uuid: UUID,
)

@Dao
interface BudgetTargetDao {
    @Query("SELECT * FROM budget_targets")
    suspend fun getAll(): List<BudgetTarget>

    @Query("SELECT * FROM budget_targets WHERE uuid = :budgetTargetId LIMIT 1")
    suspend fun findById(budgetTargetId: UUID): BudgetTarget?

    @Insert
    suspend fun insert(vararg budgetTargets: BudgetTarget)

    @Delete
    suspend fun delete(vararg budgetTarget: BudgetTarget)

    @Update
    suspend fun update(budgetTarget: BudgetTarget)
}
