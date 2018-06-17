smarty.get( "/cluster/listCluster?group=admin", "cluster/cluster_list_content", "cluster-list-content", function(){
       /* console.log("get...");*/
}, true );

smarty.register_function( 'cluster_state', function( params ){
    var address = params['address'];
    var id = params["id"];
    var div_id = "cluster-state-" +  id;
    getClusterInfoByAddress(address, function(obj){
        var state = obj.res.cluster_state;
        if(state == "ok"){
            $("#" + div_id).append( "<span class='state-ok'>OK</span>" );
        } else {
            $("#" + div_id).append( "<span class='state-fail'>Fail</span>" );
        }

    });
});

$(document).on("click", "#add-cluster", function(){
    smarty.open( "cluster/add_cluster_model", {}, { title: "Add New Cluster", width:400, height:345},function(){

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



$("#clusterName, #address, #userName, #password").on("blur", function(){
    var obj = $(this);
    var val = obj.val();
    if(val != null && val != ""){
        obj.removeClass("input-error");
    }
})

// save redis cluster
$("#save").on("click", function(){
    var clusterNameObj = $("#clusterName");
    var addressObj = $("#address");
    var groupIdObj = $("#group");
    var redisPasswordObj = $("#redisPassword");

    var clusterName = clusterNameObj.val().trim();
    var address = addressObj.val().trim();
    var userGroup = $("#group option:selected").val().trim();
    var redisPassword = redisPasswordObj.val().trim();

    if(clusterName == null || clusterName == ""){
        clusterNameObj.addClass("input-error");
        layer.msg("Cluster Name can't be null");
        return;
    } else {
        clusterNameObj.removeClass("input-error");
    }

    if(address == null || address == ""){
        addressObj.addClass("input-error");
        layer.msg("IP can't be null");
        return;
    }

    if(userGroup == null || userGroup == "" || userGroup == 0){
        layer.msg("Please select a group");
        return;
    }


    var cluster = {};
    cluster.clusterName = clusterName;
    cluster.address = address;
    cluster.userGroup =  userGroup;
    cluster.clusterType = "machine";
    cluster.redisPassword = redisPassword;
    addCluster(cluster, function(obj){
        location.reload();
    });

    $(".no-machine").show();
    /*var userNameObj = $("#userName");
    var passwordObj = $("#password");

    var userName = userNameObj.val().trim();
    var password = passwordObj.val().trim();

    if(userName == null || userName == ""){
        userNameObj.addClass("input-error");
        layer.msg("User name can't be null");
        return;
    } else {
        userNameObj.removeClass("input-error");
    }

    if(password == null || password == ""){
        passwordObj.addClass("input-error");
        layer.msg("Password can't be null");
        return;
    } else {
        passwordObj.removeClass("input-error");
    }*/

    // TODO:ajax request 成功或失败
})


/*delete redis cluster*/
$("body").delegate(".delete-container","click", function(){
    var deleteObj = $(this).parent().parent();
    var clusterId = $(this).attr("data-cluster-id");
    var clusterName = deleteObj.attr("data");
    bootbox.prompt({
        title: "Please enter the cluster name what you will delete\n["+clusterName+"]",
        buttons: {
            confirm: {
                label: 'Delete',
                className: 'btn-danger'
           },
            cancel: {
                label: 'Cancel'
            }
        },
        callback: function (result){

            console.log("clusterId" + clusterId)
            if(result != null && result != "") {
                if(result == clusterName) {
                    //TODO: ajax request
                    $.ajax({
                        type: "POST",
                        url: "/cluster/removeCluster?clusterId="+clusterId,
                        dataType: "json",
                        contentType : "application/json; charset=utf-8",
                        success: function(result){
                            console.log(result);
                            var code = parseInt(result.code);
                            if(code == 0) {
                                 deleteObj.remove();
                                 layer.msg("delete " + clusterName + " successfully")
                                 /* test pull request conflict*/
                            } else {
                                layer.msg("delete " + clusterName + " failed")
                            }
                        },
                        error: function(error){
                            alert("error");
                            console.log(error);
                        }
                    })
                } else {
                    layer.msg("cluster name wrong")
                }
            }
        }
    })
});

