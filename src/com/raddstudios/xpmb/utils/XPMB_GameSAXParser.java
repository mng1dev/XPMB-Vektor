package com.raddstudios.xpmb.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.raddstudios.xpmb.utils.Serialization.gameClass;

class MySAXTerminatorException extends SAXException {
	// Parameterless Constructor
	public MySAXTerminatorException() {
	}

	// Constructor that accepts a message
	public MySAXTerminatorException(String message) {
		super(message);
	}
}

public class XPMB_GameSAXParser {

	public static ArrayList<gameClass> parse(String xml) {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		ArrayList<gameClass> output = new ArrayList<gameClass>();
		XMLHandler handler = new XMLHandler();
		SAXParser sp;
		try {
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler((ContentHandler) handler);
			xr.parse(new InputSource(new StringReader(xml)));
			output.addAll(handler.getParsedData());
			return output;
		} 
		catch(MySAXTerminatorException e){
			output.addAll(handler.getParsedData());
			Log.i("Catch!","Output size is:"+output.size());
			return output;
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ArrayList<gameClass>();
	}

	private static String baseImgURL;
	public static String getBaseURL(){
		return baseImgURL;
	} 

	static class XMLHandler extends DefaultHandler {

		private boolean in_game = false;
		private boolean in_gametitle = false;
		private boolean in_images = false;
		private boolean in_boxart = false;
		private boolean in_baseimgurl = false;
		private ArrayList<gameClass> output = new ArrayList<gameClass>();
		private gameClass gc = new gameClass(null, null);
		private int games = 0;

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException,MySAXTerminatorException {
			// atts.getValue("name");
			//Log.i("Tag IN", localName);
			if (games > 4)
				throw new MySAXTerminatorException();
			if (localName.equalsIgnoreCase("Game")) {
				in_game = true;
			} else if (localName.equalsIgnoreCase("GameTitle")) {
				in_gametitle = true;
			} else if (localName.equalsIgnoreCase("Images")) {
				in_images = true;
			} else if (localName.equalsIgnoreCase("boxart")
					&& atts.getValue("side").equalsIgnoreCase("front")) {
				gc.setURL(atts.getValue("thumb"));
			} else if (localName.equalsIgnoreCase("baseimgurl")) {
				in_baseimgurl = true;
			}

		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			//Log.i("Tag OUT", localName);
			if (localName.equalsIgnoreCase("Game")) {
				in_game = false;
			} else if (localName.equalsIgnoreCase("GameTitle")) {
				in_gametitle = false;
			} else if (localName.equalsIgnoreCase("Images")) {
				in_images = false;
			} else if (localName.equalsIgnoreCase("boxart")) {
				in_boxart = false;
			} else if (localName.equalsIgnoreCase("baseimgurl")) {
				in_baseimgurl = false;
			}
		}

		@Override
		public void characters(char ch[], int start, int length) {
			String textBetween = new String(ch, start, length);
			if (in_game) {
				if (in_gametitle) {
					gc.setTitle(textBetween);
				}
			} else if(in_baseimgurl){
				baseImgURL = textBetween;
			}else {
				if (null != gc.getTitle() && null != gc.getURL()) {
					output.add(new gameClass(gc.getTitle(), gc.getURL()));
					games++;
					gc.clear();
				}
			}
		}

		@Override
		public void startDocument() throws SAXException {
			// Do some startup if needed

		}

		public ArrayList<gameClass> getParsedData() {
			return this.output;
		}
	}

}
