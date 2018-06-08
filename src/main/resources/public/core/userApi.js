/******************************** com.newegg.ec.cache.app.controller.UserController ********************************/
/**
 * @type GET
 */
function  list(callback){
   ajax.async_get("/user/listUser",callback);
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
 * @param  User{id=0, username='null', password='null', userGroup='null'}
 */
function  listGroup(callback){
   ajax.async_get("/user/listGroup",callback);
}
/**
 * @type POST 
 * @param  User{id=0, username='null', password='null', userGroup='null'}
 */
function  addUser(user,callback){
   ajax.async_post("/user/addUser",user,callback);
}
/******************************** com.newegg.ec.cache.app.controller.NodeController ********************************/
/**
 * @type POST 
 * @param  {}
 */
function  dockernodeCheckAccess(reqPram,callback){
   ajax.async_post("/node/dockernodeCheckAccess",reqPram,callback);
}
/**
 * @type GET 
 * @param  com.newegg.ec.cache.plugin.basemodel.PluginType 
 * @param  int
 */
function  getNodeByClusterId(pluginType,clusterId,callback){
   ajax.async_get("/node/getNodeByClusterId?pluginType="+pluginType+"&clusterId="+clusterId+"",callback);
}
/**
 * @type POST 
 * @param  {}
 */
function  humpbacknodeCheckAccess(reqPram,callback){
   ajax.async_post("/node/humpbacknodeCheckAccess",reqPram,callback);
}
/**
 * @type GET 
 * @param  com.newegg.ec.cache.plugin.basemodel.PluginType 
 * @param  int
 */
function  getNodeList(pluginType,clusterId,callback){
   ajax.async_get("/node/getNodeList?pluginType="+pluginType+"&clusterId="+clusterId+"",callback);
}
/**
 * @type POST 
 * @param  OperatePram{pluginType=null, req=null}
 */
function  nodeInstall(nodeRequestPram,callback){
   ajax.async_post("/node/nodeInstall",nodeRequestPram,callback);
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
function  nodeStop(nodeRequestPram,callback){
   ajax.async_post("/node/nodeStop",nodeRequestPram,callback);
}
/**
 * @type GET 
 * @param  com.newegg.ec.cache.plugin.basemodel.PluginType
 */
function  getImageList(pluginType,callback){
   ajax.async_get("/node/getImageList?pluginType="+pluginType+"",callback);
}
/**
 * @type GET
 */
function  getPluginList(callback){
   ajax.async_get("/node/getPluginList",callback);
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
function  nodePullImage(nodeRequestPram,callback){
   ajax.async_post("/node/nodePullImage",nodeRequestPram,callback);
}
/******************************** com.newegg.ec.cache.app.controller.ClusterController ********************************/
/**
 * @type GET 
 * @param  int
 */
function  getCluster(id,callback){
   ajax.async_get("/cluster/getCluster?id="+id+"",callback);
}
/**
 * @type GET 
 * @param  User{id=0, username='null', password='null', userGroup='null'}
 */
function  listClusterByUser(callback){
   ajax.async_get("/cluster/listClusterByUser",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  clusterExistAddress(address,callback){
   ajax.async_get("/cluster/clusterExistAddress?address="+address+"",callback);
}
/**
 * @type GET 
 * @param  User{id=0, username='null', password='null', userGroup='null'}
 */
function  getClusterListInfo(callback){
   ajax.async_get("/cluster/getClusterListInfo",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  getClusterInfoByAddress(address,callback){
   ajax.async_get("/cluster/getClusterInfoByAddress?address="+address+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  redisDbList(address,callback){
   ajax.async_get("/cluster/redisDbList?address="+address+"",callback);
}
/**
 * @type GET 
 * @param  int
 */
function  getClusterHost(id,callback){
   ajax.async_get("/cluster/getClusterHost?id="+id+"",callback);
}
/**
 * @type POST 
 * @param  Cluster{id=0, clusterName='null', userGroup='null', address='null', redisPassword='null', sslUsername='null', sslPassword='null', clusterType='null'}
 */
function  addCluster(cluster,callback){
   ajax.async_post("/cluster/addCluster",cluster,callback);
}
/**
 * @type GET 
 * @param  String
 */
function  getNodeInfo(address,callback){
   ajax.async_get("/cluster/getNodeInfo?address="+address+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  listCluster(group,callback){
   ajax.async_get("/cluster/listCluster?group="+group+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  removeCluster(clusterId,callback){
   ajax.async_get("/cluster/removeCluster?clusterId="+clusterId+"",callback);
}
/**
 * @type POST 
 * @param  com.newegg.ec.cache.app.model.RedisQueryParam@3d512652
 */
function  redisQuery(redisQueryParam,callback){
   ajax.async_post("/cluster/redisQuery",redisQueryParam,callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int 
 * @param  String 
 * @param  int
 */
function  importNode(ip,port,masterIP,masterPort,callback){
   ajax.async_get("/cluster/importNode?ip="+ip+"&port="+port+"&masterIP="+masterIP+"&masterPort="+masterPort+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  detailNodeList(address,callback){
   ajax.async_get("/cluster/detailNodeList?address="+address+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  initSlot(address,callback){
   ajax.async_get("/cluster/initSlot?address="+address+"",callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int 
 * @param  String
 */
function  forgetNode(ip,port,masterId,callback){
   ajax.async_get("/cluster/forgetNode?ip="+ip+"&port="+port+"&masterId="+masterId+"",callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int 
 * @param  int 
 * @param  int
 */
function  moveSlot(ip,port,startKey,endKey,callback){
   ajax.async_get("/cluster/moveSlot?ip="+ip+"&port="+port+"&startKey="+startKey+"&endKey="+endKey+"",callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int 
 * @param  String 
 * @param  String
 */
function  batchConfig(ip,port,configName,configValue,callback){
   ajax.async_get("/cluster/batchConfig?ip="+ip+"&port="+port+"&configName="+configName+"&configValue="+configValue+"",callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int
 */
function  getClusterInfo(ip,port,callback){
   ajax.async_get("/cluster/getClusterInfo?ip="+ip+"&port="+port+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  nodeList(address,callback){
   ajax.async_get("/cluster/nodeList?address="+address+"",callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int 
 * @param  String
 */
function  beSlave(ip,port,masterId,callback){
   ajax.async_get("/cluster/beSlave?ip="+ip+"&port="+port+"&masterId="+masterId+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  getRedisConfig(address,callback){
   ajax.async_get("/cluster/getRedisConfig?address="+address+"",callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int
 */
function  beMaster(ip,port,callback){
   ajax.async_get("/cluster/beMaster?ip="+ip+"&port="+port+"",callback);
}
/******************************** com.newegg.ec.cache.app.controller.check.CheckController ********************************/
/**
 * @type GET 
 * @param  String
 */
function  checkAddress(address,callback){
   ajax.async_get("/check/checkAddress?address="+address+"",callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkBatchWgetPermission(req,callback){
   ajax.async_post("/check/checkBatchWgetPermission",req,callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkBatchHostNotPass(req,callback){
   ajax.async_post("/check/checkBatchHostNotPass",req,callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkBatchHumpbackContainerName(req,callback){
   ajax.async_post("/check/checkBatchHumpbackContainerName",req,callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  String 
 * @param  String
 */
function  checkUserPermisson(ip,userName,password,callback){
   ajax.async_get("/check/checkUserPermisson?ip="+ip+"&userName="+userName+"&password="+password+"",callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkBatchDirPermission(req,callback){
   ajax.async_post("/check/checkBatchDirPermission",req,callback);
}
/**
 * @type GET 
 * @param  String
 */
function  checkRedisVersion(address,callback){
   ajax.async_get("/check/checkVersion?address="+address+"",callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkBatchUserPermisson(req,callback){
   ajax.async_post("/check/checkBatchUserPermisson",req,callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkRule(req,callback){
   ajax.async_post("/check/checkRule",req,callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int
 */
function  checkPortPass(ip,port,callback){
   ajax.async_get("/check/checkPortPass?ip="+ip+"&port="+port+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  checkClusterName(clusterId,callback){
   ajax.async_get("/check/checkClusterName?clusterId="+clusterId+"",callback);
}
/**
 * @type GET 
 * @param  String 
 * @param  int
 */
function  checkPortNotPass(ip,port,callback){
   ajax.async_get("/check/checkPortNotPass?ip="+ip+"&port="+port+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  checkIp(ip,callback){
   ajax.async_get("/check/checkIp?ip="+ip+"",callback);
}
/******************************** com.newegg.ec.cache.app.controller.MonitorController ********************************/
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
function  monitorGetMaxField(clusterId,startTime,endTime,key,limit,callback){
   ajax.async_get("/monitor/getMaxField?clusterId="+clusterId+"&startTime="+startTime+"&endTime="+endTime+"&key="+key+"&limit="+limit+"",callback);
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
 * @param  SlowLogParam{hostList=null, logLimit=0}
 */
function  monitorSlowLogs(logParam,callback){
   ajax.async_post("/monitor/slowLogs",logParam,callback);
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
/******************************** com.newegg.ec.cache.app.controller.AlarmController ********************************/
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
function  deleteCaseLog(logId,callback){
   ajax.async_get("/alarm/deleteCaseLog?logId="+logId+"",callback);
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
function  getCaseList(clusterId,callback){
   ajax.async_get("/alarm/getCaseLogs?clusterId="+clusterId+"",callback);
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
function  deleteRule(ruleId,callback){
   ajax.async_get("/alarm/deleteRule?ruleId="+ruleId+"",callback);
}
