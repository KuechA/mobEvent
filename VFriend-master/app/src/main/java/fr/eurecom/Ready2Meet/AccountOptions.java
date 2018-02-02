package fr.eurecom.Ready2Meet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import de.hdodenhof.circleimageview.CircleImageView;
import fr.eurecom.Ready2Meet.database.User;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountOptions extends Fragment {

    private String oldName, oldMail;

    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private static final int PICK_GALLERY = 2;
    private String pictureUri = null;

    public AccountOptions() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_options, container, false);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null) {
                    //user auth state is changed - user is null
                    //launch login activity
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
            }
        };

        final String uid = user.getUid();

        final EditText nameText = (EditText) view.findViewById(R.id.username);
        final EditText mailText = (EditText) view.findViewById(R.id.mail);
        final CircleImageView imgView = (CircleImageView) view.findViewById(R.id
                .change_profile_picture);
        mailText.setText(user.getEmail());
        oldMail = user.getEmail();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Users/" + uid);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                nameText.setText(user.DisplayName);
                oldName = user.DisplayName;
                Picasso.with(getContext()).load(user.ProfilePictureURL).fit().into(imgView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: Error handling
            }
        });

        // Open image selection dialog by clicking on image
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_GALLERY);
            }
        });

        // Cancel this dialog -> Go back
        Button cancelButton = (Button) view.findViewById(R.id.cancel_action);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        Button okButton = (Button) view.findViewById(R.id.set_changes);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Try to set mail if it has changed
                String newMail = mailText.getText().toString().trim();
                if(! oldMail.equals(newMail)) {
                    if(user != null && ! newMail.equals("")) {
                        user.updateEmail(newMail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Email address is updated. " +
                                            "Please sign in with new email id!", Toast
                                            .LENGTH_LONG).show();
                                    signOut();
                                    progressBar.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(getActivity(), "Failed to update email!",
                                            Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
                    } else if(newMail.equals("")) {
                        mailText.setError("Enter email");
                    }
                }

                String newName = nameText.getText().toString();
                if(oldName.equals(newName)) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("Users").child(uid).child("DisplayName").setValue(newName);
                    Toast.makeText(getActivity(), "Username Updated!", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Sign out
        Button signOut = (Button) view.findViewById(R.id.sign_out);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        // Show dialog to change password
        Button resetPassword = (Button) view.findViewById(R.id.change_password_button);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = inflater.inflate(R.layout.reset_password_dialog, null);
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext
                        ());
                alertDialogBuilder.setView(dialogView);

                final EditText newPassword = (EditText) dialogView.findViewById(R.id.newPassword);
                final EditText newPasswordConfirmation = (EditText) dialogView.findViewById(R.id
                        .newPasswordConfirmation);

                // set dialog message
                alertDialogBuilder.setCancelable(true).setPositiveButton("OK", new
                        DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(newPassword.getText().toString().equals(newPasswordConfirmation
                                .getText().toString())) {
                            user.updatePassword(newPassword.getText().toString().trim())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Password is updated, " +
                                                "please sign in with the new password!", Toast
                                                .LENGTH_SHORT).show();
                                        signOut();
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to update " +
                                                "password!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        // Send mail to reset password
        Button sendEmail = (Button) view.findViewById(R.id.sending_pass_reset_button);
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.sendPasswordResetEmail(oldMail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Reset password email is sent!", Toast
                                    .LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to send reset email!", Toast
                                    .LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Remove the user
        Button btnRemoveUser = (Button) view.findViewById(R.id.remove_user_button);
        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user != null) {
                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Your profile is deleted:( Create "
                                        + "a new account now!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), SignupActivity.class));
                                getActivity().finish();
                            } else {
                                Toast.makeText(getActivity(), "Failed to delete your account!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    //sign out method

    public void signOut() {
        auth.signOut();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == PICK_GALLERY && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            StorageReference storage = FirebaseStorage.getInstance().getReference().child
                    ("ProfilePictures").child(auth.getCurrentUser().getUid());

            storage.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask
                    .TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    pictureUri = downloadUri.toString();
                    FirebaseDatabase.getInstance().getReference().child("Users").child(auth
                            .getCurrentUser().getUid()).child("ProfilePictureURL").setValue
                            (pictureUri);
                    Toast.makeText(getActivity(), "Done", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), "Couldn't upload image to database", Toast
                            .LENGTH_LONG).show();
                }
            });
        }

    }

}
