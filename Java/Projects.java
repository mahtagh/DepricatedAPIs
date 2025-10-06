package MAHTA.Mahta.Final;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import MAHTA.Mahta.Controller;
import MAHTA.Mahta.MethodCall;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class Projects {

	public static ArrayList<TableMethod> methodcalls = new ArrayList<TableMethod>();
	public static ArrayList<TableMethod> tablemethods = new ArrayList<Projects.TableMethod>();

	public static Pom parsingXML(String pomPath) {
		String fileName = pomPath;
		ArrayList<String> libraries = new ArrayList<String>();
		Pom pom = null;
		try {
			File inputFile = new File("D:\\python\\pom\\" + pomPath + ".xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
//			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("dependencies");
//			System.out.println("----------------------------");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
//				System.out.println("\nCurrent Element :" + nNode.getNodeName());

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					NodeList a = eElement.getElementsByTagName("dependency");
					for (int i = 0; i < a.getLength(); i++) {
						Node n = a.item(i);
//						System.out.println("dependency: " + n.getNodeName());
						if (n.getNodeType() == Node.ELEMENT_NODE) {
							Element e = (Element) n;
							try {
								libraries.add(e.getElementsByTagName("artifactId").item(0).getTextContent()
										+ e.getElementsByTagName("version").item(0).getTextContent());
//							System.out.println(e.getElementsByTagName("artifactId").item(0).getTextContent());
//							System.out.println(e.getElementsByTagName("version").item(0).getTextContent());
							} catch (Exception ex) {
								// TODO: handle exception
							}
						}
					}

				}
			}
			pom = new Pom(fileName, libraries);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pom;
	}

	public static String readFromInputStream(String path) {
		StringBuilder resultStringBuilder = new StringBuilder();
		try {
			File myObj = new File(path);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				resultStringBuilder.append(data).append("\n");
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		return resultStringBuilder.toString();
	}

	public static ArrayList<String> listFilesForFolder(final File folder) {
		ArrayList<String> deprecatedFiles = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			// System.out.println(fileEntry.getName());
			String name = fileEntry.getName();
			String filePath = folder.toString() + "/" + name;
			// String file = readFromInputStream(filePath);
			deprecatedFiles.add(filePath);
		}
		return deprecatedFiles;
	}

	public static class Pom {
		String nameFile;
		ArrayList<String> libraries = new ArrayList<String>();

		public Pom(String nameFile, ArrayList<String> libraries) {
			super();
			this.nameFile = nameFile;
			this.libraries = libraries;
		}

		public String getAllLibraries() {
			String result = "";
			int i = 0;
			for (String s : libraries) {
				if (!s.trim().isEmpty()) {
//					System.out.println("lib is: " + s + s.trim().isEmpty());
					if (i == libraries.size() - 1) { // the last one is empty
						result = result + "\"" + s + "\"";
					} else {
						result = result + "\"" + s + "\",";
					}
				}
				i++;
			}
//			System.out.println(result);
			return result;
		}

	}

//	public static ArrayList<Pom> getLibraries() {
//
//		String s = readFromInputStream("D:\\python\\test.txt");
////		System.out.println(s);
//		ArrayList<Pom> pom = new ArrayList<Pom>();
//		String name = "";
//		ArrayList<String> libraries = new ArrayList<String>();
//		String[] each = s.split(",");
//		for (String e : each) {
////			System.out.println(e);
//			String[] lib = e.split("#");
//			for (String l : lib) {
////				 System.out.println(l);
//				if (l.endsWith(".xml")) {
////					System.out.println("-------");
//					name = l.replace(".xml", "");
//					pom.add(new Pom(name, libraries));
//					libraries.removeAll(libraries);
//				} else {
//					libraries.add(l);
//				}
//			}
//		}
////		for (Pom p : pom) {
////			System.out.println(p.nameFile + " " + p.libraries.toString());
////		}
//		return pom;
//	}

	public static Connection db_connect() {
		Connection conn = null;

		try {
			conn = DriverManager
					.getConnection("jdbc:mysql://localhost:3306/deprecation?" + "user=mahta&password=abc123");

		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return conn;
	}

	public static void insertToDatabase() {
		Connection conn = db_connect();
		Statement stmt = null;

		try {
			PreparedStatement statement = conn.prepareStatement(
					"INSERT INTO `projects_log`(project_name,project_version,project_year,method_name,par_num,library_name,library_version) "
							+ "VALUE (?, ? ,? ,?,?,?,?)");

			int count = 0;

			for (TableMethod dp : methodcalls) {
				statement.setString(1, dp.project);
				statement.setString(2, dp.versionproject);
				statement.setString(3, dp.pyear);
				statement.setString(4, dp.name);
				statement.setString(5, Integer.toString(dp.numPar));
				statement.setString(6, dp.library);
				statement.setString(7, dp.version);

				statement.addBatch();
				count++;
				// execute every 100 rows or less
				if (count % 1 == 0 || count == methodcalls.size()) {
					statement.executeBatch();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static class TableMethod {
		String name;
		String library;
		int numPar;
		String version;

		String project;
		String versionproject;
		String pyear;

		public TableMethod(String name, String library, int numPar, String version, String path) {
			super();
			this.name = name;
			this.library = library;
			this.numPar = numPar;
			this.version = version;
//			System.out.println(path);
			path = path.replaceAll("/", "=").replace("\\", "=");
			String[] splitPath = path.split("=");
			// System.out.println(splitPath[2]);
			String[] splitName = splitPath[3].split("_");
//			 System.out.println("lib " + splitName[0]+" year " + splitName[1] + "v " +
//			 splitName[2]);
			this.project = splitName[0];
			this.pyear = splitName[1];
			this.versionproject = splitName[2];

		}

		public TableMethod(String name, String library, String numPar, String version) {
			super();
			this.name = name;
			this.library = library;
			this.numPar = Integer.parseInt(numPar);
			this.version = version;
		}

	}

	public static ArrayList<TableMethod> getLibrariesTable(String S) {
		ArrayList<TableMethod> tm = new ArrayList<Projects.TableMethod>();
		String sql = "SELECT DISTINCT name,parameter_number,library,version FROM methods_log WHERE status = \"dep\" AND CONCAT(library,version) IN ("
				+ S + ");";
		Connection conn = db_connect();

		try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
//			 System.out.println(sql);
			ResultSet rs = pstmt.executeQuery();
			// System.out.println("hi");
			while (rs.next()) {
				tm.add(new TableMethod(rs.getString("name"), rs.getString("library"), rs.getString("parameter_number"),
						rs.getString("version")));
//				System.out.println(rs.getString("name") + "\t" + rs.getString("parameter_number") + "\t"
//						+ rs.getString("library"));

			}
			pstmt.close();
		}
		// Handle any errors that may have occurred.
		catch (SQLException e) {
			// System.out.println("hi");
			e.printStackTrace();
		}
		return tm;
	}

	public static void getMethodCall(File file) {

		ArrayList<String> deprecated;
		deprecated = listFilesForFolder(file);

		CompilationUnit cu;
		try {
			// System.out.println(deprecated.size());
			for (String path : deprecated) {
				cu = StaticJavaParser.parse(new File(path));
//				 System.out.println(path);
				ArrayList<TableMethod> className = new ArrayList<TableMethod>();
				VoidVisitor<ArrayList<TableMethod>> classNameVisitor = new ClassNameCollector();
				// Visit.
				classNameVisitor.visit(cu, className);
//				System.out.println(className.size());
				// Print Class's name

				// mds is full now

				for (TableMethod data : className) {
//					System.out.println(methods.get(s));
//					methodcalls.add(data);
					TableMethod temp = new TableMethod(data.name, data.library, data.numPar, data.version, path);
					methodcalls.add(temp);
//					 System.out.println(methodcalls.get(methodcalls.size()-1).versionproject);
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.toString());
			e.printStackTrace();
		}

	}

	public static class ClassNameCollector extends VoidVisitorAdapter<ArrayList<TableMethod>> {
		@Override
		public void visit(ClassOrInterfaceDeclaration n, ArrayList<TableMethod> collector) {
			super.visit(n, collector);

			for (MethodDeclaration method : n.getMethods()) {
				// Make the visitor go through everything inside the method.
				ArrayList<TableMethod> mds = new ArrayList<TableMethod>();
				method.accept(new MethodCallVisitor(), mds);
				collector.addAll(mds);
			}

		}
	}

	private static class MethodCallVisitor extends VoidVisitorAdapter<ArrayList<TableMethod>> {
		// public ArrayList<String> mds = new ArrayList<String>();
		@Override
		public void visit(MethodCallExpr n, ArrayList<TableMethod> arg) {
			// Found a method call
			for (TableMethod t : tablemethods) {
//				System.out.println(n.getNameAsString() + " " + t.name + "---" + n.getArguments().size() + " " + t.numPar );
				if (n.getNameAsString().equals(t.name) && n.getArguments().size() == t.numPar) {
//					System.out.println("found");
					arg.add(t);
				}

			}
			super.visit(n, arg);

		}
	}

	public static void main(String[] args) {

		final File folder = new File("D:\\python\\project");

		if (folder.isDirectory()) {
			// System.out.println();
			String[] fileList = folder.list();

			for (String str : fileList) {
				final File file = new File("D:\\python\\project" + "/" + str);
				System.out.println("---------" + str);
				Pom p = parsingXML(str);
//				System.out.println(p.getAllLibraries());
				tablemethods = getLibrariesTable(p.getAllLibraries());

				getMethodCall(file);

				System.out.println(methodcalls.size());
//				for (TableMethod t : methodcalls) {
//					System.out.println(t.versionproject);
//				}
			}

		}
		insertToDatabase();
		System.out.println("done!");
	}
}
