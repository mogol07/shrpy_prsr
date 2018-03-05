/**
 * 
 */

// import java.io.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.nio.charset.StandardCharsets.UTF_8;
// import static java.nio.file.StandardOpenOption.APPEND;
// import static java.nio.file.StandardOpenOption.CREATE;

import java.util.Collections;
import java.util.List;
import java.io.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import java.net.URL;

/**
 * @author Mike
 *
 */
public class ShorpyParser {

	/**
	 * @param args
	 */
	private static final String strDescrName = "descript.ion"; // file with description of photo

	public static void main(String[] args) throws IOException {

		// TODO 

		// pages addresses
		final String strURL = "http://www.shorpy.com/image/tid/125"; // Ben Shahn
//		final String strURL = "http://www.shorpy.com/image/tid/209"; // Alfred Eisenstaedt
//		final String strURL = "http://www.shorpy.com/image/tid/129"; // Arthur Rothstein
		final String strTaskType = "CreateList";
		final int intConTimeOut = 10*1000; // timeout is 10 sec
//		final int intTestPage = 0; // limit of parsed pages for test
		final boolean boolIsProd = true;

		switch (strTaskType) {
		case "CreateList":
			Document doc = null;
			// get page
			doc = Jsoup.connect(strURL).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(intConTimeOut)
					.post();

			// process answer, which located in section wrapper -> wrap3 -> content1
			// -> h1 - Photographer's name
			// -> description - short bio
			// -> pager -> pager-list - list with his photos pages
			// -> images ->

			// create list of photographies формируем список работ фотографа и его био
			String strName = doc.select("h1").text();
			String strDescription = doc.select("div.description").get(1).child(0).text();
			System.out.println(strName + "\r\n" + strDescription);
//			задаем счётчик страниц с фотографиями
			String strPageCounter = "0";
			int intTotalPage;
			String prefix = "?page=";
			String suffix = "&";
			Integer intPageExist = doc.select("div.pager").size();
			if (intPageExist > 0) {
				strPageCounter = doc.select("a.pager-last.active").attr("href");
				strPageCounter = strPageCounter.substring(strPageCounter.indexOf(prefix) + prefix.length(), strPageCounter.indexOf(suffix));
				intTotalPage = Integer.parseInt(strPageCounter);
			} else {
				intTotalPage = 0;
			}
			System.out.println("Total number of pages is " + intTotalPage);
			int intPageCounter = 0;
			do {
				System.out.println("processing page " + String.valueOf(intPageCounter+1) + " of " + intTotalPage);
//				processing photos from current page
				List<Node> sss = doc.select("ul.images").first().childNodes();
				for (int i = 0; i < sss.size(); i = i + 2) {
					Document photopage = null;
					String strImageName = "";
					// get page
					photopage = Jsoup.connect(strURL+"?page=" + intPageCounter).data("query", "Java").userAgent("Mozilla").cookie("auth", "token").timeout(intConTimeOut)
							.post();					
					strImageName = photopage.select("ul.images").first().childNode(i).childNode(2).childNode(0).childNode(0).toString().split("\\:")[0].replaceAll("&amp;","&"); // short name
					strImageName = strImageName.replace("$", "USD"); 
					
					String strImageFullPageUrl = "http://www.shorpy.com"
							+ photopage.select("ul.images").first().childNode(i).childNode(2).childNode(0).attr("href")
							+ "?size=_original"; // url of full size image
	
					Document resultImageFull = Jsoup.connect(strImageFullPageUrl).data("query", "Java").userAgent("Mozilla")
							.cookie("auth", "token").ignoreContentType(true).timeout(intConTimeOut).get();
					String strImageFullUrl = resultImageFull.select("img[src$=.jpg]").attr("src");
	
					String strImageDescr = resultImageFull.select("div.caption").first().childNode(1).toString()
							.replace(" &nbsp;", ""); // краткое наименование "очищенное"
														
					strImageDescr = strImageDescr.substring(0, strImageDescr.length() - 2);
					String strPhotoDate = strImageDescr.split("\\.")[0];
					System.out.println(strImageDescr + "\r\n" + FrmtDate(strPhotoDate) + " " 
							+ strImageName  + ".jpg \r\n" + strImageFullUrl);
					getImages(strImageFullUrl,(FrmtDate(strPhotoDate) + " " + strImageName), strImageDescr);
				}
				intPageCounter++;
			} while (intPageCounter < intTotalPage && boolIsProd);
			// default: break;
		}

	}

	private static void getImages(String src, String name, String descr) throws IOException {
		
//		String strDirName = "D:\\Downloads"; // folder for saving files (in Windows)
//		String strDirName = "/home/mogol/Downloads"; // folder for saving files (in Linux)
		String strDirName = "/app/shrpy_prsr/output/"; // folder for saving files (in Docker image)
//		String strDirName = System.getProperty("user.home") + "/Downloads/"; //universal path
//		String folder = null; 
		
		// Exctract the name of the image from the src attribute 
		int indexname = src.lastIndexOf("/"); 
		
		if (indexname == src.length()) { 
			src = src.substring(1,indexname); } 
		
		indexname = src.lastIndexOf("/"); 
//		String name = src.substring(indexname, src.length());
		
//		System.out.println(name); 
		
		//Open a URL Stream
		URL url = new URL(src); 
		InputStream in = url.openStream(); 
		
		OutputStream out = new BufferedOutputStream(new FileOutputStream(
				strDirName + File.separator + name + ".jpg")); 
		
		for (int b; (b = in.read()) != -1;) { 
			out.write(b); 
		}
		
		out.close(); 
		in.close(); 

//		write desription of photo to file
		try {
			Path path = Paths.get(strDirName + File.separator + strDescrName);
			Charset charset = UTF_8;
			List<String> list = Collections.singletonList("\"" + name + ".jpg" + "\" " + descr);
		    Files.write(path, list, charset, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
	}

	/**
	 * check month name and format date accordingly
	 */
	private static String FrmtDate(String PhotoDate) {
		String[] arrPhotoDate = PhotoDate.split(" ");
		String newDate;
		if (arrPhotoDate.length > 1) {
			switch (arrPhotoDate[0]) {
			case "January":
				newDate = arrPhotoDate[1] + "-" + "01";
				break;
			case "February":
				newDate = arrPhotoDate[1] + "-" + "02";
				break;
			case "March":
				newDate = arrPhotoDate[1] + "-" + "03";
				break;
			case "April":
				newDate = arrPhotoDate[1] + "-" + "04";
				break;
			case "May":
				newDate = arrPhotoDate[1] + "-" + "05";
				break;
			case "June":
				newDate = arrPhotoDate[1] + "-" + "06";
				break;
			case "July":
				newDate = arrPhotoDate[1] + "-" + "07";
				break;
			case "August":
				newDate = arrPhotoDate[1] + "-" + "08";
				break;
			case "September":
				newDate = arrPhotoDate[1] + "-" + "09";
				break;
			case "October":
				newDate = arrPhotoDate[1] + "-" + "10";
				break;
			case "November":
				newDate = arrPhotoDate[1] + "-" + "11";
				break;
			case "December":
				newDate = arrPhotoDate[1] + "-" + "12";
				break;
			default:
				newDate = arrPhotoDate[1];
				break;
			}
		} else {
			newDate = arrPhotoDate[0];
		}
		;
		return newDate;
	};

}
