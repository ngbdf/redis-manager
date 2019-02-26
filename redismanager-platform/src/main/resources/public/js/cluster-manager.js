$(document).ready(function(){
    window.pluginType = getQueryString("pluginType");
    window.clusterId = getQueryString("clusterId");
    getCluster(window.clusterId, function(obj){
        window.cluster = obj.res;
        getClusterInfoByAddress(window.clusterId, window.cluster.address, function(obj){
            if( obj.res.cluster_slots_ok == 0 ){
                sparrow_win.confirm("the cluster is not slot can you init it", function(){
                    initSlot(window.clusterId, window.cluster.address, function( obj ){
                    });
                });
            }
        });
        checkRedisVersion(window.clusterId, window.cluster.address,function(obj){
            if( obj.res  == 2 ){
                sparrow_win.confirm("Only support Redis Cluster mode", function(){
                    window.location.href = "/monitor/clusterMonitorList";
                });
            }
        });
        show_cluster_node_list( window.cluster.address );
    });

});

$('[href="#nodeManager"]').click(function () {
    $('iframe[name="node-manager"]').attr("src","/node/manager?pluginType=" + window.pluginType  + "&clusterId=" + window.clusterId);
});

$('[href="#clusterManager"]').click(function () {
    show_cluster_node_list( window.cluster.address );
});

function show_cluster_node_list(address){
    smarty.get( "/cluster/detailNodeList?clusterId="+window.clusterId+"&address=" + address, "cluster/cluster_manager_content", "clusterManager", function(){
        // 默认展开
        //$(".expand-all").trigger("click");
    }, true );
}

smarty.register_function('slave_tag', function( params ){
    var slot = params['slot'];
    if( typeof(slot) == "undefined" || slot == null ){
        return "";
    }
    var length = slot.length;
    if( length > 0 ){
        return "hidden";
    }
    return "";
});

smarty.register_function( 'node_status_tag', function( params ){
    var status = params['status'];
    var index = params['index'];
    if( typeof(status) == "undefined" || status == null ){
        return "...";
    }
    if( status.indexOf("fail") > 0 ){
        return "<span class='node-status danger glyphicon glyphicon-remove-circle' aria-hidden='true' data-status='fail' data-status-id='" + index + "'></span>";
    }
    return "<span class='node-status success glyphicon glyphicon-ok-circle' aria-hidden='true' data-status='ok' data-status-id='" + index + "'></span>";
});

smarty.register_function( 'slot_empty', function( params ){
    var slots = params['slots'];
    var nodeid = params['nodeid'];
    var res = "style='display:none'";
    if( typeof(slots) == 'object' ){
        for( var key in slots ){
            if( key == nodeid ){
                return "";
            }
        }
    }
    return res;
});


smarty.register_function("slot_num", function( params ){
    var slots = params['slots'];
    var size = params['size'];
    var index = params['index'];
    if( typeof(slots) == "undefined" || slots == null || size == 0){
        return "<span class='badge background-danger slot' data-status='empty' data-index='" + index + "'> - </span>";
    }
    var avg = Math.round(16384/size);
    var count = 0;
    try{
        for(var i in slots){
            var temp = slots[i];
            if( temp.indexOf("-") == -1 ){
                count++;
            }else{
                var arr = temp.split("-");
                count += arr[1] - arr[0] + 1;
            }
        }
    }catch( e ){
        return "";
    }
    var  subtract = count - avg;
    var res = "";
    if( subtract > 20 ){
        res =  '<span class="badge background-warning slot" data-status="warn" data-index="' + index + '">' + count + ' <i class="icon-long-arrow-up"></i></span>';
    }else if( subtract < -20 ){
        res =  '<span class="badge background-warning slot" data-status="warn" data-index="' + index + '">' + count + ' <i class="icon-long-arrow-down"></i></span>';
    }else{
        res = '<span class="badge slot" data-status="normal"  data-index="' + index + '">' + count + '</span>';
    }
    return res;
});

smarty.register_function("new_node_show", function( params ){
    var slaveList = params['slaveList'];
    if( typeof(slaveList) == "undefined" || slaveList == null || slaveList.length <= 0 ){
        return "new_node_widthout_slot";
    }
    return "";
});


smarty.register_modifier( 'json_string', function( val ) {
    return JSON.stringify( val );
} );

smarty.register_function("slot_num", function( params ){
    var slots = params['slots'];
    var size = params['size'];
    var index = params['index'];
    if( typeof(slots) == "undefined" || slots == null || size == 0){
        return "<span class='badge background-danger slot' data-status='empty' data-index='" + index + "'> - </span>";
    }
    var avg = Math.round(16384/size);
    var count = 0;
    try{
        for(var i in slots){
            var temp = slots[i];
            if( temp.indexOf("-") == -1 ){
                count++;
            }else{
                var arr = temp.split("-");
                count += arr[1] - arr[0] + 1;
            }
        }
    }catch( e ){
        console.log( e );
        return "";
    }
    var  subtract = count - avg;
    var res = "";
    if( subtract > 20 ){
        res =  '<span class="badge background-warning slot" data-status="warn" data-index="' + index + '">' + count + ' <i class="icon-long-arrow-up"></i></span>';
    }else if( subtract < -20 ){
        res =  '<span class="badge background-warning slot" data-status="warn" data-index="' + index + '">' + count + ' <i class="icon-long-arrow-down"></i></span>';
    }else{
        res = '<span class="badge slot" data-status="normal"  data-index="' + index + '">' + count + '</span>';
    }
    return res;
});

smarty.register_function( 'cluster_status', function(params){
    var state = params["state"];
    if( state.toLowerCase() == "ok" ){
        return "<span class='label-success'>Cluster Healthy</span> ";
    }else {
        return "<span class='label-danger'>Cluster Bad</span> ";
    }
});


$(document).on("click", ".expand-all", function(){
    if( $(this).hasClass("glyphicon-triangle-top") ){
        $(this).removeClass("glyphicon-triangle-top");
        $(this).addClass("glyphicon-triangle-bottom");
        window.expand_all = false;
    }else{
        $(this).removeClass("glyphicon-triangle-bottom");
        $(this).addClass("glyphicon-triangle-top");
        window.expand_all = true;
    }

    $("td.expand").each(function(){
        $(this).trigger("click");
    });
    window.expand_all = null;
});
$(document).on("click", "td.expand", function(){
    var isshow = $(this).data("show");
    if(isshow){
        if( window.expand_all  === false ){
            return;
        }
        $(this).data("show",false);
        $(this).html("<span class='glyphicon glyphicon-plus'></span>");
    }else{
        if( window.expand_all  === true ){
            return;
        }
        $(this).data("show",true);
        $(this).html("<span class='glyphicon glyphicon-minus'></span>");
    }
    var index = $(this).data("slave-index");

    var avg_size = 0;
    if( $("#cluster_size").data("size") ){
        avg_size = Math.round( $("#cluster-known-nodes").data("size")/$("#cluster_size").data("size") );
    }

    if( isshow ){ //如果 isshow 那么点击应该隐藏起来
        $("tr.slave[data-index=" + index + "]").addClass("hidden");
        $("#expand-slave-" + index).removeClass("hidden");
        $("[data-status-id=" + index + "]").each(function(){
            if( $(this).data("status") == "fail" ){
                if( ! $("#expand-slave-" + index).hasClass("background-danger") ){
                    $("#expand-slave-" + index).addClass("background-danger");
                }
                return false;
            }
            if( $("tr[data-index=" + index + "]").length != avg_size ){
                if( ! $("#expand-slave-" + index).hasClass("background-warning") ){
                    $("#expand-slave-" + index).addClass("background-warning");
                }
                return false;
            }
        });
    }else{
        $("#expand-slave-" + index).addClass("hidden");
        $("tr.slave[data-index=" + index + "]").removeClass("hidden");
    }
});

$(document).on("click", ".slot-type", function(){
    var type = $(this).data("type");
    // 没有这样的数据就直接返回
    if( !$(".slot-type[data-type='" + type + "']").length ){
        return;
    }
    $(".slot").each(function(){
        if( $(this).data("status") == type ){
            var index = $(this).data("index");
            table_tr_prepend(index);
        }
    });
});

$(document).on("click", ".status-type", function(){
    var type = $(this).data("type");
    if( !$(".status-type[data-type='" + type + "']").length ){
        return;
    }
    $(".node-status").each(function(){
        if( $(this).data("status") == type ){
            var index = $(this).data("status-id");
            table_tr_prepend(index);
        }
    });
});

function table_tr_prepend(index){
    $("tr.slave[data-index=" + index + "]").each(function(){
        $("#nodes-details").prepend( $(this).prop("outerHTML") );
        $(this).remove();
    });
    var master_content = $("tr.master[data-index=" + index + "]").prop("outerHTML");
    $("tr.master[data-index=" + index + "]").remove();
    $("#nodes-details").prepend( master_content );
};

$(document).on("click", "#import-node", function(){
    smarty.open( "cluster/import_node", {}, { title: "Import node",  width:330, height:270},function(){
        $("#import-node-confirm").click(function(){
            var data = sparrow_form.encode( "import-node-form",0 ); //0 表示所有字段都提交， 2 表示有改变的才提交
            if ( sparrow.empty( data ) ) {
                return false;
            }
            layer.closeAll();
            var address =  window.cluster.address;
            var hostArr = address.split(",");
            var tmps = hostArr[0].split(":");
            var masterIp = tmps[0];
            var masterPort = tmps[1];
            importNode(window.clusterId, data.ip, data.port,masterIp,masterPort, function(){

            });
        });
    } );
});

$(document).on("click", "#batch-config", function(){
    smarty.open( "cluster/batch_config", {}, { title: "Batch Config",  width:330, height:270},function(){
        $("#batch-config-confirm").click(function(){
            var data = sparrow_form.encode( "batch-config-form",0 ); //0 表示所有字段都提交， 2 表示有改变的才提交
            if ( sparrow.empty( data ) ){
                return false;
            }
            if(data.configName.trim() == "requirepass") {
                layer.msg("Warn: Changing passwords is not supported.", function(){})
                return false;
            }
            layer.closeAll();
            var address =  window.cluster.address;
            var hostArr = address.split(",");
            var tmps = hostArr[0].split(":");
            var masterIp = tmps[0];
            var masterPort = tmps[1];
            batchConfig(window.clusterId, masterIp, masterPort,data.configName, data.configValue, function(){
            });
        });
    } );
});

$(document).on("click", ".be-master", function(){
    var ip = $(this).data("ip");
    var port = $(this).data("port");
    sparrow_win.confirm('Confirm set the node master',function(){
        beMaster(window.clusterId, ip, port, function(data){
            var result = data.res;
            if(new String(result) == 'true') {
                sparrow_win.msg("be master successfully.");
                delay(function(){show_cluster_node_list( window.cluster.address )});
            } else {
                sparrow_win.msg("be master error.");
            }
        });
     });
});

// delay 3 second
function delay(fun){
    setTimeout(fun, 3000);
}

$(document).on("click", ".move-slot", function(){
    var ip = $(this).data("ip");
    var port = $(this).data("port");
    smarty.open( "cluster/move_slot", {}, { title: "Move Slot",  width:330, height:270},function(){
        $("#move-slot-confirm").click(function(){
            var data = sparrow_form.encode( "move-slot-form",0 );
            if ( sparrow.empty( data ) ){
                return false;
            }
            layer.closeAll();
            var startKey = data["startKey"];
            var endKey = data["endKey"];
            moveSlot(window.clusterId, ip, port, startKey, endKey, function(obj){
                if( obj.code == 0 ){
                    sparrow_win.msg("success!");
                }else{
                    sparrow_win.msg("fail!");
                }
            });
        });
    });
});

$(document).on("click", ".memory-purge", function(){
    var ip = $(this).data("ip");
    var port = $(this).data("port");
    var clusterId = window.clusterId
    memoryPurge(clusterId,ip,port,function(result){
        console.log(result);
    });

});

$(document).on("click", ".forget-node", function(){
    var masterId = $(this).data("nodeid");
    var ip = $(this).data("ip");
    var port = $(this).data("port");
    sparrow_win.confirm("Confirm forget "+ ip + ":" + port +" the node", function(){
        var address =  ip + ":" +  port;
        clusterExistAddress( address, function(obj){
            if( obj.res == true || obj.res == "true" ){
                sparrow_win.msg("the node is in cluster db can not forget");
            }else{
                forgetNode(window.clusterId, ip, port, masterId, function(){
                     sparrow_win.msg("forget node");
                     delay(function(){show_cluster_node_list( window.cluster.address )});
                });
            }
        });
    })
});

// be slave
$(document).on("click", ".be-slave", function(){
    var ip = $(this).data("ip");
    var port = $(this).data("port");
    var address = ip + ":" + port;
    smarty.fopen( "/cluster/detailNodeList?clusterId="+window.clusterId+"&address=" + address, "cluster/be_slave", true, { title: "Select master for move slave " + ip + ":" + port, width:1000, height:580}, function(){
        $(".move-slave-confirm").click(function(){
            layer.closeAll();
            var masterId = $(this).data("nodeid");
            beSlave(window.clusterId, ip, port, masterId, function(data){
                var result = data.res;
                if(new String(result) == 'true') {
                    sparrow_win.msg("move slave successfully.");
                    delay(function(){show_cluster_node_list( window.cluster.address )});
                } else {
                    sparrow_win.msg("move slave error.");
                }
            });
        });
    });
});

$(document).on("click", ".node-info", function(){
    var host = $(this).data("ip") + ":" + $(this).data("port");
    smarty.fopen( "/cluster/getNodeInfo?clusterId="+window.clusterId+"&address="+ host, "cluster/info_format", true, { title: "Info", area: '800px', type: 1, closeBtn: 1, anim: 2, shadeClose: true},  function(obj){
    } );
});

$(document).on("click", ".view-config", function(){
    var host = $(this).data("ip") + ":" + $(this).data("port");
    smarty.fopen( "/cluster/getRedisConfig?clusterId="+window.clusterId+"&address="+ host, "cluster/config_format", true, { title: "Config", area: '800px', type: 1, closeBtn: 1, anim: 2, shadeClose: true},  function(obj){
    } );
});

// delay 3 second
function delay(fun){
    setTimeout(fun, 3000);
}