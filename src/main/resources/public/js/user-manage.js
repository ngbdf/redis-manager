$(function() {
   // 加载数据
   // getAllUsers();
});

$("#addUser").on("click", function() {
    $("#myModal .modal-input").val("");
    $('#myModal').modal('show');
})

$(".updateUser").on("click", function() {

    var userID = $(this).attr("data");
    $('#myModal').modal('show');
    /*if(userID != null && userID != '') {
       // TODO: Ajax 请求后台获取单个 user 信息，填充到 modal 表单中
       $('#myModal').modal('show');
     } else {
        layer.msg("Get user data error.");
     }*/

})

$("#confirm").on("click",  function() {
    // TODO: 获取表单数据，请求后台,刷新当前页

})

$(".deleteUser").on("click", function() {
    layer.confirm('Confirm to delete this user?', {
      title: 'Delete',
      btn: ['Delete','Cancel'], //按钮
      skin: 'danger'
    }, function(){
         var userID = $(this).attr("data");
         if(userID != null && userID != '') {
            // TODO: 请求后台删除
         }

    }, function(){
        // cancel
    });
})

function getAllUsers() {
    $.ajax({
        type: "POST",
        url: "/user/getAllUsers",
        success: function(result) {
            // user model list
            var userList = result;
            if(userList != null) {
                buildUserTable(userList);
            }
        }
    })
}

function buildUserTable(userList) {
    var size = userList.length;
    var tbody = $("#userTable").find("tbody");
    var content;
    for(var index = 0; index < size; index++) {
        var num = index + 1;
        var user = userList[index];
        var userID = user.id;
        var group = user.userGroup;
        var userName = user.username;
        var password = user.password;
        content += '<tr>' +
                        '<td>' + num + '</td>' +
                        '<td>' + group + '</td>' +
                        '<td>' + userName + '</td>' +
                        '<td class="hidden-phone">' + password + '</td>' +
                        '<td>' +
                        '    <button class="btn btn-primary btn-xs updateUser" data="' + userID + '"><i class="icon-pencil" title="update user"></i></button>' +
                        '    <button class="btn btn-danger btn-xs deleteUser" data="' + userID + '"><i class="icon-trash "  title="delete user"></i></button>' +
                        '</td>' +
                    '</tr>';
    }
    tbody.html(content);
}
