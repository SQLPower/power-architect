/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.enterprise;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import ca.sqlpower.enterprise.ClientSideSessionUtils;
import ca.sqlpower.enterprise.client.SPServerInfo;
import ca.sqlpower.enterprise.client.ServerProperties;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.util.HTMLUserPrompter;
import ca.sqlpower.util.UserPrompter.UserPromptOptions;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;
import ca.sqlpower.util.Version;

public abstract class ServerInfoProvider {
	
	public static final String defaultWatermarkMessage = "<html>This version of SQL Power Architect is for EVALUATION PURPOSES ONLY. To obtain a full Production License, please visit <a href=\"http://www.sqlpower.ca/architect-e\">www.sqlpower.ca/architect-e</a></html>";
	
	private static Map<String,Version> version = new HashMap<String, Version>();
	
	private static Map<String,Boolean> licenses = new HashMap<String, Boolean>();
	
	private static Map<String,String> watermarkMessages = new HashMap<String, String>();
	
	public static Version getServerVersion(
			String host,
			String port,
			String path, 
			String username, 
			String password) throws MalformedURLException,IOException 
	{
		init(toURL(host, port, path), username, password);
		return version.get(generateServerKey(host, port, path, username, password));
	}
	
	public static boolean isServerLicensed(SPServerInfo infos) 
			throws MalformedURLException,IOException 
	{
		return isServerLicensed(
				infos.getServerAddress(), 
				String.valueOf(infos.getPort()), 
				infos.getPath(), 
				infos.getUsername(), 
				infos.getPassword());
	}

	public static boolean isServerLicensed(
			String host,
			String port,
			String path, 
			String username, 
			String password) throws MalformedURLException,IOException 
	{
		init(toURL(host, port, path), username, password);
		return licenses.get(generateServerKey(host, port, path, username, password));
	}
	
	private static URL toURL(
			String host,
			String port,
			String path) throws MalformedURLException 
	{
		// Build the base URL
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(host);
		sb.append(":");
		sb.append(port);
		sb.append(path);
		sb.append(path.endsWith("/")?"":"/");
		sb.append(ClientSideSessionUtils.REST_TAG);
		sb.append("/serverinfo");
		
		// Spawn a connection object
		return new URL(sb.toString());
	}

	private static void init(URL url, String username, String password) throws IOException {
		
		if (version.containsKey(generateServerKey(url, username, password))) return;
		
		try {
			HttpParams params = new BasicHttpParams();
	        HttpConnectionParams.setConnectionTimeout(params, 2000);
	        DefaultHttpClient httpClient = new DefaultHttpClient(params);
	        httpClient.setCookieStore(ArchitectClientSideSession.getCookieStore());
	        httpClient.getCredentialsProvider().setCredentials(
	            new AuthScope(url.getHost(), AuthScope.ANY_PORT), 
	            new UsernamePasswordCredentials(username, password));
	        
	        HttpUriRequest request = new HttpOptions(url.toURI());
    		String responseBody = httpClient.execute(request, new BasicResponseHandler());
			
			// Decode the message
			String serverVersion;
			Boolean licensedServer;
			final String watermarkMessage;
			try {
				JSONObject jsonObject = new JSONObject(responseBody);
				serverVersion = jsonObject.getString(ServerProperties.SERVER_VERSION.toString());
				licensedServer = jsonObject.getBoolean(ServerProperties.SERVER_LICENSED.toString());
				watermarkMessage = jsonObject.getString(ServerProperties.SERVER_WATERMARK_MESSAGE.toString());
			} catch (JSONException e) {
				throw new IOException(e.getMessage());
			}
			
			// Save found values
			version.put(generateServerKey(url, username, password), new Version(serverVersion));
			licenses.put(generateServerKey(url, username, password), licensedServer);
			watermarkMessages.put(generateServerKey(url, username, password), watermarkMessage);
			
			// Notify the user if the server is not licensed.
			if (!licensedServer) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					    HyperlinkListener hyperlinkListener = new HyperlinkListener() {
                            @Override
                            public void hyperlinkUpdate(HyperlinkEvent e) {
                                try {
                                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                        if (e.getURL() != null) {
                                            BrowserUtil.launch(e.getURL().toString());
                                        }
                                    }
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        };
					    HTMLUserPrompter htmlPrompter = new HTMLUserPrompter(
					            UserPromptOptions.OK, 
					            UserPromptResponse.OK, 
					            null, 
					            watermarkMessage,
					            hyperlinkListener,
					            "OK");
					    htmlPrompter.promptUser("");
					}
				});
			}
			
		} catch (URISyntaxException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}
	
	public static String getWatermarkMessage(SPServerInfo infos) 
			throws MalformedURLException,IOException 
	{
		return getWatermarkMessage(
				infos.getServerAddress(), 
				String.valueOf(infos.getPort()), 
				infos.getPath(), 
				infos.getUsername(), 
				infos.getPassword());
	}
	
	public static String getWatermarkMessage(
			String host,
			String port,
			String path, 
			String username, 
			String password)
	{
		String message = defaultWatermarkMessage;
		try {
			if (!isServerLicensed(host,port,path,username,password)) {
				message = watermarkMessages.get(generateServerKey(host, port, path, username, password));
			} else {
				message = "";
			}
		} catch (Exception e) {
			// no op
		}
		return message;		
	}
	
	private static String generateServerKey(
			String host, 
			String port, 
			String path, 
			String username, 
			String password) throws MalformedURLException 
	{
		return generateServerKey(
			toURL(host, port, path), 
			username, 
			password);
	}
	
	private static String generateServerKey(
			URL url, 
			String username, 
			String password) 
	{
		return 
			String.valueOf(
				url.toString()
					.concat(username)
					.concat(password)
				.hashCode());
	}
}
