import matplotlib.pyplot as plt
import matplotlib.patches as patches
import os

def draw_erd_diagram_vn(output_path="docs/erd_database_diagram_vn.png"):
    fig = plt.figure(figsize=(19, 12), dpi=250)
    ax = fig.add_subplot(111)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    plt.rcParams['font.sans-serif'] = ['Segoe UI', 'Arial', 'Tahoma']
    fig.patch.set_facecolor('#F8FAFC')

    # Title Banner
    ax.text(50, 97.2, "SƠ ĐỒ CƠ SỞ DỮ LIỆU QUAN HỆ (ERD - TIẾNG VIỆT)", 
            ha='center', va='center', fontsize=18, fontweight='bold', color='#0F172A', family='Segoe UI')
    ax.text(50, 94.6, "Hệ Thống Đặt Vé Du Lịch (Web-Du-Lich) — Chuẩn 4 Bảng Cốt Lõi 3NF (Tích hợp Lịch hẹn tư vấn vào Đơn đặt vé)", 
            ha='center', va='center', fontsize=11, style='italic', color='#475569', family='Segoe UI')

    def draw_entity_table(x, y, w, h, table_name, pk_list, fk_list, attr_list, header_color="#1E3A8A"):
        header_h = 3.2
        header_rect = patches.FancyBboxPatch((x, y + h - header_h), w, header_h,
                                             boxstyle="square,pad=0",
                                             facecolor=header_color, edgecolor='#0F172A', linewidth=1.2)
        ax.add_patch(header_rect)
        ax.text(x + w / 2, y + h - header_h / 2, f"BẢNG: {table_name}",
                ha='center', va='center', fontsize=10.5, fontweight='bold', color='#FFFFFF')

        body_rect = patches.FancyBboxPatch((x, y), w, h - header_h,
                                           boxstyle="square,pad=0",
                                           facecolor='#FFFFFF', edgecolor='#0F172A', linewidth=1.2)
        ax.add_patch(body_rect)

        cur_y = y + h - header_h - 1.2
        line_spacing = 1.35

        for col, col_type in pk_list:
            ax.text(x + 0.6, cur_y, "[PK]", fontsize=8.2, fontweight='bold', color='#B91C1C')
            ax.text(x + 3.2, cur_y, col, fontsize=8.5, fontweight='bold', color='#0F172A')
            ax.text(x + w - 0.6, cur_y, col_type, ha='right', fontsize=8.0, style='italic', color='#64748B')
            cur_y -= line_spacing

        for col, col_type, ref in fk_list:
            ax.text(x + 0.6, cur_y, "[FK]", fontsize=8.2, fontweight='bold', color='#2563EB')
            ax.text(x + 3.2, cur_y, col, fontsize=8.5, fontweight='bold', color='#1E40AF')
            ax.text(x + w - 0.6, cur_y, col_type, ha='right', fontsize=8.0, style='italic', color='#64748B')
            cur_y -= line_spacing

        if pk_list or fk_list:
            ax.plot([x + 0.4, x + w - 0.4], [cur_y + 0.5, cur_y + 0.5], color='#E2E8F0', linewidth=1)
            cur_y -= 0.5

        for col, col_type in attr_list:
            ax.text(x + 0.8, cur_y, col, fontsize=8.2, color='#1E293B')
            ax.text(x + w - 0.6, cur_y, col_type, ha='right', fontsize=8.0, color='#64748B')
            cur_y -= line_spacing

    # Table 1: NguoiDung (Left)
    draw_entity_table(
        x=4, y=42, w=26, h=36,
        table_name="NguoiDung",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[],
        attr_list=[
            ("ten_dang_nhap", "VARCHAR(50) UNIQUE"),
            ("mat_khau", "VARCHAR(255)"),
            ("ho_ten", "NVARCHAR(100)"),
            ("email", "VARCHAR(100) UNIQUE"),
            ("so_dien_thoai", "VARCHAR(20)"),
            ("vai_tro", "VARCHAR(30)"),
            ("trang_thai", "BIT (DEFAULT 1)"),
            ("ngay_tao", "DATETIME2")
        ],
        header_color="#1E3A8A"
    )

    # Table 2: DonDatCho (Center) - INTEGRATED CONSULTATION APPOINTMENT
    draw_entity_table(
        x=35, y=14, w=29, h=74,
        table_name="DonDatCho",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[
            ("nguoi_dung_id", "BIGINT (NULL)", "NguoiDung(id)")
        ],
        attr_list=[
            ("ma_dat_cho", "VARCHAR(30) UNIQUE"),
            ("ten_khach_hang", "NVARCHAR(100)"),
            ("so_dien_thoai", "VARCHAR(20)"),
            ("email", "VARCHAR(100)"),
            ("loai_phuong_tien", "VARCHAR(20)"),
            ("diem_di", "NVARCHAR(100)"),
            ("diem_den", "NVARCHAR(100)"),
            ("ngay_di", "DATETIME2"),
            ("ngay_ve", "DATETIME2 (NULL)"),
            ("la_khu_hoi", "BIT (DEFAULT 0)"),
            ("so_luong_khach", "INT (DEFAULT 1)"),
            ("tong_tien", "DECIMAL(18,2)"),
            ("trang_thai", "VARCHAR(20)"),
            ("han_giu_cho", "DATETIME2 (15p)"),
            ("ly_do_huy", "NVARCHAR(255)"),
            ("--- Tư vấn sau đặt vé ---", "----------------"),
            ("co_hen_tu_van", "BIT (DEFAULT 0)"),
            ("ngay_hen_tu_van", "DATETIME2 (NULL)"),
            ("ghi_chu_tu_van", "NVARCHAR(500)"),
            ("trang_thai_tu_van", "VARCHAR(20)"),
            ("nhan_vien_tu_van_id", "BIGINT (NULL)"),
            ("--- Quản trị đơn ---", "----------------"),
            ("ngay_tao", "DATETIME2"),
            ("ngay_cap_nhat", "DATETIME2")
        ],
        header_color="#0F766E"
    )

    # Table 3: Ve (Top Right)
    draw_entity_table(
        x=69, y=36, w=27, h=52,
        table_name="Ve",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[
            ("don_dat_cho_id", "BIGINT (NULL)", "DonDatCho(id)")
        ],
        attr_list=[
            ("loai_phuong_tien", "VARCHAR(20)"),
            ("so_hieu_chuyen", "VARCHAR(50)"),
            ("ten_phuong_tien", "NVARCHAR(100)"),
            ("tuyen_duong", "NVARCHAR(200)"),
            ("diem_di", "NVARCHAR(100)"),
            ("diem_den", "NVARCHAR(100)"),
            ("gio_di", "DATETIME2"),
            ("gio_den", "DATETIME2"),
            ("so_ghe", "NVARCHAR(50)"),
            ("hang_ve", "VARCHAR(50)"),
            ("ten_hanh_khach", "NVARCHAR(100)"),
            ("so_cccd", "VARCHAR(30)"),
            ("loai_khach", "VARCHAR(20)"),
            ("gia_goc", "DECIMAL(18,2)"),
            ("phi_dich_vu", "DECIMAL(18,2)"),
            ("phu_phi", "DECIMAL(18,2)"),
            ("gia_cuoi", "DECIMAL(18,2)"),
            ("ma_ve_dien_tu", "VARCHAR(50)"),
            ("du_lieu_qr", "NVARCHAR(MAX)"),
            ("trang_thai", "VARCHAR(20)")
        ],
        header_color="#0369A1"
    )

    # Table 4: GiaoDich (Bottom Right)
    draw_entity_table(
        x=69, y=5, w=27, h=27,
        table_name="GiaoDich",
        pk_list=[("id", "BIGINT IDENTITY")],
        fk_list=[
            ("don_dat_cho_id", "BIGINT", "DonDatCho(id)")
        ],
        attr_list=[
            ("ma_giao_dich", "VARCHAR(50) UNIQUE"),
            ("so_tien", "DECIMAL(18,2)"),
            ("phuong_thuc", "VARCHAR(30)"),
            ("trang_thai", "VARCHAR(20)"),
            ("ma_tham_chieu", "VARCHAR(100)"),
            ("ghi_chu", "NVARCHAR(255)"),
            ("thoi_gian_giao_dich", "DATETIME2")
        ],
        header_color="#15803D"
    )

    # -------------------------------------------------------------------------
    # Draw Relationship Connectors (CLEAN 1-N RELATIONSHIPS)
    # -------------------------------------------------------------------------
    def draw_relation(x1, y1, x2, y2, label, card1="1", card2="N", color="#1E293B"):
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                    arrowprops=dict(arrowstyle="-|>", color=color, lw=1.6, mutation_scale=12))
        mid_x = (x1 + x2) / 2
        mid_y = (y1 + y2) / 2
        ax.text(mid_x, mid_y + 0.9, label, ha='center', va='bottom', fontsize=8.2, fontweight='bold', color=color,
                bbox=dict(boxstyle="round,pad=0.2", facecolor="#F1F5F9", edgecolor="none"))
        ax.text(x1, y1 + 0.6, card1, fontsize=9, fontweight='bold', color=color)
        ax.text(x2, y2 + 0.6, card2, fontsize=9, fontweight='bold', color=color)

    # Rel 1: NguoiDung -> DonDatCho (nguoi_dung_id) - DUY NHẤT 1 LIÊN KẾT 1-N
    draw_relation(30, 60, 35, 60, "NguoiDung.id = DonDatCho.nguoi_dung_id", "1", "0..N", "#1E3A8A")

    # Rel 2: DonDatCho -> Ve (1:N)
    draw_relation(64, 62, 69, 62, "DonDatCho.id = Ve.don_dat_cho_id", "1", "1..N", "#0369A1")

    # Rel 3: DonDatCho -> GiaoDich (1:N)
    draw_relation(64, 20, 69, 20, "DonDatCho.id = GiaoDich.don_dat_cho_id", "1", "1..N", "#15803D")

    # Legend & Rules Box (Bottom Left)
    legend_box = patches.FancyBboxPatch((4, 5), 26, 32, boxstyle="round,pad=0.4",
                                        facecolor='#F1F5F9', edgecolor='#94A3B8', linewidth=1)
    ax.add_patch(legend_box)
    ax.text(5, 34.5, "QUY TẮC RÀNG BUỘC & CHUẨN HÓA 4 BẢNG (3NF):", fontsize=9.2, fontweight='bold', color='#0F172A')
    
    legend_items = [
        ("• Tinh gọn CSDL:", "Giảm từ 5 bảng xuống ĐÚNG 4 BẢNG CỐT LÕI,"),
        ("", "bỏ bảng LichHen độc lập để tránh dư thừa."),
        ("• Nghiệp vụ tư vấn:", "Lịch hẹn chỉ xuất hiện SAU KHI ĐẶT VÉ;"),
        ("", "thông tin tư vấn lưu trực tiếp vào DonDatCho."),
        ("• Chuẩn 1NF & 2NF:", "Mọi cột đều nguyên tử đơn giá trị;"),
        ("", "toàn bộ bảng dùng PK đơn (BIGINT IDENTITY)."),
        ("• Chuẩn 3NF:", "Không có phụ thuộc hàm bắc cầu;"),
        ("", "chuẩn hóa 100% tiếng Việt rõ nghĩa."),
        ("• ON DELETE SET NULL:", "DonDatCho -> Ve: Hủy đơn thì ghế trở về"),
        ("", "kho mở bán (trạng thái AVAILABLE)."),
        ("• ON DELETE CASCADE:", "DonDatCho -> GiaoDich: Dọn dẹp thanh toán.")
    ]
    cur_ly = 32
    for tag, desc in legend_items:
        if tag:
            ax.text(5, cur_ly, tag, fontsize=8.0, fontweight='bold', color='#1E40AF')
            ax.text(10, cur_ly, desc, fontsize=7.8, color='#334155')
        else:
            ax.text(10, cur_ly, desc, fontsize=7.8, color='#334155')
        cur_ly -= 1.6

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    plt.tight_layout()
    plt.savefig(output_path, dpi=250, bbox_inches='tight')
    if output_path != "docs/erd_database_diagram.png":
        plt.savefig("docs/erd_database_diagram.png", dpi=250, bbox_inches='tight')
    plt.close()
    print(f"Sơ đồ ERD tiếng Việt đã được tạo tại: {output_path}")

if __name__ == "__main__":
    draw_erd_diagram_vn("docs/erd_database_diagram_vn.png")
