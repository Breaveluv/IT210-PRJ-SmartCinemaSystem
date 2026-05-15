# Build stage
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app
COPY . /app
RUN gradle build -x test

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Tạo user không phải root để bảo mật
RUN useradd -m -u 1000 appuser

# Copy jar file từ build stage
COPY --from=builder /app/build/libs/SmartCinemaBookingSystem-0.0.1-SNAPSHOT.jar /app/app.jar

# Thay đổi quyền sở hữu
RUN chown -R appuser:appuser /app

# Sử dụng user không phải root
USER appuser

# Expose port
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]

