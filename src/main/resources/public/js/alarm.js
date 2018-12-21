$(function(){
    window.clusterId = getQueryString("clusterId");
    getCluster(window.clusterId, function(obj){
        window.cluster = obj.res;
        //如果是4.0的redis，显示memory doctor按钮
        if(obj.res.version4){
          $("#memory-doctor").show();
        }

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
