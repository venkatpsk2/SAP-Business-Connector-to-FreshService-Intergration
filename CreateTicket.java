package com.sftp.dev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.AuthSchemeBase;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

public class CreateTicket {
	
		String apiToken = "******";
		String apiEndpoint = "************";

		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String element = IDataUtil.getString(pipelineCursor, "element");

		if (element != null) {
			String date = element.substring(0, 24);
			String error = element.substring(24);
			try {
				File f = new File("/opt/sapbc48/server/packages/SAPBC_FreshServiceIntegration/logs/HTTP_API.log");
				BufferedWriter bwrr = new BufferedWriter(new FileWriter(f, true));

				HttpClientBuilder hcBuilder = HttpClientBuilder.create();
				RequestBuilder reqBuilder = RequestBuilder.post();
				RequestConfig.Builder rcBuilder = RequestConfig.custom();

				// URL object from API endpoint:
				URL url = new URL(apiEndpoint + "/api/v2/tickets");
				String urlHost = url.getHost();
				int urlPort = url.getPort();
				String urlProtocol = url.getProtocol();
				reqBuilder.setUri(url.toURI());

				// Authentication:
				List<String> authPrefs = new ArrayList<>();
				authPrefs.add(AuthSchemes.BASIC);
				rcBuilder.setTargetPreferredAuthSchemes(authPrefs);
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(new AuthScope(urlHost, urlPort, AuthScope.ANY_REALM),
						new UsernamePasswordCredentials(apiToken, "X"));
				hcBuilder.setDefaultCredentialsProvider(credsProvider);
				AuthCache authCache = new BasicAuthCache();
				AuthSchemeBase authScheme = new BasicScheme();
				authCache.put(new HttpHost(urlHost, urlPort, urlProtocol), authScheme);
				HttpClientContext hccContext = HttpClientContext.create();
				hccContext.setAuthCache(authCache);

				// Body:
				JsonObject newObject = new JsonObject();
				long id = 52000179013L;
				newObject.addProperty("subject", "SAP Business Connector issue");
				newObject.addProperty("description",
						"This incident is auto-created due to a Service Error encountered in " + "<b>"
								+ "SAP Business Connector(SAP BC) Development Environment (SED)" + "</b>"
								+ "<br><br>Brief Description of the Error : " + "<b style=\"color:red\">" + error
								+ "</b>" + "<br><br>Time of the error: " + "<b>" + date + "</b>"
								+ "<br><a href = \"********(your error log link)******)\">Click to view the detailed error log</a>"
								+ "<br><br>This incident will be worked on by the relevant teams for resolution of the issue. Please refer to the incident notes for further details.<br><br>Regards,<br>SAP BC Support Team</p>");
				newObject.addProperty("email", "*****(email)*****");
				newObject.addProperty("priority", 1);
				newObject.addProperty("status", 2);
				newObject.addProperty("group_id", id);
				newObject.addProperty("urgency", 1);
				newObject.addProperty("impact", 1);
				newObject.addProperty("priority", 1);
				String jsonbody1 = newObject.toString();
				JsonObject gson = new JsonParser().parse(jsonbody1).getAsJsonObject();
				JSONObject jo2 = new JSONObject(gson.toString());
				List<String> list = new ArrayList<String>();
				list.add("***********(CC email)");
				list.add("***********(CC email)");
				jo2.put("cc_emails", list);
				String jsonbody = jo2.toString();
				HttpEntity entity = new StringEntity(jsonbody,
						ContentType.APPLICATION_JSON.withCharset(Charset.forName("utf-8")));
				reqBuilder.setEntity(entity);

				// Execute:
				RequestConfig rc = rcBuilder.build();
				reqBuilder.setConfig(rc);

				HttpClient hc = hcBuilder.build();
				HttpUriRequest req = reqBuilder.build();
				HttpResponse response = hc.execute(req, hccContext);

				// Save log to file:
				HttpEntity body = response.getEntity();
				InputStream is = body.getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				int response_status = response.getStatusLine().getStatusCode();
				String response_body = sb.toString();

				bwrr.write("Response Status: " + response_status + "\n");
				bwrr.flush();
				bwrr.write("Body:\n");
				bwrr.flush();
				bwrr.write(response_body + "\n");
				bwrr.flush();
				if (response_status > 400) {
					bwrr.write("X-Request-Id: " + response.getFirstHeader("x-request-id").getValue() + "\n");
					bwrr.flush();
				} else if (response_status == 201) {
					// For creation response_status is 201 where are as for other actions it is 200
					try {
						bwrr.write("Ticket Creation Successfull \n");
						bwrr.flush();
						// Creating JSONObject for the response string
						JSONObject response_json = new JSONObject(sb.toString());
						bwrr.write("Ticket ID: " + response_json.get("id") + "\n");
						bwrr.flush();
						bwrr.write("Location : " + response.getFirstHeader("location").getValue() + "\n");
						bwrr.flush();
					} catch (JSONException e) {
						bwrr.write("Error in JSON Parsing\n :" + e + "\n");
						bwrr.flush();
					}
				}
				bwrr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		pipelineCursor.destroy();
	}

}
