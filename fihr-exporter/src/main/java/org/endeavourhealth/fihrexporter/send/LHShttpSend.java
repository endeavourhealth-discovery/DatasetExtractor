package org.endeavourhealth.fihrexporter.send;

import org.apache.commons.io.FileUtils;
import org.endeavourhealth.fihrexporter.repository.Repository;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.*;
import java.nio.file.StandardOpenOption;
import org.json.*;
import java.util.Scanner;

public class LHShttpSend {

	private static String location = "";

	public String GetToken(Repository repository)
	{
		try {

			String tokenurl = repository.tokenurl;
			String clientid = repository.clientid;
			String clientsecret = repository.clientsecret;
			String scope = repository.scope;
			String granttype = repository.granttype;

			String token = "";

			String encoded = "client_id="+clientid+"&client_secret="+clientsecret+"&scope="+scope+"&grant_type="+granttype;

			URL obj = new URL(tokenurl);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(encoded);

			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();

			String output="";

			StringBuffer response = new StringBuffer();

			BufferedReader in = new BufferedReader(
				new InputStreamReader(con.getInputStream()));

			while ((output = in.readLine()) != null) {
				response.append(output);
			}
			in.close();

			//printing result from response
			//System.out.println(response.toString());
			System.out.println(response.toString());

			JSONObject json = new JSONObject(response.toString());
			System.out.println(json.getString("access_token"));

			token = json.getString("access_token");

			return token;
		}catch(Exception e){
			System.out.println(e);
			return "?";
		}
	}

	private Integer SendTLS(String url, String method, String encoded, String token)
	{
		try {
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			con.setRequestMethod(method);

			con.setRequestProperty("Content-Type","application/json");
			con.setRequestProperty("Authorization","Bearer "+token);

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

			//Scanner scan = new Scanner(System.in);
			//System.out.print("Press any key to continue . . . ");
			//scan.nextLine();

			System.out.println(repository.outputFHIR);

			if (repository.outputFHIR != null) {
				String folder = repository.outputFHIR;
				String file = folder+resource+"-"+anId+strid+".json";

				boolean FileExists = false;
				Path path = Paths.get(file);
				if (Files.notExists(path)) {FileExists=true;}

				Files.write(Paths.get(file), encoded.getBytes());

				location = resource+"-"+anId+strid+".json";

				if (FileExists==false) repository.Audit(anId, strid, resource, 123, location, encoded, patientid, typeid);

				return 0;
			}

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
				responseCode = SendTLS(url,method, encoded, repository.token);
			}

			if (method == "PUT") {repository.UpdateAudit(anId, strid, encoded, responseCode);}

			if (method == "POST") {

				System.out.println(LHShttpSend.location);

				String[] ss = LHShttpSend.location.split("/");

				if (LHShttpSend.location.contains("/_history/")) {
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