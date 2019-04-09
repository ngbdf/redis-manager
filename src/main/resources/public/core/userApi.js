/******************************** com.newegg.ec.cache.controller.NodeController ********************************/
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
 * @param  com.newegg.ec.cache.module.clusterbuild.plugins.basemodel.PluginType 
 * @param  int
 */
function  getNodeList(pluginType,clusterId,callback){
   ajax.async_get("/node/getNodeList?pluginType="+pluginType+"&clusterId="+clusterId+"",callback);
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
 * @param  com.newegg.ec.cache.module.clusterbuild.plugins.basemodel.PluginType
 */
function  getImageList(pluginType,callback){
   ajax.async_get("/node/getImageList?pluginType="+pluginType+"",callback);
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
 * @param  {}
 */
function  humpbacknodeCheckAccess(reqPram,callback){
   ajax.async_post("/node/humpbacknodeCheckAccess",reqPram,callback);
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
 * @param  int
 */
function  getNodeByClusterId(pluginType,clusterId,callback){
   ajax.async_get("/node/getNodeByClusterId?pluginType="+pluginType+"&clusterId="+clusterId+"",callback);
}
/******************************** com.newegg.ec.cache.controller.MonitorController ********************************/
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
 * @type POST 
 * @param  SlowLogParam{clusterId=0hostList=null, logLimit=0}
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
/******************************** com.newegg.ec.cache.controller.check.CheckController ********************************/
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
 */
function  checkClusterName(clusterId,callback){
   ajax.async_get("/check/checkClusterName?clusterId="+clusterId+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  checkIp(ip,callback){
   ajax.async_get("/check/checkIp?ip="+ip+"",callback);
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
function  checkAddress(address,callback){
   ajax.async_get("/check/checkAddress?address="+address+"",callback);
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
 * @type POST 
 * @param  String
 */
function  checkBatchHumpbackContainerName(req,callback){
   ajax.async_post("/check/checkBatchHumpbackContainerName",req,callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkBatchHostNotPass(req,callback){
   ajax.async_post("/check/checkBatchHostNotPass",req,callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  checkRedisVersion(clusterId,address,callback){
   ajax.async_get("/check/checkVersion?clusterId="+clusterId+"&address="+address+"",callback);
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
function  checkBatchWgetPermission(req,callback){
   ajax.async_post("/check/checkBatchWgetPermission",req,callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkBatchDirPermission(req,callback){
   ajax.async_post("/check/checkBatchDirPermission",req,callback);
}
/**
 * @type POST 
 * @param  String
 */
function  checkBatchUserPermisson(req,callback){
   ajax.async_post("/check/checkBatchUserPermisson",req,callback);
}
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
 * @param  int
 */
function  getUser(id,callback){
   ajax.async_get("/user/getUser?id="+id+"",callback);
}
/**
 * @type GET
 */
function  logout(callback){
   ajax.async_get("/user/logout",callback);
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
 * @param  ClusterCheckRule{id=null, clusterId='null', limitName='null', formula='null', description='null', updateTime='0'}
 */
function  checkAlarmRule(rule,callback){
   ajax.async_post("/alarm/checkAlarmRule",rule,callback);
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
function  deleteRule(ruleId,callback){
   ajax.async_get("/alarm/deleteRule?ruleId="+ruleId+"",callback);
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
function  deleteAllLog(cluster,callback){
   ajax.async_get("/alarm/deleteAllLog?cluster="+cluster+"",callback);
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
function  getCaseList(clusterId,callback){
   ajax.async_get("/alarm/getCaseLogs?clusterId="+clusterId+"",callback);
}
/**
 * @type POST 
 * @param  java.lang.Integer
 */
function  countWarningLogByClusterId(clusterId,callback){
   ajax.async_post("/alarm/countWarningLogByClusterId",clusterId,callback);
}
/******************************** com.newegg.ec.cache.controller.ClusterController ********************************/
/**
 * @type GET 
 * @param  int
 */
function  getCluster(id,callback){
   ajax.async_get("/cluster/getCluster?id="+id+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  int
 */
function  getClusterInfo(clusterId,ip,port,callback){
   ajax.async_get("/cluster/getClusterInfo?clusterId="+clusterId+"&ip="+ip+"&port="+port+"",callback);
}
/**
 * @type POST 
 * @param  com.newegg.ec.cache.core.entity.redis.RedisQueryParam@4c04216f
 */
function  redisQuery(redisQueryParam,callback){
   ajax.async_post("/cluster/redisQuery",redisQueryParam,callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  nodeList(clusterId,address,callback){
   ajax.async_get("/cluster/nodeList?clusterId="+clusterId+"&address="+address+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  detailNodeList(clusterId,address,callback){
   ajax.async_get("/cluster/detailNodeList?clusterId="+clusterId+"&address="+address+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  redisDbList(clusterId,address,callback){
   ajax.async_get("/cluster/redisDbList?clusterId="+clusterId+"&address="+address+"",callback);
}
/**
 * @type GET 
 * @param  int
 */
function  getClusterHost(id,callback){
   ajax.async_get("/cluster/getClusterHost?id="+id+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  getRedisConfig(clusterId,address,callback){
   ajax.async_get("/cluster/getRedisConfig?clusterId="+clusterId+"&address="+address+"",callback);
}
/**
 * @type GET
 */
function  clustersGroup(callback){
   ajax.async_get("/cluster/clustersGroup",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  int 
 * @param  String
 */
function  beSlave(clusterId,ip,port,masterId,callback){
   ajax.async_get("/cluster/beSlave?clusterId="+clusterId+"&ip="+ip+"&port="+port+"&masterId="+masterId+"",callback);
}
/**
 * @type GET
 */
function  listCluster(callback){
   ajax.async_get("/cluster/listCluster",callback);
}
/**
 * @type POST 
 * @param  Cluster{id=0, clusterName='null', userGroup='null', address='null', redisPassword='null', clusterType='null', isVersion4=false}
 */
function  addCluster(cluster,callback){
   ajax.async_post("/cluster/addCluster",cluster,callback);
}
/**
 * @type GET 
 * @param  String
 */
function  removeCluster(clusterId,callback){
   ajax.async_get("/cluster/removeCluster?clusterId="+clusterId+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  getNodeInfo(clusterId,address,callback){
   ajax.async_get("/cluster/getNodeInfo?clusterId="+clusterId+"&address="+address+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  int
 */
function  beMaster(clusterId,ip,port,callback){
   ajax.async_get("/cluster/beMaster?clusterId="+clusterId+"&ip="+ip+"&port="+port+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  int
 */
function  memoryPurge(clusterId,ip,port,callback){
   ajax.async_get("/cluster/memoryPurge?clusterId="+clusterId+"&ip="+ip+"&port="+port+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  int 
 * @param  int 
 * @param  int
 */
function  moveSlot(clusterId,ip,port,startKey,endKey,callback){
   ajax.async_get("/cluster/moveSlot?clusterId="+clusterId+"&ip="+ip+"&port="+port+"&startKey="+startKey+"&endKey="+endKey+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  initSlot(clusterId,address,callback){
   ajax.async_get("/cluster/initSlot?clusterId="+clusterId+"&address="+address+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  int 
 * @param  String
 */
function  forgetNode(clusterId,ip,port,masterId,callback){
   ajax.async_get("/cluster/forgetNode?clusterId="+clusterId+"&ip="+ip+"&port="+port+"&masterId="+masterId+"",callback);
}
/**
 * @type GET 
 * @param  int
 */
function  memoryDoctor(clusterId,callback){
   ajax.async_get("/cluster/memoryDoctor?clusterId="+clusterId+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  int 
 * @param  String 
 * @param  int
 */
function  importNode(clusterId,ip,port,masterIP,masterPort,callback){
   ajax.async_get("/cluster/importNode?clusterId="+clusterId+"&ip="+ip+"&port="+port+"&masterIP="+masterIP+"&masterPort="+masterPort+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  int 
 * @param  String 
 * @param  String
 */
function  batchConfig(clusterId,ip,port,configName,configValue,callback){
   ajax.async_get("/cluster/batchConfig?clusterId="+clusterId+"&ip="+ip+"&port="+port+"&configName="+configName+"&configValue="+configValue+"",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  getClusterListByGroup(group,callback){
   ajax.async_get("/cluster/getClusterListByGroup?group="+group+"",callback);
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
 * @param  User{id=0, username='null', password='null', userGroup='null'}
 */
function  listClusterByUser(callback){
   ajax.async_get("/cluster/listClusterByUser",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String 
 * @param  String 
 * @param  String
 */
function  importDataToCluster(clusterId,address,targetAddress,keyFormat,callback){
   ajax.async_get("/cluster/importDataToCluster?clusterId="+clusterId+"&address="+address+"&targetAddress="+targetAddress+"&keyFormat="+keyFormat+"",callback);
}
/**
 * @type GET 
 * @param  int 
 * @param  String
 */
function  getClusterInfoByAddress(clusterId,address,callback){
   ajax.async_get("/cluster/getClusterInfoByAddress?clusterId="+clusterId+"&address="+address+"",callback);
}
/**
 * @type GET
 */
function  getImportCountList(callback){
   ajax.async_get("/cluster/getImportCountList",callback);
}
/**
 * @type GET 
 * @param  String
 */
function  clusterExistAddress(address,callback){
   ajax.async_get("/cluster/clusterExistAddress?address="+address+"",callback);
}
