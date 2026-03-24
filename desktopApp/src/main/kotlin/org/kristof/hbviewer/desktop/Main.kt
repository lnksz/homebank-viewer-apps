package org.kristof.hbviewer.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.kristof.hbviewer.ViewerApp
import org.kristof.hbviewer.ViewerController
import org.kristof.hbviewer.InMemoryViewerStore

fun main() = application {
    FileKit.init(appId = "org.kristof.hbviewer")
    val controller = ViewerController(InMemoryViewerStore())

    Window(onCloseRequest = ::exitApplication, title = "HBViewer") {
        val filePicker = rememberFilePickerLauncher(
            type = FileKitType.File("xhb")
        ) { file ->
            file ?: return@rememberFilePickerLauncher
            controller.import(file.name, kotlinx.coroutines.runBlocking { file.readString() })
        }

        ViewerApp(
            controller = controller,
            onOpenFile = {
                filePicker.launch()
            }
        )
    }
}
