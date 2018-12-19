$(function(){
    getClusterListInfo(function(obj){
        var clusterListInfo = obj.res;
        $("#cluster-number").text(clusterListInfo.clusterNumber);
        $("#cluster-ok-number").text(clusterListInfo.clusterOkNumber);
        $("#cluster-fail-number").text(clusterListInfo.clusterFailNumber);
        updateWarningCount();
    });

    smarty.get( "/user/listGroup", "monitor/monitor_list", "group-classify", function(){
        // 默认展开第一项
        $('.my-list-panel-header').eq(0).find("a").trigger("click");
    }, true);

})

function updateWarningCount(){
    listClusterByUser(function(obj){
        var clusterIds = [];
        var clusters = obj.res;
        clusters.forEach(function(element){
            clusterIds.push(element.id);
        });
        countTotalAlarm(clusterIds, function(obj){
            $("#cluster-alarm-count").text(obj.res);
        });
    });
}

$(document).on("click", ".collapsed", function(res){
    var domObj = $(this);
    var flag = domObj.attr("aria-expanded");
    var group = domObj.data("group");
    smarty.get( "/cluster/getClusterListByGroup?group=" + group, "monitor/cluster_info_list", "group-id-" + group, function(obj){
        $(".cluster-info-detail-" + group).click();
    }, true );
});

$(document).on("click", ".cluster-info-detail", function(res){
    var clusterId = $(this).data("cluster-id");
    var address = $(this).data("cluster-address");
    getCluster(clusterId , function(obj){
        var data = {};
        data.clusterType = obj.res.clusterType;
        data.redisPassword = obj.res.redisPassword;
        getClusterInfoByAddress(clusterId, address, function(obj){
            data.res = obj.res;
            smarty.html( "monitor/cluster_info", data, "cluster-info-" + clusterId,function () {
                if(data.res.cluster_state != "ok") {
                    $("#cluster-" + clusterId).addClass("cluster-container-fail");
                }
                countWarningLogByClusterId(clusterId, function(obj){
                    var alarmNumber = parseInt(obj.res);
                    if(alarmNumber > 0){
                        $("#cluster-alarm-" + clusterId).text(alarmNumber);
                    } else {
                        $("#cluster-alarm-" + clusterId).remove();
                    }
                    $("#cluster-type").text( $("#cluster-info-" + clusterId ).data("type-target") );
                });
            });
        });
    });

});


$(document).on("click", "#memory-doctor", function(e){
     //阻止子元素事件向上冒泡
     e.stopPropagation();
     var clusterId = $(this).data("cluster-id");
     //弹出一个自定义弹出层
     layer.open({
        type: 1 //Page层类型
       ,area: ['600px', '75%']
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
