package com.nibm.tmapp.student

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nibm.tmapp.model.CourseMaterial
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ViewCourseMaterialsScreen(viewCourseMaterialsViewModel: ViewCourseMaterialsViewModel = viewModel()) {
    val courseMaterialListState by viewCourseMaterialsViewModel.courseMaterialListState.collectAsState()

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (courseMaterialListState) {
                is CourseMaterialListState.Loading -> {
                    CircularProgressIndicator()
                }
                is CourseMaterialListState.Success -> {
                    val materials = (courseMaterialListState as CourseMaterialListState.Success).materials
                    if (materials.isEmpty()) {
                        Text(
                            text = "No course materials found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 64.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                        ) {
                            items(materials) { material ->
                                CourseMaterialItem(material = material)
                            }
                        }
                    }
                }
                is CourseMaterialListState.Error -> {
                    val message = (courseMaterialListState as CourseMaterialListState.Error).message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CourseMaterialItem(material: CourseMaterial) {
    val context = LocalContext.current
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .padding(vertical = 10.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW)
                val uri = Uri.parse(material.url)
                if (material.fileName.endsWith(".pdf", ignoreCase = true)) {
                    intent.setDataAndType(uri, "application/pdf")
                } else {
                    intent.setData(uri)
                }
                context.startActivity(intent)
            }
            .semantics { contentDescription = "Course material: ${material.title}" },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp) // Spacing between items
        ) {
            Text(
                text = material.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = material.fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            material.uploadedAt?.let {
                Text(
                    text = "Uploaded: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
