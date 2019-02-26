$(function(){
    listCluster(function(obj){
        var res = obj.res;
        for(var i = 0; i < res.length; i++){
            $("#from-cluster-li").append("<li><a class='cluster-li-link' href='#' data-target='from-cluster' data-detail='" + JSON.stringify(res[i]) + "'>" + res[i]["clusterName"] + "</a></li>");
        }
        for(var i = 0; i < res.length; i++){
            $("#to-cluster-li").append("<li><a class='cluster-li-link' href='#' data-target='to-cluster' data-detail='" + JSON.stringify(res[i]) + "'>" + res[i]["clusterName"] + "</a></li>");
        }
    });
    // 定时刷新导入列表
    setInterval(showImportCountTable, 1000);
});

$(document).on("click", ".cluster-li-link", function(){
    var detail = $(this).data("detail");
    var target = $(this).data("target");
    $("#" + target).text( detail["clusterName"] );
    $("#" + target).data("detail", detail);
});

$("#syn-cluster-button").click(function(){
    var fromDetail = $("#from-cluster").data("detail");
    var fromCluster = $("#from-cluster").text();
    var targetAddress = $("#to-cluster").data("detail");
    var targetCluster = $("#to-cluster").text();
    var keyFormat = $("input[name='format-key']").val();
    if( !fromDetail || !targetAddress ){
        sparrow_win.alert("please select cluster");
        return;
    }

    if( !keyFormat ){
        sparrow_win.alert("please input key format");
        return;
    }

    if( fromCluster == targetCluster ){
        sparrow_win.alert("sorry you from cluster equal target cluster");
        return;
    }
    importDataToCluster(window.clusterId, fromDetail["address"],targetAddress["address"], keyFormat,function(obj){
    });
});

function showImportCountTable(){
    smarty.get( "/cluster/getImportCountList", "cluster/import_cluster_count_table", "cluster-import-count-table", function(){
    }, true );
}
