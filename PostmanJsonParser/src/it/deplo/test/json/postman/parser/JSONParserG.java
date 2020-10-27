package it.deplo.test.json.postman.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class JSONParserG implements BiConsumer<Object, Object>, Consumer<Object> {
	
	private static final Logger logger = LogManager.getLogger(JSONParserG.class);
	
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
	protected String getSettingCollectionVariableString(String mainElement, String chiave, String subtree, String numberedVariableName) {
		
		try {
			String snakedVariableName = getseparatedCase(mainElement, chiave, "_");
			String variableName = getNumberedVariable(snakedVariableName);

			boolean thisIsArray = isJSONArray();
			boolean parentIsNull = ( parent == null );
			boolean parentIsArray = !parentIsNull && parent.isJSONArray();
			
			long parentHashCode = (parent == null) ? 0l : parent.hashCode();
			
			logger.debug("-----------------------------------------");
			logger.debug("------------>[" + thisIsArray + "]");
			logger.debug("------------>parent[" + parentHashCode + "] currentnode[" + currentNode + "] chiave[" + chiave + "] subtree[" + subtree + "]");
			logger.debug("------------>[" + parentIsArray +"]");
			
			logger.debug("-----------------------------------------\n\n\n");
			
			// rimpiazza il JSON con i nomi delle variabili
			if ( getParent() != null && getParent().isJSONArray() || isJSONArray() ) {
				String regexSubtree = "(<string>|<date>|<double>|<boolean>)";
				subtree = subtree.replaceFirst(regexSubtree, getVariabilizedName(numberedVariableName) );

				((HashMap<Object, Object>)jsonToParse).replace(mainElement, getVariabilizedName(numberedVariableName) );
				
				String regexChiave = "(" + chiave + ")";
				chiave = chiave.replaceFirst(regexChiave, numberedVariableName );
		
					
				System.out.println("ARRAY CHIAVE[" + chiave + "] numbered[" + numberedVariableName + "] subtree[" + subtree + "]");
					
			} else {
				((HashMap<Object, Object>)jsonToParse).replace(chiave, getVariabilizedName(variableName) );
			}
			
			String definitionRow = "\nvar " + variableName + " = \n";
			
			return definitionRow + "pm.collectionVariables.set(\"" + variableName + "\", " + variableName + ");";
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
	protected String getFullTestString(String mainElement, Object chiave, String numberedVariableName ) {

		StringBuffer sb = new StringBuffer();

		String jeararchizedObject = getseparatedCase(mainElement, chiave, ".");
		String snakedVariableName = getseparatedCase(mainElement, chiave, "_");
		
			
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
				String arrayNameDotKey = getseparatedCase(numberedVariableName, chiave, ".");
			
				sb.append("const "+ arrayNameDotKey + " = jsonData." + mainElement + ".find (m => m." + chiave + " === pm.collectionVariables.get(\"" + numberedVariableName + "\") );\n");

		    //pm.expect(sports).to.include(pm.collectionVariables.get("sportCorrenteID"), "Impossibile verificare lo sport");
		    //pm.expect(sports).to.be.an("object", "Impossibile ottenere l'oggetto sport")
		    sb.append("pm.expect(" + arrayNameDotKey + ").include(pm.collectionVariables.get(\"" + numberedVariableName + "\"), \"Impossibile verificare " + numberedVariableName + "\")");

		} else {
			sb.append("pm.expect(jsonData." + jeararchizedObject +").to.eql(pm.collectionVariables.get(\"" + numberedVariableName + "\"), \"Impossibile verificare il campo [" + snakedVariableName + "] valore memorizzato[\" + " + getCollectionVariableGET(numberedVariableName) + " + \"]\")");			
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
		
		currentNode = chiave.toString();
		
		if ( ! ( subtree instanceof JSONArray ) && ! ( subtree instanceof JSONObject )   ) {
			print( chiave.toString(), subtree.toString(), getNumberedVariable( chiave.toString() ));
			
//			coppieChiaviVariabile.put(chiave, getSnakedCase(padre, chiave));
		
		} else if ( subtree instanceof JSONArray ) {
			//System.out.println("PARSELEMENT ARRAY [" + chiave + "] [" + subtree + "]");
			JSONArray jsonArray = (JSONArray) subtree;
			JSONParserG arrayParser = new JSONParserG(this, chiave, jsonArray);
			jsonArray.forEach( arrayParser );
			currentNode = null;
		} else {
			//System.out.println("PARSELEMENT OBJECT [" + chiave + "] [" + subtree + "]");
			JSONObject jsonObject = (JSONObject) subtree;
			JSONParserG jsonParser = new JSONParserG(this, chiave, jsonObject);
			jsonObject.forEach( jsonParser );
		}
	}

	private void print(String chiave, String subtree, String numberedVariable) {
		

		String creazioneVariabile = getSettingCollectionVariableString(currentNode, chiave, subtree, numberedVariable);		
		String rigaTest = getFullTestString(currentNode, chiave, numberedVariable);
		
		listaIstruzioniCreazioneVariabiliPostman.add(creazioneVariabile);
		listaIstruzioniTestPostman.add(rigaTest);
		}
	
	
	public void printAll() {
		logger.info("\n############### listaOrdinataTestPostman ######################");
		for (String rigaCorrenteTest : listaIstruzioniTestPostman) {
			System.out.println(rigaCorrenteTest);
		}
		logger.info("#############-- FINE listaOrdinataTestPostman --####################");
		
		
		logger.info("\n############### listaOrdinataCreazioneVariabiliPostman ######################");
		for (String rigaCorrenteVariabile : listaIstruzioniCreazioneVariabiliPostman) {
			System.out.println(rigaCorrenteVariabile);
		}
		logger.info("#############-- FINE listaOrdinataCreazioneVariabiliPostman --####################");
		
		
		logger.info("\n############### listaOrdinataCreazioneVariabiliPostman ######################");
		System.out.println(jsonToParse);
		logger.info("#############-- FINE listaOrdinataCreazioneVariabiliPostman --####################");
	}
	
	
	private String getNumberedVariable(String variableName ) {
		
		int i = 0;
		
		while ( listaNomiUniciVariabili.contains( (variableName + i)  ) ) {
			i++;
		}
		
		variableName = variableName + i;
		
		listaNomiUniciVariabili.add(variableName);
		
		return variableName;
		
	}

	private String getVariabilizedName ( String variableName ) {
		return new String("{{" + variableName + "}}" );
	}
	
	
	@Override
	public void accept(Object subtree) {
		if ( currentNode == null ) {
			logger.debug("CurrentNode [" + currentNode + "]");
			currentNode = "";
		}
		logger.debug("Parsing Array for currentnode[" + currentNode + "] subtree[" + subtree + "]");
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
