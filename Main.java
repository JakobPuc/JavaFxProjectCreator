import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

/**
 * IF program fails it does not clean after it self !
 */
public class Main {

	private static Scanner sc;
	private static String projectName;
	private static String mainFileName;
	// only suports one package
	private static String packageName;
	private static String pathForPackage;

	private static String main = new String("/src/main/java/");
	private static String test = new String("/src/test/java/");
	private static String appTest = new String("example/AppTest.txt");
	private static String mainApp = new String("example/App.txt");
	private static String pom = new String("example/pom.txt");

	public static void main(String[] args) {
		sc = new Scanner(System.in);
		System.out.println("Starting program for creating JavaFX projects using maven");
		System.out.print("Enter project name: ");
		projectName = readCLI();
		System.out.print("Enter package name: ");
		packageName = readCLI();
		pathForPackage = reformatPackageNameToFilePath(packageName);
		System.out.print("Main file name: ");
		mainFileName = readCLI();
		mainFileName = mainFileName.substring(0, 1).toUpperCase() + mainFileName.substring(1);
		makeFolder(projectName);
		makeFolder(projectName.concat(main).concat(pathForPackage));
		makeFolder(projectName.concat(test).concat(pathForPackage));
		makeFile(projectName.concat(main).concat(pathForPackage), mainApp, mainFileName + ".java", true);
		makeFile(projectName.concat(test).concat(pathForPackage), appTest, mainFileName + "Test.java", true);
		makeFile(projectName, pom, "pom.xml", false);
		modifyPom();
		System.out.println("Created project");
		sc.close();
		System.exit(0);
	}

	private static String readCLI() {
		String str = sc.nextLine();
		while (true) {
			if (str.isEmpty()) {
				System.out.println("Sorry, but you need to enter a value");
				str = sc.nextLine();
			} else {
				return str.trim();
			}

		}
	}

	private static void makeFolder(String path) {
		Path file = Paths.get(path);
		try {
			Files.createDirectories(file);
			System.out.println("Created folder " + path);
		} catch (FileAlreadyExistsException e) {
			System.out.println("folder alredy exist");
		} catch (Exception e) {
			System.out.println("Error creating a folder " + e.getMessage());
			System.exit(1);
		}
	}

	private static void makeFile(String pathToFile, String src, String file, boolean imprtYN) {
		Path source = Paths.get(src);
		Path dest = Paths.get(pathToFile + "/" + file);
		try (BufferedReader br = Files.newBufferedReader(source);
				BufferedWriter bw = Files.newBufferedWriter(dest)) {
			if (imprtYN) {
				bw.write("package " + packageName + ";");
			}
			bw.newLine();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.equals("public class App extends Application {")) {
					line = "public class " + mainFileName + " extends Application {";
				}

				bw.write(line);
				bw.newLine();
			}
			System.out.println("Created file: " + pathToFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private static String reformatPackageNameToFilePath(String packageN) {
		if (packageN.contains(".")) {
			String tmp = packageN.replace('.', '/');
			return tmp;
		} else {
			return packageN;
		}
	}

	private static void modifyPom() {
		File xmlFile = new File(projectName + "/pom.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xmlFile);

			NodeList groupList = doc.getElementsByTagName("mainClass");
			if (groupList.getLength() > 0) {
				groupList.item(0).setTextContent(packageName + "." + mainFileName);
			}
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			DOMSource src = new DOMSource(doc);
			StreamResult res = new StreamResult(new File(projectName + "/pom.xml"));
			trans.transform(src, res);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
