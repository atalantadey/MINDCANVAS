package com.ardent.mindcanvas20.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.ardent.mindcanvas20.R;
import com.ardent.mindcanvas20.database.NotesDatabase;
import com.ardent.mindcanvas20.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class createnote extends AppCompatActivity {

    private EditText noteTitle,noteSubtitle,noteContent;
    private TextView noteDateTime,tv_WebUrl;
    private ImageView imageSave,imageback,imageNote;
    private String SelectedColour,selectedImagePath;
    private View viewSubtitleIndicator;
    private static final int REQUEST_CODE_STORAGE_PERMISSION=1;
    private static final int REQUEST_CODE_SELECT_IMAGE=2;
    private LinearLayout layout_WebUrl;
    private AlertDialog dialogAddUrl,dialogDeleteNote;
    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_createnote);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        noteTitle=findViewById(R.id.inputNoteTitle);
        noteSubtitle=findViewById(R.id.inputNoteSubtitle);
        noteContent=findViewById(R.id.inputNote);
        noteDateTime=findViewById(R.id.textDateTime);
        imageback=findViewById(R.id.imageBack);
        imageSave=findViewById(R.id.imageSave);
        imageNote=findViewById(R.id.imageNote);
        layout_WebUrl=findViewById(R.id.ll_webUrlCreateNote);
        tv_WebUrl=findViewById(R.id.tv_UrlCreateNote);

        viewSubtitleIndicator=findViewById(R.id.ViewSubtitleIndicator);
        //set Date and Time
        noteDateTime.setText(
                new SimpleDateFormat("EEEE,dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date())
        );
        //on pressing Save Button
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
        //On pressing Back button
        imageback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //on Clicking Options
        SelectedColour="#1D0050";
        selectedImagePath="";

        if(getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote=(Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.iv_Remove_URL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_WebUrl.setText(null);
                layout_WebUrl.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.iv_Remove_Image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.iv_Remove_Image).setVisibility(View.GONE);
                selectedImagePath="";
            }
        });

        initOptions();
        setNoteSubtitleIndicatorColor();
    }
    private void saveNote(){
        if(noteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note Title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }else if(noteTitle.getText().toString().trim().isEmpty() && noteContent.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note can't Be Empty", Toast.LENGTH_SHORT).show();
            return;}

        final Note note =new Note();
        note.setTitle(noteTitle.getText().toString());
        note.setSubtitle(noteSubtitle.getText().toString());
        note.setContent(noteContent.getText().toString());
        note.setDate_time(noteDateTime.getText().toString());
        note.setColor(SelectedColour);
        note.setImage_path(selectedImagePath);

        if(alreadyAvailableNote!=null){
            note.setId(alreadyAvailableNote.getId());
        }

        if(layout_WebUrl.getVisibility()==View.VISIBLE){
            note.setWeb_link(tv_WebUrl.getText().toString());
        }
        @SuppressLint("StaticFieldLeak")
        class saveNoteTask extends AsyncTask<Void,Void,Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent=new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new saveNoteTask().execute();
    }
    private void initOptions(){
        final LinearLayout layoutOptions=findViewById(R.id.layoutOptions);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior= BottomSheetBehavior.from(layoutOptions);
        layoutOptions.findViewById(R.id.textOptions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        final ImageView imageColor1= layoutOptions.findViewById(R.id.image_color1);
        final ImageView imageColor2= layoutOptions.findViewById(R.id.image_color2);
        final ImageView imageColor3= layoutOptions.findViewById(R.id.image_color3);
        final ImageView imageColor4= layoutOptions.findViewById(R.id.image_color4);
        final ImageView imageColor5= layoutOptions.findViewById(R.id.image_color5);

        layoutOptions.findViewById(R.id.viewcolor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedColour = "#fdffb6";
                imageColor1.setImageResource(R.drawable.baseline_done_outline_24);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteSubtitleIndicatorColor();
            }
        });
        layoutOptions.findViewById(R.id.viewcolor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedColour = "#caffbf";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.baseline_done_outline_24);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteSubtitleIndicatorColor();
            }
        });
        layoutOptions.findViewById(R.id.viewcolor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedColour = "#9bf6ff";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.baseline_done_outline_24);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setNoteSubtitleIndicatorColor();
            }
        });
        layoutOptions.findViewById(R.id.viewcolor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedColour = "#a0c4ff";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.baseline_done_outline_24);
                imageColor5.setImageResource(0);
                setNoteSubtitleIndicatorColor();
            }
        });
        layoutOptions.findViewById(R.id.viewcolor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedColour = "#ffc6ff";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.baseline_done_outline_24);
                setNoteSubtitleIndicatorColor();
            }
        });

        if(alreadyAvailableNote!=null&& alreadyAvailableNote.getColor()!=null && !alreadyAvailableNote.getColor().trim().isEmpty() ){
            switch (alreadyAvailableNote.getColor()){
                case "#fdffb6":
                    layoutOptions.findViewById(R.id.viewcolor1).performClick();
                    break;
                case "#caffbf":
                    layoutOptions.findViewById(R.id.viewcolor2).performClick();
                    break;
                case "#9bf6ff":
                    layoutOptions.findViewById(R.id.viewcolor3).performClick();
                    break;
                case "#a0c4ff":
                    layoutOptions.findViewById(R.id.viewcolor4).performClick();
                    break;
                case "#ffc6ff":
                    layoutOptions.findViewById(R.id.viewcolor5).performClick();
                    break;
            }
        }

        layoutOptions.findViewById(R.id.layout_addImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)==
                        PackageManager.PERMISSION_DENIED){
                    Toast.makeText(getApplicationContext(), "step 1", Toast.LENGTH_SHORT).show();
                    //request permission
                    ActivityCompat.requestPermissions(
                            createnote.this,
                            new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else{
                    selectImage();
                    Toast.makeText(getApplicationContext(), "called selectImage()", Toast.LENGTH_SHORT).show();
                }
            }
        });//add Image
        layoutOptions.findViewById(R.id.layout_addUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddDialog();
            }
        });//add URL

        if(alreadyAvailableNote!=null){
            layoutOptions.findViewById(R.id.layout_delNote).setVisibility(View.VISIBLE);
            layoutOptions.findViewById(R.id.layout_delNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }

    }
    private void showDeleteNoteDialog(){
        if(dialogDeleteNote==null){
            AlertDialog.Builder builder=new AlertDialog.Builder(createnote.this);
            View view =LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,(ViewGroup) findViewById(R.id.Layout_delNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote=builder.create();
            if(dialogDeleteNote.getWindow()!=null){
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.tv_delNotebtn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent =new Intent();
                            intent.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.tv_Canceldel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    }
    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_CODE_SELECT_IMAGE);
            Toast.makeText(createnote.this, "select Image Done", Toast.LENGTH_SHORT).show();
        }
    }
    private void setNoteSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable =(GradientDrawable) viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(SelectedColour));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "Reached OnActivityResult", Toast.LENGTH_SHORT).show();
        if(requestCode== REQUEST_CODE_SELECT_IMAGE && resultCode==RESULT_OK){
            if(data!=null){
                Uri selectedImageUri=data.getData();
                if(selectedImageUri!=null){
                    try{
                        InputStream inputStream= getContentResolver()
                                .openInputStream(selectedImageUri);
                        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.iv_Remove_Image).setVisibility(View.VISIBLE);
                        selectedImagePath =getpathFromUri(selectedImageUri);
                        Toast.makeText(this, "Image Displayed", Toast.LENGTH_SHORT).show();
                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
    private String getpathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor =getContentResolver().query(contentUri,null,null,null,null);
        if(cursor==null){
            filePath=contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index=cursor.getColumnIndex("_data");
            filePath=cursor.getString(index);
            cursor.close();
        }return  filePath;
    }
    private void showAddDialog(){
        if(dialogAddUrl==null){
            AlertDialog.Builder builder= new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url,(ViewGroup) findViewById(R.id.Layout_addURLContainer));
            builder.setView(view);
            dialogAddUrl =builder.create();
            if(dialogAddUrl.getWindow()!=null){
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            final EditText inputUrl =view.findViewById(R.id.inputURL);
            inputUrl.requestFocus();
            view.findViewById(R.id.tv_addUrl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(inputUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(getApplicationContext(), "Enter URL", Toast.LENGTH_SHORT).show();
                    }else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString().trim()).matches()){
                        Toast.makeText(getApplicationContext(), "Enter a Valid URL", Toast.LENGTH_SHORT).show();
                    }else{
                        tv_WebUrl.setText(inputUrl.getText().toString());
                        layout_WebUrl.setVisibility(View.VISIBLE);
                        dialogAddUrl.dismiss();
                    }
                }
            });
            view.findViewById(R.id.tv_CancelUrl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddUrl.dismiss();
                }
            });
        }dialogAddUrl.show();
    }
    private void setViewOrUpdateNote(){
        noteTitle.setText(alreadyAvailableNote.getTitle());
        noteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        noteContent.setText(alreadyAvailableNote.getContent());
        noteDateTime.setText(alreadyAvailableNote.getDate_time());
        if(alreadyAvailableNote.getImage_path()!=null && !alreadyAvailableNote.getImage_path().trim().isEmpty()){
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImage_path()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.iv_Remove_Image).setVisibility(View.VISIBLE);
            selectedImagePath=alreadyAvailableNote.getImage_path();
        }//set Image
        if(alreadyAvailableNote.getWeb_link()!=null && !alreadyAvailableNote.getWeb_link().trim().isEmpty()){
            tv_WebUrl.setText(alreadyAvailableNote.getWeb_link());
            layout_WebUrl.setVisibility(View.VISIBLE);
        }
    }
}