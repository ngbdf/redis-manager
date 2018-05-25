$(document).ready(function(){
    window.pluginType = getQueryString("pluginType");
    window.clusterId = getQueryString("clusterId");
    $("#plugin-name").text( firstUpperCase(window.pluginType) );
    init_install_ui(window.clusterId);
});

function init_install_ui(clusterId){
    getImageList(window.pluginType, function(obj){
        console.log( obj );
        var userGroup = window.user.userGroup || "";
        var groupList = [];
        if( userGroup != "" ){
            groupList = userGroup.split(",");
        }
        obj.groups = groupList;
        createClusterStep( obj, clusterId );
    });
}

$(document).on("click", "#start-install-cluster", function(obj){
    var installParam = sparrow_form.encode( "create-cluster-form", 1 );
    if ( !sparrow.empty( installParam )  ){
        installParam.pluginType = window.pluginType;
        if( clusterId ){
            installParam.clusterId = clusterId;
        }
        var param = {
            "pluginType": window.pluginType,
            "req": installParam
        }
        console.log( param );
        nodePullImage( param, function(obj){
            if( obj.code ==  0 ){
                nodeInstall( param, function(obj){
                    if( obj.code == 0 ){
                        sparrow_win.confirm("success, skip to cluster manager?", function(){
                            window.location.href = "/cluster/clusterListManager";
                        });
                    }else{
                        sparrow_win.msg("all node install fail");
                    }
                });
            }
        });
    }
});

function  createClusterStep( data, clusterId){
    smarty.html( "plugin/" + window.pluginType + "/create_cluster_step", data, "create-cluster-container",function () {
        $("select").selectpicker();
        if( clusterId ){
            // 如果有传入 clusterId 需要重绘界面
            getCluster(clusterId, function (obj) {
                var cluster = obj.res;
                $("[name='clusterName']").val( cluster.clusterName );
                $("[name='clusterName']").attr("disabled","disabled");
                console.log( cluster );
            });
        }
        autosize(document.querySelectorAll('textarea'));
        var data = {};
        data.id = window.user.id;
        connect( JSON.stringify(data), "/webSocket/createClusterLog");
    });
}