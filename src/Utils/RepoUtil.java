package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;


import Const.Const;

public class RepoUtil {
	private Repository repo;
	private MemoryStore memStore;
	private NativeStore natStore;
	private File repoFile;
	private RepositoryConnection repoConn;
	private UriUtil subjUri;
	private UriUtil predUri;
	private UriUtil objUri;
	private PredicateUtil predUtil;
	private ValueFactory valueFactory;
	
	/**
	 * To get the repository within memory.
	 */
	public RepoUtil() {
		repoFile = new File(Const.repoPath);
		memStore = new MemoryStore();
		repo = new SailRepository(memStore);
	}
	
	/**
	 * To get the repository on the disk.
	 * @param repoPath the repository file path
	 */
	public RepoUtil(String repoPath) {
		repoFile = new File(repoPath);
		natStore = new NativeStore(repoFile);
		repo = new SailRepository(natStore);
	}
	
	/**
	 * To get the repository on the Http server.
	 * @param server the server address
	 * @param repoId the repository ID
	 */
	public RepoUtil(String server, String repoId) {
		repo = new HTTPRepository(server, repoId);
	}
	
	/**
	 * To initialize the repository and the connection.
	 */
	public void initialize() {
		subjUri = new UriUtil();
		predUri = new UriUtil();
		objUri = new UriUtil();
		predUtil = new PredicateUtil();
		valueFactory = new ValueFactoryImpl();
		try {
			repo.initialize();
			repoConn = repo.getConnection();
			repoConn.setAutoCommit(false);//why deprecate the setAutoCommit method?
		} catch(RepositoryException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Begin the add methods.
	 */
	public void begin() {
		try {
			if(repoConn.isActive()) {
				return;
			}
			repoConn.begin();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Commit all the transactions.
	 */
	public void commit() {
		try {
			repoConn.commit();
		} catch (RepositoryException e) {
			e.printStackTrace();
			try {
				repoConn.rollback();
			} catch (RepositoryException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * Roll back all the transactions of the repository.
	 */
	public void rollBack() {
		try {
			repoConn.rollback();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	
	public RepositoryConnection getConnection() {
		try {
			if(repoConn.isActive()) {
				return repoConn;
			} else {
				return repo.getConnection();
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * To set the subject namespace
	 * and type.
	 * @param ns
	 * @param type
	 */
	public void setSubjNsAndType(String ns, String type) {
		subjUri.setNameSpace(ns);
		subjUri.setType(type);
	}
	
	public void setNameSpace(String ns) {
		setNameSpace(ns,ns,ns);
	}
	
	public void setNameSpace(String subjNs, String predNs, String objNs) {
		subjUri.setNameSpace(subjNs);
		predUri.setNameSpace(predNs);
		objUri.setNameSpace(objNs);
	}
	
	public void setSubjType(String type) {
		subjUri.setType(type);
	}
	
	/**
	 * To set the predicate namespace
	 * and type.
	 * @param ns
	 * @param type
	 */
	public void setPredNsAndType(String ns, String type) {
		predUri.setNameSpace(ns);
		predUri.setType(type);
	}
	
	/**
	 * set the predicate type
	 * @param type
	 */
	public void setPredType(String type) {
		predUri.setType(type);
	}
	
	/**
	 * To set the object namespace
	 * and type.
	 */
	public void setObjNsAndType(String ns, String type) {
		objUri.setNameSpace(ns);
		objUri.setType(type);
	}
	
	public void setObjType(String type) {
		objUri.setType(type);
	}
	
	/**
	 * The URI-URI-Literal format SPO record.
	 */
	public void addRecord(URI subj, URI pred, Literal obj) {
		try {
//			repoConn = repo.getConnection();
			repoConn.add(subj, pred, obj);
//			repoConn.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * The URI-URI-URI format SPO record.
	 */
	public void addRecord(URI subj, URI pred, URI obj) {
		try {
//			repoConn = repo.getConnection();
			repoConn.add(subj, pred, obj);
//			repoConn.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	public <T> void addRecord (T a, String b) {
		System.out.println(b+" "+a);
	}
	
	/**
	 * /**
	 * The Str-Str-Str format SPO record.
	 * @param subjStr
	 * @param predStr
	 * @param objStr
	 * @param uriFlag If flag is true, obj is URI.
	 */
	//TODO The template method
	public void addRecord(String subjStr, String predStr, String objStr, boolean uriFlag) {
		Resource subj;
		URI pred;
		Value obj;
		Statement statement;
		if(subjStr.length() > 0) {
			subj = subjUri.getUri(subjStr);
		} else {
			subj = valueFactory.createBNode();
		}
		
		if(predUtil.isDefUri(predStr)) {
			pred = predUtil.getDefUri(predStr);
		} else {
			pred = predUri.getUri(predStr);
		}
		
		if(uriFlag) {
			obj = objUri.getUri(objStr);
			
		} else {
			obj = valueFactory.createLiteral(objStr);
		}
		statement = valueFactory.createStatement(subj, pred, obj);
		
		try {
//			repoConn = repo.getConnection();
			repoConn.add(statement);
		} catch (RepositoryException e) {
			e.printStackTrace();
		} 
//		finally {
//			try {
//				repoConn.close();
//			} catch (RepositoryException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	
	/**
	 * The Str-Str-Date format SPO record.
	 * @param subjStr
	 * @param predStr
	 * @param objDate
	 */
	public void addRecord(String subjStr, String predStr, Date objDate, boolean uriFlag) {
		Resource subj;
		URI pred;
		Value obj;
		Statement statement;
		try {
			if(subjStr.length() > 0) {
				subj = subjUri.getUri(subjStr);
			} else {
				subj = valueFactory.createBNode();
			}
			
			if(predUtil.isDefUri(predStr)) {
				pred = predUtil.getDefUri(predStr);
			} else {
				pred = predUri.getUri(predStr);
			}
			
			obj = valueFactory.createLiteral(objDate);

			statement = valueFactory.createStatement(subj, pred, obj);
			repoConn.add(statement);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The Str-Str-Boolean format SPO record.
	 * @param subjStr
	 * @param predStr
	 * @param objBool
	 */
	public void addRecord(String subjStr, String predStr, boolean objBool, boolean uriFlag) {
		Resource subj;
		URI pred;
		Value obj;
		Statement statement;
		try {
			if(subjStr.length() > 0) {
				subj = subjUri.getUri(subjStr);
			} else {
				subj = valueFactory.createBNode();
			}
			
			if(predUtil.isDefUri(predStr)) {
				pred = predUtil.getDefUri(predStr);
			} else {
				pred = predUri.getUri(predStr);
			}
			
			obj = valueFactory.createLiteral(objBool);

			statement = valueFactory.createStatement(subj, pred, obj);
			repoConn.add(statement);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The Str-Str-Boolean format SPO record.
	 * @param subjStr
	 * @param predStr
	 * @param objBool
	 */
	public void addRecord(String subjStr, String predStr, long objBool, boolean uriFlag) {
		Resource subj;
		URI pred;
		Value obj;
		Statement statement;
		try {
			if(subjStr.length() > 0) {
				subj = subjUri.getUri(subjStr);
			} else {
				subj = valueFactory.createBNode();
			}
			
			if(predUtil.isDefUri(predStr)) {
				pred = predUtil.getDefUri(predStr);
			} else {
				pred = predUri.getUri(predStr);
			}
			
			obj = valueFactory.createLiteral(objBool);

			statement = valueFactory.createStatement(subj, pred, obj);
			repoConn.add(statement);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The RDF reader for insert records
	 * with stream reader.
	 */
	public void addRecords(String rdfPath, String baseURI, RDFFormat format) {
		File rdfFile = new File(rdfPath);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(rdfFile)));
			repoConn.begin();
//			repoConn = repo.getConnection();
			repoConn.add(reader, baseURI, format);
//			repoConn.close();
			repoConn.commit();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (RepositoryException e) {
			try {
				repoConn.rollback();
			} catch (RepositoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**@deprecated
	 * Read the RDF file with SPO format in stream.
	 */
	public void addRecords(String spoFilePath) {
//		File spoFile = new File(spoFilePath);
		InputStream in = null;
		try {
			in = new FileInputStream(spoFilePath);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		BufferedReader reader = null;
		String line = null;
		String[] segments = null;
		String subjStr, predStr, objStr;
		URI subj, pred;
		Literal lit = null;
		Statement stat = null;
//		ArrayList<Statement> model = new ArrayList<Statement>();
		LinkedHashModel model = new LinkedHashModel();
		
		System.out.println("Start reading file in line!");
		reader = new BufferedReader(new InputStreamReader(in));
		
		try {
			while( (line = reader.readLine()) != null) {
				if(line.length() <= 0) {
					continue;
				}
				//TODO
				segments = line.split("\t");
				subjStr = segments[0];
				predStr = segments[1];
				objStr = segments[2];
				ValueFactory vf = repo.getValueFactory();
				subj = vf.createURI(subjStr);
				pred = vf.createURI(predStr);
				lit = vf.createLiteral(objStr);
				stat = vf.createStatement(subj, pred, lit);
				model.add(stat);
			}
			reader.close();
			in.close();
			repoConn = repo.getConnection();
			repoConn.add(model);
			saveRDFTurtle(model, ".//test.n3", RDFFormat.N3);
			repoConn.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save triples into RDF files 
	 * with the specific RDF format.
	 * @param model the model including statements of SPO
	 * @param filePath the file path to save the records
	 * @param rdfFormat the rdf file format like RDFFormat.N3
	 */
	/*
	 * 
	 */
	public void saveRDFTurtle(LinkedHashModel model, String filePath, RDFFormat rdfFormat) {
		try {
			FileOutputStream out = new FileOutputStream(filePath);
			RDFWriter writer = null;
			writer = Rio.createWriter(rdfFormat, out);
			writer.startRDF();
			for(Statement stat: model) {
				writer.handleStatement(stat);
			}
			writer.endRDF();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * To save the triples in RDF turtle format
	 * directly from the Sesame database. 
	 * The third parameter is "self".
	 * @param filePath the file path to save the records
	 * @param rdfFormat the rdf file format like RDFFormat.N3
	 * @param self any string would be ok
	 */
	public void saveRDFTurtle(String filePath, RDFFormat rdfFormat, String self) {
		System.out.println("-------Outputing--------");
		try {
			FileOutputStream out = new FileOutputStream(filePath);
			RDFWriter writer = Rio.createWriter(rdfFormat, out);
			RepositoryConnection newConn = repo.getConnection();
			newConn.export(writer);
			newConn.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		}
		System.out.println("-------Output ends--------");
	}
}
