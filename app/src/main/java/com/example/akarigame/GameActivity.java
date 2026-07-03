package com.example.akarigame;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private Puzzle puzzle;
    private GridLayout akariGrid;
    private Button[][] cellButtons;

    private LinearLayout messageContainer;
    private TextView tvLampConflict, tvWallConflict, tvUnlitCells;

    private static final int COLOR_NORMAL = Color.rgb(255, 255, 200); // Kuning muda untuk area cahaya
    private static final int COLOR_CONFLICT = Color.rgb(255, 150, 150); // Merah muda untuk area konflik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Inisialisasi komponen
        akariGrid = findViewById(R.id.akariGrid);

        // Inisialisasi TextView status
        messageContainer = findViewById(R.id.messageContainer);
        tvLampConflict = findViewById(R.id.tvLampConflict);
        tvWallConflict = findViewById(R.id.tvWallConflict);
        tvUnlitCells = findViewById(R.id.tvUnlitCells);

        // Tombol Kembali
        Button btnKembali = findViewById(R.id.btnKembali);
        btnKembali.setOnClickListener(v -> finish());

        // Tombol Reset
        Button btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(v -> {
            puzzle.resetBoard();
            updateGrid();
        });

        // Ambil puzzle
        int puzzleNumber = getIntent().getIntExtra("PUZZLE_NUMBER", 0);
        puzzle = PuzzleData.puzzles.get(puzzleNumber);
        cellButtons = new Button[puzzle.getSize()][puzzle.getSize()];

        updateGrid();
    }

    private void updateGrid() {
        int size = puzzle.getSize();
        int[][] board = puzzle.getCurrentBoard();
        akariGrid.removeAllViews();

        // Atur jumlah kolom sesuai ukuran papan
        akariGrid.setColumnCount(size);
        akariGrid.setRowCount(size);


        // Array untuk menandai sel yang konflik
        boolean[][] conflictCells = new boolean[size][size];

        // Cek semua lampu untuk konflik
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 9) {
                    checkLampConflict(r, c, conflictCells, board);
                }
            }
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Button cell = new Button(this);
                final int finalRow = row;
                final int finalCol = col;

                // Atur gravity dan padding untuk semua sel
                cell.setGravity(Gravity.CENTER);  // <-- Ini yang membuat konten tengah
                cell.setPadding(0, 0, 0, 0);     // <-- Hilangkan padding default

                // Atur warna dasar berdasarkan tipe sel
                if (board[row][col] == -1) {
                    cell.setBackgroundColor(Color.BLACK); // Tembok
                } else if (board[row][col] >= 0 && board[row][col] <= 4) {
                    cell.setBackgroundColor(Color.BLACK); // Kotak angka
                    cell.setText(String.valueOf(board[row][col]));
                    cell.setTextColor(Color.WHITE);
                    cell.setTextSize(16); // Atur ukuran teks
                    cell.setTypeface(null, Typeface.BOLD); // Tebalkan angka
                } else if (board[row][col] == 5) {
                    cell.setBackgroundColor(Color.WHITE); // Kotak putih kosong
                } else if (board[row][col] == 6) {
                    cell.setBackgroundColor(COLOR_NORMAL); // Area terkena cahaya
                    cell.setOnClickListener(v -> placeLamp(finalRow, finalCol)); // Tetap bisa diklik
                } else if (board[row][col] == 9) {
                    // Lampu - warna tergantung konflik
                    cell.setBackgroundColor(conflictCells[row][col] ? Color.RED : Color.YELLOW);
                    cell.setText("💡");
                    cell.setTextSize(16);
                    cell.setGravity(Gravity.CENTER); // Pastikan emoji juga tengah
                }

                // Jika sel ini dalam konflik tapi bukan lampu, beri warna merah muda
                if (conflictCells[row][col] && board[row][col] != 9) {
                    cell.setBackgroundColor(COLOR_CONFLICT);
                }

                // Set listener untuk sel yang bisa diklik
                if (board[row][col] == 5) {
                    cell.setOnClickListener(v -> placeLamp(finalRow, finalCol));
                } else if (board[row][col] == 9) {
                    cell.setOnClickListener(v -> removeLamp(finalRow, finalCol));
                }

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 100;
                params.height = 100;
                params.columnSpec = GridLayout.spec(col);
                params.rowSpec = GridLayout.spec(row);
                params.setMargins(2, 2, 2, 2);

                cell.setLayoutParams(params);
                akariGrid.addView(cell);
            }
        }
    }

    private void placeLamp(int row, int col) {
        int[][] board = puzzle.getCurrentBoard();
        // Perubahan: Boleh tempatkan lampu di cell kosong (5) ATAU area terang (6)
        if (board[row][col] == 5 || board[row][col] == 6) {
            board[row][col] = 9; // Tempatkan lampu
            spreadLight(row, col);
            updateGrid();
            checkSolution();
        }
    }

    private void removeLamp(int row, int col) {
        int[][] board = puzzle.getCurrentBoard();
        if (board[row][col] == 9) {
            board[row][col] = 5;
            clearAndRespreadLight();
            updateGrid();
        }
    }

    private void spreadLight(int row, int col) {
        int[][] board = puzzle.getCurrentBoard();
        int size = puzzle.getSize();

        // Sebarkan cahaya ke 4 arah
        spreadDirection(board, row, col, 0, -1);  // Kiri
        spreadDirection(board, row, col, 0, 1);   // Kanan
        spreadDirection(board, row, col, -1, 0);  // Atas
        spreadDirection(board, row, col, 1, 0);   // Bawah
    }

    private void spreadDirection(int[][] board, int row, int col, int rowDir, int colDir) {
        int size = puzzle.getSize();
        int r = row + rowDir;
        int c = col + colDir;

        while (r >= 0 && r < size && c >= 0 && c < size &&
                (board[r][c] == 5 || board[r][c] == 6)) {
            board[r][c] = 6; // Tetap set ke 6 (area terang)
            r += rowDir;
            c += colDir;
        }
    }

    private void checkLampConflict(int row, int col, boolean[][] conflictCells, int[][] board) {
//        int size = puzzle.getSize();
//
//        // Cek 4 arah sekeliling (atas, kanan, bawah, kiri)
//        int[] dr = {-1, 0, 1, 0}; // delta row
//        int[] dc = {0, 1, 0, -1}; // delta col
//
//        for (int i = 0; i < 4; i++) {
//            int newRow = row + dr[i];
//            int newCol = col + dc[i];
//
//            // Pastikan tidak keluar dari batas board
//            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
//                if (board[newRow][newCol] == 9) { // Jika ada lampu di sebelahnya
//                    conflictCells[row][col] = true; // Tandai lampu ini konflik
//                    conflictCells[newRow][newCol] = true; // Tandai lampu sebelahnya juga konflik
//                }
//            }
//        }

        // Ke atas
        for (int i = row - 1; i >= 0; i--) {
            int c = board[i][col];
            if (isWall(c)) break;
            if (c == 9) {
                conflictCells[row][col] = true; // Tandai lampu ini konflik
                conflictCells[i][col] = true; // Tandai lampu sebelahnya juga konflik
            }
        }
        // Ke bawah
        for (int i = row + 1; i < board.length; i++) {
            int c = board[i][col];
            if (isWall(c)) break;
            if (c == 9) {
                conflictCells[row][col] = true; // Tandai lampu ini konflik
                conflictCells[i][col] = true; // Tandai lampu sebelahnya juga konflik
            }
        }
        // Ke kiri
        for (int j = col - 1; j >= 0; j--) {
            int c = board[row][j];
            if (isWall(c)) break;
            if (c == 9) {
                conflictCells[row][col] = true; // Tandai lampu ini konflik
                conflictCells[row][j] = true; // Tandai lampu sebelahnya juga konflik
            }
        }
        // Ke kanan
        for (int j = col + 1; j < board.length; j++) {
            int c = board[row][j];;
            if (isWall(c)) break;
            if (c == 9) {
                conflictCells[row][col] = true; // Tandai lampu ini konflik
                conflictCells[row][j] = true; // Tandai lampu sebelahnya juga konflik
            }
        }

    }

    private void clearAndRespreadLight() {
        int[][] board = puzzle.getCurrentBoard();
        int size = puzzle.getSize();

        // Clear semua cahaya (termasuk overlap)
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 6) {
                    board[r][c] = 5;
                }
            }
        }

        // Respread cahaya
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 9) {
                    spreadLight(r, c);
                }
            }
        }
    }

    private void checkSolution() {
        int[][] board = puzzle.getCurrentBoard();

        // Reset semua pesan
        tvLampConflict.setVisibility(View.GONE);
        tvWallConflict.setVisibility(View.GONE);
        tvUnlitCells.setVisibility(View.GONE);
        messageContainer.setVisibility(View.GONE);

        boolean hasLampConflict = false;
        boolean hasWallConflict = false;
        boolean hasUnlitCells = false;

        // 1. Cek lampu bertabrakan
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                if (board[r][c] == 9 && hasAdjacentLamp(r, c, board)) {
                    hasLampConflict = true;
                    tvLampConflict.setText("⚠️ Ada lampu yang bertabrakan");
                    tvLampConflict.setVisibility(View.VISIBLE);
                    break;
                }
            }
            if (hasLampConflict) break;
        }

        // 2. Cek dinding angka
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[r].length; c++) {
                if (board[r][c] >= 0 && board[r][c] <= 4) {
                    int lampCount = countAdjacentLamps(r, c, board);
                    if (lampCount != board[r][c]) {
                        hasWallConflict = true;
                        tvWallConflict.setText("⚠️ Lampu di dinding tidak sesuai");
                        tvWallConflict.setVisibility(View.VISIBLE);
//                        if (lampCount > board[r][c]) {
//                            board[r][c] = 7; // Tandai dengan warna ungu
//                        }
                        break;
                    }
                }
            }
            if (hasWallConflict) break;
        }

        // 3. Cek kotak belum terang
        outerLoop:
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 5) {
                    hasUnlitCells = true;
                    tvUnlitCells.setText("⚠️ Masih ada kotak yang belum diterangi");
                    tvUnlitCells.setVisibility(View.VISIBLE);
                    break outerLoop;
                }
            }
        }

        // Tampilkan container jika ada pesan
        if (hasLampConflict || hasWallConflict || hasUnlitCells) {
            messageContainer.setVisibility(View.VISIBLE);
        }

        if (!hasLampConflict && !hasWallConflict && !hasUnlitCells) {
            showWinDialog();
        }
    }


    // Cek lampu yang bertabrakan
    private boolean hasAdjacentLamp(int row, int col, int[][] board) {
//        int[] dr = {-1, 0, 1, 0}; // Atas, kanan, bawah, kiri
//        int[] dc = {0, 1, 0, -1};
//
//        for (int i = 0; i < 4; i++) {
//            int newRow = row + dr[i];
//            int newCol = col + dc[i];
//
//            if (newRow >= 0 && newRow < board.length &&
//                    newCol >= 0 && newCol < board[0].length) {
//                if (board[newRow][newCol] == 9) {
//                    return true;
//                }
//            }
//        }
//        return false;

        // Ke atas
        for (int i = row - 1; i >= 0; i--) {
            int c = board[i][col];
            if (isWall(c)) break;
            if (c == 9) return true;
        }
        // Ke bawah
        for (int i = row + 1; i < board.length; i++) {
            int c = board[i][col];
            if (isWall(c)) break;
            if (c == 9) return true;
        }
        // Ke kiri
        for (int j = col - 1; j >= 0; j--) {
            int c = board[row][j];
            if (isWall(c)) break;
            if (c == 9) return true;
        }
        // Ke kanan
        for (int j = col + 1; j < board.length; j++) {
            int c = board[row][j];;
            if (isWall(c)) break;
            if (c == 9) return true;
        }
        return false;
    }

    private int countAdjacentLamps(int row, int col, int[][] board) {
        int count = 0;
        int[] dr = {-1, 0, 1, 0}; // Atas, kanan, bawah, kiri
        int[] dc = {0, 1, 0, -1};

        for (int i = 0; i < 4; i++) {
            int newRow = row + dr[i];
            int newCol = col + dc[i];

            if (newRow >= 0 && newRow < board.length &&
                    newCol >= 0 && newCol < board[0].length) {
                if (board[newRow][newCol] == 9) {
                    count++;
                }
            }
        }
        return count;
    }

    private void showWinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Selamat!")
                .setMessage("Anda berhasil menyelesaikan puzzle!")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private boolean isWall(int value) {
        return (value >= 0 && value <= 4) || value == -1;
    }

//    private boolean isSolved() {
//        for (int i = 0; i < boardSize; i++) {
//            for (int j = 0; j < boardSize; j++) {
//                int cell = puzzle.getCurrentCell(i, j);
//
//                // 1. Cek semua sel putih harus diterangi
//                if (cell == 5) return false; // sel putih belum diterangi
//
//                // 2. Cek konflik antar lampu (saling menerangi)
//                if (cell == 9) { // lampu
//                    if (hasAdjacentLamp(i, j)) return false;
//                }
//
//                // 3. Cek jumlah lampu di sekitar sel angka
//                if (cell >= 0 && cell <= 4) { // dinding bernomor
//                    int count = hitungLampuSekitar(i, j);
//                    if (count != cell) return false;
//                }
//            }
//        }
//
//        // Semua aturan terpenuhi
//        return true;
//    }

//    private boolean adaLampuSejajar(int x, int y) {
//        // Ke atas
//        for (int i = x - 1; i >= 0; i--) {
//            int c = puzzle.getCurrentCell(i, y);
//            if (isWall(c)) break;
//            if (c == 9) return true;
//        }
//        // Ke bawah
//        for (int i = x + 1; i < boardSize; i++) {
//            int c = puzzle.getCurrentCell(i, y);
//            if (isWall(c)) break;
//            if (c == 9) return true;
//        }
//        // Ke kiri
//        for (int j = y - 1; j >= 0; j--) {
//            int c = puzzle.getCurrentCell(x, j);
//            if (isWall(c)) break;
//            if (c == 9) return true;
//        }
//        // Ke kanan
//        for (int j = y + 1; j < boardSize; j++) {
//            int c = puzzle.getCurrentCell(x, j);
//            if (isWall(c)) break;
//            if (c == 9) return true;
//        }
//        return false;
//    }

//    private int hitungLampuSekitar(int x, int y) {
//        int count = 0;
//        if (x > 0 && puzzle.getCurrentCell(x - 1, y) == 9) count++;
//        if (x < boardSize - 1 && puzzle.getCurrentCell(x + 1, y) == 9) count++;
//        if (y > 0 && puzzle.getCurrentCell(x, y - 1) == 9) count++;
//        if (y < boardSize - 1 && puzzle.getCurrentCell(x, y + 1) == 9) count++;
//        return count;
//    }

}