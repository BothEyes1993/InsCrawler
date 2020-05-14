package com.sbcm.utils;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsCrawlerTest {

	private static final Logger logger = LoggerFactory.getLogger(InsCrawlerTest.class);

	private String full_id ="";
	private String full_name ="";
	private String end_cursor="";
	private Boolean has_next_page=true;
	private List<Map> userList = new ArrayList<Map>();

	public void runingXml(String url){
		String charset = "UTF-8";
		String xmlResult = ConnectionUtils.get(url, charset);// 得到xml字符串
		if(StringUtils.isBlank(xmlResult)){
			return;
		}
		try {
			List<String> lists = (new InsCrawlerTest()).getSubUtil(xmlResult,"window._sharedData = (.*?);</script>");
			JSONObject shareData = JSONObject.parseObject(lists.get(0));

			JSONObject entry_data = JSONObject.parseObject(shareData.getString("entry_data"));
			JSONArray ProfilePage = entry_data.getJSONArray("ProfilePage");
			JSONObject p0 = ProfilePage.getJSONObject(0);
			String user =JSONObject.parseObject(p0.getString("graphql")).getString("user");
			String edge_owner =JSONObject.parseObject(user).getString("edge_owner_to_timeline_media");
			JSONArray edges =JSONObject.parseObject(edge_owner).getJSONArray("edges");

			for (int i = 0; i < edges.size(); i++) {
				JSONObject ind = edges.getJSONObject(i);
				JSONObject jo = JSONObject.parseObject(ind.getString("node"));
				String id = jo.getString("id");
				String typename = jo.getString("__typename");
				String display_url = jo.getString("display_url");
				Integer comment = JSONObject.parseObject(jo.getString("edge_media_to_comment")).getInteger("count");
				Integer like = JSONObject.parseObject(jo.getString("edge_media_preview_like")).getInteger("count");

				Map usermap = new HashMap();
				usermap.put("id",id);
				usermap.put("typename",typename);
				usermap.put("display_url",display_url);
				usermap.put("comment",comment);
				usermap.put("like",like);
				userList.add(usermap);
			}
			logger.info("html抓取结果成功:"+userList.size(),userList);

			JSONObject page_info =JSONObject.parseObject(JSONObject.parseObject(edge_owner).getString("page_info"));
			end_cursor = page_info.getString("end_cursor");
			has_next_page = page_info.getBoolean("has_next_page");
			full_id = JSONObject.parseObject(user).getString("id");
			full_name = JSONObject.parseObject(user).getString("full_name");
			runingPage();
		} catch (Exception e) {
			logger.error("xml解析失败:",e);
		}
	}

	public void runingPage(){
		if(!has_next_page){
			logger.info("has_next_page为false。总数据："+userList.size(),userList);
			exportUser();
			return;
		}
		String charset = "UTF-8";
		JSONObject variables = new JSONObject();
		variables.put("id",full_id);
		variables.put("first",50);
		variables.put("after",end_cursor);
		String variablesStr = variables.toJSONString();
		try {
			variablesStr= URLEncoder.encode(variablesStr,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("variables参数错误:"+e.getMessage());
		}
		String url = "https://www.instagram.com/graphql/query/?query_hash=9dcf6e1a98bc7f6e92953d5a61027b98&variables="+variablesStr;
		String jsonResult = ConnectionUtils.get(url, charset);// 得到JSON字符串
		if(StringUtils.isBlank(jsonResult)){
			return;
		}
		try {
			JSONObject object = JSONObject.parseObject(jsonResult);// 转化为JSON类
			String status =object.getString("status");
			String data =object.getString("data");
			if(("ok".equals(status) || "ok"==status) && !StringUtils.isBlank(data)){
				String user =JSONObject.parseObject(data).getString("user");
				String edge_owner =JSONObject.parseObject(user).getString("edge_owner_to_timeline_media");
				JSONObject page_info =JSONObject.parseObject(JSONObject.parseObject(edge_owner).getString("page_info"));
				end_cursor = page_info.getString("end_cursor");
				has_next_page = page_info.getBoolean("has_next_page");

				JSONArray edges =JSONObject.parseObject(edge_owner).getJSONArray("edges");
				for (int i = 0; i < edges.size(); i++) {
					JSONObject ind = edges.getJSONObject(i);
					JSONObject jo = JSONObject.parseObject(ind.getString("node"));
					String id = jo.getString("id");
					String typename = jo.getString("__typename");
					String display_url = jo.getString("display_url");
					Integer comment = JSONObject.parseObject(jo.getString("edge_media_to_comment")).getInteger("count");
					Integer like = JSONObject.parseObject(jo.getString("edge_media_preview_like")).getInteger("count");

					Map usermap = new HashMap();
					usermap.put("id",id);
					usermap.put("typename",typename);
					usermap.put("display_url",display_url);
					usermap.put("comment",comment);
					usermap.put("like",like);
					userList.add(usermap);
				}
				logger.info("json抓取结果成功:"+userList.size(),userList);
				runingPage();
			}
		} catch (Exception e) {
			logger.error("json解析失败:",e);
		}
	}

	public void exportUser(){
		String[] headers = {"ID","类型","评论数","点赞数","封面地址"};
		String [] properties = new String[]{"id","typename","comment","like","display_url"};  // 查询对应的字段
		try {
			String path = System.getProperty("user.dir");
			OutputStream os = new FileOutputStream(path+"/"+full_name+".xls");
			ExcelExportUtil transToExcel = new ExcelExportUtil();
			transToExcel.exporteExcel(full_name,headers,properties,userList,os);
			os.close();

		}catch (FileNotFoundException e){
			System.out.println("无法找到文件");
		}catch (IOException e){
			System.out.println("写入文件失败");
		}
	}

	public List<String> getSubUtil(String soap, String rgex){
		List<String> list = new ArrayList<String>();
		Pattern pattern = Pattern.compile(rgex);// 匹配的模式
		Matcher m = pattern.matcher(soap);
		while (m.find()) {
			int i = 1;
			list.add(m.group(i));
			i++;
		}
		return list;
	}

	public void runCrawler(){
		runingXml("https://www.instagram.com/bingbing_fan/");
	}

	public static void main(String[] args) {
		(new InsCrawlerTest()).runCrawler();
	}
	
}
