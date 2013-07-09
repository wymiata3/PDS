package com.ku.voltset.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;

import android.app.Activity;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.widget.Toast;

public class ExcelExporter extends Activity {

	int reportNumber;
	ArrayList<String> values;
	String owner;
	InputStream templateStream;
	FileInputStream[] optionalSignature;
	float MIN = 0.5f, MAX = 1.5f;

	public ExcelExporter(int reportNumber, ArrayList<String> values,
			String owner, InputStream in, FileInputStream... signatureStream) {
		this.reportNumber = reportNumber;
		this.values = values;
		this.owner = owner;
		this.templateStream = in;
		this.optionalSignature = signatureStream;
		HSSFWorkbook workbook = null;
		try {
			workbook = openWorkBook(this.templateStream);
		} catch (IOException io) {
			Toast.makeText(getApplicationContext(), "Error opening template",
					Toast.LENGTH_SHORT).show();
			return;
		}
		HSSFSheet sheet = workbook.getSheetAt(0);
		HSSFCellStyle greenStyle = setStyle(workbook, HSSFColor.LIME.index,
				HSSFCellStyle.SOLID_FOREGROUND);
		HSSFCellStyle redStyle = setStyle(workbook, HSSFColor.DARK_RED.index,
				HSSFCellStyle.SOLID_FOREGROUND);
		HSSFFont redFont = setFont(workbook, HSSFColor.RED.index,13);
		HSSFFont greenFont = setFont(workbook, HSSFColor.GREEN.index,13);
		setValueAtNonEmptyRow(workbook,sheet, "VoltSet", 0, 1,13);
		setValueAtNonEmptyRow(workbook,
				sheet,
				this.owner.equalsIgnoreCase("") ? "Did you forget to save the owner?"
						: this.owner, 3, 1,13);
		setValueAtNonEmptyRow(workbook,sheet, java.text.DateFormat.getDateTimeInstance()
				.format(Calendar.getInstance().getTime()), 4, 1,11);
		int mRow = 8;
		for (int i = 0; i < values.size(); i++) {
			String measurement = values.get(i).toString();
			setValueAtEmptyRow(sheet, (i + 1) + ") measurement:", mRow, 0);
			setValueAtNonEmptyRow(workbook,sheet, values.get(i).toString() + "V", mRow,
					2,13);
			if (correctMeasurement(Float.parseFloat(measurement), MIN, MAX)) {
				setOK(sheet, "OK", mRow, 3, greenStyle, greenFont);
			} else
				setOK(sheet, "Wrong", mRow, 3, redStyle, redFont);
			mRow++;
		}
		if (this.optionalSignature.length > 0) {
			try {
				addSignature(workbook, sheet, this.optionalSignature[0], mRow);
			} catch (IOException io) {
				Toast.makeText(getApplicationContext(),
						"Signature error, possibly corrupted. Recreate",
						Toast.LENGTH_SHORT).show();
			}
		}
		save(workbook, "Report" + this.reportNumber + ".xls");
	}

	private void setOK(HSSFSheet sheet, String value, int rowNumber,
			int columnNumber, HSSFCellStyle style, HSSFFont font) {
		HSSFRow row = sheet.getRow(rowNumber);
		HSSFCell cell = row.createCell(columnNumber);
		cell.setCellStyle(style);
		style.setFont(font);
		cell.setCellValue(new HSSFRichTextString(value));
	}

	private boolean correctMeasurement(float measurement, float min, float max) {
		if ((measurement > max) || (measurement < min)) {
			return false;
		}
		return true;
	}

	private HSSFWorkbook openWorkBook(InputStream in) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook(in);
		return workbook;
	}

	private HSSFCellStyle setStyle(HSSFWorkbook workbook,
			short foregroundColor, short pattern) {
		HSSFCellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(foregroundColor);
		style.setFillPattern(pattern);
		return style;
	}

	private HSSFFont setFont(HSSFWorkbook workbook, int fontColor,int fontSize) {
		HSSFFont font = workbook.createFont();
		font.setColor((short) fontColor);
		font.setFontHeightInPoints((short)fontSize);
		return font;
	}

	private void setValueAtNonEmptyRow(HSSFWorkbook workbook,HSSFSheet sheet, String cName,
			int rowNumber, int columnNumber,int fontSize) {
		// (0,1)-company
		// (3,1)-employee
		// (4,3)-date
		HSSFRow row = sheet.getRow(rowNumber);
		HSSFCell cell = row.createCell(columnNumber);
		HSSFFont font=workbook.createFont();
		font.setFontHeightInPoints((short)fontSize);
		cell.getCellStyle().setFont(font);
		cell.setCellValue(cName);
	}

	private void setValueAtEmptyRow(HSSFSheet sheet, String value,
			int rowNumber, int columnNumber) {
		HSSFRow row = sheet.createRow(rowNumber);
		HSSFCell cell = row.createCell(columnNumber);
		cell.setCellValue(value);
		sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber,
				columnNumber, columnNumber + 1));
	}

	private void addSignature(HSSFWorkbook workbook, HSSFSheet sheet,
			FileInputStream in, int rowNumber) throws IOException {
		ByteArrayOutputStream img_bytes = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) != -1)
			img_bytes.write(b);
		in.close();
		int col1 = 0, row1 = rowNumber;
		HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0,
				(short) col1, row1, (short) (col1 + 5), row1 + 6);
		anchor.setAnchorType(2);
		int index = workbook.addPicture(img_bytes.toByteArray(),
				HSSFWorkbook.PICTURE_TYPE_PNG);
		HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
		patriarch.createPicture(anchor, index);
		anchor.setAnchorType(2);
	}

	private boolean isExternalStoragePresent() {

		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		if (!((mExternalStorageAvailable) && (mExternalStorageWriteable))) {
			Toast.makeText(getApplicationContext(), "SD card not present",
					Toast.LENGTH_LONG).show();

		}
		return (mExternalStorageAvailable) && (mExternalStorageWriteable);
	}

	public static boolean createDirIfNotExists(String path) {
		boolean ret = true;

		File file = new File(Environment.getExternalStorageDirectory(), path);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				ret = false;
			}
		}
		return ret;
	}

	private void save(HSSFWorkbook workbook, String filename) {
		FileOutputStream fos = null;
		String dir="iamsofuckingawesome";
		try {
			if (isExternalStoragePresent()) {
				createDirIfNotExists(dir);
			}
			String str_path = Environment.getExternalStorageDirectory()
					.toString();
			File file = new File(str_path, "/"+dir+"/report" + reportNumber + ".xls");
			fos = new FileOutputStream(file);
			workbook.write(fos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}