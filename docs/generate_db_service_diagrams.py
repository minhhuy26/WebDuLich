import matplotlib.pyplot as plt
import matplotlib.patches as patches
import os

def draw_erd_diagram(output_path="docs/erd_database_diagram.png"):
    fig = plt.figure(figsize=(18, 12), dpi=250)
    ax = fig.add_subplot(111)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    plt.rcParams['font.sans-serif'] = ['Segoe UI', 'Arial', 'Tahoma']
    fig.patch.set_facecolor('#F8FAFC')

    # Title Banner
    ax.text(50, 97.2, "SƠ ĐỒ THỰC THỂ LIÊN KẾT & LƯỢC ĐỒ QUAN HỆ CSDL (ERD)", 
            ha='center', va='center', fontsize=18, fontweight='bold', color='#0F172A', family='Segoe UI')
    ax.text(50, 94.6, "Hệ Thống Quản Lý Đặt Vé Du Lịch (Web-Du-Lich) — Chuẩn 5 Bảng Cốt Lõi 3NF (MS SQL Server)", 
            ha='center', va='center', fontsize=11, style='italic', color='#475569', family='Segoe UI')

    def draw_entity_table(x, y, w, h, table_name, pk_list, fk_list, attr_list, header_color="#1E3A8A"):
        # Header Box
        header_h = 3.2
        header_rect = patches.FancyBboxPatch((x, y + h - header_h), w, header_h,
                                             boxstyle="square,pad=0",
                                             facecolor=header_color, edgecolor='#0F172A', linewidth=1.2)
        ax.add_patch(header_rect)
        ax.text(x + w / 2, y + h - header_h / 2, f"TABLE: {table_name}",
                ha='center', va='center', fontsize=10.5, fontweight='bold', color='#FFFFFF')

        # Body Box
        body_rect = patches.FancyBboxPatch((x, y), w, h - header_h,
                                           boxstyle="square,pad=0",
                                           facecolor='#FFFFFF', edgecolor='#0F172A', linewidth=1.2)
        ax.add_patch(body_rect)

        # Content
        cur_y = y + h - header_h - 1.2
        line_spacing = 1.35

        # Primary Keys
        for col, col_type in pk_list:
            ax.text(x + 0.6, cur_y, "[PK]", fontsize=8.2, fontweight='bold', color='#B91C1C')
            ax.text(x + 3.2, cur_y, col, fontsize=8.5, fontweight='bold', color='#0F172A')
            ax.text(x + w - 0.6, cur_y, col_type, ha='right', fontsize=8.0, style='italic', color='#64748B')
            cur_y -= line_spacing

        # Foreign Keys
        for col, col_type, ref in fk_list:
            ax.text(x + 0.6, cur_y, "[FK]", fontsize=8.2, fontweight='bold', color='#2563EB')
            ax.text(x + 3.2, cur_y, col, fontsize=8.5, fontweight='bold', color='#1E40AF')
            ax.text(x + w - 0.6, cur_y, col_type, ha='right', fontsize=8.0, style='italic', color='#64748B')
            cur_y -= line_spacing

        # Thin separator line
        if pk_list or fk_list:
            ax.plot([x + 0.4, x + w - 0.4], [cur_y + 0.5, cur_y + 0.5], color='#E2E8F0', linewidth=1)
            cur_y -= 0.5

        # Normal Attributes
        for col, col_type, extra in attr_list:
            ax.text(x + 0.8, cur_y, col, fontsize=8.2, color='#1E293B')
            ax.text(x + w - 0.6, cur_y, col_type, ha='right', fontsize=8.0, color='#64748B')
            cur_y -= line_spacing

    # Table 1: users (Top Left)
    draw_entity_table(
        x=5, y=55, w=25, h=33,
        table_name="users",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[],
        attr_list=[
            ("username", "VARCHAR(50) UNIQUE", "NOT NULL"),
            ("password", "VARCHAR(255)", "BCrypt Hashed"),
            ("full_name", "NVARCHAR(100)", "NOT NULL"),
            ("email", "VARCHAR(100) UNIQUE", "NOT NULL"),
            ("phone", "VARCHAR(20)", "NULL"),
            ("role", "VARCHAR(30)", "RBAC 4 Roles"),
            ("status", "BIT", "DEFAULT 1 (Active)"),
            ("created_at", "DATETIME2", "DEFAULT GETDATE()")
        ],
        header_color="#1E3A8A"
    )

    # Table 2: appointments (Bottom Left)
    draw_entity_table(
        x=5, y=9, w=25, h=38,
        table_name="appointments",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[
            ("user_id", "BIGINT (NULL)", "users(id)"),
            ("assigned_operator_id", "BIGINT (NULL)", "users(id)")
        ],
        attr_list=[
            ("appointment_code", "VARCHAR(30) UNIQUE", "APT-yyMM-..."),
            ("customer_name", "NVARCHAR(100)", "NOT NULL"),
            ("phone", "VARCHAR(20)", "NOT NULL"),
            ("email", "VARCHAR(100)", "NULL"),
            ("preferred_date", "DATETIME2", "NOT NULL"),
            ("transport_type", "VARCHAR(20)", "AIR | TRAIN"),
            ("notes", "NVARCHAR(500)", "Nhu cầu đoàn"),
            ("status", "VARCHAR(20)", "PENDING/CONFIRMED"),
            ("created_at", "DATETIME2", "DEFAULT GETDATE()")
        ],
        header_color="#0D9488"
    )

    # Table 3: reservations (Center)
    draw_entity_table(
        x=37.5, y=36, w=25, h=52,
        table_name="reservations",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[
            ("user_id", "BIGINT (NULL)", "users(id)")
        ],
        attr_list=[
            ("booking_code", "VARCHAR(30) UNIQUE", "RES-yyyyMM-..."),
            ("customer_name", "NVARCHAR(100)", "NOT NULL"),
            ("customer_phone", "VARCHAR(20)", "NOT NULL"),
            ("customer_email", "VARCHAR(100)", "NOT NULL"),
            ("transport_type", "VARCHAR(20)", "AIR | TRAIN"),
            ("departure_place", "NVARCHAR(100)", "Ga / Sân bay đi"),
            ("arrival_place", "NVARCHAR(100)", "Ga / Sân bay đến"),
            ("departure_date", "DATETIME2", "NOT NULL"),
            ("return_date", "DATETIME2", "Khứ hồi (NULL)"),
            ("is_round_trip", "BIT", "DEFAULT 0"),
            ("passenger_count", "INT", "DEFAULT 1 (>=1)"),
            ("total_amount", "DECIMAL(18,2)", "Tổng tiền đơn"),
            ("status", "VARCHAR(20)", "CREATED/CONFIRMED"),
            ("hold_expires_at", "DATETIME2", "Hạn giữ chỗ 15p"),
            ("cancel_reason", "NVARCHAR(255)", "Lý do hủy vé"),
            ("created_at", "DATETIME2", "DEFAULT GETDATE()"),
            ("updated_at", "DATETIME2", "Cập nhật đơn")
        ],
        header_color="#1E3A8A"
    )

    # Table 4: tickets (Top Right)
    draw_entity_table(
        x=70, y=35, w=25, h=53,
        table_name="tickets",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[
            ("reservation_id", "BIGINT (NULL)", "reservations(id)")
        ],
        attr_list=[
            ("transport_type", "VARCHAR(20)", "AIR | TRAIN"),
            ("vehicle_code", "VARCHAR(50)", "VN210, SE1..."),
            ("vehicle_name", "NVARCHAR(100)", "Vietnam Airlines..."),
            ("route_info", "NVARCHAR(200)", "Hà Nội - Sài Gòn"),
            ("departure_place", "NVARCHAR(100)", "Ga / Sân bay đi"),
            ("arrival_place", "NVARCHAR(100)", "Ga / Sân bay đến"),
            ("departure_time", "DATETIME2", "Giờ xuất phát"),
            ("arrival_time", "DATETIME2", "Giờ đến nơi"),
            ("seat_number", "NVARCHAR(50)", "Ghế 12A / Toa 4 G11"),
            ("seat_class", "VARCHAR(50)", "ECONOMY / SLEEPER_4"),
            ("passenger_name", "NVARCHAR(100)", "Tên hành khách"),
            ("id_card", "VARCHAR(30)", "CCCD / Hộ chiếu"),
            ("passenger_type", "VARCHAR(20)", "ADULT/CHILD/STUDENT"),
            ("base_price", "DECIMAL(18,2)", "Giá cơ sở"),
            ("service_fee", "DECIMAL(18,2)", "Thuế 120k / BH 10k"),
            ("extra_fee", "DECIMAL(18,2)", "Hành lý / Suất ăn"),
            ("final_price", "DECIMAL(18,2)", "Giá cuối cùng"),
            ("ticket_code", "VARCHAR(50)", "ETK-AIR-... / ETK-TRN"),
            ("qr_code_data", "NVARCHAR(MAX)", "Base64 QR Image"),
            ("status", "VARCHAR(20)", "AVAILABLE/HELD/ISSUED")
        ],
        header_color="#0369A1"
    )

    # Table 5: trans_details (Bottom Center)
    draw_entity_table(
        x=37.5, y=3, w=25, h=27,
        table_name="trans_details",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[
            ("reservation_id", "BIGINT", "reservations(id)")
        ],
        attr_list=[
            ("payment_code", "VARCHAR(50) UNIQUE", "PAY-yyMM-..."),
            ("amount", "DECIMAL(18,2)", "Số tiền thanh toán"),
            ("payment_method", "VARCHAR(30)", "VNPAY/CARD/TRANSFER"),
            ("payment_status", "VARCHAR(20)", "SUCCESS/REFUNDED"),
            ("transaction_reference", "VARCHAR(100)", "Mã GD Cổng/Bank"),
            ("note", "NVARCHAR(255)", "Ghi chú giao dịch"),
            ("transaction_time", "DATETIME2", "DEFAULT GETDATE()")
        ],
        header_color="#15803D"
    )

    # -------------------------------------------------------------------------
    # Draw Relationship Connectors & Annotations
    # -------------------------------------------------------------------------
    def draw_relation(x1, y1, x2, y2, label, card1="1", card2="N", color="#1E293B"):
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                    arrowprops=dict(arrowstyle="-|>", color=color, lw=1.6, mutation_scale=12))
        mid_x = (x1 + x2) / 2
        mid_y = (y1 + y2) / 2
        ax.text(mid_x, mid_y + 0.9, label, ha='center', va='bottom', fontsize=8.5, fontweight='bold', color=color,
                bbox=dict(boxstyle="round,pad=0.2", facecolor="#F1F5F9", edgecolor="none"))
        ax.text(x1, y1 + 0.6, card1, fontsize=9, fontweight='bold', color=color)
        ax.text(x2, y2 + 0.6, card2, fontsize=9, fontweight='bold', color=color)

    # Rel 1: users -> reservations (1:N)
    draw_relation(30, 70, 37.5, 70, "users.id = reservations.user_id", "1", "0..N", "#1E3A8A")

    # Rel 2: users -> appointments (Customer) (1:N)
    draw_relation(14, 55, 14, 47, "users.id = appointments.user_id", "1", "0..N", "#0D9488")

    # Rel 3: users -> appointments (Assigned Operator) (1:N)
    draw_relation(22, 55, 22, 47, "users.id = appointments.assigned_operator_id", "1", "0..N", "#D97706")

    # Rel 4: reservations -> tickets (1:N)
    draw_relation(62.5, 62, 70, 62, "reservations.id = tickets.reservation_id", "1", "1..N", "#0369A1")

    # Rel 5: reservations -> trans_details (1:N)
    draw_relation(50, 36, 50, 30, "reservations.id = trans_details.reservation_id", "1", "1..N", "#15803D")

    # Legend & Technical Rules Box (Bottom Right)
    legend_box = patches.FancyBboxPatch((70, 3), 25, 28, boxstyle="round,pad=0.4",
                                        facecolor='#F1F5F9', edgecolor='#94A3B8', linewidth=1)
    ax.add_patch(legend_box)
    ax.text(71, 29, "QUY TẮC TOÀN VẸN & CHUẨN HÓA 3NF:", fontsize=9.5, fontweight='bold', color='#0F172A')
    
    legend_items = [
        ("• Chuẩn 1NF:", "Mọi thuộc tính đều là nguyên tử tính (Atomic)."),
        ("• Chuẩn 2NF:", "100% bảng dùng PK đơn (BIGINT IDENTITY),"),
        ("", "không tồn tại phụ thuộc hàm bộ phận."),
        ("• Chuẩn 3NF:", "Không có phụ thuộc hàm bắc cầu;"),
        ("", "giá tính toán qua Strategy, không dư thừa."),
        ("• ON DELETE SET NULL:", "reservations -> tickets: Hủy/xóa đơn"),
        ("", "vẫn bảo toàn kho vé xe/bay (Ticket Status: AVAILABLE)."),
        ("• ON DELETE CASCADE:", "reservations -> trans_details:"),
        ("", "Xóa đơn tự động dọn dẹp đối soát thanh toán."),
        ("• Index Tối ưu:", "Composite Index trên (transport_type,"),
        ("", "departure_place, arrival_place, status).")
    ]
    cur_ly = 26.5
    for tag, desc in legend_items:
        if tag:
            ax.text(71, cur_ly, tag, fontsize=8.0, fontweight='bold', color='#1E40AF')
            ax.text(76, cur_ly, desc, fontsize=7.8, color='#334155')
        else:
            ax.text(76, cur_ly, desc, fontsize=7.8, color='#334155')
        cur_ly -= 1.6

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    plt.tight_layout()
    plt.savefig(output_path, dpi=250, bbox_inches='tight')
    plt.close()
    print(f"ERD Diagram generated successfully at: {output_path}")

def draw_service_class_diagram(output_path="docs/service_class_diagram.png"):
    fig = plt.figure(figsize=(20, 13), dpi=250)
    ax = fig.add_subplot(111)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    plt.rcParams['font.sans-serif'] = ['Segoe UI', 'Arial', 'Tahoma']
    fig.patch.set_facecolor('#F8FAFC')

    # Title Banner
    ax.text(50, 97.5, "SƠ ĐỒ THIẾT KẾ CÁC LỚP TẦNG DỊCH VỤ (SERVICE LAYER CLASS DIAGRAM)", 
            ha='center', va='center', fontsize=18, fontweight='bold', color='#0F172A', family='Segoe UI')
    ax.text(50, 95.0, "Mô Hình Đa Tầng Spring Boot 3 & Strategy Pattern (Web-Du-Lich)", 
            ha='center', va='center', fontsize=11, style='italic', color='#475569', family='Segoe UI')

    def draw_class_box(x, y, w, h, class_name, stereotype="«interface»", methods=None, fields=None, header_color="#1E3A8A"):
        header_h = 3.2
        # Header
        header_rect = patches.FancyBboxPatch((x, y + h - header_h), w, header_h,
                                             boxstyle="square,pad=0",
                                             facecolor=header_color, edgecolor='#0F172A', linewidth=1.1)
        ax.add_patch(header_rect)
        if stereotype:
            ax.text(x + w / 2, y + h - 1.1, stereotype, ha='center', va='center', fontsize=7.5, style='italic', color='#E2E8F0')
            ax.text(x + w / 2, y + h - 2.3, class_name, ha='center', va='center', fontsize=9.2, fontweight='bold', color='#FFFFFF')
        else:
            ax.text(x + w / 2, y + h - header_h / 2, class_name, ha='center', va='center', fontsize=9.5, fontweight='bold', color='#FFFFFF')

        # Body
        body_rect = patches.FancyBboxPatch((x, y), w, h - header_h,
                                           boxstyle="square,pad=0",
                                           facecolor='#FFFFFF', edgecolor='#0F172A', linewidth=1.1)
        ax.add_patch(body_rect)

        cur_y = y + h - header_h - 1.0
        line_spacing = 1.15

        if fields:
            for f in fields:
                ax.text(x + 0.5, cur_y, f, fontsize=7.5, color='#334155')
                cur_y -= line_spacing
            ax.plot([x + 0.3, x + w - 0.3], [cur_y + 0.3, cur_y + 0.3], color='#CBD5E1', linewidth=0.8)
            cur_y -= 0.6

        if methods:
            for m in methods:
                ax.text(x + 0.5, cur_y, m, fontsize=7.2, color='#0F172A')
                cur_y -= line_spacing

    # -------------------------------------------------------------------------
    # ROW 1: CONTROLLER LAYER (Top: Y=81 to 91)
    # -------------------------------------------------------------------------
    controllers = [
        (4, "TicketCatalogController", ["+ showSearchForm()", "+ searchTickets()", "+ viewVehicleSeats()"]),
        (23, "ReservationController", ["+ showBookingForm()", "+ processHoldBooking()", "+ viewBooking()", "+ cancelBooking()"]),
        (42, "PaymentController", ["+ showPaymentForm()", "+ processPayment()", "+ paymentSuccess()"]),
        (61, "AppointmentController", ["+ showScheduleForm()", "+ scheduleAppointment()", "+ searchAppointments()"]),
        (80, "AdminController", ["+ dashboard()", "+ manageReservations()", "+ manageAppointments()", "+ manageTickets()"])
    ]
    for cx, cname, cmethods in controllers:
        draw_class_box(cx, 81, 16, 11, cname, "«Controller»", cmethods, header_color="#4338CA")

    # -------------------------------------------------------------------------
    # ROW 2: SERVICE INTERFACES LAYER (Y=56 to 73)
    # -------------------------------------------------------------------------
    # 1. TicketCatalogService
    draw_class_box(
        4, 56, 16.5, 17,
        "TicketCatalogService", "«interface»",
        methods=[
            "+ searchTickets(dto): List<Ticket>",
            "+ getTicketById(id): Ticket",
            "+ getTicketsByVehicle(vCode, time)",
            "+ getOccupiedSeats(vCode): List<str>",
            "+ getAvailablePlaces(type, isDep)",
            "+ generateConsecutiveSeat(...)"
        ],
        header_color="#1E3A8A"
    )

    # 2. ReservationService
    draw_class_box(
        23, 56, 17, 17,
        "ReservationService", "«interface»",
        methods=[
            "+ createHoldReservation(dto, user)",
            "+ getByBookingCode(code): Res",
            "+ confirmAndIssue(code, method...)",
            "+ cancelReservation(code, reason)",
            "+ getAllReservations(): List<Res>",
            "+ getReservationsByPhone(phone)",
            "+ releaseExpiredHoldReservations()"
        ],
        header_color="#1E3A8A"
    )

    # 3. PaymentService
    draw_class_box(
        42.5, 56, 16.5, 17,
        "PaymentService", "«interface»",
        methods=[
            "+ processPayment(dto): TransDetail",
            "+ getByPaymentCode(code): Trans",
            "+ getPaymentsByReservation(id)",
            "+ getAllPayments(): List<Trans>"
        ],
        header_color="#1E3A8A"
    )

    # 4. AppointmentService
    draw_class_box(
        61.5, 56, 16.5, 17,
        "AppointmentService", "«interface»",
        methods=[
            "+ createAppointment(apt): Apt",
            "+ findByCode(code): Apt",
            "+ findAll(): List<Appointment>",
            "+ findByPhone(phone): List<Apt>",
            "+ updateStatus(id, status, opId)",
            "+ count(): long"
        ],
        header_color="#1E3A8A"
    )

    # 5. Utilities: QrCodeService & EmailService
    draw_class_box(
        80.5, 65, 15.5, 8,
        "QrCodeService", "«Service»",
        methods=[
            "+ generateBase64QrCode(content,",
            "  width, height): String (ZXing)"
        ],
        header_color="#0F766E"
    )

    draw_class_box(
        80.5, 54, 15.5, 10,
        "EmailService", "«interface»",
        methods=[
            "+ sendReservationHeldEmail()",
            "+ sendReservationConfirmedEmail()",
            "+ sendReservationCancelledEmail()",
            "+ sendAppointmentConfirmedEmail()"
        ],
        header_color="#0F766E"
    )

    # -------------------------------------------------------------------------
    # ROW 3: SERVICE IMPLEMENTATIONS LAYER (Y=28 to 47)
    # -------------------------------------------------------------------------
    # TicketCatalogServiceImpl
    draw_class_box(
        4, 30, 16.5, 18,
        "TicketCatalogServiceImpl", "«@Service»",
        fields=["- ticketRepository: TicketRepo"],
        methods=[
            "+ searchTickets(searchDto)",
            "+ getOccupiedSeats(vCode)",
            "+ generateConsecutiveSeat(...)",
            "- buildAirSeats()",
            "- buildTrainSeats()"
        ],
        header_color="#0284C7"
    )

    # ReservationServiceImpl
    draw_class_box(
        23, 27, 17, 21,
        "ReservationServiceImpl", "«@Service @Transactional»",
        fields=[
            "- reservationRepo: ResRepo",
            "- ticketRepo: TicketRepo",
            "- transDetailRepo: TransRepo",
            "- strategyFactory: StrategyFact",
            "- qrCodeService: QrService",
            "- emailService: EmailService"
        ],
        methods=[
            "+ createHoldReservation(dto, user)",
            "+ confirmAndIssue(code, method...)",
            "+ cancelReservation(code, reason)",
            "+ @Scheduled releaseExpired..."
        ],
        header_color="#0284C7"
    )

    # PaymentServiceImpl
    draw_class_box(
        42.5, 30, 16.5, 18,
        "PaymentServiceImpl", "«@Service @Transactional»",
        fields=[
            "- transDetailRepo: TransRepo",
            "- reservationService: ResService"
        ],
        methods=[
            "+ processPayment(dto): TransDetail",
            "+ getByPaymentCode(code)",
            "+ getPaymentsByReservation(id)",
            "+ getAllPayments()"
        ],
        header_color="#0284C7"
    )

    # AppointmentServiceImpl
    draw_class_box(
        61.5, 30, 16.5, 18,
        "AppointmentServiceImpl", "«@Service @Transactional»",
        fields=[
            "- appointmentRepo: AptRepo",
            "- userRepo: UserRepo",
            "- emailService: EmailService"
        ],
        methods=[
            "+ createAppointment(apt)",
            "+ updateStatus(id, status, opId)",
            "+ findByCode(code)",
            "+ findByPhone(phone)"
        ],
        header_color="#0284C7"
    )

    # -------------------------------------------------------------------------
    # ROW 4: STRATEGY PATTERN & REPOSITORIES (Y=2 to 21)
    # -------------------------------------------------------------------------
    # TicketStrategy Interface
    draw_class_box(
        4, 5, 18, 17,
        "TicketStrategy", "«interface (Strategy)»",
        methods=[
            "+ getTransportType(): TransportType",
            "+ calculatePrice(ticket, type, fee)",
            "+ validateReservation(res, tickets)",
            "+ calculateCancellationFee(res)",
            "+ getPolicyDescription(): String"
        ],
        header_color="#B45309"
    )

    # AirTicketStrategy
    draw_class_box(
        24, 11, 15.5, 11,
        "AirTicketStrategy", "«@Component»",
        methods=[
            "+ calculatePrice (Thuế 120k)",
            "+ validateReservation (CCCD)",
            "+ calculateCancellationFee (24h)"
        ],
        header_color="#D97706"
    )

    # TrainTicketStrategy
    draw_class_box(
        24, 1, 15.5, 9.5,
        "TrainTicketStrategy", "«@Component»",
        methods=[
            "+ calculatePrice (BH 10k, HS/SV)",
            "+ validateReservation (Ga tàu)",
            "+ calculateCancellationFee (4h-24h)"
        ],
        header_color="#D97706"
    )

    # TicketStrategyFactory
    draw_class_box(
        41.5, 3, 17.5, 13,
        "TicketStrategyFactory", "«@Component (Factory)»",
        fields=["- strategyMap: Map<Transport, Strat>"],
        methods=[
            "+ @Autowired (List<TicketStrategy>)",
            "+ getStrategy(type): TicketStrategy"
        ],
        header_color="#7C2D12"
    )

    # Repositories Box (Bottom Right)
    repo_box = patches.FancyBboxPatch((61.5, 2), 34.5, 21, boxstyle="round,pad=0.3",
                                     facecolor='#F1F5F9', edgecolor='#64748B', linewidth=1.2)
    ax.add_patch(repo_box)
    ax.text(63, 20.5, "SPRING DATA JPA REPOSITORIES (Persistence Layer):", fontsize=9, fontweight='bold', color='#0F172A')
    
    repos = [
        ("• TicketRepository:", "findBySearchCriteria, findByVehicleCodeAndDepTime, findOccupiedSeats"),
        ("• ReservationRepository:", "findByBookingCode, findExpiredReservations, findByCustomerPhone"),
        ("• TransDetailRepository:", "findByPaymentCode, findByReservationIdOrderByTransactionTimeDesc"),
        ("• AppointmentRepository:", "findByAppointmentCode, findByPhoneOrderByCreatedAtDesc"),
        ("• UserRepository:", "findByUsername, findByEmail, existsByUsername, existsByEmail")
    ]
    ry = 18.2
    for r_name, r_methods in repos:
        ax.text(63, ry, r_name, fontsize=8.0, fontweight='bold', color='#1E40AF')
        ax.text(63, ry - 1.2, r_methods, fontsize=7.2, style='italic', color='#334155')
        ry -= 3.1

    # -------------------------------------------------------------------------
    # Draw Arrows & Link Annotations
    # -------------------------------------------------------------------------
    # Controller -> Service Interface (Dashed with arrow)
    ax.annotate('', xy=(12, 73), xytext=(12, 81), arrowprops=dict(arrowstyle="->", color='#4338CA', lw=1.2, ls='--'))
    ax.annotate('', xy=(31, 73), xytext=(31, 81), arrowprops=dict(arrowstyle="->", color='#4338CA', lw=1.2, ls='--'))
    ax.annotate('', xy=(50, 73), xytext=(50, 81), arrowprops=dict(arrowstyle="->", color='#4338CA', lw=1.2, ls='--'))
    ax.annotate('', xy=(69, 73), xytext=(69, 81), arrowprops=dict(arrowstyle="->", color='#4338CA', lw=1.2, ls='--'))

    # Service Impl -> Service Interface (Implements: Solid with empty triangle style)
    ax.annotate('', xy=(12, 56), xytext=(12, 48), arrowprops=dict(arrowstyle="-|>", color='#0284C7', lw=1.4))
    ax.annotate('', xy=(31, 56), xytext=(31, 48), arrowprops=dict(arrowstyle="-|>", color='#0284C7', lw=1.4))
    ax.annotate('', xy=(50, 56), xytext=(50, 48), arrowprops=dict(arrowstyle="-|>", color='#0284C7', lw=1.4))
    ax.annotate('', xy=(69, 56), xytext=(69, 48), arrowprops=dict(arrowstyle="-|>", color='#0284C7', lw=1.4))

    # Air & Train Strategy -> TicketStrategy
    ax.annotate('', xy=(22, 16), xytext=(24, 16), arrowprops=dict(arrowstyle="-|>", color='#B45309', lw=1.2))
    ax.annotate('', xy=(22, 6), xytext=(24, 6), arrowprops=dict(arrowstyle="-|>", color='#B45309', lw=1.2))

    # ReservationServiceImpl -> TicketStrategyFactory
    ax.annotate('', xy=(41.5, 10), xytext=(35, 27), arrowprops=dict(arrowstyle="->", color='#7C2D12', lw=1.3, ls=':'))
    ax.text(37, 18, "uses Factory", fontsize=7.5, style='italic', color='#7C2D12')

    # PaymentServiceImpl -> ReservationService
    ax.annotate('', xy=(40, 64), xytext=(42.5, 38), arrowprops=dict(arrowstyle="->", color='#1E3A8A', lw=1.2, ls=':'))

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    plt.tight_layout()
    plt.savefig(output_path, dpi=250, bbox_inches='tight')
    plt.close()
    print(f"Service Class Diagram generated successfully at: {output_path}")

if __name__ == "__main__":
    draw_erd_diagram("docs/erd_database_diagram.png")
    draw_service_class_diagram("docs/service_class_diagram.png")
