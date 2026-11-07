---
name: VietTravel Hub Design System
description: Modern Vietnamese Aviation & Railway Travel Portal - Human-Crafted, Authoritative, High Craft
colors:
  primary: "#0f2942"
  primary-interactive: "#0062d2"
  primary-hover: "#004ea8"
  primary-subtle: "#f0f6ff"
  accent-cta: "#d9531e"
  accent-hover: "#be4314"
  accent-subtle: "#fff7ed"
  train-bronze: "#8b4513"
  train-amber: "#b45309"
  train-subtle: "#fffbeb"
  surface: "#ffffff"
  surface-subtle: "#f8fafc"
  surface-alt: "#f1f5f9"
  text-primary: "#0f172a"
  text-secondary: "#334155"
  text-muted: "#64748b"
  border-subtle: "#e2e8f0"
  border-strong: "#cbd5e1"
  success: "#059669"
  warning: "#d97706"
  danger: "#dc2626"
typography:
  display:
    fontFamily: "Be Vietnam Pro, system-ui, sans-serif"
    fontSize: "2.5rem"
    fontWeight: 750
    lineHeight: 1.2
    letterSpacing: "-0.03em"
  heading:
    fontFamily: "Be Vietnam Pro, system-ui, sans-serif"
    fontSize: "1.625rem"
    fontWeight: 700
    lineHeight: 1.25
    letterSpacing: "-0.02em"
  body:
    fontFamily: "Be Vietnam Pro, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.6
  mono:
    fontFamily: "JetBrains Mono, monospace"
    fontSize: "0.875rem"
    fontWeight: 600
rounded:
  sm: "6px"
  md: "10px"
  lg: "16px"
  xl: "22px"
  full: "9999px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "36px"
components:
  button-primary:
    backgroundColor: "{colors.primary-interactive}"
    textColor: "#ffffff"
    rounded: "{rounded.md}"
    padding: "11px 22px"
  button-cta:
    backgroundColor: "{colors.accent-cta}"
    textColor: "#ffffff"
    rounded: "{rounded.md}"
    padding: "11px 22px"
---

# Design System

<!-- impeccable:design-tokens 1 -->

## Overview

VietTravel Hub áp dụng ngôn ngữ thiết kế **Modern Aviation & Heritage Rail** — định hướng giao diện mang tầm vóc một nền tảng du lịch trực tuyến chuyên nghiệp chuẩn quốc tế (như Vietnam Airlines Lotus, Traveloka, Kayak, Trainline).

Thiết kế ưu tiên:
- **Độ tin cậy hàng đầu (Authoritative Trust):** Tone màu xanh đại dương sẫm (`#0f2942`) phối hợp cùng sắc xanh hàng không (`#0062d2`) và điểm nhấn terracotta ấm áp (`#d9531e`), loại bỏ hoàn toàn các dải màu neon, gradient text và hiệu ứng kính bóng bẩy giả tạo.
- **Công thái học đặt vé (Search Cockpit Ergonomics):** Bảng tìm kiếm chuyến bay & tàu hỏa là trung tâm; hỗ trợ chuyển tab mượt mà, nút đảo chiều trực quan, thông tin giờ bay/tàu rõ ràng và số liệu hiển thị bằng tabular numerals.
- **Tiêu chuẩn Craft Floor:** Tuân thủ độ tương phản WCAG AA (>= 4.5:1), cỡ chữ chức năng tối thiểu 12px, không sử dụng kicker/eyebrow thừa, không dùng card viền dày 4px (side-tabs).

## Colors

- **Primary Brand (Deep Maritime Navy):** `#0f2942` - Tạo cảm giác uy tín, vững chãi của thương hiệu du lịch hàng đầu.
- **Interactive Blue:** `#0062d2` (Hover: `#004ea8`, Subtle: `#f0f6ff`, Border: `#bae6fd`) - Dành cho các hành động tương tác chính, liên kết, tìm kiếm vé máy bay.
- **Terracotta CTA Accent:** `#d9531e` (Hover: `#be4314`, Subtle: `#fff7ed`, Border: `#fed7aa`) - Dành cho nút đặt vé (Conversion CTA) với tỷ lệ tương phản cao và sức hút mạnh mẽ.
- **Heritage Railway Accent:** `#8b4513` & `#b45309` (Subtle: `#fffbeb`, Border: `#fde68a`) - Bản sắc đường sắt Việt Nam ấm áp, hoài niệm và lịch thiệp.
- **Neutral Canvas:**
  - Page Background: `#f8fafc`
  - Card Surface: `#ffffff`
  - Subtle Muted Surface: `#f1f5f9`
- **Typography Colors:**
  - Text Primary: `#0f172a` (Tỷ lệ tương phản > 14:1 trên nền trắng)
  - Text Secondary: `#334155` (Tỷ lệ tương phản > 7:1)
  - Text Muted: `#64748b` (Tỷ lệ tương phản > 4.5:1)

## Typography

- **Font chữ:** `Be Vietnam Pro` (toàn bộ tiêu đề & nội dung) kết hợp `JetBrains Mono` (mã đặt chỗ, đồng hồ đếm ngược, giá vé, số ghế).
- **Scale:**
  - Hero Display: 38px / Line-height 1.2 / Weight 750 / Letter-spacing -0.03em
  - Section Title (H2): 24px - 26px / Line-height 1.25 / Weight 700 / Letter-spacing -0.02em
  - Card / Group Title (H3): 17px - 18px / Line-height 1.3 / Weight 650
  - Body Text: 14.5px / Line-height 1.6 / Color `#334155`
  - Metadata / Micro labels: 12px - 13px / Weight 600 / Letter-spacing 0.015em
- **Số liệu & Giá vé:** Luôn dùng `font-variant-numeric: tabular-nums` để các hàng cột thẳng tắp, không bị lệch khi số thay đổi.

## Layout

- `app-container`: max-width `1240px`, padding `0 24px`, căn giữa tự nhiên.
- `app-container-lg`: max-width `1380px` (cho bảng điều hành Admin và sơ đồ toa tàu).
- Bố cục lưới nhất quán: `grid-2`, `grid-3`, `grid-4` với gap `20px - 24px`.
- Khung tìm kiếm (Search Cockpit): Nằm cân đối, nổi nhẹ trên nền banner với padding `28px 32px`, các trường nhập liệu sắp xếp logic theo luồng suy nghĩ của hành khách.

## Elevation & Depth

- Chiều sâu được tạo dựng bằng bóng đổ phân lớp vật lý tự nhiên (ambient light + key light), không sử dụng vầng sáng màu neon (colored halos):
  - `shadow-xs`: `0 1px 2px rgba(15, 23, 42, 0.04)`
  - `shadow-sm`: `0 1px 3px rgba(15, 23, 42, 0.06), 0 2px 6px -1px rgba(15, 23, 42, 0.04)`
  - `shadow-md`: `0 4px 14px -2px rgba(15, 23, 42, 0.08), 0 2px 4px -2px rgba(15, 23, 42, 0.04)`
  - `shadow-lg`: `0 12px 30px -4px rgba(15, 23, 42, 0.1), 0 4px 8px -2px rgba(15, 23, 42, 0.04)`

## Shapes

- Form controls & Input fields: Radius `8px - 10px` vừa vặn, viền `1px solid #cbd5e1`, trạng thái focus viền `#0062d2` cùng bóng `0 0 0 3px rgba(0, 98, 210, 0.15)`.
- Cards: Radius `14px - 16px`, đường viền thanh lịch `1px solid #e2e8f0`.
- Buttons & Tabs: Pill radius (`var(--radius-full)`) cho bộ chuyển tab tìm kiếm; radius `8px - 10px` cho các nút bấm hành động.

## Components

- **Navigation Bar:** Nền kính mờ tinh tế (`rgba(255, 255, 255, 0.94)`, backdrop blur 12px), đường phân cách mỏng `#e2e8f0`. Logo thương hiệu với chữ solid sẫm màu, biểu tượng máy bay origami/vector sắc nét.
- **Search Cockpit:**
  - Tab Switcher: 2 tab [Vé Máy Bay Nội Địa] và [Vé Tàu Hỏa Bắc Nam] với trạng thái active rõ rệt.
  - Sân bay / Ga tàu: Dropdown nhóm theo miền / km, nút đảo chiều tròn linh hoạt ở giữa.
  - Dải cam kết (Assurance Ribbon): Thanh ngang thanh thoát ngay dưới form, truyền tải giá trị cốt lõi bằng icon SVG chuẩn xác.
- **Route Showcase Cards:** Thẻ hành trình có ảnh phong cảnh tỉ lệ đẹp, tên chặng to rõ, giá vé định dạng chuẩn VND, nút đặt vé trực tiếp.
- **Flight & Train Result Rows:** Thiết kế dạng boarding pass tinh tế: hiển thị logo hãng hàng không/đường sắt, timeline chuyến đi dạng `Giờ đi - Sân bay đi ── Thời gian ── Giờ đến - Sân bay đến`, giá vé theo người và nút chọn vé.
- **Seat Map:** Sơ đồ toa tàu trực quan với ký hiệu đầu tàu, số ghế, phân loại toa (Ghế mềm, Giường K6, Giường K4) và chú thích màu sắc: Trống (xanh dịu), Đang chọn (cam ấm), Đã bán (xám trung tính).

## Do's and Don'ts

### Do's:
- Sử dụng màu đơn sắc có độ tương phản cao cho văn bản (Black Obsidian `#0f172a` hoặc Slate `#334155`).
- Sử dụng `font-variant-numeric: tabular-nums` cho số tiền, thời gian, mã chuyến bay.
- Thiết kế trạng thái rỗng (empty state) và trạng thái tải chu đáo.
- Đặt nhãn (label) rõ ràng trên trường nhập liệu với kích thước tối thiểu 13px.
- Sử dụng icon SVG đồng bộ stroke và viewbox.

### Don'ts:
- KHÔNG dùng gradient text (`background-clip: text`) trên tiêu đề hoặc thương hiệu.
- KHÔNG dùng viền màu dày 4px một cạnh trên card (side-tabs / top-bars) - biểu hiện rõ nhất của template AI.
- KHÔNG dùng card icon pastel 4 cột lặp đi lặp lại vô nghĩa.
- KHÔNG dùng thẻ kicker/eyebrow phía trên tiêu đề chính.
- KHÔNG dùng bóng sáng neon tỏa màu (colored glowing halos).
- KHÔNG đặt văn bản dưới 12px cho các thành phần chức năng tương tác.
