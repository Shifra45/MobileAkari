package com.example.akarigame;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private Button btnMudah, btnSedang, btnSulit;
    private String currentDifficulty = "easy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi tombol-tombol level
        btnMudah = findViewById(R.id.btnMudah);
        btnSedang = findViewById(R.id.btnSedang);
        btnSulit = findViewById(R.id.btnSulit);

        // Inisialisasi GridLayout
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.setColumnCount(4); // Set default column count

        updateButtonColors("easy");

        btnMudah.setOnClickListener(v -> {
            currentDifficulty = "easy";
            updateButtonColors("easy");
            showLevelsForDifficulty("easy", 24); // 24 level untuk easy
        });

        btnSedang.setOnClickListener(v -> {
            currentDifficulty = "normal";
            updateButtonColors("normal");
            showLevelsForDifficulty("normal", 24); // 24 level untuk normal
        });

        btnSulit.setOnClickListener(v -> {
            currentDifficulty = "hard";
            updateButtonColors("hard");
            showLevelsForDifficulty("hard", 24); // 24 level untuk hard
        });

        // Tampilkan level easy secara default
        showLevelsForDifficulty("easy", 24);
    }

    private void showLevelsForDifficulty(String difficulty, int totalLevels) {
        gridLayout.removeAllViews();
        List<Puzzle> puzzles = PuzzleData.getPuzzlesByDifficulty(difficulty);

        for (int i = 1; i <= totalLevels; i++) {
            Button levelButton = new Button(this);
            levelButton.setText(String.valueOf(i));
            levelButton.setContentDescription("Level " + i + " " + difficulty);

            // Warna berbeda untuk level yang tersedia vs belum tersedia
            if (i <= puzzles.size()) {
                levelButton.setBackgroundColor(Color.WHITE);
                levelButton.setTextColor(Color.BLACK);
            } else {
                levelButton.setBackgroundColor(Color.LTGRAY);
                levelButton.setTextColor(Color.GRAY);
                levelButton.setContentDescription("Level " + i + " belum tersedia");
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(60);
            params.height = dpToPx(60);
            params.setMargins(8, 8, 8, 8);

            final int levelIndex = i - 1;
            levelButton.setOnClickListener(v -> {
                if (levelIndex < puzzles.size()) {
                    openBlankActivity(PuzzleData.puzzles.indexOf(puzzles.get(levelIndex)));
                } else {
                    Toast.makeText(this, "Level belum tersedia", Toast.LENGTH_SHORT).show();
                }
            });

            gridLayout.addView(levelButton);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void updateButtonColors(String selectedDifficulty) {
        // Reset semua warna tombol
        btnMudah.setBackgroundColor(Color.LTGRAY);
        btnSedang.setBackgroundColor(Color.LTGRAY);
        btnSulit.setBackgroundColor(Color.LTGRAY);

        // Set warna tombol yang aktif
        switch (selectedDifficulty) {
            case "easy":
                btnMudah.setBackgroundColor(Color.DKGRAY);
                btnMudah.setTextColor(Color.WHITE);
                break;
            case "normal":
                btnSedang.setBackgroundColor(Color.DKGRAY);
                btnSedang.setTextColor(Color.WHITE);
                break;
            case "hard":
                btnSulit.setBackgroundColor(Color.DKGRAY);
                btnSulit.setTextColor(Color.WHITE);
                break;
        }
    }

    private void openBlankActivity(int puzzleNumber) {
        if (puzzleNumber < PuzzleData.puzzles.size()) { // Pastikan puzzle tersedia
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("PUZZLE_NUMBER", puzzleNumber); // Kirim nomor puzzle ke BlankActivity
            startActivity(intent);
        } else {
            Toast.makeText(this, "Soal belum tersedia", Toast.LENGTH_SHORT).show();
        }
    }
}