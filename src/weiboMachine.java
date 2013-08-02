import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;

import org.openrdf.rio.RDFFormat;

import Utils.RepoUtil;


public class weiboMachine {
	static RepoUtil repo;
	static DateFormat dateFormat;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		repo = new RepoUtil(".//repoWeibo");
		repo.initialize();
//		read("..//weibo_temp");
//		repo.saveRDFTurtle(".//resultTemp.N3", RDFFormat.N3, "self");
//		System.out.println(isTimeStamp("1289387501"));
		repo.query();
//		repo.timeLineQuery();
//		repo.sourceQuery();
	}

	private static void read(String path) {
		File file = new File(path);
    	String [] fileList = file.list();
    	int fileCount = 1;
    	for (String fileElement : fileList) {
    		if(fileElement.endsWith("csv")){
    			BufferedReader reader;
    			String str = null;
    			long count=1;
    			boolean smallFlag = false;
    			try {
					reader = new BufferedReader(new InputStreamReader(
							new FileInputStream(path + "/" + fileElement),"UTF-8"));
					try {
						reader.readLine();//deal with the first line
						System.out.println(fileElement);
						while((str = reader.readLine()) != null) {
							count++;
							/*********************
							int location = str.indexOf(",@");
							if(location > 0) {
								str = str.substring(0, location);
							}
							location = str.indexOf(",\"@");
							if(location > 0) {
								str = str.substring(0, location);
							}
							System.out.println(str);
							/*********************/
							String [] strList = str.split(",");
							int len = strList.length;
							/**********************/
							//if the line is small, skip it
							if(smallFlag) {
								smallFlag = false;
								continue;
							}
							if(len < 7) {
								smallFlag = true;
								continue;
							}
							/*********************/
							if(strList[1].length() < 1) {
								continue;
							}
							/**********************/
							//if the line is very big, do extra operations.
							if(len > 16) {
								//TODO
								
							}
							/**********************
							if(strList[5].length() > 0) {
								System.out.println(fileElement);
								System.out.println("Line Number: "+count);
								System.out.println(strList[5].length());
							}
							/**********************
							if(strList[8].length() > 0) {
//								System.out.println(fileElement);
//								System.out.println("Line Number: "+count);
								System.out.println(strList[8]);
							}
							/**********************
							if(strList[12].length() == 0) {
								System.out.println(fileElement);
								System.out.println("Line Number: "+count);
								System.out.println(strList.length);
								System.out.println(strList[strList.length-4]);
							}
							/*********************/
//							System.out.println("Line "+count+" processing...");
							process(strList);
//							System.out.println("Line "+count+" processed.");
							
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
    		}
    		System.out.println("File "+fileCount+" completed.");
    		System.out.println("File name: "+fileElement);
    		fileCount++;
//    		break; //add this line to read only one file.
     	}

	}

	private static void process(String[] strList) {
		String s=null,p=null,o=null;
		
		String wid = strList[0];	//weibo ID
		String uid = strList[1];	//user ID
		if(uid.length() < 1) {	//The weibo has been deleted.
			//TODO
			return;
		}
		String nickName = strList[3];
		String headUrl = strList[4];
		boolean retweetFlag = false;
		String retweetId = "0000000";
		if(strList[5].length() > 0) {	//retweeted weibo ID
			retweetId = strList[5];
			//TODO
			retweetFlag = true;
		}
		String weiboText = strList[6];	//weibo Text
		
		int tsLoc = 0;					//find the time stamp location
//		System.out.println(strList.length);
		for(tsLoc=strList.length-1; tsLoc>0; tsLoc--) {
			if(strList[tsLoc].matches("[0-9]{10}")) {
				if(tsLoc < 3) {
					return; //the weibo is invalid
				}
				break;
			}
		}
//		System.out.println(strList[strList.length]);
//		System.out.println(tsLoc);
//		System.out.println(strList[tsLoc]);
//		System.out.println(strList[tsLoc-1]);
//		System.out.println(strList[tsLoc-2]);
		long retweetNum = Integer.parseInt(strList[tsLoc-2]);	//the number of retweet
		long replyNum = Integer.parseInt(strList[tsLoc-1]);	//the number of reply
		long timeStamp = Integer.parseInt(strList[tsLoc]);
//		String timeStamp = new String(strList[tsLoc]);
//		String date = dateFormat.format(timeStamp);		//the post date
		
		repo.setNameSpace("http://weibo.com/");
		repo.begin();
		/******** Processing ********/
		/* the subject is user */
		repo.setSubjType("user");
		repo.setPredType("post");
		repo.setObjType("weibo");
		s = uid;
		p = "create";
		o = wid;
		repo.addRecord(s, p, o, true);
		if(retweetFlag) {
			s = uid;
			p = "retweet";
			o = retweetId;
			repo.addRecord(s, p, o, true);
		}
		
		repo.setSubjType("user");
		repo.setPredType("setting");
		p = "nickname";
		o = nickName;
		repo.addRecord(s, p, o, false);
		
		p = "head-url";
		o = headUrl;
		repo.addRecord(s, p, o, false);
		
		/* the subject is weibo */
		
		repo.setSubjType("weibo");
		repo.setPredType("from");
		repo.setObjType("user");
		s = wid;
		p = "post-from";
		o = uid;
		repo.addRecord(s, p, o, true);
		
		if(retweetFlag) {
			repo.setObjType("weibo");
			s = wid;
			p = "retweet-from";
			o = retweetId;
			repo.addRecord(s, p, o, true);
			retweetFlag = false;
		}
		
		repo.setSubjType("weibo");
		repo.setPredType("property");
		s = wid;
		p = "post-date";
		repo.addRecord(s, p, timeStamp, false);
		
		p = "text";
		o = weiboText;
		repo.addRecord(s, p, o, false);
		
		p = "retweet-number";
		repo.addRecord(s, p, retweetNum, false);
		
		p = "reply-number";
		repo.addRecord(s, p, replyNum, false);
		
		repo.setObjType("source");
		if(strList[8].length() > 0) {
			p = "source";
			o = strList[8];
			repo.addRecord(s, p, o, true);
		}
		
		repo.setSubjType("weibo");
		repo.setPredType("url-property");
		s = wid;
		
		if(strList[tsLoc-7].length() > 0) {
			p = "text-url";
			o = strList[tsLoc-7];
			repo.addRecord(s, p, o, false);
		}
		
		if(strList[tsLoc-5].length() > 0) {
			p = "photo-url";
			o = strList[tsLoc-5];
			repo.addRecord(s, p, o, false);
		}
		
		if(strList[tsLoc-4].length() > 0) {
			p = "audio-url";
			o = strList[tsLoc-4];
			repo.addRecord(s, p, o, false);
		}
		
		if(strList[tsLoc-3].length() > 0) {
			p = "video-url";
			o = strList[tsLoc-3];
			repo.addRecord(s, p, o, false);
		}
		
		/************************/
		repo.commit();
		
	}
	

}
