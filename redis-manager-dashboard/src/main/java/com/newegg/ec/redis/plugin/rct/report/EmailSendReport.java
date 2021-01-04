package com.newegg.ec.redis.plugin.rct.report;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.newegg.ec.redis.config.RCTConfig;
import com.newegg.ec.redis.entity.ExcelData;
import com.newegg.ec.redis.entity.RDBAnalyze;
import com.newegg.ec.redis.entity.ReportData;
import com.newegg.ec.redis.plugin.rct.cache.AppCache;
import com.newegg.ec.redis.plugin.rct.report.converseFactory.ReportDataConverseFacotry;
import com.newegg.ec.redis.util.ExcelUtil;
import com.newegg.ec.redis.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author kz37
 * @date 2018/10/23
 */
public class EmailSendReport {
    private static final String PROJECT_PATH = System.getProperty("user.dir");

    private final static Logger LOG = LoggerFactory.getLogger(EmailSendReport.class);

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public void sendEmailReport(RDBAnalyze rdbAnalyze, RCTConfig.Email emailInfo, Map<String,String> reportData, Map<String, ReportData> latestPrefixData) {

        File reportFile = null;
        try {
        	LOG.info("start send email!");
            //获取写入Excel的数据
            Map<String, List<ExcelData>> excelDataMap = getExcelDataMap(reportData, latestPrefixData);
            String source = PROJECT_PATH + "/template/RCT_Report_Template.xlsx";
            String time = simpleDateFormat.format(AppCache.scheduleProcess.get(rdbAnalyze.getId()));
            String dest = PROJECT_PATH + "/template/" + rdbAnalyze.getId() + "/" + "RCT_Report_"+time+".xlsx";
            FileUtil.copyFile(source, dest);
            reportFile = new File(dest);
            ExcelUtil.writeExcelToFile(excelDataMap, reportFile);
            Properties properties = new Properties();
            if (null != emailInfo.getUserName() && null != emailInfo.getPassword()) {
                properties.setProperty("mail.user", emailInfo.getUserName());
                properties.setProperty("mail.password", emailInfo.getPassword());
            }
            properties.setProperty("mail.smtp.host", emailInfo.getSmtp());
            Session session = Session.getDefaultInstance(properties);
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(emailInfo.getFromName()));
            String [] sendToArr = rdbAnalyze.getMailTo().split(";");
            List<InternetAddress> list = new ArrayList<InternetAddress>(sendToArr.length);
            for(int i = 0; i < sendToArr.length; i++){
                list.add(new InternetAddress(sendToArr[i]));
            }
            InternetAddress[] addresses = (InternetAddress[])list.toArray(new InternetAddress[list.size()]);
            mimeMessage.setRecipients(Message.RecipientType.TO, addresses);
            
//            String subject = rdbAnalyze.getClusterId()+" "+emailInfo.getSubject() +" "+ time;
            String subject = rdbAnalyze.getCluster().getClusterName()+" "+emailInfo.getSubject() +" "+ time;
            MimeBodyPart text = new MimeBodyPart();
            String emailText = "Hi all: \n \n The latest redis analysis report has been generated, see attachment. \n \n";
            text.setText(emailText);
            MimeBodyPart attachFile = new MimeBodyPart();
            attachFile.attachFile(reportFile);
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(text);
            mp.addBodyPart(attachFile);
            mimeMessage.setSubject(subject);
            mimeMessage.setSentDate(new Date());
            mimeMessage.setContent(mp);
            Transport.send(mimeMessage);
            LOG.info("sendEmail success");
        }
        catch(Exception e){
            LOG.error("sendEmail faild", e);
        }
        finally {
            if(null != reportFile) {
                reportFile.delete();
            }
        }
    }


    /**
     *
     * @param reportData key: AnalyzerConstant, value: Set<String> :[{},{}]
     * @return Map key: sheetName, List<ExcelData>
     */
    private Map<String, List<ExcelData>> getExcelDataMap(Map<String,String> reportData, Map<String, ReportData> latestPrefixData) {
        Map<String, List<ExcelData>> mapResult = new HashMap<>();
        IAnalyzeDataConverse analyzeDataConverse = null;
//        String prefixCount = reportData.getOrDefault(IAnalyzeDataConverse.PREFIX_KEY_BY_COUNT,null);
//        String prefixMemory = reportData.getOrDefault(IAnalyzeDataConverse.PREFIX_KEY_BY_MEMORY,null);
//        List<String> prefix = new ArrayList<>();
//        Map<String,String> map = new HashMap<>();
//        if(Objects.nonNull(prefixCount)){
//            JSONArray array = JSONObject.parseArray(prefixCount);
//            for (int i = 0;i<array.size();i++){
//                JSONObject object = array.getJSONObject(i);
//                if(map.containsKey("prefixKey")){
//
//                }
//                map.put(object.getString("prefixKey"), object.toJSONString());
//            }
//           prefix.addAll(new ArrayList<>(JSONObject.parseArray(prefixCount, String.class)));
//        }
//        if(Objects.nonNull(prefixMemory)){
//            prefix.addAll(new ArrayList<>(JSONObject.parseArray(prefixMemory, String.class)));
//        }
//
//
//        reportData.remove(IAnalyzeDataConverse.PREFIX_KEY_BY_MEMORY);
//        reportData.put(IAnalyzeDataConverse.PREFIX_KEY_BY_COUNT,JSONObject.toJSONString(prefix));

        for(Map.Entry<String,String> entry : reportData.entrySet()) {
            analyzeDataConverse = ReportDataConverseFacotry.getReportDataConverseByString(entry.getKey());
            if(null != analyzeDataConverse) {
                List<String> list = new ArrayList<>();
                if(Objects.equals(IAnalyzeDataConverse.TOP_KEY_ANALYZE,entry.getKey())){
                    JSONObject object = JSONObject.parseObject(entry.getValue());
                    for (String key:object.keySet()){
                        list.addAll(new ArrayList<>(JSONObject.parseArray(object.getString(key), String.class)));
                    }
                    mapResult.putAll(analyzeDataConverse.getPrefixAnalyzerData(new HashSet<>(list), latestPrefixData));
                }else if (Objects.equals(IAnalyzeDataConverse.PREFIX_KEY_BY_MEMORY,entry.getKey())){
                    list.addAll(new ArrayList<>(JSONObject.parseArray(entry.getValue(), String.class)));
                    mapResult.put(entry.getKey(),analyzeDataConverse.getPrefixAnalyzerData(new HashSet<>(list), latestPrefixData).getOrDefault(entry.getKey(),new ArrayList<>()));
                }else if(Objects.equals(IAnalyzeDataConverse.PREFIX_KEY_BY_COUNT,entry.getKey())){
                    list.addAll(new ArrayList<>(JSONObject.parseArray(entry.getValue(), String.class)));
                    mapResult.put(entry.getKey(),analyzeDataConverse.getPrefixAnalyzerData(new HashSet<>(list), latestPrefixData).getOrDefault(entry.getKey(),new ArrayList<>()));
                }else {
                    list.addAll(new ArrayList<>(JSONObject.parseArray(entry.getValue(), String.class)));
                    mapResult.putAll(analyzeDataConverse.getPrefixAnalyzerData(new HashSet<>(list), latestPrefixData));
                }

            }
        }
        return mapResult;
    }
}
