$(document).on("click", "#add-cluster", function(){
    listGroup(function(obj){
        smarty.open( "cluster/add_cluster_model", obj, { title: "Add New Cluster", width:400, height:420},function(){

        });
    });
});

$(document).on("click", "#save-cluster", function(){
    var cluster = sparrow_form.encode("add-cluster-form", 0);
    if ( sparrow.empty( cluster ) ){
        return false;
    }
    addCluster(cluster, function(obj){
        location.reload();
    });
});


/*delete redis cluster*/
$("body").delegate(".delete-container","click", function(){
    var deleteObj = $(this).parent().parent();
    var clusterId = $(this).attr("data-cluster-id");
    //removeCluster
    sparrow_win.confirm("confirm delete the cluster", function(){
        removeCluster(clusterId, function(obj){
            location.reload();
        });
    });
});
