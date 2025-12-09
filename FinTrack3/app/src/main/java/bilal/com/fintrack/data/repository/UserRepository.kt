package bilal.com.fintrack.data.repository

import bilal.com.fintrack.data.local.dao.UserDao
import bilal.com.fintrack.data.local.entities.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    
    fun getUser(userId: String): Flow<User?> = userDao.getUser(userId)
    
    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()
    
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
}
