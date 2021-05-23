package com.example.seyyah1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.seyyah1.adapter.CustomAdapter;
import com.example.seyyah1.model.Yerler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    ArrayList<Yerler> yerListesi = new ArrayList<>();
    ListView listView;
    
    CustomAdapter customAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    String documentId;

    private String selectedFilter = "all";
    private String currentSearchText = "";
    private SearchView searchView;

    //Oluşturduğumuz menüyü bağlamak için iki metot ekliyouz: onCreateOptionsMenu ve onOptionsItemSelected
    @Override // Menü ilk oluşturulduğunda yapılcak işlemler bu metotta
    public boolean onCreateOptionsMenu(Menu menu) {
        // xml dosyalarını bağlamak için Inflater kullanacağız
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.seyyah_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override // Menüden bişey (item) seçilirse yapılacak işlemler bu metotta
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.yer_ekle) {
            Intent intent = new Intent(this,MapsActivity.class); // yerk ekle seçildiğinde mapsactivity çalışsın
            intent.putExtra("info","new"); // listeye nereden gidilldiğini anlamamız için
            startActivity(intent);
        }else if (item.getItemId() == R.id.cikis_yap){
            firebaseAuth.signOut();
            Intent intent = new Intent(MainActivity.this,LoginActiviy.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.listView);
        customAdapter = new CustomAdapter(this,yerListesi);
        listView.setAdapter(customAdapter);
        initSearchWidgets();
        getData();

    }

    private void initSearchWidgets() {
        searchView = (SearchView) findViewById(R.id.serach_bar);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                currentSearchText = s;
                ArrayList<Yerler> filteredPlaces = new ArrayList<Yerler>();

                for(Yerler place: yerListesi)
                {
                    if(place.getName().toLowerCase().contains(s.toLowerCase()))
                    {
                        if(selectedFilter.equals("all"))
                        {
                            filteredPlaces.add(place);
                        }
                        else
                        {
                            if(place.getName().toLowerCase().contains(selectedFilter))
                            {
                                filteredPlaces.add(place);
                            }
                        }
                    }
                }
                CustomAdapter adapter = new CustomAdapter(getApplicationContext(),filteredPlaces);
                listView.setAdapter(adapter);

                return false;
            }
        });
    }

    public void getData() {
        // Firebase İşlemleri
        try {
            CollectionReference collectionReference = firebaseFirestore.collection("Places");
            collectionReference.whereEqualTo("useremail",firebaseAuth.getCurrentUser().getEmail()).orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                    if (error != null){
                        error.printStackTrace();
                        Toast.makeText(MainActivity.this, error.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                    }
                    if (queryDocumentSnapshots != null){

                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                            Map<String,Object> data = snapshot.getData();

                            String nameFromDatabase = (String) data.get("name");
                            String latitudeFromDatabase = (String) data.get("latitude");
                            String longitudeFromDatabase = (String) data.get("longitude");
                            Double latitude = Double.parseDouble(latitudeFromDatabase);
                            Double longitude = Double.parseDouble(longitudeFromDatabase);

                            Yerler yeni_yer = new Yerler(nameFromDatabase,latitude,longitude);
                            yerListesi.add(yeni_yer);
                            customAdapter.notifyDataSetChanged();
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"Data Bulunamadı",Toast.LENGTH_LONG);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        // yeni eklenenler
        // kaydedilenler listesindeki bir item e tıklanınca ne olacak ?
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Lütfen İşlem Seçiniz ");
                //alertDialog.setMessage(place.name);// kullanıcının haritadan seçtiği adres
                alertDialog.setPositiveButton("Göster", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                        intent.putExtra("info","old");
                        intent.putExtra("place",yerListesi.get(position)); // tıklanan itemdeki yer bilgisi intetn ile gönderildi
                        startActivity(intent);
                    }
                });
                alertDialog.setNegativeButton("Sil", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(MainActivity.this);
                        alertDialog2.setTitle("Emin misiniz?");
                        alertDialog2.setCancelable(false);
                        alertDialog2.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Yerler secilenYer = yerListesi.get(position);
                                    String silinecekYerAdi = secilenYer.name;
                                    if (silinecekYerAdi.matches("")){
                                        System.out.println("Böyle yer yok");
                                    }else
                                        getDocumentId(silinecekYerAdi,"delete",null);
                                        yerListesi.remove(position);
                                        customAdapter.notifyDataSetChanged();
                                        yerListesi.clear();
                                }catch (Exception e){
                                    System.out.println("Hata şurada  " + e.getLocalizedMessage().toString());
                                }
                            }
                        });
                        alertDialog2.setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog2.show();
                    }
                });
                alertDialog.setNeutralButton("Güncelle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Yerler secilenYer = yerListesi.get(position);
                            String guncellenecekYerAdi = secilenYer.name;
                            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                            View myView = inflater.inflate(R.layout.activity_update,null);
                            EditText etGiris = myView.findViewById(R.id.etGiris);
                            AlertDialog.Builder alertDialog3 = new AlertDialog.Builder(MainActivity.this);
                            alertDialog3.setView(myView)
                                    .setCancelable(false)
                                    .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String yeniAd = etGiris.getText().toString();
                                            getDocumentId(guncellenecekYerAdi,"update",yeniAd);
                                            secilenYer.name = yeniAd;
                                            customAdapter.notifyDataSetChanged();
                                            yerListesi.clear();
                                        }
                                    })
                                    .setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                alertDialog.show();
            }
        });
    }

    public void deleteData(String silinecekDocumentId){

        firebaseFirestore.collection("Places").document(silinecekDocumentId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Kayıt Silindi!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void updateData(String guncellenecekDocumentId, String yeniYerAdi){
        try {
            DocumentReference docRef = firebaseFirestore.collection("Places").document(guncellenecekDocumentId);

            Map<String,Object> updates = new HashMap<>();
            updates.put("name", yeniYerAdi);

            docRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                // [START_EXCLUDE]
                @Override
                public void onComplete(@NonNull Task<Void> task) {}
                // [START_EXCLUDE]
            });
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage().toString());
        }
        Toast.makeText(MainActivity.this,"Başarıyla Güncellendi",Toast.LENGTH_LONG).show();
    }

    public void getDocumentId(String yerAdi,String islem, String yeniYerAdi){
        CollectionReference collectionReference = firebaseFirestore.collection("Places");
        collectionReference.whereEqualTo("name",yerAdi).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    error.printStackTrace();
                    Toast.makeText(MainActivity.this, error.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
                if (queryDocumentSnapshots != null){

                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){

                        documentId = snapshot.getId().toString();
                        if (islem.matches("delete")){
                            deleteData(documentId);
                        }else if (islem.matches("update")){
                            updateData(documentId,yeniYerAdi);
                        }
                    }
                }else{
                    Toast.makeText(MainActivity.this,"Data Bulunamadı",Toast.LENGTH_LONG);
                }
            }
        });
    }
}