package org.pharmacyparser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PharmacyExporter {

    private static XSSFCellStyle createStyleForTitle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    public static void writeIntoExcel(ArrayList<PharmacyProduct> productsArray) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            XSSFSheet sheet = workbook.createSheet("ExportedData");

            XSSFCellStyle titleStyle = createStyleForTitle(workbook);
            int rowNum = 0;
            Row titleRow = sheet.createRow(rowNum++);

            // Name title
            Cell titleNameCell = titleRow.createCell(0, CellType.STRING);
            titleNameCell.setCellValue("Название товара");
            titleNameCell.setCellStyle(titleStyle);

            // Label title
            Cell titleLabelCell = titleRow.createCell(1, CellType.STRING);
            titleLabelCell.setCellValue("Аптека");
            titleLabelCell.setCellStyle(titleStyle);

            // Price title
            Cell titlePriceCell = titleRow.createCell(2, CellType.NUMERIC);
            titlePriceCell.setCellValue("Цена");
            titlePriceCell.setCellStyle(titleStyle);

            // Data
            for (var product : productsArray) {
                Row row = sheet.createRow(rowNum++);

                Cell nameCell = row.createCell(0);
                nameCell.setCellValue(product.getName());

                Cell labelCell = row.createCell(1);
                labelCell.setCellValue(product.getPharmacyLabel());

                Cell priceCell = row.createCell(2);
                priceCell.setCellValue(product.getPrice());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (FileOutputStream output = new FileOutputStream("exported.xls")) {
                workbook.write(output);
                System.out.format("Data of %d products has been exported.\n\n", productsArray.size());
            }
        }

    }

}
