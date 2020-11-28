package it.deplo.test.json.postman.parser.handler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonCustomHandlerReader {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, IOException {

		try {
			//parsing the JSON string inside the file we created earlier.					
			JSONParser parser = new JSONParser();
	    	JSONCustomHandlerParserG handler = new JSONCustomHandlerParserG();
			parser.parse(new FileReader("C:/Sviluppo/PSE-PiattaformaSanitaria/DatiChiamata.json"), handler );
       
		} catch(ParseException pe) {
			pe.printStackTrace();
		}
	}
}