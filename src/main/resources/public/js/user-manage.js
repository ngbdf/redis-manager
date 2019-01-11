$(function() {
   // 加载数据
    getAllUsers();
});

$("#addUser").on("click", function() {
    $("#myModal .modal-input").val("");
    $('#myModal').modal('show');
})

function updateUser(userID) {
    $('#myModal').modal('show');
    if(userID != null && userID != '') {
       $('#myModal').modal('show');
           $.ajax({
               type: "GET",
               url: "/user/getUser?id=" + userID ,
               success: function(result) {
                   if(result.code == 0){
                       console.log(result.res);
                       $('#group').val(result.res.userGroup);
                       $('#userName').val(result.res.username);
                       $('#password').val(result.res.password);
                       $('#userId').val(result.res.id);
                   } else {
                        layer.msg("Get user data error.");
                   }
               }
          })
    }

}

$("#confirm").on("click",  function() {

    var group =  $('#group').val();
    var userName = $('#userName').val();
    var password = $('#password').val();
    var userId = $('#userId').val();
    if(group != '' && userName != '' && password != ''){

        var user;
        if(userId != '' && userId != null){
          user = JSON.stringify({
            "userId": userId,
            "group": group,
            "username": userName,
            "password": password
          });
        }else{
          user = JSON.stringify({
            "group": group,
            "username": userName,
            "password": password
          });
        }

        $.ajax({
               type: "POST",
               data: user,
               dataType: "json",
               contentType: "application/json",
               url: "/user/addUser",
               success: function(result) {
                    if(result.code == 0){
                         layer.msg("Add/Update User success.")
                         getAllUsers();
                         $("#myModal .modal-input").val("");
                         $('#myModal').modal('hide');
                    }else{
                         layer.msg("Add/Update User failure.")
                    }

               }
           })

    }else{
       layer.msg("Please input complete data.");
    }
})

function deleteUser(userID) {
    layer.confirm('Confirm to delete this user?', {
      title: 'Delete',
      btn: ['Delete','Cancel'], //按钮
      skin: 'danger'
    }, function(){
         if(userID != null && userID != '') {
             $.ajax({
                    type: "GET",
                    url: "/user/removeUser?id=" + userID ,
                    success: function(result) {
                        if(result.code == 0){
                             layer.msg("Delete User success.")
                             getAllUsers();
                        }else{
                             layer.msg("Delete User failure.")
                        }
                    }
             })
         }

    }, function(){
        // cancel
    });
}

function getAllUsers() {
    $.ajax({
        type: "GET",
        url: "/user/listUser",
        success: function(result) {
          // user model list
          buildUserTable(result.res);
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
                        '    <button class="btn btn-primary btn-xs updateUser" onclick="updateUser(' + userID + ')"><i class="icon-pencil" title="update user"></i></button>' +
                        '    <button class="btn btn-danger btn-xs deleteUser" onclick="deleteUser(' + userID + ')"><i class="icon-trash "  title="delete user"></i></button>' +
                        '</td>' +
                    '</tr>';
    }
    tbody.html(content);


}
