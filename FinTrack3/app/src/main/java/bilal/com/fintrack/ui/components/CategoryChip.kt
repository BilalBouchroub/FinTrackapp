package bilal.com.fintrack.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bilal.com.fintrack.data.local.entities.Category

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = { onSelected(!isSelected) },
        label = { Text(category.name) },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        modifier = modifier
    )
}
