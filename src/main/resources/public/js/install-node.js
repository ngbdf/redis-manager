$(document).ready(function(){
    window.pluginType = getQueryString("pluginType");
    window.clusterId = getQueryString("clusterId");
    $("#plugin-name").text( firstUpperCase(window.pluginType) );
    init_install_ui(window.clusterId);
});

function init_install_ui(clusterId){
    getImageList(window.pluginType, function(obj){
        listGroup(function(data){
            obj.groups = data.res;
            createClusterStep( obj, clusterId );
        });
    });
}

$(document).on("click", "#start-install-cluster", function(obj){
    var installParam = sparrow_form.encode( "create-cluster-form", 1 );
    if ( !sparrow.empty( installParam )  ){
        installParam.pluginType = window.pluginType;
        if( window.clusterId ){
            installParam.clusterId = clusterId;
        }
        console.log(installParam);
        var param = {
            "pluginType": window.pluginType,
            "req": installParam
        }
        nodePullImage( param, function(obj){
            if( obj.code ==  0 ){
                nodeInstall( param, function(obj){
                    if( obj.code == 0 ){
                        if( window.clusterId ){
                            sparrow_win.msg("success install");
                        }else{
                            sparrow_win.confirm("success, skip to cluster manager?", function(){
                                window.parent.location.href = "/";
                            });
                        }
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
                $("[name='redisPassword']").val(cluster.redisPassword);
                $("[name='redisPassword']").attr("disabled", "disabled");
            });
            getNodeByClusterId(window.pluginType, clusterId, function(obj){
                var node = obj.res;
                //说明这是从我们平台创建的集群
                if(node != null){
                        $("[name='image']").selectpicker("val", node.image);
                        $("[name='image']").attr("disabled","disabled");
                        $('[name="userGroup"]').selectpicker("val", node.userGroup);
                        $('[name="userGroup"]').attr("disabled","disabled");

                        if( node.username ){
                            $('[name="username"]').val(node.username);
                            $('[name="username"]').attr("disabled","disabled");
                        }
                        if( node.password ){
                            $('[name="password"]').val(node.password);
                            $('[name="password"]').attr("disabled","disabled");
                        }
                        if( node.installPath ){
                            $('[name="installPath"]').val( node.installPath );
                            $('[name="installPath"]').attr("disabled","disabled");
                        }
                }else{
                     alert("该集群是由外部导入，扩容请务必保证所有节点信息一致");
                }

            });
        }
        autosize(document.querySelectorAll('textarea'));
        var data = {};
        data.id = window.user.id;
        connect( JSON.stringify(data), "/webSocket/createClusterLog");
    });
}