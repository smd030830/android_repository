from pathlib import Path
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

SRC_DOCX = Path('final_output') / '최종보고서_DogLog.docx'
DOCX = Path('final_output') / '최종보고서_DogLog_DB보완.docx'
FONT = '맑은 고딕'

def east_asia(run, font=FONT):
    run.font.name = font
    run._element.rPr.rFonts.set(qn('w:eastAsia'), font)

def p(doc, text='', style=None, size=10, bold=False, color=None):
    para = doc.add_paragraph(style=style)
    para.paragraph_format.space_after = Pt(6)
    para.paragraph_format.line_spacing = 1.1
    run = para.add_run(text)
    east_asia(run)
    run.font.size = Pt(size)
    run.bold = bold
    if color:
        run.font.color.rgb = RGBColor.from_string(color)
    return para

def shade(cell, fill):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = tcPr.find(qn('w:shd'))
    if shd is None:
        shd = OxmlElement('w:shd')
        tcPr.append(shd)
    shd.set(qn('w:fill'), fill)

def cell_text(cell, text, bold=False, size=7.7, color=None):
    cell.text = ''
    para = cell.paragraphs[0]
    para.paragraph_format.space_after = Pt(0)
    para.paragraph_format.line_spacing = 1.0
    run = para.add_run(str(text))
    east_asia(run)
    run.font.size = Pt(size)
    run.bold = bold
    if color:
        run.font.color.rgb = RGBColor.from_string(color)
    cell.vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER

def table(doc, headers, rows, widths=None):
    t = doc.add_table(rows=1, cols=len(headers))
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    t.style = 'Table Grid'
    for i, h in enumerate(headers):
        cell_text(t.rows[0].cells[i], h, True, 7.6, '0B2545')
        shade(t.rows[0].cells[i], 'E8EEF5')
    for row in rows:
        cells = t.add_row().cells
        for i, v in enumerate(row):
            cell_text(cells[i], v, False, 7.3)
    if widths:
        for row in t.rows:
            for i, w in enumerate(widths):
                row.cells[i].width = Inches(w)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return t

doc = Document(SRC_DOCX)
# Remove previous appended DB capture section if script is rerun.
# Simple safe approach: append a new dated correction only once by marker check.
all_text = '\n'.join(par.text for par in doc.paragraphs)
if '10. DB 테이블 캡처본 반영' not in all_text:
    doc.add_page_break()
    p(doc, '10. DB 테이블 캡처본 반영', 'Heading 1')
    p(doc, '실행 중 DBMS 화면에서 확인한 실제 저장 레코드를 보고서에 추가하였다. 아래 내용은 첨부한 DB 테이블 캡처본을 기준으로 정리한 것이며, 회원가입, 강아지 등록, 일지 작성, 문의/답장 기능이 DB에 반영되었음을 보여준다.')

    p(doc, '10.1 USER 테이블 캡처 내용', 'Heading 2')
    p(doc, '기존 JSP 호환 로그인 테이블이다. 캡처본에서 임시보호자 계정과 입양희망자 계정이 각각 저장되어 있음을 확인할 수 있다.', size=9)
    table(doc, ['userID', 'userPassword', 'userEmail', 'userGender', 'userType'], [
        ['test123', 'test123', 'test123@doglog.com', 'male', 'foster'],
        ['user111', 'user111', 'user111@doglog.com', 'female', 'adopter'],
    ], [1.0, 1.05, 2.05, 1.0, 1.0])

    p(doc, '10.2 APP_USER 테이블 캡처 내용', 'Heading 2')
    p(doc, '신규 회원가입 저장용 사용자 테이블이다. 임시보호자와 입양희망자가 모두 저장되어 있으며, 로그인/마이페이지 권한 분기에 사용된다.', size=9)
    table(doc, ['userID', 'userPassword', 'userEmail', 'userGender', 'userType'], [
        ['user001', 'user001', 'user001@doglog.com', 'female', 'foster'],
        ['user01', 'user01', 'user01@doglog.com', 'female', 'foster'],
        ['user111', 'user111', 'user111@doglog.com', 'female', 'adopter'],
        ['user222', 'user222', 'user222@doglog.com', 'male', 'adopter'],
    ], [1.0, 1.05, 2.05, 1.0, 1.0])

    p(doc, '10.3 DOG 테이블 캡처 내용', 'Heading 2')
    p(doc, '강아지 등록 결과가 저장된 테이블이다. 세 강아지 모두 fosterId가 test123으로 저장되어 있어 임시보호자 본인 강아지 목록 필터링과 작성 권한 검증에 사용된다. photoData는 Base64 이미지 데이터이므로 표에서는 길이를 줄여 표시하였다.', size=9)
    table(doc, ['id', 'name', 'breed', 'age', 'status', 'fosterId', 'photoData'], [
        ['7', '마루', '푸들', '3', '임시보호중', 'test123', '/9j/4AAQSkZJRgABAQA...'],
        ['10', '밀크', '말티즈', '13', '임시보호중', 'test123', '/9j/4AAQSkZJRgABAQA...'],
        ['12', '호두', '말티푸', '1', '임시보호중', 'test123', '/9j/4AAQSkZJRgABAQA...'],
    ], [0.45, 0.65, 0.8, 0.45, 1.0, 0.85, 2.15])

    p(doc, '10.4 DIARY 테이블 캡처 내용', 'Heading 2')
    p(doc, '일지 작성 결과가 저장된 테이블이다. 한국어 본문, 사료량(foodAmount), 배변량(poopCount), 작성자 fosterId가 함께 저장되어 일지 상세 화면과 작성자 연락 기능의 근거가 된다.', size=9)
    table(doc, ['id', 'dogName', 'dateText', 'foodAmount', 'poopCount', 'content', 'photoData', 'createdAt', 'fosterId'], [
        ['10', '마루', '2026.06.11 15:46', '49', '3', '오늘 산책 갔다와서 목욕했어요', '', '2026-06-11 15:46:14', 'test123'],
        ['11', '마루', '2026.06.12 04:33', '39', '2', '오늘 애견카페 갔다왔어요', '', '2026-06-12 04:33:19', 'test123'],
        ['12', '마루', '2026.06.12 06:17', '46', '1', 'test1231443', '/9j/4AAQSkZJRg...', '2026-06-12 06:17:33', 'test123'],
    ], [0.35, 0.6, 1.0, 0.75, 0.75, 1.55, 1.15, 1.35, 0.75])

    p(doc, '10.5 CONTACT_MESSAGE 테이블 캡처 내용', 'Heading 2')
    p(doc, '입양희망자의 문의와 임시보호자의 답장이 저장된 테이블이다. senderId는 문의 작성자, receiverId는 임시보호자이며 replyContent/repliedAt을 통해 답장 여부를 확인할 수 있다.', size=9)
    table(doc, ['id', 'senderId', 'receiverId', 'dogName', 'title', 'content', 'createdAt', 'replyContent', 'repliedAt'], [
        ['6', 'user111', 'test123', '밀크', '밀크 문의', '강아지가 귀엽네요', '2026-06-12 07:27:56', '[NULL]', '[NULL]'],
        ['7', 'user222', 'test123', '밀크', '밀크 문의', '강아지 입양 문의드...', '2026-06-12 08:27:23', '연락처 남겨주세요~', '2026-06-12 08:28:22'],
    ], [0.35, 0.75, 0.8, 0.6, 0.8, 1.35, 1.25, 1.3, 1.25])

    p(doc, 'DB 캡처본을 통해 확인한 CRUD 흐름', 'Heading 2')
    for item in [
        'Create: 회원가입, 강아지 등록, 일지 작성, 문의 작성이 각각 APP_USER/USER, DOG, DIARY, CONTACT_MESSAGE에 저장되었다.',
        'Read: 홈 화면과 상세 화면은 DOG와 DIARY의 저장 데이터를 서버 API로 조회하여 표시한다.',
        'Update: CONTACT_MESSAGE의 replyContent와 repliedAt이 채워져 문의 답장 기능이 실제 DB 레코드를 수정함을 확인했다.',
        'Delete: 보고서 본문 테스트 결과와 함께 DeleteDog/DeleteDiary API는 fosterId 조건으로 본인 데이터만 삭제하도록 검증했다.',
    ]:
        para = doc.add_paragraph(style='List Bullet')
        para.paragraph_format.space_after = Pt(4)
        run = para.add_run(item)
        east_asia(run)
        run.font.size = Pt(9.5)

doc.save(DOCX)
print(DOCX.resolve())
