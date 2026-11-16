import os
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from report_helpers import (
    set_cell_background, set_cell_margins, set_table_borders,
    add_callout, add_styled_table, add_heading_1, add_heading_2, add_heading_3,
    add_paragraph, add_bullet_point, add_image_with_caption
)

def create_document():
    doc = docx.Document()

    # 1. Page Margins (A4 format standard: 20mm top/bottom, 25mm left, 20mm right)
    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(0.8)
        section.page_width = Inches(8.27)
        section.page_height = Inches(11.69)

    # =========================================================================
    # COVER / HEADER BANNER
    # =========================================================================
    p_univ = doc.add_paragraph()
    p_univ.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_univ.paragraph_format.space_before = Pt(0)
    p_univ.paragraph_format.space_after = Pt(2)
    r_univ = p_univ.add_run("BÁO CÁO KIẾN TRÚC HỆ THỐNG & ĐẶC TẢ QUY TRÌNH NGHIỆP VỤ\n")
    r_univ.font.name = "Times New Roman"
    r_univ.font.size = Pt(13)
    r_univ.font.bold = True
    r_univ.font.color.rgb = RGBColor(0x47, 0x55, 0x69)

    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(8)
    p_title.paragraph_format.space_after = Pt(6)
    r_title = p_title.add_run("HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH (WEB-DU-LICH)\n(VÉ MÁY BAY & VÉ TÀU HỎA)")
    r_title.font.name = "Times New Roman"
    r_title.font.size = Pt(18)
    r_title.font.bold = True
    r_title.font.color.rgb = RGBColor(0x1B, 0x36, 0x5D)

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_before = Pt(0)
    p_sub.paragraph_format.space_after = Pt(14)
    r_sub = p_sub.add_run("Multi-Tier System Architecture & End-to-End Business Workflow Specification\n"
                          "Nền tảng: Java 17 | Spring Boot 3.2.5 | Spring Security 6 | Spring Data JPA | Thymeleaf | MS SQL Server")
    r_sub.font.name = "Times New Roman"
    r_sub.font.size = Pt(10.5)
    r_sub.font.italic = True
    r_sub.font.color.rgb = RGBColor(0x02, 0x84, 0xC7)

    # Decorative horizontal separator line
    p_line = doc.add_paragraph()
    p_line.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_line.paragraph_format.space_after = Pt(12)
    r_line = p_line.add_run("―" * 55)
    r_line.font.color.rgb = RGBColor(0xCB, 0xD5, 0xE1)

    # Metadata Box
    add_callout(doc, [
        "Học phần / Chuyên đề: Phát triển Ứng dụng Web Doanh nghiệp & Kiến trúc Hệ thống",
        "Tên đồ án: Xây dựng Hệ thống Quản lý và Phân phối Vé Du lịch Trực tuyến (Web-Du-Lich)",
        "Loại hình vé hỗ trợ: 02 phân hệ chuyên biệt - Vé Máy Bay Hàng Không & Vé Tàu Hỏa Đường Sắt",
        "Công nghệ chủ đạo: Spring Boot, Spring Web MVC, Spring Data JPA, Hibernate, Thymeleaf, SQL Server",
        "Mục tiêu báo cáo: Hoàn thành 100% hai yêu cầu trọng tâm: (1) Thiết kế & phân tích kiến trúc đa tầng tổng thể; (2) Đặc tả chi tiết các dòng chảy công việc (Workflows) cốt lõi của đề tài."
    ], title="THÔNG TIN HỒ SƠ ĐỒ ÁN")

    # =========================================================================
    # LỜI MỞ ĐẦU
    # =========================================================================
    add_heading_1(doc, "LỜI MỞ ĐẦU & TỔNG QUAN HỆ THỐNG")
    add_paragraph(doc, 
        "Trong bối cảnh chuyển đổi số của ngành du lịch và vận tải hành khách hiện đại, nhu cầu tích hợp đa phương thức "
        "di chuyển trên cùng một nền tảng số đang trở thành xu thế tất yếu. Người tiêu dùng ngày nay không chỉ tìm kiếm "
        "các chuyến bay thương mại đường dài với thời gian tối ưu, mà còn có nhu cầu rất lớn đối với phương thức vận tải "
        "đường sắt (tàu hỏa) nhờ tính an toàn, cảnh quan du lịch và chi phí hợp lý.")
    add_paragraph(doc,
        "Đề tài 'Hệ Thống Quản Lý Đặt Vé Du Lịch' (Web-Du-Lich) được nghiên cứu và hiện thực hóa nhằm giải quyết trọn vẹn "
        "bài toán kinh doanh đa phương tiện này. Hệ thống được xây dựng dựa trên kiến trúc phân tầng chuẩn mực "
        "(Multi-Tier Enterprise Architecture), áp dụng nghiêm ngặt các nguyên lý thiết kế phần mềm hiện đại như "
        "Separation of Concerns (SoC), Single Responsibility Principle (SRP) và các mẫu thiết kế hướng đối tượng "
        "(Strategy Pattern, Factory Pattern, Front Controller Pattern, PRG Pattern).")
    add_paragraph(doc,
        "Báo cáo chuyên đề này được biên soạn bám sát yêu cầu nghiệp vụ thực tế, tập trung làm rõ 2 nội dung then chốt:", bold_prefix="Nhiệm vụ trọng tâm: ")
    add_bullet_point(doc, "Yêu cầu 1: ", "Vẽ sơ đồ kiến trúc tổng thể (Architecture System) của hệ thống và giải thích chi tiết cơ chế hoạt động, vai trò của từng tầng và từng thành phần cấu thành.")
    add_bullet_point(doc, "Yêu cầu 2: ", "Nêu các chức năng cốt lõi trong đồ án và mô tả chi tiết dòng chảy công việc (Workflows) từ góc độ nghiệp vụ, tương tác giao diện cho đến xử lý giao dịch dữ liệu phía backend.")

    # =========================================================================
    # PHẦN 1: KIẾN TRÚC TỔNG THỂ HỆ THỐNG (MULTI-TIER SYSTEM ARCHITECTURE)
    # =========================================================================
    add_heading_1(doc, "PHẦN 1: THIẾT KẾ KIẾN TRÚC TỔNG THỂ HỆ THỐNG (SYSTEM ARCHITECTURE)")
    
    add_heading_2(doc, "1.1. Sơ đồ Kiến trúc Tổng thể Hệ thống (Architecture System Diagram)")
    add_paragraph(doc,
        "Hệ thống 'Web-Du-Lich' được tổ chức theo mô hình kiến trúc 4 tầng (Multi-Tier Architecture). "
        "Kiến trúc này đảm bảo tính độc lập giữa giao diện hiển thị, lớp điều phối ứng dụng, lớp nghiệp vụ cốt lõi "
        "và tầng lưu trữ dữ liệu bền vững. Sơ đồ dưới đây minh họa toàn diện bức tranh tổng thể cấu trúc hệ thống:")

    # Insert System Architecture Diagram
    add_image_with_caption(doc, "system_architecture.png", 
                           "Hình 1.1: Sơ đồ Kiến trúc Đa tầng Tổng thể Hệ thống Quản lý Đặt Vé Du Lịch (Web-Du-Lich)", 
                           width_inches=6.5)

    add_callout(doc, [
        "Presentation Tier: Đảm nhiệm hiển thị giao diện tương tác cho Khách hàng, Nhân viên phòng vé và Quản trị viên dựa trên Thymeleaf SSR, Bootstrap 5 và sơ đồ toa xe trực quan.",
        "Application Controller Tier: Vận hành trên Tomcat 8080, do DispatcherServlet điều phối, kiểm soát an ninh đa tầng qua Spring Security 6 RBAC và lọc yêu cầu qua các Spring MVC Controllers chuyên biệt.",
        "Business Logic & Service Tier: Trái tim xử lý của hệ thống, quản lý tính nguyên tử giao dịch (@Transactional), đồng bộ khóa chống bán vượt chỗ (ConcurrentHashMap & Pessimistic Locks), quản lý Strategy/Factory cho 2 loại vé và Scheduler giải phóng chỗ giữ.",
        "Data Access & Persistence Tier: Tối ưu hóa truy vấn với HikariCP Connection Pool, Spring Data JPA/Hibernate ORM và cơ sở dữ liệu Microsoft SQL Server đạt chuẩn ACID tuyệt đối."
    ], title="TÓM TẮT CẤU TRÚC ĐA TẦNG (4 TIERS)")

    add_heading_2(doc, "1.2. Giải thích Cơ chế Hoạt động & Vai trò của Từng Thành phần")

    # 1.2.1 Presentation Tier
    add_heading_3(doc, "1.2.1. Tầng Giao Diện Người Dùng (Presentation Tier - Client & Admin Interfaces)")
    add_paragraph(doc,
        "Tầng Presentation đóng vai trò là cửa ngõ giao tiếp trực tiếp giữa người dùng với toàn bộ hệ thống. "
        "Tầng này không lưu trữ logic nghiệp vụ mà thuần túy thu thập dữ liệu đầu vào, hiển thị trạng thái và "
        "truyền tải các yêu cầu HTTP/HTTPS về phía máy chủ ứng dụng.")
    
    add_paragraph(doc, "Hệ thống phân định rõ ràng 3 nhóm tác nhân (Actors) chính:", bold_prefix="Các tác nhân tương tác (Actors):")
    add_bullet_point(doc, "Khách hàng (End-User / Customer & Guest): ", 
                     "Bao gồm khách vãng lai và thành viên đã đăng ký tài khoản. Khách hàng có thể tra cứu lịch bay và lịch chạy tàu theo thời gian thực; "
                     "xem sơ đồ ghế và tình trạng chỗ trống theo từng khoang tàu; lựa chọn hạng vé; nhập thông tin hành khách; "
                     "giữ chỗ có thời hạn (15 phút đối với Vé máy bay, 10 phút đối với Vé tàu hỏa); thực hiện thanh toán; "
                     "tra cứu đơn vé cá nhân bằng Số điện thoại và Mã đặt chỗ; đăng ký lịch hẹn tư vấn chuyến đi.")
    add_bullet_point(doc, "Nhân viên phòng vé (Ticket Officer / Teller - ROLE_NHANVIEN): ", 
                     "Tiếp nhận danh sách các yêu cầu đặt lịch hẹn tư vấn từ khách hàng; liên hệ tư vấn hành trình; "
                     "hỗ trợ khách hàng xuất vé điện tử; xử lý các yêu cầu đổi chuyến bay; xác nhận thanh toán tiền mặt/chuyển khoản; "
                     "và thực hiện chính sách hoàn hủy vé theo quy định.")
    add_bullet_point(doc, "Quản trị viên hệ thống (System Administrator - ROLE_ADMIN): ", 
                     "Nắm quyền quản trị toàn diện hệ thống: cấu hình thêm/sửa chuyến bay, hãng hàng không, sân bay; "
                     "quản lý danh mục đoàn tàu, cấu hình toa xe (ghế mềm, giường nằm khoang 4, khoang 6) và các ga tàu; "
                     "quản trị tài khoản người dùng và phân quyền RBAC; giám sát Dashboard báo cáo doanh thu tài chính và tỷ lệ lấp đầy ghế (Occupancy Rate).")

    add_paragraph(doc, "Ngăn xếp công nghệ phía máy khách (Frontend Technology Stack):", bold_prefix="Công nghệ sử dụng: ")
    add_bullet_point(doc, "Thymeleaf Template Engine 3.1: ", 
                     "Công nghệ Server-Side Rendering (SSR) hiện đại của hệ sinh thái Spring. Thymeleaf xử lý sinh mã HTML trực tiếp tại server, "
                     "giúp tối ưu hóa SEO vượt trội so với Single Page Application (SPA), bảo mật cao do không để lộ logic nghiệp vụ ra trình duyệt, "
                     "đồng thời tự động tích hợp thẻ CSRF token ẩn vào mọi biểu mẫu form.")
    add_bullet_point(doc, "HTML5, CSS3 & Bootstrap 5.3: ", 
                     "Đảm bảo giao diện hiện đại, sạch sẽ và hoàn toàn tương thích Responsive Web Design trên mọi kích thước màn hình từ Desktop, Tablet đến Smartphone.")
    add_bullet_point(doc, "Sơ đồ Toa tàu Tương tác Trực quan (Train Seat Map UI): ", 
                     "Được dựng bằng CSS Grid và mã màu thời gian thực: Màu xanh lá (Ghế trống), Màu đỏ/xám (Ghế đã bán trên chặng trùng), "
                     "Màu vàng (Ghế đang được người khác giữ chỗ), Màu xanh dương (Ghế người dùng đang chọn).")
    add_bullet_point(doc, "Vanilla JavaScript & AJAX: ", 
                     "Cung cấp đồng hồ đếm ngược thời gian thực (Countdown Timer) thông báo chính xác số phút/giây còn lại trong phiên giữ chỗ; "
                     "hỗ trợ validate dữ liệu tức thì phía trình duyệt trước khi người dùng gửi form lên máy chủ.")
    add_bullet_point(doc, "Mô hình Post/Redirect/Get (PRG Pattern): ", 
                     "Được áp dụng nghiêm ngặt cho toàn bộ các form giao dịch nhạy cảm (đặt chỗ, thanh toán, đăng ký tư vấn). "
                     "Sau khi nhận request POST, server xử lý lưu trữ và ngay lập tức trả về mã HTTP 302 Redirect sang trang GET. "
                     "Cơ chế này loại bỏ hoàn toàn rủi ro người dùng bấm F5 hoặc reload trang dẫn đến việc tạo bản ghi trùng lặp (Double Submission).")

    # 1.2.2 Application Controller Tier
    add_heading_3(doc, "1.2.2. Tầng Điều Khiển Ứng Dụng (Application Controller Tier - Spring Web MVC & Security)")
    add_paragraph(doc,
        "Tầng Application Controller là đầu não tiếp nhận mọi yêu cầu gửi đến từ tầng Presentation, "
        "thực hiện xác thực danh tính, phân quyền bảo mật, kiểm tra tính hợp lệ của dữ liệu đầu vào (Validation), "
        "đóng gói dữ liệu vào các DTO và điều phối gọi xuống các dịch vụ nghiệp vụ tương ứng.")

    add_paragraph(doc, "Hạ tầng máy chủ và Bộ điều phối trung tâm:", bold_prefix="Web Server & Front Controller: ")
    add_bullet_point(doc, "Embedded Apache Tomcat Server (Port 8080): ", 
                     "Máy chủ web nhúng sẵn trong Spring Boot, hoạt động theo mô hình đa luồng (Multi-threaded Non-blocking IO), "
                     "sẵn sàng tiếp nhận và phục vụ hàng nghìn kết nối đồng thời với độ trễ tối thiểu.")
    add_bullet_point(doc, "DispatcherServlet (Front Controller Pattern): ", 
                     "Mọi HTTP Request từ trình duyệt đều đi qua cửa ngõ duy nhất là `DispatcherServlet`. "
                     "Nó tra cứu `HandlerMapping` để tìm Controller và Method tương ứng; sử dụng `HandlerAdapter` để bóc tách tham số "
                     "và chuyển đổi dữ liệu HTTP thành các đối tượng Java DTO; sau đó tiếp nhận kết quả trả về từ Controller "
                     "và điều phối qua `ViewResolver` để kết xuất giao diện Thymeleaf hoàn chỉnh trả về client.")

    add_paragraph(doc, "Hệ thống Bảo mật và Phân quyền (Spring Security 6 RBAC):", bold_prefix="Cơ chế Bảo mật & Phân quyền: ")
    add_bullet_point(doc, "Cấu hình SecurityFilterChain: ", 
                     "Hệ thống phân cấp bảo mật chặt chẽ bằng cách cấu hình chuỗi bộ lọc Filter Chain tại `SecurityConfig.java`. "
                     "Các tài nguyên tĩnh (/css/**, /js/**, /images/**) và trang tra cứu tìm kiếm chuyến đi được mở công khai (`permitAll()`). "
                     "Trang cá nhân (`/my-bookings/**`) yêu cầu quyền `ROLE_KHACH`, `ROLE_NHANVIEN` hoặc `ROLE_ADMIN`. "
                     "Khu vực quản trị cấu hình hệ thống (`/admin/flights/**`, `/admin/trains/**`, `/admin/users/**`) được khóa chặt chỉ dành riêng cho `ROLE_ADMIN`.")
    add_bullet_point(doc, "Mã hóa Mật khẩu Chuẩn Công nghiệp (BCryptPasswordEncoder): ", 
                     "Toàn bộ mật khẩu người dùng trong bảng `users` được băm mật mã bằng thuật toán BCrypt với độ mạnh 10 rounds, "
                     "kết hợp muối ngẫu nhiên (salt) chống lại hoàn toàn các hình thức tấn công vét cạn (brute-force) hay tra bảng băm (rainbow table).")
    add_bullet_point(doc, "Bảo vệ CSRF Toàn diện: ", 
                     "Bảo vệ chống tấn công mạo danh Cross-Site Request Forgery bằng cách tự động đính kèm và kiểm tra mã token bí mật trong mọi yêu cầu POST/PUT/DELETE.")
    add_bullet_point(doc, "Kiểm soát Truy cập Đơn hàng (Data Privacy Guard): ", 
                     "Hàm `validateBookingAccess()` trong các Controller đảm bảo khách hàng chỉ có thể xem chi tiết đơn vé nếu cung cấp đúng Số điện thoại "
                     "đã dùng khi đặt vé hoặc đăng nhập đúng tài khoản sở hữu đơn, ngăn ngừa hoàn toàn nguy cơ rò rỉ dữ liệu hành khách.")

    add_paragraph(doc, "Bảng danh mục các Controller cốt lõi và nhiệm vụ cụ thể:", bold_prefix="Danh sách Key Controllers trong Project:")
    
    ctrl_headers = ["Controller", "Request Mapping", "Phân quyền (RBAC)", "Vai trò & Nhiệm vụ nghiệp vụ chính"]
    ctrl_data = [
        ["HomeController", "/", "Công khai (PermitAll)", "Hiển thị trang chủ, banner giới thiệu, thống kê số lượng chuyến bay, tuyến tàu và số khách đã phục vụ."],
        ["FlightController", "/flights/**", "Công khai / Khách", "Điều phối tìm kiếm chuyến bay, chọn hạng vé (Eco/Bus), submit form hành khách, tạo giữ chỗ 15p, hiển thị chi tiết vé, đổi chuyến bay."],
        ["TrainController", "/trains/**", "Công khai / Khách", "Tìm lịch tàu theo ga đi - ga đến, chọn toa xe, gọi thuật toán kiểm tra chỗ trống sơ đồ ghế, giữ chỗ 10p, hiển thị chi tiết vé tàu."],
        ["ConsultationController", "/consultations/**", "Công khai (Form) / Nhân viên (List)", "Tiếp nhận form đăng ký lịch hẹn tư vấn chuyến đi (áp dụng PRG Pattern), validate khung giờ hẹn, cho phép nhân viên tiếp nhận và xử lý."],
        ["CustomerController", "/my-bookings/**", "ROLE_KHACH, NHANVIEN, ADMIN", "Cho phép khách hàng tra cứu toàn bộ lịch sử các đơn đặt vé máy bay và tàu hỏa đã thực hiện."],
        ["AdminController", "/admin/**", "ROLE_ADMIN & ROLE_NHANVIEN", "Dashboard thống kê doanh thu, tỷ lệ lấp đầy ghế; CRUD chuyến bay, đoàn tàu, toa xe, ga tàu; phân quyền user; xác nhận xuất vé."],
        ["AuthController", "/login, /register", "Công khai (PermitAll)", "Xử lý đăng ký tài khoản thành viên mới, form đăng nhập và điều hướng thông minh dựa trên Role sau khi đăng nhập thành công."]
    ]
    add_styled_table(doc, ctrl_headers, ctrl_data, [Inches(1.5), Inches(1.2), Inches(1.3), Inches(2.5)])

    add_paragraph(doc, "Đối tượng Truyền Dữ Liệu (Data Transfer Objects - DTOs) & Bean Validation:", bold_prefix="Tầng DTO & Validation: ")
    add_bullet_point(doc, "Tính Độc Lập DTO: ", 
                     "Hệ thống không liên kết trực tiếp Entity CSDL với form web mà sử dụng các DTO chuyên biệt như `FlightBookingRequestDto`, `TrainBookingRequestDto`, "
                     "`PassengerInputDto`, `TrainPassengerInputDto`, `FlightSearchDto`, `TrainSearchDto`, `ChangeFlightRequestDto`. "
                     "Việc này ngăn chặn lỗ hổng Mass Assignment và tối ưu hóa lượng dữ liệu vận chuyển trên mạng.")
    add_bullet_point(doc, "Kiểm Tra Ràng Buộc Nghiêm Ngặt (Hibernate Validator / JSR-380): ", 
                     "Sử dụng các annotation khai báo: `@Valid`, `@NotBlank`, `@Size`, `@Pattern` (kiểm tra CCCD 12 chữ số, Hộ chiếu chuẩn quốc tế 1 ký tự hoa + 7-8 số, SĐT 10 số), "
                     "`@FutureOrPresent` (ngày khởi hành phải từ hôm nay trở đi), `@Min`, `@Max`. Bất kỳ vi phạm nào đều được `BindingResult` bắt giữ và hiển thị lỗi thân thiện.")
    add_bullet_point(doc, "Xử Lý Lỗi Tập Trung (@ControllerAdvice): ", 
                     "Lớp `GlobalExceptionHandler` bắt giữ toàn bộ `BusinessException`, `ResourceNotFoundException` và lỗi hệ thống không mong muốn, "
                     "chuyển đổi thành thông điệp rõ ràng trên trang lỗi `error.html` mà không để lộ cấu trúc nội bộ máy chủ.")

    # 1.2.3 Business Logic & Service Tier
    add_heading_3(doc, "1.2.3. Tầng Xử Lý Nghiệp Vụ Cốt Lõi (Business Logic & Service Tier - Core Ticket Engine)")
    add_paragraph(doc,
        "Business Logic & Service Tier là trái tim của hệ thống Web-Du-Lich. Toàn bộ các thuật toán tính toán giá vé phức tạp, "
        "chính sách phụ thu, thuật toán kiểm tra chồng lấn chặng tàu, kiểm soát tương tranh ghế ngồi và quản lý vòng đời đơn vé "
        "đều được hiện thực hóa trọn vẹn tại tầng này.")

    add_paragraph(doc, "Kiến trúc Mẫu Thiết Kế Hướng Đối Tượng (Design Patterns):", bold_prefix="Áp dụng Strategy Pattern & Factory Pattern:")
    add_paragraph(doc,
        "Hệ thống định nghĩa Interface chiến lược chung `TicketService` bao bọc 4 hành vi cốt lõi của mọi loại hình vận tải: "
        "`getTicketType()`, `calculateTotalFare()`, `validateBooking()`, `processBooking()`, `cancelBooking()`. "
        "Thành phần `TicketServiceFactory` đóng vai trò là nhà máy cung cấp dịch vụ, tự động ánh xạ và phân phối Service xử lý "
        "tương ứng với loại vé (FLIGHT -> `FlightTicketService`, TRAIN -> `TrainTicketService`). Thiết kế này đáp ứng hoàn hảo "
        "nguyên lý Open/Closed Principle (OCP), cho phép hệ thống dễ dàng mở rộng thêm các phương thức vận tải mới (như BusTicketService, CruiseTicketService) "
        "trong tương lai mà không cần sửa đổi mã nguồn hiện có.")

    add_paragraph(doc, "Bảng so sánh chi tiết nghiệp vụ giữa 2 dịch vụ vé cốt lõi:", bold_prefix="So sánh chi tiết 2 Service nghiệp vụ:")
    srv_headers = ["Đặc tả Nghiệp vụ", "FlightTicketService (Vé Máy Bay)", "TrainTicketService (Vé Tàu Hỏa)"]
    srv_data = [
        ["Thuật toán tính giá vé", 
         "Giá = (Giá cơ bản * Hệ số hạng ghế [Bus: 1.8, Eco: 1.0] * Hệ số thời điểm bay cận ngày) + Thuế an ninh sân bay cố định 120.000đ/khách + Phụ phí hành lý quá cước 50.000đ/kg.", 
         "Giá = Khoảng cách cự ly giữa Ga đi và Ga đến (km) * Đơn giá loại toa (Ghế mềm: 700đ/km, Giường K6: 1.000đ/km, VIP K4: 1.300đ/km) + Phí bảo hiểm 10.000đ."],
        ["Chính sách đối tượng hành khách", 
         "Người lớn (100%), Trẻ em 2-12 tuổi (75%), Em bé < 2 tuổi (10% giá vé, ngồi chung với người lớn). Bắt buộc tỷ lệ em bé <= số người lớn.", 
         "Áp dụng giảm trừ ưu tiên xã hội: Học sinh / Sinh viên (giảm 10%), Người cao tuổi trên 60 tuổi (giảm 15%), Trẻ em 6-10 tuổi (giảm 25%), Trẻ em < 6 tuổi miễn vé."],
        ["Thuật toán phân bổ chỗ ngồi", 
         "Quản lý theo tổng số lượng chỗ còn lại của chuyến bay (`availableEconomySeats`, `availableBusinessSeats`). Khóa giữ chỗ theo số lượng yêu cầu.", 
         "Thuật toán Segment Overlap Check kiểm tra trực quan trên từng vị trí ghế: một ghế có thể bán cho nhiều khách nếu các khoảng chặng [Ga đi, Ga đến] không giao cắt."],
        ["Thời hạn Giữ chỗ (Hold Limit)", 
         "15 phút tính từ thời điểm tạo đơn giữ chỗ thành công.", 
         "10 phút tính từ thời điểm chọn ghế trên sơ đồ toa xe."],
        ["Chính sách Đổi vé (Change Policy)", 
         "Cho phép đổi sang chuyến bay mới: Phí đổi cố định 350.000đ/khách + Chênh lệch giá vé chuyến mới (nếu giá mới cao hơn). Chuyến cũ phải còn trước giờ bay.", 
         "Hủy vé cũ và đặt vé mới theo chính sách hoàn vé."],
        ["Chính sách Hoàn / Hủy vé (Refund)", 
         "Hủy trước giờ bay >= 24h: Phí hủy 10% (hoàn 90%). Hủy từ 4h - 24h: Phí hủy 30% (hoàn 70%). Dưới 4h hoặc sau cất cánh: Không được hoàn tiền.", 
         "Hủy trước giờ tàu chạy >= 24h: Phí hủy 10%. Từ 4h - 24h: Phí hủy 20%. Dưới 4h: Phí hủy 30%. Tàu đã khởi hành: Không hoàn tiền."]
    ]
    add_styled_table(doc, srv_headers, srv_data, [Inches(1.8), Inches(2.4), Inches(2.3)])

    add_paragraph(doc, "Quản lý Giao Dịch Nguyên Tử & Kiểm Soát Tương Tranh (Concurrency Control):", bold_prefix="An Toàn Giao Dịch & Concurrency: ")
    add_bullet_point(doc, "Tính Nguyên Tử Giao Dịch (@Transactional): ", 
                     "Tất cả các phương thức đặt chỗ, hủy vé, đổi vé đều được bao bọc trong annotation `@Transactional(isolation = Isolation.REPEATABLE_READ)`. "
                     "Đảm bảo các thao tác ghi dữ liệu phức tạp (giảm số ghế trống, tạo bản ghi Booking, tạo danh sách TicketDetail, tạo bản ghi Payment) "
                     "hoặc cùng thành công trọn vẹn (Commit) hoặc tự động quay lui (Rollback) hoàn toàn nếu có bất kỳ ngoại lệ nào xảy ra, "
                     "không bao giờ để lại trạng thái dữ liệu rác hay dữ liệu không nhất quán.")
    add_bullet_point(doc, "Cơ Chế Khóa Đồng Thời Chống Bán Vượt Chỗ (Overbooking Prevention): ", 
                     "Hệ thống áp dụng mô hình khóa kết hợp: Trong bộ nhớ (In-Memory Locking) thông qua `ConcurrentHashMap<Long, Object> flightLocks / trainLocks`. "
                     "Khi một luồng thực hiện đặt chỗ cho chuyến bay có ID = X, nó sẽ chiếm giữ khóa màn hình `synchronized(flightLock)`. "
                     "Nhờ đó, dù có hàng trăm người dùng cùng bấm nút 'Xác nhận đặt vé' vào cùng một phần nghìn giây, các thao tác kiểm tra số ghế và trừ ghế "
                     "vẫn được xếp hàng thực thi tuần tự tuyệt đối, loại bỏ 100% nguy cơ Race Condition và Overbooking.")

    add_paragraph(doc, "Tác vụ Ngầm Tự Động Giải Phóng Ghế Quá Hạn (Scheduler & Notifications):", bold_prefix="Hệ Thống Scheduler Tự Động: ")
    add_bullet_point(doc, "BookingHoldExpirationScheduler: ", 
                     "Được kích hoạt định kỳ mỗi 60 giây (`@Scheduled(fixedDelay = 60000)`). Scheduler tự động quét trong CSDL tìm các đơn vé ở trạng thái `HOLD` "
                     "đã vượt quá thời gian giữ chỗ (15 phút đối với Vé máy bay, 10 phút đối với Vé tàu hỏa). "
                     "Ngay khi phát hiện, hệ thống tự động: (1) Hoàn trả số ghế trống vào chuyến bay hoặc nhả trạng thái ghế trên toa tàu; "
                     "(2) Đổi trạng thái đơn vé sang `EXPIRED`; (3) Ghi nhật ký hệ thống. Cơ chế này đảm bảo ghế không bị 'om' vô thời hạn, "
                     "giúp tối đa hóa doanh thu cho doanh nghiệp vận tải.")
    add_bullet_point(doc, "EmailService Thông Minh: ", 
                     "Dịch vụ gửi email tự động gửi thông báo xác nhận đặt chỗ thành công, cung cấp mã PNR điện tử, thông tin hành khách và chi tiết thanh toán "
                     "ngay khi giao dịch hoàn tất.")

    # 1.2.4 Data Access & Persistence Tier
    add_heading_3(doc, "1.2.4. Tầng Truy Cập & Lưu Trữ Dữ Liệu (Data Access & Persistence Tier - SQL Database)")
    add_paragraph(doc,
        "Tầng Data Access & Persistence chịu trách nhiệm lưu trữ bền vững toàn bộ dữ liệu nghiệp vụ của hệ thống, "
        "đảm bảo tính toàn vẹn tham chiếu, khả năng khôi phục sau sự cố và cung cấp tốc độ truy vấn tối ưu.")

    add_paragraph(doc, "Bể Kết Nối Hiệu Năng Cao (HikariCP Connection Pooling):", bold_prefix="Hạ tầng Bể kết nối (HikariCP): ")
    add_paragraph(doc,
        "Hệ thống tích hợp HikariCP - giải pháp Connection Pool hàng đầu thế giới về hiệu năng và độ trễ thấp. "
        "HikariCP duy trì sẵn một số lượng kết nối kết nối vật lý mở tới Microsoft SQL Server (cấu hình minimum-idle, maximum-pool-size, connection-timeout). "
        "Khi một tác vụ từ Service gửi yêu cầu truy vấn, HikariCP cung cấp ngay một kết nối có sẵn từ hàng đợi thay vì phải trải qua quá trình bắt tay TCP "
        "và xác thực phức tạp, giúp giảm thiểu độ trễ truy vấn xuống mức microsecond.")

    add_paragraph(doc, "Khung Ánh Xạ Đối Tượng Quan Hệ (Spring Data JPA & Hibernate 6 ORM):", bold_prefix="Lớp ORM & JPA Repositories: ")
    add_paragraph(doc,
        "Spring Data JPA cung cấp giao diện lập trình mạnh mẽ trên nền tảng Hibernate ORM. "
        "Hệ thống không sử dụng các câu lệnh SQL ghép chuỗi thủ công dễ bị tấn công SQL Injection mà hoàn toàn làm việc với các JPA Repositories "
        "(`FlightRepository`, `TrainRepository`, `TrainStationRepository`, `TrainCoachRepository`, `TrainSeatRepository`, "
        "`TrainTicketRepository`, `FlightBookingRepository`, `FlightPassengerRepository`, `CustomerRepository`, `UserRepository`, `PaymentRepository`, `ConsultationRepository`). "
        "JPA tự động sinh mã các thao tác CRUD cơ bản, đồng thời hỗ trợ ngôn ngữ truy vấn hướng đối tượng JPQL và Derived Query Methods "
        "để thực hiện các phép lọc dữ liệu phức tạp theo điều kiện ngày, sân bay và tuyến ga.")

    add_paragraph(doc, "Hệ Quản Trị Cơ Sở Dữ Liệu Quan Hệ (Microsoft SQL Server):", bold_prefix="Hệ CSDL Microsoft SQL Server: ")
    add_bullet_point(doc, "Tính Toàn Vẹn ACID Tuyệt Đối: ", 
                     "Microsoft SQL Server đảm bảo đầy đủ 4 thuộc tính ACID (Atomicity, Consistency, Isolation, Durability), "
                     "bảo đảm rằng một khi giao dịch đã hoàn tất thành công, dữ liệu được ghi cố định xuống ổ đĩa và không bao giờ bị mất mát ngay cả khi mất điện đột ngột.")
    add_bullet_point(doc, "Mô Hình Thực Thể Quan Hệ (Relational Schema): ", 
                     "Cơ sở dữ liệu được chuẩn hóa ở mức 3NF với 15 bảng quan hệ chặt chẽ: `users`, `customers`, `bookings`, `ticket_details`, "
                     "`flights`, `flight_bookings`, `flight_passengers`, `trains`, `train_coaches`, `train_seats`, `train_stations`, "
                     "`train_bookings`, `train_tickets`, `payments`, `consultations`.")
    add_bullet_point(doc, "Chiến Lược Đánh Chỉ Mục (B-Tree Indexes Optimization): ", 
                     "Để đảm bảo tốc độ phản hồi dưới 100ms khi tìm kiếm giữa hàng triệu bản ghi, hệ thống đã thiết lập các chỉ mục B-Tree "
                     "trên các trường khóa tra cứu trọng yếu: `flight_number`, `departure_date`, `departure_station_id`, `arrival_station_id`, `booking_code`, `status`.")

    add_heading_2(doc, "1.3. Cơ chế Hoạt động Phối hợp Xuyên suốt (End-to-End Collaboration Flow)")
    add_paragraph(doc,
        "Một chu trình xử lý hoàn chỉnh của hệ thống từ lúc người dùng tương tác đến khi dữ liệu được ghi bền vững "
        "tuân theo dòng chảy 6 bước tuần tự sau:")
    
    flow_headers = ["Bước", "Thành phần tham gia", "Giao thức / Cơ chế", "Nội dung xử lý chi tiết"]
    flow_data = [
        ["1", "Browser -> Tomcat", "HTTP/HTTPS (GET / POST)", "Người dùng submit form tìm kiếm hoặc đặt vé. Trình duyệt đóng gói dữ liệu gửi tới cổng 8080 của Embedded Tomcat."],
        ["2", "Tomcat -> Spring Security", "Filter Chain Interception", "Chuỗi bộ lọc kiểm tra xác thực, kiểm tra quyền truy cập Role-Based Access Control, xác thực CSRF token hợp lệ."],
        ["3", "DispatcherServlet -> Controller", "HandlerMapping & DTO Binding", "DispatcherServlet điều phối request tới method tương ứng của Controller; đóng gói tham số vào DTO và thực hiện Bean Validation."],
        ["4", "Controller -> Service Layer", "Java Method Calls", "Controller gọi hàm nghiệp vụ trong Service. Dịch vụ mở giao dịch @Transactional, chiếm giữ in-memory lock tương ứng để chống overbooking."],
        ["5", "Service -> Data Access Layer", "JPA Repository & HikariCP", "Service gọi các Repository; HikariCP cấp kết nối; Hibernate dịch JPQL thành mã SQL Server T-SQL; CSDL thực thi và trả về dữ liệu."],
        ["6", "Service -> Controller -> Client", "PRG Pattern / Thymeleaf SSR", "Service hoàn tất giao dịch (Commit); Controller trả về Model cho Thymeleaf render mã HTML hoặc trả về HTTP 302 Redirect (PRG) về cho trình duyệt."]
    ]
    add_styled_table(doc, flow_headers, flow_data, [Inches(0.6), Inches(2.0), Inches(1.8), Inches(2.1)])

    # =========================================================================
    # PHẦN 2: CÁC CHỨC NĂNG TRỌNG TÂM VÀ MÔ TẢ DÒNG CHẢY CÔNG VIỆC (WORKFLOW SPECIFICATION)
    # =========================================================================
    add_heading_1(doc, "PHẦN 2: CÁC CHỨC NĂNG TRỌNG TÂM VÀ MÔ TẢ DÒNG CHẢY CÔNG VIỆC (WORKFLOW SPECIFICATION)")
    add_paragraph(doc,
        "Nhằm cụ thể hóa cơ chế vận hành của kiến trúc hệ thống đã thiết kế ở Phần 1, phần này tiến hành đặc tả "
        "dòng chảy công việc (Workflow) của 6 chức năng nghiệp vụ trọng tâm nhất trong đồ án 'Web-Du-Lich'. "
        "Mỗi chức năng đều được phân tích theo các khía cạnh: Mục đích & Tác nhân tham gia, Sơ đồ luồng hoạt động, "
        "Bảng mô tả các bước tuần tự (Input - Process - Output - Exception) và các quy tắc nghiệp vụ đặc thù đi kèm.")

    # -------------------------------------------------------------------------
    # 2.1 Chức năng 1: Đặt vé máy bay & giữ chỗ 15 phút
    # -------------------------------------------------------------------------
    add_heading_2(doc, "2.1. Chức năng 1: Quy trình Tìm kiếm, Giữ chỗ và Đặt vé Máy bay (Flight Booking & Hold Workflow)")
    add_paragraph(doc,
        "Chức năng đặt vé máy bay cho phép khách hàng tra cứu các chuyến bay nội địa và quốc tế, lựa chọn hạng vé "
        "(Phổ thông - Economy hoặc Thương gia - Business), nhập danh sách hành khách, thực hiện giữ chỗ có thời hạn 15 phút "
        "và tiến hành thanh toán xuất vé điện tử.")
    
    add_paragraph(doc, "Tác nhân tham gia:", bold_prefix="Tác nhân: ")
    add_bullet_point(doc, "Khách hàng (Client): ", "Thực hiện tìm kiếm, nhập danh sách hành khách và chọn phương thức thanh toán.")
    add_bullet_point(doc, "FlightController: ", "Tiếp nhận form tìm kiếm, hiển thị danh sách chuyến bay, validate thông tin và điều phối giữ chỗ.")
    add_bullet_point(doc, "FlightTicketService: ", "Tính giá vé động, phụ phí hành lý, kiểm soát khóa đồng bộ và trừ số ghế trống.")
    add_bullet_point(doc, "Hệ CSDL SQL Server: ", "Ghi nhận bản ghi `flight_bookings`, `flight_passengers` và `payments`.")

    add_image_with_caption(doc, "workflow_flight_booking.png", 
                           "Hình 2.1: Sơ đồ Dòng chảy Quy trình Đặt vé Máy bay & Giữ chỗ 15 phút", 
                           width_inches=6.5)

    add_paragraph(doc, "Bảng đặc tả chi tiết các bước trong quy trình đặt vé máy bay:", bold_prefix="Đặc tả các bước thực hiện:")
    fl_headers = ["Bước", "Hành động / Tác vụ", "Dữ liệu đầu vào (Input)", "Xử lý nghiệp vụ tại Backend", "Kết quả đầu ra (Output)"]
    fl_data = [
        ["1", "Tìm kiếm chuyến bay", "Nơi đi, Nơi đến, Ngày khởi hành, Số lượng hành khách (`FlightSearchDto`)", 
         "`FlightController` tiếp nhận, gọi `FlightRepository.findAvailableFlights()` lọc theo ngày và sân bay.", 
         "Danh sách chuyến bay phù hợp kèm giá vé gốc và số ghế trống mỗi hạng."],
        ["2", "Lựa chọn hạng vé & chuyến", "Chuyến bay được chọn (ID), Hạng ghế (ECONOMY / BUSINESS)", 
         "Hệ thống kiểm tra số ghế trống còn lại của chuyến bay. Chuyển hướng sang form nhập thông tin hành khách.", 
         "Giao diện form `flight_booking_form.html` tương ứng với số lượng hành khách đã đăng ký."],
        ["3", "Nhập thông tin hành khách & Validate", "Họ tên, Số CCCD / Hộ chiếu, Loại khách (Adult/Child/Infant), Hành lý ký gửi thêm (kg)", 
         "`FlightController` gọi Bean Validation. Kiểm tra regex CCCD 12 số hoặc Hộ chiếu quốc tế; kiểm tra số lượng em bé <= số người lớn; tính phụ phí hành lý 50.000đ/kg và thuế sân bay 120.000đ/người.", 
         "Nếu lỗi: Trả lại form kèm cảnh báo đỏ. Nếu hợp lệ: Tính toán tổng chi phí cuối cùng."],
        ["4", "Khóa ghế & Tạo giữ chỗ (Hold)", "Bấm nút 'Xác nhận giữ chỗ 15 phút'", 
         "Mở giao dịch `@Transactional`. Chiếm giữ khóa `flightLocks.get(flightId)`. Kiểm tra lại số ghế trống lần cuối. Trừ số ghế trong bảng `flights`. Tạo bản ghi `flight_bookings` với trạng thái `HOLD` và thời hạn `holdExpiresAt = now() + 15 phút`.", 
         "Đơn vé được lưu thành công. Giao diện hiển thị trang thông tin đơn vé kèm Đồng hồ đếm ngược (Countdown) 15:00."],
        ["5", "Thanh toán & Xuất vé", "Khách chọn hình thức: Chuyển khoản / VNPAY / Tiền mặt", 
         "Hệ thống kiểm tra thời gian hiện tại <= `holdExpiresAt`. Ghi nhận thanh toán vào bảng `payments`. Cập nhật trạng thái đơn vé sang `CONFIRMED`. Sinh mã PNR điện tử và gọi `EmailService` gửi vé về hòm thư khách hàng.", 
         "Hiển thị hóa đơn xác nhận đặt vé thành công và hướng dẫn làm thủ tục check-in tại sân bay."]
    ]
    add_styled_table(doc, fl_headers, fl_data, [Inches(0.6), Inches(1.5), Inches(1.5), Inches(1.7), Inches(1.2)])

    # -------------------------------------------------------------------------
    # 2.2 Chức năng 2: Sơ đồ ghế toa tàu & Segment Overlap
    # -------------------------------------------------------------------------
    add_heading_2(doc, "2.2. Chức năng 2: Quy trình Chọn chỗ trên Sơ đồ toa tàu và Thuật toán Chặn trùng lấn chặng (Train Seat Map & Segment Overlap Workflow)")
    add_paragraph(doc,
        "Khác biệt hoàn toàn so với vé máy bay vốn bay thẳng từ điểm A đến điểm B, đặc thù cốt lõi của vận tải đường sắt "
        "là một chuyến tàu chạy qua nhiều ga trung gian dọc chiều dài đất nước (ví dụ: Tuyến Thống Nhất Bắc - Nam "
        "chạy qua Hà Nội, Phủ Lý, Nam Định, Ninh Bình, Thanh Hóa, Vinh, Đồng Hới, Huế, Đà Nẵng, Nha Trang, Sài Gòn). "
        "Do đó, một vị trí ghế/giường duy nhất có thể bán cho nhiều khách hàng khác nhau, miễn là các khoảng chặng ga mà họ đi "
        "không giao cắt hoặc chồng lấn lên nhau.")

    add_paragraph(doc, "Thuật toán Kiểm tra Chồng lấn Chặng (Segment Overlap Check Algorithm):", bold_prefix="Cơ sở Toán học của Thuật toán:")
    add_paragraph(doc,
        "Giả sử khách hàng đang yêu cầu đặt chỗ từ Ga xuất phát có chỉ mục $S_{req}$ đến Ga kết thúc có chỉ mục $E_{req}$ ($S_{req} < E_{req}$). "
        "Đối với một vị trí ghế cụ thể trên toa tàu, hệ thống sẽ quét qua toàn bộ các vé đã được phát hành hoặc đang giữ chỗ cho ghế đó "
        "trong cùng ngày chạy tàu. Mỗi vé đã bán có khoảng chặng từ $S_{booked}$ đến $E_{booked}$.")
    add_paragraph(doc,
        "Theo định lý về sự giao cắt khoảng, hai khoảng chặng $[S_{req}, E_{req}]$ và $[S_{booked}, E_{booked}]$ là KHÔNG CHỒNG LẤN KHI VÀ CHỈ KHI:",
        bold_prefix="Điều kiện Ghế Hợp Lệ (Available): ")

    add_callout(doc, [
        "(E_req <= S_booked)   HOẶC   (S_req >= E_booked)",
        "Ý nghĩa thực tiễn: Khách yêu cầu xuống tàu trước hoặc tại đúng ga mà khách trước đó bước lên, HOẶC khách yêu cầu bước lên tàu tại hoặc sau ga mà khách trước đó đã xuống tàu.",
        "Nếu điều kiện trên KHÔNG được thỏa mãn, tức là tồn tại sự chồng lấn: !(E_req <= S_booked || S_req >= E_booked) -> Vị trí ghế đó lập tức bị đánh dấu là ĐÃ BÁN (OCCUPIED) và không cho phép đặt."
    ], title="CÔNG THỨC LOGIC CỐT LÕI (SEGMENT OVERLAP FORMULA)")

    add_image_with_caption(doc, "workflow_train_seat_overlap.png", 
                           "Hình 2.2: Sơ đồ Mô phỏng Thuật toán Kiểm tra Chồng lấn Chặng Tuyến Đường Sắt Bắc - Nam", 
                           width_inches=6.5)

    add_paragraph(doc, "Bảng đặc tả quy trình đặt vé tàu và render sơ đồ ghế động:", bold_prefix="Đặc tả quy trình thực hiện:")
    tr_headers = ["Bước", "Tác vụ", "Dữ liệu tham gia", "Xử lý tại Service & Thuật toán", "Hiển thị tại Giao diện"]
    tr_data = [
        ["1", "Tra cứu tuyến tàu", "Ga khởi hành, Ga đến, Ngày đi (`TrainSearchDto`)", 
         "`TrainTicketService` tìm các chuyến tàu có lộ trình đi qua cả 2 ga và đảm bảo khoảng cách km Ga đến > Ga đi.", 
         "Danh sách các mác tàu (SE1, SE3, SE5...) cùng thời gian xuất phát dự kiến."],
        ["2", "Lựa chọn đoàn tàu & Toa xe", "Mác tàu (Train ID), Loại toa: Ghế mềm điều hòa / Giường nằm khoang 6 / Giường VIP khoang 4", 
         "Hệ thống tải danh sách các ghế thuộc toa xe đó từ bảng `train_seats`.", 
         "Giao diện hiển thị thanh lựa chọn toa xe và thông tin cước phí theo từng loại chỗ."],
        ["3", "Render Sơ đồ ghế động (Dynamic Seat Map)", "Mã Toa (Coach ID), Khoảng ga [S_req, E_req]", 
         "Chạy thuật toán `Segment Overlap Check` cho từng ghế trên toa đối chiếu với bảng `train_tickets`. Đánh dấu trạng thái `AVAILABLE`, `OCCUPIED` hoặc `HOLDING`.", 
         "Sơ đồ trực quan toa xe hiển thị với màu sắc tương ứng. Ghế đã bán bị khóa không cho click; ghế trống hiển thị xanh cho phép chọn."],
        ["4", "Chọn ghế & Đối tượng ưu tiên", "Click các vị trí ghế mong muốn, chọn diện ưu tiên (Sinh viên / Người cao tuổi / Trẻ em)", 
         "Tính giá vé tự động: `Giá = Khoảng cách (km) * Đơn giá toa * (1 - Tỉ lệ giảm ưu tiên) + Bảo hiểm 10.000đ`. Áp dụng giảm 10% (SV), 15% (NCT), 25% (Trẻ em).", 
         "Bảng tóm tắt thông tin các vé đang chọn và tổng số tiền thanh toán được cập nhật theo thời gian thực."],
        ["5", "Khóa ghế & Giữ chỗ 10 phút", "Nhập họ tên, CCCD hành khách và bấm 'Giữ chỗ'", 
         "Mở `@Transactional`, chiếm giữ khóa `trainLocks.get(trainId)`. Lưu bản ghi `train_bookings` trạng thái `HOLD` và tạo các `train_tickets` tạm thời với hạn giữ chỗ 10 phút.", 
         "Chuyển sang trang thanh toán vé tàu kèm đồng hồ đếm ngược 10:00. Trên sơ đồ ghế chung của khách khác, các ghế này đổi sang màu VÀNG."],
        ["6", "Xác nhận thanh toán & Phát hành vé", "Hoàn tất chuyển khoản hoặc thanh toán tại quầy", 
         "Cập nhật trạng thái `train_bookings` sang `CONFIRMED`. Cấp mã số vé tàu điện tử chính thức.", 
         "Khách hàng nhận được thẻ lên tàu hỏa điện tử có chứa thông tin Toa, Số ghế, Lộ trình ga và mã vạch tra cứu."]
    ]
    add_styled_table(doc, tr_headers, tr_data, [Inches(0.6), Inches(1.5), Inches(1.5), Inches(1.7), Inches(1.2)])

    # -------------------------------------------------------------------------
    # 2.3 Chức năng 3: Đổi chuyến bay
    # -------------------------------------------------------------------------
    add_heading_2(doc, "2.3. Chức năng 3: Quy trình Đổi chuyến bay và Xử lý chênh lệch giá (Flight Change Workflow)")
    add_paragraph(doc,
        "Trong hoạt động hàng không, nhu cầu thay đổi lịch trình bay vì lý do đột xuất của hành khách diễn ra rất thường xuyên. "
        "Chức năng Đổi chuyến bay cung cấp cơ chế an toàn, tự động tính toán chi phí phát sinh và hoán đổi chỗ ngồi giữa 2 chuyến bay.")

    add_paragraph(doc, "Quy tắc nghiệp vụ đổi chuyến bay:", bold_prefix="Chính sách & Công thức tính phí đổi:")
    add_bullet_point(doc, "Điều kiện hợp lệ: ", "Đơn vé phải ở trạng thái `CONFIRMED`. Chuyến bay cũ chưa cất cánh và còn cách giờ khởi hành tối thiểu quy định (thường là trước 24 giờ).")
    add_bullet_point(doc, "Công thức chi phí đổi vé: ", 
                     "Tổng chi phí phát sinh = (Phí đổi cố định 350.000 VNĐ * Số lượng hành khách) + Tiền chênh lệch giá vé. "
                     "Trong đó: Tiền chênh lệch = Giá vé chuyến mới - Giá vé chuyến cũ. Nếu giá chuyến mới cao hơn, khách hàng phải thanh toán thêm khoản chênh lệch này; "
                     "nếu giá chuyến mới thấp hơn giá chuyến cũ, hệ thống giữ nguyên mức giá cũ (không hoàn tiền thừa) theo chuẩn quy định hàng không quốc tế.")

    add_paragraph(doc, "Dòng chảy công việc tuần tự:", bold_prefix="Quy trình 5 bước thực hiện:")
    add_bullet_point(doc, "Bước 1 - Yêu cầu đổi chuyến: ", "Khách hàng tra cứu đơn vé tại `/flights/booking/{code}` và nhấn nút 'Yêu cầu đổi chuyến bay'.")
    add_bullet_point(doc, "Bước 2 - Tìm chuyến bay mới: ", "Hệ thống hiển thị danh sách các chuyến bay thay thế còn chỗ trống cùng chặng hoặc chặng khác vào ngày mới.")
    add_bullet_point(doc, "Bước 3 - Tính toán chi phí: ", "Khách hàng chọn chuyến mới. `FlightTicketService.calculateChangeFee()` tự động tính toán chính xác tổng chi phí đổi gồm phí cố định và chênh lệch hạng vé.")
    add_bullet_point(doc, "Bước 4 - Thanh toán khoản phụ thu: ", "Khách hàng xác nhận biểu phí và hoàn tất thanh toán khoản tiền bổ sung.")
    add_bullet_point(doc, "Bước 5 - Hoán đổi giao dịch nguyên tử: ", 
                     "Khối `@Transactional` thực thi đồng thời: (1) Hoàn trả số ghế trống của chuyến bay cũ (`flightOld.availableSeats += paxCount`); "
                     "(2) Trừ số ghế trống của chuyến bay mới (`flightNew.availableSeats -= paxCount`); "
                     "(3) Cập nhật mã chuyến bay mới và tổng tiền mới vào đơn `flight_bookings`; "
                     "(4) Ghi nhận hóa đơn thanh toán phụ thu vào `payments`; (5) Gửi email thông báo vé mới.")

    # -------------------------------------------------------------------------
    # 2.4 Chức năng 4: Đăng ký tư vấn du lịch (PRG Pattern)
    # -------------------------------------------------------------------------
    add_heading_2(doc, "2.4. Chức năng 4: Quy trình Đăng ký Lịch hẹn Tư vấn Du lịch theo Chuẩn PRG Pattern (Consultation Booking Workflow)")
    add_paragraph(doc,
        "Đối với các đoàn khách du lịch gia đình, tour cơ quan hoặc những khách hàng chưa có kinh nghiệm đặt vé tàu/máy bay, "
        "hệ thống cung cấp tính năng Đăng ký lịch hẹn tư vấn. Chuyên viên phòng vé sẽ chủ động liên hệ hỗ trợ giải đáp thắc mắc "
        "và thiết kế lộ trình trước khi khách hàng tiến hành chốt vé.")

    add_paragraph(doc, "Áp dụng Mô hình Post/Redirect/Get (PRG Pattern):", bold_prefix="Kiến trúc Triển khai PRG Pattern:")
    add_paragraph(doc,
        "Trong các hệ thống thương mại điện tử, việc người dùng sau khi submit form rồi vô tình bấm phím F5 (Refresh) hoặc nút Back trên trình duyệt "
        "thường gây ra sự cố nghiêm trọng là gửi lại request POST (Double Submission), dẫn đến việc trùng lặp đơn hàng hoặc lịch hẹn trong cơ sở dữ liệu. "
        "Hệ thống Web-Du-Lich giải quyết triệt để vấn đề này bằng mô hình PRG chuẩn mực:")

    add_image_with_caption(doc, "workflow_consultation_prg.png", 
                           "Hình 2.3: Mô hình Tuần tự Post/Redirect/Get (PRG) trong Đăng ký Lịch hẹn Tư vấn", 
                           width_inches=6.5)

    add_paragraph(doc, "Chi tiết các pha trong mô hình PRG:", bold_prefix="Các pha xử lý chi tiết:")
    add_bullet_point(doc, "Pha 1 - Form Display (GET): ", 
                     "Người dùng truy cập `/consultations/new`. Controller khởi tạo đối tượng `Consultation` rỗng với ngày hẹn mặc định "
                     "là 09:00 ngày làm việc tiếp theo (tự động bỏ qua ngày Chủ nhật) và render view `consultation_form.html`.")
    add_bullet_point(doc, "Pha 2 - Form Submission (POST): ", 
                     "Người dùng điền thông tin và submit form lên endpoint `POST /consultations`. Controller thực hiện `@Valid`. "
                     "Nếu có lỗi validation (SĐT sai định dạng, ngày hẹn trong quá khứ), trả lại ngay form kèm thông báo lỗi. "
                     "Nếu hợp lệ, Controller gọi `ConsultationService.createConsultation()` lưu bản ghi vào CSDL ở trạng thái `PENDING`.")
    add_bullet_point(doc, "Pha 3 - Redirect Response (HTTP 302): ", 
                     "Thay vì trả về view HTML trực tiếp, Controller lập tức gửi về trình duyệt mã phản hồi HTTP 302 Redirect "
                     "tới địa chỉ `redirect:/consultations/success?code={consultationCode}`.")
    add_bullet_point(doc, "Pha 4 - Success Page (GET): ", 
                     "Trình duyệt nhận mã 302 sẽ tự động kích hoạt một request `GET /consultations/success`. Trang thành công hiển thị mã lịch hẹn "
                     "và lời cảm ơn. Lúc này, dù người dùng có nhấn F5 bao nhiêu lần, trình duyệt cũng chỉ gửi lại lệnh GET an toàn (Idempotent), "
                     "hoàn toàn không làm phát sinh thêm bất kỳ bản ghi mới nào.")
    add_bullet_point(doc, "Pha 5 - Tiếp nhận & Xử lý (Nhân viên): ", 
                     "Nhân viên phòng vé (`ROLE_NHANVIEN`) đăng nhập hệ thống, truy cập màn hình `/consultations` để xem danh sách lịch hẹn cần xử lý, "
                     "gọi điện thoại cho khách theo đúng khung giờ đã đăng ký và cập nhật trạng thái lịch hẹn sang `CONTACTED` hoặc `COMPLETED`.")

    # -------------------------------------------------------------------------
    # 2.5 Chức năng 5: Scheduler Giải phóng ghế giữ chỗ
    # -------------------------------------------------------------------------
    add_heading_2(doc, "2.5. Chức năng 5: Tác vụ Ngầm Tự động Thu hồi Ghế Giữ chỗ Quá hạn (Hold Seat Expiration Scheduler Workflow)")
    add_paragraph(doc,
        "Nhằm bảo vệ lợi ích kinh doanh của doanh nghiệp và tạo sự công bằng cho tất cả khách hàng, hệ thống thiết lập cơ chế "
        "giữ chỗ có thời hạn (15 phút đối với Vé máy bay, 10 phút đối với Vé tàu hỏa). Nếu sau khoảng thời gian này khách hàng không hoàn tất "
        "thanh toán, hệ thống phải tự động thu hồi lại số ghế đó để mở bán cho những khách hàng khác có nhu cầu.")

    add_paragraph(doc, "Cơ chế Vận hành của Bộ Lập lịch (Scheduler Architecture):", bold_prefix="Cơ chế vận hành:")
    add_bullet_point(doc, "Cấu hình Lập lịch: ", "Bật tính năng lập lịch toàn hệ thống qua `@EnableScheduling` tại `ScheduleConfig.java`. Lớp `BookingHoldExpirationScheduler` được cấu hình phương thức `@Scheduled(fixedDelay = 60000)` để chạy ngầm liên tục mỗi 1 phút một lần.")
    add_bullet_point(doc, "Quét Dữ liệu Quá hạn: ", 
                     "Tại mỗi chu kỳ, Scheduler truy vấn hai nguồn dữ liệu độc lập: (1) `flightBookingRepository.findByStatusAndCreatedAtBefore(FlightBookingStatus.HOLD, now - 15 phút)`; "
                     "(2) `trainBookingRepository.findByStatusAndCreatedAtBefore(TrainBookingStatus.HOLD, now - 10 phút)`.")
    add_bullet_point(doc, "Xử lý Thu hồi Nguyên tử: ", 
                     "Với mỗi đơn vé máy bay quá hạn, Service cộng trả lại số ghế trống tương ứng vào chuyến bay (`flight.availableEconomySeats += holdQty`) "
                     "và chuyển trạng thái đơn sang `EXPIRED`. Với mỗi đơn vé tàu hỏa quá hạn, Service chuyển trạng thái các vé giữ chỗ sang `EXPIRED`, "
                     "lập tức giải phóng các vị trí ghế trên sơ đồ toa tàu.")
    add_bullet_point(doc, "Ghi Log & Đảm bảo An toàn: ", 
                     "Toàn bộ tiến trình thu hồi đều được ghi log chi tiết phục vụ kiểm toán (Auditing) và được bọc trong giao dịch riêng biệt, "
                     "đảm bảo không gây ảnh hưởng hay khóa nghẽn tới các phiên giao dịch mua vé đang diễn ra bình thường của những khách hàng khác.")

    # -------------------------------------------------------------------------
    # 2.6 Chức năng 6: Quản trị nghiệp vụ & Dashboard Analytics
    # -------------------------------------------------------------------------
    add_heading_2(doc, "2.6. Chức năng 6: Quản trị Nghiệp vụ, Phê duyệt Đơn vé và Dashboard Phân tích Doanh thu (Admin Management & Analytics Workflow)")
    add_paragraph(doc,
        "Phân hệ quản trị cung cấp bức tranh toàn cảnh về hoạt động kinh doanh vận tải và trao quyền cho Quản trị viên (`ROLE_ADMIN`) "
        "cùng Nhân viên phòng vé (`ROLE_NHANVIEN`) điều hành toàn bộ dữ liệu cấu hình và dòng tiền của hệ thống.")

    add_paragraph(doc, "Các quy trình nghiệp vụ chính trong phân hệ Quản trị:", bold_prefix="Các nghiệp vụ trọng tâm:")
    add_bullet_point(doc, "Dashboard Thống kê Doanh thu & Tỷ lệ Lấp đầy: ", 
                     "Màn hình `/admin` tổng hợp dữ liệu từ cả 2 phân hệ máy bay và tàu hỏa thông qua đối tượng `AdminStatisticsDto`: "
                     "Tổng doanh thu thực tế theo ngày/tháng/loại vé; tổng số vé đã phát hành thành công; tổng số đơn đang giữ chỗ; "
                     "số lượng lịch hẹn tư vấn mới cần xử lý; và biểu đồ tỷ lệ lấp đầy ghế (Seat Occupancy Rate) của từng chuyến bay/chuyến tàu "
                     "giúp ban quản lý kịp thời điều chỉnh kế hoạch mở thêm chuyến.")
    add_bullet_point(doc, "Quản trị Danh mục Vận hành (CRUD Operations): ", 
                     "Admin thực hiện thêm mới, chỉnh sửa thông tin hoặc tạm dừng khai thác đối với: Danh sách chuyến bay, sân bay cất/hạ cánh, "
                     "lịch chạy tàu, cấu hình toa xe (ghế mềm, giường nằm khoang 4/khoang 6) và danh mục các ga đón trả khách.")
    add_bullet_point(doc, "Phê duyệt Xuất vé & Quản lý Thanh toán: ", 
                     "Đối với các đơn đặt vé chọn phương thức thanh toán tiền mặt tại quầy hoặc chuyển khoản ngân hàng chờ đối soát, "
                     "nhân viên phòng vé kiểm tra biên lai và bấm nút 'Xác nhận thanh toán' trên giao diện quản trị. Hệ thống ngay lập tức chuyển trạng thái đơn sang `CONFIRMED` "
                     "và phát hành vé điện tử chính thức cho khách hàng.")
    add_bullet_point(doc, "Quản lý Người dùng & Phân quyền RBAC: ", 
                     "Admin quản lý danh sách tài khoản, kích hoạt hoặc vô hiệu hóa tài khoản, thiết lập lại mật khẩu và cấp phát vai trò phù hợp "
                     "(`ROLE_KHACH`, `ROLE_NHANVIEN`, `ROLE_ADMIN`).")

    # =========================================================================
    # PHẦN 3: ĐÁNH GIÁ KIẾN TRÚC & ĐỊNH HƯỚNG MỞ RỘNG
    # =========================================================================
    add_heading_1(doc, "PHẦN 3: ĐÁNH GIÁ KIẾN TRÚC VÀ ĐỊNH HƯỚNG PHÁT TRIỂN")
    
    add_paragraph(doc, "Đánh giá các tiêu chuẩn kiến trúc phần mềm đạt được:", bold_prefix="Ưu điểm kiến trúc:")
    add_bullet_point(doc, "Tính Độc Lập & Dễ Bảo Trì (Maintainability): ", 
                     "Mô hình phân tầng tách biệt hoàn toàn giữa UI (Thymeleaf), Điều khiển (Spring MVC), Nghiệp vụ (Spring Service) "
                     "và Dữ liệu (Spring Data JPA). Khi có sự thay đổi về giao diện hiển thị hoặc thay đổi hệ CSDL, các tầng còn lại hoàn toàn không bị ảnh hưởng.")
    add_bullet_point(doc, "Tính Toàn Vẹn & Khả Năng Chịu Tải (Reliability & Scalability): ", 
                     "Sự kết hợp giữa HikariCP Connection Pool, cơ chế khóa chống bán vượt chỗ (In-Memory Locking) và tính nguyên tử `@Transactional` "
                     "giúp hệ thống vận hành ổn định, tin cậy dưới áp lực đặt vé cao điểm mùa du lịch/lễ tết.")
    add_bullet_point(doc, "Bảo Mật Cao Cấp (Security): ", 
                     "Tuân thủ nghiêm ngặt chuẩn bảo mật Spring Security 6 với phân quyền RBAC đa tầng, mã hóa mật khẩu BCrypt, "
                     "bảo vệ CSRF và cơ chế kiểm soát quyền riêng tư đơn vé theo SĐT.")

    add_paragraph(doc, "Định hướng nâng cấp và mở rộng trong các giai đoạn tiếp theo:", bold_prefix="Định hướng nâng cấp:")
    add_bullet_point(doc, "Tích hợp Cổng Thanh toán Trực tuyến (Payment Gateway): ", 
                     "Tích hợp kết nối API cổng thanh toán VNPAY, MoMo, ZaloPay qua cơ chế Webhook/IPN để tự động hóa 100% quy trình xác nhận giao dịch tài chính.")
    add_bullet_point(doc, "Bộ nhớ Đệm Hiệu năng Cao (Redis Caching): ", 
                     "Bổ sung tầng Redis Cache phía trước CSDL để lưu trữ đệm các kết quả tìm kiếm chuyến bay, sơ đồ tàu và danh mục ga tĩnh, "
                     "giảm tải tối đa cho Microsoft SQL Server.")
    add_bullet_point(doc, "Hàng đợi Bất đồng bộ (Message Queue): ", 
                     "Tích hợp RabbitMQ hoặc Apache Kafka để xử lý bất đồng bộ các tác vụ gửi email thông báo vé và xuất hóa đơn điện tử VAT.")

    # =========================================================================
    # PHẦN 4: TÀI LIỆU THAM KHẢO & PHỤ LỤC
    # =========================================================================
    add_heading_1(doc, "PHẦN 4: PHỤ LỤC THIẾT KẾ CƠ SỞ DỮ LIỆU & TÀI LIỆU THAM KHẢO")
    
    add_paragraph(doc, "Danh mục các bảng vật lý cốt lõi trong cơ sở dữ liệu `DatVeDuLich` (Microsoft SQL Server):", bold_prefix="Phụ lục CSDL:")
    db_headers = ["Tên Bảng", "Chức năng lưu trữ chính", "Khóa chính (PK)", "Khóa ngoại liên kết (FK)"]
    db_data = [
        ["users", "Tài khoản đăng nhập hệ thống và phân quyền", "id", "Không có"],
        ["customers", "Hồ sơ thông tin khách hàng (Chuẩn 3NF)", "id", "user_id -> users(id)"],
        ["flights", "Thông tin chuyến bay, hãng bay, giá vé, số ghế trống", "id", "Không có"],
        ["flight_bookings", "Đơn đặt vé máy bay, trạng thái (HOLD, CONFIRMED...)", "id", "flight_id -> flights(id), customer_id -> customers(id)"],
        ["flight_passengers", "Danh sách hành khách bay, số CCCD/Passport, hành lý", "id", "booking_id -> flight_bookings(id)"],
        ["trains", "Danh mục đoàn tàu hỏa (SE1, SE3...)", "id", "Không có"],
        ["train_stations", "Danh mục các ga tàu và khoảng cách km tính từ Ga Hà Nội", "id", "Không có"],
        ["train_coaches", "Danh mục toa xe của từng đoàn tàu", "id", "train_id -> trains(id)"],
        ["train_seats", "Sơ đồ chi tiết vị trí các ghế/giường trên toa xe", "id", "coach_id -> train_coaches(id)"],
        ["train_bookings", "Đơn đặt vé tàu hỏa, tổng cước và trạng thái", "id", "train_id -> trains(id), customer_id -> customers(id)"],
        ["train_tickets", "Chi tiết từng vé tàu theo chặng ga và đối tượng ưu tiên", "id", "booking_id -> train_bookings(id), seat_id -> train_seats(id)"],
        ["payments", "Lịch sử giao dịch tài chính, phương thức và mã GD", "id", "booking_id -> flight_bookings / train_bookings"],
        ["consultations", "Yêu cầu đăng ký lịch hẹn tư vấn chuyến đi", "id", "customer_id -> customers(id)"]
    ]
    add_styled_table(doc, db_headers, db_data, [Inches(1.5), Inches(2.6), Inches(0.8), Inches(1.8)])

    add_paragraph(doc, "Tài liệu tham khảo:", bold_prefix="Tài liệu tham khảo chính:")
    add_bullet_point(doc, "1. ", "Spring Boot Reference Documentation (v3.2.5) - VMware Tanzu & Spring Team.")
    add_bullet_point(doc, "2. ", "Spring Security Architecture & Role-Based Access Control Specification (v6.x).")
    add_bullet_point(doc, "3. ", "Thymeleaf Template Engine - Documentation and Best Practices for Responsive Web Applications.")
    add_bullet_point(doc, "4. ", "Enterprise Integration Patterns: Designing, Building, and Deploying Messaging Solutions - Gregor Hohpe & Bobby Woolf.")
    add_bullet_point(doc, "5. ", "Microsoft SQL Server Transact-SQL Architecture & Query Optimization Guide.")

    # Save Document
    output_filename = "Bao_Cao_Kien_Truc_He_Thong_Va_Quy_Trinh_Nghiep_Vu_Web_Du_Lich.docx"
    doc.save(output_filename)
    print(f"Báo cáo hoàn chỉnh đã được lưu thành công vào file: {output_filename}")

if __name__ == '__main__':
    create_document()
