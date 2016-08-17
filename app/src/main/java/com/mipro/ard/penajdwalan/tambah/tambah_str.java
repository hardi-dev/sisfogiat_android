package com.mipro.ard.penajdwalan.tambah;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;
import com.kosalgeek.android.photoutil.PhotoLoader;
import com.mipro.ard.penajdwalan.MainActivity;
import com.mipro.ard.penajdwalan.R;
import com.mipro.ard.penajdwalan.jadwal.pilih_personil;
import com.mipro.ard.penajdwalan.json_handler.MyApplication;
import com.mipro.ard.penajdwalan.json_handler.MyCommand;
import com.mipro.ard.penajdwalan.json_handler.parser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class tambah_str extends AppCompatActivity {
    Button btn_camera, btn_galery;
    LinearLayout linearMain;
    GalleryPhoto galleryPhoto;
    CameraPhoto cameraPhoto;
    final int GALERY_REQUEST = 1200;
    final int CAMERA_REQUEST = 2100;
    final String TAG = this.getClass().getSimpleName();

    ArrayList<String> imageList = new ArrayList<>();
    TextView title_bar;
    ImageButton m_done_btn, m_back_btn;

    ProgressDialog PD;
    String idJadwal;

    String reqTglMulai, reqJamMulai, reqTglSelesai, reqJamSelesai;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_str);

        linearMain = (LinearLayout) findViewById(R.id.linearMain);
        btn_camera = (Button) findViewById(R.id.btn_cam_up_sp);
        btn_galery = (Button) findViewById(R.id.btn_galery_up_sp);

        PD = new ProgressDialog(tambah_str.this);
        PD.setMessage("Sedang Mengunggah...");
        PD.setCancelable(false);


        Intent getJadwal    = getIntent();
        idJadwal            = getJadwal.getStringExtra("idJadwal");
        reqTglMulai         = getJadwal.getStringExtra("tglMulai");
        reqJamMulai         = getJadwal.getStringExtra("jamMulai");
        reqTglSelesai       = getJadwal.getStringExtra("tglSelesai");
        reqJamSelesai       = getJadwal.getStringExtra("jamSelesai");

        Log.d("IsiJadwalRequest", idJadwal + reqTglMulai + reqJamMulai + reqTglSelesai + reqJamSelesai);


        title_bar = (TextView) findViewById(R.id.bar_title);
        m_back_btn = (ImageButton) findViewById(R.id.kembali_btn);
        m_done_btn = (ImageButton) findViewById(R.id.search_btn);

        title_bar.setText("SURAT PERINTAH");

        m_back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent back_home = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(back_home);
            }
        });



        m_done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();

            }
        });




        galleryPhoto    = new GalleryPhoto(tambah_str.this);
        cameraPhoto     = new CameraPhoto(getApplicationContext());

        btn_galery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galInt = galleryPhoto.openGalleryIntent();
                startActivityForResult(galInt, GALERY_REQUEST);

            }
        });

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    startActivityForResult(cameraPhoto.takePhotoIntent(), CAMERA_REQUEST);
                    cameraPhoto.addToGallery();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    protected void save(){
        final MyCommand myCommand = new MyCommand();
        PD.show();
        for (String imagePath : imageList){
            try {
                Bitmap bitmap = PhotoLoader.init().from(imagePath).requestSize(512, 512).getBitmap();
                final String encodedString = ImageBase64.encode(bitmap);
                String url = "http://"+ parser.IP_PUBLIC +"/ditlantas/json/surat/insert_str.php";

                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                PD.dismiss();
                                JSONObject jPesan = null;
                                try {
                                    jPesan = new JSONObject(response);
                                    boolean pesan = jPesan.names().get(0).equals("success");
                                    if (pesan){
                                            new MaterialDialog.Builder(tambah_str.this)
                                                    .title("Pengaturan Berhasil")
                                                    .positiveText("Lanjut")
                                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                            Intent intent = new Intent(getApplicationContext(), pilih_personil.class);
                                                            intent.putExtra("idJadwal", idJadwal);
                                                            intent.putExtra("tglMulai", reqTglMulai);
                                                            intent.putExtra("jamMulai", reqJamMulai);
                                                            intent.putExtra("tglSelesai", reqTglSelesai);
                                                            intent.putExtra("jamSelesai", reqJamSelesai);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    })
                                                    .show();

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                imageList.clear();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Terjadi Kesalahan saat Mengambil Gamabar", Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> params = new HashMap<String, String>();
                        params.put("idJadwal", idJadwal);
                        params.put("image", encodedString);
                        return params;
                    }
                };

                myCommand.add(stringRequest);

            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Error While Loading Image ", Toast.LENGTH_SHORT).show();

            }

        }
        myCommand.execute();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == GALERY_REQUEST){
                galleryPhoto.setPhotoUri(data.getData());
                String photoPath = galleryPhoto.getPath();

                imageList.add(photoPath);

                Log.d(TAG, galleryPhoto.getPath());

                try {
                    Bitmap bitmap = PhotoLoader.init().from(photoPath).requestSize(1024, 1024).getBitmap();
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    ImageView imageView = new ImageView(getApplicationContext());
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setPadding(0,0,0,10);
                    imageView.setAdjustViewBounds(true);
                    imageView.setImageBitmap(bitmap);
                    linearMain.addView(imageView);


                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Error While Loading Image ", Toast.LENGTH_SHORT).show();
                }
            }else if(requestCode == CAMERA_REQUEST){

                String photoPath = cameraPhoto.getPhotoPath();
                Log.d("isiPhotoPat", photoPath);

                imageList.add(photoPath);

                try {
                    Bitmap bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    ImageView imageView = new ImageView(getApplicationContext());
                    imageView.setLayoutParams(layoutParams);
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setPadding(0,0,0,10);
                    imageView.setAdjustViewBounds(true);
                    imageView.setImageBitmap(bitmap);
                    linearMain.addView(imageView);


                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Error While Loading Image ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
