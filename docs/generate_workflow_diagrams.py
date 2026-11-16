import matplotlib.pyplot as plt
import matplotlib.patches as patches

def draw_workflow_flight():
    fig, ax = plt.subplots(figsize=(14, 5.5), dpi=250)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 40)
    ax.axis('off')
    plt.rcParams['font.sans-serif'] = ['Segoe UI', 'Arial']
    fig.patch.set_facecolor('#FFFFFF')

    ax.text(50, 37, "QUY TRÌNH ĐẶT VÉ MÁY BAY & GIỮ CHỖ CÓ THỜI HẠN (15 PHÚT)", 
            ha='center', va='center', fontsize=13, fontweight='bold', color='#0F172A')

    steps = [
        {"x": 2, "w": 16, "title": "BƯỚC 1\nTìm Chuyến Bay", "desc": "Khách chọn Nơi đi,\nNơi đến, Ngày bay,\nSố lượng khách\n(FlightSearchDto)", "color": "#E0F2FE", "border": "#0284C7"},
        {"x": 22, "w": 16, "title": "BƯỚC 2\nChọn Hạng Ghế", "desc": "Chọn chuyến bay,\nchọn Economy / Business.\nChuyển sang form nhập\nthông tin hành khách", "color": "#F0FDF4", "border": "#16A34A"},
        {"x": 42, "w": 16, "title": "BƯỚC 3\nValidate Dữ Liệu", "desc": "Kiểm tra số CCCD/Passport,\ntỷ lệ Infant/Adult,\nhành lý quá cước,\ntính thuế sân bay 120k", "color": "#FEF3C7", "border": "#D97706"},
        {"x": 62, "w": 16, "title": "BƯỚC 4\nKhóa Ghế & Giữ Chỗ", "desc": "flightLocks đồng bộ,\ntrừ ghế trống trong DB,\ntạo Booking status HOLD\nCountdown 15 phút", "color": "#FEE2E2", "border": "#DC2626"},
        {"x": 82, "w": 16, "title": "BƯỚC 5\nThanh Toán & Vé", "desc": "Thanh toán trong 15p,\nchuyển status CONFIRMED,\nxuất mã PNR & gửi email\nthông báo khách hàng", "color": "#F3E8FF", "border": "#9333EA"}
    ]

    for i, s in enumerate(steps):
        box = patches.FancyBboxPatch((s["x"], 8), s["w"], 24,
                                     boxstyle="round,pad=0.2,rounding_size=0.6",
                                     linewidth=1.5, edgecolor=s["border"], facecolor=s["color"])
        ax.add_patch(box)
        ax.text(s["x"] + s["w"]/2, 28, s["title"], ha='center', va='center', fontsize=9.5, fontweight='bold', color='#0F172A')
        ax.plot([s["x"]+1, s["x"]+s["w"]-1], [24, 24], color=s["border"], linestyle='-', linewidth=1)
        ax.text(s["x"] + s["w"]/2, 16, s["desc"], ha='center', va='center', fontsize=8, color='#334155')

        if i < len(steps) - 1:
            ax.annotate('', xy=(steps[i+1]["x"], 20), xytext=(s["x"] + s["w"], 20),
                        arrowprops=dict(facecolor='#475569', edgecolor='#475569', width=1.5, headwidth=6, headlength=6))

    # Expired branch note
    ax.annotate('Nếu quá 15 phút chưa thanh toán\n-> Scheduler tự động nhả ghế & hủy đơn',
                xy=(70, 8), xytext=(70, 2),
                ha='center', va='center', fontsize=8, color='#B91C1C', fontweight='bold',
                arrowprops=dict(arrowstyle="->", color="#DC2626", lw=1.2, ls="--"))

    plt.tight_layout()
    plt.savefig("workflow_flight_booking.png", dpi=250, bbox_inches='tight')
    plt.close()

def draw_workflow_train_overlap():
    fig, ax = plt.subplots(figsize=(14, 6), dpi=250)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 45)
    ax.axis('off')
    plt.rcParams['font.sans-serif'] = ['Segoe UI', 'Arial']
    fig.patch.set_facecolor('#FFFFFF')

    ax.text(50, 42, "THUẬT TOÁN KIỂM TRA CHỒNG LẤN CHẶNG (SEGMENT OVERLAP CHECK) VÉ TÀU", 
            ha='center', va='center', fontsize=13, fontweight='bold', color='#0F172A')

    # Line of railway stations
    ax.plot([10, 90], [33, 33], color='#0284C7', linewidth=3, zorder=2)
    stations = [
        {"name": "Ga Hà Nội\n(km 0)", "x": 10},
        {"name": "Ga Nam Định\n(km 87)", "x": 26},
        {"name": "Ga Vinh\n(km 319)", "x": 42},
        {"name": "Ga Huế\n(km 688)", "x": 58},
        {"name": "Ga Đà Nẵng\n(km 791)", "x": 74},
        {"name": "Ga Sài Gòn\n(km 1726)", "x": 90}
    ]
    for st in stations:
        ax.scatter(st["x"], 33, s=120, color='#0284C7', edgecolor='#FFFFFF', linewidth=2, zorder=3)
        ax.text(st["x"], 36, st["name"], ha='center', va='bottom', fontsize=8.5, fontweight='bold', color='#0F172A')

    # Ticket 1 (Hà Nội -> Vinh)
    rect1 = patches.FancyBboxPatch((10, 22), 32, 5, boxstyle="round,pad=0.2,rounding_size=0.4",
                                  fc='#E0F2FE', ec='#0284C7', lw=1.5)
    ax.add_patch(rect1)
    ax.text(26, 24.5, "Khách A đã đặt ghế 12: Hà Nội → Vinh (Chặng 1)", ha='center', va='center', fontsize=8.5, color='#0369A1', fontweight='bold')

    # Ticket 2 (Huế -> Sài Gòn)
    rect2 = patches.FancyBboxPatch((58, 22), 32, 5, boxstyle="round,pad=0.2,rounding_size=0.4",
                                  fc='#DCFCE7', ec='#16A34A', lw=1.5)
    ax.add_patch(rect2)
    ax.text(74, 24.5, "Khách B đặt cùng ghế 12: Huế → Sài Gòn (HỢP LỆ)", ha='center', va='center', fontsize=8.5, color='#15803D', fontweight='bold')

    # Overlapping attempt (Nam Định -> Huế)
    rect3 = patches.FancyBboxPatch((26, 12), 32, 5, boxstyle="round,pad=0.2,rounding_size=0.4",
                                  fc='#FEE2E2', ec='#DC2626', lw=1.5, ls='--')
    ax.add_patch(rect3)
    ax.text(42, 14.5, "Khách C muốn đặt ghế 12: Nam Định → Huế (BỊ TỪ CHỐI DO CHỒNG LẤN VỚI KHÁCH A)",
            ha='center', va='center', fontsize=8.5, color='#B91C1C', fontweight='bold')

    # Formula Box
    formula_rect = patches.FancyBboxPatch((5, 1), 90, 7.5, boxstyle="round,pad=0.2,rounding_size=0.4",
                                          fc='#F8FAFC', ec='#64748B', lw=1)
    ax.add_patch(formula_rect)
    ax.text(50, 5.5, "Điều kiện không chồng lấn: (ReqEnd <= BookedStart) HOẶC (ReqStart >= BookedEnd)",
            ha='center', va='center', fontsize=9.5, fontweight='bold', color='#0F172A')
    ax.text(50, 2.5, "Công thức giá: Giá vé = Khoảng cách (km) × Đơn giá loại toa (Ghế mềm 700đ, Giường K6 1000đ, VIP K4 1300đ) × (1 - Tỉ lệ giảm ưu tiên)",
            ha='center', va='center', fontsize=8.5, color='#475569')

    plt.tight_layout()
    plt.savefig("workflow_train_seat_overlap.png", dpi=250, bbox_inches='tight')
    plt.close()

def draw_workflow_prg_consultation():
    fig, ax = plt.subplots(figsize=(14, 5), dpi=250)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 35)
    ax.axis('off')
    plt.rcParams['font.sans-serif'] = ['Segoe UI', 'Arial']
    fig.patch.set_facecolor('#FFFFFF')

    ax.text(50, 32, "MÔ HÌNH POST/REDIRECT/GET (PRG) TRONG ĐĂNG KÝ TƯ VẤN DU LỊCH", 
            ha='center', va='center', fontsize=13, fontweight='bold', color='#0F172A')

    # 3 Participants: Browser, Controller, Database
    ax.text(15, 26, "Trình Duyệt (Client)", ha='center', va='center', fontsize=10, fontweight='bold', color='#0284C7')
    ax.text(50, 26, "ConsultationController", ha='center', va='center', fontsize=10, fontweight='bold', color='#0D9488')
    ax.text(85, 26, "ConsultationService & DB", ha='center', va='center', fontsize=10, fontweight='bold', color='#7C3AED')

    ax.plot([15, 15], [3, 23], color='#CBD5E1', linestyle='--', lw=1.2)
    ax.plot([50, 50], [3, 23], color='#CBD5E1', linestyle='--', lw=1.2)
    ax.plot([85, 85], [3, 23], color='#CBD5E1', linestyle='--', lw=1.2)

    # Step 1: POST form
    ax.annotate('', xy=(50, 19), xytext=(15, 19),
                arrowprops=dict(facecolor='#0284C7', edgecolor='#0284C7', width=1.2, headwidth=6, headlength=5))
    ax.text(32.5, 20.5, "1. POST /consultations (Form Data)", ha='center', va='center', fontsize=8.5, fontweight='bold', color='#0284C7')

    # Step 2: Validate & Save DB
    ax.annotate('', xy=(85, 16), xytext=(50, 16),
                arrowprops=dict(facecolor='#0D9488', edgecolor='#0D9488', width=1.2, headwidth=6, headlength=5))
    ax.text(67.5, 17.5, "2. Validate & Save (PENDING)", ha='center', va='center', fontsize=8.5, color='#0D9488', fontweight='bold')

    # Step 3: 302 Redirect
    ax.annotate('', xy=(15, 12), xytext=(50, 12),
                arrowprops=dict(facecolor='#DC2626', edgecolor='#DC2626', width=1.2, headwidth=6, headlength=5))
    ax.text(32.5, 13.5, "3. HTTP 302 Redirect (/consultations/success?code=...)", ha='center', va='center', fontsize=8.5, fontweight='bold', color='#DC2626')

    # Step 4: GET success
    ax.annotate('', xy=(50, 8), xytext=(15, 8),
                arrowprops=dict(facecolor='#16A34A', edgecolor='#16A34A', width=1.2, headwidth=6, headlength=5))
    ax.text(32.5, 9.5, "4. GET /consultations/success (Hiển thị trang thành công)", ha='center', va='center', fontsize=8.5, color='#16A34A', fontweight='bold')

    # Note
    ax.text(50, 2, "Lợi ích PRG: Khi người dùng F5 / Refresh trang, trình duyệt chỉ gửi lại lệnh GET an toàn (Idempotent),\nloại bỏ hoàn toàn lỗi trùng lặp dữ liệu (Double Submit)!",
            ha='center', va='center', fontsize=8.5, color='#334155', style='italic')

    plt.tight_layout()
    plt.savefig("workflow_consultation_prg.png", dpi=250, bbox_inches='tight')
    plt.close()

if __name__ == '__main__':
    draw_workflow_flight()
    draw_workflow_train_overlap()
    draw_workflow_prg_consultation()
    print("All workflow diagrams generated successfully!")
