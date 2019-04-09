$(document).ready(function(){
    window.clusterId = getQueryString("clusterId");
    window.endTime = getQueryString("endTime") || getCurrentTime();
    window.startTime = getQueryString("startTime") || (window.endTime - 60 * 60);
    window.host = getQueryString("host") || "all";
    window.date = getQueryString("date") || "minute";
    window.type = getQueryString("type") || "max";
    window.address = "";

    getCluster(window.clusterId , function(obj){
        var cluster = obj.res;
        window.address = cluster.address;
        $("#monitorTitle").html(cluster.clusterName);
        init();
    })

    $(".start-time").flatpickr();
    $(".end-time").flatpickr();
    $(".start-time").val(timestampToDate(window.startTime));
    $(".end-time").val(timestampToDate(window.endTime));
    // set type selector
    $("#dataType").selectpicker("val", window.type);

     $("#instance").text(window.host);
     $("#data-type").text(window.type);

    auth();

});

$(window).resize(function(){
    var chart = $(".chart-sm");
    if(isNotEmpty(chart)){
        $(".chart-sm").each(function(){
                var id = $(this).attr('id');
                var dom = document.getElementById(id)
                if(isNotEmpty(dom) && isNotEmpty(echarts.getInstanceByDom(dom))){
                    echarts.getInstanceByDom(dom).resize();
                }
         });
    }

});

function getCurrentTime(){
    return Date.parse(new Date())/1000;
}

// select time
$(".relative-section ul li").on("click", function(){
    var timeRangeObj = $(this);
    timeRangeObj.addClass("time-selected").siblings().removeClass("time-selected");
    timeRangeObj.parent().siblings().children().removeClass('time-selected');
    var timeRange = parseInt(timeRangeObj.attr("data")); // timeUnit is minute
    var currentTime = getCurrentTime();
    window.endTime = currentTime;
    window.startTime = window.endTime - timeRange * 60;
    if(timeRange >= 60*24*3 ) {
        window.date = "day";
    }else if(timeRange >= 60*12 ) {
        window.date = "hour";
    }else{
        window.date = "minute";
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
        if(timeRange >= 72){
            window.date = "day";
        }else if(timeRange >= 3){
            window.date = "hour";
        }else{
            window.date = "minute";
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
    var host = $(this).attr("title") || "all";
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
        getClusterInfoByAddress(window.clusterId, address, function(obj){
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

//memory doctor
$(document).on("click", "#memory-doctor", function(e){

     var clusterId = window.cluster.id;
     //弹出一个自定义弹出层
     layer.open({
        type: 1 //Page层类型
       ,area: ['600px', '60%']
       ,title: 'Memory Diagnosis'
       ,shade: 0.4 //遮罩透明度
       ,maxmin: true //允许全屏最小化
       ,anim: 1 //0-6的动画形式，-1不开启
       ,content: "<div class='margin-around'><ul id = 'result'></ul></div>"
     });
     var index = layer.load(1, {
       shade: [0.1,'#fff'] //0.1透明度的白色背景
     });

     memoryDoctor(clusterId,function(result){
           layer.close(index);
           var nodes = result.res;

           if(nodes instanceof Array){
             nodes.forEach(function(element){
                 var html = "<li><span class='node'>"+element.addr+"</span>"+element.result+"</li>"
                 $("#result").append(html);
              });
           }else{
             $("#result").append(nodes);
           }

     });
});

// info command
$("#info").on("click", function(){
    var host = window.host;
    if(host != "all" && host != "" && host != null){
        smarty.fopen( "/cluster/getNodeInfo?clusterId="+window.clusterId+"&address="+ host, "cluster/info_format", true, { title: "Info", area: '800px', type: 1, closeBtn: 1, anim: 2, shadeClose: true},  function(obj){
        });
    } else {
        layer.msg("Please select one node");
    }
})

// redis config
$("#config").on("click", function(){
    var host = window.host;
    if(host != "all" && host != "" && host != null){
        smarty.fopen( "/cluster/getRedisConfig?clusterId="+window.clusterId+"&address="+ host, "cluster/config_format", true, { title: "Config", area: '800px', type: 1, closeBtn: 1, anim: 2, shadeClose: true},  function(obj){
        });
    } else {
        layer.msg("Please select one node");
    }
})

function reloadMonitor(){
    window.location.href = "/monitor/clusterMonitor?clusterId="+window.clusterId +"&startTime=" + window.startTime + "&endTime="+window.endTime + "&host=" + window.host + "&type=" + window.type + "&date=" + window.date;
}

function init(){
    // cluster info for state
    getClusterInfoByAddress(window.clusterId, window.address, function(obj){
        var clusterInfo = obj.res;
        var state = clusterInfo.cluster_state;
        if(state == "ok"){
            $("#cluster-state").html('<span class="label-success cluster-info">Cluster Healthy</span>');
        } else {
            $("#cluster-state").html('<span class="label-danger cluster-info">Cluster Bad</span>');
        }
        $("#cluster-nodes-number").text(clusterInfo.cluster_known_nodes);
        $("#master-number").text(clusterInfo.cluster_size);
    })
    monitorGetMaxField(window.clusterId ,window.startTime,window.endTime,"response_time", 2,function(obj){
        var resList = obj.res;
        var topListStr = "";
        for( var i = 0; i < resList.length; i++ ){
            topListStr += '<p class="top-item">Top-' + (i + 1) + ': <span class="top_request_monitor" title="'+ resList[i]["host"] + '" >&nbsp;' +  resList[i]["response_time"] + "&nbsp; ms"+ '&nbsp;</span></p>';
        }
        $("#top-response-time").append( topListStr );
    });
    monitorGetAvgField(window.clusterId ,window.startTime,window.endTime,window.host, "response_time",function(obj){
        $("#avg-response").html( obj.res );
    });

    monitorGetDbSize(window.clusterId, window.address,function(obj){
        if( obj.res ){
            $("#all-key").html( Math.round(obj.res) );
        }
    });
    monitorGetMaxField(window.clusterId ,window.startTime,window.endTime,"total_keys", 2,function(obj){
        var data = obj.res[0];
        if(isNotEmpty(data)) {
            $("#max-all-key").attr("title", data.host);
            $("#max-all-key").html(data.total_keys );
        }

    });
    monitorGetMinField(window.clusterId ,window.startTime,window.endTime,"total_keys", 2,function(obj){
        var data = obj.res[0];
        if(isNotEmpty(data)) {
            $("#min-all-key").attr("title", data.host)
            $("#min-all-key").html(data.total_keys );
        }

    });

    monitorGetMaxField(window.clusterId ,window.startTime,window.endTime,"connected_clients", 2,function(obj){
        var resList = obj.res;
        var topListStr = "";
        for( var i = 0; i < resList.length; i++ ){
            topListStr += '<p class="top-item">Top-' + (i + 1) + ': <span class="top_request_monitor" title="'+ resList[i]["host"] + '" >&nbsp;' + resList[i]["connected_clients"]  + '</span></p>';
        }
        $("#top-avg-connection").append( topListStr );
    });

    monitorGetAvgField(window.clusterId ,window.startTime,window.endTime,window.host, "connected_clients",function(obj){
        if( obj.res ){
            $("#avg-connection").html( Math.round(obj.res) );
        }
    });

    monitorGetMaxField(window.clusterId ,window.startTime,window.endTime,"instantaneous_ops_per_sec", 2,function(obj){
        var resList = obj.res;
        var topListStr = "";
        for( var i = 0; i < resList.length; i++ ){
            topListStr += '<p class="top-item">Top-' + (i + 1) + ':  <span class="top_request_monitor" title="'+ resList[i]["host"] + '" >&nbsp;' + resList[i]["instantaneous_ops_per_sec"]  + '</span></p>';
        }
        $("#top-avg-instantaneous").append( topListStr );
    });

    monitorGetAvgField(window.clusterId ,window.startTime,window.endTime,window.host, "instantaneous_ops_per_sec",function(obj){
        if( obj.res ){
            $("#avg-instantaneous").html( Math.round(obj.res) );
        }
    });

   monitorGetGroupNodeInfo(window.clusterId ,window.startTime,window.endTime,window.host,window.type,window.date,function(obj){
        var data = obj.res[0];
        if(isNotEmpty(data)){
                var storageUnit = calStorageUnit(data.usedMemory);
                var numberUnit = calNumberUnit(data.expires);
                var usefulData = refactor(obj.res,window.date,storageUnit,numberUnit);
                clearChart_invalidatedData(usefulData);
                buildChart("charts-cpu","平均每秒使用CPU时间","date","usedCpuUser",usefulData,"CPU usage","  /s");
                buildChart("charts-memory","内存占用","date","usedMemory",usefulData,"memory usage", storageUnit);
                buildChart("charts-client","客户端连接数","date","connectedClients",usefulData,"client connections"," ");
                buildChart("charts-ops","每秒指令数(instantaneous_ops_per_sec )","date","instantaneousOpsPerSec",usefulData,"command  /sec"," ");
                buildChart("charts-commands","每秒命令数(total_commands_processed)","date","totalCommandsProcessed",usefulData,"command  /sec  "," ");
                buildChart("charts-Keyspace-expires","有TTL的key总数","date","expires",usefulData,"keys with ttl",numberUnit);
                buildChart("charts-hitRate","命中率","date","keyspaceHitRate",usefulData,"hitRate_avg"," ");
                buildChart("charts-memFragmentationRatio","节点内存碎片率","date","memFragmentationRatio",usefulData," mem_fragmentation_ratio"," ");
        }
    });

    getCluster(window.clusterId , function(obj){
            window.cluster = obj.res;
            //如果是4.0的redis，显示memory doctor按钮
            if(obj.res.version4){
              $("#memory-doctor").show();
            }

            $("#clusterName").html(obj.res.clusterName);
    });

    // set node options
    var address = window.address;
    nodeList(window.clusterId, address, function(nodeObj){
        var nodeList = nodeObj.res;
        window.nodeList = nodeList;
        var options = '<option>all</option>';
        for(var i = 0, len = nodeList.length; i < len; i++){
            var node = nodeList[i];
            var host = node.ip + ":" + node.port;
            var role = node.role;
            options += '<option data-subtext="'+role+'">'+host+'</option>';
        }
        var nodeListObj = $("#nodeList");
        nodeListObj.append(options);
        nodeListObj.val(window.host);
        nodeListObj.selectpicker("refresh");
        var logNodeListObj = $("#logNodeList");
        logNodeListObj.append(options);
        logNodeListObj.selectpicker("refresh");
    });
}

function  calStorageUnit(numberValue){
    var sizeUnit = '';
    if( numberValue < 1024 ){
        sizeUnit = 'B';
    }else if( numberValue /1024 <1024 ){
        sizeUnit = 'KB';
    }else if( numberValue /1024/1024 <1024 ){
        sizeUnit = 'MB';
    }else{
        sizeUnit = 'GB';
    }
    return sizeUnit;
}

function  calNumberUnit(numberValue){
    var numberUnit = '';
    if( numberValue/10000 < 10 ){
        numberUnit = '';
    }else {
        numberUnit = 'W';
    }
    return numberUnit;
}

function refactor(originData,timeUnit,storageUnit,numberUnit){
    var maxIndex = originData.length;
    for(var index=maxIndex-1;index>0;index--){
        var thisRecord = originData[index];
        var fontRecord = originData[index-1];

        // 做差值 获得有用的 统计值
        thisRecord.totalConnectionsReceived = (thisRecord.totalConnectionsReceived - fontRecord.totalConnectionsReceived).toFixed(2);
        thisRecord.totalCommandsProcessed = (thisRecord.totalCommandsProcessed - fontRecord.totalCommandsProcessed).toFixed(2);
        thisRecord.totalNetInputBytes = (thisRecord.totalNetInputBytes - fontRecord.totalNetInputBytes).toFixed(2);
        thisRecord.totalNetOutputBytes = (thisRecord.totalNetOutputBytes - fontRecord.totalNetOutputBytes).toFixed(2);
        thisRecord.keyspaceHits = (thisRecord.keyspaceHits - fontRecord.keyspaceHits).toFixed(2);
        thisRecord.keyspaceMisses = (thisRecord.keyspaceMisses - fontRecord.keyspaceMisses).toFixed(2);
        thisRecord.usedCpuSys = (thisRecord.usedCpuSys - fontRecord.usedCpuSys).toFixed(2);
        thisRecord.usedCpuUser = (thisRecord.usedCpuUser - fontRecord.usedCpuUser).toFixed(2);
        thisRecord.usedCpuSysChildren = (thisRecord.usedCpuSysChildren - fontRecord.usedCpuSysChildren).toFixed(2);
        thisRecord.usedCpuUserChildren = (thisRecord.usedCpuUserChildren - fontRecord.usedCpuUserChildren).toFixed(2);

        // 时间单位换算 获得有用的 统计值
        if(timeUnit == 'minute'){
            thisRecord.totalCommandsProcessed = ( thisRecord.totalCommandsProcessed /60 ).toFixed(2);
            thisRecord.usedCpuUser = ( thisRecord.usedCpuUser /60 ).toFixed(4);
        }else if(timeUnit == 'hour'){
            thisRecord.totalCommandsProcessed = ( thisRecord.totalCommandsProcessed /60 /60 ).toFixed(2);
            thisRecord.usedCpuUser = ( thisRecord.usedCpuUser /60/60 ).toFixed(4);
        }else if(timeUnit == 'day'){
            thisRecord.totalCommandsProcessed = ( thisRecord.totalCommandsProcessed /60 /60 /24 ).toFixed(2);
            thisRecord.usedCpuUser = ( thisRecord.usedCpuUser /60/60/24 ).toFixed(4);
        }

        // 计算 获得有用的 统计值
        thisRecord.keyspaceHitRate = (Number(thisRecord.keyspaceHits) / ( Number(thisRecord.keyspaceHits) + Number(thisRecord.keyspaceMisses))).toFixed(2);
        if(isNaN(thisRecord.keyspaceHitRate) || thisRecord.keyspaceHitRate==Infinity){
            thisRecord.keyspaceHitRate = 0.0;
        }

        // 存储单位换算 获得有用的 统计值
        if (storageUnit == 'KB'){
            thisRecord.usedMemory = (thisRecord.usedMemory/1024).toFixed(2);
            thisRecord.usedMemoryRss = (thisRecord.usedMemoryRss/1024).toFixed(2);
            thisRecord.usedMemoryPeak = (thisRecord.usedMemoryPeak/1024).toFixed(2);
        }else if (storageUnit == 'MB'){
            thisRecord.usedMemory = (thisRecord.usedMemory /1024/1024).toFixed(2);
            thisRecord.usedMemoryRss = (thisRecord.usedMemoryRss/1024/1024).toFixed(2);
            thisRecord.usedMemoryPeak = (thisRecord.usedMemoryPeak/1024/1024).toFixed(2);
        }else if (storageUnit == 'GB'){
            thisRecord.usedMemory = (thisRecord.usedMemory/1024/1024/1024).toFixed(2);
            thisRecord.usedMemoryRss = (thisRecord.usedMemoryRss/1024/1024/1024).toFixed(2);
            thisRecord.usedMemoryPeak = (thisRecord.usedMemoryPeak/1024/1024/1024).toFixed(2);
        }else {

        }

        if(numberUnit == 'W'){
            thisRecord.expires = (thisRecord.expires/10000).toFixed(2);
        }

        var dateStr_hhh = thisRecord.date.toString();
        // 时间字符串转化
        var splitIndex_date = dateStr_hhh.indexOf(":");
        if(splitIndex_date != -1){
            var str1 = dateStr_hhh.substring(0,splitIndex_date);
            var str2 = dateStr_hhh.substring(splitIndex_date+1,dateStr_hhh.length);
            if(str1.length  == 8 ){  //20180505:5
                if(str2.length  == 1 ){
                    str2 = '0'+str2;
                }
                dateStr_hhh = str1.substring(4,6)+'/'+str1.substring(6,8) + ' ' + str2 +':00';
            }else {
                if(str1.length ==1){
                    str1 = '0'+str1;
                }
                if(str2.length ==1){
                    str2 = '0'+str2;
                }
                dateStr_hhh = str1+':'+str2;
            }
            thisRecord.date = dateStr_hhh;
        }else{
            thisRecord.date = dateStr_hhh.substring(4,6)+'/'+dateStr_hhh.substring(6,8) ;
        }

    }
    // 清除没有做减法的数据
    originData.shift();
    return originData;
}

// 清除不合理的数据
function clearChart_invalidatedData(originData){
    var clearChart_commands = false;
    var clearChart_cpu = false;
    var clearChart_hitRate = false;
    originData.forEach(function(thisRecord){
        if(thisRecord.totalCommandsProcessed<0){
            clearChart_commands = true;
        }
        if(thisRecord.usedCpuUser<0){
            clearChart_cpu = true;
        }
        if(thisRecord.keyspaceHitRate>1 || thisRecord.keyspaceHitRate<0 ){
            clearChart_hitRate = true;
        }
    });
    if(clearChart_commands){
        originData.forEach(function(thisRecord){
            thisRecord.totalCommandsProcessed = "clear";
        });
        console.log("have some invalidate data, clear chart-totalCommandsProcessed");
    }
    if(clearChart_cpu){
        originData.forEach(function(thisRecord){
            thisRecord.usedCpuUser = "clear";
        });
        console.log("have some invalidate data, clear chart-usedCpuUser");
    }
    if(clearChart_hitRate){
        originData.forEach(function(thisRecord){
            thisRecord.keyspaceHitRate = "clear";
        });
        console.log("have some invalidate data, clear chart-keyspaceHitRate");
    }
}


function buildChart(webElementId,titleText,xAxisFieldName,yAxisFieldName,chartData,legendText,yAxisText){

    var myChart = echarts.init(document.getElementById(webElementId));
    myChart.showLoading({
        text: '数据正在努力加载...',
        textStyle: { fontSize : 30 , color: '#444' },
        effectOption: {backgroundColor: 'rgba(0, 0, 0, 0)'}
    });
    if(chartData == "" || chartData == "undefined"){
          return;
        }
    var option_bar = {
        title: {
            text: titleText,
            subtext: ''
        },
        color: [
            '#7cb5ec','#90ed7d','#FE8463','#9BCA63','#FAD860','#60C0DD','#0084C6'
        ],
        xAxis: {
            type: 'category',
            name: ''
        },
        yAxis: {
            type: 'value',
            name: yAxisText,
            offset: '0',
            position: 'left',
            scale: true,
            nameTextStyle: {
                fontSize: 12,
                align:'right'
            }
        },
        tooltip:{
            show: true,
            trigger: 'axis',
            triggerOn: 'mousemove|click',
//            formatter : function(name){
//                return legendText+': {b}';
//            }
        },
        grid:{
            bottom: 40,
            left: 80,
            top: 60,
        },
        legend:{
            show: true,
            top: 'bottom',
            selectedMode: false,
            formatter: function(name){
                return legendText;
            }
        },
        dataset:{
            source: chartData
        },
        series: [{
            type: 'line',
            dimensions: [xAxisFieldName, yAxisFieldName],
            areaStyle: {normal: {}},
        }]
    };
    myChart.setOption(option_bar);
    myChart.hideLoading();
}



// slow log
$("#logNodeList").on('changed.bs.select', function (e) {
    slowLog();
})
// slow log function
function slowLog(){
    $("#slow-log-table").empty();
    var logParam = {};
    var logNode = $("#logNodeList").selectpicker("val");
    if(logNode == "all"){
        logParam.hostList = window.nodeList;
    } else {
        var ipAndPort = logNode.split(":");
        logParam.hostList = [{"ip":ipAndPort[0], "port": parseInt(ipAndPort[1])}];
    }
    logParam.clusterId = window.clusterId;
    var tableStr = '<table class="table table-bordered scrollbar">';
    tableStr += "<thead><tr><th>Host</th><th>Slow Date</th><th>Run Time</th><th>Type</th><th>command</th></tr></thead><tbody>"
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
        tableStr += tr;
        tableStr += "</tbody></table>";
        $("#slow-log-table").append( tableStr );
        $("#slow-log-table>table").dataTable({
            pageLength:15,
            lengthMenu: [15, 30, 50, 100, 200, 300 ],
            order: [[ 1, 'asc' ]]
        });
    });
}


function auth() {
    var log ="                                                                                                                                                                        \n"+
             " #@@@@@@@,                             .@:   @@'              ,@;             '@.                                                                                       \n"+
             " #@@@@@@@@#                            .@:   @@#              ,@@             @@.                                                                                       \n"+
             " #@     `@@:                           .@:                    ,@@,           ,@@.                                                                                       \n"+
             " #@      ,@@                           .@:                    ,@@@           @@@.                                                                                       \n"+
             " #@       @@                           .@:                    ,@@@`         `@@@.                                                                                       \n"+
             " #@       #@        @@@           @@@` .@:           .@@@     ,@'@#         #@'@.       +@@+           @@@.         ,@@@          ,@@@            @@@           @@'     \n"+
             " #@       @@      @@@@@@@       @@@@@@@.@:   :@`    @@@@@@;   ,@,@@         @@:@.     @@@@@@@.    `@:#@@@@@@      @@@@@@@+      .@@@@@@@#@      @@@@@@@     @@:@@@@     \n"+
             " #@ .     @@     @@+   @@#     @@@   '@@@:   :@`   @@#   @;   ,@,;@;       '@::@.    '@@   ;@@    `@@@@   @@#    `@@`  `@@`    `@@'   @@@@     @@+   @@#    @@@@        \n"+
             " #@      #@;    #@:     @@`   #@+     ,@@:   :@`  `@@         ,@, @@       @@ :@.    ;      #@.   `@@+     @@    `:     ,@#    @@,     #@@    #@,     @@`   @@@         \n"+
             " #@     @@@     @@      ,@'   @@       @@:   :@`  `@+         ,@, #@,     ,@' :@.           ,@;   `@@      +@`           @@   ,@#       @@    @#      :@;   @@'         \n"+
             " #@@@@@@@#     :@:       @#  :@'       ;@:   :@`   @@         ,@,  @#     @@  :@.           `@'   `@#      :@,           @@   #@`       @@   :@,       @#   @@`         \n"+
             " #@@@@@@;      #@`       @#  +@.       .@:   :@`   #@@        ,@,  @@`   `@#  :@.         @@@@'   `@:      .@,        @@@@@   @@        #@   #@`       @#   @@          \n"+
             " #@    @@`     #@@@@@@@@@@@  #@        .@:   :@`    @@@#      ,@,  ,@'   +@`  :@.     #@@@@@@@'   `@:      .@,    .@@@@@@@@   @@        #@   @@@@@@@@@@@@   @@          \n"+
             " #@    .@#     #@            #@        .@:   :@`     .@@@.    ,@,   @@   @@   :@.    @@@:   `@'   `@:      .@,   '@@#    @@   @@        #@   @@             @@          \n"+
             " #@     @@`    #@`           #@`       .@:   :@`       :@@.   ,@,   ;@: ;@:   :@.   ;@+     `@'   `@:      .@,   @@      @@   @@        #@   #@`            @@          \n"+
             " #@     :@+    ;@:           '@:       '@:   :@`        ,@#   ,@,    @@ @@    :@.   #@      ,@'   `@:      .@,  ,@:      @@   #@        @@   '@:            @@          \n"+
             " #@      @@     @@           `@@       @@:   :@`         @@   ,@,    +@.@+    :@.   #@      #@'   `@:      .@,  :@,     :@@   '@'      `@@   `@#            @@          \n"+
             " #@      +@;    @@:      ;    @@,     +@@:   :@`         @#   ,@,    `@@@     :@.   #@.    :@@'   `@:      .@,  ,@#    `@@@    @@`     @@@    @@,      ;    @@          \n"+
             " #@       @@    `@@+   ,@@    .@@;   @@@@:   :@`  .@,   @@;   ,@,     #@#     :@.   ,@@`  +@@@'   `@:      .@,   @@;  `@@@@    #@@.   @@@@    `@@+   ,@@    @@          \n"+
             " #@       #@,    ,@@@@@@@;     ,@@@@@@@.@:   :@`  .@@@@@@@    ,@,     ,@`     :@.    @@@@@@@`@'   `@:      .@,   ,@@@@@@ @@     #@@@@@@;#@     ,@@@@@@@;    @@          \n"+
             "                   ,@@@          '@@#               +@@@                               @@@                         #@@.           @@@'  #@       ,@@@                   \n"+
             "                                                                                                                                        @@                              \n"+
             "                                                                                                                                        @#                              \n"+
             "                                                                                                                                       #@;                              \n"+
             "                                                                                                                               @`     #@@                               \n"+
             "                                                                                                                               @@@@@@@@@                                \n"+
             "                                                                                                                                '@@@@@;                                 \n"+
             "                                                                                                                                                                        \n"+
             "                                                                                                                                                 @AU. GL LF JZ .        \n"+
             "                                                                                                                                                 www.newegg.com         \n"+
             "                                                                                                                                                                        \n";

    console.log(log);
}
