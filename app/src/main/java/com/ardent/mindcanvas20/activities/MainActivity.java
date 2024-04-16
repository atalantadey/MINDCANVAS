package com.ardent.mindcanvas20.activities;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.ardent.mindcanvas20.R;
import com.ardent.mindcanvas20.adapters.NoteAdapter;
import com.ardent.mindcanvas20.database.NotesDatabase;
import com.ardent.mindcanvas20.entities.Note;
import com.ardent.mindcanvas20.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;

//TODO NAVIGATION MENU

public class MainActivity extends AppCompatActivity implements NotesListener {
    public static final int REQUEST_CODE_ADD_NOTE =1;
    public static final int REQUEST_CODE_UPDATE_NOTE=2;
    public static final int REQUEST_CODE_SHOW_NOTE=3;
    private RecyclerView notesRecyclerView;
    ImageView imageAddNoteMain,navMenu;
    private List<Note> noteList;
    private NoteAdapter noteAdapter;
    private int NoteClickedPosition=-1;
    ConstraintLayout mainlayout;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        imageAddNoteMain =findViewById(R.id.image_addNoteMain);
        notesRecyclerView=findViewById(R.id.rv_notes);
        navMenu=findViewById(R.id.iv_nav_menu);
        mainlayout=findViewById(R.id.main);
        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        noteList= new ArrayList<>();
        noteAdapter =new NoteAdapter(noteList,this);
        notesRecyclerView.setAdapter(noteAdapter);

        navMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }

        });

        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), createnote.class),REQUEST_CODE_ADD_NOTE
                );
            }
        });//Add Note
        getNotes(REQUEST_CODE_SHOW_NOTE,false);//retrive Notes from the db

        EditText SearchNote=findViewById(R.id.inputSearch);
        SearchNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.CancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size()!=0){
                    noteAdapter.searchNote(s.toString());
                }
            }
        });
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        NoteClickedPosition=position;
        Intent intent=new Intent(getApplicationContext(), createnote.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("note",note);
        startActivityForResult(intent,REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final int requestCode,final boolean isNoteDeleted){

        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void,Void, List<Note>>{
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                //We checked if the note list is empty it means the app is just started since we have declared it as a global variable, in
                //this case, we are adding all notes from the database to this note list and notify the adapter about the new dataset. In
                //another case, if the note list is not empty then it means notes are already loaded from the database so we are just
                //adding only the latest note to the note list and notify adapter about new note inserted. And last we scrolled our
                //recycler view to the top.
                /*if(noteList.size()==0){
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                }else {
                    noteList.add(0,notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                }notesRecyclerView.smoothScrollToPosition(0);*/

                if(requestCode==REQUEST_CODE_SHOW_NOTE){
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                }else if(requestCode==REQUEST_CODE_ADD_NOTE){
                    noteList.add(0,notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                }else if(requestCode==REQUEST_CODE_UPDATE_NOTE){
                    noteList.remove(NoteClickedPosition);
                    if(isNoteDeleted){
                        noteAdapter.notifyItemRemoved(NoteClickedPosition);
                    }else{
                        noteList.add(NoteClickedPosition,notes.get(NoteClickedPosition));
                        noteAdapter.notifyItemChanged(NoteClickedPosition);
                    }

                }

            }
        }
        new GetNotesTask().execute();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_ADD_NOTE && resultCode==RESULT_OK){
            getNotes(REQUEST_CODE_ADD_NOTE,false);
        }else if(requestCode==REQUEST_CODE_UPDATE_NOTE && resultCode==RESULT_OK){
            if(data!=null){
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
            }
        }
    }
}