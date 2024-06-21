package com.example.composenotes

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Note(val id: String, val title: String)

class MainActivity : ComponentActivity() {

    private val firestore: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirestoreApp()
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun FirestoreApp() {
        var notes by remember { mutableStateOf(listOf<Note>()) }
        var NoteTitle by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            firestore.collection("notes").addSnapshotListener { value, _ ->
                Log.d("test","test---$value")
                notes = value?.map { doc ->
                    Note(doc.id, doc.getString("title") ?: "")
                } ?: listOf()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Notes") })
            },
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = NoteTitle,
                        onValueChange = { NoteTitle = it },
                        label = { Text("Note Title") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val newNote = hashMapOf("title" to NoteTitle)
                        firestore.collection("notes").add(newNote)
                        NoteTitle = ""
                    }) {
                        Text("Add Note")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(notes) { note ->
                            NoteItem(note = note)
                        }
                    }
                }
            }
        )
    }

    @Composable
    fun NoteItem(note: Note) {
        var editMode by remember { mutableStateOf(false) }
        var noteTitle by remember { mutableStateOf(note.title) }

        if (editMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it }
                )
                Button(onClick = {
                    firestore.collection("notes").document(note.id).update("title", noteTitle)
                    editMode = false
                }) {
                    Text("Update")
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(note.title)
                Row {
                    Button(onClick = { editMode = true }) {
                        Text("Edit")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        firestore.collection("notes").document(note.id).delete()
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
