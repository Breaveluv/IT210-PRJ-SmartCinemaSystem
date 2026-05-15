# SmartCinemaBookingSystem - Docker Setup

## Yêu cầu

- Docker Desktop (hoặc Docker + Docker Compose)
- RAM tối thiểu: 2GB
- Dung lượng disk: 1GB

## Chạy ứng dụng

### 1. Build và chạy bằng Docker Compose

```bash
# Build image và chạy tất cả services
docker-compose up --build

# Nếu chỉ chạy (đã build trước đó)
docker-compose up

# Chạy ở chế độ background
docker-compose up -d

# Xem logs của ứng dụng
docker-compose logs -f app

# Xem logs của MySQL
docker-compose logs -f mysql
```

### 2. Dừng ứng dụng

```bash
# Dừng tất cả services
docker-compose down

# Dừng và xóa volumes (xóa database)
docker-compose down -v
```

## Truy cập ứng dụng

- **URL ứng dụng**: http://localhost:8080
- **Database MySQL**: localhost:3306
  - Username: root
  - Password: 123456
  - Database: smartcinema_db

## Cấu hình

### Biến môi trường có thể tùy chỉnh

Sửa file `docker-compose.yml`:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/smartcinema_db?...
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: 123456
```

### Port khác

Thay đổi port trong `docker-compose.yml`:

```yaml
ports:
  - "9090:8080"  # Truy cập: http://localhost:9090
```

## Build image riêng lẻ

Nếu muốn build image mà không dùng docker-compose:

```bash
# Build image
docker build -t smartcinema-app:1.0 .

# Chạy container (cần MySQL chạy riêng hoặc tạo network)
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/smartcinema_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=123456 \
  smartcinema-app:1.0
```

## Troubleshooting

### Ứng dụng không kết nối được database

- Đảm bảo MySQL container đã khởi động xong (chờ 10-15 giây)
- Kiểm tra logs: `docker-compose logs mysql`
- Kiểm tra network: `docker network ls`

### Tất cả containers dừng lại

```bash
# Xóa containers cũ
docker-compose down

# Xóa images cũ
docker image prune

# Build lại
docker-compose up --build
```

### Xóa dữ liệu database

```bash
# Xóa volume MySQL
docker volume rm smartcinemabookingsystem_mysql_data

# Hoặc sử dụng lệnh
docker-compose down -v
```

## Hiệu suất

- JVM Heap: Mặc định 256MB - 512MB
- Để tăng Performance, sửa `JAVA_OPTS` trong docker-compose.yml:
  ```yaml
  JAVA_OPTS: "-Xmx1024m -Xms512m"
  ```

## Notes

- Ứng dụng auto-create database nếu chưa tồn tại
- Hibernate sẽ tự động tạo/cập nhật các bảng (ddl-auto=update)
- MySQL data được lưu trữ trong volume `mysql_data`
- Logs ứng dụng được lưu trong thư mục `logs/`

