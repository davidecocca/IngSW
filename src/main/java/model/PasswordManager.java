package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import util.CryptoUtils;
import util.CryptoUtils.CryptoException;
import util.RandomPasswordGenerator;

public class PasswordManager {

	//TreeMap of credentials
	private TreeMap<String, Credential> data;
	//Master password
	private String masterPassword;
	//Database file
	private File dbFile;
	//PasswordManager instance
	private static PasswordManager instance;

	public static synchronized PasswordManager getInstance() throws IOException {
		if (instance == null) {
			instance = new PasswordManager();
		}
		return instance;
	}

	private PasswordManager() throws IOException {
		//Create database file
		this.dbFile = new File("db.mpm");
		if (!dbFile.exists()) {
			dbFile.createNewFile();
		}
	}

	//Init password manager
	public void init(String masterPassword) throws CryptoException, IOException {

		//Set master password
		setMasterPassword(masterPassword);

		//If file is empty create a new TreeMap, otherwise load data from file
		if (this.dbFile.length() != 0) {
			//Read database TreeMap from encrypted file
			this.data = (TreeMap<String, Credential>) CryptoUtils.decrypt(masterPassword, dbFile);
		}
		else {
			//Create a new TreeMap
			this.data = new TreeMap<String, Credential>();
			//Write to file
			writeToFile();
		}

	}

	//Get master password
	public String getMasterPassword() {
		return this.masterPassword;
	}

	//Set master password
	public void setMasterPassword(String masterPassword) {
		this.masterPassword = masterPassword;
	}

	//Get dbFile
	public File getDbFile() {
		return this.dbFile;
	}

	//Get data TreeMap
	public TreeMap<String, Credential> getCredentials() {
		return this.data;
	}

	//Print data TreeMap
	public void printAll(PrintStream ps) {
		for(String key : data.keySet()) {
			Credential c = data.get(key);
			c.print(ps);
		}
	}

	//Get a credential
	public Credential getCredential(String site) {
		return this.data.get(site);
	}

	//Add new credential
	public void addCredential(String st, String un, String pw, String ct) throws IOException, CryptoException, DuplicatedKeyException {
		Credential c = data.get(st);
		//If credential is not in the TreeMap yet, add to it, else throw an exception
		if (c == null) {
			c = new Credential(st,un,pw, ct);
			data.put(st, c);
			writeToFile();
		}
		else {
			throw new DuplicatedKeyException();
		}
	}

	//Edit credential
	public void editCredential(String st, String un, String pw, String ct) throws IOException, CryptoException {
		Credential c = new Credential(st,un,pw, ct);
		data.put(st, c);
		writeToFile();
	}

	//Remove a credential
	public void removeCredential(String st) throws IOException, CryptoException {
		data.remove(st);
		writeToFile();
	}

	//Clear all data from the data TreeMap
	public void clearData() throws IOException, CryptoException {
		data.clear();
		writeToFile();
	}

	//Write to file (with encryption)
	public void writeToFile() throws IOException, CryptoException {
		File file = new File("db.mpm");
		CryptoUtils.encrypt(masterPassword, data, file);
	}

	//Write to file (without encryption)
	public void writeToFileNoEncoding(File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		for(String key : data.keySet()) {
			Credential c = data.get(key);
			bw.write(c.toString());
		}
		bw.close();
	}

	//Export to an .xls excel file
	public void exportToXlsExcel(File file) throws IOException {
		// Create a Workbook
		Workbook workbook = new HSSFWorkbook();
		// Create a Sheet
		Sheet sheet = workbook.createSheet("Data");
		//Create rows
		int rowNum = 0;
		for(String key : data.keySet()) {
			Credential c = data.get(key);
			Row row = sheet.createRow(rowNum++);
			//Set site
			row.createCell(0).setCellValue(c.getSite());
			//Set username
			row.createCell(1).setCellValue(c.getUsername());
			//Set password
			row.createCell(2).setCellValue(c.getPassword());
			//Set category
			row.createCell(3).setCellValue(c.getCategory());
		}
		// Resize all columns to fit the content size
		for(int i = 0; i < 4; i++) {
			sheet.autoSizeColumn(i);
		}
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		fileOut.close();

		// Closing the workbook
		workbook.close();
	}

	//Export to an .xlsx excel file
	public void exportToXlsxExcel(File file) throws IOException {
		// Create a Workbook
		Workbook workbook = new XSSFWorkbook();
		// Create a Sheet
		Sheet sheet = workbook.createSheet("Data");
		//Create rows
		int rowNum = 0;
		for(String key : data.keySet()) {
			Credential c = data.get(key);
			Row row = sheet.createRow(rowNum++);
			//Set site
			row.createCell(0).setCellValue(c.getSite());
			//Set username
			row.createCell(1).setCellValue(c.getUsername());
			//Set password
			row.createCell(2).setCellValue(c.getPassword());
			//Set category
			row.createCell(3).setCellValue(c.getCategory());
		}
		// Resize all columns to fit the content size
		for(int i = 0; i < 4; i++) {
			sheet.autoSizeColumn(i);
		}
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(file);
		workbook.write(fileOut);
		fileOut.close();

		// Closing the workbook
		workbook.close();
	}


	//Import from file. Credentials with the same key(same site) will be overwritten
	public void importFromFile(File file) throws CryptoException, IOException {
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader((new InputStreamReader(new FileInputStream(file), "UTF-8")));
		String line;
		StringTokenizer tok;
		while ((line = br.readLine()) != null) {
			tok = new StringTokenizer(line, ";");
			String st = tok.nextToken();
			String un = tok.nextToken();
			String pw = tok.nextToken();
			String ct = tok.nextToken();
			Credential c = new Credential(st, un, pw, ct);
			data.put(st, c);
		}
		writeToFile();
	}

	//Import from an excel file. Credentials with the same key(same site) will be overwritten
	public void importFromExcel(File file) throws EncryptedDocumentException, IOException, CryptoException {
		//Create a Workbook from an Excel file (.xls or .xlsx)
		Workbook workbook = WorkbookFactory.create(file);
		//Iterating over all the sheets in the workbook
		for(Sheet sheet: workbook) {
			//Create a DataFormatter to format and get each cell's value as String
			DataFormatter dataFormatter = new DataFormatter();
			//Iterating over all the rows in a Sheet
			for (Row row: sheet) {
				//Get site
				String st = dataFormatter.formatCellValue(row.getCell(0));
				//Get username
				String un = dataFormatter.formatCellValue(row.getCell(1));
				//Get password
				String pw = dataFormatter.formatCellValue(row.getCell(2));
				//Get category
				String ct = dataFormatter.formatCellValue(row.getCell(3));

				//If all credential fields are valid
				if (!st.equals("") && !un.equals("") && !pw.equals("") && !ct.equals("")) {
					//Create a new credential
					Credential c = new Credential(st, un, pw, ct);
					//Put the new credential into the TreeMap
					data.put(st, c);
				}

			}
		}
		//Write to file
		writeToFile();
	}

	public static String generateRandomPassword(int length, boolean upperCase, boolean digits, boolean symbols) {
		return RandomPasswordGenerator.generateRandomPassword(length, upperCase, digits, symbols);
	}

	//Exception for duplicated key
	public static class DuplicatedKeyException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DuplicatedKeyException() {

		}

		public DuplicatedKeyException(String message, Throwable throwable) {
			super(message, throwable);
		}
	}

}
