import matplotlib.pyplot as plt
import matplotlib.patches as patches

def draw_system_architecture(output_path="system_architecture.png"):
    fig = plt.figure(figsize=(16, 12), dpi=250)
    ax = fig.add_subplot(111)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    # Font setup
    plt.rcParams['font.sans-serif'] = ['Segoe UI', 'Arial', 'Tahoma']
    
    # Background
    fig.patch.set_facecolor('#F8FAFC')
    
    # Title Banner
    ax.text(50, 97, "MULTI-TIER SYSTEM ARCHITECTURE", 
            ha='center', va='center', fontsize=20, fontweight='bold', color='#0F172A', family='Segoe UI')
    ax.text(50, 94.2, "Web-Du-Lich: Flight & Train Ticket Management Platform Overview", 
            ha='center', va='center', fontsize=12, style='italic', color='#475569', family='Segoe UI')

    # Color Palette
    border_color = '#1E293B'
    tier_bg = '#FFFFFF'
    header_bg = '#F1F5F9'
    accent_blue = '#0284C7'
    accent_dark = '#0F172A'
    subtext_color = '#334155'
    tag_bg = '#E0F2FE'
    tag_border = '#38BDF8'

    tiers_config = [
        {
            "id": 1,
            "title": "1. PRESENTATION TIER (Client, Staff & Admin Web Interfaces)",
            "y": 75,
            "height": 16.5,
            "left_title": "Actors & User Interfaces:",
            "left_lines": [
                "• End-User (Customer / Guest): Tra cứu chuyến bay & tàu hỏa, xem sơ đồ ghế/toa tàu trực quan,",
                "  giữ chỗ tạm thời (15p/10p), thanh toán vé, tra cứu đơn bằng SĐT/Mã đơn, đăng ký tư vấn.",
                "• Ticket Officer (Staff - ROLE_NHANVIEN): Tiếp nhận danh sách tư vấn, hỗ trợ xuất vé điện tử,",
                "  xử lý yêu cầu đổi chuyến bay, xác nhận thanh toán tiền mặt/chuyển khoản và hoàn hủy vé.",
                "• System Admin (ROLE_ADMIN): Cấu hình chuyến bay/tàu/ga, quản lý tài khoản người dùng,",
                "  phân quyền RBAC, giám sát báo cáo doanh thu & tỷ lệ lấp đầy ghế (Seat Occupancy Rate)."
            ],
            "right_title": "Technologies & Frontend Stack:",
            "right_lines": [
                "• Server-Side Rendering: Thymeleaf Template Engine",
                "• Styling & UI: HTML5, CSS3, Bootstrap 5.3",
                "• Visual Seat Map: Dynamic SVG & Responsive Grid",
                "• Client Scripting: Vanilla JS (Countdown Hold Timer,",
                "  AJAX validation, dynamic seat selection state)",
                "• Design Pattern: Post/Redirect/Get (PRG) Pattern"
            ],
            "arrow_label": "HTTP Requests / Responses (RESTful, Form POST, PRG Pattern)"
        },
        {
            "id": 2,
            "title": "2. APPLICATION CONTROLLER TIER (Spring Web MVC & Spring Security)",
            "y": 52,
            "height": 16.5,
            "left_title": "Core Infrastructure & Security:",
            "left_lines": [
                "• Web Server: Embedded Apache Tomcat Server (Port 8080, Multi-threaded IO)",
                "• Front Controller: DispatcherServlet (Điều phối toàn bộ HTTP Requests, HandlerMapping & ViewResolver)",
                "• Spring Security 6 RBAC: Phân quyền theo vai trò (ROLE_KHACH, ROLE_NHANVIEN, ROLE_ADMIN)",
                "• Protection Mechanisms: CSRF Protection tokens, BCryptPasswordEncoder (10 rounds),",
                "  Session Fixation Protection, Phone & Booking Code authorization checks",
                "• Exception Handling: @ControllerAdvice (GlobalExceptionHandler) bắt Business & Validation errors"
            ],
            "right_title": "Key MVC Controllers:",
            "right_lines": [
                "• FlightController: Tìm chuyến, chọn hạng vé, submit DTO,",
                "  giữ chỗ 15p, đổi chuyến bay (/flights/*)",
                "• TrainController: Lịch tàu, chọn toa xe, render sơ đồ ghế,",
                "  giữ chỗ 10p theo chặng (/trains/*)",
                "• ConsultationController: Đăng ký tư vấn PRG (/consultations)",
                "• AdminController: Quản trị bay/tàu/ga, user, duyệt vé (/admin/*)",
                "• CustomerController & AuthController: Tra cứu vé & Auth"
            ],
            "arrow_label": "Java Method Calls (Data Transfer Objects - DTOs & Domain Models)"
        },
        {
            "id": 3,
            "title": "3. BUSINESS LOGIC & SERVICE TIER (Core Travel Ticket Engine)",
            "y": 29,
            "height": 16.5,
            "left_title": "Transaction, Concurrency & Schedulers:",
            "left_lines": [
                "• Transaction Management: Atomicity qua @Transactional(isolation = REPEATABLE_READ)",
                "  đảm bảo trừ ghế, tạo booking và lưu chi tiết vé diễn ra nguyên tử, tự rollback nếu lỗi.",
                "• Concurrency Control: ConcurrentHashMap in-memory locks (flightLocks, trainLocks)",
                "  kết hợp Pessimistic Locking chống Race Condition và hiện tượng bán vượt chỗ (Overbooking).",
                "• Background Schedulers: BookingHoldExpirationScheduler (@Scheduled mỗi 60s)",
                "  tự động quét và giải phóng các ghế giữ chỗ quá hạn (15 phút máy bay, 10 phút tàu hỏa).",
                "• Notification Service: EmailService tự động gửi email thông báo vé điện tử và mã PNR."
            ],
            "right_title": "Domain Strategy & Business Services:",
            "right_lines": [
                "• Design Pattern: Strategy Pattern & Factory Pattern",
                "  cho 2 loại vé chuyên biệt (TicketServiceFactory):",
                "  1. FlightTicketService: Định giá động, thuế sân bay",
                "     120k/pax, hành lý quá cước 50k/kg, phí đổi 350k,",
                "     chính sách hủy vé (>24h phí 10%, 4-24h phí 30%).",
                "  2. TrainTicketService: Thuật toán Segment Overlap Check",
                "     cho phép bán ghế theo từng chặng, tính giá theo cự ly",
                "     km * đơn giá loại toa, ưu đãi SV (10%), Trẻ em (25%).",
                "  3. BookingService & ConsultationService: Quản lý vòng đời."
            ],
            "arrow_label": "Spring Data JPA / Hibernate (ORM & JPQL Queries)"
        },
        {
            "id": 4,
            "title": "4. DATA ACCESS & PERSISTENCE TIER (SQL Database Engine)",
            "y": 6,
            "height": 16.5,
            "left_title": "Connection Pool & Repository Layer:",
            "left_lines": [
                "• Connection Pool: HikariCP Connection Pooling (Cấu hình tối ưu độ trễ thấp, giữ kết nối ổn định)",
                "• Spring Data JPA Repositories: Tự động sinh CRUD và tối ưu truy vấn liên bảng phức tạp:",
                "  FlightRepository, TrainRepository, TrainStationRepository, TrainCoachRepository, TrainSeatRepository,",
                "  TrainTicketRepository, FlightBookingRepository, CustomerRepository, UserRepository, ConsultationRepository.",
                "• JPQL & Derived Queries: Tìm kiếm chuyến bay theo ngày/sân bay, kiểm tra giao cắt chặng vé tàu."
            ],
            "right_title": "Database Engine & Physical Schema:",
            "right_lines": [
                "• Engine: Microsoft SQL Server (Transact-SQL)",
                "• Integrity: Chuẩn ACID tuyệt đối, khóa ngoại liên kết",
                "• Core Tables: users, customers, bookings, ticket_details,",
                "  flights, flight_bookings, flight_passengers,",
                "  trains, train_coaches, train_seats, train_stations,",
                "  train_bookings, train_tickets, payments, consultations",
                "• B-Tree Indexes: flight_number, departure_date,",
                "  departure_station_id, arrival_station_id, booking_code"
            ],
            "arrow_label": None
        }
    ]

    for tier in tiers_config:
        y_pos = tier["y"]
        h = tier["height"]

        # Main Tier Box
        rect = patches.FancyBboxPatch((3, y_pos), 94, h,
                                      boxstyle="round,pad=0.2,rounding_size=0.8",
                                      linewidth=1.8, edgecolor='#1E293B', facecolor='#FFFFFF', zorder=2)
        ax.add_patch(rect)

        # Header Box of Tier
        header_rect = patches.FancyBboxPatch((3, y_pos + h - 2.8), 94, 2.8,
                                             boxstyle="round,pad=0.2,rounding_size=0.4",
                                             linewidth=1.2, edgecolor='#1E293B', facecolor='#E2E8F0', zorder=3)
        ax.add_patch(header_rect)
        ax.text(4.5, y_pos + h - 1.5, tier["title"],
                ha='left', va='center', fontsize=12, fontweight='bold', color='#0F172A', family='Segoe UI', zorder=4)

        # Inner Divider Line (Split Left and Right)
        ax.plot([58, 58], [y_pos + 0.6, y_pos + h - 3.2], color='#CBD5E1', linestyle='--', linewidth=1.2, zorder=3)

        # Left Column Title & Content
        ax.text(4.5, y_pos + h - 4.0, tier["left_title"],
                ha='left', va='center', fontsize=10, fontweight='bold', color='#1E40AF', family='Segoe UI', zorder=4)
        
        curr_y = y_pos + h - 5.5
        for line in tier["left_lines"]:
            ax.text(4.5, curr_y, line,
                    ha='left', va='top', fontsize=8.6, color='#1E293B', family='Segoe UI', zorder=4)
            curr_y -= 1.8

        # Right Column Title & Content
        ax.text(59.5, y_pos + h - 4.0, tier["right_title"],
                ha='left', va='center', fontsize=10, fontweight='bold', color='#0369A1', family='Segoe UI', zorder=4)
        
        curr_y_r = y_pos + h - 5.5
        for line in tier["right_lines"]:
            ax.text(59.5, curr_y_r, line,
                    ha='left', va='top', fontsize=8.6, color='#1E293B', family='Segoe UI', zorder=4)
            curr_y_r -= 1.8

        # Arrow down to next tier
        if tier["arrow_label"]:
            arrow_y_start = y_pos
            arrow_y_end = y_pos - 3.8
            # Draw arrow line
            ax.annotate('', xy=(50, arrow_y_end), xytext=(50, arrow_y_start),
                        arrowprops=dict(facecolor='#0F172A', edgecolor='#0F172A', width=2, headwidth=8, headlength=7),
                        zorder=5)
            # Arrow text label
            ax.text(50, y_pos - 2.0, f"  {tier['arrow_label']}  ",
                    ha='center', va='center', fontsize=9, fontweight='bold', color='#0F172A',
                    bbox=dict(boxstyle="round,pad=0.3", fc="#FEF3C7", ec="#F59E0B", lw=1.2),
                    family='Segoe UI', zorder=6)

    plt.tight_layout()
    plt.savefig(output_path, dpi=300, bbox_inches='tight')
    plt.close()
    print(f"Diagram saved to {output_path}")

if __name__ == '__main__':
    draw_system_architecture()
