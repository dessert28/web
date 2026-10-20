# 2026-10-15 validation
JAVA_HOME=D:\pc\.jdks\graalvm-jdk-17.0.12
Command: .\mvnw.cmd -B test (cwd shiyan2)
BUILD SUCCESS. Tests run: 1, Failures: 0, Errors: 0, Skipped: 0.
Browser reset: name blank, price fields 0.0.
Browser POST 1: 好商品,100.0,50.0 -> row id 1.
Browser POST 2: 灯饰1,1000.0,500.0 -> rows id 1 and 2.
GET / -> 404; GET /shiyan1/toAdd -> rendered form.
Existing code screenshots compared with current sources and retained. Historical running screenshots replaced.

Visual QA: all 8 WPS-exported PDF pages rendered with pypdfium2 and inspected. No clipping or overlapping tables/images. Standard render_docx.py unavailable because LibreOffice is missing; WPS export used instead. All package parts preserved except intended document text, core metadata and two result screenshots.
