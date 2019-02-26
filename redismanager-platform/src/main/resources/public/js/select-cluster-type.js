/**
 * Created by lzz on 2018/5/23.
 */

$(".create-cluster-link").click(function(){
    var url = $(this).data("target");
    var type = $(this).data("type");
    getPluginList(function (obj) {
        if( sparrow.in_array( type, obj.res ) != -1 ){
            window.location.href = url;
        }else{
            layer.alert("can not support " + type, {icon: 6, time: 1000},function(){

            });
        }
    });
});