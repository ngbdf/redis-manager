$(document).ready(function(){
    getCluster(window.clusterId, function(obj){
        window.address = obj.res.address;
    })
});

// query dbx
$(document).on("click", "#query-db", function(){
    smarty.fopen( "/cluster/redisDbList?address="+window.address, "monitor/redis_query", true, { title: "Query", width:800, height:500},  function(obj){
        var db = parseInt($(".db-list li").eq(0).find("a").attr("data-db"));
        $("#query-key").attr("data-db", db);
        $(".db-dropdown").html('db' + db + '<span class="caret"></span>');
        query(db)
    } );
});

$("body").delegate(".select-db", "click", function(){
    var db = parseInt($(this).attr("data-db"));
    $(".db-dropdown").html('db' + db + '<span class="caret"></span>');
    $("#query-key").attr("data-db", db);
    query(db)
})

$("body").delegate("#query-key", "click", function(){
    var db = $("#query-key").attr("data-db") || -1;
    if(db == -1){
        db = parseInt($(".db-list li").eq(0).find("a").attr("data-db"));
        $("#query-key").attr("data-db", db);
        $(".db-dropdown").html('db' + db + '<span class="caret"></span>');
        alert("555")
    }
    console.log(db)
    query(db)
})

function query(db){
    var queryKey = $("#query-input").val();
    var redisQueryParam = new Object;
    redisQueryParam.address = window.address;
    redisQueryParam.db = db;
    redisQueryParam.key = queryKey || "*";
    redisQuery(redisQueryParam, function(obj){
        var result = obj.res;
        console.log(result)
        $("#show-result").html(syntaxHighlightRedisResult(result));
    })
}
