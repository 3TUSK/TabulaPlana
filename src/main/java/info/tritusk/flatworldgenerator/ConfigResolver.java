package info.tritusk.flatworldgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.minecraftforge.common.config.Configuration;

public final class ConfigResolver {
	
	//No need to make it able to be initialized
	private ConfigResolver() {}
	
	public static boolean useGlobalSettings;
	
	public static int globalHorizon;
	public static int globalCloudHeight;
	
	public static void initMainConfig(File file) {
		Configuration config = new Configuration(file);
		
		config.load();
		
		useGlobalSettings = config.getBoolean("useGlobalSettings", "Main", false, "Use unified, global settings to replace all world-specified settings.");
		
		globalHorizon = config.get("GlobalSettings", "globalHorizon", 63).getInt();
		globalCloudHeight = config.get("GlobalSettings", "globalCloudHeight", 255).getInt();
		
		if (config.hasChanged())
			config.save();
	}
	
	
	public static boolean xmlReaderInitialized = false;
	
	private static DocumentBuilder reader;
	
	public static void initXMLReader() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			reader = factory.newDocumentBuilder();
			xmlReaderInitialized = reader != null;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private static int count = 0;

	public static void loadCustomFlatWorldConfig(File configFlatWorld) {
		try {
			System.out.println(configFlatWorld.getAbsolutePath());
			InputStream data = new FileInputStream(configFlatWorld);
			Document dataParsed = reader.parse(data);
			dataParsed.normalize();		
			NodeList main = dataParsed.getElementsByTagName("CustomFlatWorldInfo").item(0).getChildNodes();
			
			String name = "DummyWorld" + count;
			String generatorCode = "1:7";
			boolean enableStructure = true;
			float cloudHeight = globalCloudHeight;
			double horizon = globalHorizon;
			
			for (int i = 0;i < main.getLength();i++) {
				Node node = main.item(i);
				
				if ("name".equals(node.getNodeName()))
					name = node.getTextContent();
				
				if ("generator".equals(node.getNodeName()))
					generatorCode = node.getTextContent();
				
				if ("structure".equals(node.getNodeName()))
					enableStructure = Boolean.valueOf(node.getTextContent());
				
				if ("cloudHeight".equals(node.getNodeName()))
					cloudHeight = Float.valueOf(node.getTextContent());
				
				if ("horizon".equals(node.getNodeName()))
					horizon = Double.valueOf(node.getTextContent());
			}
			
			if (name.length() > 12)
				name = name.substring(0, 12);
			//There is no other way to approach this though.
			//WorldType requires that name of a new WorldType is shorter than 16, 
			//and I have used 4 of them
			
			new WorldTypeCustomFlat(name, generatorCode, enableStructure).setCloudHeight(cloudHeight).setHorizon(horizon);		
			++count;
			CustomFlatWorldGenerator.log.info("Successfully loaded custom flat world type: " + name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
