package it.deplo.test.json.postman.parser.handler;

import java.util.function.Consumer;

public class ArrayPostmanConsumer implements Consumer<String> {

	public ArrayPostmanConsumer(Object chiave, String snakedVariableName) {

	}

	@Override
	public void accept(String chiave) {
		
		System.out.println("Sto CONSUMANDO L'ARRAY [" + chiave + "]");
		
	}
	
	

}
