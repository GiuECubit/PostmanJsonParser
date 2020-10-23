package it.deplo.test.json.postman.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonRead {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		
		JSONParser parser = new JSONParser();
		//JsonParser to convert JSON string into Json Object

		try {
			JSONAware jsonToParse = (JSONAware)parser.parse(new FileReader("C:/Sviluppo/PSE-PiattaformaSanitaria/DatiChiamata.json"));
			//parsing the JSON string inside the file we created earlier.
			
			JSONParserG jsonParserG = new JSONParserG(jsonToParse);

			System.out.println("----------- JSON INPUT ----------------");
			
			System.out.println( jsonToParse );

			System.out.println("------------FINE ---------------");
			
			//System.out.println(jsonObject);

			if ( jsonToParse instanceof JSONArray ) {
				((ArrayList<String>)jsonToParse).forEach( jsonParserG );
			} else if ( jsonToParse instanceof JSONObject ) {
				((HashMap)jsonToParse).forEach( jsonParserG );				
			}
			
			jsonParserG.printAll();
			
						
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}