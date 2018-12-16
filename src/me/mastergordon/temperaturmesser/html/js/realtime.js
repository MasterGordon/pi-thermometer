var high = 0
var low = 0
$(function() {
  $.get("/temperature/", function(data) {
    var temp = parseInt(data);
    temp = temp / 1000
    $("#temp").text(temp + "°C")
    updateTemp()
  });
  updateTemp()
})

function updateTemp() {
  setTimeout(function() {
    $.get("/temperature/", function(data) {
      var temp = parseInt(data);
      temp = temp / 1000
      $("#temp").text(temp + "°C")
      updateTemp()
    });
  }, 3000)
}
