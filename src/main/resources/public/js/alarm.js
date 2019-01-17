$(function(){

    window.clusterId = getQueryString("clusterId");
    getCluster(window.clusterId, function(obj){
        var cluster = obj.res;

        var clusterId = cluster.id;
        var address = cluster.address;
        getClusterInfoByAddress(clusterId, address, function(obj){
           if(obj.res.redis_mode != "cluster") {
                sparrow_win.confirm("Only support Redis Cluster mode", function(){
                    window.location.href = "/monitor/clusterMonitorList";
                });
           }
        });
        window.cluster = cluster;
        $("#cluster-name").text( obj.res.clusterName );
    });

    $('[href="#alarmHistory"]').trigger("click");

});

$('[href="#alarmRule"]').click(function(){
    reBuildRuleList();
});

$('[href="#alarmHistory"]').click(function(){
    reBuildCaseHistory();
});

function reBuildRuleList(){
   smarty.get( "/alarm/getRuleList?clusterId=" + window.clusterId, "alarm/rule_list_content", "alarm-content", function(){
        $("table").dataTable({});
    }, true );
}


function reBuildCaseHistory(){
   smarty.get( "/alarm/getCaseLogs?clusterId=" + window.clusterId, "alarm/history_list_content", "alarm-content", function(){
        $("table").dataTable({});
    }, true );
}

$(document).on("click","#addAlarmRule",function(){
    var ruleJson = {};
    smarty.open( "alarm/rule_modal",ruleJson , { title: "Add Alarm Rule", width:500, height:300},function(){
        $("#rule-submit").click(function(){
             var rule = sparrow_form.encode( "alarm-rule-form",2 );
             if ( !sparrow.empty( rule )  ){
                 rule.clusterId = window.clusterId;
                 addRule(rule,function(obj){
                      if( obj.code <= 0 ){
                         layer.closeAll();
                         reBuildRuleList();
                      }
                 });
             }
        });
    });
});

$(document).on("click",".btn-rule-delete",function(){
    var id = $(this).data( "id" );
    deleteRule(id,function(){
        reBuildRuleList();
    });
});

$(document).on("click",".btn-history-delete",function(){
    var id = $(this).data( "id" );
    deleteCaseLog(id,function(){
        reBuildCaseHistory();
    });
});

$(document).on("click","#deleteAllHistory",function(){

  var id = window.cluster.id;
  var r = confirm("Are You Sure Delete All Logs ?");
  if (r==true){
     deleteAllLog(id,function(){
            reBuildCaseHistory();
       });
  }
});

