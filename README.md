# SAM WALLET

Aplikasi Android untuk mengontrol keuangan pribadi.

## Fitur versi awal
- PIN 6 digit saat pertama kali membuka aplikasi.
- Pencatatan pemasukan dan pengeluaran.
- Perhitungan saldo otomatis.
- Anggaran total pengeluaran bulanan.
- Riwayat transaksi terbaru.
- Ringkasan dan saran keuangan sederhana.
- Penyimpanan lokal menggunakan SQLite; aplikasi tidak memerlukan akun atau internet.

## Membuat APK
GitHub Actions membangun APK otomatis saat ada push ke branch main atau melalui menu Actions > Build SAM WALLET APK > Run workflow.

Hasil build bernama SamWallet.apk. Workflow juga mencoba menerbitkannya sebagai GitHub Release agar APK dapat diunduh langsung tanpa mengekstrak source project.

## Catatan
- Ini adalah APK debug untuk uji coba/penggunaan pribadi, belum merupakan rilis bertanda tangan untuk Play Store.
- PIN pada versi awal disimpan dalam preferensi aplikasi sebagai perlindungan dasar, bukan keamanan tingkat perbankan. Jangan gunakan PIN ATM/rekening.
- Data transaksi disimpan di perangkat. Menghapus data aplikasi atau uninstall dapat menghapus data transaksi.
- Anggaran versi awal berupa batas total bulanan, belum per kategori.
