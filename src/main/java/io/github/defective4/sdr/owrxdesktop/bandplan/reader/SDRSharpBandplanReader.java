package io.github.defective4.sdr.owrxdesktop.bandplan.reader;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.github.defective4.sdr.owrxdesktop.application.util.ColorEncoder;
import io.github.defective4.sdr.owrxdesktop.bandplan.Band;
import io.github.defective4.sdr.owrxdesktop.bandplan.Bandplan;

public class SDRSharpBandplanReader extends BandplanReader {

    public static final BandplanReaderFactory<SDRSharpBandplanReader> FACTORY = SDRSharpBandplanReader::new;
    private final DocumentBuilder documentBuilder;

    public SDRSharpBandplanReader(Reader reader) {
        super(reader);
        try {
            documentBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Bandplan readBandplan(String name) throws IOException, SAXException {
        Document document = documentBuilder.parse(new InputSource(reader));
        NodeList rootList = document.getChildNodes();
        Map<String, Color> colors = new HashMap<>();
        List<Band> bands = new ArrayList<>();
        for (int i = 0; i < rootList.getLength(); i++) {
            Node rootNode = rootList.item(i);
            if (rootNode.getNodeType() == Node.ELEMENT_NODE && rootNode.getNodeName().equals("ArrayOfRangeEntry")) {
                NodeList entries = rootNode.getChildNodes();
                for (int j = 0; j < entries.getLength(); j++) {
                    Node entry = entries.item(j);
                    if (entry.getNodeType() == Node.ELEMENT_NODE && entry.getNodeName().equals("RangeEntry")) {
                        NamedNodeMap attrs = entry.getAttributes();
                        long startFreq = Long.parseLong(attrs.getNamedItem("minFrequency").getTextContent());
                        long endFreq = Long.parseLong(attrs.getNamedItem("maxFrequency").getTextContent());
                        if (startFreq > Integer.MAX_VALUE || endFreq > Integer.MAX_VALUE) continue;
                        String colorText = attrs.getNamedItem("color").getTextContent();
                        if (colorText.length() == 8) colorText = colorText.substring(0, 6);
                        Color color = ColorEncoder.setColorAlpha(Color.decode("#" + colorText), 100);
                        String label = entry.getTextContent();
                        colors.put(colorText, color);
                        bands.add(new Band((int) startFreq, (int) endFreq, color, label));
                    }
                }
            }
        }
        return new Bandplan(bands, colors, "[SDR#] " + name);
    }

}
