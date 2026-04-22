# Ứng dụng hỗ trợ học tập cho sinh viên HaUI

![GitHub repo size](https://img.shields.io/github/repo-size/thaobuiphg/financial-management-app)
![GitHub stars](https://img.shields.io/github/stars/thaobuiphg/financial-management-app?style=social)
![GitHub forks](https://img.shields.io/github/forks/thaobuiphg/financial-management-app?style=social)
![Platform](https://img.shields.io/badge/platform-Android-green)
![Language](https://img.shields.io/badge/language-Java-orange)
![Database](https://img.shields.io/badge/database-SQLite-blue)
![IDE](https://img.shields.io/badge/tool-Android%20Studio-brightgreen)

## Giới thiệu
Đây là ứng dụng Android được phát triển nhằm hỗ trợ sinh viên Trường Đại học Công nghiệp Hà Nội trong việc quản lý học tập một cách hiệu quả.  

Ứng dụng giúp người dùng quản lý thời gian biểu, theo dõi kết quả học tập và lưu trữ tài liệu một cách tập trung, thuận tiện và dễ sử dụng.

---

## Mục tiêu
- Quản lý lịch học, lịch thi và deadline  
- Theo dõi và đánh giá kết quả học tập  
- Lưu trữ và quản lý tài liệu học tập  
- Hỗ trợ xây dựng lộ trình học tập  

---

## Chức năng chính

### Quản lý thời gian biểu
- Thêm, sửa, xóa lịch trình  
- Hiển thị lịch theo tuần  
- Nhắc nhở trước khi sự kiện diễn ra  

### Theo dõi kết quả học tập
- Nhập và quản lý điểm số  
- Tự động tính điểm trung bình (GPA)  
- Gợi ý điểm cần đạt  
- Hiển thị kết quả học tập chi tiết  

### Thống kê và so sánh
- Xem thống kê điểm theo học kỳ  
- So sánh kết quả giữa các kỳ  
- Hiển thị biểu đồ trực quan  

### Quản lý tài liệu học tập
- Lưu trữ tài liệu (file, link)  
- Lọc theo môn học  
- Mở và chia sẻ tài liệu  

---

## Công nghệ sử dụng
- **Ngôn ngữ:** Java  
- **Giao diện:** XML (Android)  
- **Cơ sở dữ liệu:** SQLite (Local Database)  
- **Công cụ:** Android Studio  
- **Thư viện:** MPAndroidChart (vẽ biểu đồ)  

---

## Kiến trúc hệ thống
Android Application
↓
SQLite Database (local)

- Ứng dụng hoạt động **offline**  
- Không sử dụng backend server  
- Dữ liệu lưu trực tiếp trên thiết bị  

---

## Đóng góp cá nhân (Bùi Phương Thảo)
- Xây dựng chức năng **quản lý thời gian biểu**  
- Phát triển các tính năng:
  - Thêm / sửa / xóa lịch trình  
  - Hiển thị lịch theo tuần  
  - Nhắc nhở sự kiện bằng `AlarmManager`  
- Xử lý:
  - Lưu trữ dữ liệu với SQLite  
  - Hiển thị danh sách bằng RecyclerView  

---

## Video demo
[![Xem video](https://img.youtube.com/vi/VIDEO_ID/0.jpg)](https://youtu.be/GiArNymcOSs?si=mCK8gEWViH-5G4eu)

---

## Hướng phát triển
- Tích hợp backend (Firebase / API) để đồng bộ dữ liệu  
- Cải thiện giao diện và trải nghiệm người dùng  
- Nâng cấp thuật toán gợi ý học tập  
- Thêm tính năng cộng đồng (chia sẻ, thảo luận)  

---

## Ghi chú
Dự án được thực hiện trong học phần:  
**Phát triển ứng dụng trên thiết bị di động – HaUI (2025)**
