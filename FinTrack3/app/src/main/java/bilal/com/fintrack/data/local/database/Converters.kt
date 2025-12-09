package bilal.com.fintrack.data.local.database

import androidx.room.TypeConverter
import bilal.com.fintrack.data.local.entities.BudgetPeriod
import bilal.com.fintrack.data.local.entities.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromBudgetPeriod(value: BudgetPeriod): String {
        return value.name
    }

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod {
        return BudgetPeriod.valueOf(value)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
