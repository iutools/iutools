$(document).ready(function() {

  $(".menu-toggle").click(function(e) {
    e.stopPropagation();
    $(this).toggleClass("active").next("#main_nav_menu").slideToggle();
		selfActive = $(this).hasClass("active")?true:false;
		if (selfActive) {
      $(".drop-menu-toggle").removeClass("active").next(".drop-menu").slideUp();
		}
	});

  $(".drop-menu-toggle").click(function(e) {
    e.stopPropagation();
        $(".drop-menu-toggle").not($(this)).removeClass("active").next(".drop-menu").slideUp();
		$(this).toggleClass("active").next(".drop-menu").slideToggle();
	});

})

$(document).click(function() {
  $(".drop-menu-toggle").removeClass("active").next(".drop-menu").slideUp();
});

$(window).bind('resize orientationchange', function() {
  $(".menu-toggle.active").removeClass("active");
  $(".drop-menu-toggle.active").removeClass("active").next(".drop-menu").hide();
  ww = $("#main_nav").width();
	adjustMenu();
});

var adjustMenu = function() {
  var ww = $("#main_nav").width();
  if (ww >= 540) {
    $("#main_nav_menu").css("display", "flex");
  } else {
    $("#main_nav_menu").css("display", "none");
  }
}