package com.itlab.data.repository

import com.itlab.data.dao.FolderDao
import com.itlab.data.mapper.NoteFolderMapper
import com.itlab.domain.model.NoteFolder
import com.itlab.domain.repository.NoteFolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteFolderRepositoryImpl(
    private val folderDao: FolderDao,
    private val mapper: NoteFolderMapper,
) : NoteFolderRepository {
    override suspend fun createFolder(folder: NoteFolder): String {
        folderDao.insert(mapper.toEntity(folder))
        return folder.id
    }

    override fun observeFolders(): Flow<List<NoteFolder>> =
        folderDao.getAllFolders().map { entities ->
            entities.map { mapper.toDomain(it) }
        }

    override suspend fun renameFolder(
        id: String,
        name: String,
    ) {
        folderDao.updateName(id, name)
    }

    override suspend fun deleteFolder(id: String) {
        folderDao.getFolderById(id)?.let {
            folderDao.delete(it)
        }
    }

    override suspend fun getFolderById(id: String): NoteFolder? =
        folderDao.getFolderById(id)?.let {
            mapper.toDomain(it)
        }

    override suspend fun updateFolder(folder: NoteFolder) {
        folderDao.update(mapper.toEntity(folder))
    }
}
