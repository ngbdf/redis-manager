var Script = function () {



//    sidebar dropdown menu

    jQuery('#sidebar .sub-menu > a').click(function () {
        var last = jQuery('.sub-menu.open', $('#sidebar'));
        last.removeClass("open");
        jQuery('.arrow', last).removeClass("open");
        jQuery('.sub', last).slideUp(200);
        var sub = jQuery(this).next();
        if (sub.is(":visible")) {
            jQuery('.arrow', jQuery(this)).removeClass("open");
            jQuery(this).parent().removeClass("open");
            sub.slideUp(200);
        } else {
            jQuery('.arrow', jQuery(this)).addClass("open");
            jQuery(this).parent().addClass("open");
            sub.slideDown(200);
        }
        var o = ($(this).offset());
        diff = 200 - o.top;
        if(diff>0)
            $("#sidebar").scrollTo("-="+Math.abs(diff),500);
        else
            $("#sidebar").scrollTo("+="+Math.abs(diff),500);
    });

//    sidebar toggle


    /*$(function() {
        function responsiveView() {
            var wSize = $(window).width();
            if (wSize <= 768) {
                $('#container').addClass('sidebar-close');
                $('#sidebar > ul').hide();
            }

            if (wSize > 768) {
                $('#container').removeClass('sidebar-close');
                $('#sidebar > ul').show();
            }
        }
        $(window).on('load', responsiveView);
        $(window).on('resize', responsiveView);
    });*/

	/*失效*/
    $('.icon-reorder').click(function () {
        if ($('#sidebar > ul').is(":visible") === true) {
        	$('.wrapper').animate({'left': '0px'}, 200);
        	$('#sidebar').animate({'margin-left': '-180px'}, 200, function(){
        		$('#sidebar > ul').hide();
        	})
            /*$('#main-content').css({'margin-left': '0px'});
            $('#sidebar').css({'margin-left': '-180px'});
            $('#sidebar > ul').hide();*/
            $("#container").addClass("sidebar-closed");
        } else {
        	$('.wrapper').animate({'left': '180px'}, 200);
            /*$('#main-content').css({'margin-left': '180px'});*/
            $('#sidebar > ul').show();
            $('#sidebar').animate({'margin-left': '0'}, 200);
            /*$('#sidebar').css({'margin-left': '0'});*/
            $("#container").removeClass("sidebar-closed");
        }
    });
    
    

	$('#icon-reorder').click(function () {
        if ($('#sidebar > ul').is(":visible") === true) {
        	$('#main-content').animate({'margin-left': '0px'}, 200);
        	$('#sidebar').animate({'margin-left': '-180px'}, 200, function(){
        		$('#sidebar > ul').hide();
        	})
            $("#main-content").addClass("sidebar-closed");
        } else {
            
            $('#sidebar').animate({'margin-left': '0'}, 200);
            $('#main-content').animate({'margin-left': '180px'}, 200);
            $('#sidebar > ul').show();
            $("#main-content").removeClass("sidebar-closed");
        }
	});
// custom scrollbar
    $("#sidebarD").niceScroll({styler:"fb",cursorcolor:"#e8403f", cursorwidth: '3', cursorborderradius: '10px', background: '#404040', cursorborder: ''});

    $("html").niceScroll({styler:"fb",cursorcolor:"#e8403f", cursorwidth: '6', cursorborderradius: '10px', background: '#404040', cursorborder: '', zindex: '1000'});

// widget tools

    jQuery('.widget .tools .icon-chevron-down').click(function () {
        var el = jQuery(this).parents(".widget").children(".widget-body");
        if (jQuery(this).hasClass("icon-chevron-down")) {
            jQuery(this).removeClass("icon-chevron-down").addClass("icon-chevron-up");
            el.slideUp(200);
        } else {
            jQuery(this).removeClass("icon-chevron-up").addClass("icon-chevron-down");
            el.slideDown(200);
        }
    });

    jQuery('.widget .tools .icon-remove').click(function () {
        jQuery(this).parents(".widget").parent().remove();
    });

//    tool tips

    $('.tooltips').tooltip();

//    popovers

    $('.popovers').popover();



// custom bar chart

    if ($(".custom-bar-chart")) {
        $(".bar").each(function () {
            var i = $(this).find(".value").html();
            $(this).find(".value").html("");
            $(this).find(".value").animate({
                height: i
            }, 2000)
        })
    }

	// select menu
	$(".sidebar-menu li").on("click", function(){
		var obj = $(this).addClass("active").siblings();
		obj.removeClass("active");
		obj.find("li").removeClass("active");
	})

//custom select box

//    $(function(){
//
//        $('select.styled').customSelect();
//
//    });



}();