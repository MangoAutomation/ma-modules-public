package com.infiniteautomation.datafilesource.contexts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import au.com.bytecode.opencsv.CSVReader;

public class CSVImporter {
	public void doImport(File f, AbstractCSVDataSource impl) throws FileNotFoundException, IOException {
		CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(f)));
		String[] nextLine;
		int rowNum = 0;
		while((nextLine = csvReader.readNext()) != null) {
			impl.importRow(nextLine, rowNum);
			rowNum += 1;
		}
		csvReader.close();
	}
}
