smarty.register_function( 'json_format', function( params ){
    var node = params['node'];
    return JSON.stringify(node);
});


smarty.register_function( 'service_status', function( params ){
    var ip = params['ip'];
    var port = params['port'];
    var id = params["id"];
    checkPortPass(ip, port, function(obj){
        var code = parseInt(obj.code);
        var statusObj = $("#status-" + id );
        var status = "FAIL";
        if(code == 0){
            status = "OK";
        }
        statusObj.text( status );
        $("#node-" + id).data("status", status);
    });
});


smarty.register_function( 'in_cluster', function( params ){
    var ip = params['ip'];
    var port = params['port'];
    /*var host = ip + port;*/
    var id = params['id'];
    var ipPort = ip+port;
    var nodeList = window.nodeList;
    console.log( nodeList );
    var isInCluster = false;
    for(var i = 0, len = nodeList.length; i < len; i++){
        var node = nodeList[i];
        var comparedIpPort = node.ip + node.port;
        if(ipPort == comparedIpPort){
           isInCluster = true;
           break;
        }
    }
    var inCluster = "NO";
    if( isInCluster ){
        inCluster = "YES";
    }
    return inCluster;
});