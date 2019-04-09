$(document).ready(function(){
    window.pluginType = getQueryString("pluginType");
    window.clusterId = getQueryString("clusterId");
    getCluster(clusterId, function(obj){
        var cluster = obj.res;
        nodeList(clusterId, cluster.address, function(obj){
            window.nodeList = obj.res;
            rebuildNodeListTable( window.clusterId );
        });

        getNodeList(window.pluginType, window.clusterId, function(obj){
            window.nodeListDBSize = obj.res.length;
        });
    });
});

function rebuildNodeListTable(){
    smarty.get( "/node/getNodeList?pluginType="+ window.pluginType  +"&clusterId=" + window.clusterId , "plugin/" + window.pluginType + "/" + window.pluginType + "_mode_manager", "node-list", function(obj){
        /*$("table").dataTable({
        });*/
    }, true );
}

$("[href='#node-list']").click( function () {
    rebuildNodeListTable();
});

$("[href='#add-node']").click( function () {
    $('iframe[name="node-install-manager"]').attr("src","/node/install?pluginType="+ window.pluginType +"&clusterId=" + window.clusterId);
});

$(document).on("click", ".start-node", function(){
    var reqParam = getReqParam( this );
    if( reqParam.status == "OK" ){
        sparrow_win.alert("The node already start");
        return;
    }
    sparrow_win.confirm("Confirm start the node", function(){
        nodeStart(reqParam, function(obj){
        });
    });
});

$(document).on("click", ".stop-node", function(){
    var reqParam = getReqParam( this );
    reqParam.clusterId = window.clusterId;
    if( reqParam.inCluster == "YES" && window.nodeListDBSize != 1 ){
        sparrow_win.alert("The node is in cluster please forget it then retry");
        return;
    }
    sparrow_win.confirm("Confirm stop the node", function(){
        nodeStop(reqParam, function(obj){
            $("[href='#node-list']").trigger("click");
        });
    });
});

$(document).on("click", ".restart-node", function(){
    var reqParam = getReqParam( this );
    sparrow_win.confirm("Confirm restart the node", function(){
        nodeRestart(reqParam, function(obj){
            $("[href='#node-list']").trigger("click");
        });
    });
});

$(document).on("click", ".delete-node", function(){
    var reqParam = getReqParam( this );
    if( reqParam.status == "OK" ){
        sparrow_win.alert("The node is running please stop it");
        return;
    }
    sparrow_win.confirm("Confirm delete the node", function(){
        nodeRemove(reqParam, function(obj){
            $("[href='#node-list']").trigger("click");
        });
    });
});

$(document).on("click", ".import-node-to-cluster", function(){
    var reqParam = getReqParam( this );
    if( reqParam.inCluster == "YES" ){
        sparrow_win.alert("The node is already in cluster");
        return;
    }
    sparrow_win.confirm("Confirm import the node to cluster", function(){
        var nodeDetail = reqParam.req;
        var clusterId = nodeDetail.clusterId;
        getCluster( clusterId, function(obj){
            var cluster = obj.res;
            var hostArr = cluster.address.split(",");
            var tmps = hostArr[0].split(":");
            var masterIp = tmps[0];
            var masterPort = tmps[1];
            importNode(cluster.id, nodeDetail.ip, nodeDetail.port,masterIp,masterPort, function(){
                $("[href='#node-list']").trigger("click");
            });
        });
    });
});



function getReqParam(obj){
    var nodeRequestPram = {"pluginType": window.pluginType};
    var detail = getNodeDetail(obj);
    var id = detail.id;
    nodeRequestPram.inCluster = $("#in-cluster-" + id).text();
    nodeRequestPram.status = $("#node-" + id).data("status");
    nodeRequestPram.req = detail;
    return nodeRequestPram;
}

function getNodeDetail(obj){
    var id = $( obj ).data("id");
    var detail = $("#node-" + id).data("detail");
    return detail;
}
