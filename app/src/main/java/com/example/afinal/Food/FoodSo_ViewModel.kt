package com.example.afinal.Food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TheFoodSoViewModel : ViewModel() {
    private val _results = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _results

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _favoriteMeals = MutableStateFlow<List<FavoriteMeal>>(emptyList())
    val favoriteMeals: StateFlow<List<FavoriteMeal>> = _favoriteMeals

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _areaMeals = MutableStateFlow<List<Meal>>(emptyList())
    val areaMeals: StateFlow<List<Meal>> = _areaMeals

    private val mealCache = mutableMapOf<String, Meal?>()
    private val categoryMealCache = mutableMapOf<String, List<Meal>>()

    private val _isLoading = MutableStateFlow(false)
    val isLoadingMeals: StateFlow<Boolean> get() = _isLoading

    private var isAscending = true // Track sort order


    fun toggleSort() {
        val letters = ('A'..'Z').toList() // List of letters A to E
        isAscending = !isAscending // Toggle the sort order

        _results.value = _results.value
            .filter { meal -> letters.any { letter -> meal.strMeal.startsWith(letter, ignoreCase = true) } } // Filter meals starting with A to E
            .sortedBy { it.strMeal.split(" ").firstOrNull() }
            .let {
                if (isAscending) it else it.reversed() // Sort based on current order
            }
    }


    fun fetchMealsByArea(area: String) {
        viewModelScope.launch {
            try {
                val response = TheFoodSoService.getInstance().searchMealsByArea(area)
                _areaMeals.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
                _areaMeals.value = emptyList()
            }
        }
    }

    fun getCachedMealByName(mealName: String): Meal? {
        return mealCache[mealName]
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(UnstableApi::class) suspend fun fetchMealByName(mealName: String): Meal? {
        return try {
            val response = TheFoodSoService.getInstance().searchMeals(mealName)
            val meal = response.meals?.firstOrNull { it.strMeal == mealName }
            mealCache[mealName] = meal
            meal
        } catch (e: Exception) {
            null
        }
    }

    init {
        fetchMeals()
        fetchCategories()
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(UnstableApi::class)
    fun toggleFavorite(meal: Meal) {
        val area = meal.strArea ?: "Unknown"
        val favorite = FavoriteMeal(meal.idMeal, meal.strMeal, meal.strMealThumb, area)
        val currentFavorites = _favoriteMeals.value.toMutableList()
        Log.d("TheFoodSoViewModel", "Toggling favorite for meal: ${meal.strMeal}")
        if (currentFavorites.any { it.id == favorite.id }) {
            Log.d("TheFoodSoViewModel", "Removing from favorites: ${meal.strMeal}")
            currentFavorites.removeAll { it.id == favorite.id }
        } else {
            Log.d("TheFoodSoViewModel", "Adding to favorites: ${meal.strMeal}")
            currentFavorites.add(favorite)
        }
        _favoriteMeals.value = currentFavorites
    }

    fun clearMeals() {
        _results.value = emptyList()
    }

    fun removeFavorite(mealId: String) {
        val currentFavorites = _favoriteMeals.value.toMutableList()
        currentFavorites.removeAll { it.id == mealId }
        _favoriteMeals.value = currentFavorites
    }

    fun isFavorite(mealId: String): Boolean {
        return _favoriteMeals.value.any { it.id == mealId }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(UnstableApi::class)
    fun fetchMeals(query: String = "", category: String = "") {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val cachedMeals = categoryMealCache[category]
                if (cachedMeals != null) {
                    _results.value = cachedMeals
                } else {
                    val meals = if (category.isNotEmpty()) {
                        val categoryResponse = TheFoodSoService.getInstance().searchMealsByCategory(category)
                        val mealsWithDetails = coroutineScope {
                            categoryResponse.meals?.map { meal ->
                                async {
                                    fetchFullMealDetails(meal)
                                }
                            }?.awaitAll() ?: emptyList()
                        }
                        mealsWithDetails
                    } else {
                        val response = TheFoodSoService.getInstance().searchMeals(query)
                        response.meals ?: emptyList()
                    }
                    if (category.isNotEmpty()) {
                        categoryMealCache[category] = meals
                    }
                    _results.value = meals
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(UnstableApi::class)
    private suspend fun fetchFullMealDetails(meal: Meal): Meal {
        return try {
            val fullMealResponse = TheFoodSoService.getInstance().getMealDetails(meal.idMeal)
            val fullMeal = fullMealResponse.meals?.firstOrNull { it.idMeal == meal.idMeal }
            fullMeal ?: meal
        } catch (e: Exception) {
            Log.e("MealViewModel", "Error fetching full meal details: ${e.message}")
            meal
        }
    }


    fun getMealDetails(mealId: String): StateFlow<Meal?> {
        val mealFlow = MutableStateFlow<Meal?>(null)
        viewModelScope.launch {
            val cachedMeal = getCachedMealByName(mealId)
            if (cachedMeal != null) {
                mealFlow.value = cachedMeal
            } else {
                val fetchedMeal = fetchMealByName(mealId)
                mealFlow.value = fetchedMeal
            }
        }
        return mealFlow
    }
    fun fetchDefaultMeals() {
        viewModelScope.launch {
            try {
                val response = TheFoodSoService.getInstance().searchMeals("")
                _results.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
                _results.value = emptyList()
            }
        }
    }

    // Update searchMeals function
    fun searchMeals(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                fetchDefaultMeals() // Fetch default meals if query is empty
            } else {
                try {
                    val response = TheFoodSoService.getInstance().searchMeals(query)
                    _results.value = response.meals ?: emptyList()
                } catch (e: Exception) {
                    _error.value = e.message ?: "An unknown error occurred"
                    _results.value = emptyList()
                }
            }
        }
    }


    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = TheFoodSoService.getInstance().getCategories()
                _categories.value = response.categories
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred"
            }
        }
    }
}
