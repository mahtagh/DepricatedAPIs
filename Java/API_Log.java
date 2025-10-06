package MAHTA.Mahta.Final;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import javax.sound.midi.Soundbank;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class API_Log {

	public static ArrayList<ArrayList<Method>> Data = new ArrayList<ArrayList<Method>>();

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
					"INSERT INTO `methods_log`(library,version,Date,return_type,name,parameter,signature,parameter_number,status) "
							+ "VALUE (?, ?, ? ,? ,?, ?,?, ?, ?)");

			int count = 0;

			for (ArrayList<Method> d : Data) {
				for (Method m : d) {
					statement.setString(1, m.library);
					statement.setString(2, m.version);
					statement.setString(3, m.year);
					statement.setString(4, m.type);
					statement.setString(5, m.name);
					statement.setString(6, m.parameter.toString());
					statement.setString(7, m.getSigniture(m));
					statement.setString(8, m.parameter.size() + "");
					statement.setString(9, m.status);

					statement.addBatch();
					count++;
					// execute every 10 rows or less
					// System.out.println(d.size());
					if (count % 1 == 0) {
						statement.executeBatch();
					}
				}

			}
			System.out.println(count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	// all pathes
	public static ArrayList<String> listFilesForFolder(final File folder) {
		// folder --> each version of library
		ArrayList<String> deprecatedFiles = new ArrayList<String>();
		// System.out.println("---" + folder.getName());
		for (final File fileEntry : folder.listFiles()) {
			// each java files in each folder
			// System.out.println(fileEntry.getName());
			String name = fileEntry.getName();
			String filePath = folder.toString() + "/" + name;
			String file = readFromInputStream(filePath);
			deprecatedFiles.add(filePath);
			// System.out.println("bye");
		}
		return deprecatedFiles;
	}

	public static ArrayList<Method> getMethodOfVersion(File version) {
		// folder = name of each folder
		// System.out.println(version.getName());
		ArrayList<Method> allMethodVersion = new ArrayList<Method>();
		for (final File fileEntry : version.listFiles()) {
			// each java files in each version
			// System.out.println(fileEntry.getName());
			String name = fileEntry.getName();
			String filePath = version.toString() + "/" + name;
			//System.out.println(filePath);
			String file = readFromInputStream(filePath);
			CompilationUnit cu = StaticJavaParser.parse(file);

			MethodNamePrinter methodNameVisitor = new MethodNamePrinter();
			methodNameVisitor.visit(cu, null);
			ArrayList<Method> methods = methodNameVisitor.mds;
			for (Method m : methods) {
				//System.out.println(m.annotation);
				allMethodVersion.add(new Method(filePath, m));
			}
		}
		// System.out.println(allMethodVersion.get(0).library);
//		for(Method m: allMethodVersion) {
//			System.out.println(m.name);
//		}
		return allMethodVersion;
	}

	public static ArrayList<Method> getDeprecated(ArrayList<Method> arg) {
		ArrayList<Method> result = new ArrayList<Method>();
		for (Method m : arg) {
			// System.out.println(m.name);
			NodeList<AnnotationExpr> annotation = m.annotation;
			//System.out.println(m.annotation);
			if (!annotation.isEmpty()) {
				for (int j = 0; j < annotation.size(); j++) {
					//System.out.println(annotation.size());
					if (annotation.get(j).toString().toLowerCase().equals("@deprecated")) {
						result.add(m);
						// System.out.println(m.name);
					}
				}
			}
		}
		// System.out.println("inside method " + result.size());
		return result;
	}

	public static void createData() {
		// a list kolie isEmpty? --> get deprecated
		// a not empty libraryname last = new library --> ghabli+ deprecated jadid
		// a not empty libname != new lib --> get deprecated
		final File folder = new File("D:/Java");
		int count = 0;
		if (folder.isDirectory()) {
			String[] fileList = folder.list();
			for (String str : fileList) {
				final File file = new File("D:/Java" + "/" + str);
				System.out.println("D:/Java" + "/" + str);
				ArrayList<Method> methods = getMethodOfVersion(file);
				ArrayList<Method> deprecatedmethods = getDeprecated(methods);
				count = count + deprecatedmethods.size();
				 //System.out.println(deprecatedmethods.size());
				// System.out.println(deprecatedmethods.size());
				if (!deprecatedmethods.isEmpty()) {
					if (Data.isEmpty()) {
						for (Method m : deprecatedmethods) {
							m.status = "dep";
						}
						Data.add(deprecatedmethods);
					}
					if (!Data.isEmpty()) {
						//System.out.println((Data.get(Data.size() - 1).get(0).library));
						if (!Data.get(Data.size() - 1).get(0).library.equals(deprecatedmethods.get(0).library)) {
							//System.out.println(Data.get(Data.size() - 1).get(0).library + " " + deprecatedmethods.get(0).library);
							// System.out.println("hi1");
							for (Method m : deprecatedmethods) {
								m.status = "dep";
							}
							Data.add(deprecatedmethods);
						} else {
							ArrayList<Method> previous = Data.get(Data.size() - 1);
							ArrayList<Method> now = new ArrayList<Method>();
							for (Method p : previous) {
								boolean match = false;
								for (Method m : methods) {
									if (m.getSigniture(m).equals(p.getSigniture(p))) {
										match = true;
										//System.out.println(m.annotation.toString().toLowerCase());
										if (m.annotation.toString().toLowerCase().equals("@deprecated")) {
											//System.out.println("hi");
//											m.status = "dep";
//											now.add(m);
										}
										else {
											m.status = "undep";
											now.add(m);
										}
										break;
									}
								}
								if (match == false) {
									//System.out.println("hi");
									Method m = new Method(p,"del");
									//p.status = "del";
									now.add(m);
								}
							}
							// System.out.println("hi");
							for (Method m : deprecatedmethods) {
								m.status = "dep";
								now.add(m);
							}							
							Data.add(now);
							// System.out.println(now.get(0).name + " " +
							// Data.get(Data.size()-1).get(0).name);
						}
					}

				}
			}

		}
		//System.out.println(count);
	}

	public static class Method {
		NodeList<AnnotationExpr> annotation;
		String type;
		String name;
		NodeList<Parameter> parameter;
		String library;
		String version;
		String year;
		String status;

		public Method() {
			// this.name= "-------------";
		}
		
		public Method(Method method, String status) {
			// this.name= "-------------";
			this.annotation = method.annotation;
			this.type = method.type;
			this.name = method.name;
			this.parameter = method.parameter;
			this.library = method.library;
			this.year = method.year;
			this.version = method.version;
			this.status = status;
			
		}

		public Method(NodeList<AnnotationExpr> annotation, String type, String name, NodeList<Parameter> parameter) {
			this.annotation = annotation;
			this.type = type;
			this.name = name;
			this.parameter = parameter;

		}

		public Method(String path, Method method) {
			this.annotation = method.annotation;
			this.type = method.type;
			this.name = method.name;
			this.parameter = method.parameter;
			// System.out.println(path);
			path = path.replaceAll("/", "=").replace("\\", "=");
			String[] splitPath = path.split("=");
			// System.out.println(splitPath[2]);
			String[] splitName = splitPath[2].split("_");
			// System.out.println("lib " + splitName[0]+" year " + splitName[1] + "v " +
			// splitName[2]);
			this.library = splitName[0];
			this.year = splitName[1];
			this.version = splitName[2];
		}

		public String getSigniture(Method m) {
			return m.type + " " + m.name + " " + m.parameter.toString();
		}

	}

	private static class MethodNamePrinter extends VoidVisitorAdapter<Void> {
		public ArrayList<Method> mds = new ArrayList<Method>();
		@Override
		public void visit(MethodDeclaration md, Void arg) {
			// System.out.println(md.getNameAsString());
			Method m = new Method(md.getAnnotations(),md.getTypeAsString(),md.getNameAsString(),md.getParameters());
			//System.out.println("-" + md.getAnnotations());
			mds.add(m);
		}

	}

	public static void main(String[] args) {
		createData();
		System.out.println("done");
		insertToDatabase();
		System.out.println("done!");
//		System.out.println(Data.get(1).get(0).status);
//		System.out.println(Data.size());
//		System.out.println(Data.get(Data.size() - 1).size());
//		System.out.println(Data.get(Data.size() - 1).get(0).status);

	}

}
