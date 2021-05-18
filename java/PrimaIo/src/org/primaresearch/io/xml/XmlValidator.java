/*
 * Copyright 2019 PRImA Research Lab, University of Salford, United Kingdom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primaresearch.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;


/**
 * XML validator for a specific schema.<br>
 * Validators are usually managed by a ValidatorProvider.
 *
 * @author Christian Clausner
 *
 */
public class XmlValidator {

	URL schemaSource;
	Schema schema = null;
	XmlFormatVersion schemaVersion;

	public XmlValidator(URL schemaSource, XmlFormatVersion schemaVersion) {
		this.schemaSource = schemaSource;
		this.schemaVersion = schemaVersion;
	}

	/**
	 * Returns the source file of the schema for this validator
	 * @return Schema location
	 */
	public URL getSchemaSource() {
		return schemaSource;
	}

	/**
	 * Returns the target URL of a base URL after redirecting
	 * @param urlString
	 * @return Final URL
	 * @throws IOException
	 */
	public static URL retrieveFinalURL(String urlString) throws IOException {
		URL url = new URL(urlString);
		URLConnection urlConnection = url.openConnection();

		if(!(urlConnection instanceof JarURLConnection)){
			HttpURLConnection con = (HttpURLConnection) urlConnection;
			con.setInstanceFollowRedirects(false);
			con.connect();
			con.getInputStream();

			if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
				String redirectUrl = con.getHeaderField("Location");
				return retrieveFinalURL(redirectUrl);
			}
		}

		return url;
	}

	/**
	 * Returns the schema object that can be used for validating XML (e.g. DOM or SAX).
	 */
	public Schema getSchema() {
		if (schema == null) {
    		//SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				URL schemaSource = retrieveFinalURL(getSchemaSource().toString());
				InputStream inputStream = schemaSource.openStream();
				Source src = new StreamSource(inputStream);
				schema = schemaFactory.newSchema(src);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return schema;
	}

	/**
	 * Returns the schema version of this validator
	 * @return Version object
	 */
	public XmlFormatVersion getSchemaVersion() {
		return schemaVersion;
	}

}
