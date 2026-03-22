package org.kristof.hbviewer.android

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import org.kristof.hbviewer.ViewerApp
import org.kristof.hbviewer.ViewerController
import org.kristof.hbviewer.InMemoryViewerStore

class MainActivity : ComponentActivity() {
    private lateinit var controller: ViewerController

    private val openDocument = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return@registerForActivityResult
        controller.import(uri.lastPathSegment ?: "homebank.xhb", text)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = ViewerController(InMemoryViewerStore())
        setContent {
            ViewerApp(
                controller = controller,
                onOpenFile = { openDocument.launch(arrayOf("*/*")) }
            )
        }
    }
}
