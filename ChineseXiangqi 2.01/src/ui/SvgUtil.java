package ui;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class SvgUtil {
    public static String readPathData(String url) {
        try {
            InputStream stream = SvgUtil.class.getResourceAsStream(url);
            if (stream == null)
                throw new RuntimeException("找不到资源文件: " + url);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(stream);

            StringBuilder content = new StringBuilder();
            NodeList pathNodes = document.getElementsByTagName("path");
            for (int i = 0; i < pathNodes.getLength(); i++) {
                Element pathElement = (Element) pathNodes.item(i);
                String dAttribute = pathElement.getAttribute("d");
                if (!dAttribute.isEmpty())
                    content.append(dAttribute);
            }
            return content.toString();
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
