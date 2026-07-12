# Notion Record

Notion의 `About programming` 최상위 페이지를 GitHub 문서 구조로 이전하기 위한 디렉터리입니다.

## 목표 구조

```text
notion-record/
├─ ssafy/
├─ infra-tool/
├─ study/
├─ books/
├─ cs/
└─ english/
```

## 변환 기준

- Notion Export의 Markdown 및 CSV를 대상으로 합니다.
- 파일명 뒤에 붙는 32자리 Notion 페이지 ID를 제거합니다.
- 최상위 페이지의 제목 구분을 GitHub 디렉터리로 변환합니다.
- Markdown 내부의 Notion 로컬 링크는 GitHub 상대 경로로 변경합니다.
- 데이터베이스 CSV와 연결된 Markdown 페이지를 같은 분류 아래에 배치합니다.
- `_all.csv` 중복 파일은 제외합니다.
- 이미지와 첨부파일은 해당 문서의 `assets` 디렉터리로 이동하도록 구성합니다.

## 실행

저장소 루트에서 Notion Export ZIP을 지정해 실행합니다.

```bash
python scripts/migrate_notion_export.py \
  /path/to/ExportBlock-Part-1.zip \
  notion-record
```

변환 결과를 검토한 후 커밋합니다.
