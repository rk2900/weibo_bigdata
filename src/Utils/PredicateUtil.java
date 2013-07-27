package Utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;

public class PredicateUtil{
	private HashMap<String,URI> defaultUri;
	private ArrayList<String> propertyPred;
	
	public PredicateUtil() {
		super();
		defaultUri = new HashMap<String,URI>();
		propertyPred = new ArrayList<String>();
		initialize();
	}
	/**
	 * Read the default RDF file
	 * and create default RDF set.
	 */
	private void initialize() {
		//TODO
//		defaultUri.put("name", RDFS.LABEL);
		defaultUri.put("weiboText", RDFS.LITERAL);
//		defaultUri.put("commentContext", RDFS.COMMENT);
//		
//		//TODO
//		propertyPred.add("name");
//		propertyPred.add("friend");
//		propertyPred.add("follower");
//		propertyPred.add("createWeibo");
//		propertyPred.add("createComment");
//		propertyPred.add("commentTo");
	}
	
	/**
	 * To insert one default URI record.
	 * @param key the name of the default URI
	 * @param uri the true URI of the default URI
	 */
	public void insertDefaultUri(String key, URI uri) {
//		String uriKey = new String(key.toUpperCase());
		String uriKey = new String(key);
		ValueFactory valueFactory = new ValueFactoryImpl();
		URI defUri = valueFactory.createURI(uri.toString());
		defaultUri.put(uriKey, defUri);
	}
	
	public void insertPropertyPred(URI uri) {
		propertyPred.add(uri.toString());
	}
	
	public boolean isDefUri(String predStr) {
		return defaultUri.containsKey(predStr);
	}
	
	public URI getDefUri (String predStr) {
		return defaultUri.get(predStr);
	}

}
