/******************************** com.newegg.ec.cache.controller.UserController ********************************/
/**
 * @type GET
 */
function  list(callback){
   ajax.async_get("/user/listUser",callback);
}
/**
 * @type POST 
 * @param  {}
 */
function  addUser(user,callback){
   ajax.async_post("/user/addUser",user,callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  String
 */
function  verifyLogin(username,password,callback){
   ajax.async_get("/user/verifyLogin?username="+username+"&password="+password+"",callback);
}
/**
 * @type GET
 */
function  logout(callback){
   ajax.async_get("/user/logout",callback);
}
/**
 * @type GET 
 * @param  User{id=0, username='null', password='null', userGroup='null'}
 */
function  listGroup(callback){
   ajax.async_get("/user/listGroup",callback);
}
/**
 * @type GET 
 * @param  int
 */
function  getUser(id,callback){
   ajax.async_get("/user/getUser?id="+id+"",callback);
}
/**
 * @type GET 
 * @param  int
 */
function  removeUser(id,callback){
   ajax.async_get("/user/removeUser?id="+id+"",callback);
}
/**
 * @type GET
 */
function  autoGetUser(callback){
   ajax.async_get("/user/autoGetUser",callback);
}
/******************************** com.newegg.ec.cache.controller.AlarmController ********************************/
/**
 * @type POST 
 * @param  ClusterCheckRule{id=null, clusterId='null', limitName='null', formula='null', description='null', updateTime='0'}
 */
function  addRule(rule,callback){
   ajax.async_post("/alarm/addRule",rule,callback);
}
/**
 * @type POST 
 * @param  java.lang.Integer
 */
function  countWarningLogByClusterId(clusterId,callback){
   ajax.async_post("/alarm/countWarningLogByClusterId",clusterId,callback);
}
/**
 * @type GET 
 * @param  String
 */
function  deleteRule(ruleId,callback){
   ajax.async_get("/alarm/deleteRule?ruleId="+ruleId+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  getCaseList(clusterId,callback){
   ajax.async_get("/alarm/getCaseLogs?clusterId="+clusterId+"",callback);
}
/**
 * @type POST 
 * @param  ClusterCheckRule{id=null, clusterId='null', limitName='null', formula='null', description='null', updateTime='0'}
 */
function  checkAlarmRule(rule,callback){
   ajax.async_post("/alarm/checkAlarmRule",rule,callback);
}
/**
 * @type GET 
 * @param  String
 */
function  deleteAllLog(cluster,callback){
   ajax.async_get("/alarm/deleteAllLog?cluster="+cluster+"",callback);
}
/**
 * @type POST 
 * @param  java.util.List
 */
function  countTotalAlarm(clusterIds,callback){
   ajax.async_post("/alarm/countTotal",clusterIds,callback);
}
/**
 * @type GET 
 * @param  String
 */
function  getRuleList(clusterId,callback){
   ajax.async_get("/alarm/getRuleList?clusterId="+clusterId+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  deleteCaseLog(logId,callback){
   ajax.async_get("/alarm/deleteCaseLog?logId="+logId+"",callback);
}
/******************************** com.newegg.ec.cache.controller.NodeController ********************************/
/**
 * @type POST 
 * @param  {}
 */
function  humpbacknodeCheckAccess(reqPram,callback){
   ajax.async_post("/node/humpbacknodeCheckAccess",reqPram,callback);
}
/**
 * @type GET 
 * @param  com.newegg.ec.cache.module.clusterbuild.plugins.basemodel.PluginType 
 * @param  int
 */
function  getNodeByClusterId(pluginType,clusterId,callback){
   ajax.async_get("/node/getNodeByClusterId?pluginType="+pluginType+"&clusterId="+clusterId+"",callback);
}
/**
 * @type POST 
 * @param  {}
 */
function  dockernodeCheckAccess(reqPram,callback){
   ajax.async_post("/node/dockernodeCheckAccess",reqPram,callback);
}
/**
 * @type GET 
 * @param  com.newegg.ec.cache.module.clusterbuild.plugins.basemodel.PluginType
 */
function  getImageList(pluginType,callback){
   ajax.async_get("/node/getImageList?pluginType="+pluginType+"",callback);
}
/**
 * @type POST 
 * @param  OperatePram{pluginType=null, req=null}
 */
function  nodeStop(nodeRequestPram,callback){
   ajax.async_post("/node/nodeStop",nodeRequestPram,callback);
}
/**
 * @type POST 
 * @param  OperatePram{pluginType=null, req=null}
 */
function  nodePullImage(nodeRequestPram,callback){
   ajax.async_post("/node/nodePullImage",nodeRequestPram,callback);
}
/**
 * @type POST 
 * @param  OperatePram{pluginType=null, req=null}
 */
function  nodeRemove(nodeRequestPram,callback){
   ajax.async_post("/node/nodeRemove",nodeRequestPram,callback);
}
/**
 * @type POST 
 * @param  OperatePram{pluginType=null, req=null}
 */
function  nodeStart(nodeRequestPram,callback){
   ajax.async_post("/node/nodeStart",nodeRequestPram,callback);
}
/**
 * @type POST 
 * @param  OperatePram{pluginType=null, req=null}
 */
function  nodeRestart(nodeRequestPram,callback){
   ajax.async_post("/node/nodeRestart",nodeRequestPram,callback);
}
/**
 * @type POST 
 * @param  OperatePram{pluginType=null, req=null}
 */
function  nodeInstall(nodeRequestPram,callback){
   ajax.async_post("/node/nodeInstall",nodeRequestPram,callback);
}
/**
 * @type GET
 */
function  getPluginList(callback){
   ajax.async_get("/node/getPluginList",callback);
}
/**
 * @type GET 
 * @param  com.newegg.ec.cache.module.clusterbuild.plugins.basemodel.PluginType 
 * @param  int
 */
function  getNodeList(pluginType,clusterId,callback){
   ajax.async_get("/node/getNodeList?pluginType="+pluginType+"&clusterId="+clusterId+"",callback);
}
/******************************** com.newegg.ec.cache.controller.MonitorController ********************************/
/**
 * @type GET 
 * @param  int 
 * @param  int 
 * @param  int 
 * @param  String 
 * @param  int
 */
function  monitorGetMaxField(clusterId,startTime,endTime,key,limit,callback){
   ajax.async_get("/monitor/getMaxField?clusterId="+clusterId+"&startTime="+startTime+"&endTime="+endTime+"&key="+key+"&limit="+limit+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  int 
 * @param  int 
 * @param  String 
 * @param  String
 */
function  monitorGetAvgField(clusterId,startTime,endTime,host,key,callback){
   ajax.async_get("/monitor/getAvgField?clusterId="+clusterId+"&startTime="+startTime+"&endTime="+endTime+"&host="+host+"&key="+key+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  int 
 * @param  int 
 * @param  String 
 * @param  String 
 * @param  String
 */
function  monitorGetGroupNodeInfo(clusterId,startTime,endTime,host,type,date,callback){
   ajax.async_get("/monitor/getGroupNodeInfo?clusterId="+clusterId+"&startTime="+startTime+"&endTime="+endTime+"&host="+host+"&type="+type+"&date="+date+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  int 
 * @param  int 
 * @param  String 
 * @param  int
 */
function  monitorGetMinField(clusterId,startTime,endTime,key,limit,callback){
   ajax.async_get("/monitor/getMinField?clusterId="+clusterId+"&startTime="+startTime+"&endTime="+endTime+"&key="+key+"&limit="+limit+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  int 
 * @param  int 
 * @param  String
 */
function  monitorGetAllField(clusterId,startTime,endTime,key,callback){
   ajax.async_get("/monitor/getAllField?clusterId="+clusterId+"&startTime="+startTime+"&endTime="+endTime+"&key="+key+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  monitorGetDbSize(clusterId,host,callback){
   ajax.async_get("/monitor/getDbSize?clusterId="+clusterId+"&host="+host+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  int 
 * @param  int 
 * @param  String
 */
function  monitorGetLastNodeInfo(clusterId,startTime,endTime,host,callback){
   ajax.async_get("/monitor/getLastNodeInfo?clusterId="+clusterId+"&startTime="+startTime+"&endTime="+endTime+"&host="+host+"",callback);
}
/**
 * @type POST 
 * @param  SlowLogParam{clusterId=0hostList=null, logLimit=0}
 */
function  monitorSlowLogs(logParam,callback){
   ajax.async_post("/monitor/slowLogs",logParam,callback);
}
