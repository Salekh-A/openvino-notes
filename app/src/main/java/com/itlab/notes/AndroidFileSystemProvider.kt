package com.itlab.notes

import android.content.Context
import com.itlab.domain.app.FileSystemProvider
import java.io.File
import java.io.InputStream

class AndroidFileSystemProvider(
    private val context: Context,
) : FileSystemProvider {
    override fun openAsset(fileName: String): InputStream = context.assets.open(fileName)

    override fun listAssets(path: String): Array<String>? = context.assets.list(path)

    override fun getFilesDir(): File = context.filesDir
}
