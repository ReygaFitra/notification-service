# ===================================================================================
# Tahap 1: Builder - Lingkungan untuk meng-compile native image
# ===================================================================================
# === FIX: Menggunakan base image yang benar, yang sudah termasuk tool native-image ===
FROM ghcr.io/graalvm/native-image-community:25 AS builder

# Membuat user non-root untuk keamanan
RUN useradd -m graalvm && usermod -aG root graalvm

WORKDIR /usr/src/app

# Salin file-file yang dibutuhkan untuk build Gradle.
COPY --chown=graalvm:root gradlew .
COPY --chown=graalvm:root gradle ./gradle
COPY --chown=graalvm:root build.gradle.kts .
COPY --chown=graalvm:root settings.gradle.kts .
COPY --chown=graalvm:root gradle.properties .

# Berikan izin eksekusi pada Gradle wrapper.
RUN chmod +x ./gradlew

# Berikan kepemilikan direktori kerja kepada user graalvm
RUN chown -R graalvm:root /usr/src/app

# Pindah ke user non-root
USER graalvm

# Hanya download dependensi, jangan build
RUN ./gradlew dependencies --build-cache

# Salin sisa source code aplikasi.
COPY src ./src

# === FIX: Tambahkan 'clean' untuk memaksa Gradle re-detect toolchain ===
RUN ./gradlew clean nativeCompile --no-daemon


# ===================================================================================
# Tahap 2: Deployment - Image final yang sangat kecil
# ===================================================================================
# Menggunakan base image yang sama seperti contoh Anda untuk konsistensi.
FROM docker.io/oraclelinux:8-slim

# Buat user non-root untuk menjalankan aplikasi di production.
RUN useradd -m appuser

WORKDIR /app

# Salin HANYA native binary yang sudah di-compile dari tahap 'builder'.
# Path default untuk output nativeCompile Gradle ada di build/native/nativeCompile/
COPY --from=builder /usr/src/app/build/native/nativeCompile/notification-service /app/application

# Ganti pemilik file aplikasi ke user non-root.
RUN chown appuser:appuser /app/application

# Pindah ke user non-root.
USER appuser

# Expose port yang digunakan oleh Spring Boot.
EXPOSE 8080

# Perintah untuk menjalankan aplikasi.
CMD ["/app/application"]
