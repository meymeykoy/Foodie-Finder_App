package com.example.afinal.Food
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenUI(nc: NavHostController, vm: TheFoodSoViewModel = viewModel()) {
    val categories by vm.categories.collectAsState(emptyList())
    val meals by vm.meals.collectAsState(emptyList())
//    val areaMeals by vm.areaMeals.collectAsState(emptyList())
    val isLoadingMeals by vm.isLoadingMeals.collectAsState(false)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val selectedCategory = remember { mutableStateOf<String?>(null) }
    val error by vm.error.collectAsState() // Assuming you have an error state in your ViewModel

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FoodieFinder",
                            style = TextStyle(
                                fontFamily = FontFamily.Cursive,
                                fontSize = 30.sp,
                                color = Color.White,
                                letterSpacing = 2.5.sp,
                                shadow = Shadow(
                                    color = Color(0x80000000),
                                    offset = Offset(3f, 3f),
                                    blurRadius = 4f
                                )
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle navigation click */ }) {
                        val painter =
                            rememberAsyncImagePainter("https://i.pinimg.com/736x/bc/63/e8/bc63e892be4e597a70ec50b825f95978.jpg")
                        Image(
                            painter = painter,
                            contentDescription = "Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .padding(5.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD64174),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x81DDD6D6))
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                item {
                    LazyRow(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(categories) { category ->
                            CategoryItem(
                                category = category,
                                isSelected = category.strCategory == selectedCategory.value,
                                onCategorySelected = {
                                    selectedCategory.value = it.strCategory
                                    vm.clearMeals()
                                    vm.fetchMeals(category = it.strCategory)
                                }
                            )
                        }
                    }
                }

                item {
                    Carousel(meals = meals) { meal ->
                        MealCarouselItem(
                            meal = meal,
                            onClick = { selectedMeal ->
                                nc.navigate("MealDetail/${selectedMeal.strMeal}")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                }

                error?.let {
                    item {
                        Text("Error: $it", color = MaterialTheme.colorScheme.error)
                    }
                }

                // Meals List
                items(meals) { meal ->
                    MealItem(
                        meal = meal,
                        isLoading = isLoadingMeals,
                        viewModel = vm,
                        onClick = { nc.navigate("MealDetail/${meal.strMeal}") },
                        onFavoriteChanged = { isFavorite ->
                            coroutineScope.launch {
                                val message = if (isFavorite) {
                                    "Added to favorites"
                                } else {
                                    "Removed from favorites"
                                }
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Carousel(meals: List<Meal>, itemContent: @Composable (Meal) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { meals.size })

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (meals.isEmpty()) {
            CircularProgressIndicator()
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // Adjusted height to fit the images and content compactly
                    .background(Color(0xFFFFFFFF))
            ) {
                HorizontalPager(state = pagerState) { page ->
                    itemContent(meals[page])
                }
            }
            CustomDotsIndicator(
                totalDots = meals.size,
                selectedIndex = pagerState.currentPage
            )
        }
    }
}

@Composable
fun CustomDotsIndicator(totalDots: Int, selectedIndex: Int) {
    val selectedColor = Color(0xFFFF80AB)
    val unSelectedColor = Color(0xFFB0B0B0)

    DotsIndicator(
        totalDots = totalDots,
        selectedIndex = selectedIndex,
        selectedColor = selectedColor,
        unSelectedColor = unSelectedColor
    )
}

@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    selectedColor: Color,
    unSelectedColor: Color
) {
    LazyRow(
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(10.dp)
    ) {
        items(totalDots) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (index == selectedIndex) selectedColor else unSelectedColor)
            )
            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun MealCarouselItem(meal: Meal, onClick: (Meal) -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth() // Ensure the item fills the width of the screen
            .clickable { onClick(meal) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFFFF))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                AsyncImage(
                    model = meal.strMealThumb,
                    contentDescription = meal.strMeal,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth() // Makes sure the image fills the width of the screen
                        .aspectRatio(16 / 9f) // Adjusted aspect ratio for better fit
                        .clip(RoundedCornerShape(10.dp))
                )
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(Color(0xFF1E201E))
//                        .padding(top = 8.dp)
//
//                ) {
//                    Text(
//                        text = meal.strMeal,
//                        style = TextStyle(
//                            fontSize = 10.sp,
//                            color = Color(0xFFb0b4b7)
//                        ),
//                        modifier = Modifier.align(Alignment.Center)
//                            .clip(RoundedCornerShape(10.dp))
//                    )
//                }

                }
            }
        }
    }


@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onCategorySelected: (Category) -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFFF80AB) else Color.White
    val contentColor = if (isSelected) Color.White else Color.Black

    Button(
        onClick = { onCategorySelected(category) },
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = contentColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(160.dp)
            .height(80.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = category.strCategoryThumb,
                contentDescription = category.strCategory,
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.strCategory,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                ),
                maxLines = 1,
                modifier = Modifier.weight(1f),
                softWrap = true
            )
        }
    }
}

@Composable
fun MealItem(
    meal: Meal,
    isLoading: Boolean,
    viewModel: TheFoodSoViewModel,
    onClick: (Meal) -> Unit,
    onFavoriteChanged: (Boolean) -> Unit
) {
    var isFavorite by remember { mutableStateOf(viewModel.isFavorite(meal.idMeal)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(meal) },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = meal.strMealThumb,
                contentDescription = meal.strMeal,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = meal.strMeal,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                )
            }
            IconButton(onClick = {
                isFavorite = !isFavorite
                viewModel.toggleFavorite(meal)
                onFavoriteChanged(isFavorite)
            }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite Icon",
                    tint = Color(0xFFFF80AB)
                )
            }
        }
    }
}
