package com.ardent.mindcanvas20.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ardent.mindcanvas20.R;
import com.ardent.mindcanvas20.entities.Note;
import com.ardent.mindcanvas20.listeners.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** @noinspection ALL*/
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{

    private List<Note> notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Note> noteSource;
    public NoteAdapter(List<Note> notes, NotesListener  notesListener) {
        this.notes = notes;
        this.notesListener=notesListener;
        noteSource=notes;
    }
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,parent,false
                )
        );
    }
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position),position);
            }
        });

    }
    @Override
    public int getItemCount() {
        return notes.size();
    }
    @Override
    public int getItemViewType(int position) {
        return (position);
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView textTitle,textSubtitle,textDateTime;
        LinearLayout layoutNote;
        RoundedImageView RoundedimageNote;
        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle=itemView.findViewById(R.id.TextTitle);
            textSubtitle=itemView.findViewById(R.id.TextSubtitle);
            textDateTime=itemView.findViewById(R.id.textDate);
            layoutNote=itemView.findViewById(R.id.Layout_note);
            RoundedimageNote=itemView.findViewById(R.id.RoundedimageNote);
        }
        void setNote(Note note){
            textTitle.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty()){
                textSubtitle.setVisibility(View.GONE);
            }else{
                textSubtitle.setText(note.getSubtitle());
            }
            textDateTime.setText(note.getDate_time());

            GradientDrawable gradientDrawable;
            gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if(note.getColor()!=null){
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            }else{
                gradientDrawable.setColor(Color.parseColor("#4E47C6"));
            }
            if(note.getImage_path()!=null){
                RoundedimageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImage_path()));
                RoundedimageNote.setVisibility(View.VISIBLE);
            }else {
                RoundedimageNote.setVisibility(View.GONE);
            }
        }
    }
    public void searchNote(final String searchKeyword){
        timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    notes=noteSource;
                }else{
                    ArrayList<Note> temp=new ArrayList<>();
                    for(Note note:noteSource){
                        if(note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase()) ||
                                note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())||
                                note.getContent().toLowerCase().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    notes=temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        },500);
    }
    public void CancelTimer(){
        if(timer!=null){
            timer.cancel();
        }
    }
}
