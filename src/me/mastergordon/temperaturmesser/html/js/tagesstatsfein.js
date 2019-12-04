var temp = {}
var high = 0
var low = 0
$(function() {
  var date = new Date();
  date.setHours(0)
  date.setMinutes(0)
  $.get("/feinstaub/" + date.valueOf() + "/" + new Date().valueOf(), function(data) {
    var temp = JSON.parse(data);
    temp.labels.reverse();
    temp.data1.reverse();
    temp.data2.reverse();
    console.log(temp);
    var ctx = "myChart";
    var chart = new Chart(ctx, {
      "type": "line",
      "data": {
        labels: temp.labels,
        datasets: [{
          label: "PM2.5 in µg/m³",
          data: temp.data1,
          pointRadius: 0,
          borderWidth: 1
        },{
          label: "PM10 in µg/m³",
          data: temp.data2,
          pointRadius: 0,
          borderWidth: 1
        }]
      }
    })
  });
})
