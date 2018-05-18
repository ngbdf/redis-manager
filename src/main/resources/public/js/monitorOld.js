$(document).ready(function(){
    window.clusterId = getQueryString("clusterId");
    window.endTime = getQueryString("endTime") || getCurrentTime();
    window.startTime = getQueryString("startTime") || (window.endTime - 60 * 60);
    window.host = getQueryString("host") || "all";
    window.date = getQueryString("date") || "minute";
    window.type = getQueryString("type") || "max";
    window.address = ""
    getCluster(window.clusterId, function(obj){
        window.address = obj.res.address;
        console.log(window.address);
        init();
    })
    $('th[data-field="responseTime"]').trigger("click");
    // init time selector
    $(".start-time").flatpickr();
    $(".end-time").flatpickr();
    $(".start-time").val(timestampToDate(window.startTime));
    $(".end-time").val(timestampToDate(window.endTime));

    // set type selector
    $("#dataType").selectpicker("val", window.type);

});

function getCurrentTime(){
    return Date.parse(new Date())/1000;
}

// select time
$(".relative-section ul li").on("click", function(){
    var timeRangeObj = $(this);
    timeRangeObj.addClass("time-selected").siblings().removeClass("time-selected");
    timeRangeObj.parent().siblings().children().removeClass('time-selected');
    var timeRange = parseInt(timeRangeObj.attr("data"));
    var currentTime = getCurrentTime();
    window.endTime = currentTime;
    window.startTime = window.endTime - timeRange * 60;
    if(timeRange > 720) {
        window.date = "hour";
    }
    reloadMonitor();
})

// query by time selector
$(".query").on("click", function(){
    var startStr = $('#startTime').val().trim();
    var endStr = $('#endTime').val().trim();
    if(startStr == '' || endStr == ''){
        layer.msg("datetime can't be null", function(){});
        return;
    }
    var startTime = Date.parse(new Date(startStr.replace(/-/g, '/')));
    var endTime = Date.parse(new Date(endStr.replace(/-/g, '/')));
    if(startTime < endTime){
        var timeRange = (endTime - startTime) / 1000 / 60 / 60;
        if(timeRange > 12){
            window.date = "hour";
        }
        window.startTime = startTime / 1000;
        window.endTime = endTime / 1000;
        reloadMonitor();
    } else {
        layer.msg("End time needs to be greater than start time");
        return;
    }
})

$("#nodeList").on('changed.bs.select', function (e){
    var host = $(this).selectpicker('val');
    window.host = host;
    reloadMonitor();
})

$("body").delegate(".top_request_monitor", "click", function(){
    var host = $(this).text();
    window.host = host;
    reloadMonitor();
});


$('#dataType').on('changed.bs.select', function (e) {
    var type = $(this).selectpicker('val').toLowerCase();
    window.type = type;
    reloadMonitor();
});

// cluster info command
$(".cluster-info").on("click", function(){
    getCluster(window.clusterId, function(obj){
        var address = obj.res.address;
        getClusterInfoByAddress(address, function(obj){
            layer.open({
                title: 'Cluster Info',
                type: 1,
                area: '500px;',
                skin: 'layui-layer-demo', //样式类名
                closeBtn: 1, //显示关闭按钮
                anim: 2,
                shadeClose: true, //开启遮罩关闭
                content: '<pre style="padding: 20px; height:auto; border: none;">'+ syntaxHighlight(obj.res) +'</pre>'
            });
        })
    })
})

// info command
$("#info").on("click", function(){
    var host = window.host;
    if(host != "all" && host != "" && host != null){
        getNodeInfo(host, function(obj){
            var info = obj.res;
            layer.open({
                title: 'Info',
                type: 1,
                area: '800px',
                skin: 'layui-layer-demo', //样式类名
                closeBtn: 1, //显示关闭按钮
                anim: 2,
                shadeClose: true, //开启遮罩关闭
                content: '<pre style="padding: 20px; border: none;">'+ syntaxHighlightRedisResult( obj.res ) +'</pre>'
            });
        })
    } else {
        layer.msg("Please select one node");
    }
})

// redis config
$("#config").on("click", function(){
    var host = window.host;
    if(host != "all" && host != "" && host != null){
        smarty.fopen( "/cluster/getRedisConfig?address="+window.address, "cluster/redis_format", true, { title: "Config", area: '800px', type: 1, closeBtn: 1, anim: 2, shadeClose: true},  function(obj){
        console.log(obj)
        } );
    } else {
        layer.msg("Please select one node");
    }
})

smarty.register_function( 'format_redis_result', function( params ){
    var content = params['content'];
    return syntaxHighlightRedisResult( content );
});

function reloadMonitor(){
    window.location.href = "/monitor/manager?clusterId="+window.clusterId+"&startTime=" + window.startTime + "&endTime="+window.endTime + "&host=" + window.host + "&type=" + window.type + "&date=" + window.date;
}

function init(){
    monitorGetMaxField(window.clusterId,window.startTime,window.endTime,"response_time", 2,function(obj){
        var resList = obj.res;
        var topListStr = "";
        for( var i = 0; i < resList.length; i++ ){
            topListStr += '<p class="top-item">Top-' + (i + 1) + ': [<span class="top_request_monitor">' + resList[i]["host"] + '</span>] <span class="danger">' + resList[i]["response_time"] + '</span><span>/ms</span></p>';
        }
        $("#top-response-time").append( topListStr );
    });
    monitorGetAvgField(window.clusterId,window.startTime,window.endTime,window.host, "response_time",function(obj){
        $("#avg-response").html( obj.res );
    });

    monitorGetAvgField(window.clusterId,window.startTime,window.endTime,window.host, "total_keys",function(obj){
        $("#all-key").html( obj.res );
    });
    monitorGetMaxField(window.clusterId,window.startTime,window.endTime,"total_keys", 2,function(obj){
        $("#max-all-host").html( obj.res.host );
        $("#max-all-key").html( obj.res.total_keys );
    });
    monitorGetMinField(window.clusterId,window.startTime,window.endTime,"total_keys", 2,function(obj){
        $("#min-all-host").html( obj.res.host );
        $("#min-all-key").html( obj.res.total_keys );
    });

    monitorGetMaxField(window.clusterId,window.startTime,window.endTime,"connected_clients", 2,function(obj){
        var resList = obj.res;
        var topListStr = "";
        for( var i = 0; i < resList.length; i++ ){
            topListStr += '<p class="top-item">Top-' + (i + 1) + ': [<span class="top_request_monitor">' + resList[i]["host"] + '</span>] <span class="danger">' + resList[i]["connected_clients"] + '</span></p>';
        }
        $("#top-avg-connection").append( topListStr );
    });

    monitorGetAvgField(window.clusterId,window.startTime,window.endTime,window.host, "connected_clients",function(obj){
        $("#avg-connection").html( obj.res );
    });

    monitorGetMaxField(window.clusterId,window.startTime,window.endTime,"instantaneous_ops_per_sec", 2,function(obj){
        var resList = obj.res;
        var topListStr = "";
        for( var i = 0; i < resList.length; i++ ){
            topListStr += '<p class="top-item">Top-' + (i + 1) + ': [<span class="top_request_monitor">' + resList[i]["host"] + '</span>] <span class="danger">' + resList[i]["instantaneous_ops_per_sec"] + '</span></p>';
        }
        $("#top-avg-instantaneous").append( topListStr );
    });

    monitorGetAvgField(window.clusterId,window.startTime,window.endTime,window.host, "instantaneous_ops_per_sec",function(obj){
        $("#avg-instantaneous").html( obj.res );
    });

    getCluster(window.clusterId, function(obj){
        $("#clusterName").html(obj.res.clusterName);
    });

    // set node options
    var address = window.address
    nodeList(address, function(nodeObj){
        var nodeList = nodeObj.res
        window.nodeList = nodeList;
        var options = '<option>all</option>';
        for(var i = 0, len = nodeList.length; i < len; i++){
            var node = nodeList[i];
            var host = node.ip + ":" + node.port;
            var role = node.role;
            options += '<option data-subtext="'+role+'">'+host+'</option>';
        }
        var nodeListObj = $("#nodeList")
        nodeListObj.append(options);
        nodeListObj.val(window.host);
        nodeListObj.selectpicker("refresh");
        var logNodeListObj = $("#logNodeList");
        logNodeListObj.append(options);
        logNodeListObj.selectpicker("refresh");
    });
}

$("#field-title > th").click(function () {
    var field = $(this).data("field");
    if( field != "date" ){
        $("#field-title > th").removeClass("selected");
        $(this).addClass("selected");
        ajax.async_get("/monitor/getGroupNodeInfo?clusterId=" + window.clusterId + "&startTime="+ window.startTime +"&endTime=" + window.endTime + "&host=" + window.host + "&type=" + window.type + "&date=" + window.date, function(obj){
            makeCharts("light", "#FFFFFF", field, obj.res);
            console.log(obj.res)
            smarty.html( "monitor/node_info_table", obj, "node-info-table",function () {
                console.log("html ...");
            });
        });
    }
});

// slow log
$("#logNodeList").on('changed.bs.select', function (e) {
    slowLog();
})
// slow log function
function slowLog(){
    $("#slow-log-table>tbody").empty();
    var logParam = {};
    var logNode = $("#logNodeList").selectpicker("val");
    console.log(logNode)
    if(logNode == "all"){
        logParam.hostList = window.nodeList;
    } else {
        var ipAndPort = logNode.split(":");
        logParam.hostList = [{"ip":ipAndPort[0], "port": parseInt(ipAndPort[1])}];
    }
    monitorSlowLogs(logParam,function(obj){
        var items = obj.res;
        var tr = "";
        for(var index in items){
            tr += "<tr>";
            tr += "<td class='one_slow_log info-hover'>" + items[index].host + "</td>";
            tr += "<td>" + items[index].showDate + "</td>";
            tr += "<td>" + items[index].runTime + "</td>";
            tr += "<td>" + items[index].type + "</td>";
            tr += "<td>" + items[index].command + "</td>";
            tr += "</tr>";
        }
        $("#slow-log-table>tbody").append( tr );
        $("#slow-log-table").dataTable({
            pageLength:15,
            lengthMenu: [15, 30, 50, 100, 200, 300 ],
            order: [[ 1, 'asc' ]]
        });
    });
}

function makeCharts(theme, bgColor, field, char_data_table) {
    var len = char_data_table.length;
    var chart1;
    if (chart1) {
        chart1.clear();
    }
    // background
    if (document.body) {
        document.body.style.backgroundColor = bgColor;
    }

    // column chart
    chart1 = AmCharts.makeChart("node-info-chart", {
        type: "serial",
        theme: theme,
        dataProvider: char_data_table,
        categoryField: "date",
        startDuration: 1,
        categoryAxis: {
            gridPosition: "start"
        },
        valueAxes: [{
            title: "Metric"
        }],
        graphs: [{
            type: "column",
            title: field,
            valueField: field,
            lineAlpha: 0,
            fillAlphas: 0.8,
            balloonText: "[[title]] in [[category]]  <b>[[value]]</b>"
        }],
        colors:	["#3fa7dc"]
    });
}

