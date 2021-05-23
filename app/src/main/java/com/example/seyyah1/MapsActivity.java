package com.example.seyyah1;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.seyyah1.model.Yerler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap; // otomatik oluşturulan maps objesi
    LocationManager locationManager; // Sınıf, sistemin konum servislerine ulaşmayı sağlar
    LocationListener locationListener; // Arayüz, Laocation Manager ile çalışır, konum değiştiğinde haberdar olmamızı sağlar
    SQLiteDatabase database;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    // geri tuşuna basılırsa ne olacak?
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MapsActivity.this,MainActivity.class);
        startActivity(intent);
        finish(); // MapsActivity komple kapatılacak, main e geri dönülecek
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info"); // Main activity de info yazdığımız için böyle yazıldı

        if(info.matches("new")){ // Yani kullanıcı yeni bir yer eklemek istiyor ise burası, listeden bir yeri göstermek istiyorsa else çalışacak.
            //sistem servislerine locationManager ile erişip, konum_servisini kullanmaya izin veriyor.
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {// konum değiştiğinde ne yapılacaksa bu kısımda olacak
                    // Kullanıcı harita üzerinde rahat gezinsin, sürekli bulunduğu konuma otomatik zoomlamasın diye aşağıdaki kodlar yazılıyor
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.example.seyyah1",MODE_PRIVATE); // veritabanı ismi olarak paket ismi verildi
                    boolean trackBoolean = sharedPreferences.getBoolean("trackBoolean",false);

                    if (!trackBoolean) {
                        // kullanıcının mevcut konumu uyg açıldığında zom yapılıyor
                        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply();
                    }
                }
            };
            // Kullanıcıdan konum izni alınıyor
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                // izin verilmediyse, izin isteniyor
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
                // request code yani istek konu daha sonra yapılacak kontroller için yazılıyor
            } else { // izin verilmişse yapılacak olanlar
                // konum güncellenmesi ne kadar sürede bir yapılsın, 0 ise çok sık günceller ama çok pil tüketir
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                // uygulma açıldığında son bulunan konumdan itibaren harita ekranda gözüküyor.
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastLocation != null) { // uygulama olur da son konumu veremezse diye kontrol
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }
            }
        } else {
            //veritabanından dan çekilp intent ile gönderilen veriler alınacak
            mMap.clear();
            Yerler gelen_yer = (Yerler) intent.getSerializableExtra("place");
            // kullanıcının kaydettiği yer gözüksün diye marker ekleyelim
            LatLng latLng = new LatLng(gelen_yer.latitude,gelen_yer.longitude);
            String yer_adi = gelen_yer.name;
            mMap.addMarker(new MarkerOptions().position(latLng).title(yer_adi));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        }
    }
    // izin alındaktan sonra yapılacak işlemler
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0) {
            if(requestCode == 1) { // istek yaparken 1 yamıştık
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    // Yukarıda yazılanların aynısı tekrar ediliyor, yapmasak da olurdu
                    Intent intent = getIntent();
                    String info = intent.getStringExtra("info");

                    if (info.matches("new")){
                        // kullanıcının koumunu bir location objesi olarak alıyor
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        }else {
                            //veritabanından çekilp intent ile gönderilen veriler alınacak
                            mMap.clear();
                            Yerler gelen_yer = (Yerler) intent.getSerializableExtra("place");
                            // kullanıcının kaydettiği yer gözüksün diye marker ekleyelim
                            LatLng latLng = new LatLng(gelen_yer.latitude,gelen_yer.longitude);
                            String yer_adi = gelen_yer.name;
                            mMap.addMarker(new MarkerOptions().position(latLng).title(yer_adi));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                        }
                    }
                }
            }
        }
    }
    // kullanıcı haritaya uzun tıkladığında ne olacağı yazılacak
    @Override
    public void onMapLongClick(LatLng latLng) {

        // harita üzerinde tıklanan noktanın bulunması için  Geocoder oluşturuluyor.
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());// local get default: uyg nerde kullanılıyorsa oranın dili ile harita etiketlenecek
        String adress = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);//sonuçların arasından en iyi 1 tanesini listeler
            if (addressList != null && addressList.size()>0){ // adres listesi boş mu değil mi kontrolü
                if (addressList.get(0).getThoroughfare()!=null){ // Cadde adı
                    adress += addressList.get(0).getThoroughfare();
                    if (addressList.get(0).getSubThoroughfare()!=null){ // adres no
                        adress += " ";
                        adress += addressList.get(0).getSubThoroughfare();
                    }
                }
            }else{
                adress = "Adres Bulunamadı";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().title(adress).position(latLng));

        // BU KISIMDA DATABASE İŞLEMLERİ YAPILACAK, BUNUN İÇİN PLACE İSİMLİ BİR SINIF OLUŞTURDUK

        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;

        final Yerler place = new Yerler(adress,latitude,longitude);

        // Gerçekten kaydetmek istiyor musun diye kullanıcıya bir pencere gösterelim

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setCancelable(false); // kullanıcı bizim seçeneklerden birini seçmek zorunda, seçmeden kapatamaz dialog pemceresini
        alertDialog.setTitle("Kaydedilsin mi?");
        alertDialog.setMessage(place.name);// kullanıcının haritadan seçtiği adres
        alertDialog.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Firebase işlemleri
                try {
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    String userEmail = firebaseUser.getEmail();

                    HashMap<String, Object> hash_add_place = new HashMap<>();
                    hash_add_place.put("useremail",userEmail);
                    hash_add_place.put("name",place.name);
                    hash_add_place.put("latitude",String.valueOf(place.latitude));
                    hash_add_place.put("longitude",String.valueOf(place.longitude));
                    hash_add_place.put("date", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Places").add(hash_add_place).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            //String documentId = documentReference.getId();
                            Toast.makeText(MapsActivity.this,"Kaydedildi!",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(MapsActivity.this,MainActivity.class);
                            //intent.putExtra("reference",documentId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//tüm açık akitviteleri kapat
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapsActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                        }
                    });
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"İptal Edildi!",Toast.LENGTH_LONG).show();
            }
        });
        alertDialog.show();

    }
}