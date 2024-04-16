package com.ardent.mindcanvas20.listeners;

import com.ardent.mindcanvas20.entities.Note;

public interface NotesListener {
     default void onNoteClicked(Note note, int position){

    }
}
