package com.arkingsoft.wget.core;
/*
 * A simple Java class to provide functionality similar to Wget.
 *
 * Note: Could also strip out all of the html w/ jtidy.
 */

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class JWGet
{
	public final static String SECURITY_PRINCIPAL = "security.principal";
	public final static String SECURITY_CREDENTIALS = "security.credentials";

	public static void main(String[] args)
	{
		if ( (args.length < 1) )
		{
			System.err.println( "\n\tUsage: java JWGet urlToGet [destination]" );
			System.exit(1);
		}

		HostnameVerifier hv = new HostnameVerifier()
		{
			public boolean verify(String urlHostName, SSLSession session)
			{
				System.out.println("Warning: URL Host: " + urlHostName + " vs. "
						+ session.getPeerHost());
				return true;
			}
		};

		HttpsURLConnection.setDefaultHostnameVerifier(hv);

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}

		Authenticator au = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						System.getProperties().getProperty(JWGet.SECURITY_PRINCIPAL, "hguerrero"), 
						System.getProperties().getProperty(JWGet.SECURITY_CREDENTIALS, "XX").toCharArray());
			}
		};
		
		Authenticator.setDefault(au);

		String url = args[0];

		URL u;
		InputStream is = null;
		BufferedOutputStream outStream = null;

		try
		{
			byte[] buf;
			int byteRead,byteWritten=0;

			u = new URL(url);
			is = u.openStream();

			String filename = args.length == 2 ? args[1] : url.substring(url.lastIndexOf('/') + 1);

			outStream = new BufferedOutputStream( new FileOutputStream( filename ) );

			buf = new byte[1024];

			while ((byteRead = is.read(buf)) != -1) {
				outStream.write(buf, 0, byteRead);
				byteWritten += byteRead;
			}

			System.out.println("Downloaded Successfully.\n");

			System.out.println("File name:\""+filename+ "\"\nNo of Bytes :" + byteWritten + "\n");

		}
		catch (MalformedURLException mue)
		{
			System.err.println("Ouch - a MalformedURLException happened.");
			mue.printStackTrace();
			System.exit(2);
		}
		catch (IOException ioe)
		{
			System.err.println("Oops- an IOException happened.");
			ioe.printStackTrace();
			System.exit(3);
		}
		finally
		{
			try
			{
				if (is != null) is.close();
				if (outStream != null) outStream.close();
			}
			catch (IOException ioe)
			{
			}
		}

	}

}
