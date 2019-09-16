package org.endeavourhealth.fihrexporter.send;

import org.endeavourhealth.fihrexporter.repository.Repository;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LHShttpSend {

	private static String location = "";

	private Integer SendTLS(String url, String method, String encoded)
	{
		try {
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			con.setRequestMethod(method);

			con.setRequestProperty("Content-Type","application/json");

			// Send request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(encoded);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String output;
			StringBuffer response = new StringBuffer();

			while ((output = in.readLine()) != null) {
				response.append(output);
			}
			in.close();

			//printing result from response
			System.out.println(response.toString());

			if (method == "POST") {location = con.getHeaderField("location");}

			return responseCode;
		}catch(Exception e){
			System.out.println(e);
			return 0;
		}
	}

	private Integer SendHttp(String url, String method, String encoded)
	{
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod(method);

			con.setRequestProperty("Content-Type","application/json");

			// Send request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(encoded);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));
			String output;
			StringBuffer response = new StringBuffer();

			while ((output = in.readLine()) != null) {
				response.append(output);
			}
			in.close();

			//printing result from response
			System.out.println(response.toString());

			if (method == "POST") {location = con.getHeaderField("location");}

			return responseCode;
		}catch(Exception e){
			System.out.println(e);
			return 0;
		}
	}

	public Integer Post(Repository repository, Integer anId, String strid, String url, String encoded, String resource, Integer patientid, Integer typeid)
	{
		try {

			String location = ""; String method = "POST";

			// decide if it's a post or a put?
			if (anId != 0) {location = repository.getLocation(anId, resource);}

			// snomed reference?
			if (location.length() == 0) {
				location = repository.GetMedicationReference(strid);
			}

			if (location.length() > 0) {
				url = url + "/" + location;
				method = "PUT";
			}

			int responseCode = 0;

			if (url.contains("http:")) {
				responseCode = SendHttp(url, method, encoded);
			}
			if (url.contains("https:")) {
				responseCode = SendTLS(url,method, encoded);
			}

			/*
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			if (url.contains("https:")) {HttpsURLConnection con = (HttpsURLConnection) obj.openConnection()};

			con.setRequestMethod(method);

			con.setRequestProperty("Content-Type","application/json");

			// Send request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(encoded);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String output;
			StringBuffer response = new StringBuffer();

			while ((output = in.readLine()) != null) {
				response.append(output);
			}
			in.close();

			//printing result from response
			System.out.println(response.toString());
			 */

			if (method == "PUT") {repository.UpdateAudit(anId, strid, encoded, responseCode);}

			if (method == "POST") {

				//System.out.println("location = " + con.getHeaderField("location"));
				//location = con.getHeaderField("location");

				System.out.println(LHShttpSend.location);

				String[] ss = LHShttpSend.location.split("/");

				if (location.contains("/_history/")) {
					location = ss[ss.length - 3];
				} else {
					location = ss[ss.length - 1];
				}

				repository.Audit(anId, strid, resource, responseCode, location, encoded, patientid, typeid);
			}

			return responseCode;
		}catch(Exception e){
			System.out.println(e);
			return 0;
		}
	}
}