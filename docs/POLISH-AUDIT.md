# Audit polish fork

Basis: `main` 70eb9763 (Upgrade 1 beta 1). Audit kode awal; bukan hasil uji visual, aksesibilitas, jank profiler, atau uji perangkat. Prioritas P0 = keselamatan data/pemutaran; P1 = fungsi/aksesibilitas/performa; P2 = visual/gerak. Risiko adalah risiko saat mengubah area, bukan bukti bug telah terjadi. Kerjakan berurutan; centang hanya setelah implementasi dan verifikasi terkait. Jangan ubah database, navigasi utama, atau perilaku pemutaran tanpa bertanya.

## Aturan bersama
- [ ] P1 · Bahasa gerak bersama: durasi transisi 200–450 ms, pegas interaktif damping sekitar 0,75 dan stagger 40–80 ms; tidak mengubah waktu animasi lingkungan yang sengaja lambat. Hormati skala animasi sistem, mode ringan dan visibilitas. Risiko: animasi komponen upstream tidak boleh berubah tanpa sengaja.
- [ ] P1 · Uji TalkBack, ukuran font sistem besar, layar sempit, kontras, RTL, mode ringan, reduce motion, tema terang/gelap dan status tidak terlihat. Belum diverifikasi di perangkat.
- [ ] P0 · Data streak/recovery dan riwayat tidak boleh direkayasa atau diubah untuk animasi; playback tidak boleh tertunda. Pertahankan preferensi, kredit, lisensi, signing dan workflow.

## 1. Streak
- [x] P2 · Implementasi awal api berlapis, flicker, glow, percikan naik, keadaan abu-abu dan asap; ukuran dan animasi mengikuti visibilitas host. Re-ignition memakai transisi bentuk/warna; tidak mengubah data.
- [x] P2 · Count-up, hari muncul berurutan, level-up memakai pegas dan perayaan bertahap; label kalender TalkBack dalam bahasa Indonesia/Inggris. Musik, riwayat dan penyimpanan tidak diubah.
- [x] P1 · Durasi warna menggunakan spesifikasi bersama. Ditambahkan tes unit gerbang mode sistem/mode ringan/perangkat.
- [ ] P1 · Verifikasi nyata: mode ringan, skala animasi nol, lifecycle, pemulihan/level-up, perayaan sekali per hari, TalkBack, ukuran font besar, abu-abu dan teks layar kecil. Build dan tes terpilih lulus pada Actions 45394915150; hasil visual belum dinilai di HP.
- [ ] P1 · Profil GPU dan scroll untuk memastikan item list tersembunyi tidak terus beranimasi; risiko overdraw/jank belum diukur.

## 2. Recap (story)
- [x] P2 · Implementasi mesh berbasis warna tema, variasi masuk dan transisi geser/fade, serta pergeseran elemen latar; animasi berat digerbang oleh lifecycle, mode ringan dan sistem. Perlu uji visual dan performa perangkat.
- [x] P1 · Timer Recap berhenti saat layar tertutup/background, dipegang atau dialog terbuka; logika gerbang punya unit test. Verifikasi TalkBack, gesture dan perhitungan waktu masih terbuka.
- [x] P2 · Hari puncak/jam favorit memakai bucket harian dan per jam yang sudah tersedia dalam snapshot statistik; bucket jam tak presisi sudah difilter oleh statistik existing. Kosong atau tidak valid tidak ditampilkan. Pengguna mengizinkan pembacaan riwayat; tidak ada query baru atau perubahan skema.
- [ ] P1 · `RecapScreen.kt` membagikan tangkapan graphics layer layar, sedangkan `RecapShare.kt` adalah renderer terpisah yang tidak ditemukan pemanggilnya dalam source. Verifikasi ekspor hasil tangkapan, resolusi, font dan pemotongan teks; jangan hapus renderer lama tanpa memeriksa pemakai lainnya.

## 3. Navbar dan kaca
- [ ] P2 · Bubble dan morph sudah memakai bahasa gerak bersama; warna Profil dan tombol melayang disejajarkan dengan kartu serta chip dapat bertambah tinggi. Audit konsistensi tint/border/padding pada seluruh layar masih terbuka. Risiko perubahan utama navigasi: **tanya sebelum mengubah perilaku navigasi**, fokus, urutan atau rute.
- [ ] P1 · Haze/navbar memakai sumber blur yang ada; pastikan tidak menambah capture/blur, tetap ringan pada Android lama/perangkat RAM rendah dan nonaktif saat mode ringan. Uji label, touch target dan kontras; belum diuji perangkat.

## 4. Pemutar dan lirik
- [ ] P1 · Peralihan player/lirik potret dan lanskap kini memakai durasi bersama dan tanpa animasi pada mode ringan/perangkat terbatas; permukaan kaca potret tidak ditumpuk dua kali saat lirik terbuka. Animasi pelepasan sampul dan feedback seek hanya bersifat visual dan tunduk pada mode ringan; callback dan ambang gesture tidak diubah. Marquee judul/artis dinonaktifkan pada mode ringan; animasi artis tidak lagi memaksa skala animasi sistem. `PlayerScreen.kt`, `PlayerLayout.kt`, `PlayerGlassCard.kt`: audit pembukaan, Vintage/Kartu Kaca/Piringan, ruang kontrol dan teks panjang; cakupan kaca telah diperluas tetapi hasil visual belum diverifikasi. Risiko: overlay, pemotongan teks/kontras dan jank.
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
- Streak: implementasi awal, tes gerbang dan label aksesibilitas dipush. Build debug, benchmark dan tes terpilih pada Actions 45394915150 lulus; pemeriksaan perangkat masih terbuka.
- Navbar: pengguna menyetujui perubahan visual saja; pegas gelembung, peregangan saat drag dan morph scroll diselaraskan dengan bahasa gerak bersama; morph dan kemunculan navbar tidak dianimasikan pada mode ringan, perangkat terbatas, sistem tanpa animasi atau saat aplikasi tidak aktif; tes gerbang ditambahkan. Tab, rute dan aksi tidak diubah. Profil dan tombol melayang memakai tint/border yang konsisten dengan kartu kaca; ruang nama/tombol Profil dibagi agar lebih tahan teks panjang. Chip shelf menggunakan tinggi minimum agar dapat bertambah untuk font sistem besar. Animasi tombol melayang menghormati mode ringan, perangkat terbatas dan visibilitas layar; tes gerbang ditambahkan dan lulus pada Actions 45410303742. Build, benchmark, dan tes terpilih pada Actions 45410303742 lulus. Pengujian visual perangkat dan konsistensi komponen lain masih terbuka.
- Recap: implementasi awal mesh berbasis tema, transisi, pembatasan lifecycle dan area teks bergulir. Slide hari puncak/jam favorit menggunakan bucket statistik existing dengan izin membaca riwayat; tidak ada perubahan database. Build, benchmark, dan tes Streak/Recap terpilih pada Actions 45402275103 lulus. Pengujian perangkat dan fitur lain masih terbuka.
