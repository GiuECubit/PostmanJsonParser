package it.deplo.test.json.postman.parser.handler;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

public class JSONCustomHandlerParserG implements ContentHandler {

	private static final Logger logger = LogManager.getLogger("DEBUGGER");
	private static final Logger outputter = LogManager.getLogger("OUTPUTTER");

	private String currentNodeName = "";
	private String previousChain = "";

	private int counter = 0;
	private boolean parentIsArray = false;
	
	
	/* ################### JSON ########################### */
	@Override
	public void startJSON() throws ParseException, IOException {
		System.out.println("StartJSON #######################");
	}

	@Override
	public void endJSON() throws ParseException, IOException {
		System.out.println("endJSON #######################");
	}
	/* ################### JSON ########################### */

	
	
	
	

	/* ************ OBJECT ENTRY  *****************/

	@Override
	public boolean startObjectEntry(String key) throws ParseException, IOException {
		currentNodeName = key;
		increaseChain();
		print("StartObjectEntry: [" + key + "] previousChain[" + previousChain + "]");
		return true;
	}

	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		print("EndObjectEntry");
		decreaseChain();
		return true;
	}
	/* ************ OBJECT ENTRY  *****************/

	public boolean primitive(Object value) throws ParseException, IOException {
		print("Key[" + currentNodeName + "] : " + value);
		return true;
	}	
	


	/* ********************** OBJECT *****************/
	@Override
	public boolean startObject() throws ParseException, IOException {
		print("StartObject");
		//System.out.println("StartObject currentNodeName[" + currentNodeName + "]");
		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		print("EndObject");
		decreaseChain();
		return true;
	}
	/* ********************** OBJECT *****************/

	
	

	
	/* @@@@@@@@@@@@@@@@@ ARRAY @@@@@@@@@@@@@@@@@@ */
	@Override
	public boolean startArray() throws ParseException, IOException {
		print("inside startArray");
		parentIsArray  = true;
		counter++;
		return true;
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		print("inside endArray");
		parentIsArray = false;
		counter--;
		return true;
	}
	/* @@@@@@@@@@@@@@@@@ ARRAY @@@@@@@@@@@@@@@@@@ */
	
	
	private void decreaseChain() {
		//System.out.println("PreviousChain[" + previousChain + "]");
		counter--;
		
		if ( previousChain.indexOf('.') < 0 ) {
			previousChain = "";
		} else {
			int end = previousChain.lastIndexOf("." + currentNodeName);		
			previousChain = previousChain.substring( 0, end );
		}
	}

	private void increaseChain() {
		counter++;
		if ( "".equals( previousChain ) ) {
			previousChain = currentNodeName;
		} else {
			previousChain = previousChain + "." + currentNodeName;
		}
		//logger.debug("Aggiunto nodo[" + currentNodeName + "] alla chain [" + previousChain + "]");
	}
	
	private void print ( String logging ) {
		String tabbed = "";
		for ( int i=0; i<counter; i++) {
			tabbed += "\t";
		}
	logger.debug(tabbed + logging);
	}

}
