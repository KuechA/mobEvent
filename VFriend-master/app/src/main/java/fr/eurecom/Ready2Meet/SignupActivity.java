package fr.eurecom.Ready2Meet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import fr.eurecom.Ready2Meet.database.User;

public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, name;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private Uri imageUri = Uri.parse("https://firebasestorage.googleapis" + "" + "" + "" + "" +
            "" + ".com/v0/b/ready2meet-e0286.appspot" + "" + "" + "" + "" + "" + "" + "" + "" +
            "" + ".com/o/ProfilePictures%2FDefaultProfilePicture" + "" + "" + "" + "" + "" + "" +
            "" + ".jpg?alt=media&token=56bc3fe3-c68d-4d6e-80aa-135c762c0635");

    private InputStream inputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ImageView imgview = (ImageView) findViewById(R.id.imageView);
        inputStream = getResources().openRawResource(R.raw.default_profile_picture);
        Picasso.with(getApplicationContext()).load(R.raw.default_profile_picture).fit().into
                (imgview);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        Button btnSignIn = (Button) findViewById(R.id.sign_in_button);
        Button btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        name = (EditText) findViewById(R.id.displayname);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Button btnResetPassword = (Button) findViewById(R.id.btn_reset_password);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                final String displayname = name.getText().toString().trim();

                if(TextUtils.isEmpty(displayname) && ! displayname.contains("/")) {
                    Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast
                            .LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast
                            .LENGTH_SHORT).show();
                    return;
                }

                if(password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 " +
                            "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" +
                            "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" +
                            "" + "" + "characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener
                        (SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" +
                                task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        FirebaseUser user = auth.getCurrentUser();

                        final String signupEUID = user.getUid();

                        StorageReference storage = FirebaseStorage.getInstance().getReference()
                                .child("ProfilePictures").child(signupEUID);
                        if(inputStream != null) {
                            storage.putStream(inputStream).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    imageUri = taskSnapshot.getDownloadUrl();
                                    FirebaseDatabase.getInstance().getReference().child("Users")
                                            .child(signupEUID).child("ProfilePictureURL")
                                            .setValue(imageUri.toString());

                                    ImageView imgview = (ImageView) findViewById(R.id.imageView);
                                    Picasso.with(getApplicationContext()).load(imageUri).fit()
                                            .into(imgview);

                                    Toast.makeText(getApplication(), "Done", Toast.LENGTH_LONG)
                                            .show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplication(), "Couldn't upload image " +
                                            "to database", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        FirebaseDatabase.getInstance().getReference().child("Events").child
                                ("-L0AEWfuhQx3DjXz7H6Q").child("current")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                int value = (dataSnapshot.getValue(Integer.class));
                                value += 1;
                                FirebaseDatabase.getInstance().getReference().child("Events")
                                        .child("-L0AEWfuhQx3DjXz7H6Q").child("current").setValue
                                        (value);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        Map<String, Boolean> events = new HashMap<>();
                        events.put("-L0AEWfuhQx3DjXz7H6Q", true);

                        User userObj = new User(displayname, events, imageUri.toString());

                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        mDatabase.child("Events").child("-L0AEWfuhQx3DjXz7H6Q").child
                                ("Participants").child(signupEUID).setValue(true);
                        mDatabase.child("Users").child(signupEUID).setValue(userObj);

                        if(! task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Authentication failed." + task
                                    .getException(), Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(SignupActivity.this, Main2Activity.class));
                            finish();
                        }
                    }
                });

            }
        });

        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 2 && resultCode == RESULT_OK) {
            imageUri = data.getData();

            ImageView imgview = (ImageView) findViewById(R.id.imageView);
            Picasso.with(getApplicationContext()).load(imageUri).fit().into(imgview);
            try {
                inputStream = getContentResolver().openInputStream(imageUri);
            } catch(FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find selected file", Toast
                        .LENGTH_LONG).show();
            }
        }

    }
}
