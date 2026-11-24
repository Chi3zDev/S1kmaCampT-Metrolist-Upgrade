# Audit polish fork

Basis: `main` 70eb9763 (Upgrade 1 beta 1). Audit kode awal; bukan hasil uji visual, aksesibilitas, jank profiler, atau uji perangkat. Prioritas P0 = keselamatan data/pemutaran; P1 = fungsi/aksesibilitas/performa; P2 = visual/gerak. Risiko adalah risiko saat mengubah area, bukan bukti bug telah terjadi. Kerjakan berurutan; centang hanya setelah implementasi dan verifikasi terkait. Jangan ubah database, navigasi utama, atau perilaku pemutaran tanpa bertanya.

## Aturan bersama
- [ ] P1 · Bahasa gerak bersama: durasi transisi 200–450 ms, pegas interaktif damping sekitar 0,75 dan stagger 40–80 ms; tidak mengubah waktu animasi lingkungan yang sengaja lambat. Hormati skala animasi sistem, mode ringan dan visibilitas. Risiko: animasi komponen upstream tidak boleh berubah tanpa sengaja.
- [ ] P1 · Uji TalkBack, ukuran font sistem besar, layar sempit, kontras, RTL, mode ringan, reduce motion, tema terang/gelap dan status tidak terlihat. Belum diverifikasi di perangkat.
- [ ] P0 · Data streak/recovery dan riwayat tidak boleh direkayasa atau diubah untuk animasi; playback tidak boleh tertunda. Pertahankan preferensi, kredit, lisensi, signing dan workflow.

## 1. Streak
- [ ] P2 · `StreakScreen.kt:228` api memakai satu skala berulang, glow statis dan partikel tidak naik: perlu lapisan api, flicker tidak seragam, glow dan partikel terikat visibilitas; padam menjadi abu-abu dan asap serta re-ignition yang tidak memalsukan data. Risiko: GPU/jank dan efek tetap berjalan saat tersembunyi.
- [ ] P2 · `StreakCard.kt` menampilkan ikon serta angka statis; `StreakScreen.kt` dan `StreakCelebration.kt` perlu count-up, stagger hari, koreografi level-up dan perayaan berurutan. Risiko: perubahan nilai harus tetap diumumkan secara benar oleh TalkBack; jangan tunda musik.
- [ ] P1 · `StreakScreen.kt:89` warna memakai tween 500 ms; samakan transisi interaktif tanpa mengubah logika pemulihan. Periksa ruang hari/angka besar dan kontras abu-abu di layar kecil; belum teruji.
- [ ] P1 · Tambah tes logika timing/motion dan verifikasi mode ringan, skala animasi nol, lifecycle, pemulihan, serta perayaan sekali per hari. Jangan modifikasi storage/DAO tanpa izin.

## 2. Recap (story)
- [ ] P2 · `RecapScreen.kt:167–210` memakai tiga warna dengan dua lingkaran statis untuk semua slide dan `AnimatedContent` default; variasi masuk, mesh, geser/fade/parallax belum ada. Risiko: jank, efek bergerak di background, motion sickness.
- [ ] P1 · `RecapScreen.kt:159` progress diperbarui setiap 50 ms selama tidak ditekan/dijeda; audit lifecycle, visibilitas, mode ringan dan timer tanpa mengubah semantik data. Pertimbangkan dampak aksesibilitas hit target dan navigasi gesture; uji TalkBack.
- [ ] P2 · Hari puncak/jam favorit tidak tampak dalam model slide saat ini; butuh sumber data yang valid. **Jika memerlukan query atau perubahan database, tanya pengguna dahulu.** Jangan mengarang statistik.
- [ ] P1 · `RecapShare.kt` mempunyai renderer Canvas terpisah dan font sistem, sedangkan layar memakai Compose; kesamaan hasil dan font belum terjamin. Perbaiki rendering bersama atau verifikasi representasi; risiko ekspor, memori dan teks terpotong.

## 3. Navbar dan kaca
- [ ] P2 · `FloatingNavigationBar.kt` sudah memiliki bubble/drag dan spring damping 0,9; belum memakai bahasa gerak bersama. Audit morph scroll dan konsistensi tint/border/padding pada tombol, chip, kartu dan Profil. Risiko perubahan utama navigasi: **tanya sebelum mengubah perilaku navigasi**, fokus, urutan atau rute.
- [ ] P1 · Haze/navbar memakai sumber blur yang ada; pastikan tidak menambah capture/blur, tetap ringan pada Android lama/perangkat RAM rendah dan nonaktif saat mode ringan. Uji label, touch target dan kontras; belum diuji perangkat.

## 4. Pemutar dan lirik
- [ ] P1 · `PlayerScreen.kt`, `PlayerLayout.kt`, `PlayerGlassCard.kt`: audit pembukaan, Vintage/Kartu Kaca/Piringan, ruang kontrol dan teks panjang; cakupan kaca telah diperluas tetapi hasil visual belum diverifikasi. Risiko: overlay, pemotongan teks/kontras dan jank.
- [ ] P2 · `VinylRecord.kt` dan `PlayerAura.kt` sudah punya gerbang visibilitas/skala animasi; uji rotasi, perlambatan, tonearm, aura, seekbar dan glow di perangkat. Jangan ubah seek, queue, kontrol atau timing playback tanpa persetujuan.
- [ ] P1 · Uji lirik kecil dan mode ringan/animasi nol; hindari blur tambahan serta recomposition per frame di luar area gambar.

## 5. Poster lirik
- [ ] P1 · `GlassLyricsPoster.kt:54` membatasi skala konten 0,7–1,15 sementara slider editor menyediakan 0,6–1,35: respons di ujung rentang tidak konsisten. Risiko perubahan tata letak/ekspor dan preferensi tersimpan.
- [ ] P1 · Renderer Klasik dan Kartu Kaca berbagi jalur pemanggilan, tetapi preview dan ekspor memakai ukuran berbeda: uji hasil pixel/layout, enam baris, RTL, teks panjang, font fallback, kontras dan format persegi/Story. Belum ada bukti hasil perangkat identik.
- [ ] P2 · Kartu Kaca memakai warna/opacity/radius tetap yang berbeda dari parameter kaca renderer Klasik; audit konsistensi dan tetap jaga branding serta keterbacaan.

## 6. Tentang, Settings dan teks
- [ ] P2 · Audit pengelompokan Appearance dan switch berulang, spacing, ikon, penjelasan serta badge versi; pertahankan semua kredit, GPL, font, tautan dan dukungan Palestina. Risiko mengubah preferensi atau menyembunyikan kredit.
- [ ] P1 · Periksa semua teks baru melalui resources Inggris/Indonesia, terutama pembesaran font, scrolling dan ellipsis. Jangan ubah navigasi Settings utama tanpa izin.

## 7. Performa, bug, dan rilis
- [ ] P1 · Profil recomposition/frame dan animasi pada latar/tertutup, Android 11 ke bawah dan RAM rendah; audit sumber blur tunggal dan objek Canvas. Temuan jank saat ini belum diukur.
- [ ] P2 · Ukur APK, audit kode dan lint; jangan hapus kode/kredit/aset tanpa bukti. Tambahkan tes untuk logika yang diubah dan jalankan unit test.
- [ ] P1 · Build Actions, gabung tanpa force, perbarui `latest-build`, dan rilis `v14.0.1-upgrade.2` dengan signing yang ada hanya setelah area dan pemeriksaan selesai. CI umum/Nightly pada main sebelumnya masih mengantre; status perlu dicek lagi sebelum rilis.

## Log pekerjaan
- Audit kode awal dibuat; tidak ada kode aplikasi yang diubah pada tahap audit.
