package fr.uge.plutus.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

operator fun PaddingValues.plus(size: Dp): PaddingValues = PaddingValues(
    calculateLeftPadding(LayoutDirection.Ltr) + size,
    calculateTopPadding() + size,
    calculateRightPadding(LayoutDirection.Ltr) + size,
    calculateBottomPadding() + size
)