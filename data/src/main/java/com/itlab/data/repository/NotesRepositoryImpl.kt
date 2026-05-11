package com.itlab.data.repository

import com.itlab.data.dao.MediaDao
import com.itlab.data.dao.NoteDao
import com.itlab.data.mapper.NoteMapper
import com.itlab.domain.model.Note
import com.itlab.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotesRepositoryImpl(
    private val noteDao: NoteDao,
    private val mediaDao: MediaDao,
    private val mapper: NoteMapper,
) : NotesRepository {
    override fun observeNotes(): Flow<List<Note>> =
        noteDao.getAllNotes().map { entities ->
            entities.map { mapper.toDomain(it) }
        }

    override fun observeNotesByFolder(folderId: String): Flow<List<Note>> =
        noteDao.getNotesByFolder(folderId).map { entities ->
            entities.map { mapper.toDomain(it) }
        }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteByld(id)?.let {
            mapper.toDomain(it)
        }

    override suspend fun createNote(note: Note): String {
        val (notesEntity, mediaEntities) = mapper.toEntities(note)
        noteDao.insert(notesEntity)
        if (mediaEntities.isNotEmpty()) mediaDao.insertAll(mediaEntities)
        return note.id
    }

    override suspend fun updateNote(note: Note) {
        val (noteEntity, mediaEntities) = mapper.toEntities(note)
        noteDao.update(noteEntity)

        mediaDao.deleteByNoteId(note.id)
        if (mediaEntities.isNotEmpty()) mediaDao.insertAll(mediaEntities)
    }

    override suspend fun deleteNote(id: String) {
        noteDao.getNoteByld(id)?.let { noteDao.delete(it) }
    }
}
