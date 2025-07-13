package com.nibm.tmapp.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import android.widget.Toast
import java.util.*
import com.google.firebase.auth.FirebaseAuth

import com.nibm.tmapp.model.Assignment
import com.nibm.tmapp.model.Submission



@Composable
fun ViewAssignmentsScreen(viewAssignmentsViewModel: ViewAssignmentsViewModel = viewModel()) {
    val assignmentListState by viewAssignmentsViewModel.assignmentListState.collectAsState()

    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (assignmentListState) {
                is AssignmentListState.Loading -> {
                    CircularProgressIndicator()
                }
                is AssignmentListState.Success -> {
                    val assignments = (assignmentListState as AssignmentListState.Success).assignments
                    if (assignments.isEmpty()) {
                        Text(
                            text = "No assignments found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                        ) {
                            items(assignments) { assignment ->
                                val context = LocalContext.current
                                AssignmentItem(assignment = assignment, context = context, viewAssignmentsViewModel = viewAssignmentsViewModel)
                            }
                        }
                    }
                }
                is AssignmentListState.Error -> {
                    val message = (assignmentListState as AssignmentListState.Error).message
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
fun AssignmentItem(assignment: Assignment, context: Context, viewAssignmentsViewModel: ViewAssignmentsViewModel) {
    var isUploading by remember { mutableStateOf(false) }

    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = assignment.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Due on: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(assignment.dueDate)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(12.dp))
            val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                uri: Uri? ->
                uri?.let { fileUri ->
                    // Placeholder for assignmentId. You'll need to pass the actual assignment ID here.
                    val assignmentId = assignment.documentId // Assuming 'assignment' object has an 'id' property
                    val fileName = "assignment_${System.currentTimeMillis()}" // Generate unique filename
                    isUploading = true
                    uploadAssignmentToFirebaseStorage(assignmentId, fileName, fileUri, context) { success ->
                        isUploading = false
                    }
                }
            }

            val studentId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val studentSubmission by viewAssignmentsViewModel.getStudentSubmissionForAssignment(assignment.documentId, studentId).collectAsState()

            Spacer(modifier = Modifier.height(12.dp))

            studentSubmission?.let { submission ->
                Text(
                    text = "Your Submission Status: ${submission.status}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(4.dp))
                submission.fileName?.let { filename ->
                    Text(
                        text = "Submitted File: $filename",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                submission.grade?.let { grade ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Grade: $grade",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                submission.feedback?.let { feedback ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Feedback: $feedback",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (studentSubmission == null || studentSubmission?.status == "Submitted") { // Allow re-upload if not graded yet
                Button(onClick = { pickFileLauncher.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Upload Assignment")
                }
            } else {
                // Optionally show a disabled button or message if already graded
                Button(onClick = { /* Do nothing */ }, enabled = false, modifier = Modifier.fillMaxWidth()) {
                    Text("Assignment Graded")
                }
            }
        }
    }
}

fun uploadAssignmentToFirebaseStorage(assignmentId: String, fileName: String, fileUri: Uri, context: Context, onComplete: (Boolean) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val assignmentRef = storageRef.child("assignments/$assignmentId/$fileName")

    assignmentRef.putFile(fileUri)
        .addOnSuccessListener { taskSnapshot ->
            assignmentRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                val db = FirebaseFirestore.getInstance()
                val currentUser = FirebaseAuth.getInstance().currentUser
                val studentId = currentUser?.uid ?: ""
                val studentName = currentUser?.displayName ?: "Anonymous Student"

                val submission = Submission(
                    assignmentId = assignmentId,
                    studentId = studentId,
                    studentName = studentName,
                    fileUrl = downloadUrl,
                    fileName = fileName,
                    submittedAt = Date()
                )

                db.collection("submissions").add(submission)
                    .addOnSuccessListener { 
                        Toast.makeText(context, "Assignment uploaded and link updated!", Toast.LENGTH_SHORT).show()
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                        onComplete(false)
                    }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to get download URL: ${e.message}", Toast.LENGTH_LONG).show()
                onComplete(false)
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Upload failed: ${exception.message}", Toast.LENGTH_LONG).show()
            onComplete(false)
        }
}
