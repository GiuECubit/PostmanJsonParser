package it.deplo.test.json.postman.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class JSONParserG implements BiConsumer<Object, Object>, Consumer<Object> {
	
	protected String currentNode = null;
	
	
	protected JSONAware  jsonToParse;


	private JSONParserG parent;


	private static ArrayList<String> listaIstruzioniTestPostman = new ArrayList<String>();
	private static ArrayList<String> listaIstruzioniCreazioneVariabiliPostman = new ArrayList<String>();
	
	private static ArrayList<String> listaNomiUniciVariabili = new ArrayList<String>();
	
	
	public JSONParserG(JSONAware jsonToParse) {
		this.jsonToParse = jsonToParse;
	}
	
	public JSONParserG(JSONParserG jsonParserG, Object currentNode) {
		this.parent = jsonParserG;
		this.currentNode = currentNode.toString();
	}

	public JSONParserG(JSONParserG jsonParserG, Object _currentNode, JSONAware jsonToParse) {
		this( jsonParserG,_currentNode);
		this.jsonToParse = jsonToParse;
	}
	
	
	public boolean isJSONArray() {
		return ( this.jsonToParse instanceof JSONArray );
	}
	
	public boolean isJSONObject() {
		return ( this.jsonToParse instanceof JSONObject );
	}
	


	public JSONParserG getParent() {
		return parent;
	}
	
	
	@SuppressWarnings("unchecked")
	protected String getSettingCollectionVariableString(String mainElement, String chiave, String subtree) {
		
		try {
			String snakedVariableName = getseparatedCase(mainElement, chiave, "_");
			String variableName = getVariable(snakedVariableName);

			boolean thisIsArray = isJSONArray();
			boolean parentIsNull = ( parent == null );
			boolean parentIsArray = !parentIsNull && parent.isJSONArray();
			
			long parentHashCode = (parent == null) ? 0l : parent.hashCode();
			
			System.out.println("-----------------------------------------");
			System.out.println("------------>[" + thisIsArray + "]");
			System.out.println("------------>parent[" + parentHashCode + "] currentnode[" + currentNode + "] chiave[" + chiave + "] subtree[" + subtree + "]");
			System.out.println("------------>[" + parentIsArray +"]");
			
			System.out.println("-----------------------------------------\n\n\n");
			
			// rimpiazza il JSON con i nomi delle variabili
			if ( getParent() != null && getParent().isJSONArray() || isJSONArray() ) {
					System.out.println("CHIAVE[" + chiave + "] subtree[" + subtree + "]");
			} else {
				((HashMap<Object, Object>)jsonToParse).replace(chiave, getVariabilizedName(variableName) );
			}
			
			return "pm.collectionVariables.set(\"" + variableName + "\", " + variableName + ");";
		} catch ( Exception e ) {
			throw new RuntimeException("Impossibile  ottenere il getSettingCollectionVariableString per mainelement[" + mainElement + "] chiave[" + chiave + "] subtree[" + subtree + "]", e );
		}
	}

		
	/**
	 * Restituisce il nome di una variabile in snaked_case minuscolo
	 *  
	 * @param padre: prima parte della variabile, uguale al nodo padre
	 * @param chiave: seconda parte della variabile, che identifica il figlio
	 * @return
	 */
	private String getseparatedCase(String padre, Object chiave, String separator) {
		
		String composed = chiave.toString();
		
		if ( padre != null ) {
			composed = padre + separator + chiave.toString();
		}
		return composed;
	}
		
	
	/**
	 * Restituisce la riga di test compilata da inserire nella parte TEST della chiamata Postman
	 * 
	 * @param mainElement
	 * @param chiave
	 * @return
	 */
	protected String getFullTestString(String mainElement, Object chiave) {

		StringBuffer sb = new StringBuffer();

		String jeararchizedObject = getseparatedCase(mainElement, chiave, ".");
		String snakedVariableName = getseparatedCase(mainElement, chiave, "_");
		String variableName = getVariable(snakedVariableName);
		
		/*
		 * const sports = jsonData.sport.find
	      (m => m.id === pm.collectionVariables.get("sportCorrenteID"));

	    //pm.expect(sports).to.include(pm.collectionVariables.get("sportCorrenteID"), "Impossibile verificare lo sport");
	    //pm.expect(sports).to.be.an("object", "Impossibile ottenere l'oggetto sport")
	    pm.expect(sports.id).include(pm.collectionVariables.get("sportCorrenteID"), "Impossibile verificare lo sport")
		 */
		
		// se e' un array la stringa di ricerca deve essere diversa
		//if ( jsonToParse instanceof JSONArray ) {
		 if ( getParent() != null && getParent().isJSONArray() || isJSONArray() ) {
				String arrayNameDotKey = getseparatedCase(variableName, chiave, ".");
			
				sb.append("const "+ arrayNameDotKey + " = jsonData." + mainElement + ".find (m => m." + chiave + " === pm.collectionVariables.get(\"" + variableName + "\") );\n");

		    //pm.expect(sports).to.include(pm.collectionVariables.get("sportCorrenteID"), "Impossibile verificare lo sport");
		    //pm.expect(sports).to.be.an("object", "Impossibile ottenere l'oggetto sport")
		    sb.append("pm.expect(" + arrayNameDotKey + ").include(pm.collectionVariables.get(\"" + variableName + "\"), \"Impossibile verificare " + variableName + "\")");

		} else {
			sb.append("pm.expect(jsonData." + jeararchizedObject +").to.eql(pm.collectionVariables.get(\"" + snakedVariableName + "\"), \"Impossibile verificare il campo [" + snakedVariableName + "] valore memorizzato[\" + " + getCollectionVariableGET(snakedVariableName) + " + \"]\")");			
		}

		return sb.toString() + "\n";
	}
	
	
	
	private String getCollectionVariableGET( String snakedVariable ) {
		return "pm.collectionVariables.get(\"" + snakedVariable + "\")";
	}


	/**
	 * Analizza il JSON affidando il parsing corretto fino al dettaglio della chiave 
	 * 
	 * RICORSIVO
	 * 
	 * @param chiave
	 * @param subtree
	 * @param consumer
	 */
	@SuppressWarnings("unchecked")
	protected void parseElement(Object chiave, Object subtree) {

		if ( ! ( subtree instanceof JSONArray ) && ! ( subtree instanceof JSONObject )   ) {
			print(chiave.toString(), subtree.toString());
			
//			coppieChiaviVariabile.put(chiave, getSnakedCase(padre, chiave));
		
		} else if ( subtree instanceof JSONArray ) {
			System.out.println("PARSELEMENT ARRAY [" + chiave + "] [" + subtree + "]");
			JSONArray jsonArray = (JSONArray) subtree;
			currentNode = chiave.toString();
			JSONParserG arrayParser = new JSONParserG(this, chiave, jsonArray);
			jsonArray.forEach( arrayParser );
			currentNode = null;
		} else {
			System.out.println("PARSELEMENT OBJECT [" + chiave + "] [" + subtree + "]");
			JSONObject jsonObject = (JSONObject) subtree;
			JSONParserG jsonParser = new JSONParserG(this, chiave, jsonObject);
			jsonObject.forEach( jsonParser );
		}
	}

	private void print(String chiave, String subtree) {
		
		String creazioneVariabile = getSettingCollectionVariableString(currentNode, chiave, subtree);		
		String rigaTest = getFullTestString(currentNode, chiave);
		
		listaIstruzioniCreazioneVariabiliPostman.add(creazioneVariabile);
		listaIstruzioniTestPostman.add(rigaTest);
		}
	
	
	public void printAll() {
		System.out.println("\n############### listaOrdinataTestPostman ######################");
		for (String rigaCorrenteTest : listaIstruzioniTestPostman) {
			System.out.println(rigaCorrenteTest);
		}
		System.out.println("#############-- FINE listaOrdinataTestPostman --####################");
		
		System.out.println("\n############### listaOrdinataCreazioneVariabiliPostman ######################");
		for (String rigaCorrenteVariabile : listaIstruzioniCreazioneVariabiliPostman) {
			System.out.println(rigaCorrenteVariabile);
		}
		System.out.println("#############-- FINE listaOrdinataCreazioneVariabiliPostman --####################");
		
		
		System.out.println("\n############### listaOrdinataCreazioneVariabiliPostman ######################");
		System.out.println(jsonToParse);
		System.out.println("#############-- FINE listaOrdinataCreazioneVariabiliPostman --####################");
	}
	
	
	private String getVariable(String variableName ) {
		
		while ( listaNomiUniciVariabili.contains( variableName ) ) {
			variableName = variableName + "_";
		}
		
		listaNomiUniciVariabili.add(variableName);
		
		return variableName;
		
	}

	private String getVariabilizedName ( String variableName ) {
		return new String("{{" + variableName + "}}" );
	}
	
	
	@Override
	public void accept(Object subtree) {
		System.out.println("Parsing Array for currentnode[" + currentNode + "] subtree[" + subtree + "]");
		parseElement(currentNode, subtree);
	}

	@Override
	public void accept(Object key, Object subTree) {
		parseElement(key, subTree);
	}
	
	/*
	 * const sports = jsonData.sport.find
      (m => m.id === pm.collectionVariables.get("sportCorrenteID"));

    //pm.expect(sports).to.include(pm.collectionVariables.get("sportCorrenteID"), "Impossibile verificare lo sport");
    //pm.expect(sports).to.be.an("object", "Impossibile ottenere l'oggetto sport")
    pm.expect(sports.id).include(pm.collectionVariables.get("sportCorrenteID"), "Impossibile verificare lo sport")
	 */
}
