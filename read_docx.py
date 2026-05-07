import sys
import zipfile
import xml.etree.ElementTree as ET
try:
    with zipfile.ZipFile('SRS Template - Hệ thống Đặt vé Rạp chiếu phim.docx') as docx:
        xml_content = docx.read('word/document.xml')
        tree = ET.XML(xml_content)
        texts = [node.text for node in tree.iter('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}t') if node.text]
        text = '\n'.join(texts)
        # using utf-8 output manually
        sys.stdout.buffer.write(text.encode('utf-8'))
except Exception as e:
    print('Error:', e)
