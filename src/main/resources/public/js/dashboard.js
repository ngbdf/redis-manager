$(function(){
	/*select menu li*/
	$(".active-list li").on('click', function() {
		$(this).addClass("active").siblings().removeClass("active")
	})
	
	/*show or hide sidebar*/
	$(".sidebar-icon").on("click", function(){
		var object = $(".sidebar-icon");
		var isHide = object.attr("isHide");
		if(isHide == 0) {
			object.attr("isHide", "1");
			$(".sidebar").addClass("small-sidebar");
			$(".main-panel").animate({left: '100px'}, 200);
			$(".main-panel").css("width", "calc(100% - 100px)");
			$(".sidebar").animate({width: '100px'}, 200);
			$(".sidebar ul p").hide(300);
			$(".logo").html('<img src="../img/redis.svg" class="logo-img"/>');
		} else {
			object.attr("isHide", "0");
			$(".sidebar").removeClass("small-sidebar");
			$(".logo").html('<p>Redis Manager</p>');
			$(".sidebar").animate({width: '260px'}, 200);
			$(".main-panel").animate({left: '260px'}, 200);
			$(".main-panel").css("width", "calc(100% - 260px)");
			$(".sidebar ul p").show(300);
		}
	})
})

/*
function resizeIframeParentHeight() {
	var h = $('body').outerHeight();
	alert(h);
	$(".content-page", window.parent.document).css('height', $('body').outerHeight());
}*/
