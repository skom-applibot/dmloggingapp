/*
 * Copyright (c) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package jp.co.applibot.backup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class BackupController extends HttpServlet {
	private static final String BUCKETNAME = "gaebackup-datamining.appspot.com";

	/**
	 * auto generated
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(BackupController.class.getName());
	private final GcsService gcsService =
			GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());
	private static final String HOSTNAME = "https://cloudssh.developers.google.com/projects/gaebackup-datamining/zones/asia-east1-c/instances/legend-aggregate?authuser=0";
	private static final String USERID   = "komiya_sakura_applibot_co_jp";
	private static final String PASS = "";

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String json = "";
		String className = "";
		String key = "";
		Long now = new Date().getTime();
		if(request != null) {
			BufferedReader bufferReaderBody = new BufferedReader(request.getReader());
			String body = bufferReaderBody.readLine();
			json = body;
		}
		
		int indexLast = json.indexOf("\":");
		if(indexLast > 0) {
			className = json.substring(1, indexLast);
		}
		className = className.replaceAll("\"", "");
		
		JSONParser parser = new JSONParser();
//		try {
//			 
//			Object wrapObj = parser.parse(json);
//	
//			JSONObject jsonWrapObject = (JSONObject) wrapObj;
//	 
//			String jsonObj = jsonWrapObject.get(className).toString();
//			
//			Object obj = parser.parse(jsonObj);
//			
//			JSONObject jsonObject = (JSONObject) obj;
//			
//			key = jsonObject.get("id").toString();
//	 
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
		
		  JSch jsch=new JSch();
		  String host="107.167.177.138";
		  String user="dmlogging";
		  String passwd = "Applibot12345";
		  try {
			Session session=jsch.getSession(user, host, 22);
			session.setPassword(passwd);
			  session.connect(30000); 
			  Channel channel=session.openChannel("exec");
		     // ((ChannelExec)channel).setCommand("\"" + json +"\"" +  ">> /home/komiya_sakura_applibot_co_jp/test.txt");
		      ((ChannelExec)channel).setCommand("mkdir /home/komiya_sakura_applibot_co_jp/logfiles");
		      LOGGER.warning("command set");
		      channel.setInputStream(null);
		      ((ChannelExec)channel).setErrStream(System.err);
		      
		      InputStream in=channel.getInputStream();
		 
		      channel.connect();
		      byte[] tmp=new byte[1024];
		      while(true){
		        while(in.available()>0){
		          int i=in.read(tmp, 0, 1024);
		          if(i<0)break;
		          LOGGER.warning(new String(tmp, 0, i));
		        }
		        if(channel.isClosed()){
		          if(in.available()>0) continue; 
		          LOGGER.warning("exit-status: "+channel.getExitStatus());
		          break;
		        }
		        try{Thread.sleep(1000);}catch(Exception ee){}
		      }
		      channel.disconnect();
		      session.disconnect();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		GcsFilename fileName = new GcsFilename(BUCKETNAME, className+"_"+now.toString()+"_"+key);
		GcsFileOptions options = new GcsFileOptions.Builder().mimeType("text/html").acl("public-read").build();
		GcsOutputChannel outputChannel = gcsService.createOrReplace(fileName, options);
		
		outputChannel.write(ByteBuffer.wrap(json.getBytes("UTF8")));
		outputChannel.close();

		//reply
		PrintWriter out = response.getWriter();
		out.println(json);
	}
	
}
