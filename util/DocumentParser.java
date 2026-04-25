package com.rabbiter.oes.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文档解析工具类
 */
@Component
public class DocumentParser {
    
    /**
     * 解析上传的文档内容
     */
    public String parseDocument(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = fileName.toLowerCase().substring(fileName.lastIndexOf("."));
        
        System.out.println("🔍 DocumentParser: 开始解析文件: " + fileName + ", 扩展名: " + extension);
        System.out.println("🔍 DocumentParser: 文件大小: " + file.getSize() + " bytes");
        
        switch (extension) {
            case ".pdf":
                System.out.println("📄 DocumentParser: 解析PDF文档");
                return parsePDF(file.getInputStream());
            case ".docx":
            case ".doc":
                System.out.println("📝 DocumentParser: 解析Word文档，自动检测格式");
                return parseWordDocument(file.getInputStream());
            case ".xlsx":
                System.out.println("📊 DocumentParser: 解析Excel(.xlsx)文档");
                return parseExcel(file.getInputStream());
            case ".xls":
                System.out.println("📊 DocumentParser: 解析Excel(.xls)文档");
                return parseExcel(file.getInputStream());
            case ".txt":
                System.out.println("📄 DocumentParser: 解析文本文档");
                return parseText(file);
            default:
                throw new IllegalArgumentException("不支持的文件格式: " + extension + "。支持的格式：.pdf, .docx, .doc, .xlsx, .xls, .txt");
        }
    }
    
    /**
     * 解析PDF文档
     */
    private String parsePDF(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    /**
     * 解析Word文档（自动检测.doc和.docx格式）
     */
    private String parseWordDocument(InputStream inputStream) throws IOException {
        System.out.println("🔧 DocumentParser.parseWordDocument: 开始解析Word文档");
        
        // 先将InputStream转换为byte数组，以便多次使用（Java 8兼容方式）
        byte[] fileData;
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            fileData = baos.toByteArray();
            baos.close();
        } catch (Exception e) {
            throw new IOException("无法读取文档数据: " + e.getMessage(), e);
        }
        
        System.out.println("🔧 DocumentParser.parseWordDocument: 文档数据大小: " + fileData.length + " bytes");
        
        // 首先尝试解析为.docx格式（OOXML）
        try (java.io.ByteArrayInputStream docxStream = new java.io.ByteArrayInputStream(fileData)) {
            System.out.println("🔧 DocumentParser.parseWordDocument: 尝试解析为.docx格式");
            XWPFDocument document = new XWPFDocument(docxStream);
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            document.close();
            System.out.println("✅ DocumentParser.parseWordDocument: 成功解析为.docx格式，内容长度: " + content.length());
            return content.toString();
        } catch (Exception e) {
            System.out.println("⚠️ DocumentParser.parseWordDocument: .docx解析失败: " + e.getMessage());
            System.out.println("🔧 DocumentParser.parseWordDocument: 尝试解析为.doc格式");
            
            // 如果.docx解析失败，尝试解析为.doc格式（OLE2）
            try (java.io.ByteArrayInputStream docStream = new java.io.ByteArrayInputStream(fileData);
                 HWPFDocument document = new HWPFDocument(docStream);
                 WordExtractor extractor = new WordExtractor(document)) {
                String content = extractor.getText();
                System.out.println("✅ DocumentParser.parseWordDocument: 成功解析为.doc格式，内容长度: " + content.length());
                return content;
            } catch (Exception e2) {
                System.err.println("❌ DocumentParser.parseWordDocument: .doc解析也失败: " + e2.getMessage());
                throw new IOException("Word文档解析失败，既不是有效的.docx格式，也不是有效的.doc格式。" +
                    "\n.docx错误: " + e.getMessage() + 
                    "\n.doc错误: " + e2.getMessage(), e2);
            }
        }
    }
    
    /**
     * 解析Word文档(.docx格式) - 保留作为备用方法
     */
    private String parseWordX(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            return content.toString();
        }
    }
    
    /**
     * 解析Word文档(.doc格式) - 保留作为备用方法
     */
    private String parseWordLegacy(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }
    
    /**
     * 解析Excel文档（支持.xls和.xlsx格式）
     */
    private String parseExcel(InputStream inputStream) throws IOException {
        System.out.println("🔧 DocumentParser.parseExcel: 开始解析Excel文档");
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            StringBuilder content = new StringBuilder();
            
            String workbookType = workbook.getClass().getSimpleName();
            System.out.println("📊 DocumentParser.parseExcel: 成功创建Workbook，类型: " + workbookType);
            System.out.println("📊 DocumentParser.parseExcel: 工作表数量: " + workbook.getNumberOfSheets());
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                content.append("工作表: ").append(sheet.getSheetName()).append("\n");
                
                int rowCount = 0;
                for (Row row : sheet) {
                    if (row == null) continue;
                    
                    boolean hasContent = false;
                    StringBuilder rowContent = new StringBuilder();
                    
                    for (Cell cell : row) {
                        if (cell == null) {
                            rowContent.append("\t");
                            continue;
                        }
                        
                        String cellValue = "";
                        try {
                            switch (cell.getCellType()) {
                                case STRING:
                                    cellValue = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    // 检查是否为日期
                                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                        cellValue = cell.getDateCellValue().toString();
                                    } else {
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    cellValue = String.valueOf(cell.getBooleanCellValue());
                                    break;
                                case FORMULA:
                                    try {
                                        cellValue = String.valueOf(cell.getNumericCellValue());
                                    } catch (Exception e) {
                                        cellValue = cell.getStringCellValue();
                                    }
                                    break;
                                default:
                                    cellValue = "";
                                    break;
                            }
                        } catch (Exception e) {
                            System.err.println("⚠️ 解析单元格失败: " + e.getMessage());
                            cellValue = "";
                        }
                        
                        if (!cellValue.trim().isEmpty()) {
                            hasContent = true;
                        }
                        rowContent.append(cellValue).append("\t");
                    }
                    
                    if (hasContent) {
                        content.append(rowContent.toString()).append("\n");
                        rowCount++;
                    }
                    
                    // 限制行数，避免内容过长
                    if (rowCount > 1000) {
                        content.append("...(内容已截断，超过1000行)\n");
                        break;
                    }
                }
                content.append("\n");
            }
            
            System.out.println("✅ Excel解析完成，内容长度: " + content.length());
            return content.toString();
        } catch (Exception e) {
            System.err.println("❌ Excel解析失败: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Excel文档解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析文本文档
     */
    private String parseText(MultipartFile file) throws IOException {
        return new String(file.getBytes(), "UTF-8");
    }
    
    /**
     * 限制文档内容长度，避免超出AI模型的上下文限制
     */
    public String limitContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        
        if (content.length() <= maxLength) {
            return content;
        }
        
        // 截取前部分内容，并在适当位置断开
        String truncated = content.substring(0, maxLength);
        int lastNewLine = truncated.lastIndexOf('\n');
        if (lastNewLine > maxLength * 0.8) {
            return truncated.substring(0, lastNewLine) + "\n...(内容已截断)";
        }
        
        return truncated + "...(内容已截断)";
    }
}
