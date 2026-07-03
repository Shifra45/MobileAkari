//package com.example.akarigame;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.GridLayout;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class NumberActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_number);
//
//        GridLayout gridLayout = findViewById(R.id.gridLayout);
//
//        // Jumlah tombol yang akan ditampilkan (4x6 = 24 tombol)
//        int numberOfButtons = 24;
//
//        // Loop untuk membuat tombol-tombol
//        for (int i = 1; i <= numberOfButtons; i++) {
//            Button button = new Button(this);
//            button.setText(String.valueOf(i)); // Set teks tombol dengan angka
//
//            // Atur parameter layout untuk tombol
//            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
//            params.width = 0; // Lebar dinamis
//            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
//            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Kolom ke-i % 4
//            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Baris ke-(i / 4)
//            params.setMargins(8, 8, 8, 8); // Margin antar tombol
//
//            button.setLayoutParams(params);
//
//            // Set listener untuk tombol angka
//            final int buttonNumber = i; // Simpan nomor tombol
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    openBlankActivity(); // Buka BlankActivity
//                }
//            });
//
//            gridLayout.addView(button); // Tambahkan tombol ke GridLayout
//        }
//    }
//
//    private void openBlankActivity() {
//        Intent intent = new Intent(this, GameActivity.class);
//        startActivity(intent); // Buka BlankActivity
//    }
//}