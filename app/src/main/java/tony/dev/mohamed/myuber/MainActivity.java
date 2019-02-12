package tony.dev.mohamed.myuber;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dmax.dialog.SpotsDialog;
import tony.dev.mohamed.myuber.Retrofit.Common;
import tony.dev.mohamed.myuber.models.UberDriver;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "hash_key";
    private static final int APP_REQUEST_CODE = 1000;
    //aleart dialog for waiting
    public SpotsDialog spotsDialog;
    //mAuth for Firebase Authentication
    private FirebaseAuth mAuth;
    //mDatabase for FirebaseDatabase Object
    private FirebaseDatabase mDatabase;
    //mRefUsers for storing firebasedatabase refrence
    private DatabaseReference mRefUsers;
    //tow button varaibles for signIn andSignUp
    private Button signIn_btn, signUp_btn;
    // eidttext for getting uberDriver informations
    private EditText email_edit_signup, pass_edit_signup, email_edit, pass_edit, name_edit, phone_edit;
    // the view for the snackbar
    private RelativeLayout relativeLayout;
    //the view for forget text view
    private TextView forget_pass;
    //for custom font to the whole app
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize mAuth
        mAuth = FirebaseAuth.getInstance();
        //intitialize firebase database
        mDatabase = FirebaseDatabase.getInstance();
        //intitialize mRefUsers
        mRefUsers = mDatabase.getReference().child(Common.user_driver_tbl);
        //init views
        relativeLayout = findViewById(R.id.myRelative);
        //init forget text view
        forget_pass = findViewById(R.id.forget_pass);
        //set onClickListener for text view
        forget_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showing aleart dialog to reset password
                showResetDialog();
            }
        });

        //to get the hash key for facebook developer account
        //    printHashKey(this);

    }

    public void printHashKey(Context pContext) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i(TAG, "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "printHashKey()", e);
        } catch (Exception e) {
            Log.e(TAG, "printHashKey()", e);
        }
    }

    //method for aleart dialog to reset password
    private void showResetDialog() {
        //creating aleart dialog builder object
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //setting cancelable to false
        builder.setCancelable(false);
        //set title
        builder.setTitle("RESET PASSWORD");
        //set message
        builder.setMessage("Please Enter Your email to reset the password");
        /**
         * getting layoutinflater object
         *
         */
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //convert xml file into corespondings views objects
        final View view = inflater.inflate(R.layout.reset_password, null);
        //intialize reset email edit text
        final AppCompatEditText resetEmail = view.findViewById(R.id.reset_email);
        //setting cancel button
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //hide the dailog
                dialogInterface.dismiss();
            }
        });
        //seeting reset button
        builder.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //showing spots dialog
                final SpotsDialog dialog = new SpotsDialog(MainActivity.this, "Please Wait...");
                dialog.show();
                //getting email form uberDriver
                String emial = resetEmail.getText().toString();
                //checking if the email is empty
                if (TextUtils.isEmpty(emial)) {
                    dialog.dismiss();
                    showResetDialog();
                    Snackbar.make(relativeLayout, "Please Enter Your Email For Reset", Snackbar.LENGTH_LONG).show();
                } else {
                    //send password reset by email
                    mAuth.sendPasswordResetEmail(emial).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                Snackbar.make(relativeLayout, "Please Check Your Email", Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(relativeLayout, "Reset Filed !", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        //set the custom view
        builder.setView(view);
        //show the dialog
        builder.show();
    }

    //Signing in button
    public void signIn_btn(View view) {
        //showing aleart dialog for getting inputs from the uberDriver
        showSignInDailog();
    }

    //showing aleart dialog for signing in
    private void showSignInDailog() {
        //creating new object of aleart dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // seting a title for the aleart dailog
        builder.setTitle("Sign In");
        // seting a message for the aleart dailog
        builder.setMessage("Please Enter All Fields !");
        /* * *
         *customize the layout of the alert dialog
         *creating LayoutInflater object that convert views to java objects
         * The LayoutInflater takes XML file as an input and builds View objects from it.
         * */
        LayoutInflater inflater = LayoutInflater.from(this);
        //LayoutInflater Instantiates a layout XML file into its corresponding View objects.
        View view = inflater.inflate(R.layout.layout_signup, null);
        // set the custom layout (view after converting by LayoutInflater)
        builder.setView(view);
        //init email edit text
        email_edit_signup = view.findViewById(R.id.email_edittext_signup);
        //init pass edit text
        pass_edit_signup = view.findViewById(R.id.pass_edittext_signup);

        //text watcher to get the user email and password and save them to static varialble
        //to use them agin when the dialog is hidden and reopend
        email_edit_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Common.userSignInEmail = s.toString();
            }
        });
        pass_edit_signup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Common.userSignInPass=s.toString();
            }
        });

        if(builder!=null){
            if(Common.userSignInEmail!=null){
                email_edit_signup.setText(Common.userSignInEmail);
            }
            if(Common.userSignInPass!=null){
                pass_edit_signup.setText(Common.userSignInPass);
            }
        }
        // set button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss(hide) dialog
                dialogInterface.dismiss();
            }
        });
        // set ok button
        builder.setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss(hide) dialog
                dialogInterface.dismiss();
                //get email that uberDriver entered
                String email = email_edit_signup.getText().toString();
                //get pass that uberDriver entered
                String pass =pass_edit_signup.getText().toString();
                //check if one of the edittexts is empty
                if (TextUtils.isEmpty(email)) {
                    Snackbar.make(relativeLayout, "Please Enter Your Email !", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(pass)) {
                    Snackbar.make(relativeLayout, "Please Enter Your password !", Snackbar.LENGTH_LONG).show();
                } else {
                    signIn(email, pass);
                }
            }
        });
        //show the aleart dialog
        builder.show();
    }

    //sign up (register) button
    public void signUp_btn(View view) {
        //Show signUp dailog
        showSignUpDialog();
    }

    //showing aleart dialog for signing up
    private void showSignUpDialog() {
        //creating new object of aleart dialog builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // seting a title for the aleart dailog
        builder.setTitle("Sign Up");
        // seting a message for the aleart dailog
        builder.setMessage(R.string.dailog_msg);
        /* * *
         *customize the layout of the alert dialog
         *creating LayoutInflater object that convert views to java objects
         * The LayoutInflater takes XML file as an input and builds View objects from it.
         * */
        LayoutInflater inflater = LayoutInflater.from(this);
        //LayoutInflater Instantiates a layout XML file into its corresponding View objects.
        View view = inflater.inflate(R.layout.layout_signin, null);

        //init email edit text
        email_edit = view.findViewById(R.id.email_edittext);
        //init pass edit text
        pass_edit = view.findViewById(R.id.pass_edittext);
        //init name edit text
        name_edit = view.findViewById(R.id.name_edittext);
        //init phone edit text
        phone_edit = view.findViewById(R.id.phone_edittext);

        // set button cancel
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        //set button ok
        builder.setPositiveButton("Sign Up", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss(hide) the aleart dailog
                dialogInterface.dismiss();
                //get email that uberDriver entered
                String email = email_edit.getText().toString();
                //get name that uberDriver entered
                String name = name_edit.getText().toString();
                //get phone that uberDriver entered
                String phone = phone_edit.getText().toString();
                //get pass that uberDriver entered
                String pass = pass_edit.getText().toString();

                //check if one of the edittexts is empty
                if (TextUtils.isEmpty(email)) {
                    Snackbar.make(relativeLayout, "Please Enter Email !", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(pass)) {
                    Snackbar.make(relativeLayout, "Please Enter password !", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(name)) {
                    Snackbar.make(relativeLayout, "Please Enter name !", Snackbar.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(phone)) {
                    Snackbar.make(relativeLayout, "Please Enter phone !", Snackbar.LENGTH_LONG).show();
                } else {
                    signUp(email, pass, name, phone);
                }
            }
        });
        // set the custom layout (view after converting by LayoutInflater)
        builder.setView(view);
        //finally show the dailog
        builder.show();
    }


    //create new account method
    private void signUp(final String email, String password, final String name, final String phone) {
        /**
         * showing the spots dialog
         */
        spotsDialog = new SpotsDialog(this);
        spotsDialog.show();
        //creating new account with email and passwored has been entered
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in uberDriver's information
                            Log.d("login_in", "createUserWithEmail:success");
                            //getting the current uberDriver
                            FirebaseUser user = mAuth.getCurrentUser();
                            /***
                             * after creating uberDriver account, firebase generate uid
                             * for every uberDriver has a unique uid
                             */
                            //gettting current uberDriver id (uid)
                            String uid = "";
                            if (user != null) {
                                uid = user.getUid();
                            }
                            /***
                             * a hash map (key pairs value)
                             * we are useing hashmap to stor uberDriver data to firebase
                             */
                            Map userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("phone", phone);
                            userData.put("email", email);
                            userData.put("vechleType", "UberX");
                            //set uberDriver data to mUsers database according to his uid
                            mRefUsers.child(uid).setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //hide the spots dialog
                                        spotsDialog.dismiss();
                                        //showing snack bar with success message when registration is successed
                                        Snackbar.make(relativeLayout, "Registration Success !", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //showing snack bar with failed message when registration is failed
                                    Snackbar.make(relativeLayout, "Registration failed !" + e.getMessage(), Snackbar.LENGTH_LONG).show();

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the uberDriver.
                            Log.w("login_in", "createUserWithEmail:failure", task.getException());
                            //hide the spots dialog
                            spotsDialog.dismiss();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                Snackbar.make(relativeLayout, "Registration failed !" + Objects.requireNonNull(task.getException()).getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                            }

                        }

                    }
                });
    }

    //sign in to your account
    private void signIn(String email, String password) {
        /**
         * showing the spots dialog
         */
        spotsDialog = new SpotsDialog(this);
        spotsDialog.show();
        //SignIn with an existing account with email and passwored has been entered
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //=====================================================================================================
                            // Sign in success, update UI with the signed-in uberDriver's information
                            Log.d("signIn", "signInWithEmail:success");
                            //get current signed in uberDriver
                            FirebaseUser user = mAuth.getCurrentUser();
                            //after signing in successed, we get uberDriver information
                            mRefUsers.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    //getting uberDriver information and store it to uberDriver object
                                    Common.uberDriver = dataSnapshot.getValue(UberDriver.class);
                                    Log.d("car_type", Common.uberDriver.getVechleType());
                                    //hide spots dialog
                                    spotsDialog.dismiss();
                                    //show snackbar with success message
                                    Snackbar.make(relativeLayout, "Sign in  success !", Snackbar.LENGTH_LONG).show();
                                    /**
                                     * intent to go to Maps activity after signed in success
                                     */
                                    Intent intent = new Intent(MainActivity.this, DriverHome.class);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        } else {
                            // If sign in fails, display a message to the uberDriver.
                            Log.w("signIn", "signInWithEmail:failure", task.getException());
                            //hide spots dialog
                            spotsDialog.dismiss();
                            //showing snackbar with fialure message
                            Snackbar.make(relativeLayout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if uberDriver is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }
}
