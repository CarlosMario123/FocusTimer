package com.example.focustimer.feature_onboarding.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.focustimer.core.navigation.NavigationConstants.ROUTE_TIMER
import com.example.focustimer.core.navigation.NavigationConstants
import com.example.focustimer.feature_onboarding.presentation.factory.OnboardingViewModelFactory
import com.example.focustimer.feature_onboarding.presentation.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModelFactory(LocalContext.current))
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val pages by viewModel.pages.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        TextButton(
            onClick = {
                viewModel.completeOnboarding()
                navController.navigate(ROUTE_TIMER) {
                    popUpTo(NavigationConstants.ROUTE_ONBOARDING) { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Saltar",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Contenido principal
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pager de contenido
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (pages.isNotEmpty()) {
                    // Contenido de la página actual
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icono circular con forma de flor
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        ),
                                        radius = 180f
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = pages[currentPage].icon,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // Textos
                        Text(
                            text = pages[currentPage].title,
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = pages[currentPage].description,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 48.dp)
                        )
                    }
                }
            }

            // Indicador de página
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                // Dots indicadores
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (index == currentPage) 10.dp else 8.dp)
                            .background(
                                if (index == currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!viewModel.nextPage()) {
                        // Última página, completar onboarding y navegar al timer
                        viewModel.completeOnboarding()
                        navController.navigate(ROUTE_TIMER) {
                            popUpTo(NavigationConstants.ROUTE_ONBOARDING) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .width(200.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = if (currentPage == pages.size - 1) "Comenzar" else "Siguiente",
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}