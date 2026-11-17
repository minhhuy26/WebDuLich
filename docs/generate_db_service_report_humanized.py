import os
import shutil
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT

def set_cell_background(cell, hex_color):
    from docx.oxml import parse_xml
    from docx.oxml.ns import nsdecls
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
    tc_pr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=140, right=140):
    from docx.oxml import parse_xml
    from docx.oxml.ns import nsdecls
    tc_pr = cell._tc.get_or_add_tcPr()
    tc_mar = parse_xml(f'<w:tcMar {nsdecls("w")}>'
                       f'<w:top w:w="{top}" w:type="dxa"/>'
                       f'<w:bottom w:w="{bottom}" w:type="dxa"/>'
                       f'<w:left w:w="{left}" w:type="dxa"/>'
                       f'<w:right w:w="{right}" w:type="dxa"/>'
                       f'</w:tcMar>')
    tc_pr.append(tc_mar)

def set_table_borders(table, color="CBD5E1", sz="4", val="single"):
    from docx.oxml import parse_xml
    from docx.oxml.ns import nsdecls
    tbl_pr = table._tbl.tblPr
    borders = parse_xml(f'<w:tblBorders {nsdecls("w")}>'
                        f'<w:top w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                        f'<w:bottom w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                        f'<w:left w:val="none"/>'
                        f'<w:right w:val="none"/>'
                        f'<w:insideH w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                        f'<w:insideV w:val="none"/>'
                        f'</w:tblBorders>')
    tbl_pr.append(borders)

def add_callout(doc, text_list, title=None, border_color="1E3A8A", bg_color="F8FAFC"):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    
    cell = table.cell(0, 0)
    cell.width = Inches(6.5)
    set_cell_background(cell, bg_color)
    set_cell_margins(cell, top=120, bottom=120, left=180, right=160)
    
    from docx.oxml import parse_xml
    from docx.oxml.ns import nsdecls
    tc_pr = cell._tc.get_or_add_tcPr()
    tc_borders = parse_xml(f'<w:tcBorders {nsdecls("w")}>'
                           f'<w:top w:val="none"/>'
                           f'<w:left w:val="single" w:sz="24" w:space="0" w:color="{border_color}"/>'
                           f'<w:bottom w:val="none"/>'
                           f'<w:right w:val="none"/>'
                           f'</w:tcBorders>')
    tc_pr.append(tc_borders)
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.15
    
    if title:
        run_title = p.add_run(f"{title}\n")
        run_title.font.name = "Times New Roman"
        run_title.font.size = Pt(11)
        run_title.font.bold = True
        run_title.font.color.rgb = RGBColor(0x1E, 0x3A, 0x8A)
    
    for idx, item in enumerate(text_list):
        if idx > 0 or title:
            p2 = cell.add_paragraph()
            p2.paragraph_format.space_before = Pt(2)
            p2.paragraph_format.space_after = Pt(2)
            p2.paragraph_format.line_spacing = 1.15
            run = p2.add_run(item)
        else:
            run = p.add_run(item)
            
        run.font.name = "Times New Roman"
        run.font.size = Pt(10.5)
        run.font.color.rgb = RGBColor(0x1F, 0x29, 0x37)
        
    p_spacer = doc.add_paragraph()
    p_spacer.paragraph_format.space_before = Pt(0)
    p_spacer.paragraph_format.space_after = Pt(4)

def add_styled_table(doc, headers, data, col_widths=None):
    table = doc.add_table(rows=len(data) + 1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    set_table_borders(table, color="CBD5E1", sz="4")
    
    hdr_cells = table.rows[0].cells
    for i, header_text in enumerate(headers):
        hdr_cells[i].text = header_text
        set_cell_background(hdr_cells[i], "1E3A8A")
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=120, right=120)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in p.runs:
            run.font.name = "Times New Roman"
            run.font.size = Pt(10)
            run.font.bold = True
            run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
            
    for row_idx, row_data in enumerate(data):
        row_cells = table.rows[row_idx + 1].cells
        bg_col = "F8FAFC" if row_idx % 2 == 1 else "FFFFFF"
        for col_idx, cell_value in enumerate(row_data):
            row_cells[col_idx].text = str(cell_value)
            set_cell_background(row_cells[col_idx], bg_col)
            set_cell_margins(row_cells[col_idx], top=90, bottom=90, left=120, right=120)
            p = row_cells[col_idx].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            for run in p.runs:
                run.font.name = "Times New Roman"
                run.font.size = Pt(9.5)
                run.font.color.rgb = RGBColor(0x1F, 0x29, 0x37)
                
    if col_widths:
        for row in table.rows:
            for idx, width in enumerate(col_widths):
                row.cells[idx].width = width

    p_spacer = doc.add_paragraph()
    p_spacer.paragraph_format.space_before = Pt(0)
    p_spacer.paragraph_format.space_after = Pt(4)
    return table

def add_heading_1(doc, text):
    h = doc.add_heading(text, level=1)
    h.paragraph_format.space_before = Pt(14)
    h.paragraph_format.space_after = Pt(4)
    h.paragraph_format.keep_with_next = True
    for r in h.runs:
        r.font.name = "Times New Roman"
        r.font.size = Pt(14)
        r.font.bold = True
        r.font.color.rgb = RGBColor(0x1E, 0x3A, 0x8A)
    return h

def add_heading_2(doc, text):
    h = doc.add_heading(text, level=2)
    h.paragraph_format.space_before = Pt(10)
    h.paragraph_format.space_after = Pt(3)
    h.paragraph_format.keep_with_next = True
    for r in h.runs:
        r.font.name = "Times New Roman"
        r.font.size = Pt(12.5)
        r.font.bold = True
        r.font.color.rgb = RGBColor(0x0F, 0x56, 0x91)
    return h

def add_heading_3(doc, text):
    h = doc.add_heading(text, level=3)
    h.paragraph_format.space_before = Pt(8)
    h.paragraph_format.space_after = Pt(2)
    h.paragraph_format.keep_with_next = True
    for r in h.runs:
        r.font.name = "Times New Roman"
        r.font.size = Pt(11.5)
        r.font.bold = True
        r.font.italic = True
        r.font.color.rgb = RGBColor(0x2B, 0x4C, 0x6F)
    return h

def add_paragraph(doc, text, bold_prefix=None, space_after=4, align=WD_ALIGN_PARAGRAPH.JUSTIFY):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = 1.2
    p.alignment = align
    
    if bold_prefix:
        r_pre = p.add_run(bold_prefix)
        r_pre.font.name = "Times New Roman"
        r_pre.font.size = Pt(11)
        r_pre.font.bold = True
        r_pre.font.color.rgb = RGBColor(0x1E, 0x3A, 0x8A)
        
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(11)
    r.font.color.rgb = RGBColor(0x1F, 0x29, 0x37)
    return p

def add_bullet_point(doc, prefix, text, space_after=2):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = 1.15
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    
    r_pre = p.add_run(prefix)
    r_pre.font.name = "Times New Roman"
    r_pre.font.size = Pt(11)
    r_pre.font.bold = True
    r_pre.font.color.rgb = RGBColor(0x0F, 0x17, 0x2A)
    
    r = p.add_run(text)
    r.font.name = "Times New Roman"
    r.font.size = Pt(11)
    r.font.color.rgb = RGBColor(0x1F, 0x29, 0x37)
    return p

def add_image_with_caption(doc, image_path, caption_text, width_inches=6.5):
    if os.path.exists(image_path):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(6)
        p_img.paragraph_format.space_after = Pt(2)
        run_img = p_img.add_run()
        run_img.add_picture(image_path, width=Inches(width_inches))
        
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.paragraph_format.space_before = Pt(2)
        p_cap.paragraph_format.space_after = Pt(8)
        run_cap = p_cap.add_run(caption_text)
        run_cap.font.name = "Times New Roman"
        run_cap.font.size = Pt(9.5)
        run_cap.font.italic = True
        run_cap.font.color.rgb = RGBColor(0x47, 0x55, 0x69)

def add_code_block(doc, code_text, title="SQL DDL SCRIPT"):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    
    cell = table.cell(0, 0)
    cell.width = Inches(6.5)
    set_cell_background(cell, "F8FAFC")
    set_cell_margins(cell, top=100, bottom=100, left=140, right=140)
    set_table_borders(table, color="CBD5E1", sz="4", val="single")
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.05
    
    r_hdr = p.add_run(f"/* {title} */\n")
    r_hdr.font.name = "Consolas"
    r_hdr.font.size = Pt(8.5)
    r_hdr.font.bold = True
    r_hdr.font.color.rgb = RGBColor(0x02, 0x84, 0xC7)
    
    r_code = p.add_run(code_text)
    r_code.font.name = "Consolas"
    r_code.font.size = Pt(8.0)
    r_code.font.color.rgb = RGBColor(0x0F, 0x17, 0x2A)
    
    p_spacer = doc.add_paragraph()
    p_spacer.paragraph_format.space_before = Pt(0)
    p_spacer.paragraph_format.space_after = Pt(4)

def build_humanized_report(output_path="docs/Bao_Cao_Thiet_Ke_CSDL_Va_Thiet_Ke_Cac_Lop_Dich_Vu_Web_Du_Lich.docx"):
    doc = docx.Document()

    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(0.8)
        section.page_width = Inches(8.27)
        section.page_height = Inches(11.69)

    # TRANG BÌA
    p_univ = doc.add_paragraph()
    p_univ.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_univ.paragraph_format.space_before = Pt(0)
    p_univ.paragraph_format.space_after = Pt(2)
    r_univ = p_univ.add_run("BÁO CÁO HỌC PHẦN ĐỒ ÁN CHUYÊN NGÀNH CÔNG NGHỆ THÔNG TIN\n")
    r_univ.font.name = "Times New Roman"
    r_univ.font.size = Pt(12)
    r_univ.font.bold = True
    r_univ.font.color.rgb = RGBColor(0x47, 0x55, 0x69)

    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(8)
    p_title.paragraph_format.space_after = Pt(6)
    r_title = p_title.add_run("THIẾT KẾ CƠ SỞ DỮ LIỆU VÀ CÁC LỚP TẦNG DỊCH VỤ\n"
                              "ĐỀ TÀI: HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH (WEB-DU-LICH)")
    r_title.font.name = "Times New Roman"
    r_title.font.size = Pt(16)
    r_title.font.bold = True
    r_title.font.color.rgb = RGBColor(0x1E, 0x3A, 0x8A)

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_before = Pt(0)
    p_sub.paragraph_format.space_after = Pt(14)
    r_sub = p_sub.add_run("Phân hệ: Vé Máy Bay Nội Địa & Vé Tàu Hỏa Bắc - Nam\n"
                          "Công nghệ sử dụng: Java 17 | Spring Boot 3.2.5 | Spring Data JPA | MS SQL Server")
    r_sub.font.name = "Times New Roman"
    r_sub.font.size = Pt(10.5)
    r_sub.font.italic = True
    r_sub.font.color.rgb = RGBColor(0x02, 0x84, 0xC7)

    p_line = doc.add_paragraph()
    p_line.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_line.paragraph_format.space_after = Pt(12)
    r_line = p_line.add_run("-" * 50)
    r_line.font.color.rgb = RGBColor(0xCB, 0xD5, 0xE1)

    add_callout(doc, [
        "Tên đề tài: Cổng Đặt Vé Du Lịch Đa Phương Tiện (Web-Du-Lich / VietTravel Hub)",
        "Nội dung 1: Thiết kế Cơ sở dữ liệu (4 bảng tiếng Việt cốt lõi: NguoiDung, DonDatCho, Ve, GiaoDich; chuẩn hóa 3NF; liên kết 1-N tinh gọn; tích hợp lịch hẹn tư vấn sau khi đặt vé)",
        "Nội dung 2: Thiết kế các lớp dịch vụ (Service Layer trong Spring Boot, Strategy Pattern cho 2 loại vé, sơ đồ lớp UML, xử lý transaction và scheduler)",
        "Môi trường triển khai: Java 17, Spring Boot 3.2.5, Hibernate, Microsoft SQL Server Developer Edition"
    ], title="HỒ SƠ ĐỒ ÁN")

    # LỜI MỞ ĐẦU
    add_heading_1(doc, "LỜI MỞ ĐẦU")
    add_paragraph(doc,
        "Báo cáo này trình bày hai nội dung kỹ thuật trọng tâm trong đồ án tốt nghiệp xây dựng website đặt vé du lịch (Web-Du-Lich): "
        "thiết kế cơ sở dữ liệu quan hệ và thiết kế các lớp tầng dịch vụ (Service Layer).")
    
    add_paragraph(doc,
        "Dự án tập trung vào hai hình thức vận chuyển gồm vé máy bay nội địa và vé tàu hỏa Thống Nhất Bắc - Nam. "
        "Toàn bộ ứng dụng viết bằng Java 17, Spring Boot 3.2.5, Spring Data JPA, bảo mật với Spring Security 6 và lưu trữ dữ liệu trên Microsoft SQL Server. "
        "Giao diện hiển thị dùng Thymeleaf kết hợp Bootstrap.")

    add_paragraph(doc,
        "Nội dung báo cáo bám sát mã nguồn thực tế của dự án, chia làm hai phần chính:")
    add_bullet_point(doc, "Phần 1: Thiết kế Cơ sở dữ liệu. ", "Mô tả cấu trúc 4 bảng cốt lõi đặt tên thuần Việt (NguoiDung, DonDatCho, Ve, GiaoDich), sơ đồ quan hệ chuẩn hóa, phân tích nghiệp vụ tích hợp lịch hẹn tư vấn trực tiếp vào đơn đặt vé sau khi đặt vé thành công giúp tối ưu hóa CSDL từ 5 bảng xuống 4 bảng, từ điển dữ liệu, phân tích 3NF, ràng buộc khóa ngoại, chỉ mục và mã DDL tạo bảng.")
    add_bullet_point(doc, "Phần 2: Thiết kế Các lớp Dịch vụ. ", "Mô tả kiến trúc phân tầng trong Spring Boot và sơ đồ lớp UML. Trình bày chi tiết các interface cùng class cài đặt (TicketCatalogService, ReservationService, PaymentService, AppointmentService, QrCodeService, EmailService, UserDetailsServiceImpl), Strategy Pattern tính giá vé và xử lý giao dịch.")

    # PHẦN 1
    add_heading_1(doc, "PHẦN 1: THIẾT KẾ CƠ SỞ DỮ LIỆU CHO ĐỀ TÀI")
    
    add_heading_2(doc, "1.1. Mục Tiêu Thiết Kế Và Cấu Trúc 4 Bảng Cốt Lõi Tinh Gọn")
    add_paragraph(doc,
        "CSDL của dự án lưu trên hệ quản trị Microsoft SQL Server. Theo quy trình nghiệp vụ đặt vé thực tế, khách hàng chỉ phát sinh nhu cầu đặt lịch hẹn tư vấn sau khi đã hoàn tất thao tác đặt chỗ hoặc mua vé (để được chuyên viên hỗ trợ các dịch vụ gia tăng như nâng hạng ghế, mua thêm hành lý ký gửi, đặt suất ăn đặc biệt trên tàu/máy bay, hỗ trợ check-in sớm hoặc xuất hóa đơn VAT đoàn). "
        "Việc duy trì bảng LichHen riêng lẻ như thiết kế ban đầu gây phân mảnh và dư thừa thuộc tính (lặp lại họ tên, số điện thoại, email khách hàng). "
        "Do đó, hệ thống đã tinh giản tối ưu CSDL xuống đúng 4 bảng cốt lõi, đưa toàn bộ thuộc tính lịch hẹn tư vấn vào trực tiếp bảng DonDatCho:")
    
    add_bullet_point(doc, "1. Bảng NguoiDung: ", "Lưu tài khoản đăng nhập, mật khẩu băm BCrypt, thông tin liên hệ và phân quyền theo 4 vai trò: khách cá nhân (ROLE_TRAVELER), đại lý (ROLE_AGENT), nhân viên điều hành (ROLE_OPERATOR) và quản trị viên (ROLE_ADMIN).")
    add_bullet_point(doc, "2. Bảng DonDatCho (Tích hợp Tư vấn sau đặt vé): ", "Lưu đơn đặt giữ chỗ của khách cho cả vé máy bay và vé tàu hỏa. Mỗi đơn có thời hạn giữ chỗ 15 phút, lưu tổng tiền cần thanh toán và các trạng thái đơn; đồng thời tích hợp trực tiếp thông tin lịch hẹn tư vấn (cờ co_hen_tu_van, ngày hẹn ngay_hen_tu_van, nội dung yêu cầu ghi_chu_tu_van, trạng thái trang_thai_tu_van và chuyên viên phụ trách nhan_vien_tu_van_id).")
    add_bullet_point(doc, "3. Bảng Ve: ", "Vừa làm kho chỗ ngồi mở bán, vừa lưu chi tiết vé của từng hành khách sau khi đặt. Mỗi dòng lưu số hiệu chuyến (VN210, SE1), số ghế hoặc vị trí giường nằm, thông tin CCCD hành khách, giá gốc, thuế phí sân bay hoặc bảo hiểm đường sắt, mã vé điện tử và chuỗi ảnh QR Code Base64.")
    add_bullet_point(doc, "4. Bảng GiaoDich: ", "Lưu nhật ký các lần thanh toán qua VNPAY, thẻ hoặc chuyển khoản ngân hàng, đồng thời ghi nhận các khoản hoàn tiền khi khách hủy vé để đối soát.")

    # 1.2 ERD
    add_heading_2(doc, "1.2. Sơ Đồ Cơ Sở Dữ Liệu Quan Hệ (ERD)")
    add_paragraph(doc,
        "Hình 1.1 là sơ đồ quan hệ 4 bảng cốt lõi đặt tên tiếng Việt. Sơ đồ được thiết kế cân đối, rõ ràng, "
        "mỗi bảng chỉ có các liên kết 1-N mạch lạc, không có liên kết chéo hay bảng mồ côi:")

    add_image_with_caption(doc, "docs/erd_database_diagram_vn.png",
                           "Hình 1.1: Sơ đồ Cơ sở dữ liệu 4 bảng chuẩn hóa (NguoiDung, DonDatCho, Ve, GiaoDich)", width_inches=6.3)

    add_heading_3(doc, "Lược Đồ Quan Hệ Hình Thức (Relational Schema):")
    add_paragraph(doc, "Cấu trúc quan hệ chuẩn biểu diễn bằng văn bản (khóa chính gạch chân, khóa ngoại in nghiêng):")
    add_bullet_point(doc, "• NguoiDung (", "id, ten_dang_nhap, mat_khau, ho_ten, email, so_dien_thoai, vai_tro, trang_thai, ngay_tao)")
    add_bullet_point(doc, "• DonDatCho (", "id, ma_dat_cho, nguoi_dung_id, ten_khach_hang, so_dien_thoai, email, loai_phuong_tien, diem_di, diem_den, ngay_di, ngay_ve, la_khu_hoi, so_luong_khach, tong_tien, trang_thai, han_giu_cho, ly_do_huy, co_hen_tu_van, ngay_hen_tu_van, ghi_chu_tu_van, trang_thai_tu_van, nhan_vien_tu_van_id, ngay_tao, ngay_cap_nhat)")
    add_bullet_point(doc, "• Ve (", "id, don_dat_cho_id, loai_phuong_tien, so_hieu_chuyen, ten_phuong_tien, tuyen_duong, diem_di, diem_den, gio_di, gio_den, so_ghe, hang_ve, ten_hanh_khach, so_cccd, loai_khach, gia_goc, phi_dich_vu, phu_phi, gia_cuoi, ma_ve_dien_tu, du_lieu_qr, trang_thai, ngay_tao)")
    add_bullet_point(doc, "• GiaoDich (", "id, don_dat_cho_id, ma_giao_dich, so_tien, phuong_thuc, trang_thai, ma_tham_chieu, ghi_chu, thoi_gian_giao_dich)")

    # 1.3 Data Dictionary
    add_heading_2(doc, "1.3. Từ Điển Dữ Liệu Chi Tiết")
    add_paragraph(doc,
        "Phần này mô tả chi tiết từng cột trong 4 bảng tiếng Việt, bao gồm kiểu dữ liệu trong SQL Server, các ràng buộc và ý nghĩa sử dụng.")

    # Table 1: NguoiDung
    add_heading_3(doc, "1.3.1. Bảng NguoiDung (Tài khoản và phân quyền)")
    headers_users = ["STT", "Tên Cột", "Kiểu Dữ Liệu", "Ràng Buộc", "Mặc Định", "Mô Tả Nghiệp Vụ"]
    data_users = [
        ["1", "id", "BIGINT", "PK, IDENTITY(1,1)", "Tự tăng", "Khóa chính của người dùng."],
        ["2", "ten_dang_nhap", "VARCHAR(50)", "NOT NULL, UNIQUE", "Không", "Tên đăng nhập, không trùng nhau."],
        ["3", "mat_khau", "VARCHAR(255)", "NOT NULL", "Không", "Mật khẩu đã băm bằng thuật toán BCrypt."],
        ["4", "ho_ten", "NVARCHAR(100)", "NOT NULL", "Không", "Họ và tên người dùng."],
        ["5", "email", "VARCHAR(100)", "NOT NULL, UNIQUE", "Không", "Email nhận vé điện tử, không trùng lặp."],
        ["6", "so_dien_thoai", "VARCHAR(20)", "NULL", "Không", "Số điện thoại liên hệ."],
        ["7", "vai_tro", "VARCHAR(30)", "NOT NULL, CHECK", "'ROLE_TRAVELER'", "Vai trò: ROLE_TRAVELER, ROLE_AGENT, ROLE_OPERATOR, ROLE_ADMIN."],
        ["8", "trang_thai", "BIT", "NOT NULL", "1", "Trạng thái tài khoản (1: hoạt động, 0: khóa)."],
        ["9", "ngay_tao", "DATETIME2", "NOT NULL", "GETDATE()", "Ngày giờ tạo tài khoản."]
    ]
    widths_dict = [Inches(0.4), Inches(1.2), Inches(1.2), Inches(1.3), Inches(0.8), Inches(1.6)]
    add_styled_table(doc, headers_users, data_users, widths_dict)

    # Table 2: DonDatCho (Tích hợp Lịch hẹn tư vấn)
    add_heading_3(doc, "1.3.2. Bảng DonDatCho (Đơn đặt giữ chỗ & Tích hợp Lịch hẹn tư vấn)")
    headers_res = ["STT", "Tên Cột", "Kiểu Dữ Liệu", "Ràng Buộc", "Mặc Định", "Mô Tả Nghiệp Vụ"]
    data_res = [
        ["1", "id", "BIGINT", "PK, IDENTITY(1,1)", "Tự tăng", "Khóa chính đơn đặt chỗ."],
        ["2", "ma_dat_cho", "VARCHAR(30)", "NOT NULL, UNIQUE", "Không", "Mã đặt chỗ (RES-yyyyMM-XXXXXX) để tra cứu."],
        ["3", "nguoi_dung_id", "BIGINT", "FK -> NguoiDung(id), NULL", "Không", "Khóa ngoại trỏ về tài khoản đặt (NULL với khách vãng lai)."],
        ["4", "ten_khach_hang", "NVARCHAR(100)", "NOT NULL", "Không", "Họ tên người đại diện liên hệ."],
        ["5", "so_dien_thoai", "VARCHAR(20)", "NOT NULL", "Không", "Số điện thoại dùng để tra cứu đơn đặt chỗ."],
        ["6", "email", "VARCHAR(100)", "NOT NULL", "Không", "Email nhận vé điện tử."],
        ["7", "loai_phuong_tien", "VARCHAR(20)", "NOT NULL, CHECK", "Không", "Loại vé: 'AIR' hoặc 'TRAIN'."],
        ["8", "diem_di", "NVARCHAR(100)", "NOT NULL", "Không", "Sân bay hoặc ga khởi hành."],
        ["9", "diem_den", "NVARCHAR(100)", "NOT NULL", "Không", "Sân bay hoặc ga đến."],
        ["10", "ngay_di", "DATETIME2", "NOT NULL", "Không", "Ngày giờ khởi hành."],
        ["11", "ngay_ve", "DATETIME2", "NULL", "Không", "Ngày về (nếu là vé khứ hồi)."],
        ["12", "la_khu_hoi", "BIT", "NOT NULL", "0", "0: một chiều, 1: khứ hồi."],
        ["13", "so_luong_khach", "INT", "NOT NULL, CHECK", "1", "Số lượng hành khách (phải >= 1)."],
        ["14", "tong_tien", "DECIMAL(18,2)", "NOT NULL", "0", "Tổng số tiền cần thanh toán của cả đơn."],
        ["15", "trang_thai", "VARCHAR(20)", "NOT NULL, CHECK", "'CREATED'", "Trạng thái: CREATED (giữ chỗ), CONFIRMED (đã trả tiền), CANCELLED, EXPIRED."],
        ["16", "han_giu_cho", "DATETIME2", "NOT NULL", "Không", "Hạn giữ chỗ (sau 15 phút từ lúc đặt)."],
        ["17", "ly_do_huy", "NVARCHAR(255)", "NULL", "Không", "Lý do hủy đơn."],
        ["18", "co_hen_tu_van", "BIT", "NOT NULL", "0", "Đánh dấu đơn vé có yêu cầu tư vấn (1: có, 0: không)."],
        ["19", "ngay_hen_tu_van", "DATETIME2", "NULL", "Không", "Thời gian hẹn tư vấn khách hàng mong muốn."],
        ["20", "ghi_chu_tu_van", "NVARCHAR(500)", "NULL", "Không", "Nội dung cần chuyên viên hỗ trợ sau khi đặt vé."],
        ["21", "trang_thai_tu_van", "VARCHAR(20)", "NOT NULL, CHECK", "'NONE'", "Trạng thái tư vấn: NONE, PENDING, CONFIRMED, COMPLETED, CANCELLED."],
        ["22", "nhan_vien_tu_van_id", "BIGINT", "NULL", "Không", "Mã chuyên viên điều hành tiếp nhận tư vấn (liên kết logic)."],
        ["23", "ngay_tao", "DATETIME2", "NOT NULL", "GETDATE()", "Ngày giờ tạo đơn đặt."],
        ["24", "ngay_cap_nhat", "DATETIME2", "NULL", "Không", "Ngày giờ cập nhật trạng thái đơn gần nhất."]
    ]
    add_styled_table(doc, headers_res, data_res, widths_dict)

    # Table 3: Ve
    add_heading_3(doc, "1.3.3. Bảng Ve (Kho vé và chi tiết vé hành khách)")
    headers_tkt = ["STT", "Tên Cột", "Kiểu Dữ Liệu", "Ràng Buộc", "Mặc Định", "Mô Tả Nghiệp Vụ"]
    data_tkt = [
        ["1", "id", "BIGINT", "PK, IDENTITY(1,1)", "Tự tăng", "Khóa chính của vé."],
        ["2", "don_dat_cho_id", "BIGINT", "FK -> DonDatCho(id)", "NULL", "Khóa ngoại trỏ về đơn đặt (NULL nếu vé còn trong kho)."],
        ["3", "loai_phuong_tien", "VARCHAR(20)", "NOT NULL, CHECK", "Không", "Loại phương tiện: 'AIR' hoặc 'TRAIN'."],
        ["4", "so_hieu_chuyen", "VARCHAR(50)", "NOT NULL", "Không", "Số hiệu chuyến bay (VN210) hoặc số hiệu tàu (SE1)."],
        ["5", "ten_phuong_tien", "NVARCHAR(100)", "NOT NULL", "Không", "Hãng bay hoặc tên đoàn tàu Thống Nhất."],
        ["6", "tuyen_duong", "NVARCHAR(200)", "NOT NULL", "Không", "Thông tin tuyến: Hà Nội - Sài Gòn..."],
        ["7", "diem_di", "NVARCHAR(100)", "NOT NULL", "Không", "Tên sân bay hoặc ga xuất phát."],
        ["8", "diem_den", "NVARCHAR(100)", "NOT NULL", "Không", "Tên sân bay hoặc ga đến."],
        ["9", "gio_di", "DATETIME2", "NOT NULL", "Không", "Ngày giờ xuất phát."],
        ["10", "gio_den", "DATETIME2", "NOT NULL", "Không", "Ngày giờ đến nơi."],
        ["11", "so_ghe", "NVARCHAR(50)", "NOT NULL", "Không", "Số ghế máy bay (12A) hoặc vị trí toa tàu (Toa 4 - Giường 11)."],
        ["12", "hang_ve", "VARCHAR(50)", "NOT NULL", "Không", "Hạng chỗ: ECONOMY, BUSINESS, SOFT_SEAT, SLEEPER_4."],
        ["13", "ten_hanh_khach", "NVARCHAR(100)", "NULL", "Không", "Họ tên hành khách in trên vé."],
        ["14", "so_cccd", "VARCHAR(30)", "NULL", "Không", "Số CCCD hoặc số hộ chiếu."],
        ["15", "loai_khach", "VARCHAR(20)", "NOT NULL, CHECK", "'ADULT'", "Loại khách: ADULT, CHILD, INFANT, STUDENT, SENIOR."],
        ["16", "gia_goc", "DECIMAL(18,2)", "NOT NULL", "0", "Giá cước gốc của chỗ ngồi."],
        ["17", "phi_dich_vu", "DECIMAL(18,2)", "NOT NULL", "0", "Thuế sân bay (120k) hoặc bảo hiểm đường sắt (10k)."],
        ["18", "phu_phi", "DECIMAL(18,2)", "NOT NULL", "0", "Phí hành lý thêm hoặc suất ăn."],
        ["19", "gia_cuoi", "DECIMAL(18,2)", "NOT NULL", "0", "Giá tiền thực thu sau khi tính đối tượng và phụ phí."],
        ["20", "ma_ve_dien_tu", "VARCHAR(50)", "NULL", "Không", "Mã vé điện tử (ETK-AIR-... hoặc ETK-TRN-...)."],
        ["21", "du_lieu_qr", "NVARCHAR(MAX)", "NULL", "Không", "Chuỗi ảnh Base64 chứa mã QR Code để soát vé."],
        ["22", "trang_thai", "VARCHAR(20)", "NOT NULL, CHECK", "'AVAILABLE'", "Trạng thái: AVAILABLE (trong kho), HELD, ISSUED, CANCELLED."],
        ["23", "ngay_tao", "DATETIME2", "NOT NULL", "GETDATE()", "Ngày giờ tạo vé."]
    ]
    add_styled_table(doc, headers_tkt, data_tkt, widths_dict)

    # Table 5: GiaoDich
    add_heading_3(doc, "1.3.4. Bảng GiaoDich (Lịch sử giao dịch thanh toán)")
    headers_trn = ["STT", "Tên Cột", "Kiểu Dữ Liệu", "Ràng Buộc", "Mặc Định", "Mô Tả Nghiệp Vụ"]
    data_trn = [
        ["1", "id", "BIGINT", "PK, IDENTITY(1,1)", "Tự tăng", "Khóa chính bản ghi thanh toán."],
        ["2", "don_dat_cho_id", "BIGINT", "FK -> DonDatCho(id)", "Không", "Đơn đặt vé cần thanh toán."],
        ["3", "ma_giao_dich", "VARCHAR(50)", "NOT NULL, UNIQUE", "Không", "Mã giao dịch (PAY-yyMMdd-XXXXXX hoặc REFUND-...)."],
        ["4", "so_tien", "DECIMAL(18,2)", "NOT NULL", "Không", "Số tiền thanh toán hoặc tiền hoàn."],
        ["5", "phuong_thuc", "VARCHAR(30)", "NOT NULL, CHECK", "Không", "Hình thức: VNPAY, BANK_TRANSFER, CREDIT_CARD, CASH."],
        ["6", "trang_thai", "VARCHAR(20)", "NOT NULL, CHECK", "'SUCCESS'", "Trạng thái: PENDING, SUCCESS, FAILED, REFUNDED."],
        ["7", "ma_tham_chieu", "VARCHAR(100)", "NULL", "Không", "Mã giao dịch ngân hàng hoặc cổng VNPAY."],
        ["8", "ghi_chu", "NVARCHAR(255)", "NULL", "Không", "Ghi chú giao dịch."],
        ["9", "thoi_gian_giao_dich", "DATETIME2", "NOT NULL", "GETDATE()", "Ngày giờ thanh toán."]
    ]
    add_styled_table(doc, headers_trn, data_trn, widths_dict)

    # 1.4 Normalization
    add_heading_2(doc, "1.4. Phân Tích Chuẩn Hóa Cơ Sở Dữ Liệu (1NF, 2NF, 3NF)")
    add_paragraph(doc,
        "CSDL tuân thủ các nguyên tắc chuẩn hóa để tránh dư thừa và không gây lỗi khi thêm, sửa hoặc xóa dữ liệu:")
    
    add_bullet_point(doc, "Chuẩn 1NF: ", "Mọi cột đều chứa giá trị nguyên tử, không có kiểu mảng hay danh sách ghép chung trong một ô. Khi một đơn có nhiều khách đi chung, hệ thống không lưu danh sách tên khách vào bảng DonDatCho mà tách riêng thành từng bản ghi trong bảng Ve. Mỗi bảng đều có khóa chính id duy nhất.")
    add_bullet_point(doc, "Chuẩn 2NF: ", "Đã đạt 1NF và tất cả 4 bảng đều dùng khóa chính đơn (cột id tự tăng). Không có bảng nào dùng khóa ghép hai cột trở lên, do đó không thể tồn tại phụ thuộc hàm bộ phận vào một phần của khóa chính. Mọi cột trong bảng đều phụ thuộc vào toàn bộ khóa chính id.")
    add_bullet_point(doc, "Chuẩn 3NF: ", "Đã đạt 2NF và không có phụ thuộc hàm bắc cầu giữa các thuộc tính không khóa. Bảng DonDatCho chỉ lưu nguoi_dung_id, các thông tin tên đăng nhập, email hay mật khẩu lưu riêng ở bảng NguoiDung. Bảng GiaoDich tách riêng để quản lý nhiều lần thanh toán hoặc hoàn tiền mà không làm nhân đôi dòng dữ liệu của đơn đặt. CSDL đạt chuẩn 3NF.")

    # 1.5 Relationships
    add_heading_2(doc, "1.5. Mối Quan Hệ Giữa Các Bảng Và Ràng Buộc Khóa Ngoại")
    add_paragraph(doc,
        "Các bảng liên kết với nhau bằng đúng 4 khóa ngoại mạch lạc kèm quy định xử lý khi xóa dữ liệu (ON DELETE):")

    headers_rel = ["Bảng Cha", "Bảng Con", "Cột Khóa Ngoại", "Quan Hệ", "Khi Xóa Bảng Cha (ON DELETE)", "Ý Nghĩa Thực Tế"]
    data_rel = [
        ["NguoiDung", "DonDatCho", "nguoi_dung_id", "1 - N", "NO ACTION", "Duy nhất 1 liên kết giữa NguoiDung và DonDatCho. Khách hàng tạo đơn giữ chỗ (NULL nếu là khách vãng lai)."],
        ["DonDatCho", "Ve", "don_dat_cho_id", "1 - N", "ON DELETE SET NULL", "Một đơn giữ chỗ gồm nhiều vé. Khi hủy hoặc xóa đơn, các vé này được trả về kho (don_dat_cho_id thành NULL, trạng thái thành AVAILABLE) để mở bán tiếp."],
        ["DonDatCho", "GiaoDich", "don_dat_cho_id", "1 - N", "ON DELETE CASCADE", "Một đơn có thể có lịch sử thanh toán và hoàn tiền. Khi xóa đơn đặt, lịch sử thanh toán tương ứng tự động xóa theo để không còn bản ghi mồ côi."]
    ]
    widths_rel = [Inches(1.0), Inches(1.1), Inches(1.2), Inches(0.7), Inches(1.2), Inches(1.3)]
    add_styled_table(doc, headers_rel, data_rel, widths_rel)

    # 1.6 Indexing
    add_heading_2(doc, "1.6. Chiến Lược Đánh Chỉ Mục (Indexes)")
    add_paragraph(doc,
        "Hệ thống tạo các chỉ mục để tăng tốc độ tìm kiếm vé và tra cứu đơn đặt chỗ:")
    
    headers_idx = ["Tên Chỉ Mục", "Bảng", "Các Cột Đánh Chỉ Mục", "Loại Chỉ Mục", "Mục Đích Sử Dụng"]
    data_idx = [
        ["PK_NguoiDung / DonDatCho...", "Cả 4 bảng", "id", "Clustered", "Khóa chính tự tăng, sắp xếp dữ liệu vật lý trên ổ cứng."],
        ["IX_Ve_search", "Ve", "loai_phuong_tien, diem_di, diem_den, gio_di, trang_thai", "Composite Non-Clustered", "Chỉ mục quan trọng nhất cho bộ lọc tìm vé máy bay và tàu hỏa tại trang chủ, giúp truy vấn dùng Index Seek thay vì quét toàn bảng."],
        ["IX_DonDatCho_ma", "DonDatCho", "ma_dat_cho", "Unique Non-Clustered", "Tra cứu đơn đặt chỗ theo mã booking với tốc độ nhanh."],
        ["IX_DonDatCho_sdt", "DonDatCho", "so_dien_thoai", "Non-Clustered", "Phục vụ khách vãng lai tra cứu danh sách vé bằng số điện thoại."],
        ["IX_DonDatCho_status", "DonDatCho", "trang_thai", "Non-Clustered", "Hỗ trợ tác vụ nền quét nhanh các đơn đang giữ chỗ quá 15 phút."],
        ["IX_Ve_ma_ve", "Ve", "ma_ve_dien_tu", "Non-Clustered", "Tra cứu vé khi khách quét mã QR lên tàu hoặc máy bay."],
        ["IX_GiaoDich_ma", "GiaoDich", "ma_giao_dich", "Unique Non-Clustered", "Tra cứu và đối soát giao dịch ngân hàng theo mã thanh toán."]
    ]
    widths_idx = [Inches(1.5), Inches(0.9), Inches(1.8), Inches(1.1), Inches(1.2)]
    add_styled_table(doc, headers_idx, data_idx, widths_idx)

    # 1.7 DDL
    add_heading_2(doc, "1.7. Kịch Bản DDL Tạo Bảng Trong SQL Server (Tiếng Việt)")
    add_paragraph(doc, "Đoạn mã DDL dưới đây dùng để khởi tạo toàn bộ CSDL DatVeDuLich cùng các ràng buộc và chỉ mục:")

    sql_ddl_sample = """﻿-- ==========================================================================================
-- HỆ THỐNG QUẢN LÝ ĐẶT VÉ DU LỊCH (TRAVEL TICKET BOOKING & MANAGEMENT PORTAL)
-- CSDL: Microsoft SQL Server (DatVeDuLich)
-- CẤU TRÚC: CHUẨN 4 BẢNG CỐT LÕI (NguoiDung, DonDatCho, Ve, GiaoDich)
-- Lịch hẹn tư vấn được tích hợp trực tiếp vào Đơn Đặt Chỗ (chỉ xuất hiện sau khi đặt vé xong)
-- Toàn bộ tên bảng và trường thuộc tính chuẩn hóa 100% Tiếng Việt
-- ==========================================================================================

USE DatVeDuLich;
GO

-- 1. XÓA BẢNG NẾU ĐÃ TỒN TẠI (Theo thứ tự khóa ngoại)
IF OBJECT_ID('dbo.GiaoDich', 'U') IS NOT NULL DROP TABLE dbo.GiaoDich;
IF OBJECT_ID('dbo.trans_details', 'U') IS NOT NULL DROP TABLE dbo.trans_details;
IF OBJECT_ID('dbo.payments', 'U') IS NOT NULL DROP TABLE dbo.payments;

IF OBJECT_ID('dbo.Ve', 'U') IS NOT NULL DROP TABLE dbo.Ve;
IF OBJECT_ID('dbo.tickets', 'U') IS NOT NULL DROP TABLE dbo.tickets;
IF OBJECT_ID('dbo.ticket_details', 'U') IS NOT NULL DROP TABLE dbo.ticket_details;
IF OBJECT_ID('dbo.flight_passengers', 'U') IS NOT NULL DROP TABLE dbo.flight_passengers;
IF OBJECT_ID('dbo.train_tickets', 'U') IS NOT NULL DROP TABLE dbo.train_tickets;

IF OBJECT_ID('dbo.LichHen', 'U') IS NOT NULL DROP TABLE dbo.LichHen;
IF OBJECT_ID('dbo.appointments', 'U') IS NOT NULL DROP TABLE dbo.appointments;
IF OBJECT_ID('dbo.consultations', 'U') IS NOT NULL DROP TABLE dbo.consultations;

IF OBJECT_ID('dbo.DonDatCho', 'U') IS NOT NULL DROP TABLE dbo.DonDatCho;
IF OBJECT_ID('dbo.reservations', 'U') IS NOT NULL DROP TABLE dbo.reservations;
IF OBJECT_ID('dbo.bookings', 'U') IS NOT NULL DROP TABLE dbo.bookings;
IF OBJECT_ID('dbo.flight_bookings', 'U') IS NOT NULL DROP TABLE dbo.flight_bookings;
IF OBJECT_ID('dbo.train_bookings', 'U') IS NOT NULL DROP TABLE dbo.train_bookings;

IF OBJECT_ID('dbo.NguoiDung', 'U') IS NOT NULL DROP TABLE dbo.NguoiDung;
IF OBJECT_ID('dbo.users', 'U') IS NOT NULL DROP TABLE dbo.users;
IF OBJECT_ID('dbo.customers', 'U') IS NOT NULL DROP TABLE dbo.customers;
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 1: NguoiDung (Tài khoản người dùng, phân quyền 4 vai trò RBAC)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.NguoiDung (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(255) NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    so_dien_thoai VARCHAR(20) NULL,
    vai_tro VARCHAR(30) NOT NULL CONSTRAINT DF_NguoiDung_vai_tro DEFAULT 'ROLE_TRAVELER',
    trang_thai BIT NOT NULL CONSTRAINT DF_NguoiDung_trang_thai DEFAULT 1,
    ngay_tao DATETIME2 NOT NULL CONSTRAINT DF_NguoiDung_ngay_tao DEFAULT GETDATE(),

    CONSTRAINT CK_NguoiDung_vai_tro CHECK (vai_tro IN ('ROLE_TRAVELER', 'ROLE_AGENT', 'ROLE_OPERATOR', 'ROLE_ADMIN'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 2: DonDatCho (Đơn đặt giữ chỗ cho Vé Máy Bay & Vé Tàu Hỏa, tích hợp Lịch Hẹn Tư Vấn)
-- Lịch hẹn tư vấn chỉ xuất hiện sau khi khách đặt vé thành công, tinh gọn CSDL xuống 4 bảng
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.DonDatCho (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_dat_cho VARCHAR(30) NOT NULL UNIQUE,
    nguoi_dung_id BIGINT NULL,
    ten_khach_hang NVARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    loai_phuong_tien VARCHAR(20) NOT NULL,
    diem_di NVARCHAR(100) NOT NULL,
    diem_den NVARCHAR(100) NOT NULL,
    ngay_di DATETIME2 NOT NULL,
    ngay_ve DATETIME2 NULL,
    la_khu_hoi BIT NOT NULL CONSTRAINT DF_DonDatCho_khu_hoi DEFAULT 0,
    so_luong_khach INT NOT NULL CONSTRAINT DF_DonDatCho_so_khach DEFAULT 1,
    tong_tien DECIMAL(18,2) NOT NULL CONSTRAINT DF_DonDatCho_tong_tien DEFAULT 0,
    trang_thai VARCHAR(20) NOT NULL CONSTRAINT DF_DonDatCho_trang_thai DEFAULT 'CREATED',
    han_giu_cho DATETIME2 NOT NULL,
    ly_do_huy NVARCHAR(255) NULL,

    -- Thuộc tính Tích hợp Lịch Hẹn Tư Vấn sau khi đặt vé
    co_hen_tu_van BIT NOT NULL CONSTRAINT DF_DonDatCho_co_hen DEFAULT 0,
    ngay_hen_tu_van DATETIME2 NULL,
    ghi_chu_tu_van NVARCHAR(500) NULL,
    trang_thai_tu_van VARCHAR(20) NOT NULL CONSTRAINT DF_DonDatCho_tt_tu_van DEFAULT 'NONE',
    nhan_vien_tu_van_id BIGINT NULL,

    ngay_tao DATETIME2 NOT NULL CONSTRAINT DF_DonDatCho_ngay_tao DEFAULT GETDATE(),
    ngay_cap_nhat DATETIME2 NULL,

    CONSTRAINT FK_DonDatCho_NguoiDung FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.NguoiDung(id),
    CONSTRAINT CK_DonDatCho_phuong_tien CHECK (loai_phuong_tien IN ('AIR', 'TRAIN')),
    CONSTRAINT CK_DonDatCho_trang_thai CHECK (trang_thai IN ('CREATED', 'CONFIRMED', 'CANCELLED', 'EXPIRED')),
    CONSTRAINT CK_DonDatCho_trang_thai_tu_van CHECK (trang_thai_tu_van IN ('NONE', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_DonDatCho_so_khach CHECK (so_luong_khach >= 1)
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 3: Ve (Kho vé khả dụng & Chi tiết vé/ghế đã đặt của từng hành khách)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.Ve (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    don_dat_cho_id BIGINT NULL,
    loai_phuong_tien VARCHAR(20) NOT NULL,
    so_hieu_chuyen VARCHAR(50) NOT NULL,
    ten_phuong_tien NVARCHAR(100) NOT NULL,
    tuyen_duong NVARCHAR(200) NOT NULL,
    diem_di NVARCHAR(100) NOT NULL,
    diem_den NVARCHAR(100) NOT NULL,
    gio_di DATETIME2 NOT NULL,
    gio_den DATETIME2 NOT NULL,
    so_ghe NVARCHAR(50) NOT NULL,
    hang_ve VARCHAR(50) NOT NULL,
    ten_hanh_khach NVARCHAR(100) NULL,
    so_cccd VARCHAR(30) NULL,
    loai_khach VARCHAR(20) NOT NULL CONSTRAINT DF_Ve_loai_khach DEFAULT 'ADULT',
    gia_goc DECIMAL(18,2) NOT NULL DEFAULT 0,
    phi_dich_vu DECIMAL(18,2) NOT NULL DEFAULT 0,
    phu_phi DECIMAL(18,2) NOT NULL DEFAULT 0,
    gia_cuoi DECIMAL(18,2) NOT NULL DEFAULT 0,
    ma_ve_dien_tu VARCHAR(50) NULL,
    du_lieu_qr NVARCHAR(MAX) NULL,
    trang_thai VARCHAR(20) NOT NULL CONSTRAINT DF_Ve_trang_thai DEFAULT 'AVAILABLE',
    ngay_tao DATETIME2 NOT NULL CONSTRAINT DF_Ve_ngay_tao DEFAULT GETDATE(),

    CONSTRAINT FK_Ve_DonDatCho FOREIGN KEY (don_dat_cho_id) REFERENCES dbo.DonDatCho(id) ON DELETE SET NULL,
    CONSTRAINT CK_Ve_phuong_tien CHECK (loai_phuong_tien IN ('AIR', 'TRAIN')),
    CONSTRAINT CK_Ve_loai_khach CHECK (loai_khach IN ('ADULT', 'CHILD', 'INFANT', 'SENIOR', 'STUDENT')),
    CONSTRAINT CK_Ve_trang_thai CHECK (trang_thai IN ('AVAILABLE', 'HELD', 'ISSUED', 'CANCELLED'))
);
GO

-- ------------------------------------------------------------------------------------------
-- BẢNG 4: GiaoDich (Lịch sử giao dịch thanh toán & đối soát tài chính)
-- ------------------------------------------------------------------------------------------
CREATE TABLE dbo.GiaoDich (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    don_dat_cho_id BIGINT NOT NULL,
    ma_giao_dich VARCHAR(50) NOT NULL UNIQUE,
    so_tien DECIMAL(18,2) NOT NULL,
    phuong_thuc VARCHAR(30) NOT NULL,
    trang_thai VARCHAR(20) NOT NULL CONSTRAINT DF_GiaoDich_trang_thai DEFAULT 'SUCCESS',
    ma_tham_chieu VARCHAR(100) NULL,
    ghi_chu NVARCHAR(255) NULL,
    thoi_gian_giao_dich DATETIME2 NOT NULL CONSTRAINT DF_GiaoDich_thoi_gian DEFAULT GETDATE(),

    CONSTRAINT FK_GiaoDich_DonDatCho FOREIGN KEY (don_dat_cho_id) REFERENCES dbo.DonDatCho(id) ON DELETE CASCADE,
    CONSTRAINT CK_GiaoDich_phuong_thuc CHECK (phuong_thuc IN ('VNPAY', 'BANK_TRANSFER', 'CREDIT_CARD', 'CASH')),
    CONSTRAINT CK_GiaoDich_trang_thai CHECK (trang_thai IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'))
);
GO

-- ------------------------------------------------------------------------------------------
-- TẠO CHỈ MỤC (INDEXES) TỐI ƯU HIỆU NĂNG
-- ------------------------------------------------------------------------------------------
CREATE INDEX IX_DonDatCho_ma_dat_cho ON dbo.DonDatCho(ma_dat_cho);
CREATE INDEX IX_DonDatCho_nguoi_dung_id ON dbo.DonDatCho(nguoi_dung_id);
CREATE INDEX IX_DonDatCho_so_dien_thoai ON dbo.DonDatCho(so_dien_thoai);
CREATE INDEX IX_DonDatCho_trang_thai ON dbo.DonDatCho(trang_thai);
CREATE INDEX IX_DonDatCho_tu_van ON dbo.DonDatCho(co_hen_tu_van, trang_thai_tu_van);

CREATE INDEX IX_Ve_don_dat_cho_id ON dbo.Ve(don_dat_cho_id);
CREATE INDEX IX_Ve_tim_kiem ON dbo.Ve(loai_phuong_tien, diem_di, diem_den, gio_di, trang_thai);
CREATE INDEX IX_Ve_ma_ve_dien_tu ON dbo.Ve(ma_ve_dien_tu);

CREATE INDEX IX_GiaoDich_don_dat_cho_id ON dbo.GiaoDich(don_dat_cho_id);
CREATE INDEX IX_GiaoDich_ma_giao_dich ON dbo.GiaoDich(ma_giao_dich);
GO"""
    add_code_block(doc, sql_ddl_sample, title="SCHEMA.SQL - CSDL DATVEDULICH (TIẾNG VIỆT)")

    # PHẦN 2
    add_heading_1(doc, "PHẦN 2: THIẾT KẾ CÁC LỚP TẦNG DỊCH VỤ")

    add_heading_2(doc, "2.1. Kiến Trúc Tầng Dịch Vụ Trong Spring Boot")
    add_paragraph(doc,
        "Tầng dịch vụ (Service Layer) nằm giữa Controller và Repository. Controller lo việc nhận HTTP request, "
        "kiểm tra form và chuyển dữ liệu cho Service. Service nhận trách nhiệm xử lý logic nghiệp vụ, gọi Repository "
        "để lưu dữ liệu vào CSDL rồi trả kết quả về.")

    add_paragraph(doc,
        "Các nguyên tắc thiết kế trong mã nguồn dự án gồm:")
    add_bullet_point(doc, "Tách Interface và Implementation. ", "Mỗi dịch vụ đều có một interface định nghĩa các hàm nghiệp vụ và một class Impl cài đặt cụ thể. Nhờ vậy các lớp gọi không bị dính chặt vào mã triển khai, dễ dàng thay thế và thuận tiện viết Unit Test với Mockito.")
    add_bullet_point(doc, "Tiêm phụ thuộc qua hàm khởi tạo (Constructor Injection). ", "Không dùng @Autowired trên từng thuộc tính mà dùng constructor để truyền các bean phụ thuộc. Cách viết này giúp các biến là final, không bị lỗi NullPointerException khi test và thấy rõ service cần những gì để chạy.")
    add_bullet_point(doc, "Đảm bảo tính trọn vẹn giao dịch với @Transactional. ", "Những hàm sửa đổi dữ liệu ở nhiều bảng (như đặt vé, xác nhận thanh toán, hoàn tiền) đều gắn @Transactional(rollbackFor = Exception.class). Nếu xảy ra lỗi giữa chừng, toàn bộ các thao tác trước đó sẽ được tự động hoàn tác về trạng thái ban đầu.")
    add_bullet_point(doc, "Dùng Strategy Pattern và Factory Pattern cho giá vé. ", "Quy tắc tính giá vé máy bay và vé tàu hỏa được tách thành hai lớp riêng biệt. Khi có thêm phương tiện mới, chỉ cần viết thêm một class Strategy mới mà không phải đụng vào mã nguồn của ReservationService.")

    # 2.2 Class Diagram
    add_heading_2(doc, "2.2. Sơ Đồ Lớp Tổng Thể Tầng Dịch Vụ")
    add_paragraph(doc,
        "Hình 2.1 là sơ đồ lớp UML biểu diễn mối quan hệ giữa các Controller, Service Interface, Service Implementation, "
        "các lớp Strategy và các Repository trong dự án:")

    add_image_with_caption(doc, "docs/service_class_diagram.png",
                           "Hình 2.1: Sơ đồ lớp UML tầng dịch vụ và mẫu thiết kế Strategy", width_inches=6.3)

    add_paragraph(doc, "Mối liên kết giữa các tầng trong sơ đồ:")
    add_bullet_point(doc, "Tầng Controller. ", "Bao gồm TicketCatalogController, ReservationController, PaymentController, AppointmentController, AdminController. Các controller này chỉ giao tiếp với Service qua interface, không gọi thẳng vào Repository.")
    add_bullet_point(doc, "Tầng Service. ", "Bao gồm các interface và lớp cài đặt ServiceImpl, chịu trách nhiệm xử lý nghiệp vụ, kiểm tra tính hợp lệ và quản lý transaction.")
    add_bullet_point(doc, "Tầng Strategy. ", "Gồm TicketStrategy, AirTicketStrategy, TrainTicketStrategy và TicketStrategyFactory để tính giá và áp dụng chính sách riêng cho từng phương tiện.")
    add_bullet_point(doc, "Tầng Repository. ", "Gồm các interface kế thừa JpaRepository của Spring Data JPA để thao tác với các bảng trong SQL Server.")

    # 2.3 Detailed Services
    add_heading_2(doc, "2.3. Thiết Kế Chi Tiết Các Lớp Dịch Vụ")
    add_paragraph(doc, "Bảng 2.1 đến 2.4 liệt kê chi tiết các interface và phương thức của từng dịch vụ trong dự án:")

    # 2.3.1 TicketCatalogService
    add_heading_3(doc, "2.3.1. TicketCatalogService và TicketCatalogServiceImpl (Quản lý kho vé)")
    add_paragraph(doc, "Dịch vụ tra cứu chuyến đi, tìm kiếm vé khả dụng và lấy sơ đồ chỗ ngồi của chuyến bay hoặc đoàn tàu.")
    
    headers_srv = ["Tên Phương Thức", "Tham Số", "Kiểu Trả Về", "Mô Tả Nghiệp Vụ"]
    data_cat = [
        ["searchTickets", "TicketSearchDto searchDto", "List<Ticket>", "Tìm vé còn bán (AVAILABLE) theo điểm đi, điểm đến, ngày đi và loại phương tiện (AIR/TRAIN). Nếu không thấy chuyến đúng ngày, tự tìm các chuyến gần nhất."],
        ["getTicketById", "Long id", "Ticket", "Lấy thông tin chi tiết một chỗ ngồi theo id. Ném ResourceNotFoundException nếu không có."],
        ["getTicketsByVehicle", "String vehicleCode, LocalDateTime depTime", "List<Ticket>", "Lấy danh sách các chỗ ngồi của một chuyến bay (VN210) hoặc đoàn tàu (SE1) theo khung giờ xuất phát."],
        ["getOccupiedSeats", "String vehicleCode", "List<String>", "Lấy danh sách các vị trí ghế đã có người đặt để vẽ sơ đồ chỗ ngồi trực quan trên giao diện."],
        ["getAvailablePlaces", "TransportType transportType, boolean isDeparture", "List<String>", "Lấy danh mục các sân bay hoặc nhà ga đang có chuyến bay/tàu mở bán."],
        ["generateConsecutiveSeat", "String baseSeat, int index, TransportType transportType", "String", "Thuật toán cấp phát ghế liền nhau khi khách đặt vé đoàn (Ví dụ: ghế 12A -> 12B -> 12C cho máy bay; Toa 4 Giường 1 -> Giường 2 cho tàu)."]
    ]
    widths_srv = [Inches(1.5), Inches(1.5), Inches(1.2), Inches(2.3)]
    add_styled_table(doc, headers_srv, data_cat, widths_srv)

    # 2.3.2 ReservationService
    add_heading_3(doc, "2.3.2. ReservationService và ReservationServiceImpl (Quản lý đặt giữ chỗ)")
    add_paragraph(doc, "Dịch vụ quản lý vòng đời đơn vé từ khi khách giữ chỗ 15 phút, tính tiền, xuất vé điện tử đến khi hủy đơn.")
    
    data_res_srv = [
        ["createHoldReservation", "ReservationRequestDto dto, User currentUser", "Reservation", "Tạo đơn đặt giữ chỗ: sinh mã booking RES-yyyyMM-XXXXXX, gọi Strategy tương ứng để kiểm tra thông tin khách và tính giá vé, chuyển trạng thái vé thành HELD, đặt hạn hold_expires_at sau 15 phút, gửi email xác nhận giữ chỗ."],
        ["getByBookingCode", "String bookingCode", "Reservation", "Lấy thông tin đơn đặt chỗ kèm danh sách vé hành khách và các lần thanh toán."],
        ["confirmAndIssue", "String bookingCode, PaymentMethod method, String transRef, String note", "Reservation", "Xác nhận thanh toán thành công: chuyển trạng thái đơn thành CONFIRMED, đổi vé sang ISSUED, sinh mã vé điện tử ETK-AIR-.../ETK-TRN-..., tạo mã QR Code Base64, ghi bản ghi vào bảng thanh toán, gửi email vé điện tử."],
        ["cancelReservation", "String bookingCode, String reason", "Reservation", "Hủy vé theo yêu cầu: gọi Strategy tính phí phạt hủy vé theo thời gian, trả các vé về trạng thái AVAILABLE trong kho, ghi bản ghi hoàn tiền REFUNDED nếu đơn đã trả tiền trước đó."],
        ["releaseExpiredHoldReservations", "Không có (Scheduled Trigger)", "void", "Tác vụ chạy ngầm định kỳ mỗi 60 giây (@Scheduled): tự động tìm các đơn CREATED đã quá 15 phút mà chưa trả tiền, chuyển trạng thái thành EXPIRED và trả toàn bộ vé về kho AVAILABLE."]
    ]
    add_styled_table(doc, headers_srv, data_res_srv, widths_srv)

    # 2.3.3 PaymentService
    add_heading_3(doc, "2.3.3. PaymentService và PaymentServiceImpl (Xử lý thanh toán)")
    add_paragraph(doc, "Dịch vụ ghi nhận giao dịch thanh toán trực tuyến và đối soát tài chính.")
    
    data_pay_srv = [
        ["processPayment", "PaymentRequestDto dto", "TransDetail", "Xử lý thanh toán: kiểm tra đơn còn trong hạn 15 phút, gọi ReservationService.confirmAndIssue để xuất vé, tạo mã giao dịch PAY-yyMMdd-XXXXXX lưu vào bảng thanh toán với trạng thái SUCCESS."],
        ["getByPaymentCode", "String paymentCode", "TransDetail", "Tìm chứng từ thanh toán theo mã giao dịch."],
        ["getPaymentsByReservation", "Long reservationId", "List<TransDetail>", "Lấy danh sách các lần thanh toán hoặc hoàn tiền của một đơn đặt chỗ."],
        ["getAllPayments", "Không có", "List<TransDetail>", "Lấy toàn bộ giao dịch thanh toán để làm báo cáo thống kê doanh thu cho Admin."]
    ]
    add_styled_table(doc, headers_srv, data_pay_srv, widths_srv)

    # 2.3.4 AppointmentService
    add_heading_3(doc, "2.3.4. AppointmentService và AppointmentServiceImpl (Quản lý lịch hẹn tư vấn)")
    add_paragraph(doc, "Dịch vụ tiếp nhận nhu cầu đặt vé đoàn hoặc tư vấn lịch trình du lịch từ khách hàng.")
    
    data_apt_srv = [
        ["createAppointment", "Appointment appointment", "Appointment", "Tiếp nhận đơn hẹn tư vấn: sinh mã APT-yyMM-XXXXXX, gán trạng thái ban đầu là PENDING."],
        ["findByCode", "String code", "Appointment", "Tra cứu tiến độ cuộc hẹn tư vấn theo mã định danh."],
        ["findByPhone", "String phone", "List<Appointment>", "Khách tra cứu các cuộc hẹn đã đăng ký theo số điện thoại cá nhân."],
        ["updateStatus", "Long id, AppointmentStatus status, Long operatorId", "Appointment", "Ghi nhận nhân viên phụ trách và đổi trạng thái cuộc hẹn (CONFIRMED, COMPLETED, CANCELLED)."]
    ]
    add_styled_table(doc, headers_srv, data_apt_srv, widths_srv)

    # 2.3.5 Utilities
    add_heading_3(doc, "2.3.5. QrCodeService và EmailService (Tiện ích vé điện tử)")
    add_paragraph(doc, "Hai dịch vụ phụ trợ hỗ trợ việc xuất vé và gửi thông tin cho khách hàng:")
    add_bullet_point(doc, "QrCodeService: ", "Sử dụng thư viện Google ZXing. Hàm generateBase64QrCode(content, width, height) mã hóa thông tin vé (mã vé, tên khách, chuyến đi, số ghế) thành chuỗi ảnh Base64 PNG (data:image/png;base64,...). Chuỗi này nhúng thẳng vào file HTML và email vé điện tử để quét mã tại cửa lên tàu/máy bay mà không cần lưu file ảnh tĩnh lên máy chủ.")
    add_bullet_point(doc, "EmailService: ", "Gửi email thông báo tự động theo cơ chế bất đồng bộ @Async của Spring để tránh làm chậm thao tác người dùng. Gồm 4 hàm: sendReservationHeldEmail (báo mã giữ chỗ), sendReservationConfirmedEmail (gửi vé điện tử kèm mã QR), sendReservationCancelledEmail (báo hủy vé và số tiền hoàn), sendAppointmentConfirmedEmail (báo lịch hẹn tư vấn).")

    # 2.3.6 Security Service
    add_heading_3(doc, "2.3.6. UserDetailsServiceImpl (Xác thực và phân quyền)")
    add_paragraph(doc,
        "Class này cài đặt interface UserDetailsService của Spring Security 6. Hàm loadUserByUsername(String username) "
        "tìm người dùng theo username hoặc email trong CSDL, sau đó chuyển role người dùng thành GrantedAuthority "
        "(ROLE_TRAVELER, ROLE_AGENT, ROLE_OPERATOR, ROLE_ADMIN) để kiểm soát quyền truy cập các đường dẫn URL.")

    # 2.4 Strategy Pattern
    add_heading_2(doc, "2.4. Mẫu Thiết Kế Strategy Và Factory Cho Hai Loại Vé")
    add_paragraph(doc,
        "Vé máy bay và vé tàu hỏa có chính sách giá, thuế phí bắt buộc và điều kiện phạt hủy vé hoàn toàn khác nhau. "
        "Bảng so sánh chi tiết giữa hai loại vé:")

    headers_strat = ["Tiêu Chí Nghiệp Vụ", "Vé Máy Bay (AirTicketStrategy)", "Vé Tàu Hỏa (TrainTicketStrategy)"]
    data_strat = [
        ["Phí dịch vụ bắt buộc", "Thuế an ninh soi chiếu và phí sân bay: 120.000đ / khách (miễn phí em bé).", "Bảo hiểm đường sắt: 10.000đ / vé."],
        ["Chính sách giá theo tuổi", "• Người lớn: tính 100% giá vé gốc.\n• Trẻ em (2 - 12 tuổi): tính 75% giá gốc.\n• Em bé (< 2 tuổi): tính 10% giá gốc.", "• Người lớn: tính 100% giá vé gốc.\n• Trẻ em: giảm 25% (tính 75%).\n• Học sinh, sinh viên: giảm 10% (tính 90%).\n• Người cao tuổi (>= 60 tuổi): giảm 15% (tính 85%)."],
        ["Kiểm tra giấy tờ tùy thân", "Người lớn bắt buộc có số CCCD 12 chữ số hoặc hộ chiếu hợp lệ; số lượng em bé không được nhiều hơn số người lớn đi cùng.", "Kiểm tra ga đi và ga đến không được trùng nhau; phải đặt trước giờ tàu chạy ít nhất 30 phút."],
        ["Quy định phạt hủy vé", "• Hủy trước giờ bay >= 24h: phạt 10% giá vé.\n• Hủy trong vòng 24h: phạt 30% giá vé.\n• Đã cất cánh: phạt 100% (không hoàn tiền).", "• Hủy trước giờ tàu chạy >= 24h: phạt 10% giá vé.\n• Hủy từ 4h đến 24h: phạt 20% giá vé.\n• Hủy dưới 4h: phạt 30% giá vé.\n• Sau giờ tàu chạy: phạt 100% (không hoàn tiền)."]
    ]
    widths_strat = [Inches(1.8), Inches(2.4), Inches(2.3)]
    add_styled_table(doc, headers_strat, data_strat, widths_strat)

    add_paragraph(doc, "Cách cài đặt Strategy Pattern trong mã nguồn:")
    add_bullet_point(doc, "Interface TicketStrategy: ", "Khai báo 5 hàm: getTransportType(), calculatePrice(ticket, passengerType, extraFee), validateReservation(reservation, tickets), calculateCancellationFee(reservation), getPolicyDescription().")
    add_bullet_point(doc, "Hai lớp triển khai: ", "AirTicketStrategy và TrainTicketStrategy được gắn annotation @Component để Spring tự động phát hiện và quản lý.")
    add_bullet_point(doc, "Lớp TicketStrategyFactory: ", "Nhận danh sách List<TicketStrategy> qua constructor và nạp vào Map<TransportType, TicketStrategy>. Khi cần tính giá hay kiểm tra đơn, hàm getStrategy(TransportType) sẽ lấy đúng class tương ứng trong O(1).")

    # 2.5 DTOs
    add_heading_2(doc, "2.5. Các Lớp Đối Tượng Truyền Dữ Liệu (DTO)")
    add_paragraph(doc,
        "Dự án dùng các lớp DTO để nhận dữ liệu từ form giao diện, tách biệt với các Entity trong CSDL "
        "và kiểm tra tính hợp lệ dữ liệu (Bean Validation) trước khi xử lý:")

    headers_dto = ["Lớp DTO", "Các Thuộc Tính Chính", "Ràng Buộc Validation", "Mục Đích Sử Dụng"]
    data_dto = [
        ["TicketSearchDto", "transportType, departurePlace, arrivalPlace, departureDate, returnDate, roundTrip, passengerCount", "@NotNull", "Nhận tiêu chí tìm kiếm chuyến bay hoặc chuyến tàu từ giao diện trang chủ."],
        ["ReservationRequestDto", "ticketId, transportType, departurePlace, arrivalPlace, customerName, customerPhone, customerEmail, passengers", "@NotBlank, @Email, @NotEmpty", "Nhận dữ liệu từ form đặt vé của khách, chứa danh sách hành khách đi cùng."],
        ["PassengerDto", "fullName, idCard, passengerType, seatNumber, extraFee", "@NotBlank", "Chứa thông tin từng hành khách (họ tên, CCCD, loại khách, phụ phí hành lý)."],
        ["PaymentRequestDto", "bookingCode, paymentMethod, amount, note", "@NotBlank, @NotNull", "Nhận yêu cầu thanh toán từ khách (chọn VNPAY, chuyển khoản, thẻ tín dụng)."],
        ["SystemStatsDto", "totalReservations, totalRevenue, availableTickets, pendingAppointments", "Dữ liệu tính toán", "Gom các chỉ số thống kê số đơn, doanh thu và vé còn trống để hiển thị lên Dashboard Admin."]
    ]
    widths_dto = [Inches(1.5), Inches(1.8), Inches(1.3), Inches(1.9)]
    add_styled_table(doc, headers_dto, data_dto, widths_dto)

    # 2.6 Concurrency & Scheduling
    add_heading_2(doc, "2.6. Quản Lý Giao Dịch Và Tác Vụ Chạy Ngầm Tự Động")
    add_paragraph(doc, "Dự án áp dụng hai cơ chế quan trọng để đảm bảo tính chính xác của dữ liệu:")
    add_bullet_point(doc, "Quản lý Transaction: ", "Các hàm createHoldReservation, confirmAndIssue và cancelReservation đều chạy dưới ngữ cảnh transaction (@Transactional). Khi có lỗi ném ra, Spring tự động rollback toàn bộ dữ liệu ở cả 3 bảng DonDatCho, Ve và GiaoDich.")
    add_bullet_point(doc, "Tác vụ chạy ngầm định kỳ (Scheduler): ", "Khai báo @EnableScheduling trong ScheduleConfig. Hàm releaseExpiredHoldReservations() chạy tự động mỗi 60 giây để quét các đơn CREATED đã quá 15 phút, đổi trạng thái thành EXPIRED và trả lại ghế về kho AVAILABLE cho người khác đặt.")
    add_bullet_point(doc, "Xử lý ngoại lệ tập trung: ", "Lớp GlobalExceptionHandler gắn @ControllerAdvice bắt các lỗi BusinessException và ResourceNotFoundException, hiển thị thông báo lỗi rõ ràng cho người dùng thay vì xuất hiện màn hình lỗi 500.")

    # TỔNG KẾT
    add_heading_1(doc, "TỔNG KẾT VÀ ĐÁNH GIÁ KẾT QUẢ")
    add_paragraph(doc,
        "Báo cáo đã trình bày chi tiết hai phần việc chính trong đề tài Đồ án tốt nghiệp Web-Du-Lich:")
    add_bullet_point(doc, "Về Cơ sở dữ liệu. ", "Dự án đã xây dựng mô hình 4 bảng chuẩn 3NF cốt lõi đặt tên tiếng Việt rõ nghĩa trên Microsoft SQL Server (NguoiDung, DonDatCho, Ve, GiaoDich), tích hợp dịch vụ tư vấn gắn liền với đơn đặt vé sau khi đặt vé xong, có sơ đồ quan hệ chuẩn hóa mạch lạc, từ điển dữ liệu đầy đủ, các ràng buộc toàn vẹn và hệ thống chỉ mục phục vụ tốt cho các câu truy vấn thực tế.")
    add_bullet_point(doc, "Về Tầng Dịch vụ. ", "Các dịch vụ tổ chức theo mô hình phân tầng Spring Boot, tách riêng interface và class triển khai, áp dụng Strategy Pattern để xử lý linh hoạt hai loại vé máy bay và tàu hỏa, đồng thời quản lý chặt chẽ transaction và tự động thu hồi vé hết hạn giữ chỗ.")

    add_callout(doc, [
        "Toàn bộ cấu trúc CSDL và mã nguồn Java đã được cài đặt hoàn chỉnh và chạy thử nghiệm thành công.",
        "Mã nguồn tuân thủ đúng yêu cầu và tiêu chí chấm điểm trong đề cương đồ án tốt nghiệp.",
        "Tài liệu kỹ thuật sẵn sàng phục vụ cho việc báo cáo và bảo vệ đồ án."
    ], title="KẾT QUẢ THỰC HIỆN")

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    doc.save(output_path)
    print(f"Báo cáo Word đã được lưu tại: {output_path}")

    root_copies = [
        "Bao_Cao_Thiet_Ke_CSDL_Va_Thiet_Ke_Cac_Lop_Dich_Vu_Web_Du_Lich_Final.docx",
        "Bao_Cao_Thiet_Ke_CSDL_Va_Thiet_Ke_Cac_Lop_Dich_Vu_Web_Du_Lich_v2.docx",
        "Bao_Cao_Thiet_Ke_CSDL_Va_Thiet_Ke_Cac_Lop_Dich_Vu_Web_Du_Lich.docx"
    ]
    for r_copy in root_copies:
        try:
            shutil.copyfile(output_path, r_copy)
            print(f"Đã sao chép sang thư mục gốc: {r_copy}")
        except PermissionError:
            print(f"Lưu ý: Tệp '{r_copy}' đang mở trong Word, bỏ qua để không lỗi.")

if __name__ == "__main__":
    build_humanized_report()
