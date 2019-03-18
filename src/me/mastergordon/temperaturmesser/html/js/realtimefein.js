var high = 0
var low = 0
$(function() {
  $.get("/feinstaub/", function(data) {
    $("#temp").html(data)
    updateTemp()
  });
  updateTemp()
})

function updateTemp() {
  setTimeout(function() {
    $.get("/feinstaub/", function(data) {
      $("#temp").html(data)
      updateTemp()
    });
  }, 3000)
}
