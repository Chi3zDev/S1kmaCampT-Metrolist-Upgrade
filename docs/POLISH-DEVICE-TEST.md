# Daftar uji perangkat — polish Upgrade 2

Status: **belum dijalankan**. Pengguna akan melakukan uji perangkat pada tahap akhir, setelah kandidat disiapkan. Hasil CI bukan pengganti daftar ini.

## Persiapan

- [ ] Cadangkan pengaturan/data dan pastikan tersedia salinan APK yang saat ini terpasang.
- [ ] Catat model HP, versi Android, ukuran font/tampilan, versi aplikasi, dan jenis APK yang diuji.
- [ ] Untuk memasang sebagai pembaruan, pastikan package dan sertifikat kandidat sama dengan aplikasi yang terpasang. APK debug atau benchmark bertanda debug bukan pembaruan produksi; jangan menghapus aplikasi atau data untuk memaksa pemasangan.
- [ ] Jangan membuat, mengubah, atau memalsukan riwayat mendengarkan demi pengujian. Gunakan data yang sudah ada; catat jika data yang dibutuhkan tidak tersedia.

## Tampilan dan gerak

- [ ] Streak: tampilan aktif/padam dan, bila kondisi nyata tersedia, pemulihan, kenaikan level, count-up, kalender serta perayaan. Musik tetap berjalan saat banner/perayaan muncul.
- [ ] Recap: periksa seluruh slide, posisi teks panjang, perpindahan, dan jeda saat ditahan atau dialog dibuka. Hari puncak/jam favorit hanya diperiksa jika data memadai. Bandingkan gambar yang dibagikan dengan slide.
- [ ] Navbar dan Profil: periksa semua tab dan tujuannya, tab Akun ketika kaca mati, pilihan posisi mini player, gelembung saat scroll, tepi/tint kartu dan tombol.
- [ ] Pemutar: buka/tutup dan periksa Vintage, Kartu Kaca, piringan di kiri, aura, seekbar, antrean, judul panjang, lirik dan glow. Pastikan kontrol audio dan seek tetap berfungsi.
- [ ] Poster: uji Klasik/Kartu Kaca, persegi/Story, pilihan ukuran ekstrem dan default, teks panjang, tanpa sampul dan RTL jika tersedia. Bandingkan preview dengan gambar ekspor.
- [ ] About/Settings: lihat badge versi, changelog, pilihan bahasa/font dan penjelasan, serta pastikan kredit dan lisensi tetap tersedia. Preferensi lama tetap terjaga setelah pembaruan.

## Matriks perbaikan pemutar kandidat 2

Status: **belum diuji**. Untuk Modern, Vintage, Classic, Kartu Kaca dan Piringan, catat kaca ON/OFF, thumbnail Compact/Fullscreen/Canvas, tombol persegi panjang ON/OFF, tema terang/gelap, layar kecil/besar, serta font normal/besar. Tandai N/A hanya jika mode tidak berlaku (misalnya artwork piringan khusus), bukan sebagai lulus. Rincian matriks ada di `PLAYER-BUGS.md`.

- [ ] Kaca OFF: panel gelap tanpa kilau; kaca ON: efek kaca tanpa panel ganda. Pada Piringan pastikan tidak ada kartu, termasuk landscape dan saat lirik dibuka.
- [ ] Fullscreen mengisi bidang tanpa pita, terpotong di tengah, scrim cukup; uji sampul kosong, lambat, gagal dan ganti lagu/crossfade tanpa kedip. Periksa juga Canvas dan video, serta Compact.
- [ ] Tombol atas: satu outline dan segmen headphone/video tidak bertumpuk. Uji kedua aksi, ukuran sentuh dan TalkBack.
- [ ] Baris bawah: semua aksi terlihat atau tersedia di menu overflow, tidak terpotong dan target tidak bertumpuk. Coba teks timer panjang dan font besar.
- [ ] Kartu Kaca: jarak sampul/judul serta scroll dan kontrol pada layar pendek. Piringan: crop kiri, ukuran label, alur/kilau, warna latar, judul/artis, tiga kontrol dan seekbar.
- [ ] Piringan berputar sekitar 5–6 detik per putaran, melambat saat jeda; tonearm mengikuti progres dan kembali istirahat. Uji latar depan/belakang, sistem animasi mati dan mode ringan.
- [ ] Simpan screenshot sebelum/sesudah untuk setiap gaya kaca ON/OFF jika perangkat dan kondisi sebelum tersedia. Jangan mengganti bukti dengan gambar sintetis.

## Aksesibilitas dan performa

- [ ] Ulangi bagian penting dalam tema terang/gelap, font sistem besar, mode ringan, animasi sistem mati dan orientasi yang tersedia.
- [ ] Dengan TalkBack, periksa label, urutan fokus, tindakan dan ukuran target sentuh; periksa kontras dan teks terpotong di layar kecil.
- [ ] Gulir dan pindah layar beberapa kali; perhatikan animasi tersendat, panas atau konsumsi baterai yang tidak biasa. Periksa saat aplikasi berpindah ke latar dan kembali.
- [ ] Jika memungkinkan, ulangi mode ringan pada Android 11 atau lebih lama/perangkat memori rendah; catat jika tidak tersedia.

## Catatan hasil

Untuk setiap masalah catat: langkah, yang diharapkan, yang terjadi, apakah konsisten, dan tangkapan layar/video jika aman. Jangan sertakan token, kredensial, kunci signing, atau data pribadi.
