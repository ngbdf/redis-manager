$("#sync-ajax").click(function(){
    ajax.post("/demo/ajaxPost", JSON.stringify({"code": 0}),function(res){
        console.log(res);
    },true);
});

$("#async-ajax").click(function(){
    ajax.async_post("/demo/ajaxPost", {"code": 0},function(res){console.log(res);});
});


$("#ajax-post-info").click(function(){
    ajax.async_post("/demo/ajaxPost", {"code": -1},function(res){console.log(res);});
});

$("#ajax-post-warn").click(function(){
    ajax.async_post("/demo/ajaxPost", {"code": 2},function(res){console.log(res);});
});

$("#ajax-post-error").click(function(){
    ajax.async_post("/demo/ajaxPost", {"code": 1},function(res){console.log(res);});
});

$("#ajax-post-default").click(function(){
    ajax.async_post("/demo/ajaxPost", {"code": 0},function(res){console.log(res);});
});

$("#async-get").click(function(){
    ajax.async_get("/demo/ajaxGet?code=2", function(res){console.log(res);});
});




