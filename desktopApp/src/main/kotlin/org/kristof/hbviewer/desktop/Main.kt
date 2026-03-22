package org.kristof.hbviewer.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.kristof.hbviewer.ViewerApp
import org.kristof.hbviewer.ViewerController
import org.kristof.hbviewer.InMemoryViewerStore
import java.io.File
import javax.swing.JFileChooser

fun main() = application {
    val controller = ViewerController(InMemoryViewerStore())

    Window(onCloseRequest = ::exitApplication, title = "HBViewer") {
        ViewerApp(
            controller = controller,
            onOpenFile = {
                val chooser = JFileChooser().apply {
                    dialogTitle = "Open HomeBank .xhb file"
                }
                val result = chooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val file: File = chooser.selectedFile
                    controller.import(file.name, file.readText())
                }
            }
        )
    }
}
