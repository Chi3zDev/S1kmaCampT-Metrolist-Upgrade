# Audit bug pemutar — Upgrade 2 beta 1

Basis audit: `feature/polish` pada `8e8a2f7e`, tanpa perubahan kode pada tahap audit. Foto 1–6 dari pengujian pengguna; foto 7 adalah referensi visual saja, bukan sumber aset. Tidak ada perangkat Android/emulator yang terhubung ke lingkungan ini, sehingga screenshot bukan bukti semua interaksi, pengaturan, performa, atau kombinasi berhasil. Checklist berikut tidak boleh dicentang sebagai lulus sebelum diverifikasi.

## Matriks pemeriksaan

Dimensi: kaca ON/OFF; `MODERN`, `VINTAGE`, `CLASSIC`, `GLASS_CARD`, `VINYL` (kode memiliki lima tipe, termasuk Modern); thumbnail `COMPACT`, `FULLSCREEN`, `CANVAS`; tombol persegi panjang ON/OFF; tema terang/gelap; layar kecil/besar; font normal/besar. Total 480 kombinasi bila tiap dimensi biner dihitung; sebagian pilihan mungkin tidak berlaku pada gaya tertentu dan harus dicatat sebagai N/A dengan alasannya, bukan dianggap lulus. Tambahkan lirik terbuka/tertutup, pemuatan sampul, orientasi dan mode ringan sebagai pemeriksaan tambahan.

| Tipe | Compact | Fullscreen | Canvas |
|---|---|---|---|
| Modern | [ ] semua varian | [ ] semua varian | [ ] semua varian |
| Vintage | [ ] semua varian | [ ] semua varian | [ ] semua varian |
| Classic / kontrol putih | [ ] semua varian | [ ] semua varian | [ ] semua varian |
| Kartu Kaca | [ ] semua varian | [ ] semua varian | [ ] semua varian |
| Piringan | [ ] semua varian | [ ] semua varian | [ ] semua varian |

Setiap sel berarti kedua keadaan kaca, kedua bentuk tombol, kedua tema, dua ukuran layar, dan dua ukuran font; belum diuji di perangkat. `PlayerScreen.kt` saat ini menonaktifkan jalur thumbnail fullscreen untuk Kartu Kaca dan Piringan (`usesCompactStyleArtwork`); kesesuaian perilaku terhadap permintaan pengguna harus diperiksa secara terpisah.

## Temuan awal dan prioritas

- [ ] **P1-01 — Panel menghilang saat kaca OFF (perbaikan kode awal; menunggu build dan uji perangkat).** Terlihat dari kode: `portraitPlayerGlassVisible` mensyaratkan master kaca dan kaca player, dan `PlayerLayout.kt` juga mengikat permukaan ke pengaturan tersebut. Panel dasar gaya perlu dipisah dari dekorasi kaca. Pengecualian permintaan eksplisit: Piringan tidak memakai panel kartu. Risiko kontras pada latar terang dan permukaan ganda saat lirik.
- [ ] **P1-02 — Pita/ruang kosong sampul dan fullscreen.** Foto 1 menunjukkan bidang kosong gelap. Ukuran dan pemotongan bergantung `usesFullscreenThumbnail`, `PlayerArtworkBackground.kt`, dan layout berbobot; investigasi geometry dan crop pada orientasi berbeda. Perlu placeholder tanpa mengubah aksi seek/swipe.
- [ ] **P1-03 — Tampilan sampul berbeda/flicker.** Foto 1–2 menunjukkan tampilan berbeda, tetapi screenshot tidak membuktikan apakah pilihan thumbnail, kondisi pemuatan, atau state yang berbeda. Periksa `playerThumbnailStyle`, `currentSong`, handoff/crossfade dan kondisi loading; jangan mengklaim reproduksi tanpa langkah atau perangkat.
- [ ] **P1-04 — Kontrol atas.** Foto menunjukkan kemungkinan cincin ganda dan segmen headphone/video bertumpuk; audit `playerControlBlend`, `VintageAudioVideoModeSwitcher`, `ButtonGroup`, ukuran dan border tanpa mengubah aksi.
- [ ] **P1-05 — Tombol bawah.** Audit ukuran sentuh, lebar dan pemotongan pada semua tipe serta font besar. Screenshot saja tidak membuktikan ketepatan target sentuh; periksa di HP/TalkBack.
- [ ] **P1-06 — Kartu Kaca ruang kosong.** Foto 6 menunjukkan ruang di antara sampul dan judul; `GLASS_CARD` memakai weighted container dan pengaturan tinggi minimum. Periksa tinggi pendek/scroll dan pembagian ruang.
- [ ] **P1-07 — Piringan (perbaikan parsial: geometri, kecepatan dan posisi istirahat; belum lengkap/teruji).** Foto 5 berbeda dari acuan 7. Saat ini panel masih aktif saat kaca ON; label 47% dan kecepatan target 120°/detik (~3 detik/putaran); tonearm digambar statis dan tidak punya posisi istirahat. Perbaiki gambar Canvas orisinal, tata letak serta gerak sesuai permintaan; semua callback dan semantik playback tetap existing. Gerak perlu berhenti sesuai visibilitas/mode ringan/skala sistem. Tidak menyalin aset referensi.
- [ ] **P1-08 — Kandidat dan versi.** Siapkan `14.0.1-upgrade.2-rc.2`, versionCode 159, chip About kandidat 2 dan catatan EN/ID tanpa penyebutan beta untuk kandidat baru. Sertifikat harus dibandingkan dengan APK beta 1. Tidak membuat stable release atau merge.

## Validasi akhir yang masih terbuka

- [ ] Semua unit test Android/shared, lint, debug dan benchmark; bandingkan sertifikat dan checksum APK kandidat sebelumnya.
- [ ] Matriks perangkat serta regresi Streak, Recap, navbar, Profil, poster, About, dan nilai bawaan lama.
- [ ] Perbarui `POLISH-DEVICE-TEST.md` untuk bug di atas.
- [ ] Preview sebelum/sesudah tiap gaya kaca ON/OFF memerlukan rendering/perangkat yang benar; jangan gunakan screenshot sintetis sebagai bukti hasil aplikasi. Foto pengguna bisa menjadi bukti sebelum untuk keadaan yang terlihat saja.
- [ ] Simpan/push commit kecil per bug; publish hanya prerelease `v14.0.1-upgrade.2-rc.2` setelah verifikasi yang bisa dilakukan, dengan status pengujian perangkat yang jujur. Tunggu persetujuan eksplisit sebelum merge/final.
