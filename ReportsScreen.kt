package com.nibm.tmapp.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReportsScreen(reportsViewModel: ReportsViewModel = viewModel()) {
    val allSubmissions by reportsViewModel.allSubmissions.collectAsState()

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "All Submissions",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        if (allSubmissions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No submissions found.")
            }
        } else {
            LazyColumn {
                items(allSubmissions) { submission ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Assignment ID: ${submission.assignmentId}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Student: ${submission.studentName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            submission.fileName?.let { fileName ->
                                Text(
                                    text = "File: $fileName",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "Submitted: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(submission.submittedAt)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Status: ${submission.status}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            submission.grade?.let { grade ->
                                Text(
                                    text = "Grade: $grade",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            submission.feedback?.let { feedback ->
                                Text(
                                    text = "Feedback: $feedback",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
