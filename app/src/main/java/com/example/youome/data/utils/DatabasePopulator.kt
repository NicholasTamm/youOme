package com.example.youome.data.utils

import android.util.Log
import com.example.youome.data.dao.*
import com.example.youome.data.database.YouOmeDatabase
import com.example.youome.data.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabasePopulator {
    
    // Test Users
    private val testUsers = listOf(
        User(
            userId = "user1",
            displayName = "John Smith",
            email = "john@example.com",
            isCurrentUser = true
        ),
        User(
            userId = "user2", 
            displayName = "Sarah Johnson",
            email = "sarah@example.com",
            isCurrentUser = false
        ),
        User(
            userId = "user3",
            displayName = "Mike Wilson", 
            email = "mike@example.com",
            isCurrentUser = false
        ),
        User(
            userId = "user4",
            displayName = "Emily Davis",
            email = "emily@example.com", 
            isCurrentUser = false
        ),
        User(
            userId = "user5",
            displayName = "Alex Brown",
            email = "alex@example.com",
            isCurrentUser = false
        )
    )
    
    // Test Groups
    private val testGroups = listOf(
        Group(
            groupId = "group1",
            name = "Weekend Trip",
            currency = "USD",
            category = "Travel"
        ),
        Group(
            groupId = "group2", 
            name = "Dinner with Friends",
            currency = "USD",
            category = "Food & Dining"
        ),
        Group(
            groupId = "group3",
            name = "Office Lunch",
            currency = "USD", 
            category = "Food & Dining"
        ),
        Group(
            groupId = "group4",
            name = "Movie Night",
            currency = "USD",
            category = "Entertainment"
        ),
        Group(
            groupId = "group5",
            name = "Shopping Spree",
            currency = "EUR",
            category = "Shopping"
        )
    )
    
    // Test Group Members
    private val testGroupMembers = listOf(
        // Weekend Trip (group1) - 4 members
        GroupMember("group1", "user1"),
        GroupMember("group1", "user2"), 
        GroupMember("group1", "user3"),
        GroupMember("group1", "user4"),
        
        // Dinner with Friends (group2) - 6 members
        GroupMember("group2", "user1"),
        GroupMember("group2", "user2"),
        GroupMember("group2", "user3"),
        GroupMember("group2", "user4"),
        GroupMember("group2", "user5"),
        
        // Office Lunch (group3) - 8 members (including current user)
        GroupMember("group3", "user1"),
        GroupMember("group3", "user2"),
        GroupMember("group3", "user3"),
        GroupMember("group3", "user4"),
        GroupMember("group3", "user5"),
        
        // Movie Night (group4) - 3 members
        GroupMember("group4", "user1"),
        GroupMember("group4", "user2"),
        GroupMember("group4", "user3"),
        
        // Shopping Spree (group5) - 2 members
        GroupMember("group5", "user1"),
        GroupMember("group5", "user4")
    )
    
    // Test Expenses
    private val testExpenses = listOf(
        // Weekend Trip expenses
        Expense(
            expenseId = "expense1",
            groupId = "group1",
            description = "Hotel booking",
            amount = 200.00,
            paidBy = "user1",
            splitBy = "user1,user2,user3,user4",
            category = "Accommodation",
            currency = "USD",
            createdAt = System.currentTimeMillis() - 86400000 * 7 // 7 days ago
        ),
        Expense(
            expenseId = "expense2", 
            groupId = "group1",
            description = "Gas for road trip",
            amount = 80.00,
            paidBy = "user2",
            splitBy = "user1,user2,user3,user4",
            category = "Transportation",
            currency = "USD",
            createdAt = System.currentTimeMillis() - 86400000 * 6 // 6 days ago
        ),
        Expense(
            expenseId = "expense3",
            groupId = "group1", 
            description = "Dinner at restaurant",
            amount = 120.00,
            paidBy = "user3",
            splitBy = "user1,user2,user3,user4",
            category = "Food & Dining",
            currency = "USD",
            createdAt = System.currentTimeMillis() - 86400000 * 5 // 5 days ago
        ),
        
        // Dinner with Friends expenses
        Expense(
            expenseId = "expense4",
            groupId = "group2",
            description = "Restaurant bill",
            amount = 150.00,
            paidBy = "user1",
            splitBy = "user1,user2,user3,user4,user5",
            category = "Food & Dining", 
            currency = "USD",
            createdAt = System.currentTimeMillis() - 86400000 * 3 // 3 days ago
        ),
        Expense(
            expenseId = "expense5",
            groupId = "group2",
            description = "Drinks at bar",
            amount = 75.00,
            paidBy = "user4",
            splitBy = "user1,user2,user3,user4,user5",
            category = "Entertainment",
            currency = "USD", 
            createdAt = System.currentTimeMillis() - 86400000 * 2 // 2 days ago
        ),
        
        // Office Lunch expenses
        Expense(
            expenseId = "expense6",
            groupId = "group3",
            description = "Team lunch",
            amount = 60.00,
            paidBy = "user1",
            splitBy = "user1,user2,user3,user4,user5",
            category = "Food & Dining",
            currency = "USD",
            createdAt = System.currentTimeMillis() - 86400000 * 1 // 1 day ago
        ),
        
        // Movie Night expenses
        Expense(
            expenseId = "expense7",
            groupId = "group4", 
            description = "Movie tickets",
            amount = 45.00,
            paidBy = "user2",
            splitBy = "user1,user2,user3",
            category = "Entertainment",
            currency = "USD",
            createdAt = System.currentTimeMillis() - 3600000 * 4 // 4 hours ago
        ),
        Expense(
            expenseId = "expense8",
            groupId = "group4",
            description = "Popcorn and snacks",
            amount = 25.00,
            paidBy = "user1", 
            splitBy = "user1,user2,user3",
            category = "Food & Dining",
            currency = "USD",
            createdAt = System.currentTimeMillis() - 3600000 * 3 // 3 hours ago
        ),
        
        // Shopping Spree expenses
        Expense(
            expenseId = "expense9",
            groupId = "group5",
            description = "Clothing shopping",
            amount = 180.00,
            paidBy = "user1",
            splitBy = "user1,user4", 
            category = "Shopping",
            currency = "EUR",
            createdAt = System.currentTimeMillis() - 3600000 * 2 // 2 hours ago
        )
    )
    
    // Test Debts (calculated from expenses)
    private val testDebts = listOf(
        // Weekend Trip debts
        Debt("group1", "user2", "user1", 50.00, "USD", false), // user2 owes user1 $50 for hotel
        Debt("group1", "user3", "user1", 50.00, "USD", false), // user3 owes user1 $50 for hotel  
        Debt("group1", "user4", "user1", 50.00, "USD", false), // user4 owes user1 $50 for hotel
        Debt("group1", "user1", "user2", 20.00, "USD", false), // user1 owes user2 $20 for gas
        Debt("group1", "user3", "user2", 20.00, "USD", false), // user3 owes user2 $20 for gas
        Debt("group1", "user4", "user2", 20.00, "USD", false), // user4 owes user2 $20 for gas
        Debt("group1", "user1", "user3", 30.00, "USD", false), // user1 owes user3 $30 for dinner
        Debt("group1", "user2", "user3", 30.00, "USD", false), // user2 owes user3 $30 for dinner
        Debt("group1", "user4", "user3", 30.00, "USD", false), // user4 owes user3 $30 for dinner
        
        // Dinner with Friends debts
        Debt("group2", "user2", "user1", 30.00, "USD", false), // user2 owes user1 $30 for restaurant
        Debt("group2", "user3", "user1", 30.00, "USD", false), // user3 owes user1 $30 for restaurant
        Debt("group2", "user4", "user1", 30.00, "USD", false), // user4 owes user1 $30 for restaurant
        Debt("group2", "user5", "user1", 30.00, "USD", false), // user5 owes user1 $30 for restaurant
        Debt("group2", "user1", "user4", 15.00, "USD", false), // user1 owes user4 $15 for drinks
        Debt("group2", "user2", "user4", 15.00, "USD", false), // user2 owes user4 $15 for drinks
        Debt("group2", "user3", "user4", 15.00, "USD", false), // user3 owes user4 $15 for drinks
        Debt("group2", "user5", "user4", 15.00, "USD", false), // user5 owes user4 $15 for drinks
        
        // Office Lunch debts
        Debt("group3", "user2", "user1", 12.00, "USD", false), // user2 owes user1 $12 for lunch
        Debt("group3", "user3", "user1", 12.00, "USD", false), // user3 owes user1 $12 for lunch
        Debt("group3", "user4", "user1", 12.00, "USD", false), // user4 owes user1 $12 for lunch
        Debt("group3", "user5", "user1", 12.00, "USD", false), // user5 owes user1 $12 for lunch
        
        // Movie Night debts
        Debt("group4", "user1", "user2", 15.00, "USD", false), // user1 owes user2 $15 for tickets
        Debt("group4", "user3", "user2", 15.00, "USD", false), // user3 owes user2 $15 for tickets
        Debt("group4", "user2", "user1", 8.33, "USD", false), // user2 owes user1 $8.33 for snacks
        Debt("group4", "user3", "user1", 8.33, "USD", false), // user3 owes user1 $8.33 for snacks
        
        // Shopping Spree debts
        Debt("group5", "user4", "user1", 90.00, "EUR", false) // user4 owes user1 €90 for shopping
    )
    
    // Test Expense Items (detailed breakdown)
    private val testExpenseItems = listOf(
        // Hotel booking expense items
        ExpenseItem("expense1", "user1", 50.00, false), // user1's share
        ExpenseItem("expense1", "user2", 50.00, false), // user2's share
        ExpenseItem("expense1", "user3", 50.00, false), // user3's share
        ExpenseItem("expense1", "user4", 50.00, false), // user4's share
        
        // Gas expense items
        ExpenseItem("expense2", "user1", 20.00, false), // user1's share
        ExpenseItem("expense2", "user2", 20.00, false), // user2's share
        ExpenseItem("expense2", "user3", 20.00, false), // user3's share
        ExpenseItem("expense2", "user4", 20.00, false), // user4's share
        
        // Dinner expense items
        ExpenseItem("expense3", "user1", 30.00, false), // user1's share
        ExpenseItem("expense3", "user2", 30.00, false), // user2's share
        ExpenseItem("expense3", "user3", 30.00, false), // user3's share
        ExpenseItem("expense3", "user4", 30.00, false), // user4's share
        
        // Restaurant bill expense items
        ExpenseItem("expense4", "user1", 30.00, false), // user1's share
        ExpenseItem("expense4", "user2", 30.00, false), // user2's share
        ExpenseItem("expense4", "user3", 30.00, false), // user3's share
        ExpenseItem("expense4", "user4", 30.00, false), // user4's share
        ExpenseItem("expense4", "user5", 30.00, false), // user5's share
        
        // Drinks expense items
        ExpenseItem("expense5", "user1", 15.00, false), // user1's share
        ExpenseItem("expense5", "user2", 15.00, false), // user2's share
        ExpenseItem("expense5", "user3", 15.00, false), // user3's share
        ExpenseItem("expense5", "user4", 15.00, false), // user4's share
        ExpenseItem("expense5", "user5", 15.00, false), // user5's share
        
        // Team lunch expense items
        ExpenseItem("expense6", "user1", 12.00, false), // user1's share
        ExpenseItem("expense6", "user2", 12.00, false), // user2's share
        ExpenseItem("expense6", "user3", 12.00, false), // user3's share
        ExpenseItem("expense6", "user4", 12.00, false), // user4's share
        ExpenseItem("expense6", "user5", 12.00, false), // user5's share
        
        // Movie tickets expense items
        ExpenseItem("expense7", "user1", 15.00, false), // user1's share
        ExpenseItem("expense7", "user2", 15.00, false), // user2's share
        ExpenseItem("expense7", "user3", 15.00, false), // user3's share
        
        // Popcorn expense items
        ExpenseItem("expense8", "user1", 8.33, false), // user1's share
        ExpenseItem("expense8", "user2", 8.33, false), // user2's share
        ExpenseItem("expense8", "user3", 8.33, false), // user3's share
        
        // Shopping expense items
        ExpenseItem("expense9", "user1", 90.00, false), // user1's share
        ExpenseItem("expense9", "user4", 90.00, false)  // user4's share
    )
    
    /**
     * Populate the database with test data
     */
    suspend fun seedDatabase(database: YouOmeDatabase) = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            Log.d("DatabasePopulator", "Starting database seed...")
            
            // Clear existing data first
            clearDatabase(database)
            
            Log.d("DatabasePopulator", "Inserting test users...")
            val userStartTime = System.currentTimeMillis()
            database.userDao().insertUsers(testUsers)
            Log.d("DatabasePopulator", "Users inserted in ${System.currentTimeMillis() - userStartTime}ms")
            
            Log.d("DatabasePopulator", "Inserting test groups...")
            val groupStartTime = System.currentTimeMillis()
            database.groupDao().insertGroups(testGroups)
            Log.d("DatabasePopulator", "Groups inserted in ${System.currentTimeMillis() - groupStartTime}ms")
            
            Log.d("DatabasePopulator", "Inserting test group members...")
            val memberStartTime = System.currentTimeMillis()
            database.groupMemberDao().insertGroupMembers(testGroupMembers)
            Log.d("DatabasePopulator", "Group members inserted in ${System.currentTimeMillis() - memberStartTime}ms")
            
            Log.d("DatabasePopulator", "Inserting test expenses...")
            val expenseStartTime = System.currentTimeMillis()
            database.expenseDao().insertExpenses(testExpenses)
            Log.d("DatabasePopulator", "Expenses inserted in ${System.currentTimeMillis() - expenseStartTime}ms")
            
            Log.d("DatabasePopulator", "Inserting test debts...")
            val debtStartTime = System.currentTimeMillis()
            database.debtDao().insertDebts(testDebts)
            Log.d("DatabasePopulator", "Debts inserted in ${System.currentTimeMillis() - debtStartTime}ms")
            
            Log.d("DatabasePopulator", "Inserting test expense items...")
            val itemStartTime = System.currentTimeMillis()
            database.expenseItemDao().insertExpenseItems(testExpenseItems)
            Log.d("DatabasePopulator", "Expense items inserted in ${System.currentTimeMillis() - itemStartTime}ms")
            
            val totalTime = System.currentTimeMillis() - startTime
            Log.d("DatabasePopulator", "Database seeded successfully in ${totalTime}ms!")
            Log.d("DatabasePopulator", "Seeded data:")
            Log.d("DatabasePopulator", "   - ${testUsers.size} users")
            Log.d("DatabasePopulator", "   - ${testGroups.size} groups")
            Log.d("DatabasePopulator", "   - ${testGroupMembers.size} group memberships")
            Log.d("DatabasePopulator", "   - ${testExpenses.size} expenses")
            Log.d("DatabasePopulator", "   - ${testDebts.size} debts")
            Log.d("DatabasePopulator", "   - ${testExpenseItems.size} expense items")
            
            // Verify the data was inserted (simplified to avoid blocking)
            val currentUserAfterInsert = database.userDao().getCurrentUser()
            Log.d("DatabasePopulator", "   - Current user after insert: ${currentUserAfterInsert?.displayName ?: "null"}")
            
        } catch (e: Exception) {
            Log.e("DatabasePopulator", "Error seeding database: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Clear all data from the database
     */
    suspend fun clearDatabase(database: YouOmeDatabase) = withContext(Dispatchers.IO) {
        try {
            Log.d("DatabasePopulator", "Starting database clear...")
            
            database.expenseItemDao().deleteAll()
            database.debtDao().deleteAll()
            database.expenseDao().deleteAll()
            database.groupMemberDao().deleteAll()
            database.groupDao().deleteAll()
            database.userDao().deleteAll()
            
            Log.d("DatabasePopulator", "Database cleared successfully!")
        } catch (e: Exception) {
            Log.e("DatabasePopulator", "Error clearing database: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Check if database is already populated with test data
     */
    suspend fun isDatabasePopulated(database: YouOmeDatabase): Boolean = withContext(Dispatchers.IO) {
        try {
            // Collect Flow to get actual lists
            val users = database.userDao().getAllUsers().let { flow ->
                var result: List<com.example.youome.data.entities.User> = emptyList()
                flow.collect { result = it }
                result
            }
            val groups = database.groupDao().getAllGroups().let { flow ->
                var result: List<com.example.youome.data.entities.Group> = emptyList()
                flow.collect { result = it }
                result
            }
            val expenses = database.expenseDao().getAllExpenses()
            val debts = database.debtDao().getAllDebts()
            
            val userCount = users.size
            val groupCount = groups.size
            val expenseCount = expenses.size
            val debtCount = debts.size
            
            // Database is considered populated if it has our test data
            // We expect: 5 users, 5 groups, 9 expenses, 25 debts
            val isPopulated = userCount >= 5 && groupCount >= 5 && expenseCount >= 9 && debtCount >= 25
            
            Log.d("DatabasePopulator", "Database population check:")
            Log.d("DatabasePopulator", "   - Users: $userCount (expected: 5+)")
            Log.d("DatabasePopulator", "   - Groups: $groupCount (expected: 5+)")
            Log.d("DatabasePopulator", "   - Expenses: $expenseCount (expected: 9+)")
            Log.d("DatabasePopulator", "   - Debts: $debtCount (expected: 25+)")
            Log.d("DatabasePopulator", "   - Is populated: $isPopulated")
            
            return@withContext isPopulated
            
        } catch (e: Exception) {
            Log.e("DatabasePopulator", "Error checking database population: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Get summary of current database state
     */
    suspend fun getDatabaseSummary(database: YouOmeDatabase) = withContext(Dispatchers.IO) {
        try {
            // Collect Flow to get actual lists
            val users = database.userDao().getAllUsers().let { flow ->
                var result: List<com.example.youome.data.entities.User> = emptyList()
                flow.collect { result = it }
                result
            }
            val groups = database.groupDao().getAllGroups().let { flow ->
                var result: List<com.example.youome.data.entities.Group> = emptyList()
                flow.collect { result = it }
                result
            }
            val expenses = database.expenseDao().getAllExpenses()
            val debts = database.debtDao().getAllDebts()
            
            val userCount = users.size
            val groupCount = groups.size
            val expenseCount = expenses.size
            val debtCount = debts.size
            
            Log.d("DatabasePopulator", "Current Database State:")
            Log.d("DatabasePopulator", "   - Users: $userCount")
            Log.d("DatabasePopulator", "   - Groups: $groupCount") 
            Log.d("DatabasePopulator", "   - Expenses: $expenseCount")
            Log.d("DatabasePopulator", "   - Debts: $debtCount")
            
        } catch (e: Exception) {
            Log.e("DatabasePopulator", "Error getting database summary: ${e.message}")
        }
    }
}
