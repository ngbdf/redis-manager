$(function(){
    smarty.get( "/user/listGroup", "monitor/monitor_list", "group-classify", function(){
       // 默认展开第一项
       $('.my-list-panel-header').eq(0).find("a").trigger("click");
    }, true );

    getClusterListInfo(function(obj){
        var clusterListInfo = obj.res;
        $("#cluster-number").text(clusterListInfo.clusterNumber);
        $("#cluster-ok-number").text(clusterListInfo.clusterOkNumber);
        $("#cluster-fail-number").text(clusterListInfo.clusterFailNumber);
        updateWarningCount();
    })

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

$(document).on("click", ".list-active", function(res){
    var group = $(this).data("group");
    var isGetData = $(this).attr("aria-expanded");
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
        getClusterInfoByAddress(address, function(obj){
            data.res = obj.res;
            console.log(data);
            smarty.html( "monitor/cluster_info", data, "cluster-info-" + clusterId,function () {
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


